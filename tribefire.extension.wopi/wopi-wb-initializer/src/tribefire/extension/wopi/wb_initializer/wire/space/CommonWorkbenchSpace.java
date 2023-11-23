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
package tribefire.extension.wopi.wb_initializer.wire.space;

import com.braintribe.logging.Logger;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.assets.default_wb_initializer.wire.contract.DefaultWbContract;
import tribefire.cortex.initializer.support.wire.space.AbstractInitializerSpace;
import tribefire.extension.wopi.wb_initializer.wire.contract.CommonWorkbenchContract;
import tribefire.extension.wopi.wb_initializer.wire.contract.IconContract;

@Managed
public class CommonWorkbenchSpace extends AbstractInitializerSpace implements CommonWorkbenchContract {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CommonWorkbenchSpace.class);

	@Import
	private DefaultWbContract workbench;

	@Import
	private IconContract icons;

	@Override
	public void standardActions() {

		workbench.exchangeContentViewFolder().setIcon(icons.viewIcon());
		// workbench.expandFolder().setIcon(icons.expandIcon());
		workbench.workWithEntityFolder().setIcon(icons.openIcon());
		workbench.gimaOpenerFolder().setIcon(icons.infoIcon());
		workbench.gimaOpenerFolder().getDisplayName().putDefault("Details");
		workbench.gimaOpenerFolder().setName("$gimaOpenerForDetails");
		workbench.deleteEntityFolder().setIcon(icons.deleteIcon());
		workbench.changeInstanceFolder().setIcon(icons.assignIcon());
		workbench.clearEntityToNullFolder().setIcon(icons.removeIcon());
		workbench.addToCollectionFolder().setIcon(icons.addIcon());
		workbench.insertBeforeToListFolder().setIcon(icons.addIcon());
		workbench.removeFromCollectionFolder().setIcon(icons.removeIcon());
		workbench.clearCollectionFolder().setIcon(icons.removeIcon());
		workbench.resourceDownloadFolder().setIcon(icons.downloadIcon());
		workbench.executeServiceRequestFolder().setIcon(icons.runIcon());
		workbench.homeConstellationFolder().setIcon(icons.homeIcon());
		workbench.notificationsConstellationFolder().setIcon(icons.mailIcon());
		workbench.clipboardConstellationFolder().setIcon(icons.copyIcon());
		workbench.changesConstellationFolder().setIcon(icons.changesIcon());
		workbench.quickAccessConstellationFolder().setIcon(icons.quickAccessIcon());

		workbench.newFolder().setIcon(icons.addIcon());
		workbench.uploadFolder().setIcon(icons.uploadIcon());
		workbench.undoFolder().setIcon(icons.backIcon());
		workbench.redoFolder().setIcon(icons.nextIcon());
		workbench.commitFolder().setIcon(icons.commitIcon());
		// workbench.refreshEntitiesFolder().setIcon(icons.refreshIcon());
		// workbench.addToClipboardFolder()

		workbench.defaultActionbarFolder().getSubFolders().remove(workbench.addToClipboardFolder());
		workbench.insertBeforeToListFolder().getDisplayName().putDefault("Insert");

		workbench.defaultTabActionbarFolder().getSubFolders().remove(workbench.clipboardConstellationFolder());
		workbench.defaultTabActionbarFolder().getSubFolders().remove(workbench.changesConstellationFolder());

		workbench.defaultSelectionFolder().getSubFolders().remove(workbench.homeConstellationFolder());
		workbench.defaultSelectionFolder().getSubFolders().remove(workbench.changesConstellationFolder());

	}

}
