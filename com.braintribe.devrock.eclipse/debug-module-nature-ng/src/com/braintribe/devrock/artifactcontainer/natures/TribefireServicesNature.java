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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

import com.braintribe.devrock.api.nature.CommonNatureIds;
import com.braintribe.devrock.dmb.builder.DebugModuleBuilder;


/**
 * the nature for the module carrier projects
 * 
 * @author pit
 *
 */
public class TribefireServicesNature implements IProjectNature {

	public static final String NATURE_ID = CommonNatureIds.NATURE_DEBUG_MODULE; //$NON-NLS-1$
	private IProject project;
	
	@Override
	public IProject getProject() {		
		return project;
	}

	@Override
	public void setProject(IProject project) {
		this.project = project;		
	}
	
	@Override
	public void configure() throws CoreException {
		IProjectDescription description = project.getDescription();
		ICommand commands[] = description.getBuildSpec();
		
		if (findCommand(DebugModuleBuilder.ID, commands) == -1) {
			List<ICommand> commandList = new ArrayList<ICommand>(Arrays.asList(commands));
			ICommand builderCommand = description.newCommand();
			builderCommand.setBuilderName(DebugModuleBuilder.ID);
			commandList.add(builderCommand);
			ICommand manipulatedCommands[] = commandList.toArray(new ICommand[commandList.size()]);
			description.setBuildSpec(manipulatedCommands);
			project.setDescription(description, null);
		}
	}
	
	private static int findCommand(String name, ICommand[] commands) {
		int i = 0;
		for (ICommand iCommand : commands) {
			if (iCommand.getBuilderName().equals(name))
				return i;
			i++;
		}
		return -1;
	}

	@Override
	public void deconfigure() throws CoreException {
		IProjectDescription description = project.getDescription();
		ICommand commands[] = description.getBuildSpec();
		
		int pos = findCommand(DebugModuleBuilder.ID, commands);
		if (pos != -1) {
			List<ICommand> commandList = new ArrayList<ICommand>(Arrays.asList(commands));
			commandList.remove(pos);
			ICommand manipulatedCommands[] = commandList.toArray(new ICommand[commandList.size()]);
			description.setBuildSpec(manipulatedCommands);
			project.setDescription(description, null);
		}
	}

}
