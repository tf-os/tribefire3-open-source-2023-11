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
package com.braintribe.devrock.dmb.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;

import com.braintribe.devrock.api.nature.NatureHelper;
import com.braintribe.devrock.api.selection.SelectionExtracter;
import com.braintribe.devrock.artifactcontainer.natures.TribefireServicesNature;
import com.braintribe.devrock.dmb.plugin.DebugModuleBuilderPlugin;
import com.braintribe.devrock.dmb.plugin.DebugModuleBuilderStatus;

/**
 * command to add a 'tribefire services' nature to a project
 * 
 * @author pit
 *
 */
public class AddTribefireServicesNature extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IProject project = SelectionExtracter.currentProject( SelectionExtracter.currentSelection());
		if (project != null) {
			if (!NatureHelper.addNature(project, TribefireServicesNature.NATURE_ID)) {
				DebugModuleBuilderStatus status = new DebugModuleBuilderStatus("cannot add nature [" + TribefireServicesNature.NATURE_ID + "] to project [" + project.getName() + "]", IStatus.ERROR);
				DebugModuleBuilderPlugin.instance().log(status);
			}
			
		}
		return null;
	}

	
}
