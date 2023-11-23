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
package com.braintribe.devrock.importer;


import java.io.File;
import java.util.List;

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
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;

import com.braintribe.devrock.bridge.eclipse.workspace.WorkspaceProjectInfo;
import com.braintribe.devrock.bridge.eclipse.workspace.WorkspaceProjectView;
import com.braintribe.devrock.eclipse.model.identification.EnhancedCompiledArtifactIdentification;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.devrock.plugin.DevrockPluginStatus;
import com.braintribe.logging.Logger;


/**
 * a helper class to import project files into Eclipse,
 * the full list is imported with a block on workspace updates,
 * and the updater is only run after all are imported. 
 * 
 * @author pit
 *
 */
public class ProjectImporter {
	private static Logger log = Logger.getLogger(ProjectImporter.class);
	/**
	 * @param activeWorkingSet - the currently active working set to attach the project to
	 * @param projects - a list of {@link EnhancedCompiledArtifactIdentification} of the projects to import
	 * @param listener - a {@link ProjectImportListener}, may be null
	 */
	public static void importProjects( final IWorkingSet activeWorkingSet, List<? extends EnhancedCompiledArtifactIdentification> projects, final ProjectImportListener listener) {
		
		if (projects == null || projects.size() == 0)
			return;
			
		WorkspaceJob job = new WorkspaceJob("Import selected projects") {
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) {
			
				System.out.println("start importing");
				
				int size = projects.size();
				monitor.beginTask("Importing projects", size);
				int i = 0;
			
				IWorkspace workspace = ResourcesPlugin.getWorkspace();
				//
				// find the current working set			
				IWorkingSetManager manager = PlatformUI.getWorkbench().getWorkingSetManager();
				if (activeWorkingSet == null) {
					log.debug( "importing [" + size + "] projects to workspace");
				}
				else {
					log.debug( "importing [" + size + "] projects to working set [" + activeWorkingSet.getName() + "]");						
				}				
				// 
				try {
					
					WorkspaceProjectView workspaceProjectView = DevrockPlugin.instance().getWorkspaceProjectView();
					
					for (EnhancedCompiledArtifactIdentification ecai : projects) {
												
						monitor.subTask("importing into Eclipse [" + ecai.asString() + "]");
						
						try {							
							IProject project = null;
							// if a working set is active, the project MAY already be in the workspace
							if (activeWorkingSet != null) {
								WorkspaceProjectInfo projectInfo = workspaceProjectView.getProjectInfo( ecai);
								if (projectInfo != null) 
									project = projectInfo.getProject();
							}
							// only import if not present yet
							if (project == null) {							
								File file = new File( ecai.getOrigin() + "/.project");
								IProjectDescription description = workspace.loadProjectDescription( new Path( file.getAbsolutePath()));
								String descriptionName = description.getName();								
								log.debug( "importing unloaded project [" + descriptionName + "]");
								
								project = workspace.getRoot().getProject(descriptionName);
								project.create(description, null);
								project.open(null);								
							}											
							
							// if we have a current working set, attach the project to it
							if (activeWorkingSet != null) {
								log.debug( "importing  project [" + project.getName() + "] to working set");																									
								manager.addToWorkingSets(project, new IWorkingSet[] {activeWorkingSet});
							}
							monitor.worked( ++i);
							// notify listener
							if (listener != null) {
								listener.acknowledgeImportedProject(ecai);
							}							
							// 
						} catch (CoreException e) {
							String msg = "Cannot import project [" + ecai + "]";
							DevrockPluginStatus status = new DevrockPluginStatus( msg, e);
							DevrockPlugin.instance().log(status);									
						}				
					}
				} finally {
					System.out.println("done importing");
				}
				
				monitor.done();													
				
				return Status.OK_STATUS;
			}			
		};		
		job.schedule();				
	}
}
