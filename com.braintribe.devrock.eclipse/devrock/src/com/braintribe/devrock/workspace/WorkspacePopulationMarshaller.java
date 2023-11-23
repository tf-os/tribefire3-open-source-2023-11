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
package com.braintribe.devrock.workspace;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;

import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.devrock.api.nature.NatureHelper;
import com.braintribe.devrock.api.selection.SelectionExtracter;
import com.braintribe.devrock.eclipse.model.identification.EnhancedCompiledArtifactIdentification;
import com.braintribe.devrock.eclipse.model.storage.StorageLockerPayload;
import com.braintribe.devrock.eclipse.model.workspace.ExportPackage;
import com.braintribe.devrock.eclipse.model.workspace.Project;
import com.braintribe.devrock.eclipse.model.workspace.WorkingSet;
import com.braintribe.devrock.eclipse.model.workspace.Workspace;
import com.braintribe.devrock.mc.core.declared.DeclaredArtifactIdentificationExtractor;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.devrock.plugin.DevrockPluginStatus;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.model.artifact.compiled.CompiledArtifact;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.version.Version;

/**
 * handles extraction of a workspace (working set, project - artifact, natures)
 * 
 * @author pit
 *
 */
public class WorkspacePopulationMarshaller {
	private static YamlMarshaller marshaller;
	static {
		marshaller = new YamlMarshaller();
		marshaller.setWritePooled(true);
	}
	private IWorkspace iWorkspace = ResourcesPlugin.getWorkspace();
	private IWorkingSetManager iWorkingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();
	private Map<String,IProject> projectToIProjectMap;

