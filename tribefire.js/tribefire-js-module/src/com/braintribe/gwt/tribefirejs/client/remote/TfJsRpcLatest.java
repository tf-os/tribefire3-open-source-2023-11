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
package com.braintribe.gwt.tribefirejs.client.remote;

import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.Set;
import java.util.function.Supplier;

import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.genericmodel.client.codec.api.GmDecodingContext;
import com.braintribe.gwt.genericmodel.client.codec.jse.JseCodec;
import com.braintribe.gwt.gmresource.session.GwtSessionResourceSupport;
import com.braintribe.gwt.gmrpc.web.client.GwtGmWebRpcEvaluator;
import com.braintribe.gwt.gmrpc.web.client.StandardDecodingContext;
import com.braintribe.gwt.gmsession.client.CortexTypeEnsurer;
import com.braintribe.gwt.gmsession.client.GwtModelAccessory;
import com.braintribe.gwt.gmsession.client.GwtPersistenceGmSession;
import com.braintribe.model.accessory.GetAccessModel;
import com.braintribe.model.accessory.GetComponentModel;
import com.braintribe.model.accessory.GetServiceModel;
import com.braintribe.model.generic.GmCoreApiInteropNamespaces;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;
import com.braintribe.model.processing.session.api.persistence.AccessDescriptor;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.auth.SessionAuthorization;
import com.braintribe.model.processing.session.impl.persistence.auth.BasicSessionAuthorization;
import com.braintribe.model.service.api.DomainRequest;
import com.braintribe.model.service.api.ExecuteInDomain;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.usersession.UserSession;
import com.braintribe.processing.async.api.JsPromise;
import com.braintribe.provider.Holder;
import com.braintribe.provider.PrototypeBeanProvider;
import com.braintribe.provider.SingletonBeanProvider;

import jsinterop.annotations.JsMethod;

// Calling it Latest to distinguish from the other two TfJsRpc classes. The name is not used in tf.js output anyway.
public class TfJsRpcLatest {

	@JsMethod(name = "connect", namespace = GmCoreApiInteropNamespaces.remote)
	public static ServicesConnection connect(String servicesUrl) {
		return new ServicesConnectionImpl(servicesUrl);
	}

	// ################################################
	// ## . . . . . . ServicesConnection . . . . . . ##
	// ################################################

	@SuppressWarnings("unusable-by-js")
	private static class ServicesConnectionImpl implements ServicesConnection {

		protected final String servicesUrl;

		public ServicesConnectionImpl(String servicesUrl) {
			this.servicesUrl = servicesUrl;
		}

		private final Supplier<GwtGmWebRpcEvaluator> serviceRequestEvaluator = new SingletonBeanProvider<GwtGmWebRpcEvaluator>() {
			@Override
			public GwtGmWebRpcEvaluator create() throws Exception {
				GwtGmWebRpcEvaluator bean = publish(new GwtGmWebRpcEvaluator());
				bean.setServerUrl(addToPath(servicesUrl, "rpc"));
				bean.setSessionIdProvider(sessionIdHolder);
				// TODO this is kind of questionable, the typeEnsurer evaluates AuthorizedRequest (GetMetaModelForTypes)
				bean.setTypeEnsurer(typeEnsurer.get());
				bean.setSendSessionIdExpressive(false);
				return bean;
			}
		};

		private static String addToPath(String a, String b) {
			return a.endsWith("/") ? a + b : a + "/" + b;
		}

		protected final Holder<String> sessionIdHolder = new Holder<>();

		protected final Supplier<CortexTypeEnsurer> typeEnsurer = new SingletonBeanProvider<CortexTypeEnsurer>() {
			@Override
			public CortexTypeEnsurer create() throws Exception {
				CortexTypeEnsurer bean = publish(new CortexTypeEnsurer());
				bean.setEvaluator(serviceRequestEvaluator.get());
				return bean;
			}
		};

		@Override
		public String servicesUrl() {
			return servicesUrl;
		}

		@Override
		public Evaluator<ServiceRequest> evaluator() {
			return serviceRequestEvaluator.get();
		}

		@Override
		public EvaluatorBuilder evaluatorBuilder() {
			return new EvaluatorBuilderImpl(evaluator());
		}

		@Override
		public ServicesSession newSession(UserSession userSession) {
			return new ServicesSessionImpl(servicesUrl, userSession);
		}

	}

	private static class EvaluatorBuilderImpl implements EvaluatorBuilder {
		private final Evaluator<ServiceRequest> evaluator;
		private String domainId;

		public EvaluatorBuilderImpl(Evaluator<ServiceRequest> evaluator) {
			this.evaluator = evaluator;
		}

		@Override
		public EvaluatorBuilder setDefaultDomain(String domainId) {
			this.domainId = domainId;
			return this;
		}

