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
package tribefire.extension.gcp.initializer.wire.space;

import tribefire.cortex.initializer.support.integrity.wire.contract.CoreInstancesContract;
import tribefire.cortex.initializer.support.wire.space.AbstractInitializerSpace;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.extension.gcp.initializer.wire.contract.ExistingInstancesContract;
import tribefire.extension.gcp.initializer.wire.contract.GcpInitializerModuleContract;
import tribefire.extension.gcp.initializer.wire.contract.GcpInitializerModuleMainContract;
import tribefire.extension.gcp.initializer.wire.contract.GcpInitializerModuleModelsContract;
import tribefire.extension.gcp.initializer.wire.contract.RuntimePropertiesContract;

/**
 * @see GcpInitializerModuleMainContract
 */
@Managed
public class GcpInitializerModuleMainSpace extends AbstractInitializerSpace implements GcpInitializerModuleMainContract {

	@Import
	private GcpInitializerModuleContract initializer;
	
	@Import
	private GcpInitializerModuleModelsContract models;
	
	@Import
	private ExistingInstancesContract existingInstances;
	
	@Import
	private CoreInstancesContract coreInstances;
	
	@Import
	private RuntimePropertiesContract properties;
	
	@Override
	public GcpInitializerModuleContract initializerContract() {
		return initializer;
	}

	@Override
	public GcpInitializerModuleModelsContract initializerModelsContract() {
		return models;
	}

	@Override
	public ExistingInstancesContract existingInstancesContract() {
		return existingInstances;
	}
	
	@Override
	public CoreInstancesContract coreInstancesContract() {
		return coreInstances;
	}

	@Override
	public RuntimePropertiesContract propertiesContract() {
		return properties;
	}
}
