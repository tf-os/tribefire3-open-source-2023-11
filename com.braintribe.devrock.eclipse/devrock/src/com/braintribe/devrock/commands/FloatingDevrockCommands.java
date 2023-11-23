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
package com.braintribe.devrock.commands;

import org.eclipse.core.commands.ExecutionException;

import com.braintribe.devrock.api.commands.AbstractDropdownCommandHandler;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.devrock.plugin.DevrockPluginStatus;

/**
 * @author pit
 *
 */
public class FloatingDevrockCommands extends AbstractDropdownCommandHandler {	
	private static final String PARAM_WORKSPACE_IMPORT = "WORKSPACE-IMPORT";
	private static final String PARAM_REPOSITORY_VIEW = "REPOSITORY-VIEW";
	
	// must declare the parameter that this commands wants
	{
		PARM_MSG = "com.braintribe.devrock.devrock.command.param.floating";
	}
	
	
	@Override
	public void process(String param) {
		
		try {
			switch (param) {
				case PARAM_WORKSPACE_IMPORT:			
					new ImportWorkspacePopulation().execute( null);			
					break;
				case PARAM_REPOSITORY_VIEW:
					new RepositoryConfigurationInfoCommand().execute(null);
					break;
				default:
					break;					
			}
		} catch (ExecutionException e) {
			DevrockPluginStatus status = new DevrockPluginStatus("cannot run command with key [" + param + "]", e);
			DevrockPlugin.instance().log(status);
		}
		
		
	}

	
		
}
