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
package com.braintribe.eclipse.model.nature;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.statushandlers.StatusManager;

public class RemoveModelNature extends AbstractHandler{

	private static ISelection getCurrentSelection() {
		IWorkbench iworkbench = PlatformUI.getWorkbench();
		IWorkbenchWindow iworkbenchwindow = iworkbench.getActiveWorkbenchWindow();
		if (iworkbenchwindow == null)
			return null;
		IWorkbenchPage page =  iworkbenchwindow.getActivePage();
		ISelection selection = page.getSelection();
		return selection;
	}
	
	private void removeNatureFromSelection() throws ExecutionException {
		IStructuredSelection selection = (IStructuredSelection) getCurrentSelection();
		
		for (@SuppressWarnings("unchecked")
		Iterator<Object> it = selection.iterator(); it.hasNext();) {
			Object item = it.next();
			IProject project = ((IAdaptable) item).getAdapter(IProject.class);
			
			if (project != null)
				removeNature(project);
		}
	}
	
	private void removeNature(IProject project) throws ExecutionException {
		try {
			IProjectDescription description = project.getDescription();
			Set<String> natures = new TreeSet<>(Arrays.asList(description.getNatureIds()));
			
			if (natures.remove(ModelNature.NATURE_ID)) {
				String manipulatedNatures[] = natures.toArray(new String[natures.size()]);
				description.setNatureIds(manipulatedNatures);
				project.setDescription(description, null);
			}
		} catch (CoreException e) {
			String msg = "error while adding nature to project description for " + project.getName();
			ModelBuilderStatus status = new ModelBuilderStatus(msg, e);
			StatusManager.getManager().handle(status);
			throw new ExecutionException( msg, e);
		}
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		removeNatureFromSelection();
		return null;
	}

}