		@Override
		public Evaluator<ServiceRequest> build() {
			Evaluator<ServiceRequest> result = evaluator;

			if (domainId != null)
				result = new DefaultDomainSettingEvaluator(evaluator, domainId);

			return result;
		}
	}

	@SuppressWarnings("unusable-by-js")
	private static class DefaultDomainSettingEvaluator implements Evaluator<ServiceRequest> {
		private final Evaluator<ServiceRequest> delegate;
		private final String domainId;

		public DefaultDomainSettingEvaluator(Evaluator<ServiceRequest> delegate, String domainId) {
			this.delegate = delegate;
			this.domainId = domainId;
		}

		@Override
		public <T> EvalContext<T> eval(ServiceRequest request) {
			request = handle(request);
			return delegate.eval(request);
		}

		private ServiceRequest handle(ServiceRequest request) {
			if (!(request instanceof DomainRequest))
				return request;

			DomainRequest dr = (DomainRequest) request;
			if (dr.getDomainId() != null)
				return request;

			ExecuteInDomain eid = ExecuteInDomain.T.create();
			eid.setDomainId(domainId);
			eid.setServiceRequest(request);

			return eid;
		}
	}

	// ################################################
	// ## . . . . . . . ServicesSession. . . . . . . ##
	// ################################################

	@SuppressWarnings("unusable-by-js")
	private static class ServicesSessionImpl extends ServicesConnectionImpl implements ServicesSession {

		private final UserSession userSession;

		public ServicesSessionImpl(String servicesUrl, UserSession userSession) {
			super(servicesUrl);
			this.userSession = userSession;
			this.sessionIdHolder.accept(userSession.getSessionId());
		}

		@Override
		public String sessionId() {
			return userSession.getSessionId();
		}

		@Override
		public UserSession userSession() {
			return userSession;
		}

		@Override
		public ModelAccessoryBuilder modelAccessory(GmMetaModel model) {
			return new ModelAccessoryBuilderImpl(model);
		}

		class ModelAccessoryBuilderImpl implements ModelAccessoryBuilder {

			private final GmMetaModel model;
			private final Set<String> useCases = newSet();
			private AccessDescriptor accessDescriptor;

			public ModelAccessoryBuilderImpl(GmMetaModel model) {
				this.model = model;
			}

			@Override
			public ModelAccessoryBuilder useCase(String useCase) {
				this.useCases.add(useCase);
				return this;
			}

			@Override
			public ModelAccessoryBuilder useCases(Set<String> useCases) {
				this.useCases.addAll(useCases);
				return this;
			}

			@Override
			public ModelAccessoryBuilder access(String accessId, String accessDenotationType) {
				return accessDescriptor(new AccessDescriptor(accessId, model, accessDenotationType));
			}

			@Override
			public ModelAccessoryBuilder accessDescriptor(AccessDescriptor accessDescriptor) {
				this.accessDescriptor = accessDescriptor;
				return this;
			}

			@Override
			public ModelAccessory build() {
				GwtModelAccessory result = new GwtModelAccessory(model);
				result.setModelAccessoryResourcesAccessFactory(accessoryResourceAccess.get());
				result.setUserRoles(userSession.getEffectiveRoles());
				result.setUseCases(useCases);

				if (accessDescriptor != null)
					result.setAccessDescriptor(accessDescriptor);

				return result;
			}
		}

		@Override
		public JsPromise<Supplier<PersistenceGmSession>> accessSessionFactory(String accessId) {
			return getModel(GetAccessModel.T, accessId) //
					.andThenMap(dataModel -> {
						AccessDescriptor ad = new AccessDescriptor(accessId, dataModel, null);
						return accessSessionFactoryBuilder(ad).build();
					}) //
					.toJsPromise();
		}

		@Override
		public JsPromise<Supplier<PersistenceGmSession>> serviceSessionFactory(String domainId) {
			return getModel(GetServiceModel.T, domainId) //
					.andThenMap(serviceModel -> {
						return serviceSessionFactoryBuilder(domainId, serviceModel).build();
					}) //
					.toJsPromise();
		}

		private Future<GmMetaModel> getModel(EntityType<? extends GetComponentModel> requestType, String externalId) {
			GetComponentModel getModel = requestType.create();
			getModel.setExternalId(externalId);

			EvalContext<? extends GmMetaModel> evalContext = getModel.eval(evaluator());

			return Future.fromAsyncCallbackConsumer(evalContext::get);
		}

		@Override
		public SessionFactoryBuilder accessSessionFactoryBuilder(AccessDescriptor accessDescriptor) {
			return new AccessSessionFactoryBuilder(accessDescriptor);
		}

		private class AccessSessionFactoryBuilder implements SessionFactoryBuilder {
			private final AccessDescriptor accessDescriptor;
			private ModelAccessory modelAccessory;

