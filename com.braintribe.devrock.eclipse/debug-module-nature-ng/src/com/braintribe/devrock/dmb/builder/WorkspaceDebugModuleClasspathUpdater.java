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
package com.braintribe.devrock.dmb.builder;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

import com.braintribe.devrock.dmb.plugin.DebugModuleBuilderPlugin;
import com.braintribe.devrock.dmb.plugin.DebugModuleBuilderStatus;

public class WorkspaceDebugModuleClasspathUpdater extends WorkspaceModifyOperation {
	
	
	private List<IProject> projects;

	public WorkspaceDebugModuleClasspathUpdater(List<IProject> projects) {
		this.projects = projects;
	}
	

	@Override
	protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {
	
		int i = 0;
		monitor.beginTask("Building debug module classpaths ", projects.size());
		for (IProject project : projects) {
			if (monitor.isCanceled()) {
				DebugModuleBuilderStatus status = new DebugModuleBuilderStatus("debug module classpath builds aborted by user", IStatus.INFO);
				DebugModuleBuilderPlugin.instance().log(status);
				return;
			}
			monitor.subTask( "resolving " + project.getName());
			DebugModuleBuilder.updateModuleCarrierClasspath(project);
			monitor.worked(++i);			
		}
	}
	
	public void runAsJob() {
		if (projects == null) {
			DebugModuleBuilderStatus status = new DebugModuleBuilderStatus("No projects to build configured", IStatus.WARNING);
			DebugModuleBuilderPlugin.instance().log(status);
			return;
		}
		Job job = new WorkspaceJob("Triggering debug module classpath build on (" + projects.size() +") debug module projects") {
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) {
								
				try {
					execute( monitor);
				} catch (Exception e) {
					DebugModuleBuilderStatus status = new DebugModuleBuilderStatus("cannot trigger debug module classpath builds", e);
					DebugModuleBuilderPlugin.instance().log(status);
				} 
				return Status.OK_STATUS;
			}
			
		};
		job.schedule();
	
	}

}
