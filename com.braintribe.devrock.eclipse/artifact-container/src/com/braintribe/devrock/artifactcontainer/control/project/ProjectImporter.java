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
package com.braintribe.devrock.artifactcontainer.control.project;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.representations.artifact.pom.ArtifactPomReader;
import com.braintribe.build.artifact.representations.artifact.pom.PomReaderException;
import com.braintribe.build.artifact.walk.multi.WalkException;
import com.braintribe.build.artifact.walk.multi.Walker;
import com.braintribe.build.artifacts.mc.wire.classwalk.contract.ClasspathResolverContract;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerStatus;
import com.braintribe.devrock.artifactcontainer.container.ArtifactContainer;
import com.braintribe.devrock.artifactcontainer.control.container.ArtifactContainerRegistry;
import com.braintribe.devrock.artifactcontainer.control.project.listener.ProjectImportListener;
import com.braintribe.devrock.artifactcontainer.control.walk.ArtifactContainerUpdateRequestType;
import com.braintribe.devrock.artifactcontainer.control.walk.wired.WiredArtifactContainerWalkController;
import com.braintribe.devrock.artifactcontainer.plugin.malaclypse.scope.wirings.MalaclypseWirings;
import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.version.VersionRangeProcessor;
import com.braintribe.model.malaclypse.WalkMonitoringResult;
import com.braintribe.plugin.commons.selection.TargetProvider;
import com.braintribe.wire.api.context.WireContext;

/**
 * a helper class to import project files into Eclipse,
 * the full list is imported with a block on workspace updates,
 * and the updater is only run after all are imported. 
 * 
 * @author pit
 *
 */
public class ProjectImporter {
	
