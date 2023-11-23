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
package com.braintribe.model.processing.session;

import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

import com.braintribe.execution.virtual.VirtualThreadExecutor;
import com.braintribe.execution.virtual.VirtualThreadExecutorBuilder;
import com.braintribe.logging.Logger;
import com.braintribe.model.processing.session.api.managed.ModelAccessoryFactory;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.processing.session.api.resource.ResourceAccessFactory;
import com.braintribe.model.processing.session.impl.remote.BasicRemoteGmSessionFactory;
import com.braintribe.model.processing.session.wire.common.GmClientSupportWireModule;
import com.braintribe.model.processing.session.wire.common.contract.CommonContract;
import com.braintribe.model.processing.session.wire.common.contract.GmClientSupportContract;
import com.braintribe.model.processing.session.wire.common.contract.HttpClientProviderContract;
import com.braintribe.model.processing.session.wire.common.contract.RemoteAuthenticationContract;
import com.braintribe.model.processing.session.wire.common.space.CommonSpace;
import com.braintribe.model.processing.session.wire.common.space.RemoteAuthenticationSpace;
import com.braintribe.model.securityservice.credentials.Credentials;
import com.braintribe.model.securityservice.credentials.UserPasswordCredentials;
import com.braintribe.model.securityservice.credentials.identification.UserNameIdentification;
import com.braintribe.model.usersession.UserSession;
import com.braintribe.provider.Holder;
import com.braintribe.transport.http.DefaultHttpClientProvider;
import com.braintribe.transport.http.HttpClientProvider;
import com.braintribe.utils.MathTools;
import com.braintribe.utils.stream.api.StreamPipeFactory;
import com.braintribe.utils.stream.api.StreamPipes;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

public class GmRemoteSessionFactoryBuilderImpl extends GmSessionFactoryBuilderImpl implements GmRemoteSessionFactoryBuilder {

	private static Logger logger = Logger.getLogger(GmRemoteSessionFactoryBuilderImpl.class);

	protected boolean useLegacy = false;
	protected String url;
	protected Credentials authenticationCredentials;
	protected Supplier<UserSession> configuredUserSessionProvider;
	protected HttpClientProvider configuredHttpClientProvider;
	protected Supplier<ExecutorService> executorService;
	protected StreamPipeFactory streamPipeFactory;

	public GmRemoteSessionFactoryBuilderImpl(String url) {
		this.url = url;
	}

