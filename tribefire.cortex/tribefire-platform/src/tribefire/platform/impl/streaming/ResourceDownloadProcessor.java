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
package tribefire.platform.impl.streaming;

import java.util.Collections;
import java.util.Set;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.exception.AuthorizationException;
import com.braintribe.logging.Logger;
import com.braintribe.model.extensiondeployment.BinaryRetrieval;
import com.braintribe.model.extensiondeployment.meta.StreamWith;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.meta.data.prompt.Visible;
import com.braintribe.model.processing.accessrequest.api.AbstractDispatchingAccessRequestProcessor;
import com.braintribe.model.processing.accessrequest.api.AccessRequestContext;
import com.braintribe.model.processing.accessrequest.api.DispatchConfiguration;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.service.api.ResponseConsumerAspect;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.source.ResourceSource;
import com.braintribe.model.resourceapi.base.ResourceRequest;
import com.braintribe.model.resourceapi.stream.BinaryRetrievalResponse;
import com.braintribe.model.resourceapi.stream.DownloadResource;
import com.braintribe.model.resourceapi.stream.DownloadSource;
import com.braintribe.model.resourceapi.stream.GetBinary;
import com.braintribe.model.resourceapi.stream.GetBinaryResponse;
import com.braintribe.model.resourceapi.stream.GetResource;
import com.braintribe.model.resourceapi.stream.GetSource;
import com.braintribe.model.resourceapi.stream.StreamBinary;
import com.braintribe.model.resourceapi.stream.StreamBinaryResponse;
import com.braintribe.model.resourceapi.stream.StreamResource;
import com.braintribe.model.resourceapi.stream.StreamSource;
import com.braintribe.model.service.api.ServiceRequest;

/**
 * <p>
 * Central processor of {@link DownloadResource} requests.
 *
 */
public class ResourceDownloadProcessor extends AbstractDispatchingAccessRequestProcessor<ResourceRequest, BinaryRetrievalResponse> {

	private static final Logger log = Logger.getLogger(ResourceDownloadProcessor.class);
	
	private Evaluator<ServiceRequest> systemEvaluator;

	private static Set<String> systemAccesses = Collections.singleton("cortex");

	@Configurable
	@Required
	public void setSystemEvaluator(Evaluator<ServiceRequest> systemEvaluator) {
		this.systemEvaluator = systemEvaluator;
	}
	
	@Override
	protected void configureDispatching(DispatchConfiguration dispatching) {
		dispatching.register(GetResource.T, this::get);
		dispatching.register(StreamResource.T, this::stream);

//		TODO: These yet unused requests don't work with the new security. Find a way to support them again or delete them  
//		dispatching.register(GetSource.T, this::getSource);
//		dispatching.register(StreamSource.T, this::streamSource);
	}

	private GetBinaryResponse get(AccessRequestContext<GetResource> context) {
		GetResource getResource = context.getOriginalRequest();
		Resource resource = getResource(context);

		String serviceId = resolveBinaryRetrieval(context.getSession(), resource);

		GetBinary getBinary = GetBinary.T.create();
		getBinary.setServiceId(serviceId);
		getBinary.setResource(resource);
		getBinary.setCondition(getResource.getCondition());
		getBinary.setRange(getResource.getRange());
		getBinary.setDomainId(getResource.getDomainId());

		return getBinary.eval(systemEvaluator).get();
	}

	private Resource getResource(AccessRequestContext<? extends DownloadResource> context) {
		// The resource needs to be queried again because the caller might send invalid or malicious parts with the request, i.e. the ResourceSource
		Resource requestResource = context.getOriginalRequest().getResource();
		
		if (!systemAccesses.contains(context.getDomainId())){

			EntityQuery query = null;
			if (requestResource.getPartition() == null) {
				query = EntityQueryBuilder.from(Resource.T).where().property(GenericEntity.id).eq(requestResource.getId()).done();
			} else {
				//@formatter:off
				query = EntityQueryBuilder.from(Resource.T).where()
							.conjunction()
								.property(GenericEntity.id).eq(requestResource.getId())
								.property(GenericEntity.partition).eq(requestResource.getPartition())
							.close()
						.done();
				//@formatter:on
			}
			return context.getSession().query().entities(query).unique();
			
		}
		
		// Because system resources like icons are stored in the otherwise restricted cortex access (or eventual other system accesses) there must be a way for common users to access them:
		Resource systemResource = context.getSystemSession().query().entity(requestResource).require();
		
		ModelAccessory userModelAccesory = context.getSession().getModelAccessory();
		
		if (!userModelAccesory.getMetaData().entity(systemResource).is(Visible.T)) {
			throw new AuthorizationException("Requested Resource with id '" + systemResource.getId() + "' is not visible for current user '" + context.getRequestorUserName() + "'");
		}
		
		return systemResource;
		
	}

