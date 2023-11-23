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
package tribefire.extension.modelling.management_wb;

import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.wire.api.module.WireTerminalModule;

import tribefire.cortex.assets.default_wb_initializer.wire.contract.DefaultWbContract;
import tribefire.cortex.initializer.support.api.WiredInitializerContext;
import tribefire.cortex.initializer.support.impl.AbstractInitializer;
import tribefire.extension.modelling.management_wb.wire.ManagementWbInitializerWireModule;
import tribefire.extension.modelling.management_wb.wire.contract.ManagementWbInitializerContract;
import tribefire.extension.modelling.management_wb.wire.contract.ManagementWbInitializerMainContract;

public class ManagementWbInitializer extends AbstractInitializer<ManagementWbInitializerMainContract> {

	@Override
	public WireTerminalModule<ManagementWbInitializerMainContract> getInitializerWireModule() {
		return ManagementWbInitializerWireModule.INSTANCE;
	}
	
	@Override
	public void initialize(PersistenceInitializationContext context, WiredInitializerContext<ManagementWbInitializerMainContract> initializerContext,
			ManagementWbInitializerMainContract mainContract) {

		DefaultWbContract workbench = mainContract.workbenchContract();
		ManagementWbInitializerContract managementWb = mainContract.initializerContract();
		
		// add entry point
		workbench.defaultRootPerspective().getFolders().add(managementWb.entryPointFolder());
		
		// add to home folder
		workbench.defaultHomeFolderPerspective().getFolders().add(managementWb.projectFolder());
		
		// add actions
		workbench.defaultActionbarFolder().getSubFolders().remove(workbench.deleteEntityFolder());
		workbench.defaultActionbarFolder().getSubFolders().remove(workbench.workWithEntityFolder());
		workbench.defaultActionbarFolder().getSubFolders().remove(workbench.addToClipboardFolder());
		workbench.defaultActionbarFolder().getSubFolders().addAll(managementWb.actionbarFolders());
	}
}
