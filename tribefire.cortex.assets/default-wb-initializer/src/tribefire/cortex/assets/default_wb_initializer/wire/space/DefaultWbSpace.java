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
package tribefire.cortex.assets.default_wb_initializer.wire.space;

import static com.braintribe.wire.api.util.Lists.list;

import com.braintribe.model.folder.Folder;
import com.braintribe.model.workbench.WorkbenchConfiguration;
import com.braintribe.model.workbench.WorkbenchPerspective;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.assets.default_wb_initializer.wire.contract.DefaultWbContract;
import tribefire.cortex.initializer.support.wire.space.AbstractInitializerSpace;

@Managed
public class DefaultWbSpace extends AbstractInitializerSpace implements DefaultWbContract {
	
	@Managed
	@Override
	public WorkbenchPerspective defaultRootPerspective() {
		return create(WorkbenchPerspective.T).workbenchPerspective("root");
	}
	
	@Managed
	@Override
	public WorkbenchPerspective defaultHomeFolderPerspective() {
		return create(WorkbenchPerspective.T).workbenchPerspective("homeFolder");
	}
	
	@Managed
	@Override
	public WorkbenchPerspective defaultActionbarPerspective() {
		WorkbenchPerspective bean = create(WorkbenchPerspective.T).workbenchPerspective("actionbar");
		
		bean.setFolders(list(
				defaultActionbarFolder()
				));
		
		return bean;
	}
	
	@Managed
	@Override
	public WorkbenchPerspective defaultGlobalActionbarPerspective() {
		WorkbenchPerspective bean = create(WorkbenchPerspective.T).workbenchPerspective("global-actionbar");
		
		bean.setFolders(list(
				defaultGlobalActionbarFolder()
				));
		
		return bean;
	}
	
	@Managed
	@Override
	public WorkbenchPerspective defaultHeaderbarPerspective() {
		WorkbenchPerspective bean = create(WorkbenchPerspective.T).workbenchPerspective("headerbar");
		
		bean.setFolders(list(
				defaultHeaderbarFolder()
				));
		
		return bean;
	}
	
	@Managed
	@Override
	public WorkbenchPerspective defaultTabActionbarPerspective() {
		WorkbenchPerspective bean = create(WorkbenchPerspective.T).workbenchPerspective("tab-actionbar");
		
		bean.setFolders(list(
				defaultTabActionbarFolder()
				));
		
		return bean;
	}
	
	@Managed
	@Override
	public WorkbenchPerspective defaultViewActionbarPerspective() {
		WorkbenchPerspective bean = create(WorkbenchPerspective.T).workbenchPerspective("view-actionbar");
		
		bean.setFolders(list(
				defaultViewActionbarFolder()
				));
		
		return bean;
	}
	
	@Managed
	@Override
	public Folder defaultActionbarFolder() {
		Folder bean = create(Folder.T).initFolder("actionbar", "Action Bar");
		
		bean.setSubFolders(list(
				workWithEntityFolder(),
				gimaOpenerFolder(),
				deleteEntityFolder(),
				changeInstanceFolder(),
				clearEntityToNullFolder(),
				addToCollectionFolder(),
				insertBeforeToListFolder(),
				removeFromCollectionFolder(),
				clearCollectionFolder(),
				refreshEntitiesFolder(),
				resourceDownloadFolder(),
				executeServiceRequestFolder(),
				addToClipboardFolder()
				));
		
		return bean;
	}

	@Managed
	@Override
	public Folder defaultViewActionbarFolder() {
		Folder bean = create(Folder.T).initFolder("view-actionbar", "View Action Bar");
		
		bean.setSubFolders(list(
				exchangeContentViewFolder(),
				expandFolder(),
				showDetailsFolder()
				));
		
		return bean;
	}

	@Managed
	@Override
	public Folder exchangeContentViewFolder() {
		 return create(Folder.T).initFolder("$exchangeContentView", "View");
	}

	@Managed
	@Override
	public Folder expandFolder() {
		 Folder bean = create(Folder.T).initFolder("$maximize", "Expand");
		 bean.getSubFolders().add(restoreFolder());
		 return bean;
	}
	
	@Managed
	@Override
	public Folder restoreFolder() {
		 Folder bean = create(Folder.T).initFolder("$restore", "Restore");
		 bean.setParent(expandFolder());
		 return bean;
	}

