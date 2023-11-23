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
package tribefire.cortex.assets.darktheme_wb_initializer;

import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.model.resource.Resource;
import com.braintribe.wire.api.module.WireTerminalModule;

import tribefire.cortex.assets.darktheme_wb_initializer.wire.DarkthemeWbWireModule;
import tribefire.cortex.assets.darktheme_wb_initializer.wire.contract.DarkthemeWbIconContract;
import tribefire.cortex.assets.darktheme_wb_initializer.wire.contract.DarkthemeWbStyleContract;
import tribefire.cortex.assets.default_wb_initializer.wire.contract.DefaultWbContract;
import tribefire.cortex.initializer.support.api.WiredInitializerContext;
import tribefire.cortex.initializer.support.impl.AbstractInitializer;

public class DarkthemeWbInitializer extends AbstractInitializer<DarkthemeWbStyleContract> {

	@Override
	public WireTerminalModule<DarkthemeWbStyleContract> getInitializerWireModule() {
		return DarkthemeWbWireModule.INSTANCE;
	}
	
	@Override
	protected void initialize(PersistenceInitializationContext context,
			WiredInitializerContext<DarkthemeWbStyleContract> initializerContext, DarkthemeWbStyleContract initializerContract) {
		
		Resource grayishBlueStylesheet = initializerContract.styleSheet();
		
		DefaultWbContract workbench = initializerContract.workbenchContract();
		DarkthemeWbIconContract icon = initializerContract.iconContract();
		
		workbench.defaultWorkbenchConfiguration().setStylesheet(grayishBlueStylesheet);
		
		workbench.executeServiceRequestFolder().setIcon(icon.run());
		workbench.tbLogoFolder().setIcon(icon.logo());
		workbench.homeConstellationFolder().setIcon(icon.home());
		workbench.changesConstellationFolder().setIcon(icon.changes());
		workbench.transientChangesConstellationFolder().setIcon(icon.changes());
		workbench.clipboardConstellationFolder().setIcon(icon.clipboard());
		workbench.notificationsConstellationFolder().setIcon(icon.notification());
		workbench.quickAccessConstellationFolder().setIcon(icon.magnifier());
		workbench.newFolder().setIcon(icon.newIcon());
		workbench.uploadFolder().setIcon(icon.upload());
		workbench.undoFolder().setIcon(icon.undo());
		workbench.redoFolder().setIcon(icon.redo());
		workbench.commitFolder().setIcon(icon.commit());
		
	}
	
}