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
package com.braintribe.model.processing.session.impl.remote;

import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.codec.marshaller.api.MarshallerRegistry;
import com.braintribe.exception.AuthorizationException;
import com.braintribe.logging.Logger;
import com.braintribe.model.access.AccessService;
import com.braintribe.model.access.common.EvaluatingAccessService;
import com.braintribe.model.access.impl.AccessServiceDelegatingAccess;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.generic.session.exception.GmSessionRuntimeException;
import com.braintribe.model.processing.accessory.impl.BasicModelAccessoryFactory;
import com.braintribe.model.processing.accessory.impl.BasicModelAccessorySupplier;
import com.braintribe.model.processing.resource.streaming.access.RemoteResourceAccessFactory;
import com.braintribe.model.processing.securityservice.api.exceptions.SecurityServiceException;
import com.braintribe.model.processing.securityservice.commons.provider.SessionAuthorizationFromUserSessionProvider;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;
import com.braintribe.model.processing.session.api.managed.ModelAccessoryFactory;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.processing.session.api.persistence.auth.SessionAuthorization;
import com.braintribe.model.processing.session.api.resource.ResourceAccessFactory;
import com.braintribe.model.processing.session.impl.persistence.BasicPersistenceGmSession;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.usersession.UserSession;
import com.braintribe.provider.Holder;
import com.braintribe.transport.http.DefaultHttpClientProvider;
import com.braintribe.transport.http.HttpClientProvider;
import com.braintribe.utils.lcd.LazyInitialized;
import com.braintribe.utils.stream.api.StreamPipeFactory;

public abstract class AbstractRemoteGmSessionFactory implements PersistenceGmSessionFactory {

	private static final Logger logger = Logger.getLogger(AbstractRemoteGmSessionFactory.class);

	protected StreamPipeFactory streamPipeFactory;
	protected Supplier<URL> streamingUrlProvider = new DefaultStreamingUrlProvider();
	protected MarshallerRegistry marshallerRegistry;
	protected HttpClientProvider httpClientProvider = new DefaultHttpClientProvider();
	protected Supplier<UserSession> userSessionProvider;
	protected Consumer<Throwable> authorizationFailureListener;
	protected int authorizationMaxRetries = 2;
	protected ModelAccessoryFactory modelAccessoryFactory;
	private Supplier<Evaluator<ServiceRequest>> requestEvaluator = new LazyInitialized<>(this::createRequestEvaluator);
	private ResourceAccessFactory<PersistenceGmSession> resourceAccessFactory;
	protected ExecutorService executorService;

	private AccessService accessService;

	@Configurable
	public void setRequestEvaluator(Evaluator<ServiceRequest> requestEvaluator) {
		this.requestEvaluator = Holder.of(requestEvaluator);
	}

	public Evaluator<ServiceRequest> getRequestEvaluator() {
		return requestEvaluator.get();
	}

	protected abstract Evaluator<ServiceRequest> createRequestEvaluator();

	@Configurable
	@Required
	public void setStreamPipeFactory(StreamPipeFactory streamPipeFactory) {
		this.streamPipeFactory = streamPipeFactory;
	}

	@Configurable
	@Required
	public void setModelAccessoryFactory(ModelAccessoryFactory modelAccessoryFactory) {
		this.modelAccessoryFactory = modelAccessoryFactory;
	}

	@Configurable
	@Required
	public void setUserSessionProvider(Supplier<UserSession> userSessionProvider) {
		logger.debug(() -> "Receiving user session provider: " + userSessionProvider);
		this.userSessionProvider = userSessionProvider;
	}

	@Configurable
	public void setResourceAccessFactory(ResourceAccessFactory<PersistenceGmSession> resourceAccessFactory) {
		this.resourceAccessFactory = resourceAccessFactory;
	}

	@Override
	public PersistenceGmSession newSession(String accessId) throws GmSessionException {
		try {

			AccessService theAccessService = getAccessService();

			AccessServiceDelegatingAccess access = new AccessServiceDelegatingAccess();
			access.setAccessId(accessId);
			access.setAccessService(theAccessService);

			ResourceAccessFactory<PersistenceGmSession> resourceAccessFactory = getResourceAccessFactory();

			ModelAccessory modelAccessory = getModelAccessoryFactory().getForAccess(accessId);

			BasicPersistenceGmSession persistenceGmSession = new BasicPersistenceGmSession();
			persistenceGmSession.setIncrementalAccess(access);
			persistenceGmSession.setResourcesAccessFactory(resourceAccessFactory);
			persistenceGmSession.setModelAccessory(modelAccessory);
			persistenceGmSession.setSessionAuthorization(getSessionAuthorization());
			persistenceGmSession.setAccessId(accessId);
			persistenceGmSession.setRequestEvaluator(getRequestEvaluator());

			return persistenceGmSession;

		} catch (GmSessionRuntimeException | AuthorizationException | SecurityServiceException e) {
			throw e;
		} catch (Exception e) {
			throw new GmSessionRuntimeException("Could not create gm session.", e);
		}

	}

