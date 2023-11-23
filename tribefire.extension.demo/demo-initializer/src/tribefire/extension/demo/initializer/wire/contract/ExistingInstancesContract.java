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
package tribefire.extension.demo.initializer.wire.contract;

import com.braintribe.model.deployment.Module;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.cortex.initializer.support.impl.lookup.GlobalId;
import tribefire.cortex.initializer.support.impl.lookup.InstanceLookup;

/**
 * <p>
 * This {@link WireSpace Wire contract} provides lookups on already existing instances. <br>
 * It exposes instances like:
 * <ul>
 * <li>Models which are coming from ModelPriming assets</li>
 * <li>Resources coming from ResourcePriming assets</li>
 * </ul>
 * </p>
 */
@InstanceLookup(lookupOnly = true)
public interface ExistingInstancesContract extends WireSpace {

	String GROUP_ID = "tribefire.extension.demo";
	String GLOBAL_ID_PREFIX = "model:" + GROUP_ID + ":";

	@GlobalId("module://" + GROUP_ID + ":demo-module")
	Module demoModule();

	@GlobalId(GLOBAL_ID_PREFIX + "demo-model")
	GmMetaModel demoModel();

	@GlobalId(GLOBAL_ID_PREFIX + "demo-deployment-model")
	GmMetaModel demoDeploymentModel();

	@GlobalId(GLOBAL_ID_PREFIX + "demo-api-model")
	GmMetaModel demoServiceModel();

	@GlobalId(GLOBAL_ID_PREFIX + "demo-cortex-api-model")
	GmMetaModel demoCortexServiceModel();

	@GlobalId("model:com.braintrige.gm:security-service-api-model")
	GmMetaModel securityServiceApiModel();

	@GlobalId("property:tribefire.extension.demo.model.data.process.HolidayRequestProcess/approvalStatus")
	GmProperty statusApproval();

}
