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
package tribefire.extension.audit.processing;


import java.io.IOException;
import java.util.Date;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.codec.marshaller.api.MarshallerRegistry;
import com.braintribe.codec.marshaller.api.OutputPrettiness;
import com.braintribe.common.attribute.AttributeContext;
import com.braintribe.exception.Exceptions;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.essential.InternalError;
import com.braintribe.logging.Logger;
import com.braintribe.model.accessapi.ManipulationRequest;
import com.braintribe.model.extensiondeployment.ServiceProcessor;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.ConfigurableCloningContext;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.clone.AbstractDirectCloning;
import com.braintribe.model.processing.clone.CloneTarget;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.service.api.ProceedContext;
import com.braintribe.model.processing.service.api.ReasonedServiceAroundProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.api.aspect.RequestEvaluationIdAspect;
import com.braintribe.model.processing.service.api.aspect.RequestorAddressAspect;
import com.braintribe.model.processing.service.api.aspect.RequestorUserNameAspect;
import com.braintribe.model.processing.session.api.managed.ModelAccessoryFactory;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.result.ResponseEnvelope;
import com.braintribe.model.service.api.result.ServiceResult;
import com.braintribe.model.service.api.result.Unsatisfied;
import com.braintribe.utils.collection.impl.AttributeContexts;

import tribefire.extension.audit.model.ServiceAuditRecord;
import tribefire.extension.audit.model.deployment.meta.AuditDataPreservation;
import tribefire.extension.audit.model.deployment.meta.AuditPreservationDepth;
import tribefire.extension.audit.model.deployment.meta.Audited;
import tribefire.extension.audit.model.deployment.meta.CreateServiceAuditRecordWith;
import tribefire.extension.audit.model.deployment.meta.ServiceAuditPreservations;
import tribefire.extension.audit.model.service.audit.api.CreateServiceAuditRecord;

public class ServiceAuditInterceptor implements ReasonedServiceAroundProcessor<ServiceRequest, Object> {
	private static final Logger logger = Logger.getLogger(ServiceAuditInterceptor.class);
	private ModelAccessoryFactory modelAccessoryFactory;
	private Supplier<String> userNameProvider = AttributeContextValueSupplier.of(RequestorUserNameAspect.class);
	private Supplier<String> userIpAddressProvider = AttributeContextValueSupplier.of(RequestorAddressAspect.class);
	private String auditAccessId;
	private Evaluator<ServiceRequest> systemEvaluator;
	private PersistenceGmSessionFactory systemSessionFactory;
	private MarshallerRegistry marshallerRegistry;

	@Configurable
	public void setAuditAccessId(String auditAccessId) {
		this.auditAccessId = auditAccessId;
	}
	
	@Required @Configurable
	public void setMarshallerRegistry(MarshallerRegistry marshallerRegistry) {
		this.marshallerRegistry = marshallerRegistry;
	}
	
	@Required @Configurable
	public void setSystemSessionFactory(PersistenceGmSessionFactory systemSessionFactory) {
		this.systemSessionFactory = systemSessionFactory;
	}
	
	@Required @Configurable
	public void setModelAccessoryFactory(ModelAccessoryFactory modelAccessoryFactory) {
		this.modelAccessoryFactory = modelAccessoryFactory;
	}
	
	@Configurable
	public void setUserNameProvider(Supplier<String> userNameProvider) {
		this.userNameProvider = userNameProvider;
	}
	
	@Configurable
	public void setUserIpAddressProvider(Supplier<String> userIpAddressProvider) {
		this.userIpAddressProvider = userIpAddressProvider;
	}
	
	@Required @Configurable
	public void setSystemEvaluator(Evaluator<ServiceRequest> systemEvaluator) {
		this.systemEvaluator = systemEvaluator;
	}

	@Override
	public Maybe<? extends Object> processReasoned(ServiceRequestContext context, ServiceRequest request,
			ProceedContext proceedContext) {
		return new StatefulAuditedServiceProcessing(context, proceedContext, request).process();
	}
	