	protected ResourceAccessFactory<PersistenceGmSession> getResourceAccessFactory() {

		if (resourceAccessFactory == null) {
			synchronized (this) {
				if (resourceAccessFactory == null) {
					// This is necessary to initialize the user session provider
					getUserSessionProvider();

					RemoteResourceAccessFactory factory = new RemoteResourceAccessFactory();
					factory.setBaseStreamingUrl(getStreamingUrl());
					factory.setSessionIdProvider(getSessionIdProvider());
					if (authorizationFailureListener != null) {
						logger.debug(() -> "Forwarding authorization failure listener " + authorizationFailureListener
								+ " to the RemoteResourceAccessFactory");
						factory.setAuthorizationFailureListener(authorizationFailureListener);
					} else {
						logger.debug(() -> "No authorization failure listener to be propagated to the RemoteResourceAccessFactory");
					}
					factory.setAuthorizationMaxRetries(authorizationMaxRetries);
					factory.setMarshallerRegistry(getMarshallerRegistry());
					factory.setResponseMimeTypeProvider(new Holder<>("gm/bin"));
					factory.setStreamPipeFactory(streamPipeFactory);
					if (this.httpClientProvider != null) {
						factory.setHttpClientProvider(this.httpClientProvider);
					} else {
						logger.trace(() -> "Creating a RemoteResourceAccessFactory without a configured HttpClientProvider. Nothing to worry about.");
					}
					resourceAccessFactory = factory;
				}
			}
		}

		return resourceAccessFactory;

	}

	protected ModelAccessoryFactory getModelAccessoryFactory() {
		if (modelAccessoryFactory != null) {
			return modelAccessoryFactory;
		}

		BasicModelAccessorySupplier accessorySupplier = new BasicModelAccessorySupplier();
		accessorySupplier.setCacheModelAccessories(true);
		accessorySupplier.setCortexSessionSupplier(this::lowLevelCortexGmSession);

		BasicModelAccessoryFactory factory = new BasicModelAccessoryFactory();
		factory.setAccessSessionProvider(sessionAuthorizationProvider());
		factory.setServiceSessionProvider(getUserSessionProvider());

		factory.setCacheModelAccessories(true);
		factory.setModelAccessorySupplier(accessorySupplier);
		factory.setCortexSessionSupplier(this::lowLevelCortexGmSession);
		factory.setDefaultServiceDomainId("serviceDomain:default");

		factory.setUserRolesProvider(() -> {
			Supplier<UserSession> provider = getUserSessionProvider();
			if (provider == null) {
				return null;
			}

			UserSession userSession = provider.get();
			if (userSession == null) {
				return null;
			}

			return userSession.getEffectiveRoles();
		});

		modelAccessoryFactory = factory;
		return factory;
	}

	private PersistenceGmSession lowLevelCortexGmSession() {

		AccessServiceDelegatingAccess access = new AccessServiceDelegatingAccess();
		access.setAccessId("cortex");
		access.setAccessService(getAccessService());

		BasicPersistenceGmSession persistenceGmSession = new BasicPersistenceGmSession();
		persistenceGmSession.setIncrementalAccess(access);
		persistenceGmSession.setAccessId("cortex");
		return persistenceGmSession;

	}

	protected Supplier<SessionAuthorization> sessionAuthorizationProvider() {
		SessionAuthorizationFromUserSessionProvider bean = new SessionAuthorizationFromUserSessionProvider();
		bean.setUserSessionProvider(getUserSessionProvider());
		return bean;
	}

	protected SessionAuthorization getSessionAuthorization() {
		return null;
	}

	@Configurable
	public void setAccessService(AccessService accessService) {
		this.accessService = accessService;
	}

	protected AccessService getAccessService() {
		if (accessService == null) {
			synchronized (this) {
				if (accessService == null) {
					EvaluatingAccessService evaluatingAccessService = new EvaluatingAccessService(getRequestEvaluator());
					accessService = evaluatingAccessService;
				}
			}
		}

		return accessService;
	}

	protected abstract URL getStreamingUrl() throws GmSessionException;

	protected Supplier<UserSession> getUserSessionProvider() {
		if (userSessionProvider == null) {
			throw new GmSessionRuntimeException("No user session provider was configured to " + this.getClass().getName());
		}
		return userSessionProvider;
	}

	protected UserSession getUserSession() {
		try {
			Supplier<UserSession> theUserSessionProvider = getUserSessionProvider();
			return theUserSessionProvider != null ? theUserSessionProvider.get() : null;
		} catch (RuntimeException e) {
			throw new RuntimeException("error while providing user session", e);
		}
	}

	protected String getSessionId() {
		UserSession userSession = getUserSession();

		if (userSession == null)
			return null;

		return userSession.getSessionId();
	}

	protected Supplier<String> getSessionIdProvider() {
		return () -> getUserSessionProvider().get().getSessionId();
	}

	protected abstract MarshallerRegistry getMarshallerRegistry() throws GmSessionException;

	@Configurable
	public void setHttpClientProvider(HttpClientProvider httpClientProvider) {
		this.httpClientProvider = httpClientProvider;
	}

	@Configurable
	public void setExecutorService(ExecutorService executorService) {
		this.executorService = executorService;
	}

	@Configurable
	public void setAuthorizationFailureListener(Consumer<Throwable> authorizationFailureListener) {
		this.authorizationFailureListener = authorizationFailureListener;
	}

	@Configurable
	public void setAuthorizationMaxRetries(int authorizationMaxRetries) {
		this.authorizationMaxRetries = authorizationMaxRetries;
	}

}
