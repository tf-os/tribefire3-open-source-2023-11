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
package com.braintribe.model.processing.rpc.test.wire.space;

import static com.braintribe.wire.api.util.Maps.entry;
import static com.braintribe.wire.api.util.Maps.linkedMap;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.servlet.Filter;
import javax.servlet.http.HttpServletResponse;

import com.braintribe.exception.AuthorizationException;
import com.braintribe.model.processing.rpc.commons.api.config.GmRpcClientConfig;
import com.braintribe.model.processing.rpc.test.commons.TestAuthenticatingUserSessionProvider;
import com.braintribe.model.processing.rpc.test.commons.TestRpcClientAuthorizationContext;
import com.braintribe.model.processing.rpc.test.service.iface.basic.BasicTestService;
import com.braintribe.model.processing.rpc.test.service.iface.streaming.StreamingTestService;
import com.braintribe.model.processing.rpc.test.wire.contract.WebRpcTestContract;
import com.braintribe.model.processing.securityservice.api.exceptions.SecurityServiceException;
import com.braintribe.model.processing.session.api.managed.NotFoundException;
import com.braintribe.model.processing.webrpc.client.BasicGmWebRpcClientConfig;
import com.braintribe.model.processing.webrpc.client.GmWebRpcClientConfig;
import com.braintribe.model.processing.webrpc.server.GmWebRpcServer;
import com.braintribe.model.usersession.UserSession;
import com.braintribe.servlet.exception.ExceptionFilter;
import com.braintribe.servlet.exception.StandardExceptionHandler;
import com.braintribe.servlet.exception.StandardExceptionHandler.Exposure;
import com.braintribe.transport.http.DefaultHttpClientProvider;
import com.braintribe.util.servlet.remote.StandardRemoteClientAddressResolver;
import com.braintribe.utils.lcd.CollectionTools2;
import com.braintribe.utils.stream.api.StreamPipes;
import com.braintribe.web.multipart.api.MalformedMultipartDataException;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.util.Lists;

@Managed
public class WebRpcTestSpace implements WebRpcTestContract {

	private static final String rpcVersion = "2";
	
	@Import
	private MetaSpace meta;

	@Import
	private ClientCommonsSpace clientCommons;

	@Import
	private ServerCommonsSpace serverCommons;

	@Import
	private MarshallingSpace marshalling;
	
	@Managed
	@Override
	public GmRpcClientConfig basic() {
		GmWebRpcClientConfig bean = new GmWebRpcClientConfig();
		bean.setServiceId(BasicTestService.ID);
		bean.setServiceInterface(BasicTestService.class);
		config(bean, false);
		return bean;
	}

	@Managed
	@Override
	public GmRpcClientConfig basicReauthorizable() {
		GmWebRpcClientConfig bean = new GmWebRpcClientConfig();
		bean.setServiceId(BasicTestService.ID);
		bean.setServiceInterface(BasicTestService.class);
		config(bean, true);
		return bean;
	}

	@Managed
	@Override
	public GmRpcClientConfig streaming() {
		GmWebRpcClientConfig bean = new GmWebRpcClientConfig();
		bean.setServiceId(StreamingTestService.ID);
		bean.setServiceInterface(StreamingTestService.class);
		config(bean, false);
		return bean;
	}

	@Managed
	@Override
	public GmRpcClientConfig streamingReauthorizable() {
		GmWebRpcClientConfig bean = new GmWebRpcClientConfig();
		bean.setServiceId(StreamingTestService.ID);
		bean.setServiceInterface(StreamingTestService.class);
		config(bean, true);
		return bean;
	}

	@Managed
	@Override
	public GmRpcClientConfig denotationDriven() {
		BasicGmWebRpcClientConfig bean = new BasicGmWebRpcClientConfig();
		config(bean, false);
		return bean;
	}

	@Managed
	@Override
	public GmRpcClientConfig denotationDrivenReauthorizable() {
		BasicGmWebRpcClientConfig bean = new BasicGmWebRpcClientConfig();
		config(bean, true);
		return bean;
	}

	@Override
	public Supplier<UserSession> currentUserSessionInvalidator() {
		TestAuthenticatingUserSessionProvider userSessionProvider = clientCommons.userSessionProvider();
		return userSessionProvider::invalidateCurrentUserSession;
	}

	@Override
	public Consumer<Throwable> authorizationFailureConsumer() {
		TestAuthenticatingUserSessionProvider userSessionProvider = clientCommons.userSessionProvider();
		return userSessionProvider::accept;
	}

	@Override
	public Set<Throwable> currentAuthorizationFailures() {
		TestRpcClientAuthorizationContext authContext = clientCommons.authContext();
		return authContext.getNotifiedFailures();
	}

	@Managed
	@Override
	public GmWebRpcServer server() {
		GmWebRpcServer bean = new GmWebRpcServer();
		bean.setEvaluator(serverCommons.serviceRequestEvaluator());
		bean.setMarshallerRegistry(marshalling.registry());
//		bean.setCryptoContext(serverCommons.cryptoContext()); TODO: remove
//		bean.setRemoteAddressResolver(internetAddressResolver());
//		bean.setRequestTracingEnabled(false);
//		bean.setRequestTracingDir(new File("logs/requests"));
//		bean.setRequestTracingTimeout(900000); // 15 min
		bean.setStreamPipeFactory(StreamPipes.simpleFactory());
		return bean;
	}

	@Managed
	private StandardRemoteClientAddressResolver internetAddressResolver() {
		StandardRemoteClientAddressResolver bean = new StandardRemoteClientAddressResolver();
		bean.setIncludeForwarded(true);
		bean.setIncludeXForwardedFor(true);
		bean.setIncludeXRealIp(true);
		bean.setLenientParsing(true);
		return bean;
	}

	@Managed
	private DefaultHttpClientProvider httpClientProvider() {
		DefaultHttpClientProvider bean = new DefaultHttpClientProvider();
		bean.setSocketTimeout(40000);
		return bean;
	}

	private void config(BasicGmWebRpcClientConfig bean, boolean reauthorization) {
		clientCommons.config(bean, reauthorization);
		bean.setStreamPipeFactory(StreamPipes.simpleFactory());
		bean.setMarshaller(marshalling.registry().getMarshaller("application/json"));
		bean.setContentType("application/json");
		bean.setVersion(rpcVersion);
		bean.setHttpClientProvider(httpClientProvider());
	}

	@Override
	public List<Filter> filters() {
		return Lists.list(exceptionFilter());
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
	private Map<Class<? extends Throwable>,Integer> exceptionStatusCodeMap() {
		//@formatter:off
		return linkedMap(
				entry(IllegalArgumentException.class, HttpServletResponse.SC_BAD_REQUEST),
				entry(UnsupportedOperationException.class, HttpServletResponse.SC_NOT_IMPLEMENTED),
				entry(NotFoundException.class, HttpServletResponse.SC_NOT_FOUND),
				entry(AuthorizationException.class, HttpServletResponse.SC_FORBIDDEN),
				entry(SecurityServiceException.class, HttpServletResponse.SC_FORBIDDEN),
				entry(MalformedMultipartDataException.class, HttpServletResponse.SC_BAD_REQUEST)
		);
		//@formatter:on
	}
}
