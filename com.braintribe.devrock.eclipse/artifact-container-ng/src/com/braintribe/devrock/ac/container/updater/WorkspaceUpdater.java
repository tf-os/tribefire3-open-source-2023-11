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
package com.braintribe.devrock.ac.container.updater;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

import com.braintribe.devrock.ac.container.ArtifactContainer;
import com.braintribe.devrock.ac.container.plugin.ArtifactContainerPlugin;
import com.braintribe.devrock.ac.container.plugin.ArtifactContainerStatus;
import com.braintribe.devrock.ac.container.updater.ProjectUpdater.Mode;

/**
 * updates all containers of all projects in the workspace
 * 
 * @author pit
 *
 */
public class WorkspaceUpdater extends WorkspaceModifyOperation {

	@Override
	protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject[] projects = root.getProjects();
		
		if (projects == null || projects.length == 0) 
			return;
				
		
		try {
			monitor.beginTask("reinitializing containers", projects.length);
			int i = 0;
			for (IProject project : projects) {
				if (project.isAccessible() == false) {
					// do not sync inaccessible projects (closed projects for instance)
					continue;
				}
				if (monitor.isCanceled()) {
					ArtifactContainerStatus status = new ArtifactContainerStatus("reinitializing container aborted by user", IStatus.INFO);
					ArtifactContainerPlugin.instance().log(status);
					return;
				}
				monitor.subTask( "resolving " + project.getName());				
				ArtifactContainer container = ArtifactContainerPlugin.instance().containerRegistry().getContainerOfProject(project);
				if (container != null) {
					// reassign
					ArtifactContainer sibling = container.reinitialize(Mode.standard);
					// trigger resolve
					sibling.getClasspathEntries(false);
				}
				else {
					System.out.println("no container for [" + project.getName() + "]");
				}
				monitor.worked(i++);
			}
		} finally {
			monitor.done();
		}					
	}
	
	public void runAsJob() {
		Job job = new WorkspaceJob("Triggering full workspace sync") {
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) {
				// rescan				
				try {
					execute( monitor);
				} catch (Exception e) {
					ArtifactContainerStatus status = new ArtifactContainerStatus("cannot trigger update of all containers in the workspace", e);
					ArtifactContainerPlugin.instance().log(status);
				} 
				return Status.OK_STATUS;
			}
			
		};
		job.schedule();
	
	}

}
