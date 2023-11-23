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
package tribefire.extension.modelling_wb;

import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.wire.api.module.WireTerminalModule;

import tribefire.cortex.assets.default_wb_initializer.wire.contract.DefaultWbContract;
import tribefire.cortex.initializer.support.api.WiredInitializerContext;
import tribefire.cortex.initializer.support.impl.AbstractInitializer;
import tribefire.extension.modelling_wb.wire.ModellingWbInitializerWireModule;
import tribefire.extension.modelling_wb.wire.contract.ModellingWbInitializerContract;
import tribefire.extension.modelling_wb.wire.contract.ModellingWbInitializerMainContract;

public class ModellingWbInitializer extends AbstractInitializer<ModellingWbInitializerMainContract> {

	@Override
	public WireTerminalModule<ModellingWbInitializerMainContract> getInitializerWireModule() {
		return ModellingWbInitializerWireModule.INSTANCE;
	}
	
	@Override
	public void initialize(PersistenceInitializationContext context, WiredInitializerContext<ModellingWbInitializerMainContract> initializerContext,
			ModellingWbInitializerMainContract mainContract) {

		DefaultWbContract workbench = mainContract.workbenchContract();
		ModellingWbInitializerContract modellingWb = mainContract.initializerContract();
		
		// add entry point
		workbench.defaultRootPerspective().getFolders().add(modellingWb.entryPointFolder());
		
		// add actions
		workbench.defaultActionbarFolder().getSubFolders().remove(workbench.deleteEntityFolder());
		workbench.defaultActionbarFolder().getSubFolders().remove(workbench.workWithEntityFolder());
		workbench.defaultActionbarFolder().getSubFolders().remove(workbench.addToClipboardFolder());
		
		workbench.defaultActionbarFolder().getSubFolders().addAll(modellingWb.actionbarFolders());
	}
}
