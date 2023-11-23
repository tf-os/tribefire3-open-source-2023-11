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

import java.io.File;
import java.util.ArrayList;
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
import com.braintribe.devrock.artifactcontainer.plugin.malaclypse.scope.wirings.MalaclypseWirings;
import com.braintribe.devrock.artifactcontainer.ui.intelligence.ArtifactIntelligenceDialog;
import com.braintribe.devrock.artifactcontainer.ui.intelligence.manual.DirectEntryArtifactDialog;
import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.artifact.ArtifactProcessor;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.artifact.processing.version.VersionRangeProcessor;
import com.braintribe.model.artifact.version.VersionRange;
import com.braintribe.plugin.commons.commands.SingleDropdownHandler;
import com.braintribe.plugin.commons.selection.PackageExplorerSelectedJarsTuple;
import com.braintribe.plugin.commons.selection.SelectionExtractor;

/**
 * command for artifact intelligence dialog 
 * @author pit
 *
 */
public abstract class AbstractArtifactInformationCommand extends SingleDropdownHandler {
	private String PARM_MSG = "com.braintribe.devrock.artifactcontainer.common.commands.command.param.artifactInformation";
	//private static final String paramForUseSelection = "useSelection";
	protected static final String paramForIgnoreSelection = "ignoreSelection";

	@Override
	protected String getParamKey() {	
		return PARM_MSG;
	}
	
	@Override
	public void process(String parameter) {	
		Display display = PlatformUI.getWorkbench().getDisplay();
		final Shell shell = display.getActiveShell();		
		
		
		
		ISelection selection = SelectionExtractor.getCurrentPackageExplorerSelection();
		final List<Artifact> selectedArtifacts = SelectionExtractor.extractSelectedProjectsArtifact(selection);
		
		final PackageExplorerSelectedJarsTuple jarTuple = SelectionExtractor.extractSelectedJars();
		List<String> jars = jarTuple.selectedJars;
		List<Solution> selections = new ArrayList<>();
		
		if (
				jars == null || jars.size() == 0 || 
				(parameter != null && parameter.equalsIgnoreCase( paramForIgnoreSelection))
			) {
			// no jars selected -> select the solution 
			DirectEntryArtifactDialog dlg = new DirectEntryArtifactDialog( "artifact-index analysis entry point specification", shell);
			if ( (parameter == null || !parameter.equalsIgnoreCase(paramForIgnoreSelection)) && 
				 selectedArtifacts != null && selectedArtifacts.size() > 0	
				){
				Dependency dependency = Dependency.T.create();
				ArtifactProcessor.transferIdentification(dependency,  selectedArtifacts.get(0));
				VersionRange range = VersionRangeProcessor.hotfixRange( selectedArtifacts.get(0).getVersion());
				dependency.setVersionRange( range);
				dlg.setInitial(dependency);			
			}
			if (dlg.open() == 0) {
				List<Solution> solutions = dlg.getSelection();
				if (solutions != null && solutions.size() > 0) {
					selections.addAll( solutions);
				}
			}
		}
		else {
			//					
			File localRepository = new File( MalaclypseWirings.fullClasspathResolverContract().contract().settingsReader().getLocalRepository());						
			
			for (String jar : jars) {				
				File jarFile = new File( jar);
				
				String remainder = jarFile.getAbsolutePath().substring( localRepository.getAbsolutePath().length());
				String [] path = remainder.replace( File.separatorChar, '/').split( "/");
				int len = path.length;
				
				Solution solution = Solution.T.create();
				StringBuffer grp = new StringBuffer();
				for (int i = 0; i < len - 3; i++) {
					if (grp.length() > 0) {
						grp.append(".");
					}
					grp.append( path[i]);
				}
				solution.setGroupId( grp.toString());
				solution.setArtifactId( path[ len - 3]);
				solution.setVersion( VersionProcessor.createFromString( path[len-2]));
				
				selections.add(solution);
			}
		}
		
		if (selections.size() == 0)
			return;

		Job job = new Job("Running Artifact Intelligence Dialog") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {						
					//																					
					shell.getDisplay().asyncExec( new Runnable() {
						
						@Override
						public void run() {
							ArtifactIntelligenceDialog antWizard = new ArtifactIntelligenceDialog( shell);
							if (jars != null) {
								antWizard.setSelection( selections);								
							}
							antWizard.open();									
						}
					});
					
			
					return Status.OK_STATUS;
				} catch (Exception e) {		
					ArtifactContainerStatus status = new ArtifactContainerStatus( "Launching the Artifact Intelligence Dialog failed", e);
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