	private StreamBinaryResponse stream(AccessRequestContext<StreamResource> context) {
		StreamResource downloadResource = context.getOriginalRequest();
		Resource resource = getResource(context);
		
		String serviceId = resolveBinaryRetrieval(context.getSession(), resource);
		
		StreamBinary streamBinary = StreamBinary.T.create();
		streamBinary.setServiceId(serviceId);
		streamBinary.setResource(resource);
		streamBinary.setCondition(downloadResource.getCondition());
		streamBinary.setCapture(downloadResource.getCapture());
		streamBinary.setRange(downloadResource.getRange());
		streamBinary.setDomainId(downloadResource.getDomainId());

		return streamBinary.eval(systemEvaluator) //
				.with(ResponseConsumerAspect.class, context::notifyResponse) //
				.get();
	}
	
	private GetBinaryResponse getSource(AccessRequestContext<GetSource> context) {
		GetSource getSource = context.getRequest();
		GetResource delegateRequest = delegateRequest(getSource, GetResource.T);
		
		GetBinaryResponse getBinaryResponse = delegateRequest.eval(context.getSession()).get();
		return getBinaryResponse;
	}
	
	private StreamBinaryResponse streamSource(AccessRequestContext<StreamSource> context) {
		StreamSource streamSource = context.getRequest();
		StreamResource delegateRequest = delegateRequest(streamSource, StreamResource.T);
		delegateRequest.setCapture(streamSource.getCapture());
		
		return delegateRequest.eval(context.getSession()) //
				.with(ResponseConsumerAspect.class, context::notifyResponse) //
				.get();
	}

	protected String resolveBinaryRetrieval(PersistenceGmSession session, Resource resource) {
		if (resource.getResourceSource() == null) {
			throw new GmSessionException("Unable to retrieve a streaming processor. Resource has no source: " + resource);
		}

		EntityType<? extends ResourceSource> sourceType = resource.getResourceSource().entityType();

		// @formatter:off
		StreamWith streamWith =
				session
					.getModelAccessory()
						.getCmdResolver()
							.getMetaData()
							.entityType(sourceType)
							.useCase(resource.getResourceSource().getUseCase())
							.meta(StreamWith.T)
							.exclusive();
		// @formatter:on

		if (streamWith == null) {
			throw new GmSessionException("Unable to retrieve the streaming processor. No " + StreamWith.T.getTypeSignature()
					+ " metadata resolved for source type " + sourceType.getTypeSignature());
		}

		BinaryRetrieval retrieval = streamWith.getRetrieval();

		if (retrieval == null) {
			throw new GmSessionException("Unable to retrieve the streaming processor. No " + BinaryRetrieval.T.getTypeSignature()
					+ " associated with metadata: " + streamWith);
		}

		log.trace(() -> "Resolved binary retrieval processor " + retrieval + " for source type " + sourceType.getTypeSignature());

		return retrieval.getExternalId();

	}

	private <T extends DownloadResource> T delegateRequest(DownloadSource originalRequest, EntityType<T> delegateRequestType) {
		Resource resource = Resource.T.create();
		resource.setResourceSource(originalRequest.getResourceSource());

		T delegateRequest = delegateRequestType.create();
		delegateRequest.setResource(resource);
		delegateRequest.setRange(originalRequest.getRange());
		delegateRequest.setCondition(originalRequest.getCondition());
		
		return delegateRequest;
	}

	
}
