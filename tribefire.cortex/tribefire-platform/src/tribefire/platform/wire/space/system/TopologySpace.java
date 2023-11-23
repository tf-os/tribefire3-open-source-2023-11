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
package tribefire.platform.wire.space.system;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.braintribe.execution.ExtendedScheduledThreadPoolExecutor;
import com.braintribe.execution.virtual.CountingVirtualThreadFactory;
import com.braintribe.logging.Logger;
import com.braintribe.model.extensiondeployment.HardwiredWorker;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.module.wire.contract.TopologyContract;
import tribefire.platform.impl.topology.CartridgeLiveInstances;
import tribefire.platform.impl.topology.HeartbeatManager;
import tribefire.platform.wire.space.common.CartridgeInformationSpace;
import tribefire.platform.wire.space.common.MessagingSpace;
import tribefire.platform.wire.space.cortex.deployment.DeploymentSpace;

@Managed
public class TopologySpace implements TopologyContract {

	private static final Logger logger = Logger.getLogger(TopologySpace.class);

	@Import
	private CartridgeInformationSpace cartridgeInformation;

	@Import
	private MessagingSpace messaging;

	@Import
	private DeploymentSpace deployment;

	@Override
	@Managed
	public CartridgeLiveInstances liveInstances() {
		CartridgeLiveInstances bean = new CartridgeLiveInstances();
		bean.setCurrentInstanceId(cartridgeInformation.instanceId());
		bean.setEnabled(consumeHeartbeats());
		bean.setAliveAge(30000); // 30 seconds
		bean.setMaxHeartbeatAge(30000); // 30 seconds
		bean.setCleanupInterval(600000); // 10 minutes
		bean.addListener("*", deployment.processor());
		return bean;
	}

	@Managed
	public HeartbeatManager heartbeatManager() {
		logger.info(() -> "Starting TFS HeartbeatManager signalling instance id: " + cartridgeInformation.instanceId());
		HeartbeatManager bean = new HeartbeatManager();
		bean.setCurrentInstanceId(cartridgeInformation.instanceId());
		bean.setConsumptionEnabled(consumeHeartbeats());
		bean.setHeartbeatConsumer(liveInstances());
		bean.setBroadcastingEnabled(true);
		bean.setBroadcastingService(heartbeatBroadcastingService());
		bean.setBroadcastingInterval(10L);
		bean.setBroadcastingIntervalUnit(TimeUnit.SECONDS);
		bean.setMessagingSessionProvider(messaging.sessionProvider());
		bean.setTopicName(messaging.destinations().heartbeatTopicName());
		bean.setAutoHeartbeatBroadcastingStart(false);
		return bean;
	}

	@Managed
	public HardwiredWorker heartbeatManagerDeployable() {
		HardwiredWorker bean = HardwiredWorker.T.create();
		bean.setExternalId("heartbeat-manager");
		bean.setName("Heartbeat Manager");
		bean.setGlobalId("hardwired:worker/" + bean.getExternalId());
		return bean;
	}

	public boolean consumeHeartbeats() {
		return true; // Whether the platform should track live instances. Could be later configurable.
	}

	@Managed
	private ScheduledExecutorService heartbeatBroadcastingService() {
		ExtendedScheduledThreadPoolExecutor bean = new ExtendedScheduledThreadPoolExecutor(1,
				new CountingVirtualThreadFactory("tribefire.heartbeat.sender[" + cartridgeInformation.instanceId() + "]-"));
		bean.setDescription("Master Heartbeat Sender");
		return bean;
	}

}
