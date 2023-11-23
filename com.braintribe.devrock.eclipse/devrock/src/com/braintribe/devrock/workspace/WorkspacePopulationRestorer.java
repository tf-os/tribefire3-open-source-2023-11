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
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

import com.braintribe.devrock.eclipse.model.storage.StorageLockerPayload;
import com.braintribe.devrock.eclipse.model.workspace.ExportPackage;
import com.braintribe.devrock.eclipse.model.workspace.Workspace;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.devrock.plugin.DevrockPluginStatus;

/**
 * {@link WorkspaceModifyOperation} implementation to run a workspace-restorer as a job  
 * @author pit
 *
 */
public class WorkspacePopulationRestorer extends WorkspaceModifyOperation {
	private ExportPackage content;

	/**
	 * @param file - the file with the {@link Workspace} 
	 */
	public WorkspacePopulationRestorer(File file) {	
		content = WorkspacePopulationMarshaller.load( file);		
	}
	
	/**
	 * @param content - the {@link Workspace} instance 
	 */
	public WorkspacePopulationRestorer(Workspace content) {
		ExportPackage ep = ExportPackage.T.create();
		ep.setWorkspace(content);
		this.content = ep;		
	}
			
	/**
	 * @param workspace
	 * @param storageLockerPayload
	 */
	public WorkspacePopulationRestorer(Workspace workspace, StorageLockerPayload storageLockerPayload) {
		ExportPackage ep = ExportPackage.T.create();
		ep.setStorageLockerPayload(storageLockerPayload);
		ep.setWorkspace(workspace);
		this.content = ep;
	}

	@Override
	protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {	
		WorkspacePopulationMarshaller wpm = new WorkspacePopulationMarshaller();
		wpm.restoreWorkspace(monitor, content);
		
	}
	
	/**
	 * threaded job to import workspace
	 */
	public void runAsJob() {
		Job job = new WorkspaceJob("Re-importing exported workspace") {
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) {
				// import				
				try {
					execute( monitor);
				} catch (Exception e) {
					DevrockPluginStatus status = new DevrockPluginStatus("cannot re-import exported workspace", e);
					DevrockPlugin.instance().log(status);
				} 
				return Status.OK_STATUS;
			}
			
		};
		job.schedule();
	
	}
	

}
