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
package com.braintribe.devrock.mnb.commands.builder;

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

import com.braintribe.devrock.mnb.plugin.ModelBuilderPlugin;
import com.braintribe.devrock.mnb.plugin.ModelBuilderStatus;

public class WorkspaceModelDeclarationUpdater extends WorkspaceModifyOperation {
	
	
	private List<IProject> projects;

	public WorkspaceModelDeclarationUpdater(List<IProject> projects) {
		this.projects = projects;
	}
	

	@Override
	protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {
		ModelBuilder mb = new ModelBuilder();
		int i = 0;
		monitor.beginTask("Building model declarations ", projects.size());
		for (IProject project : projects) {
			if (monitor.isCanceled()) {
				ModelBuilderStatus status = new ModelBuilderStatus("model declaration builds aborted by user", IStatus.INFO);
				ModelBuilderPlugin.instance().log(status);
				return;
			}
			monitor.subTask( "resolving " + project.getName());
			mb.forceBuild(project);
			monitor.worked(++i);			
		}
	}
	
	public void runAsJob() {
		if (projects == null) {
			ModelBuilderStatus status = new ModelBuilderStatus("No projects to build configured", IStatus.WARNING);
			ModelBuilderPlugin.instance().log(status);
			return;
		}
		Job job = new WorkspaceJob("Triggering model declaration build on (" + projects.size() +") model projects") {
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) {
								
				try {
					execute( monitor);
				} catch (Exception e) {
					ModelBuilderStatus status = new ModelBuilderStatus("cannot trigger update of all containers in the workspace", e);
					ModelBuilderPlugin.instance().log(status);
				} 
				return Status.OK_STATUS;
			}
			
		};
		job.schedule();
	
	}

}
