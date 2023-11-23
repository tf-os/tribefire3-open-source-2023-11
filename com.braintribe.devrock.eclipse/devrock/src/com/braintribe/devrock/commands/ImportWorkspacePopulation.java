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
package com.braintribe.devrock.commands;

import java.io.File;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.braintribe.devrock.api.storagelocker.StorageLockerSlots;
import com.braintribe.devrock.eclipse.model.storage.StorageLockerPayload;
import com.braintribe.devrock.eclipse.model.workspace.Workspace;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.devrock.plugin.DevrockPluginStatus;
import com.braintribe.devrock.workspace.WorkspacePopulationRestorer;
import com.braintribe.devrock.workspace.ui.WorkspaceImportSelectionDialog;


public class ImportWorkspacePopulation extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		
		boolean selectiveImport = DevrockPlugin.instance().storageLocker().getValue(StorageLockerSlots.SLOT_WS_IMPORT_USE_SELECTIVE_IMPORT, false);
		
		boolean importStoragePayload = DevrockPlugin.instance().storageLocker().getValue(StorageLockerSlots.SLOT_WS_IMPORT_INCLUDE_STORAGE_LOCKER_DATA, false);
		
		if (!selectiveImport) {
			FileDialog fd = new FileDialog(shell);
			fd.setFilterExtensions( new String[] {"*.yaml"});
			
			String selectedYaml = fd.open();
			
			if (selectedYaml == null) {
				return null;
			}
			
			File file = new File( selectedYaml);
			if (!file.exists()) {
				DevrockPluginStatus status = new DevrockPluginStatus("File [" + file.getAbsolutePath() + "] doesn't exist", IStatus.ERROR);
				DevrockPlugin.instance().log(status);
				return null;
			}
			
			WorkspacePopulationRestorer restorer = new WorkspacePopulationRestorer(file);
			restorer.runAsJob();			
		}
		else {
			WorkspaceImportSelectionDialog dialog = new WorkspaceImportSelectionDialog(shell);
			int open = dialog.open();
			if (open == Window.OK) {
				Workspace workspace = dialog.getSelection();
				if (workspace != null) {
					WorkspacePopulationRestorer restorer = null;
					if (importStoragePayload) {
						StorageLockerPayload storageLockerPayload = DevrockPlugin.instance().storageLocker().content();
						restorer = new WorkspacePopulationRestorer( workspace, storageLockerPayload);
					}
					else {					
						restorer = new WorkspacePopulationRestorer( workspace);
					}
					
					restorer.runAsJob();	
				}
			}			
		}
		
		
		
		return null;
	}
	

}