	@Override
	public GmRemoteSessionFactoryBuilder streamPipeFactory(StreamPipeFactory streamPipeFactory) {
		this.streamPipeFactory = streamPipeFactory;
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public GmRemoteSessionFactoryBuilder modelAccessoryFactory(ModelAccessoryFactory modelAccessoryFactory) {
		super.configuredModelAccessoryFactory = modelAccessoryFactory;
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public GmRemoteSessionFactoryBuilder authentication(String user, String password) {
		UserPasswordCredentials credentials = UserPasswordCredentials.T.create();
		UserNameIdentification identification = UserNameIdentification.T.create();
		identification.setUserName(user);

		credentials.setUserIdentification(identification);
		credentials.setPassword(password);

		this.authenticationCredentials = credentials;
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public GmRemoteSessionFactoryBuilder authentication(Credentials credentials) {
		this.authenticationCredentials = credentials;
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public GmRemoteSessionFactoryBuilder userSessionProvider(Supplier<UserSession> userSessionProvider) {
		this.configuredUserSessionProvider = userSessionProvider;
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public GmRemoteSessionFactoryBuilder httpClientProvider(HttpClientProvider httpClientProvider) {
		this.configuredHttpClientProvider = httpClientProvider;
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public PersistenceGmSessionFactory done() throws GmSessionFactoryBuilderException {

		if (this.url == null) {
			throw new GmSessionFactoryBuilderException("The URL must be set.");
		}
		if (this.authenticationCredentials == null && this.configuredUserSessionProvider == null) {
			throw new GmSessionFactoryBuilderException("The credentials or a user session provider must be set.");
		}

		if (useLegacy)
			return legacy_factory();

		HttpClientProvider clientProvider = (this.configuredHttpClientProvider != null) ? this.configuredHttpClientProvider
				: new DefaultHttpClientProvider();

		//@formatter:off
		WireContext<GmClientSupportContract> context = 
				Wire.contextBuilder(GmClientSupportWireModule.INSTANCE)
				.loadSpacesFrom(getClass().getClassLoader())
				.bindContract(RemoteAuthenticationContract.class, new RemoteAuthenticationSpace(authenticationCredentials, url))
				.bindContract(CommonContract.class, new ConfigurableCommonSpace())
				.bindContract(HttpClientProviderContract.class, () -> clientProvider)
				.build();
		//@formatter:on

		return context.contract().remoteSessionFactory();
	}

	public PersistenceGmSessionFactory legacy_factory() throws GmSessionFactoryBuilderException {

		if (this.url == null) {
			throw new GmSessionFactoryBuilderException("The URL must be set.");
		}
		if (this.authenticationCredentials == null && this.configuredUserSessionProvider == null) {
			throw new GmSessionFactoryBuilderException("The credentials or a user session provider must be set.");
		}
		try {
			boolean trace = logger.isTraceEnabled();

			if (trace) {
				logger.trace(this.toString());
			}

			BasicRemoteGmSessionFactory factory = new BasicRemoteGmSessionFactory();
			factory.setBaseUrlProvider(new Holder<>(this.url));
			if (this.authenticationCredentials != null) {
				factory.setCredentials(this.authenticationCredentials);
			}
			if (this.configuredUserSessionProvider != null) {
				factory.setUserSessionProvider(this.configuredUserSessionProvider);
			}
			if (super.configuredModelAccessoryFactory != null) {
				factory.setModelAccessoryFactory(super.configuredModelAccessoryFactory);
			}
			if (this.configuredHttpClientProvider != null) {
				factory.setHttpClientProvider(this.configuredHttpClientProvider);
			}
			if (this.configuredResourceAccessFactory != null) {
				factory.setResourceAccessFactory(configuredResourceAccessFactory);
			}

			if (executorService == null)
				executorService = () -> buildExecutorService(100, 100);
			if (streamPipeFactory == null)
				streamPipeFactory = StreamPipes.simpleFactory();

			factory.setExecutorService(this.executorService.get());
			factory.setStreamPipeFactory(streamPipeFactory);

			return factory;
		} catch (Exception e) {
			throw new GmSessionFactoryBuilderException("Could not create a PersistenceGmSessionFactory for URL " + url, e);
		}
	}

	/**
	 * Returns a String representation of this builder. The String contains all information provided by the client.
	 */
	@Override
	public String toString() {
		try {
			StringBuilder sb = new StringBuilder("GmRemoteSessionFactoryBuilderImpl with URL ");
			sb.append(this.url);
			sb.append(" [credentials: ");
			sb.append(this.authenticationCredentials);
			sb.append(", HttpClientProvider: ");
			sb.append(this.configuredHttpClientProvider);
			sb.append(", UserSessionProvider: ");
			sb.append(this.configuredUserSessionProvider);
			sb.append(']');
			return sb.toString();
		} catch (Exception e) {
			// Well, this is awkward
			logger.trace("Could not create a summary of this GmRemoteSessionFactoryBuilderImpl instance.", e);
			return super.toString();
		}
	}

	/** {@inheritDoc} */
	@Override
	public GmRemoteSessionFactoryBuilder executor(ExecutorService executorService) {
		this.executorService = () -> executorService;
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public GmRemoteSessionFactoryBuilder executor(int corePoolSize, int maxPoolSize, int keepAliveTime) {
		this.executorService = () -> buildExecutorService(corePoolSize, maxPoolSize);
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public GmRemoteSessionFactoryBuilder resourceAccessFactory(ResourceAccessFactory<PersistenceGmSession> resourceAccessFactory) {
		this.configuredResourceAccessFactory = resourceAccessFactory;
		return this;
	}

	/**
	 * Creates a new ExecutorService based on the provided parameters. The thread pool uses daemon threads.
	 * 
	 * @param corePoolSize
	 *            The core pool size of the thread pool. Values below 0 and above 100 will be corrected / adapted.
	 * @param maxPoolSize
	 *            The maximum pool size of the thread pool. Values between corePoolSize and Short.MAX_VALUE will be
	 *            corrected / adapted.
	 * @param keepAliveTime
	 *            The maximum keep-alive time of idle threads. Values between 0 and Short.MAX_VALUE will be corrected /
	 *            adapted.
	 * @return A new {@link ExecutorService} based on the provided parameters.
	 */
	protected static ExecutorService buildExecutorService(int corePoolSize, int maxPoolSize) {
		maxPoolSize = MathTools.clip(maxPoolSize, corePoolSize, Short.MAX_VALUE);

		VirtualThreadExecutor bean = VirtualThreadExecutorBuilder.newPool().concurrency(maxPoolSize)
				.threadNamePrefix("tribefire.remote.evaluator.executor-").description("Remote Evaluator").build();

		return bean;
	}

	private final class ConfigurableCommonSpace extends CommonSpace {
		@Override
		public ExecutorService executorService() {
			if (executorService != null)
				return executorService.get();

			return super.executorService();
		}
		@Override
		public StreamPipeFactory streamPipeFactory() {
			if (streamPipeFactory != null)
				return streamPipeFactory;

			return super.streamPipeFactory();
		}
	}

}