	@Managed
	@Override
	public Folder showDetailsFolder() {
		 Folder bean = create(Folder.T).initFolder("$showDetailsPanel", "Show Details");
		 bean.getSubFolders().add(hideDetailsFolder());
		 return bean;
	}
	
	@Managed
	@Override
	public Folder hideDetailsFolder() {
		 Folder bean = create(Folder.T).initFolder("$hideDetailsPanel", "Hide Details");
		 bean.setParent(showDetailsFolder());
		 return bean;
	}

	@Managed
	@Override
	public Folder workWithEntityFolder() {
		return create(Folder.T).initFolder("$workWithEntity", "Open");
	}
	
	@Managed
	@Override
	public Folder gimaOpenerFolder() {
		return create(Folder.T).initFolder("$gimaOpener", "Edit");
	}
	
	@Managed
	@Override
	public Folder deleteEntityFolder() {
		return create(Folder.T).initFolder("$deleteEntity", "Delete");
	}
	
	@Managed
	@Override
	public Folder changeInstanceFolder() {
		return create(Folder.T).initFolder("$changeInstance", "Assign");
	}
	
	@Managed
	@Override
	public Folder clearEntityToNullFolder() {
		return create(Folder.T).initFolder("$clearEntityToNull", "Remove");
	}
	
	@Managed
	@Override
	public Folder addToCollectionFolder() {
		return create(Folder.T).initFolder("$addToCollection", "Add");
	}
	
	@Managed
	@Override
	public Folder insertBeforeToListFolder() {
		return create(Folder.T).initFolder("$insertBeforeToList", "Insert Before");
	}
	
	@Managed
	@Override
	public Folder removeFromCollectionFolder() {
		return create(Folder.T).initFolder("$removeFromCollection", "Remove");
	}
	
	@Managed
	@Override
	public Folder clearCollectionFolder() {
		return create(Folder.T).initFolder("$clearCollection", "Clear");
	}
	
	@Managed
	@Override
	public Folder refreshEntitiesFolder() {
		return create(Folder.T).initFolder("$refreshEntities", "Refresh");
	}
	
	@Managed
	@Override
	public Folder resourceDownloadFolder() {
		return create(Folder.T).initFolder("$ResourceDownload", "Download");
	}
	
	@Managed
	@Override
	public Folder executeServiceRequestFolder() {
		return create(Folder.T).initFolder("$executeServiceRequest", "Execute");
	}
	
	@Managed
	@Override
	public Folder addToClipboardFolder() {
		return create(Folder.T).initFolder("$addToClipboard", "Add To Clipboard");
	}
	
	
	
	@Managed
	@Override
	public Folder defaultHeaderbarFolder() {
		Folder bean = create(Folder.T).initFolder("headerbar", "Header Bar");
		
		bean.setSubFolders(list(
				tbLogoFolder(),
				quickAccessSlotFolder(),
				globalStateSlotFolder(),
				defaultSettingsMenuFolder(),
				defaultUserMenuFolder()
				));
		
		return bean;
	}

	@Managed
	@Override
	public Folder tbLogoFolder() {
		return create(Folder.T).initFolder("tb_Logo", "Tb Logo");
	}
	
	@Managed
	@Override
	public Folder quickAccessSlotFolder() {
		return create(Folder.T).initFolder("$quickAccess-slot", "QuickAccess Slot");
	}
	
	@Managed
	@Override
	public Folder globalStateSlotFolder() {
		return create(Folder.T).initFolder("$globalState-slot", "GlobalState Slot");
	}
	
	@Managed
	@Override
	public Folder defaultSettingsMenuFolder() {
		Folder bean = create(Folder.T).initFolder("$settingsMenu", "Settings Menu");
		
		bean.setSubFolders(list(
				reloadSessionFolder(),
				showSettingsFolder(),
				uiThemeFolder(),
				showAboutFolder()
				));
		
		return bean;
	}
	
	@Managed
	@Override
	public Folder reloadSessionFolder() {
		return create(Folder.T).initFolder("$reloadSession", "Reload Session");
	}
	
	@Managed
	@Override
	public Folder showSettingsFolder() {
		return create(Folder.T).initFolder("$showSettings", "Show Settings");
	}
	
	@Managed
	@Override
	public Folder uiThemeFolder() {
		return create(Folder.T).initFolder("$uiTheme", "UI Theme");
	}
	
	@Managed
	@Override
	public Folder showAboutFolder() {
		return create(Folder.T).initFolder("$showAbout", "Show About");
	}
	