	/**
	 * extracts a full Eclipse {@link IWorkspace} to a {@link Workspace} entity 
	 * @return - the extracted {@link Workspace}
	 */
	public Workspace extractWorkspaceContent() {
		Workspace workspace = Workspace.T.create();
		
		IWorkingSet[] allWorkingSets = iWorkingSetManager.getAllWorkingSets();
		
		
		// working sets
		for (IWorkingSet iWorkingSet : allWorkingSets) {
			WorkingSet workingSet = WorkingSet.T.create();
			if (
					!iWorkingSet.isVisible() || 
					iWorkingSet.isAggregateWorkingSet()
			) {
				continue;
			}
			
			workingSet.setPageClass( iWorkingSet.getId());
				
			workingSet.setWorkingSetName( iWorkingSet.getName());
			IAdaptable[] elements = iWorkingSet.getElements();
			if (elements != null) {
				for (IAdaptable iAdaptable : elements) {
					IProject iProject = SelectionExtracter.extractProjectFromIAdaptable( iAdaptable);
					if (iProject != null) {
						// location & identification
						Maybe<Project> project = from( iProject);
						if (project.isSatisfied()) {
							workingSet.getProjects().add( project.get());							
						}
						else {
							Reason reason = project.whyUnsatisfied();
							DevrockPluginStatus status = new DevrockPluginStatus( "cannot add project to workspace extraction as ", reason);
							DevrockPlugin.instance().log(status);
						}
					}
					
				}
			}
			workspace.getWorkingSets().add(workingSet);
		}
		
		
		// all projects
		IWorkspace iWorkspace = ResourcesPlugin.getWorkspace();

		IWorkspaceRoot root = iWorkspace.getRoot();
		for (IProject iProject : root.getProjects()) {
			Maybe<Project> project = from( iProject); 
			if (project.isSatisfied()) {
				workspace.getProjects().add( project.get());
			}
			else {
				Reason reason = project.whyUnsatisfied();
				DevrockPluginStatus status = new DevrockPluginStatus( "cannot add project to workspace extraction as ", reason);
				DevrockPlugin.instance().log(status);
			}
		}
		return workspace;		 
	}
	
	
	/**
	 * extracts all selected {@link IProject} (and their {@link IWorkingSet}) from the {@link IWorkspace} 
	 * @param iSelection - the {@link ISelection}
	 * @return - the extracted {@link Workspace}
	 */
	public Workspace extractWorkspaceContent(ISelection iSelection) {
		
		List<IWorkingSet> selectedWorkingSets = SelectionExtracter.selectedWorkingSets(iSelection);				
		Set<IProject> selectedProjects = SelectionExtracter.selectedProjects(iSelection);
		
		
		if (selectedProjects.size() == 0 && selectedWorkingSets.size() == 0) {
			return null;
		}
	
		// maps workingsets to their content
		Map<IWorkingSet, List<IProject>> workingSetToProjectMap = new HashMap<>();
	
		
		// any working sets selected -> add to map
		for (IWorkingSet iWorkingset : selectedWorkingSets) {
			WorkingSet workingSet = WorkingSet.T.create();
			workingSet.setWorkingSetName( iWorkingset.getName());
			
			IAdaptable[] elements = iWorkingset.getElements();
			if (elements != null) {
				for (IAdaptable iAdaptable : elements) {
					IProject iProject = SelectionExtracter.extractProjectFromIAdaptable( iAdaptable);
					if (iProject != null) {						
						List<IProject> ownedProjects = workingSetToProjectMap.computeIfAbsent(iWorkingset, k -> new ArrayList<>());
						ownedProjects.add( iProject);																														
					}					
				}
			}
		}
	
		
		// any projects selected -> detected working set if any 
		Set<IProject> unownedProjects = new HashSet<>();
		for (IProject project : selectedProjects) {
			IWorkingSet owningWorkingset = SelectionExtracter.getOwningWorkingset(project);
			if (
					owningWorkingset != null && 
					owningWorkingset.isVisible() && 
					!owningWorkingset.isAggregateWorkingSet()
				) {
				List<IProject> ownedProjects = workingSetToProjectMap.computeIfAbsent(owningWorkingset, k -> new ArrayList<>());
				ownedProjects.add(project);
			}
			else {
				unownedProjects.add(project);
			}
		}
		
		//
		Workspace workspace = Workspace.T.create();
		if (workingSetToProjectMap.size() > 0) {
			for (Map.Entry<IWorkingSet, List<IProject>> entry : workingSetToProjectMap.entrySet()) {
				WorkingSet workingSet = WorkingSet.T.create();
				IWorkingSet iWorkingSet = entry.getKey();
				workingSet.setPageClass( iWorkingSet.getId());			
				workingSet.setWorkingSetName( iWorkingSet.getName());
				workingSet.getProjects().addAll( entry.getValue().stream() //
																	.map( ip -> from( ip)) //
																	.filter( m -> m.isSatisfied()) //
																	.map( m -> m.get()) //
																	.collect( Collectors.toList())// 
				);		
				workspace.getWorkingSets().add(workingSet);				
			}
		}
		if (unownedProjects.size() > 0) {
			workspace.getProjects().addAll( unownedProjects.stream() //
																.map( ip -> from( ip)) //
																.filter( m -> m.isSatisfied()) //
																.map( m -> m.get()) //
																.collect( Collectors.toList())
			); //
		}
	
		
		return workspace;
	}
	
	/**
	 * @param workspace - the workspace
	 * @param file - the {@link File} to write to 
	 */
	public void dump( Workspace workspace, StorageLockerPayload payload, File file) {
		if (payload != null) {
			ExportPackage ep = ExportPackage.T.create();
			ep.setStorageLockerPayload(payload);
			ep.setWorkspace(workspace);
			
			try (OutputStream out = new FileOutputStream(file)) {
				marshaller.marshall( out, ep);
			} catch (Exception e) {
				DevrockPluginStatus status = new DevrockPluginStatus( "cannot write workspace and storage extraction file  ", e);
				DevrockPlugin.instance().log(status);
			}
		}
		else {		
			try (OutputStream out = new FileOutputStream(file)) {
				marshaller.marshall( out, workspace);
			} catch (Exception e) {
				DevrockPluginStatus status = new DevrockPluginStatus( "cannot write workspace extraction file  ", e);
				DevrockPlugin.instance().log(status);
			}
		}
	}
	
