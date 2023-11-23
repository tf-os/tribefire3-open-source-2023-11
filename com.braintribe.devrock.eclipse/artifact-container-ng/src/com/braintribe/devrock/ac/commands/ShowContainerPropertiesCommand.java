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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.braintribe.devrock.ac.container.ArtifactContainer;
import com.braintribe.devrock.ac.container.dialog.ContainerInfoDialog;
import com.braintribe.devrock.ac.container.plugin.ArtifactContainerPlugin;
import com.braintribe.devrock.ac.container.plugin.ArtifactContainerStatus;
import com.braintribe.devrock.api.selection.SelectionExtracter;
import com.braintribe.logging.Logger;

/**
 * command to show the current data of a container. Alternative to using Eclipse's
 * 'container properties' or 'project properties -> configure build path -> container -> edit'
 * 
 * @author pit
 *
 */
public class ShowContainerPropertiesCommand extends AbstractHandler {
	private static Logger log = Logger.getLogger(BuildProjectCommand.class);
	
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
			
		IWorkbenchWindow activeWorkbenchWindow = HandlerUtil.getActiveWorkbenchWindow(event);		
		try {
			ISelection selection = SelectionExtracter.currentSelection(activeWorkbenchWindow);
			if (selection == null)
				return null;
			
			IProject selectedProject = SelectionExtracter.currentProject(selection);
			if (selectedProject == null) {
				return null;
			}
			ArtifactContainer containerOfProject = ArtifactContainerPlugin.instance().containerRegistry().getContainerOfProject(selectedProject);
			if (containerOfProject == null) {
				return null;
			}
			Display display = PlatformUI.getWorkbench().getDisplay();
			ContainerInfoDialog dialog = new ContainerInfoDialog( display.getActiveShell());
			dialog.setContainer(containerOfProject);
			dialog.setProject( selectedProject);
			dialog.open();
			
		} catch (Exception e) {
			String msg = "cannot open container dialog";
			log.error(msg, e);
			ArtifactContainerStatus status = new ArtifactContainerStatus( msg, e);
			ArtifactContainerPlugin.instance().log(status);
		}
		return null;		
	}
}
