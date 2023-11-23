// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package com.braintribe.gm.marshaller.processing;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.codec.marshaller.api.EntityFactory;
import com.braintribe.codec.marshaller.api.EntityRecurrenceDepth;
import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.codec.marshaller.api.MarshallerRegistry;
import com.braintribe.codec.marshaller.api.OutputPrettiness;
import com.braintribe.codec.marshaller.api.TypeExplicitness;
import com.braintribe.codec.marshaller.api.TypeExplicitnessOption;
import com.braintribe.gm.model.marshaller.api.data.MarshallQualification;
import com.braintribe.gm.model.marshaller.api.request.AbstractMarshallRequest;
import com.braintribe.gm.model.marshaller.api.request.ExecuteAndMarshallResponse;
import com.braintribe.gm.model.marshaller.api.request.MarshallAccessData;
import com.braintribe.gm.model.marshaller.api.request.MarshallData;
import com.braintribe.gm.model.marshaller.api.request.UnmarshallAccessData;
import com.braintribe.gm.model.marshaller.api.request.UnmarshallData;
import com.braintribe.mimetype.MimeTypeDetector;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.impl.AbstractDispatchingServiceProcessor;
import com.braintribe.model.processing.service.impl.DispatchConfiguration;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.utils.DateTools;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.stream.CountingOutputStream;
import com.braintribe.utils.stream.api.StreamPipe;
import com.braintribe.utils.stream.api.StreamPipeFactory;

public class MarshallerProcessor extends AbstractDispatchingServiceProcessor<AbstractMarshallRequest, Object> {
	private MarshallerRegistry marshallerRegistry;
	private StreamPipeFactory streamPipeFactory;
	private Function<String, String> mimeTypeExtensionResolver;
	private MimeTypeDetector mimeTypeDetector;
	private static final DateTimeFormatter DATE_SUFFIX_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
	private PersistenceGmSessionFactory sessionFactory;
	
	@Override
	protected void configureDispatching(DispatchConfiguration<AbstractMarshallRequest, Object> dispatching) {
		dispatching.register(ExecuteAndMarshallResponse.T, this::executeAndMarshallResponse);
		dispatching.register(MarshallData.T, this::marshallData);
		dispatching.register(UnmarshallData.T, this::unmarshallData);

// 		TODO: complete this preliminary access data requests
//		dispatching.register(MarshallAccessData.T, this::marshallAccessData);
//		dispatching.register(UnmarshallAccessData.T, this::unmarshallAccessData);
	}
	
