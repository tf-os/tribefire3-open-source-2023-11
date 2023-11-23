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
package com.braintribe.devrock.mj.commands.natures;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;

import com.braintribe.devrock.api.nature.NatureHelper;
import com.braintribe.devrock.api.selection.SelectionExtracter;
import com.braintribe.devrock.mj.natures.GwtLibraryNature;
import com.braintribe.devrock.mj.natures.GwtTerminalNature;
import com.braintribe.devrock.mj.plugin.MungoJerryPlugin;
import com.braintribe.devrock.mj.plugin.MungoJerryStatus;

/**
 * remove the model nature from the currently selected project
 * @author pit
 *
 */
public class RemoveGwtNature extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IProject project = SelectionExtracter.currentProject( SelectionExtracter.currentSelection());
		if (project != null) {
			if (!NatureHelper.removeNature(project, GwtLibraryNature.NATURE_ID, GwtTerminalNature.NATURE_ID)) {
				MungoJerryStatus status = new MungoJerryStatus("cannot remove nature [" + GwtLibraryNature.NATURE_ID + "] from project [" + project.getName() + "]", IStatus.ERROR);
				MungoJerryPlugin.instance().log(status);
			}
		}
		return null;
	}

}
