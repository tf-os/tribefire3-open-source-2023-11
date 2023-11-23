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
package tribefire.cortex.assets.default_wb_initializer.wire.contract;

import com.braintribe.model.folder.Folder;
import com.braintribe.model.workbench.WorkbenchConfiguration;
import com.braintribe.model.workbench.WorkbenchPerspective;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.cortex.initializer.support.impl.lookup.InstanceLookup;

@InstanceLookup
public interface DefaultWbContract extends WireSpace {
	
	WorkbenchConfiguration defaultWorkbenchConfiguration();
	
	WorkbenchPerspective defaultRootPerspective();

	WorkbenchPerspective defaultHomeFolderPerspective();
	
	WorkbenchPerspective defaultActionbarPerspective();
	
	WorkbenchPerspective defaultGlobalActionbarPerspective();

	WorkbenchPerspective defaultHeaderbarPerspective();

	WorkbenchPerspective defaultTabActionbarPerspective();
	
	WorkbenchPerspective defaultViewActionbarPerspective();

	Folder defaultActionbarFolder();
	
	Folder defaultViewActionbarFolder();

	Folder exchangeContentViewFolder();
	
	Folder expandFolder();
	
	Folder showDetailsFolder();
	
	Folder restoreFolder();
	
	Folder hideDetailsFolder();

	Folder workWithEntityFolder();

	Folder gimaOpenerFolder();

	Folder deleteEntityFolder();

	Folder changeInstanceFolder();

	Folder clearEntityToNullFolder();

	Folder addToCollectionFolder();

	Folder insertBeforeToListFolder();

	Folder removeFromCollectionFolder();

	Folder clearCollectionFolder();

	Folder refreshEntitiesFolder();

	Folder resourceDownloadFolder();

	Folder executeServiceRequestFolder();

	Folder addToClipboardFolder();
	
	Folder defaultHeaderbarFolder();

	Folder tbLogoFolder();

	Folder quickAccessSlotFolder();

	Folder globalStateSlotFolder();

	Folder defaultSettingsMenuFolder();

	Folder reloadSessionFolder();

	Folder showSettingsFolder();

	Folder uiThemeFolder();

	Folder showAboutFolder();

	Folder defaultUserMenuFolder();

	Folder showUserProfileFolder();

	Folder showLogoutFolder();

	Folder defaultTabActionbarFolder();

	Folder defaultExplorerFolder();

	Folder homeConstellationFolder();

	Folder changesConstellationFolder();

	Folder transientChangesConstellationFolder();

	Folder clipboardConstellationFolder();

	Folder notificationsConstellationFolder();

	Folder defaultSelectionFolder();

	Folder quickAccessConstellationFolder();

	Folder expertUiFolder();
	
	Folder defaultGlobalActionbarFolder();

	Folder newFolder();

	Folder dualSectionButtonsFolder();

	Folder uploadFolder();

	Folder undoFolder();

	Folder redoFolder();

	Folder commitFolder();

}