	@Required
	@Configurable
	public void setSessionFactory(PersistenceGmSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	@Required
	@Configurable
	public void setMimeTypeDetector(MimeTypeDetector mimeTypeDetector) {
		this.mimeTypeDetector = mimeTypeDetector;
	}
	
	@Required
	@Configurable
	public void setMimeTypeExtensionResolver(Function<String, String> mimeTypeExtensionResolver) {
		this.mimeTypeExtensionResolver = mimeTypeExtensionResolver;
	}
	
	@Required
	@Configurable
	public void setMarshallerRegistry(MarshallerRegistry marshallerRegistry) {
		this.marshallerRegistry = marshallerRegistry;
	}
	
	@Required
	@Configurable
	public void setStreamPipeFactory(StreamPipeFactory streamPipeFactory) {
		this.streamPipeFactory = streamPipeFactory;
	}
	
	private Resource marshallAccessData(ServiceRequestContext requestContext, MarshallAccessData request) {
		String accessId = request.getDomainId();
		
		if (accessId == null)
			throw new IllegalStateException(request.entityType().getShortName() + ".domainId must not be null");
			
		PersistenceGmSession session = sessionFactory.newSession(accessId);
		
		Object mergedData = session.merge().keepEnvelope(true).suspendHistory(true).doFor(request.getData());
		
		// TODO preload data to boost marshalling
		// EntityCollector entityCollector = new EntityCollector();
		// entityCollector
		
		// TODO implement marshalling
		
		return null;
	}
	
	private Resource marshallData(ServiceRequestContext requestContext, MarshallData request) {
		validate(MarshallData.T, request);
		
		Object value = request.getData();
		String mimeType = request.getMimeType();
		
		Marshaller marshaller = marshallerRegistry.getMarshaller(mimeType);

		GmSerializationOptions options = GmSerializationOptions.deriveDefaults().setOutputPrettiness(Optional.ofNullable(request.getPrettiness()).orElse(OutputPrettiness.mid))
	    	.stabilizeOrder(request.getStabilizeOrder())
	    	.writeAbsenceInformation(request.getWriteAbsenceInformation())
	    	.writeEmptyProperties(request.getWriteEmptyProperties())
	    	.set(TypeExplicitnessOption.class, Optional.ofNullable(request.getTypeExplicitness()).orElse(TypeExplicitness.auto))
	    	.set(EntityRecurrenceDepth.class, Optional.ofNullable(request.getEntityRecurrenceDepth()).orElse(0))
	    	.build();
		
		StreamPipe streamPipe = streamPipeFactory.newPipe("marshall-response");

		GenericModelType type = GMF.getTypeReflection().getType(value);
		Date now = new Date();
		
		String typeSuffix = StringTools.camelCaseToDashSeparated(StringTools.findSuffix(type.getTypeName(), "."));
		String dateSuffix = DateTools.encode(now, DATE_SUFFIX_FORMATTER);
		
		Resource resource = Resource.createTransient(streamPipe::openInputStream);
		resource.setCreated(now);
		resource.setCreator(requestContext.getRequestorUserName());
		resource.setMimeType(mimeType);
		resource.setName(Optional.ofNullable(request.getResourceName()).orElse("marshalled-" + typeSuffix + "-" + dateSuffix + "." + mimeTypeExtensionResolver.apply(mimeType)));

		try (CountingOutputStream out = new CountingOutputStream(streamPipe.openOutputStream())) {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			
			try (DigestOutputStream digestOut = new DigestOutputStream(out, digest)) {
				marshaller.marshall(digestOut, value, options);
			}

			resource.setFileSize(out.getCount());
			resource.setMd5(StringTools.toHex(digest.digest()));
			
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}

		return resource;

	}
	
	private <S extends GenericEntity, T extends S> T createFrom(EntityType<S> sourceEntityType, EntityType<T> targetEntityType, S prototype) {
		T instance = targetEntityType.create();
		
		for (Property property: sourceEntityType.getProperties()) {
			if (property.getDeclaringType() == GenericEntity.T)
				continue;
			
			property.set(instance, property.get(prototype));
		}
		
		return instance;
	}
	
	private Object unmarshallAccessData(@SuppressWarnings("unused") ServiceRequestContext requestContext, UnmarshallAccessData request) {
		String accessId = request.getDomainId();
		Resource resource = request.getResource();

		// validate
		if (accessId == null)
			throw new IllegalStateException(request.entityType().getShortName() + ".domainId must not be null");
		
		if (resource == null)
			throw new IllegalArgumentException("Unmarshall.resource may not be null");
		
		// unmarshall 
		PersistenceGmSession session = sessionFactory.newSession(accessId);
		
		Object unmarshalledData = unmarshall(resource, session::createRaw);
		
		session.commit();
		
		return unmarshalledData;
	}
	
	private Object unmarshallData(@SuppressWarnings("unused") ServiceRequestContext requestContext, UnmarshallData request) {
		Resource resource = request.getResource();
		
		if (resource == null)
			throw new IllegalArgumentException("Unmarshall.resource may not be null");
		
		return unmarshall(resource, EntityType::createRaw);
	}
	
	private Object unmarshall(Resource resource, Function<EntityType<?>, GenericEntity> entityFactory) {
		String mimeType = resource.getMimeType();
		
		if (mimeType == null) {
			try (InputStream in = resource.openStream()) {
				mimeType = mimeTypeDetector.getMimeType(in, resource.getName());
			} catch (IOException e) {
				throw new UncheckedIOException("Error while detecting mimetype for resource: " + resource, e);
			}
		}

		Marshaller marshaller = marshallerRegistry.getMarshaller(mimeType);
		
		if (marshaller == null)
			throw new NoSuchElementException("No marshaller registered for mimeType: " + mimeType);

		try (InputStream in = resource.openStream()) {
			return marshaller.unmarshall(in, GmDeserializationOptions.deriveDefaults().set(EntityFactory.class, entityFactory).build());
		} catch (IOException e) {
			throw new UncheckedIOException("Error while unmarshalling resource: " + resource, e);
		}
	}
	
	private void validate(EntityType<? extends MarshallQualification> entityType, MarshallQualification qualification) {
		String mimeType = qualification.getMimeType();
		
		if (mimeType == null)
			throw new IllegalArgumentException(entityType.getShortName() + ".mimeType may not be null");

		Marshaller marshaller = marshallerRegistry.getMarshaller(mimeType);
		if (marshaller == null)
			throw new NoSuchElementException("No marshaller registered for mimeType: " + mimeType);

	}
	
	private Resource executeAndMarshallResponse(ServiceRequestContext requestContext, ExecuteAndMarshallResponse request) {
		validate(ExecuteAndMarshallResponse.T, request);
		
		ServiceRequest serviceRequest = request.getServiceRequest();

		if (serviceRequest == null)
			throw new IllegalArgumentException("ExecuteAndMarshallResponse.serviceRequest may not be null");
		
		Object response = serviceRequest.eval(requestContext).get();
		
		MarshallData marshall = createFrom(MarshallQualification.T, MarshallData.T, request);
		marshall.setData(response);
		
		return marshall.eval(requestContext).get();
	}
}