	/**
	 * @param file - the file to read from 
	 * @return - the read {@link Workspace}
	 */
	public static ExportPackage load( File file) {
		try (InputStream in = new FileInputStream( file)) {
			GenericEntity ge = (GenericEntity) marshaller.unmarshall(in); 
			if (ge instanceof Workspace) {
				ExportPackage ep = ExportPackage.T.create();
				ep.setWorkspace( (Workspace) ge);
				return ep;
			}
			else if (ge instanceof ExportPackage) {
				return (ExportPackage) ge;
			}
		}
		 catch (Exception e) {
			DevrockPluginStatus status = new DevrockPluginStatus( "cannot load workspace extraction file  ", e);
			DevrockPlugin.instance().log(status);
		}
		return null;
	}
	
	/**
	 * @param iProject - the {@link IProject}
	 * @return - a {@link Maybe} of {@link Project}
	 */
	private Maybe<Project> from( IProject iProject) {
		String projectFileName = iProject.getLocation().toOSString();
		File projectFile = new File( projectFileName);
		File pomFile = new File( projectFile, "pom.xml");
		Maybe<CompiledArtifact> identificationPotential = DeclaredArtifactIdentificationExtractor.extractMinimalArtifact( pomFile);
		if (identificationPotential.isSatisfied()) {
			Project project = Project.T.create();
			project.setProjectName( iProject.getName());
			EnhancedCompiledArtifactIdentification ecai = EnhancedCompiledArtifactIdentification.from( identificationPotential.get());			
			project.setIdentification(ecai);
			Maybe<List<String>> natures = NatureHelper.getNatures(iProject);
			if (natures.isSatisfied()) {
				project.getNatures().addAll( natures.get());
			}
			return Maybe.complete( project);
		}
		else {
			return identificationPotential.emptyCast();
		}
	}
	
	/**
	 * @param workspace - the {@link Workspace} 
	 */
	public void restoreWorkspace( Workspace workspace) {
		projectToIProjectMap = new HashMap<>();
		List<WorkingSet> workingSets = workspace.getWorkingSets();
		if (workingSets.size() > 0) {
			for (WorkingSet workingSet : workingSets) {
				List<Project> projects = workingSet.getProjects();
				if (projects.size() > 0) {
					for (Project project : projects) {
						installProject( project, workingSet);
					}
				}
			}
		}
				
		List<Project> projects = workspace.getProjects();		
		for (Project project : projects) {		
			installProject( project, null);
		}
	}

	
	public void restoreWorkspace( IProgressMonitor monitor, ExportPackage ep) {
		Workspace workspace = ep.getWorkspace();
		projectToIProjectMap = new HashMap<>();
		List<WorkingSet> workingSets = workspace.getWorkingSets();

		monitor.beginTask("importing working sets", workingSets.size());
		
		if (workingSets.size() > 0) {
			for (WorkingSet workingSet : workingSets) {
				monitor.subTask( "importing " + workingSet.getWorkingSetName());	
				List<Project> projects = workingSet.getProjects();				
				if (projects.size() > 0) {
					for (Project project : projects) {
						monitor.subTask( "importing " + project.getProjectName() + " into "+ workingSet.getWorkingSetName());
						installProject( project, workingSet);
					}
				}
			}
		}
		
		List<Project> projects = workspace.getProjects();		
		monitor.subTask("importing remaining projects");
		for (Project project : projects) {		
			monitor.subTask( "importing " + project.getProjectName() + " into workspace ");	
			installProject( project, null);
		}
		
		if (ep.getStorageLockerPayload() != null) {
			monitor.subTask("importing settings");
			DevrockPlugin.instance().storageLocker().override( ep.getStorageLockerPayload());
		}
		
	}
	
