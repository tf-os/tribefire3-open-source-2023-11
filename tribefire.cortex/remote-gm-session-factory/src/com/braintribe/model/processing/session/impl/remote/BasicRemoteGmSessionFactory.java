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
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.DestructionAware;
import com.braintribe.codec.marshaller.api.MarshallerRegistry;
import com.braintribe.exception.AuthorizationException;
import com.braintribe.logging.Logger;
import com.braintribe.model.access.AccessService;
import com.braintribe.model.access.impl.AccessServiceDelegatingAccess;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.generic.session.exception.GmSessionRuntimeException;
import com.braintribe.model.processing.rpc.commons.api.authorization.RpcClientAuthorizationContext;
import com.braintribe.model.processing.securityservice.api.exceptions.SecurityServiceException;
import com.braintribe.model.processing.securityservice.commons.provider.AuthenticatingUserSessionProvider;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.auth.SessionAuthorization;
import com.braintribe.model.processing.session.api.resource.ResourceAccessFactory;
import com.braintribe.model.processing.session.impl.persistence.BasicPersistenceGmSession;
import com.braintribe.model.processing.webrpc.client.BasicGmWebRpcClientConfig;
import com.braintribe.model.processing.webrpc.client.GmWebRpcClientAuthorizationContext;
import com.braintribe.model.processing.webrpc.client.GmWebRpcEvaluator;
import com.braintribe.model.securityservice.credentials.Credentials;
import com.braintribe.model.usersession.UserSession;
import com.braintribe.utils.LongIdGenerator;

/**
 * <p>
 * A {@link com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory} which optionally uses
 * configured {@link AccessService} and {@link UserSession} providers, creating the necessary providers and proxies if
 * they are not given.
 * </p>
 *
 * <p>
 * If no {@code UserSession} provider is configured, {@link Credentials} must be configured.
 * </p>
 *
 * <p>
 * Note: the method <code>newRemoteSessionFactory</code> has been removed from this class due to changes in the
 * underlying GM framework. The recommended way to create a new session factory is by using
 * <code>GmSessionFactories.remote(url)</code> from the library
 * <code>com.braintribe.tribefire.cortex:GmClientSupport</code>.
 * </p>
 * <p>
 * Example:<br>
 * <br>
 * <code>
 * PersistenceGmSessionFactory sessionFactory = GmSessionFactories.remote("https://localhost:8443/tribefire-services").authentication("cortex", "cortex").done();
 * </code>
 * </p>
 */

// At this point, the method newRemoteSessionFactory used to be. It has been removed.
// GmSessionFactories.remote(url) should be used to create remote session factories.

public class BasicRemoteGmSessionFactory extends UserSessionProviderBasedRemoteGmSessionFactory implements DestructionAware {

	private static Logger logger = Logger.getLogger(BasicRemoteGmSessionFactory.class);

	private Credentials credentials;

	private boolean securityServiceInternallyCreated = false;
	private boolean userSessionProviderInternallyCreated = false;

	@Configurable
	public void setCredentials(Credentials credentials) {
		this.credentials = credentials;
	}

	@Override
	public void preDestroy() {

		// destroys UserSession provider, if created internally
		if (userSessionProvider != null && userSessionProvider instanceof AuthenticatingUserSessionProvider && userSessionProviderInternallyCreated) {
			Thread.ofVirtual().name("BasicRemoteGmSessionFactory.preDestroy-" + LongIdGenerator.provideLongId())
					.start(() -> ((AuthenticatingUserSessionProvider<?>) userSessionProvider).preDestroy());
		}
	}

	@Override
	protected Supplier<UserSession> getUserSessionProvider() {

		if (userSessionProvider != null) {
			return userSessionProvider;
		}

		if (credentials == null) {
			throw new GmSessionRuntimeException("This GM session factory has neither a UserSession provider nor Credentials configured.");
		}

		synchronized (this) {
			if (userSessionProvider == null) {
				AuthenticatingUserSessionProvider<Credentials> authenticatingUserSessionProvider = new AuthenticatingUserSessionProvider<>();
				authenticatingUserSessionProvider.setCredentials(credentials);
				authenticatingUserSessionProvider.setEvaluator(getRequestEvaluator());
				userSessionProvider = authenticatingUserSessionProvider;
				authorizationFailureListener = authenticatingUserSessionProvider;
				logger.debug(() -> "Created internal user session provider: " + userSessionProvider);
				userSessionProviderInternallyCreated = true;
			}
		}

		return userSessionProvider;

	}

