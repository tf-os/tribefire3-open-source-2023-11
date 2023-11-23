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
package com.braintribe.model.uicommand;

import java.util.Map;

import com.braintribe.model.command.Command;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * Command Interface for Run/Firing Workbench Action at TF Studio
 * 
 */
public interface RunWorkbenchAction extends Command {

	final EntityType<RunWorkbenchAction> T = EntityTypes.T(RunWorkbenchAction.class);
	
	/**
	* @param workbenchFolderId
	* 
	* The GlobalId of Workbench Folder which run/fire WorkbenchAction
	*/
	void setWorkbenchFolderId(Object workbenchFolderId);

	/**
	* @return the Object which defines the Workbench Folder GlobalId 
	*/		
	Object getWorkbenchFolderId();
	
	/**
	 * Variable values to be set when the action is a TemplateBasedAction.
	 */
	void setVariables(Map<String, Object> variableValues);

	/**
	 * Variable values when the action is a TemplateBasedAction.
	 */
	Map<String, Object> getVariables();
}
