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
package com.braintribe.devrock.bridge.eclipse.workspace;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;

import com.braintribe.devrock.api.identification.PomIdentificationHelper;
import com.braintribe.devrock.mc.core.declared.commons.HashComparators;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;

/**
 * just a wrapper for the {@link WorkspaceProjectView} to enable caching
 * @author pit
 *
 */
public class WorkspaceProjectViewSupplier implements Supplier<WorkspaceProjectView> {
	private WorkspaceProjectView currentWorkspaceProjectView;
	private Instant exposureInstant;
	private boolean dirtied;
	private Duration staleDelay = Duration.ofMillis( 1000);  // a second for now 
	
	/**
	 * @return - the current state of the workspace as a {@link WorkspaceProjectView} 
	 */
	private WorkspaceProjectView retrieveWorkspaceProjectView() {
		System.out.println("Accessing project view");
		WorkspaceProjectView view = new WorkspaceProjectView();
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		for (IProject project : root.getProjects()) {
			if (!project.isAccessible() || !project.isOpen()) {
				System.out.println("project [" + project.getName() + "] isn't accessible");
				continue;
			}
			Maybe<CompiledArtifactIdentification> caipot = PomIdentificationHelper.identifyProject(project);
			if (caipot.isUnsatisfied()) {
				System.out.println("Cannot identify pom of project [" + project.getName() + "]");
				continue;
			}
					
			CompiledArtifactIdentification cai = caipot.get();
			
			BasicWorkspaceProjectInfo workspaceProjectInfo = new BasicWorkspaceProjectInfo();
			workspaceProjectInfo.setProject(project);
			VersionedArtifactIdentification vai = VersionedArtifactIdentification.create( cai.getGroupId(), cai.getArtifactId(), cai.getVersion().asString());
			workspaceProjectInfo.setVersionedArtifactIdentification( vai);
			
			view.getArtifactsInWorkspace().put( HashComparators.versionedArtifactIdentification.eqProxy( vai), workspaceProjectInfo);
			view.getProjectsInWorkspace().put( project, workspaceProjectInfo);						
		}		
		return view;
	}
	
	/**
	 * @return - 
	 */
	@Override
	public WorkspaceProjectView get() {
		if (currentWorkspaceProjectView == null) {
			currentWorkspaceProjectView = retrieveWorkspaceProjectView();
		}
		else if (isStale()){
			WorkspaceProjectView previousView = currentWorkspaceProjectView;
			currentWorkspaceProjectView = retrieveWorkspaceProjectView();	
			if (dirtied == false) {
				dirtied = !previousView.equals(currentWorkspaceProjectView);
			}
		}
		exposureInstant = Instant.now();
		return currentWorkspaceProjectView;
	}

	/**
	 * @return - true if the last exposure is older than the delay
	 */
	private boolean isStale() {
		return Instant.now().minus( staleDelay).isAfter(exposureInstant);
	}
	
	public boolean dirtied() {
		// no old data -> no comparison, i.e. dirty
		if (currentWorkspaceProjectView == null || dirtied) {
			dirtied = false;
			return true;
		}
 
		WorkspaceProjectView storedProjectView = currentWorkspaceProjectView;
		
		currentWorkspaceProjectView = null; // set to null so it gets recalculated in any case
		WorkspaceProjectView determinedProjectView = get();
		
		// compare the two
		return !storedProjectView.equals(determinedProjectView);		
	}

	
	
}