	@Managed
	@Override
	public Folder defaultUserMenuFolder() {
		Folder bean = create(Folder.T).initFolder("$userMenu", "User Menu");
		
		bean.setSubFolders(list(
				showUserProfileFolder(),
				showLogoutFolder()
				));
		
		return bean;
	}
	
	@Managed
	@Override
	public Folder showUserProfileFolder() {
		return create(Folder.T).initFolder("$showUserProfile", "Show User Profile");
	}
	
	@Managed
	@Override
	public Folder showLogoutFolder() {
		return create(Folder.T).initFolder("$showLogout", "Show Logout");
	}
	
	
	
	@Managed
	@Override
	public Folder defaultTabActionbarFolder() {
		Folder bean = create(Folder.T).initFolder("tab-actionbar", "Tab Action Bar");
		
		bean.setSubFolders(list(
				defaultExplorerFolder(),
				defaultSelectionFolder()
				));
		
		return bean;
	}
	
	@Managed
	@Override
	public Folder defaultExplorerFolder() {
		Folder bean = create(Folder.T).initFolder("$explorer", "Explorer");
		
		bean.setSubFolders(list(
				homeConstellationFolder(),
				changesConstellationFolder(),
				transientChangesConstellationFolder(),
				clipboardConstellationFolder(),
				notificationsConstellationFolder()
				));
		
		return bean;
	}
	
	@Managed
	@Override
	public Folder homeConstellationFolder() {
		return create(Folder.T).initFolder("$homeConstellation", "Home Constellation");
	}
	
	@Managed
	@Override
	public Folder changesConstellationFolder() {
		return create(Folder.T).initFolder("$changesConstellation", "Changes Constellation");
	}
	
	@Managed
	@Override
	public Folder transientChangesConstellationFolder() {
		return create(Folder.T).initFolder("$transientChangesConstellation", "Transient Changes Constellation");
	}
	
	@Managed
	@Override
	public Folder clipboardConstellationFolder() {
		return create(Folder.T).initFolder("$clipboardConstellation", "Clipboard Constellation");
	}
	
	@Managed
	@Override
	public Folder notificationsConstellationFolder() {
		return create(Folder.T).initFolder("$notificationsConstellation", "Notifications Constellation");
	}
	
	
	
	@Managed
	@Override
	public Folder defaultSelectionFolder() {
		Folder bean = create(Folder.T).initFolder("$selection", "Selection");
		
		bean.setSubFolders(list(
				homeConstellationFolder(),
				changesConstellationFolder(),
				transientChangesConstellationFolder(),
				clipboardConstellationFolder(),
				quickAccessConstellationFolder(),
				expertUiFolder()
				));
		
		return bean;
	}
	
	@Managed
	@Override
	public Folder quickAccessConstellationFolder() {
		return create(Folder.T).initFolder("$quickAccessConstellation", "QuickAccess Constellation Folder");
	}
	
	@Managed
	@Override
	public Folder expertUiFolder() {
		return create(Folder.T).initFolder("$expertUI", "Expert UI");
	}
	
	
	
	@Managed
	@Override
	public Folder defaultGlobalActionbarFolder() {
		Folder bean = create(Folder.T).initFolder("global-actionbar", "Global Action Bar");
		
		bean.setSubFolders(list(
				newFolder(),
				dualSectionButtonsFolder(),
				uploadFolder(),
				undoFolder(),
				redoFolder(),
				commitFolder()
				));
		
		return bean;
	}
	
	@Managed
	@Override
	public Folder newFolder() {
		return create(Folder.T).initFolder("$new", "New");
	}
	
	@Managed
	@Override
	public Folder dualSectionButtonsFolder() {
		return create(Folder.T).initFolder("$dualSectionButtons", "Dual Section Buttons");
	}
	
	@Managed
	@Override
	public Folder uploadFolder() {
		return create(Folder.T).initFolder("$upload", "Upload");
	}
	
	@Managed
	@Override
	public Folder undoFolder() {
		return create(Folder.T).initFolder("$undo", "Undo");
	}
	
	@Managed
	@Override
	public Folder redoFolder() {
		return create(Folder.T).initFolder("$redo", "Redo");
	}
	
	@Managed
	@Override
	public Folder commitFolder() {
		return create(Folder.T).initFolder("$commit", "Commit");
	}
	
	@Managed
	@Override
	public WorkbenchConfiguration defaultWorkbenchConfiguration() {
		return create(WorkbenchConfiguration.T);
	}
	
}
