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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.braintribe.devrock.ac.container.plugin.ArtifactContainerPlugin;
import com.braintribe.devrock.ac.container.plugin.ArtifactContainerStatus;
import com.braintribe.devrock.ac.container.updater.WorkspaceUpdater;
import com.braintribe.logging.Logger;

/**
 * command to update the full workspace
 * @author pit
 *
 */
public class BuildWorkspaceCommand extends AbstractHandler{
	private static Logger log = Logger.getLogger(BuildWorkspaceCommand.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			WorkspaceUpdater updater = new WorkspaceUpdater();			
			updater.runAsJob();
			//updater.run( new NullProgressMonitor());
		} catch (Exception e) {
			String msg = "cannot run project updater";
			log.error(msg, e);
			ArtifactContainerStatus status = new ArtifactContainerStatus( msg, e);
			ArtifactContainerPlugin.instance().log(status);
		}
		return null;				
	}
			
		
	
}
