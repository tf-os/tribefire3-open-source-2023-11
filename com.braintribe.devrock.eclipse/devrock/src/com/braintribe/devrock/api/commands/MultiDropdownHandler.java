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
package com.braintribe.devrock.api.commands;

import java.lang.reflect.Array;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;

public abstract class MultiDropdownHandler extends AbstractDropdownCommandHandler  {

	protected String paramForGlobalExecutions = "ALL";
	protected String paramForMultipleExecutions = "SELECTED";
	
	@Override
	public void process(String parameter) {
				
		if (parameter != null) { 
			if (parameter.equalsIgnoreCase( paramForGlobalExecutions)) {			
				IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();			
				IProject [] projects = root.getProjects();
				executeMultipleCommand( projects);						
			} 
			else if (parameter.equalsIgnoreCase(paramForMultipleExecutions)) {
				Set<IProject> projects = getTargetProjects();
				if (projects != null) {
					executeMultipleCommand( projects.toArray( new IProject[0]));
				}
			}
		}
		else {							
			executeSingle( getTargetProject());
		}
	}		
	
	/**
	 * @param projects - an {@link Array} of {@link IProject} to be processed in sequence
	 */
	protected abstract void executeMultipleCommand(IProject ... projects); 			
}