	private class StatefulAuditedServiceProcessing {
		private ServiceRequestContext context;
		private ProceedContext proceedContext;
		private ServiceRequest request;
		private CmdResolver cmdResolver;
		private Date started;
		private String domainId;
		private PersistenceGmSession session;
		
		public StatefulAuditedServiceProcessing(ServiceRequestContext context, ProceedContext proceedContext,
				ServiceRequest request) {
			super();
			this.context = context;
			this.proceedContext = proceedContext;
			this.request = request;
		}
		
		public Maybe<? extends Object> process() {
			domainId = context.getDomainId();
			
			// check if auditing should happen at all
			cmdResolver = modelAccessoryFactory.getForServiceDomain(domainId).getCmdResolver();
			
			Audited audited = cmdResolver.getMetaData().entity(request).meta(Audited.T).exclusive();
			if (audited == null || !audited.isTrue() || request instanceof CreateServiceAuditRecord)
				return proceedContext.proceedReasoned(request);
			
			if (request instanceof ManipulationRequest) {
				String auditInterceptorCallId = context.findOrNull(AuditInterceptorCallId.class);
				
				if (auditInterceptorCallId != null) {
					return proceedContext.proceedReasoned(request);
				}
			}
			
			// audit
			started = new Date();
			
			try {
				Maybe<Object> maybe = proceedContext.proceedReasoned(request);
				
				storeRecord(maybe);

				return maybe;
			}
			catch (RuntimeException | Error e) {
				storeRecord(InternalError.create(Exceptions.stringify(e)).asMaybe());
				throw e;
			}
		}
		
		private ServiceAuditPreservations acquireSettings() {
			ServiceAuditPreservations settings = cmdResolver.getMetaData().entity(request).meta(ServiceAuditPreservations.T).exclusive();
			return settings != null? settings: ServiceAuditPreservations.T.create();
		}
		
		private ServiceProcessor getRecordFactory() {
			CreateServiceAuditRecordWith createWith = cmdResolver.getMetaData().entity(request).meta(CreateServiceAuditRecordWith.T).exclusive();
			return createWith != null? createWith.getRecordFactory(): null;
		}

		private void storeRecord(Maybe<?> maybe) {
			try {
				long executionTimeInMs = System.currentTimeMillis() - started.getTime();
				
				ServiceAuditPreservations settings = acquireSettings();
				
				session = systemSessionFactory.newSession(auditAccessId);
				
				ServiceAuditRecord record = initRecord(maybe);

				String user = userNameProvider.get();
				String ip = userIpAddressProvider.get();
				
				String callId = context.findOrNull(RequestEvaluationIdAspect.class);
				String parentCallId = findParentCallId(callId);
				
				record.setDate(started);
				record.setExecutionTimeInMs(executionTimeInMs);
				record.setSatisfied(maybe.isSatisfied());
				record.setUser(user);
				record.setUserIpAddress(ip);
				record.setDomainId(domainId);
				record.setCallId(callId);
				record.setParentCallId(parentCallId);
				record.setRequestType(request.entityType().getTypeSignature());
				
				if (settings.getRequestPreservation() != null) {
					Resource marshalledData = marshallData(settings.getRequestPreservation(), request, "service-request-preservation-" + callId);
					record.setRequest(marshalledData);
				}
				
				if (settings.getResultPreservation() != null) {
					Resource marshalledData = marshallData(settings.getResultPreservation(), resultFromMaybe(maybe),  "service-result-preservation-" + callId);
					record.setResult(marshalledData);
				}
				
				AttributeContexts.push(context.derive().set(AuditInterceptorCallId.class, callId).build());
				try {
					session.commit();
				}
				finally {
					AttributeContexts.pop();
				}
			} catch (Exception e) {
				logger.error("Error while storing audit record for request " + request.entityType().getTypeSignature(), e);
			}
		}

		private String findParentCallId(String callId) {
			AttributeContext parent = context.parent();
			
			while (parent != null) {
				String parentCandidateCallId = parent.findOrNull(RequestEvaluationIdAspect.class);
				
				if (parentCandidateCallId == null)
					return null;
				
				if (!parentCandidateCallId.equals(callId))
					return parentCandidateCallId;
				
				parent = parent.parent();
			}
			
			return null;
		}

