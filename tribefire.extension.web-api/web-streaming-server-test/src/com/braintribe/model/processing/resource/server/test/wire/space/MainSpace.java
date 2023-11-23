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
package com.braintribe.model.processing.resource.server.test.wire.space;

import static com.braintribe.wire.api.util.Maps.entry;
import static com.braintribe.wire.api.util.Maps.linkedMap;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.servlet.Filter;
import javax.servlet.http.HttpServletResponse;

import com.braintribe.codec.marshaller.api.ConfigurableMarshallerRegistry;
import com.braintribe.exception.AuthorizationException;
import com.braintribe.model.processing.resource.server.WebStreamingServer;
import com.braintribe.model.processing.resource.server.test.commons.TestAuthenticatingUserSessionProvider;
import com.braintribe.model.processing.resource.server.test.commons.TestAuthorizationContext;
import com.braintribe.model.processing.resource.server.test.commons.TestPersistenceGmSessionFactory;
import com.braintribe.model.processing.resource.server.test.commons.TestResourceAccessFactory;
import com.braintribe.model.processing.resource.server.test.commons.TestSecurityService;
import com.braintribe.model.processing.resource.server.test.wire.contract.MainContract;
import com.braintribe.model.processing.resource.streaming.access.RemoteResourceAccessFactory;
import com.braintribe.model.processing.securityservice.api.exceptions.SecurityServiceException;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.common.ConfigurableDispatchingServiceProcessor;
import com.braintribe.model.processing.service.common.eval.ConfigurableServiceRequestEvaluator;
import com.braintribe.model.processing.session.api.managed.NotFoundException;
import com.braintribe.model.processing.session.impl.persistence.BasicPersistenceGmSession;
import com.braintribe.model.processing.smood.Smood;
import com.braintribe.model.securityservice.SecurityRequest;
import com.braintribe.model.securityservice.credentials.UserPasswordCredentials;
import com.braintribe.model.usersession.UserSession;
import com.braintribe.provider.ThreadLocalStackedHolder;
import com.braintribe.servlet.exception.ExceptionFilter;
import com.braintribe.servlet.exception.StandardExceptionHandler;
import com.braintribe.servlet.exception.StandardExceptionHandler.Exposure;
import com.braintribe.thread.impl.ThreadLocalStack;
import com.braintribe.utils.lcd.CollectionTools2;
import com.braintribe.utils.stream.api.StreamPipes;
import com.braintribe.web.credentials.extractor.ExistingSessionFromRequestParameterProvider;
import com.braintribe.web.servlet.auth.AuthFilter;
import com.braintribe.web.servlet.auth.cookie.DefaultCookieHandler;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.annotation.Scope;
import com.braintribe.wire.api.util.Lists;

@Managed
public class MainSpace implements MainContract {

	@Import
	private MarshallingSpace marshalling;

	@Managed
	public TestSecurityService securityProcessor() {
		TestSecurityService bean = new TestSecurityService();
		return bean;
	}

	@Override
	@Managed
	public TestAuthenticatingUserSessionProvider userSessionProvider() {
		TestAuthenticatingUserSessionProvider bean = new TestAuthenticatingUserSessionProvider();
		bean.setEvaluator(serviceRequestEvaluator());
		bean.setCredentials(UserPasswordCredentials.forUserName("testuser", "testuser"));
		return bean;
	}

	@Override
	@Managed
	public TestAuthorizationContext userSessionIdProvider() {
		TestAuthorizationContext bean = new TestAuthorizationContext();
		bean.setUserSessionProvider(userSessionProvider());
		return bean;
	}

	@Override
	@Managed
	public RemoteResourceAccessFactory remoteResourceAccessFactory() {
		RemoteResourceAccessFactory bean = new RemoteResourceAccessFactory();
		bean.setMarshallerRegistry(marshalling.registry());
		bean.setSessionIdProvider(userSessionIdProvider());
		bean.setStreamPipeFactory(StreamPipes.simpleFactory());
		return bean;
	}

	@Override
	@Managed
	public ThreadLocalStackedHolder<UserSession> userSessionHolder() {
		ThreadLocalStackedHolder<UserSession> bean = new ThreadLocalStackedHolder<>();
		return bean;
	}

