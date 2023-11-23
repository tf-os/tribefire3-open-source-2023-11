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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.braintribe.devrock.importer.dependencies.ui.RemoteDependencyImportDialog;
import com.braintribe.devrock.importer.scanner.ui.QuickImportDialog;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.devrock.plugin.DevrockPluginStatus;

public class QuickImportRemoteDependencyCommand extends AbstractHandler{
		
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		Display display = PlatformUI.getWorkbench().getDisplay();
		final Shell shell = new Shell (display, QuickImportDialog.SHELL_STYLE);
		
		Job job = new Job("Running Remote QuickImporter") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {						
					//																					
					shell.getDisplay().asyncExec( new Runnable() {
						
						@Override
						public void run() {
							RemoteDependencyImportDialog quickImportDialog = new RemoteDependencyImportDialog( shell);							
							quickImportDialog.open();									
						}
					});								
					return Status.OK_STATUS;
				} catch (Exception e) {		
					DevrockPluginStatus status = new DevrockPluginStatus("Launching Quick Importer failed", e);
					DevrockPlugin.instance().log(status);	
					
				}
				return Status.CANCEL_STATUS;
			}			
		};
		
		try {
			job.schedule();
		} catch (Exception e) {
			DevrockPluginStatus status = new DevrockPluginStatus("Running Quick Importer failed", e);
			DevrockPlugin.instance().log(status);	
		}
		return null;
		
	}

}
