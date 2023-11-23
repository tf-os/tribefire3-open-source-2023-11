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
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.codec.marshaller.bin.Bin2Marshaller;
import com.braintribe.execution.virtual.VirtualThreadExecutorBuilder;
import com.braintribe.logging.ThreadRenamer;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.messaging.Destination;
import com.braintribe.model.messaging.Queue;
import com.braintribe.model.messaging.dmb.GmDmbMqMessaging;
import com.braintribe.model.processing.mqrpc.client.BasicGmMqRpcClientConfig;
import com.braintribe.model.processing.mqrpc.client.GmMqRpcClientConfig;
import com.braintribe.model.processing.mqrpc.server.GmMqRpcServer;
import com.braintribe.model.processing.rpc.commons.api.config.GmRpcClientConfig;
import com.braintribe.model.processing.rpc.test.commons.TestAuthenticatingUserSessionProvider;
import com.braintribe.model.processing.rpc.test.commons.TestRpcClientAuthorizationContext;
import com.braintribe.model.processing.rpc.test.service.iface.basic.BasicTestService;
import com.braintribe.model.processing.rpc.test.service.iface.streaming.StreamingTestService;
import com.braintribe.model.processing.rpc.test.wire.contract.MqRpcTestContract;
import com.braintribe.model.service.api.InstanceId;
import com.braintribe.model.usersession.UserSession;
import com.braintribe.transport.http.DefaultHttpClientProvider;
import com.braintribe.transport.messaging.api.MessagingContext;
import com.braintribe.transport.messaging.api.MessagingSessionProvider;
import com.braintribe.transport.messaging.dbm.GmDmbMqConnectionProvider;
import com.braintribe.transport.messaging.impl.StandardMessagingSessionProvider;
import com.braintribe.util.servlet.remote.StandardRemoteClientAddressResolver;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

@Managed
public class MqRpcTestSpace implements MqRpcTestContract {

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
		GmMqRpcClientConfig bean = new GmMqRpcClientConfig();
		bean.setServiceId(BasicTestService.ID);
		bean.setServiceInterface(BasicTestService.class);
		config(bean, false);
		return bean;
	}

	@Managed
	@Override
	public GmRpcClientConfig basicReauthorizable() {
		GmMqRpcClientConfig bean = new GmMqRpcClientConfig();
		bean.setServiceId(BasicTestService.ID);
		bean.setServiceInterface(BasicTestService.class);
		config(bean, true);
		return bean;
	}

	@Managed
	@Override
	public GmRpcClientConfig streaming() {
		GmMqRpcClientConfig bean = new GmMqRpcClientConfig();
		bean.setServiceId(StreamingTestService.ID);
		bean.setServiceInterface(StreamingTestService.class);
		config(bean, false);
		return bean;
	}

	@Managed
	@Override
	public GmRpcClientConfig streamingReauthorizable() {
		GmMqRpcClientConfig bean = new GmMqRpcClientConfig();
		bean.setServiceId(StreamingTestService.ID);
		bean.setServiceInterface(StreamingTestService.class);
		config(bean, true);
		return bean;
	}

	@Managed
	@Override
	public GmRpcClientConfig denotationDriven() {
		BasicGmMqRpcClientConfig bean = new BasicGmMqRpcClientConfig();
		config(bean, false);
		return bean;
	}

	@Managed
	@Override
	public GmRpcClientConfig denotationDrivenReauthorizable() {
		BasicGmMqRpcClientConfig bean = new BasicGmMqRpcClientConfig();
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
	public GmMqRpcServer server() {
		GmMqRpcServer bean = new GmMqRpcServer();
		bean.setConsumerId(serverId());
		bean.setExecutor(threadPool());
		bean.setMessagingSessionProvider(serverSessionProvider());
		bean.setRequestEvaluator(serverCommons.serviceRequestEvaluator());
		bean.setRequestDestinationName(requestDestinationName());
		bean.setRequestDestinationType(requestDestinationType());
		bean.setThreadRenamer(threadRenamer());
		bean.setTrusted(true);
		bean.start();
		return bean;
	}

	@Managed
	private Marshaller messageMarshaller() {
		return new Bin2Marshaller();
	}

	@Managed
	private MessagingSessionProvider serverSessionProvider() {
		StandardMessagingSessionProvider bean = new StandardMessagingSessionProvider();
		bean.setMessagingConnectionProvider(messagingConnectionProvider(serverMqContext()));

		return bean;
	}

	@Managed
	private MessagingSessionProvider clientSessionProvider() {
		StandardMessagingSessionProvider bean = new StandardMessagingSessionProvider();
		bean.setMessagingConnectionProvider(messagingConnectionProvider(serverMqContext()));
		return bean;
	}

	private GmDmbMqConnectionProvider messagingConnectionProvider(MessagingContext context) {
		return new com.braintribe.transport.messaging.dbm.GmDmbMqMessaging().createConnectionProvider(GmDmbMqMessaging.T.create(), context);
	}

	@Managed
	private MessagingContext serverMqContext() {
		MessagingContext bean = new MessagingContext();
		bean.setMarshaller(messageMarshaller());
		bean.setApplicationId(serverId().getApplicationId());
		bean.setNodeId(serverId().getNodeId());
		return bean;
	}

	@Managed
	private MessagingContext clientMqContext() {
		MessagingContext bean = new MessagingContext();
		bean.setMarshaller(messageMarshaller());
		bean.setApplicationId(clientId().getApplicationId());
		bean.setNodeId(clientId().getNodeId());
		return bean;
	}

	@Managed
	private InstanceId serverId() {
		InstanceId bean = InstanceId.T.create();
		bean.setApplicationId("server");
		bean.setNodeId("127.0.0.1");
		return bean;
	}

	@Managed
	private InstanceId clientId() {
		InstanceId bean = InstanceId.T.create();
		bean.setApplicationId("client");
		bean.setNodeId("127.0.0.1");
		return bean;
	}

	@Managed
	private EntityType<? extends Destination> requestDestinationType() {
		return Queue.T;
	}

	@Managed
	private String requestDestinationName() {
		return "test-request-queue";
	}

	@Managed
	private String responseDestinationName() {
		return "test-response-topic";
	}

	@Managed
	private ThreadRenamer threadRenamer() {
		ThreadRenamer bean = new ThreadRenamer(true);
		return bean;
	}

	@Managed
	private ExecutorService threadPool() {
		return VirtualThreadExecutorBuilder.newPool().concurrency(20).threadNamePrefix("tribefire.multicast-master-").build();
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

	private void config(BasicGmMqRpcClientConfig bean, boolean reauthorization) {
		clientCommons.config(bean, reauthorization);
		bean.setMessagingSessionProvider(clientSessionProvider());
		bean.setClientInstanceId(clientId());
		bean.setRequestDestinationName(requestDestinationName());
		bean.setRequestDestinationType(requestDestinationType());
		bean.setIgnoreResponses(false);
		bean.setResponseTopicName(responseDestinationName());
		bean.setResponseTimeout(300000);
		bean.setRetries(0);
	}

}
