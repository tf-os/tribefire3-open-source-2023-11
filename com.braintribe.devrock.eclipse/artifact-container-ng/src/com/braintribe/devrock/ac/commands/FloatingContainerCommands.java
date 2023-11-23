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

import org.eclipse.core.commands.ExecutionException;

import com.braintribe.devrock.api.commands.AbstractDropdownCommandHandler;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.devrock.plugin.DevrockPluginStatus;

/**
 * @author pit
 *
 */
public class FloatingContainerCommands extends AbstractDropdownCommandHandler {	
	private static final String PARAM_ANALYSIS_TERMINAL = "WORKSPACE-IMPORT";
	private static final String PARAM_ANALYISIS_PERSISTED = "REPOSITORY-VIEW";
	
	// must declare the parameter that this commands wants
	{
		PARM_MSG = "com.braintribe.devrock.artifactcontainer.command.param.floating";
	}
	
	
	@Override
	public void process(String param) {
		
		try {
			switch (param) {
				case PARAM_ANALYSIS_TERMINAL:			
					new AnalyseArtifactCommand().execute(null);			
					break;
				case PARAM_ANALYISIS_PERSISTED:
					new AnalyseResolutionCommand().execute(null);
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