	public void restore( IProgressMonitor monitor, List<WorkingSet> selectedWorkingSets, List<Project> selectedProjects) {
		if (selectedWorkingSets.size() > 0) {
			for (WorkingSet workingSet : selectedWorkingSets) {
				monitor.subTask( "importing " + workingSet.getWorkingSetName());	
				List<Project> projects = workingSet.getProjects();				
				if (projects.size() > 0) {
					for (Project project : projects) {
						monitor.subTask( "importing " + project.getProjectName() + " into "+ workingSet.getWorkingSetName());
						installProject( project, workingSet);
					}
				}
			}
		}
		
		List<Project> projects = selectedProjects;
		if (selectedProjects.size() > 0) {
			monitor.subTask("importing remaining projects");
			for (Project project : projects) {		
				monitor.subTask( "importing " + project.getProjectName() + " into workspace ");	
				installProject( project, null);
			}
		}
		
	}


	private void installProject(Project project, WorkingSet workingSet) {
		IWorkingSet iWorkingSet = null;
		if (workingSet != null) {
			iWorkingSet = iWorkingSetManager.getWorkingSet( workingSet.getWorkingSetName());
			if (iWorkingSet == null) {			
				iWorkingSet = iWorkingSetManager.createWorkingSet( workingSet.getWorkingSetName(), new IAdaptable[0]);
				iWorkingSet.setId( workingSet.getPageClass());
				iWorkingSetManager.addWorkingSet(iWorkingSet);
			}			
		}
		// query
		// TODO : using the direct match on the CAI is too strict - will only work if the dump is very recent.
		// requires a 'range' feature. Question is again: major.minor standard range, or version as lower boundary range.
		// ev even 'groupId, artifactId' only with getting the best match? ev Dialog?
		EnhancedCompiledArtifactIdentification enhancedCompiledArtifactIdentification = project.getIdentification();
		String expression = enhancedCompiledArtifactIdentification.getGroupId() + ":" + enhancedCompiledArtifactIdentification.getArtifactId();
		Version version = enhancedCompiledArtifactIdentification.getVersion();
		
		List<EnhancedCompiledArtifactIdentification> result = DevrockPlugin.instance().quickImportController().runPartialSourceArtifactQuery( expression);
		if (result != null && result.size() > 0) {
						
			EnhancedCompiledArtifactIdentification ecai = null;
			// only one hit -> use ? 
			if (result.size() == 1) {							
				EnhancedCompiledArtifactIdentification suspect = result.get(0);
				if (suspect.getVersion().compareTo(version) >= 0) {
					ecai = suspect;
				}				
			}
			else {
				// filter : must be at least have the same version as requested 
				List<EnhancedCompiledArtifactIdentification> matches = result.stream().filter( e -> {					
					return e.getVersion().compareTo(version) >= 0;
				}).sorted( EnhancedCompiledArtifactIdentification::compareTo).collect(Collectors.toList());
				ecai = matches.get( matches.size()-1);
			}
			
			if (ecai == null) {
				DevrockPluginStatus status = new DevrockPluginStatus( "no matching artifact found with at least the same version for: " + enhancedCompiledArtifactIdentification.asString() + "", IStatus.WARNING);
				DevrockPlugin.instance().log(status);
			}
		
			
			IProject iProject = projectToIProjectMap.get(project.getProjectName());
			if (iProject == null) {
				try {
					File file = new File( ecai.getOrigin() + "/.project");
					IProjectDescription description = iWorkspace.loadProjectDescription( new Path( file.getAbsolutePath()));
					String descriptionName = description.getName();																	
					iProject = iWorkspace.getRoot().getProject(descriptionName);
					iProject.create(description, null);
					iProject.open(null);
					projectToIProjectMap.put( project.getProjectName(), iProject);
				} catch (CoreException e) {
					DevrockPluginStatus status = new DevrockPluginStatus( "cannot install project [" + project.getProjectName() + "]", e);
					DevrockPlugin.instance().log(status);
					return;
				}						
			}
			if (iWorkingSet != null) {
				iWorkingSetManager.addToWorkingSets(iProject, new IWorkingSet[] {iWorkingSet});		
			}
		}
		else {
			DevrockPluginStatus status = new DevrockPluginStatus( "no matches found whatsoever for: " + enhancedCompiledArtifactIdentification.asString() + "", IStatus.WARNING);
			DevrockPlugin.instance().log(status);
		}
	}
}
