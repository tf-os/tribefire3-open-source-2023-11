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
package com.braintribe.devrock.arb.listener;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.devrock.api.nature.JavaProjectBuilderHelper;
import com.braintribe.devrock.arb.builder.ArtifactReflectionBuilder;
import com.braintribe.devrock.arb.plugin.ArtifactReflectionBuilderPlugin;
import com.braintribe.devrock.arb.plugin.ArtifactReflectionBuilderStatus;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.logging.Logger;

/**
 * {@link WorkspaceModifyOperation} to run the builder.. 
 * @author pit
 *
 */
public class BuilderRunner extends WorkspaceModifyOperation {
	private static Logger log = Logger.getLogger(BuilderRunner.class);
	
	private IProject project;
	
	@Configurable @Required
	public void setProject(IProject project) {
		this.project = project;
	}
	

	@Override
	protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {
		
		log.debug("called via resource change listener");
		
		// check if project has the builder attached
		Maybe<Boolean> maybe = JavaProjectBuilderHelper.hasBuilder(project, ArtifactReflectionBuilder.ID);
		if (maybe.isUnsatisfied()) {
			String msg = "cannot determine whether project [" + project.getName() + "] has the ARB attached " + maybe.whyUnsatisfied().stringify();
			log.error(msg);
			ArtifactReflectionBuilderStatus status = new ArtifactReflectionBuilderStatus(msg, IStatus.ERROR);
			ArtifactReflectionBuilderPlugin.instance().log(status);
		}
		
		if (maybe.get()) {		
			monitor.beginTask("Building " + project.getName(), IProgressMonitor.UNKNOWN);
			ArtifactReflectionBuilder.build(project);
		}
		monitor.done();
		
	}
	
	

	/**
	 * to be called as job 
	 */
	public void runAsJob() {
		Job job = new WorkspaceJob("Running artifact-reflection build on :" + project.getName()) {
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) {				
				try {
					execute( monitor);
				} catch (Exception e) {
					ArtifactReflectionBuilderStatus status = new ArtifactReflectionBuilderStatus("cannot run artifact-reflection build on :" + project.getName(), e);
					ArtifactReflectionBuilderPlugin.instance().log(status);
				} 
				return Status.OK_STATUS;
			}
			
		};
		job.schedule();
	
	}

}
