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
package com.braintribe.devrock.ac.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.braintribe.devrock.ac.container.plugin.ArtifactContainerPlugin;
import com.braintribe.devrock.ac.container.plugin.ArtifactContainerStatus;
import com.braintribe.devrock.mc.api.repository.configuration.RepositoryReflection;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.gm.model.reason.Maybe;

public class PurgeLockFilesCommand  extends AbstractHandler {
	private List<File> lockFilesFound;

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		Maybe<RepositoryReflection> repositoryReflectionMaybe = DevrockPlugin.mcBridge().reflectRepositoryConfiguration();
		if (!repositoryReflectionMaybe.isSatisfied()) {
			ArtifactContainerStatus acs = new ArtifactContainerStatus("cannot retrieve repository reflection as " + repositoryReflectionMaybe.whyUnsatisfied(), IStatus.ERROR);
			ArtifactContainerPlugin.instance().log(acs);
			return null;
		}
		RepositoryReflection reflection = repositoryReflectionMaybe.get();
		String localRepositoryPath = reflection.getRepositoryConfiguration().getCachePath();
		File repoRoot = new File( localRepositoryPath);
		if (!repoRoot.exists()) {
			ArtifactContainerStatus acs = new ArtifactContainerStatus("referenced local repository [" + localRepositoryPath  + "] doesn't exist", IStatus.ERROR);
			ArtifactContainerPlugin.instance().log(acs);
			return null;	
		}
		// scan for *.lck files
		lockFilesFound = new ArrayList<>(50);
		
		Job job = new WorkspaceJob("purging *.lck files") {
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) {
				monitor.beginTask( "scanning for *.lck files", -1);
				scan( repoRoot);				
			
				int numLockFiles = lockFilesFound.size();
				//System.out.println("Found [" + numLockFiles + "] lock files in local repository [" + repoRoot.getAbsolutePath() + "]");
				if (numLockFiles == 0) {
					ArtifactContainerStatus acs = new ArtifactContainerStatus("no lock files found in local repository [" + repoRoot.getAbsolutePath() + "]" , IStatus.INFO);
					ArtifactContainerPlugin.instance().log(acs);
					return Status.OK_STATUS;
				}
					
				int i = 0;
				monitor.beginTask("deleting found lck files", numLockFiles);
				for (File file : lockFilesFound) {
					try {
						file.delete();
						monitor.worked(++i);						
					} catch (Exception e) {
						ArtifactContainerStatus acs = new ArtifactContainerStatus("cannot delete lock file [" + file.getAbsolutePath()  + "]", IStatus.ERROR);
						ArtifactContainerPlugin.instance().log(acs);		
					}
				}
				monitor.done();
				ArtifactContainerStatus acs = new ArtifactContainerStatus("deleted  [" + i  + "] lock files from local repository [" + repoRoot.getAbsolutePath() + "]" , IStatus.INFO);
				ArtifactContainerPlugin.instance().log(acs);
				return Status.OK_STATUS;								
			}
		};
		
		job.schedule();
		
		return null;
	}

	private void scan(File repoRoot) {
		//System.out.println("scanning " + repoRoot.getAbsolutePath());
		File[] files = repoRoot.listFiles();
		if (files != null && files.length > 0) {
			for (File file : files) {
				if (file.isDirectory()) {
					scan( file);
				}
				else {
					if (file.getName().endsWith(".lck")) {
						lockFilesFound.add(file);
						//System.out.println("detected lck file : " + file.getAbsolutePath());
					}				
				}
			}
		}		
	}

	
}
