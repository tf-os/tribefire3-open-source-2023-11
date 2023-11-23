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
package com.braintribe.devrock.mj.commands;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.ISelection;

import com.braintribe.devrock.api.commands.SingleDropdownHandler;
import com.braintribe.devrock.api.selection.SelectionExtracter;
import com.braintribe.devrock.mj.plugin.MungoJerryPlugin;
import com.braintribe.devrock.mj.plugin.MungoJerryStatus;

/**
 * command for the artifact cloner / copier
 * @author pit
 *
 */
public class MungojerryCommand extends SingleDropdownHandler implements GwtAnalysisTrait {
	
	@Override
	public void process(String parameter) {		
		ISelection selection = SelectionExtracter.currentSelection();
		final IProject project = SelectionExtracter.currentProject( selection);
		
		if (project == null) {
			MungoJerryStatus status = new MungoJerryStatus( "No project selected, no dice", IStatus.INFO);
			MungoJerryPlugin.instance().log(status);			
			return;
		}		 
		process( project);				
	}	
}