	private class MetaDataProvider implements Supplier<Map<String, Object>> {
		@Override
		public Map<String, Object> get() throws RuntimeException {
			try {
				Map<String, Object> metaData = new HashMap<>();
				metaData.put("sessionId", getSessionIdProvider().get());
				return metaData;
			} catch (GmSessionException e) {
				throw new RuntimeException("error while determine sessionId", e);
			}
		}
	}

	private RpcClientAuthorizationContext<Throwable> createRpcClientAuthorizationContext() {

		if (authorizationFailureListener == null) {
			return null;
		}

		GmWebRpcClientAuthorizationContext authorizationContext = new GmWebRpcClientAuthorizationContext();
		authorizationContext.setAuthorizationFailureListener(authorizationFailureListener);
		authorizationContext.setMaxRetries(2);

		return authorizationContext;

	}

	/**
	 * Creates a new session for the specified access.<br>
	 * This is just a static convenience method that can e.g. be used from a Spring context.
	 *
	 * @throws GmSessionException
	 *             if the new session cannot be created.
	 */
	public static PersistenceGmSession newRemoteSession(final BasicRemoteGmSessionFactory factory, final String accessId) throws GmSessionException {
		try {
			final PersistenceGmSession session = factory.newSession(accessId);
			return session;
		} catch (final GmSessionException e) {
			throw new GmSessionException("Error while creating new session for access '" + accessId + "'!", e);
		}
	}

	// At this point, the method newRemoteSessionFactory used to be. It has been removed.
	// GmSessionFactories.remote(url) should be used to create remote session factories.

	@Override
	public PersistenceGmSession newSession(String accessId) throws GmSessionException {
		try {

			AccessService theAccessService = getAccessService();

			AccessServiceDelegatingAccess access = new AccessServiceDelegatingAccess();
			access.setAccessId(accessId);
			access.setAccessService(theAccessService);

			MarshallerRegistry marshallerReg = getMarshallerRegistry();

			ResourceAccessFactory<PersistenceGmSession> resourceAccessFactory = getResourceAccessFactory();

			ModelAccessory modelAccessory = getModelAccessoryFactory().getForAccess(accessId);

			SessionAuthorization sessionAuthorization = getSessionAuthorization();
			String sessionId = sessionAuthorization.getSessionId();

			URL rpcUrl = getRpcUrl();
			BasicGmWebRpcClientConfig config = new BasicGmWebRpcClientConfig();
			config.setUrl(rpcUrl.toExternalForm());
			config.setMarshaller(marshallerReg.getMarshaller("gm/bin"));
			config.setContentType("gm/bin");
			config.setMetaDataProvider(clientMetaDataProvider(() -> sessionId));
			config.setExecutorService(executorService);
			config.setHttpClientProvider(httpClientProvider);
			config.setAuthorizationContext(createRpcClientAuthorizationContext());
			config.setStreamPipeFactory(streamPipeFactory);

			GmWebRpcEvaluator requestEvaluator = GmWebRpcEvaluator.create(config);

			BasicPersistenceGmSession persistenceGmSession = new BasicPersistenceGmSession();
			persistenceGmSession.setIncrementalAccess(access);
			persistenceGmSession.setResourcesAccessFactory(resourceAccessFactory);
			persistenceGmSession.setModelAccessory(modelAccessory);
			persistenceGmSession.setSessionAuthorization(sessionAuthorization);
			persistenceGmSession.setAccessId(accessId);
			persistenceGmSession.setRequestEvaluator(requestEvaluator);

			return persistenceGmSession;

		} catch (GmSessionRuntimeException | AuthorizationException | SecurityServiceException e) {
			throw e;
		} catch (Exception e) {
			throw new GmSessionRuntimeException("Could not create gm session.", e);
		}

	}

}
