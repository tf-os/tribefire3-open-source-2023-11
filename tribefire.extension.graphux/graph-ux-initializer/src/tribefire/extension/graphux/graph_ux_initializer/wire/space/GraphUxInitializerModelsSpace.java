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
package tribefire.extension.graphux.graph_ux_initializer.wire.space;

import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.initializer.support.integrity.wire.contract.CoreInstancesContract;
import tribefire.cortex.initializer.support.wire.space.AbstractInitializerSpace;

import tribefire.extension.graphux.graph_ux_initializer.wire.contract.GraphUxInitializerModelsContract;
import tribefire.extension.graphux.graph_ux_initializer.wire.contract.ExistingInstancesContract;

@Managed
public class GraphUxInitializerModelsSpace extends AbstractInitializerSpace implements GraphUxInitializerModelsContract {

	@Import
	private ExistingInstancesContract existingInstances;
	
	@Import
	private CoreInstancesContract coreInstances;

	@Managed
	@Override
	public GmMetaModel configuredGraphUxDeploymentModel() {
		GmMetaModel model = create(GmMetaModel.T);
		model.setName(ExistingInstancesContract.GROUP_ID + ":configured-graph-ux-deployment-model");
		model.getDependencies().add(existingInstances.graphUxDeploymentModel());

		return model;
	}

	@Managed
	@Override
	public GmMetaModel configuredGraphUxServiceModel() {
		GmMetaModel model = create(GmMetaModel.T);
		model.setName(ExistingInstancesContract.GROUP_ID + ":configured-graph-ux-service-model");
		model.getDependencies().add(existingInstances.graphUxServiceModel());

		return model;
	}

//	@Managed
//	@Override
//	public GmMetaModel configuredGraphUxDataModel() {
//		GmMetaModel model = create(GmMetaModel.T);
//		model.setName(ExistingInstancesContract.GROUP_ID + ":configured-graph-ux-data-model");
//		model.getDependencies().add(existingInstances.graphUxDataModel());
//
//		return model;
//	}
	
}
