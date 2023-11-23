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

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerStatus;
import com.braintribe.devrock.artifactcontainer.ui.tb.TbWizardDialog;
import com.braintribe.model.artifact.Artifact;
import com.braintribe.plugin.commons.commands.SingleDropdownHandler;
import com.braintribe.plugin.commons.selection.SelectionExtractor;

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
		ISelection selection = SelectionExtractor.getCurrentPackageExplorerSelection();
		final List<Artifact> selectedArtifacts = SelectionExtractor.extractSelectedProjectsArtifact(selection);		
		
		
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
					ArtifactContainerStatus status = new ArtifactContainerStatus( "Launching the TB Wizard failed", e);
					ArtifactContainerPlugin.getInstance().log(status);									
				}
				return Status.CANCEL_STATUS;
			}
			
		};
		
		try {
			job.schedule();
		} catch (Exception e) {
			ArtifactContainerStatus status = new ArtifactContainerStatus( "Running the Ant Wizard failed", e);
			ArtifactContainerPlugin.getInstance().log(status);	
		}
	}	
}