	@Override
	public List<Filter> filters() {
		return Lists.list(exceptionFilter(), authFilter());
	}

	@Managed
	public Filter exceptionFilter() {
		ExceptionFilter bean = new ExceptionFilter();
		bean.setExceptionHandlers(CollectionTools2.asSet(standardExceptionHandler()));
		return bean;
	}

	@Managed
	public StandardExceptionHandler standardExceptionHandler() {
		StandardExceptionHandler bean = new StandardExceptionHandler();
		bean.setExceptionExposure(Exposure.auto);
		bean.setMarshallerRegistry(marshalling.registry());
		bean.setStatusCodeMap(exceptionStatusCodeMap());
		return bean;
	}

	@Managed
	private Map<Class<? extends Throwable>, Integer> exceptionStatusCodeMap() {
		//@formatter:off
		return linkedMap(
				entry(IllegalArgumentException.class, HttpServletResponse.SC_BAD_REQUEST),
				entry(UnsupportedOperationException.class, HttpServletResponse.SC_NOT_IMPLEMENTED),
				entry(NotFoundException.class, HttpServletResponse.SC_NOT_FOUND),
				entry(AuthorizationException.class, HttpServletResponse.SC_FORBIDDEN),
				entry(SecurityServiceException.class, HttpServletResponse.SC_FORBIDDEN)
		);
		//@formatter:on
	}

	@Managed
	private AuthFilter authFilter() {
		AuthFilter bean = new AuthFilter();
		bean.setRequestEvaluator(serviceRequestEvaluator());
		bean.setCookieHandler(new DefaultCookieHandler());
		bean.setSessionCookieProvider(r -> null);
		bean.addWebCredentialProvider("request-parameter", existingSessionFromRequestParameterProvider());
		return bean;
	}

	@Managed
	private ExistingSessionFromRequestParameterProvider existingSessionFromRequestParameterProvider() {
		return new ExistingSessionFromRequestParameterProvider();
	}

	@Override
	@Managed
	public WebStreamingServer servlet() {
		WebStreamingServer bean = new WebStreamingServer();
		bean.setSessionFactory(sessionFactory());
		bean.setMarshallerRegistry(marshalling.registry());
		bean.setDefaultUploadResponseType("application/json");
		return bean;
	}

	@Managed
	private ConfigurableServiceRequestEvaluator serviceRequestEvaluator() {
		ConfigurableServiceRequestEvaluator bean = new ConfigurableServiceRequestEvaluator();
		bean.setExecutorService(Executors.newCachedThreadPool());
		bean.setServiceProcessor(dispatchingServiceProcessor());
		return bean;
	}

	private ConfigurableDispatchingServiceProcessor dispatchingServiceProcessor() {
		ConfigurableDispatchingServiceProcessor bean = new ConfigurableDispatchingServiceProcessor();
		bean.register(SecurityRequest.T, securityProcessor());
		return bean;
	}

	@Managed
	private ThreadLocalStack<ServiceRequestContext> serviceContextStack() {
		ThreadLocalStack<ServiceRequestContext> bean = new ThreadLocalStack<>();
		return bean;
	}

	@Override
	@Managed
	public Smood access() {
		Smood bean = new Smood(new ReentrantReadWriteLock());
		return bean;

	}

	@Override
	@Managed(Scope.prototype)
	public BasicPersistenceGmSession gmSession() {
		BasicPersistenceGmSession bean = new BasicPersistenceGmSession();
		bean.setIncrementalAccess(access());
		bean.setResourcesAccessFactory(resourceAccessFactory());
		return bean;
	}

	@Override
	@Managed
	public TestResourceAccessFactory resourceAccessFactory() {
		TestResourceAccessFactory bean = new TestResourceAccessFactory();
		return bean;
	}

	@Override
	@Managed
	public TestPersistenceGmSessionFactory sessionFactory() {
		TestPersistenceGmSessionFactory bean = new TestPersistenceGmSessionFactory();
		bean.setGmSessionProvider(this::gmSession);
		return bean;
	}

	@Override
	public ConfigurableMarshallerRegistry marshallerRegistry() {
		return marshalling.registry();
	}

}
