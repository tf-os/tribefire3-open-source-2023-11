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
package com.braintribe.devrock.mnb.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;

import com.braintribe.devrock.api.nature.NatureHelper;
import com.braintribe.devrock.api.selection.SelectionExtracter;
import com.braintribe.devrock.mnb.natures.ModelNature;
import com.braintribe.devrock.mnb.plugin.ModelBuilderPlugin;
import com.braintribe.devrock.mnb.plugin.ModelBuilderStatus;

/**
 * remove the model nature from the currently selected project
 * @author pit
 *
 */
public class RemoveModelNature extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IProject project = SelectionExtracter.currentProject( SelectionExtracter.currentSelection());
		if (project != null) {
			if (!NatureHelper.removeNature(project, ModelNature.NATURE_ID)) {
				String msg = "error while removing nature [" + ModelNature.NATURE_ID + "] to project [" + project.getName() + "]";
				ModelBuilderStatus status = new ModelBuilderStatus( msg, IStatus.ERROR);
				ModelBuilderPlugin.instance().log(status);				
			}
		}
		return null;
	}

}
