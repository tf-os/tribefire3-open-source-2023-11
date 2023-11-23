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
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

import com.braintribe.devrock.ac.container.ArtifactContainer;
import com.braintribe.devrock.ac.container.plugin.ArtifactContainerPlugin;
import com.braintribe.devrock.ac.container.plugin.ArtifactContainerStatus;
import com.braintribe.devrock.api.selection.SelectionExtracter;

/**
 * updates the currently selected projects - if they have a container attached
 * 
 * @author pit
 *
 */
public class ProjectUpdater extends WorkspaceModifyOperation {

	private Set<IProject> selectedProjects;
	private IWorkbenchWindow activeWorkbenchWindow;
	public enum Mode { standard, pom};
	private Mode mode;
	
	
	public ProjectUpdater() {
		this.mode = Mode.standard;
	}
	public ProjectUpdater(Mode mode) {
		this.mode = mode;
	}
	
	
	public ProjectUpdater(IWorkbenchWindow activeWorkbenchWindow) {
		this.activeWorkbenchWindow = activeWorkbenchWindow;
	}
	@Override
	protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {
		
		if (selectedProjects == null) {
			ISelection selection = SelectionExtracter.currentSelection(activeWorkbenchWindow);
			selectedProjects = SelectionExtracter.selectedProjects(selection);
		}
		
		if (selectedProjects == null || selectedProjects.size() == 0)
			return;
		
		try {
			int i = 0;
			monitor.beginTask("reinitializing containers", selectedProjects.size());
			
			for (IProject project : selectedProjects) {
				monitor.subTask( "resolving " + project.getName());
				
				ArtifactContainer container = ArtifactContainerPlugin.instance().containerRegistry().getContainerOfProject(project);
				if (container != null) {
					// reassign - and perhaps update the VAI in the container
					ArtifactContainer sibling = container.reinitialize( mode);
					// trigger resolve
					sibling.getClasspathEntries( false);
				}
				else {
					System.out.println("no container for [" + project.getName() + "]");
				}
				monitor.worked(i++);
			}
		} 
		finally {
			monitor.done();
		}			
	}
	public void runAsJob() {
		Job job = new WorkspaceJob("Triggering sync on selected projects") {
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) {
				// rescan				
				try {
					execute( monitor);
				} catch (Exception e) {
					ArtifactContainerStatus status = new ArtifactContainerStatus("cannot trigger update the selected containers", e);
					ArtifactContainerPlugin.instance().log(status);
				} 
				return Status.OK_STATUS;
			}
			
		};
		job.schedule();
	
	}
	public void setSelectedProjects(Set<IProject> selectedProjects) {
		this.selectedProjects = selectedProjects;
		// 
		
	}
		
}