	public static void importProjects( final boolean preprocess, final TargetProvider targetProvider, final ProjectImportListener listener, final ProjectImporterTuple ... projects) {
		
		if (projects == null || projects.length == 0)
			return;
		
		final WireContext<ClasspathResolverContract> resolverContract = MalaclypseWirings.basicClasspathResolverContract();
		
		WorkspaceJob job = new WorkspaceJob("Import selected projects") {
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) {
				
				monitor.beginTask("Importing projects", projects.length);
				int i = 0;
				IWorkspace workspace = ResourcesPlugin.getWorkspace();
				//
				// find the current working set			
				IWorkingSetManager manager = PlatformUI.getWorkbench().getWorkingSetManager();
				IWorkingSet activeWorkingSet = null;
				if (targetProvider != null) {
					activeWorkingSet = targetProvider.getTargetWorkingSet();
				}
				if (ArtifactContainerPlugin.isDebugActive()) {
					if (activeWorkingSet == null) {
						ArtifactContainerPlugin.log( "importing [" + projects.length + "] projects to workspace");
					}
					else {
						ArtifactContainerPlugin.log( "importing [" + projects.length + "] projects to working set [" + activeWorkingSet.getName() + "]");						
					}
				}
				final List<IProject> importedProjects = new ArrayList<IProject>( projects.length);
				// 
				try {
					// detach the resource listener, so now workspace updates are notified to the ACP
					ArtifactContainerPlugin.getInstance().detachResourceChangeListener();
					// deactivate JDT here?
					
					for (ProjectImporterTuple tuple : projects) {
						
						// pre process container ..
						if (preprocess) {
							monitor.subTask("pre processing [" + NameParser.buildName( tuple.getArtifact()) + "]");
							if (preProcessTuple(resolverContract, tuple) == false) {
								String msg = "Cannot pre-process project [" + tuple + "], skipping import";
								ArtifactContainerStatus status = new ArtifactContainerStatus( msg, IStatus.ERROR);
								ArtifactContainerPlugin.getInstance().log(status);		
								continue;
							}
						}
						monitor.subTask("importing into Eclipse [" + NameParser.buildName( tuple.getArtifact()) + "]");
						
						try {							
							IProject project = null;
							if (activeWorkingSet != null) {
								project = ArtifactContainerPlugin.getWorkspaceProjectRegistry().getProjectForArtifact( tuple.getArtifact());
							}
							if (project == null) {							
								File file = new File( tuple.getProjectFile());
								IProjectDescription description = workspace.loadProjectDescription( new Path( file.getAbsolutePath()));
								String descriptionName = description.getName();
								if (ArtifactContainerPlugin.isDebugActive()) {
									ArtifactContainerPlugin.log( "importing unloaded project [" + descriptionName + "]");
								}
								project = workspace.getRoot().getProject(descriptionName);
								project.create(description, null);
								project.open(null);								
							}
							else {
								if (ArtifactContainerPlugin.isDebugActive()) {
									ArtifactContainerPlugin.log( "importing  project [" + NameParser.buildName(tuple.getArtifact()) + "] to working set");
								}
							}							
							importedProjects.add( project);
							tuple.setProject(project);
							
							// if we have a current working set, attach the project to it
							if (activeWorkingSet != null) {
								manager.addToWorkingSets(project, new IWorkingSet[] {activeWorkingSet});
							}
							monitor.worked( ++i);
							// notify listener
							if (listener != null) {
								listener.acknowledgeImportedProject(tuple);
							}
							// 
						} catch (CoreException e) {
							String msg = "Cannot import project [" + tuple + "]";
							ArtifactContainerStatus status = new ArtifactContainerStatus( msg, e);
							ArtifactContainerPlugin.getInstance().log(status);									
						}				
					}
				} finally {
					// re-attach the resource listener so ACP is notified again 
					ArtifactContainerPlugin	.getInstance().attachResourceChangeListener();
					// re-activate JDT here?
				}
				
				monitor.done();
			
				// make sure Eclipse has all pom's ready before we update
				
				/*
				 * Post processing jobs 
				 */
				
				// update workspace
				Job deferredUpdateJob = new Job("trigger update") {
									
					@Override
					protected IStatus run(IProgressMonitor arg0) {
						arg0.beginTask( "post processing - update containers in workspace", 0);
						WiredArtifactContainerWalkController.getInstance().updateContainers( ArtifactContainerUpdateRequestType.refresh);
						arg0.done();
						return Status.OK_STATUS;
					}
				};				
				deferredUpdateJob.schedule(1000);
				
				// initialize launch containers 
				Job deferredLaunchJob = new Job("trigger launch containers") {					
					@Override
					protected IStatus run(IProgressMonitor arg0) {
						arg0.beginTask( "post processing - update runtime classpaths", 0);
						List<ArtifactContainer> containers = new ArrayList<ArtifactContainer>( importedProjects.size());
						for (IProject project : importedProjects) {
							ArtifactContainer container = ArtifactContainerPlugin.getArtifactContainerRegistry().getContainerOfProject(project);
							if (container != null) {
								containers.add( container);
							}
							else {
								ArtifactContainerPlugin.log( "no container found yet for [" + project.getName() + "]");
							}
						}
						WiredArtifactContainerWalkController.getInstance().updateContainers( containers, ArtifactContainerUpdateRequestType.launch);
						return Status.OK_STATUS;
					}
				};				
				deferredLaunchJob.schedule(2000);
				
				// update containers that have an unresolved dependency
				Job updateUnresolvedDependencyRequesterJob = new Job( "update dependency requester") {

					@Override
					protected IStatus run(IProgressMonitor arg0) {
						scanForAndUpdateProjectWithUnresolvedDependencies(projects);
						return Status.OK_STATUS;
					}
				};
				updateUnresolvedDependencyRequesterJob.schedule( 2000);
				
				return Status.OK_STATUS;
			}			
		};		
		job.schedule();				
	}
	
	/**
	 * preprocess a tuple - i.e. run a compile walk during the job .. 
	 * @param resolverContract
	 * @param tuple
	 * @return
	 */
	protected static boolean preProcessTuple(WireContext<ClasspathResolverContract> resolverContract, ProjectImporterTuple tuple) {
		
		File projectDir = new File( tuple.getProjectFile()).getParentFile();
		File pomFile = new File( projectDir, "pom.xml");
		
		String walkScopeId;
		Solution solution;
		try {
			ArtifactPomReader reader = resolverContract.contract().pomReader();
			walkScopeId = UUID.randomUUID().toString();
			solution = reader.readPom( walkScopeId, pomFile);
		} catch (PomReaderException e1) {
			String msg = "Cannot read pom [" + pomFile.getAbsolutePath() + "] during 'import project' [" + tuple + "]";
			ArtifactContainerStatus status = new ArtifactContainerStatus( msg, e1);
			ArtifactContainerPlugin.getInstance().log(status);						
			return false;
		}
		/*
		 * walk solution 
		 */
		try {
			Walker walker = resolverContract.contract().walker( MalaclypseWirings.compileWalkContext());
			walker.walk(walkScopeId, solution);
		} catch (WalkException e) {
			String msg = "Cannot preprocess [" + NameParser.buildName(solution) + "] during 'import project' [" + tuple + "]";
			ArtifactContainerStatus status = new ArtifactContainerStatus( msg, e);
			ArtifactContainerPlugin.getInstance().log(status);						
			return false;
		}
		
		return true;
	}

	/**
	 * ask for all projects with unresolved, find whether it contains the artifacts with just imported
	 * and triggers a sync on the projects matching
	 * @param projects
	 */
	
	private static void scanForAndUpdateProjectWithUnresolvedDependencies(final ProjectImporterTuple ... projects ) {
		ArtifactContainerRegistry artifactContainerRegistry = ArtifactContainerPlugin.getArtifactContainerRegistry();
		List<ArtifactContainer> containers = new ArrayList<ArtifactContainer>();
		
		for (ArtifactContainer container : artifactContainerRegistry.getContainers()) {
			
			WalkMonitoringResult walkResult = artifactContainerRegistry.getCompileWalkResult( container.getProject().getProject());
			if (walkResult != null) {
				Set<Dependency> unresolvedDependencies = walkResult.getUnresolvedDependencies();
				if (unresolvedDependencies == null || unresolvedDependencies.size() == 0) {
					continue;
				}
		
				for (ProjectImporterTuple tuple : projects) {
					Artifact artifact = tuple.getArtifact();
					
					for (Dependency dependency : unresolvedDependencies) {
						if (!dependency.getGroupId().equalsIgnoreCase( artifact.getGroupId()))
							continue;
						if (!dependency.getArtifactId().equalsIgnoreCase( artifact.getArtifactId()))
							continue;
						if (!VersionRangeProcessor.matches( dependency.getVersionRange(),  artifact.getVersion())) {
							continue;
						}
						// match !!
						containers.add( container);
					}
				}
			}
		
		}
		// 
		if (containers.size() > 0) {
			WiredArtifactContainerWalkController.getInstance().updateContainers( containers, ArtifactContainerUpdateRequestType.combined);
		}
		
	}
	
	
}
