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
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.braintribe.devrock.api.selection.SelectionExtracter;
import com.braintribe.devrock.api.storagelocker.StorageLockerSlots;
import com.braintribe.devrock.eclipse.model.storage.StorageLockerPayload;
import com.braintribe.devrock.eclipse.model.workspace.Workspace;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.devrock.plugin.DevrockPluginStatus;
import com.braintribe.devrock.workspace.WorkspacePopulationMarshaller;

public class ExportWorkspacePopulation extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		
		boolean selectiveExport = DevrockPlugin.instance().storageLocker().getValue(StorageLockerSlots.SLOT_WS_IMPORT_USE_SELECTIVE_EXPORT, false);
		
		boolean selective = false;
		WorkspacePopulationMarshaller wpm = new WorkspacePopulationMarshaller();
		Workspace content;
		if (!selectiveExport) {		
			content = wpm.extractWorkspaceContent();
		}
		else {
			ISelection iSelection = SelectionExtracter.currentSelection();
			
			if (iSelection == null || iSelection.isEmpty()) {
				content = wpm.extractWorkspaceContent();

				DevrockPluginStatus status = new DevrockPluginStatus("Nothing selected, full workspace export done", IStatus.INFO);
				DevrockPlugin.instance().log(status);
			}
			else {									
				content = wpm.extractWorkspaceContent( iSelection);
				selective = true;
			}
		}
	
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		FileDialog fd = new FileDialog(shell, SWT.SAVE);
		if (selective) {
			fd.setText("Select target file to write-out the selected content");
		}
		else {
			fd.setText("Select target file to write-out the full content of the workspace");
		}
		fd.setFilterExtensions( new String[] {"*.yaml"});
		
		String selectedYaml = fd.open();
		if (selectedYaml == null) {
			return null;
		}
		
		if (!selectedYaml.toLowerCase().endsWith( ".yaml")) {
			selectedYaml += ".yaml";
		}		
		File file = new File( selectedYaml);
		
		if (DevrockPlugin.instance().storageLocker().getValue(StorageLockerSlots.SLOT_WS_IMPORT_INCLUDE_STORAGE_LOCKER_DATA, false)) {
			StorageLockerPayload storageLockerPayload = DevrockPlugin.instance().storageLocker().content();		
			wpm.dump(content, storageLockerPayload, file);
		}
		else {
			wpm.dump(content, null, file);
		}
		
		return null;
	}
	
	

}
