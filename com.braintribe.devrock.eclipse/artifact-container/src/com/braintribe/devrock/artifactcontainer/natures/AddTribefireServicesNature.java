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
package com.braintribe.devrock.artifactcontainer.natures;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.statushandlers.StatusManager;

import com.braintribe.devrock.artifactcontainer.ArtifactContainerStatus;
import com.braintribe.plugin.commons.selection.SelectionExtractor;

public class AddTribefireServicesNature extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IProject project = SelectionExtractor.extractSelectedProject();
		if (project != null) {
			addNature(project);
		}
		return null;
	}

	private void addNature(IProject project) throws ExecutionException {
		try {
			IProjectDescription description = project.getDescription();
			Set<String> natures = new TreeSet<>(Arrays.asList(description.getNatureIds()));
			
			if (natures.add( TribefireServicesNature.NATURE_ID)) {
				String manipulatedNatures[] = natures.toArray(new String[natures.size()]);
				description.setNatureIds(manipulatedNatures);
				project.setDescription(description, null);
			}
		} catch (CoreException e) {
			String msg = "error while adding nature ["+ TribefireServicesNature.NATURE_ID + "] to project description for " + project.getName();
			ArtifactContainerStatus status = new ArtifactContainerStatus(msg, e);
			StatusManager.getManager().handle(status);
			throw new ExecutionException(msg, e);
		}
	}
}
