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
package tribefire.extension.wopi.wb_initializer;

import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.wire.api.module.WireTerminalModule;

import tribefire.cortex.assets.default_wb_initializer.wire.contract.DefaultWbContract;
import tribefire.cortex.initializer.support.api.WiredInitializerContext;
import tribefire.cortex.initializer.support.impl.AbstractInitializer;
import tribefire.extension.wopi.wb_initializer.wire.WopiWbInitializerWireModule;
import tribefire.extension.wopi.wb_initializer.wire.contract.CommonWorkbenchContract;
import tribefire.extension.wopi.wb_initializer.wire.contract.IconContract;
import tribefire.extension.wopi.wb_initializer.wire.contract.ResourcesContract;
import tribefire.extension.wopi.wb_initializer.wire.contract.WopiWbInitializerContract;
import tribefire.extension.wopi.wb_initializer.wire.contract.WopiWbInitializerMainContract;

public class WopiWbInitializer extends AbstractInitializer<WopiWbInitializerMainContract> {

	@Override
	public WireTerminalModule<WopiWbInitializerMainContract> getInitializerWireModule() {
		return WopiWbInitializerWireModule.INSTANCE;
	}

	@Override
	public void initialize(PersistenceInitializationContext context, WiredInitializerContext<WopiWbInitializerMainContract> initializerContext,
			WopiWbInitializerMainContract initializerMainContract) {

		DefaultWbContract workbench = initializerMainContract.workbench();
		IconContract icons = initializerMainContract.icons();
		WopiWbInitializerContract wopiWorkbench = initializerMainContract.initializer();
		ResourcesContract resources = initializerMainContract.resources();
		CommonWorkbenchContract commonWorkbench = initializerMainContract.commonWorkbench();

		// update standard actions
		commonWorkbench.standardActions();

		// update header bar
		workbench.tbLogoFolder().setIcon(icons.logoIcon());

		// add entry point
		workbench.defaultRootPerspective().getFolders().add(wopiWorkbench.entryPointFolder());

		// update action bar
		workbench.defaultActionbarFolder().getSubFolders().addAll(wopiWorkbench.actionbarFolders());

		// -----------------------------------------------------------------------
		// ADD TO HOME FOLDER
		// -----------------------------------------------------------------------

		// workbenchContract.defaultHomeFolderPerspective().getFolders().addAll(list(wopiWorkbench.wopi()));

		// -----------------------------------------------------------------------
		// ADD ACTIONS
		// -----------------------------------------------------------------------

		// -----------------------------------------------------------------------
		// EXPLORER STYLING
		// -----------------------------------------------------------------------

		String explorerTitle = "WOPI Administrator";
		workbench.defaultWorkbenchConfiguration().setTitle(explorerTitle);
		workbench.defaultWorkbenchConfiguration().setFavIcon(resources.favIconOrange());
		updateCss(workbench, resources);
	}

	// -----------------------------------------------------------------------
	// HELPERS
	// -----------------------------------------------------------------------

	private void updateCss(DefaultWbContract workbench, ResourcesContract resources) {
		workbench.defaultWorkbenchConfiguration().setStylesheet(resources.extensionWopiExplorerCss());
	}
}
