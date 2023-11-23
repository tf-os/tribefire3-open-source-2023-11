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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IWorkingSet;

import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerStatus;
import com.braintribe.devrock.artifactcontainer.control.project.ProjectImporter;
import com.braintribe.devrock.artifactcontainer.control.project.ProjectImporterTuple;
import com.braintribe.plugin.commons.selection.DetailedProjectExtractionResult;
import com.braintribe.plugin.commons.selection.PackageExplorerSelectedJarsTuple;
import com.braintribe.plugin.commons.selection.SelectionExtractor;
import com.braintribe.plugin.commons.selection.SelectionTuple;
import com.braintribe.plugin.commons.selection.TargetProvider;

public class JarImportCommand extends AbstractHandler implements TargetProvider {

	private IWorkingSet activeWorkingSet;
	private boolean preprocess = false;

	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
	
		final TargetProvider targetProvider = this;
		final PackageExplorerSelectedJarsTuple jarTuple = SelectionExtractor.extractSelectedJars();
		activeWorkingSet = jarTuple.currentWorkingSet;
		List<String> jars = jarTuple.selectedJars;
		
		if (jars == null || jars.size() == 0) {
			return null;
		}
		
		List<DetailedProjectExtractionResult> extractionResult = SelectionExtractor.extractProjectsWithDetails(jars.toArray( new String[0]));	
		List<ProjectImporterTuple> tupleList = new ArrayList<>();
		
		List<String> noProject = new ArrayList<>();
		for (DetailedProjectExtractionResult detail : extractionResult) {
			if (detail.extractedProject == null) {
				noProject.add(detail.jar);
			}
			else {
				ProjectImporterTuple importerTuple = new ProjectImporterTuple( detail.extractedProject, detail.extractedArtifact);
				tupleList.add(importerTuple);
			}
		}
		
		if (!noProject.isEmpty()) {
			String msg = "the following jars have no corresponding projects:\n" + noProject.stream().collect( Collectors.joining("\n"));
			ArtifactContainerStatus status = new ArtifactContainerStatus( msg, IStatus.WARNING);
			ArtifactContainerPlugin.getInstance().log(status);		
		}
					
		Job job = new Job("Running JarImporter") {
			@Override
			protected IStatus run(IProgressMonitor arg0) {
				ProjectImporter.importProjects( preprocess, targetProvider, null, tupleList.toArray( new ProjectImporterTuple[0]));
				return Status.OK_STATUS;
			}			
		};
		
		job.schedule();
		return null;		
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
