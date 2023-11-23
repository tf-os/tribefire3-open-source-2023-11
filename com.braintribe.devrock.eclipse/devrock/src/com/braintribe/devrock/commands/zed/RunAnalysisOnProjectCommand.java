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
package com.braintribe.devrock.commands.zed;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.braintribe.devrock.api.selection.SelectionExtracter;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.devrock.plugin.DevrockPluginStatus;
import com.braintribe.logging.Logger;

/**
 * run a zed analysis on the currently selected project
 * @author pit
 *
 */
public class RunAnalysisOnProjectCommand extends AbstractHandler  implements ZedRunnerTrait {
	private static Logger log = Logger.getLogger(RunAnalysisOnProjectCommand.class);
				
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		Job job = new Job("preparing zed's analysis") {						
			@Override
			public IStatus run(IProgressMonitor arg0) {
				try {
					_execute( event);
					return Status.OK_STATUS;
				} catch (ExecutionException e) {
					log.error( "can't run analysis job", e);
					return Status.CANCEL_STATUS;
				}
			}
		};
		job.schedule();		
				
		return null;
	}
				
	
	private Object _execute(ExecutionEvent event) throws ExecutionException {
		
		IProject project = SelectionExtracter.currentProject( SelectionExtracter.currentSelection());
		try {
			if (project != null) {
				ZedRunnerTrait.process( project);				
			} // project != null
		} catch (CoreException e) {
			DevrockPluginStatus status = new DevrockPluginStatus("Project [" + project.getName() + "] cannot be processed", e);
			DevrockPlugin.instance().log(status);
			return null;
		}						
		return null;
	}
	
	}
