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
package tribefire.cortex.initializer.support.integrity.wire.contract;

import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.cortex.deployment.CortexConfiguration;
import com.braintribe.model.cortex.deployment.EnvironmentDenotationRegistry;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.cortex.initializer.support.impl.lookup.GlobalId;
import tribefire.cortex.initializer.support.impl.lookup.InstanceLookup;

/**
 * {@link InstanceLookup lookup} of some of the fundamental entities in cortex.
 */
@InstanceLookup(lookupOnly = true)
public interface CoreInstancesContract extends WireSpace {

	String cortexModelGlobalId = "model:tribefire.cortex:tribefire-cortex-model";
	String cortexServiceModelGlobalId = "model:tribefire.cortex:tribefire-cortex-service-model";
	String workbenchModelGlobalId = "model:tribefire.cortex:workbench-model";
	String essentialMetaDataModelGlobalId = "model:com.braintribe.gm:essential-meta-data-model";
	String basicMetaModelGlobalId = "model:com.braintribe.gm:basic-meta-model";
	String deploymentModelGlobalId = "model:com.braintribe.gm:deployment-model";
	String basicValueDescriptorModelGlobalId = "model:com.braintribe.gm:basic-value-descriptor-model";
	String workbenchAccessGlobalId = "hardwired:access/workbench";

	@GlobalId(cortexModelGlobalId)
	GmMetaModel cortexModel();

	@GlobalId(cortexServiceModelGlobalId)
	GmMetaModel cortexServiceModel();

	@GlobalId(workbenchModelGlobalId)
	GmMetaModel workbenchModel();

	@GlobalId(essentialMetaDataModelGlobalId)
	GmMetaModel essentialMetaDataModel();

	@GlobalId(basicMetaModelGlobalId)
	GmMetaModel basicMetaModel();

	@GlobalId(deploymentModelGlobalId)
	GmMetaModel deploymentModel();

	@GlobalId(basicValueDescriptorModelGlobalId)
	GmMetaModel basicValueDescriptorModel();

	@GlobalId(workbenchAccessGlobalId)
	IncrementalAccess workbenchAccess();

	@GlobalId(CortexConfiguration.CORTEX_CONFIGURATION_GLOBAL_ID)
	CortexConfiguration cortexConfiguration();

	@GlobalId(EnvironmentDenotationRegistry.ENVIRONMENT_DENOTATION_REGISTRY__GLOBAL_ID)
	EnvironmentDenotationRegistry environmentDenotationRegistry();

}
