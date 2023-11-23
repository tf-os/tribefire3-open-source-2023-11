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

import com.braintribe.model.extensiondeployment.meta.ProcessWith;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.initializer.support.integrity.wire.contract.CoreInstancesContract;
import tribefire.cortex.initializer.support.wire.space.AbstractInitializerSpace;

import tribefire.extension.graphux.graph_ux_initializer.wire.contract.GraphUxInitializerContract;
import tribefire.extension.graphux.graph_ux_initializer.wire.contract.GraphUxInitializerModelsContract;
import tribefire.extension.graphux.model.deployment.GraphUxService;
import tribefire.extension.graphux.graph_ux_initializer.wire.contract.ExistingInstancesContract;

@Managed
public class GraphUxInitializerSpace extends AbstractInitializerSpace implements GraphUxInitializerContract {

	@Import
	private GraphUxInitializerModelsContract models;
	
	@Import
	private ExistingInstancesContract existingInstances;
	
	@Import
	private CoreInstancesContract coreInstances;

	@Managed
	@Override
	public GraphUxService graphUxProcessor() {
		GraphUxService bean = create(GraphUxService.T);
		bean.setExternalId("service.graphux");
		
		bean.setName(GraphUxService.T.getShortName());
		bean.setModule(existingInstances.graphUxModule());

		return bean;
	}
	
	@Managed
	@Override
	public MetaData processWithGraphUxProcessor() {
		ProcessWith bean = create(ProcessWith.T);
		bean.setProcessor(graphUxProcessor());
		return bean;
	}
}
