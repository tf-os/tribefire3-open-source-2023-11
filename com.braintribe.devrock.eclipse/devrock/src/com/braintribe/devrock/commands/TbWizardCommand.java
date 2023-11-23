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

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.braintribe.devrock.api.commands.SingleDropdownHandler;
import com.braintribe.devrock.api.selection.EnhancedSelectionExtracter;
import com.braintribe.devrock.api.selection.SelectionExtracter;
import com.braintribe.devrock.eclipse.model.identification.EnhancedCompiledArtifactIdentification;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.devrock.plugin.DevrockPluginStatus;
import com.braintribe.devrock.tbrunner.TbWizardDialog;



/**
 * command for TB runner
 * @author pit
 *
 */
public class TbWizardCommand extends SingleDropdownHandler {
	
	@Override
	public void process(String parameter) {	
		Display display = PlatformUI.getWorkbench().getDisplay();
		final Shell shell = display.getActiveShell();		
		ISelection selection = SelectionExtracter.currentSelection();
		final List<EnhancedCompiledArtifactIdentification> selectedArtifacts = EnhancedSelectionExtracter.extractSelectedArtifacts(selection);		
		
		
		Job job = new Job("Running TB Wizard") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {						
					//																					
					shell.getDisplay().asyncExec( new Runnable() {
						
						@Override
						public void run() {
							TbWizardDialog antWizard = new TbWizardDialog( shell);
							if (selectedArtifacts != null) {
								antWizard.setSelectedTargetArtifacts(selectedArtifacts);								
							}
							antWizard.open();									
						}
					});
					
			
					return Status.OK_STATUS;
				} catch (Exception e) {		
					DevrockPluginStatus status = new DevrockPluginStatus( "Launching the TB Wizard failed", e);
					DevrockPlugin.instance().log(status);									
				}
				return Status.CANCEL_STATUS;
			}
			
		};
		
		try {
			job.schedule();
		} catch (Exception e) {
			DevrockPluginStatus status = new DevrockPluginStatus( "Running the Ant Wizard failed", e);
			DevrockPlugin.instance().log(status);	
		}
	}	
}