		private Resource marshallData(AuditDataPreservation preservation, Object data, String name) throws IOException {
			String mimeType = preservation.getMimeType();
			Marshaller marshaller = marshallerRegistry.getMarshaller(mimeType);
			
			if (preservation.getDepth() == AuditPreservationDepth.shallow) {
				data = new ShallowCloning().cloneValue(data);
			}
			
			// TODO: support embedded StringSource based on marshaller is CharacterMarshaller
			return marshallData(name, data, mimeType, marshaller);
		}

		private Resource marshallData(String name, Object data, String mimeType, Marshaller characterMarshaller) {
			return session.resources().create().mimeType(mimeType).name(name).store(out -> {
				characterMarshaller.marshall(out, data, GmSerializationOptions.deriveDefaults().setOutputPrettiness(OutputPrettiness.mid).build());
			});
		}
		
		private ServiceAuditRecord initRecord(Maybe<?> resultMaybe) {
			ServiceProcessor serviceProcessor = getRecordFactory();
			
			if (serviceProcessor == null)
				return session.create(ServiceAuditRecord.T);

			CreateServiceAuditRecord createRecord = CreateServiceAuditRecord.T.create();
			createRecord.setServiceId(serviceProcessor.getExternalId());
			createRecord.setRequest(request);
			createRecord.setResult(serviceResultFromMaybe(resultMaybe));
			createRecord.setDomainId(domainId);
			
			ServiceAuditRecord serviceAuditRecord = createRecord.eval(systemEvaluator).get();
			
			serviceAuditRecord = serviceAuditRecord.clone(ConfigurableCloningContext.build().supplyRawCloneWith(session).done());
			
			return serviceAuditRecord;
		}
		
		private ServiceResult serviceResultFromMaybe(Maybe<?> maybe) {
			if (maybe.isSatisfied()) {
				ResponseEnvelope responseEnvelope = ResponseEnvelope.T.create();
				responseEnvelope.setResult(maybe.get());
				return responseEnvelope;
			}
			else
				return Unsatisfied.from(maybe);
		}
		
		private Object resultFromMaybe(Maybe<?> maybe) {
			if (maybe.isSatisfied()) {
				return maybe.get();
			}
			else
				return Unsatisfied.from(maybe);
		}
	}
	
	private static class ShallowCloning extends AbstractDirectCloning {

		@Override
		protected CloneTarget acquireCloneTarget(GenericEntity entity) {
			return new ShallowCloneTarget(cloneShallow(entity));
		}
		
		private GenericEntity cloneShallow(GenericEntity ge) {
			EntityType<GenericEntity> et = ge.entityType();
			GenericEntity clone = et.createRaw();
			for (Property p: et.getProperties()) {
				if (p.getType().isScalar() || p.isIdentifier())
					p.setDirect(clone, p.getDirect(ge));
				else {
					if (p.isAbsent(ge))
						p.setAbsenceInformation(clone, GMF.absenceInformation());
					else {
						Object value = p.get(ge);
						
						if (!p.isEmptyValue(value)) {
							if (p.getType().isBase()) {
								GenericModelType actualType = BaseType.INSTANCE.getActualType(value);
								
								if (actualType.isScalar())
									p.set(clone, value);
								else 
									p.setAbsenceInformation(clone, GMF.absenceInformation());
							}
							else {
								p.setAbsenceInformation(clone, GMF.absenceInformation());
							}
							
							
						}
					}
				}
			}
	
			return clone;
		}
	}
	
	private static class ShallowCloneTarget implements CloneTarget {
		private GenericEntity entity;
		
		private ShallowCloneTarget(GenericEntity entity) {
			super();
			this.entity = entity;
		}

		@Override
		public GenericEntity getEntity() {
			return entity;
		}

		@Override
		public boolean shouldCloneTransitively() {
			return false;
		}
	}
}
