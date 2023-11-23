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
package com.braintribe.devrock.artifactcontainer.commands;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.braintribe.cfg.Configurable;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerStatus;
import com.braintribe.devrock.artifactcontainer.quickImport.ui.QuickImportDialog;
import com.braintribe.model.malaclypse.cfg.preferences.ac.qi.QuickImportAction;
import com.braintribe.plugin.commons.commands.SingleDropdownHandler;
import com.braintribe.plugin.commons.selection.TargetProvider;

public class QuickImportProjectCommand extends SingleDropdownHandler {
	private TargetProvider targetProvider = this;
	private String initialQuery;
	
	protected QuickImportAction action = QuickImportAction.importProject;
	
	@Configurable
	public void setInitialQuery(String initialQuery) {
		this.initialQuery = initialQuery;
	}
	
	@Configurable
	public void setAction(QuickImportAction action) {
		this.action = action;
	}
	
	
	@Override
	public void process(String parameter) {	
		Display display = PlatformUI.getWorkbench().getDisplay();
		//final Shell shell = new Shell (display, QuickImportDialog.SHELL_STYLE);
		final Shell shell = display.getActiveShell();
		
		Job job = new Job("Running QuickImporter") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {						
					//																					
					shell.getDisplay().asyncExec( new Runnable() {
						
						@Override
						public void run() {
							QuickImportDialog quickImportDialog = new QuickImportDialog( shell);
							quickImportDialog.setImportAction(action);															
							quickImportDialog.setInitialQuery(initialQuery);
							quickImportDialog.setTargetProvider( targetProvider);
							quickImportDialog.open();									
						}
					});
					
			
					return Status.OK_STATUS;
				} catch (Exception e) {					
					ArtifactContainerStatus status = new ArtifactContainerStatus("Launching Quick Importer failed", e);
					ArtifactContainerPlugin.getInstance().log(status);	
				}
				return Status.CANCEL_STATUS;
			}
			
		};
		
		try {
			job.schedule();
		} catch (Exception e) {
			ArtifactContainerStatus status = new ArtifactContainerStatus("Running Quick Importer failed", e);
			ArtifactContainerPlugin.getInstance().log(status);	
		}
	}	
}
