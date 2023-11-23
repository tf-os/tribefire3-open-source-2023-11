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

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.braintribe.model.processing.dmbrpc.client.BasicGmDmbRpcClientConfig;
import com.braintribe.model.processing.dmbrpc.client.GmDmbRpcClientConfig;
import com.braintribe.model.processing.dmbrpc.server.GmDmbRpcServer;
import com.braintribe.model.processing.rpc.commons.api.config.GmRpcClientConfig;
import com.braintribe.model.processing.rpc.test.commons.TestAuthenticatingUserSessionProvider;
import com.braintribe.model.processing.rpc.test.commons.TestRpcClientAuthorizationContext;
import com.braintribe.model.processing.rpc.test.service.iface.basic.BasicTestService;
import com.braintribe.model.processing.rpc.test.service.iface.streaming.StreamingTestService;
import com.braintribe.model.processing.rpc.test.wire.contract.DmbRpcTestContract;
import com.braintribe.model.usersession.UserSession;
import com.braintribe.utils.stream.api.StreamPipes;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.context.WireContextConfiguration;

@Managed
public class DmbRpcTestSpace implements DmbRpcTestContract {

	@Import
	private MetaSpace meta;

	@Import
	private ClientCommonsSpace clientCommons;

	@Import
	private ServerCommonsSpace serverCommons;

	@Import
	private MarshallingSpace marshalling;

	@Override
	public void onLoaded(WireContextConfiguration configuration) {
		server(); // start MBean RPC server
	}

	@Managed
	@Override
	public GmRpcClientConfig basic() {
		GmDmbRpcClientConfig bean = new GmDmbRpcClientConfig();
		bean.setServiceId(BasicTestService.ID);
		bean.setServiceInterface(BasicTestService.class);
		clientCommons.config(bean, false);
		return bean;
	}

	@Managed
	@Override
	public GmRpcClientConfig basicReauthorizable() {
		GmDmbRpcClientConfig bean = new GmDmbRpcClientConfig();
		bean.setServiceId(BasicTestService.ID);
		bean.setServiceInterface(BasicTestService.class);
		clientCommons.config(bean, true);
		return bean;
	}

	@Managed
	@Override
	public GmRpcClientConfig streaming() {
		GmDmbRpcClientConfig bean = new GmDmbRpcClientConfig();
		bean.setServiceId(StreamingTestService.ID);
		bean.setServiceInterface(StreamingTestService.class);
		clientCommons.config(bean, false);
		return bean;
	}

	@Managed
	@Override
	public GmRpcClientConfig streamingReauthorizable() {
		GmDmbRpcClientConfig bean = new GmDmbRpcClientConfig();
		bean.setServiceId(StreamingTestService.ID);
		bean.setServiceInterface(StreamingTestService.class);
		clientCommons.config(bean, true);
		return bean;
	}

	@Managed
	@Override
	public GmRpcClientConfig denotationDriven() {
		BasicGmDmbRpcClientConfig bean = new BasicGmDmbRpcClientConfig();
		clientCommons.config(bean, false);
		return bean;
	}

	@Managed
	@Override
	public GmRpcClientConfig denotationDrivenReauthorizable() {
		BasicGmDmbRpcClientConfig bean = new BasicGmDmbRpcClientConfig();
		clientCommons.config(bean, true);
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
	private GmDmbRpcServer server() {
		GmDmbRpcServer bean = new GmDmbRpcServer();
		bean.setEvaluator(serverCommons.serviceRequestEvaluator());
		bean.setMarshaller(marshalling.binMarshaller());
		bean.setStreamPipeFactory(StreamPipes.simpleFactory());
		return bean;
	}

}
