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
package tribefire.extension.modelling_wb.wire.space;

import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.assets.default_wb_initializer.wire.contract.DefaultWbContract;
import tribefire.cortex.initializer.support.wire.space.AbstractInitializerSpace;
import tribefire.extension.modelling_wb.wire.contract.ExistingInstancesContract;
import tribefire.extension.modelling_wb.wire.contract.ModellingWbInitializerContract;
import tribefire.extension.modelling_wb.wire.contract.ModellingWbInitializerMainContract;

@Managed
public class ModellingWbInitializerMainSpace extends AbstractInitializerSpace implements ModellingWbInitializerMainContract {

	@Import
	private ModellingWbInitializerContract initializer;
	
	@Import
	private ExistingInstancesContract existingInstances;
	
	@Import
	private DefaultWbContract workbench;
	
	@Override
	public ModellingWbInitializerContract initializerContract() {
		return initializer;
	}

	@Override
	public ExistingInstancesContract existingInstancesContract() {
		return existingInstances;
	}
	
	@Override
	public DefaultWbContract workbenchContract() {
		return workbench;
	}
}
