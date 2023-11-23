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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.ui.IWorkingSet;

import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerStatus;
import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.malaclypse.cfg.preferences.ac.qi.VersionModificationAction;
import com.braintribe.plugin.commons.commands.AbstractDropdownCommandHandler;
import com.braintribe.plugin.commons.commands.ArtifactToClipboardExpert;
import com.braintribe.plugin.commons.selection.PackageExplorerSelectedJarsTuple;
import com.braintribe.plugin.commons.selection.SelectionExtractor;
import com.braintribe.plugin.commons.selection.SelectionTuple;
import com.braintribe.plugin.commons.selection.TargetProvider;

/**
 * @author pit
 *
 */
public class DependencyToClipboardCopyCommand extends AbstractDropdownCommandHandler implements TargetProvider {	
	private String PARM_MSG = "com.braintribe.devrock.artifactcontainer.common.commands.command.param.copy";
	private String PARM_DIRECT = VersionModificationAction.untouched.name();//"direct";
	private String PARM_RANGIFY = VersionModificationAction.rangified.name();//"rangify";
	private String PARM_VARIABLE = VersionModificationAction.referenced.name();//"variable";
	
	private IWorkingSet activeWorkingSet;
	private Clipboard clipboard;
	
	@Override
	public void process(String param) {
						
		VersionModificationAction copyMode = ArtifactContainerPlugin.getInstance().getArtifactContainerPreferences(false).getQuickImportPreferences().getLastDependencyCopyMode();
		if (param == null) {
			;
		}
		else if (param.equalsIgnoreCase( PARM_DIRECT)) {
			copyMode = VersionModificationAction.untouched;
		}
		else if (param.equalsIgnoreCase( PARM_RANGIFY)) {
			copyMode = VersionModificationAction.rangified;
		}
		else if (param.equalsIgnoreCase( PARM_VARIABLE)) {
			copyMode = VersionModificationAction.referenced;	
		}		
		performCopy(copyMode);
		// store mode as last used
		ArtifactContainerPlugin.getInstance().getArtifactContainerPreferences(false).getQuickImportPreferences().setLastDependencyCopyMode(copyMode);
		
	}

	
	private void performCopy( VersionModificationAction action) {
		PackageExplorerSelectedJarsTuple tuple = SelectionExtractor.extractSelectedJars();
		activeWorkingSet = tuple.currentWorkingSet;
		
		Set<Artifact> identifiedArtifacts = new HashSet<Artifact>();
		ISelection selection = SelectionExtractor.getCurrentPackageExplorerSelection();
		identifiedArtifacts.addAll( SelectionExtractor.extractSelectedArtifacts( selection));
		
		if (identifiedArtifacts.size() > 0) {
			if (clipboard != null)
				clipboard.dispose();
			clipboard = ArtifactToClipboardExpert.copyToClipboard( action, identifiedArtifacts.toArray( new Artifact[0]));
		}
		else {
			ArtifactContainerStatus status = new ArtifactContainerStatus( "cannot identify any artifacts from selection", IStatus.WARNING);
			ArtifactContainerPlugin.getInstance().log(status);	
		}
		
	}

	@Override
	protected String getParamKey() {
		return PARM_MSG;
	}


	@Override
	public IWorkingSet getTargetWorkingSet() {	
		return activeWorkingSet;
	}
	
	@Override
	public SelectionTuple getSelectionTuple() {	
		return null;
	}
	@Override
	public IProject getTargetProject() { 
		return null;
	}	
	@Override
	public Set<IProject> getTargetProjects() {
		return null;
	}
	@Override
	public void refresh() {			
	}
	
	
	
}
