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
package tribefire.platform.wire.space.common;

import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.tfconstants.TribefireConstants;
import com.braintribe.model.service.api.InstanceId;
import com.braintribe.web.api.WebApps;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

@Managed
// TODO rename
public class CartridgeInformationSpace implements WireSpace {

	@Managed
	public InstanceId instanceId() {
		InstanceId bean = InstanceId.T.create();
		bean.setApplicationId(applicationId());
		bean.setNodeId(nodeId());
		return bean;
	}

	public String applicationId() {
		return TribefireConstants.TRIBEFIRE_SERVICES_APPLICATION_ID;
	}

	/** The id of the node where the current instance is running. */
	@Managed
	public String nodeId() {
		String nodeId = TribefireRuntime.getProperty(TribefireRuntime.ENVIRONMENT_NODE_ID, WebApps.nodeId());
		return nodeId;
	}

}