			public AccessSessionFactoryBuilder(AccessDescriptor accessDescriptor) {
				this.accessDescriptor = accessDescriptor;
			}

			@Override
			public SessionFactoryBuilder modelAccessory(ModelAccessory modelAccessory) {
				this.modelAccessory = modelAccessory;
				return this;
			}

			@Override
			public Supplier<PersistenceGmSession> build() {
				return () -> newPersistenceSession(modelAccessory());
			}

			private ModelAccessory modelAccessory() {
				if (modelAccessory != null)
					return modelAccessory;
				else
					return ServicesSessionImpl.this.modelAccessory(accessDescriptor.dataModel()) //
							.access(accessDescriptor.accessId(), accessDescriptor.accessDenotationType()) //
							.build();
			}

			private PersistenceGmSession newPersistenceSession(ModelAccessory modelAccessory) {
				GwtPersistenceGmSession bean = new GwtPersistenceGmSession();
				bean.configureAccessDescriptor(accessDescriptor);
				bean.setModelAccessory(modelAccessory);
				bean.setResourcesAccessFactory(resourceAccess.get());
				bean.setModelAccessoryResourcesAccessFactory(accessoryResourceAccess.get());
				bean.setRequestEvaluator(evaluatorBuilder().setDefaultDomain(accessDescriptor.accessId()).build());
				bean.setSessionIdSupplier(sessionIdHolder);
				bean.setUserNameSupplier(userSession.getUser()::getName);
				bean.setUserRolesSupplier(userSession::getEffectiveRoles);
				return bean;
			}

		}

		@Override
		public SessionFactoryBuilder serviceSessionFactoryBuilder(String domainId, GmMetaModel model) {
			return new ServiceDomainSessionFactoryBuilder(domainId, model);
		}

		private class ServiceDomainSessionFactoryBuilder implements SessionFactoryBuilder {
			private final String externalId;
			private final GmMetaModel model;

			private ModelAccessory modelAccessory;

			public ServiceDomainSessionFactoryBuilder(String externalId, GmMetaModel model) {
				this.externalId = externalId;
				this.model = model;
			}

			@Override
			public SessionFactoryBuilder modelAccessory(ModelAccessory modelAccessory) {
				this.modelAccessory = modelAccessory;
				return this;
			}

			@Override
			public Supplier<PersistenceGmSession> build() {
				return () -> newPersistenceSession(modelAccessory());
			}

			private ModelAccessory modelAccessory() {
				if (modelAccessory != null)
					return modelAccessory;
				else
					return ServicesSessionImpl.this.modelAccessory(model).build();
			}

			private PersistenceGmSession newPersistenceSession(ModelAccessory modelAccessory) {
				TfjsTransientPersistenceGmSession bean = new TfjsTransientPersistenceGmSession();
				bean.setAccessId(externalId);
				bean.setModelAccessory(modelAccessory);
				bean.setEvaluator(evaluatorBuilder().setDefaultDomain(externalId).build());
				bean.setSessionAuthorization(newSessionAuthorization());
				bean.setResourcesAccessFactory(resourceAccess.get());

				return bean;
			}

			private SessionAuthorization newSessionAuthorization() {
				BasicSessionAuthorization result = new BasicSessionAuthorization();
				result.setUserName(userSession.getUser().getName());
				result.setUserRoles(userSession.getEffectiveRoles());
				result.setSessionId(userSession.getSessionId());

				return result;
			}
		}

		private final Supplier<GwtSessionResourceSupport> resourceAccess = new SingletonBeanProvider<GwtSessionResourceSupport>() {
			@Override
			public GwtSessionResourceSupport create() throws Exception {
				GwtSessionResourceSupport bean = publish(restBasedAbstractResourceAccess.get());
				return bean;
			}
		};

		private final Supplier<GwtSessionResourceSupport> accessoryResourceAccess = new SingletonBeanProvider<GwtSessionResourceSupport>() {
			@Override
			public GwtSessionResourceSupport create() throws Exception {
				GwtSessionResourceSupport bean = publish(restBasedAbstractResourceAccess.get());
				bean.setAccessoryAxis(true);
				return bean;
			}
		};

		private final Supplier<GwtSessionResourceSupport> restBasedAbstractResourceAccess = new PrototypeBeanProvider<GwtSessionResourceSupport>() {
			@Override
			public GwtSessionResourceSupport create() throws Exception {
				GwtSessionResourceSupport bean = new GwtSessionResourceSupport();
				bean.setSessionIdProvider(sessionIdHolder);
				bean.setStreamBaseUrl(servicesUrl + "/api/v1/");
				return bean;
			}
		};

		@Override
		public <T> JsPromise<T> decodeJse(String jseValue) {
			GmDecodingContext context = new StandardDecodingContext(typeEnsurer.get());
			return new JseCodec().<T> decodeAsync(jseValue, context).toJsPromise();
		}

	}

}
