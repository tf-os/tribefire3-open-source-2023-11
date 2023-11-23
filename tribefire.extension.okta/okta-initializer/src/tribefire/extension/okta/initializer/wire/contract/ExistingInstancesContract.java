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
package tribefire.extension.okta.initializer.wire.contract;

import com.braintribe.model.deployment.Module;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.cortex.initializer.support.impl.lookup.GlobalId;
import tribefire.cortex.initializer.support.impl.lookup.InstanceLookup;
import tribrefire.extension.okta.common.OktaCommons;

@InstanceLookup(lookupOnly = true)
public interface ExistingInstancesContract extends WireSpace, OktaCommons {

	String GLOBAL_ID_PREFIX = "model:";

	// ***************************************************************************************************
	// System
	// ***************************************************************************************************

	// ***************************************************************************************************
	// Modules
	// ***************************************************************************************************

	@GlobalId("module://tribefire.extension.okta:okta-module")
	Module oktaModule();

	// ***************************************************************************************************
	// Models
	// ***************************************************************************************************

	@GlobalId(GLOBAL_ID_PREFIX + OKTA_DATA_MODEL_NAME)
	GmMetaModel oktaModel();

	@GlobalId(GLOBAL_ID_PREFIX + OKTA_API_MODEL_NAME)
	GmMetaModel oktaApiModel();

	@GlobalId(GLOBAL_ID_PREFIX + OKTA_DEPLOYMENT_MODEL_NAME)
	GmMetaModel oktaDeploymentModel();

}
