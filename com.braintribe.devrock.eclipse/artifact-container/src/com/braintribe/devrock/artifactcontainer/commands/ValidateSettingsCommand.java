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
package com.braintribe.devrock.artifactcontainer.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;

public class ValidateSettingsCommand extends AbstractHandler  {

	public ValidateSettingsCommand() {	
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		Job validationJob = new Job( "Internal validation") {				
			@Override
			protected IStatus run(IProgressMonitor progressMonitor) {
				ArtifactContainerPlugin.getInstance().getValidator().validate( progressMonitor);
				return Status.OK_STATUS;
			}
		};
					
		validationJob.schedule();
		return null;
	}
	

}
