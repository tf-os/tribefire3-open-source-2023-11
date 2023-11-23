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

import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.initializer.support.integrity.wire.contract.CoreInstancesContract;

import tribefire.extension.graphux.graph_ux_initializer.wire.contract.GraphUxInitializerContract;
import tribefire.extension.graphux.graph_ux_initializer.wire.contract.GraphUxInitializerMainContract;
import tribefire.extension.graphux.graph_ux_initializer.wire.contract.GraphUxInitializerModelsContract;
import tribefire.extension.graphux.graph_ux_initializer.wire.contract.ExistingInstancesContract;
import tribefire.extension.graphux.graph_ux_initializer.wire.contract.RuntimePropertiesContract;
import tribefire.extension.simple.initializer.wire.contract.SimpleInitializerModelsContract;

@Managed
public class GraphUxInitializerMainSpace implements GraphUxInitializerMainContract {

	@Import
	private GraphUxInitializerContract initializer;
	
	@Import
	private GraphUxInitializerModelsContract models;
	
	@Import
	private ExistingInstancesContract existingInstances;
	
	@Import
	private RuntimePropertiesContract properties;
	
	@Import
	private CoreInstancesContract coreInstances;
	
	@Import
	private tribefire.extension.simple.initializer.wire.contract.ExistingInstancesContract simpleExistingInstances;

	
	@Override
	public GraphUxInitializerContract initializer() {
		return initializer;
	}

	@Override
	public GraphUxInitializerModelsContract models() {
		return models;
	}

	@Override
	public ExistingInstancesContract existingInstances() {
		return existingInstances;
	}
	
	@Override
	public RuntimePropertiesContract properties() {
		return properties;
	}
	
	@Override
	public CoreInstancesContract coreInstances() {
		return coreInstances;
	}
	
	@Override
	public tribefire.extension.simple.initializer.wire.contract.ExistingInstancesContract simpleExistingInstances() {
		return simpleExistingInstances;
	}

}
