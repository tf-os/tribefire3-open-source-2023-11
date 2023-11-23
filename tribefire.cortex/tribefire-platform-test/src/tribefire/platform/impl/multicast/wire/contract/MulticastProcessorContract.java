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
package tribefire.platform.impl.multicast.wire.contract;

import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.service.api.InstanceId;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.transport.messaging.api.MessagingSessionProvider;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.platform.impl.multicast.TestMulticastConsumer;
import tribefire.platform.impl.topology.CartridgeLiveInstances;

public interface MulticastProcessorContract extends WireSpace {

	Long DEFAULT_TIMEOUT = 3000L;
	Long REQUEST_TIMEOUT = 2000L;

	InstanceId ADDRESSEE_A = instanceId("a", null);
	InstanceId ADDRESSEE_B = instanceId("b", null);

	InstanceId INSTANCE_A1 = instanceId(ADDRESSEE_A.getApplicationId(), "1");
	InstanceId INSTANCE_A2 = instanceId(ADDRESSEE_A.getApplicationId(), "2");
	InstanceId INSTANCE_B1 = instanceId(ADDRESSEE_B.getApplicationId(), "1");
	InstanceId INSTANCE_B2 = instanceId(ADDRESSEE_B.getApplicationId(), "2");

	InstanceId[] ALL_INSTANCES = { INSTANCE_A1, INSTANCE_A2, INSTANCE_B1, INSTANCE_B2 };

	CartridgeLiveInstances liveInstances();

	TestMulticastConsumer consumer(InstanceId instanceId);

	MessagingSessionProvider sessionProvider(InstanceId instanceId);

	static InstanceId instanceId(String appId, String nodeId) {
		InstanceId id = InstanceId.T.create();
		id.setApplicationId(appId);
		id.setNodeId(nodeId);
		return id;
	}
	
	Evaluator<ServiceRequest> evaluator();

}
