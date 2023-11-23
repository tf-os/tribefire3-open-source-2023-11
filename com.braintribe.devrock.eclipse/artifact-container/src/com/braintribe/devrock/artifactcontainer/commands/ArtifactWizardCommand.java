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
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerStatus;
import com.braintribe.devrock.artifactcontainer.ui.wizard.ArtifactWizardDialog;
import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.artifact.processing.artifact.ArtifactProcessor;
import com.braintribe.plugin.commons.commands.SingleDropdownHandler;
import com.braintribe.plugin.commons.selection.SelectionExtractor;

/**
 * command for the artifact cloner / copier
 * @author pit
 *
 */
public class ArtifactWizardCommand extends SingleDropdownHandler {
	
	@Override
	public void process(String parameter) {	
		Display display = PlatformUI.getWorkbench().getDisplay();
		final Shell shell = display.getActiveShell();		
		ISelection selection = SelectionExtractor.getCurrentPackageExplorerSelection();
		final Artifact selectedArtifact = SelectionExtractor.extractSelectedArtifact(selection);
		
		
		
		Job job = new Job("Running Artifact Wizard") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {						
					//																					
					shell.getDisplay().asyncExec( new Runnable() {
						
						@Override
						public void run() {
							ArtifactWizardDialog artifactWizard = new ArtifactWizardDialog( shell);
							if (selectedArtifact != null) {
								artifactWizard.setSelectedTargetArtifact(selectedArtifact);
								Artifact source = Artifact.T.create();
								ArtifactProcessor.transferIdentification(source, selectedArtifact);
								artifactWizard.setSelectedSourceArtifact(source);
							}
							artifactWizard.open();									
						}
					});
					
			
					return Status.OK_STATUS;
				} catch (Exception e) {					
					ArtifactContainerStatus status = new ArtifactContainerStatus( "Launching the Artifact Wizard failed", e);
					ArtifactContainerPlugin.getInstance().log(status);	
				}
				return Status.CANCEL_STATUS;
			}
			
		};
		
		try {
			job.schedule();
		} catch (Exception e) {
			ArtifactContainerStatus status = new ArtifactContainerStatus( "Running the Artifact Wizard failed", e);
			ArtifactContainerPlugin.getInstance().log(status);	
		}
	}	
}
