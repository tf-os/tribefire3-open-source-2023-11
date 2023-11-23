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
package tribefire.platform.wire.space.messaging;

import java.util.concurrent.ExecutorService;

import com.braintribe.execution.virtual.VirtualThreadExecutor;
import com.braintribe.execution.virtual.VirtualThreadExecutorBuilder;
import com.braintribe.model.extensiondeployment.HardwiredWorker;
import com.braintribe.model.messaging.Topic;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.mqrpc.server.GmMqRpcServer;
import com.braintribe.model.processing.service.api.aspect.EndpointExposureAspect;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.platform.impl.multicast.MulticastProcessor;
import tribefire.platform.wire.space.common.CartridgeInformationSpace;
import tribefire.platform.wire.space.common.EnvironmentSpace;
import tribefire.platform.wire.space.common.MessagingSpace;
import tribefire.platform.wire.space.common.RuntimeSpace;
import tribefire.platform.wire.space.rpc.RpcSpace;
import tribefire.platform.wire.space.system.TopologySpace;

@Managed
public class MulticastSpace implements WireSpace {

	// @formatter:on
	@Import
	private CartridgeInformationSpace cartridgeInformation;
	@Import
	private EnvironmentSpace environment;
	@Import
	private MessagingSpace messaging;
	@Import
	private RpcSpace rpc;
	@Import
	private RuntimeSpace runtime;
	@Import
	private TopologySpace topology;
	// @formatter:off

	@Managed
	public MulticastProcessor processor() {
		MulticastProcessor bean = new MulticastProcessor();
		bean.setMessagingSessionProvider(messaging.sessionProvider());
		bean.setRequestTopicName(messaging.destinations().multicastRequestTopicName());
		bean.setResponseTopicName(messaging.destinations().multicastResponseTopicName());
		bean.setSenderId(cartridgeInformation.instanceId());
		bean.setLiveInstances(topology.liveInstances());
		bean.setMetaDataProvider(rpc.clientMetaDataProvider());
		return bean;
	}

	@Managed
	public GmMqRpcServer consumer() {
		GmMqRpcServer bean = new GmMqRpcServer();
		bean.setRequestEvaluator(rpc.serviceRequestEvaluator());
		bean.setMessagingSessionProvider(messaging.sessionProvider());
		bean.setRequestDestinationName(messaging.destinations().multicastRequestTopicName());
		bean.setRequestDestinationType(Topic.T);
		bean.setConsumerId(cartridgeInformation.instanceId());
		bean.setExecutor(threadPool());
		bean.setThreadRenamer(runtime.threadRenamer());
		bean.setTrusted(false);
		bean.setKeepAliveInterval(environment.property(TribefireRuntime.ENVIRONMENT_MULTICAST_KEEP_ALIVE_INTERVAL, Long.class, 10000L));
		bean.setMetaDataResolverProvider(rpc.metaDataResolverProvider());
		bean.setEndpointExposure(EndpointExposureAspect.MULTICAST);
		return bean;
	}

	@Managed
	public HardwiredWorker consumerDeployable() {
		HardwiredWorker bean = HardwiredWorker.T.create();
		bean.setExternalId("multicast-consumer");
		bean.setName("Multicast Consumer");
		bean.setGlobalId("hardwired:worker/" + bean.getExternalId());
		return bean;
	}

	@Managed
	private ExecutorService threadPool() {
		VirtualThreadExecutor bean = VirtualThreadExecutorBuilder.newPool().concurrency(250).threadNamePrefix("tribefire.multicast-master-").description("Master Multicast Consumer").build();
		return bean;
	}
}
