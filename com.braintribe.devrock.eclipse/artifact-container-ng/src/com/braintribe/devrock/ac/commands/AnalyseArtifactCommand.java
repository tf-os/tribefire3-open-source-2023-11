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
package com.braintribe.devrock.ac.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.braintribe.devrock.ac.container.plugin.ArtifactContainerPlugin;
import com.braintribe.devrock.ac.container.plugin.ArtifactContainerStatus;
import com.braintribe.devrock.ac.container.resolution.viewer.ContainerResolutionViewer;
import com.braintribe.devrock.ac.container.resolution.yaml.YamlResolutionViewer;
import com.braintribe.devrock.analysis.ui.AnalysisDialog;
import com.braintribe.devrock.api.selection.EnhancedSelectionExtracter;
import com.braintribe.devrock.api.selection.SelectionExtracter;
import com.braintribe.devrock.api.storagelocker.StorageLockerSlots;
import com.braintribe.devrock.eclipse.model.identification.EnhancedCompiledArtifactIdentification;
import com.braintribe.devrock.mc.api.classpath.ClasspathResolutionScope;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.essential.Canceled;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.compiled.CompiledTerminal;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;

/**
 * allows to specify an artifact, run it's CP resolution and see the result in the Analyzer
 * 
 * @author pit
 *
 */
public class AnalyseArtifactCommand extends AbstractHandler {	

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {	
		ISelection selection = SelectionExtracter.currentSelection();
		List<VersionedArtifactIdentification> vais = new ArrayList<>();	
		if (selection!= null) {
			List<EnhancedCompiledArtifactIdentification> extractSelectedArtifacts = EnhancedSelectionExtracter.extractSelectedArtifacts(selection);
			if (extractSelectedArtifacts != null && extractSelectedArtifacts.size() > 0) {
				extractSelectedArtifacts.stream().map( ecai -> VersionedArtifactIdentification.parse( ecai.asString())).forEach( vais::add);
			}
		}
		
		// Dialog to select analysis target	
		Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay());
		
		
		AnalysisDialog analysisDialog = new AnalysisDialog( shell);
		if (vais.size() > 0) {
			analysisDialog.setInitialIdentifications( vais);
		}
		// check return value, in order to detect the cancel button being pressed in the dialog..
		int retval = analysisDialog.open();
		if (retval == org.eclipse.jface.dialogs.Dialog.CANCEL) {
			return null;
		}

		
		// process
		// resolve artifact 
		Maybe<List<CompiledTerminal>> compiledTerminalsMaybe = analysisDialog.getSelection();
		
		if (compiledTerminalsMaybe.isEmpty()) {
			return null;
		}
		
		if (compiledTerminalsMaybe.isUnsatisfied()) {
			ArtifactContainerStatus status = ArtifactContainerStatus.create("No selection ", compiledTerminalsMaybe.whyUnsatisfied());
			ArtifactContainerPlugin.instance().log(status);			
			return null;
		}
		List<CompiledTerminal> compiledTerminals = compiledTerminalsMaybe.get();
		
		ClasspathResolutionScope scope = analysisDialog.getScopeSelection();
		
		// cfg
		Maybe<AnalysisArtifactResolution> resolutionMaybe = null;
		Maybe<RepositoryConfiguration> customRepositoryConfigurationMaybe = analysisDialog.getCustomRepositoryConfiguration();

		RepositoryConfiguration repositoryConfiguration = null;
		if (customRepositoryConfigurationMaybe.isUnsatisfiedBy(Canceled.T)) {
			resolutionMaybe = DevrockPlugin.mcBridge().resolveClasspath(compiledTerminals, scope);				
		}
		else if (customRepositoryConfigurationMaybe.isUnsatisfied()) {
			ArtifactContainerStatus status = new ArtifactContainerStatus("Cannot load specified custom repository-configuration as " + customRepositoryConfigurationMaybe.whyUnsatisfied().stringify(), IStatus.ERROR);
			ArtifactContainerPlugin.instance().log(status);			
			return null;
		}
		else {
			repositoryConfiguration = customRepositoryConfigurationMaybe.get();
			resolutionMaybe = DevrockPlugin.mcBridge().customBridge( repositoryConfiguration).resolveClasspath(compiledTerminals, scope);
		}
						
		if (resolutionMaybe.isUnsatisfied()) {
			String failedTerminals = compiledTerminals.stream().map( ct -> ct.asString()).collect( Collectors.joining(","));
			ArtifactContainerStatus status = new ArtifactContainerStatus("cannot resolve classpath for [" + failedTerminals + "] because " + resolutionMaybe.whyUnsatisfied().stringify(), IStatus.ERROR);
			ArtifactContainerPlugin.instance().log(status);		
		}
		else {
			boolean enableViewerButton = DevrockPlugin.instance().storageLocker().getValue( StorageLockerSlots.SLOT_ARTIFACT_VIEWER_ENABLED, true);
			if (!enableViewerButton) {				
				YamlResolutionViewer yamlViewer = new YamlResolutionViewer(shell);
				yamlViewer.setResolution( resolutionMaybe.get());
				yamlViewer.open();				
			}
			else {				
				ContainerResolutionViewer resolutionViewer = new ContainerResolutionViewer( shell);
				resolutionViewer.setResolution( resolutionMaybe.get());
				resolutionViewer.preemptiveDataRetrieval();
				resolutionViewer.open();
			}				
		}

		
		return null;
	}

	
}
