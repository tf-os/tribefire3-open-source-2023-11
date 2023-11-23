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
package com.braintribe.gwt.gme.notification.client.expert;

import com.braintribe.cfg.Required;
import com.braintribe.gwt.gme.constellation.client.ExplorerConstellation;
import com.braintribe.gwt.logging.client.Logger;
import com.braintribe.model.folder.Folder;
import com.braintribe.model.processing.notification.api.CommandExpert;
import com.braintribe.model.uicommand.RunWorkbenchAction;

public class RunWorkbenchActionExpert implements CommandExpert<RunWorkbenchAction> {

	private static Logger logger = new Logger(RunWorkbenchActionExpert.class);
	private ExplorerConstellation explorerConstellation;
	
	@Required
	public void setExplorerConstellation(ExplorerConstellation explorerConstellation) {
		this.explorerConstellation = explorerConstellation;
	}	

	@Override
	public void handleCommand(RunWorkbenchAction command) {
		Object workbenchFolderID = command.getWorkbenchFolderId();
		Folder rootFolder = explorerConstellation.getWorkbench().getRootFolder();
		if (rootFolder == null) {
			logger.info("RunWorkbenchAction - no Root Folder defined in Workbench!");
			return;
		}
		
		Object id = rootFolder.getId();
		Folder folder = null;
		if (id.equals(workbenchFolderID))
			folder = rootFolder;
		else
			folder = findSubFolder(rootFolder, workbenchFolderID);
		
		if (folder == null) {
			logger.info("RunWorkbenchAction - no Folder found for: " + id.toString());
			return;
		}
		
		explorerConstellation.runWorkbenchAction(folder, command);
	}

	private Folder findSubFolder(Folder parentFolder, Object id) {		
		if (parentFolder == null || parentFolder.getSubFolders() == null || parentFolder.getSubFolders().isEmpty())
			return null;

		Folder folder = null;
		for (Folder subFolder : parentFolder.getSubFolders()) {
			Object subFolderId = subFolder.getId();
			if (subFolderId.equals(id)) 
				return subFolder;
			if (subFolder.getSubFolders() != null) {
				folder = findSubFolder(subFolder, id);
				if (folder != null)
					return folder;
			}
		}		
		return folder;		
	}
	
}
