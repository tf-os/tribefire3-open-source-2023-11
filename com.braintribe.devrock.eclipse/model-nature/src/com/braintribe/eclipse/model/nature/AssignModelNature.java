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
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.statushandlers.StatusManager;

public class AssignModelNature extends AbstractHandler{

	private static ISelection getCurrentSelection() {
		IWorkbench iworkbench = PlatformUI.getWorkbench();
		IWorkbenchWindow iworkbenchwindow = iworkbench.getActiveWorkbenchWindow();
		if (iworkbenchwindow == null)
			return null;
		IWorkbenchPage page =  iworkbenchwindow.getActivePage();
		ISelection selection = page.getSelection();
		return selection;
	}
	
	private void addNatureToSelection() throws ExecutionException {
		IStructuredSelection selection = (IStructuredSelection) getCurrentSelection();
		
		IProject project = null;
		
		for (@SuppressWarnings("unchecked")
		Iterator<Object> it = selection.iterator(); it.hasNext();) {
			Object item = it.next();			
			if (item instanceof IAdaptable) {
				IAdaptable adaptable = (IAdaptable) item;							
				IPackageFragment fragment = adaptable.getAdapter( IPackageFragment.class);
				if (fragment != null) {
					project = fragment.getJavaProject().getProject();
					if (project != null) {
						break;
					}
				}
				IResource resource =  adaptable.getAdapter( IResource.class);
				if (resource != null) {
					project = resource.getProject();
					if (project != null) {
						break;
					}
				}
				IWorkbenchAdapter workbenchAdapter = adaptable.getAdapter( IWorkbenchAdapter.class);
				if (workbenchAdapter != null) {
					Object obj = workbenchAdapter.getParent( item);
					if (obj instanceof IJavaProject) {
						project = ((IJavaProject) obj).getProject();
						if (project != null) {									
							break;
						}
					}
				}
			
				IJavaProject javaproject = adaptable.getAdapter(IJavaProject.class);
				if (javaproject != null) {
					project = javaproject.getProject();
					break;
				}
				project = ((IAdaptable) item).getAdapter(IProject.class);
				if (project != null) {			
					break;
				}
			}						
		}
		if (project != null) {
			addNature(project);
		}
	}
	
	private void addNature(IProject project) throws ExecutionException {
		try {
			IProjectDescription description = project.getDescription();
			Set<String> natures = new TreeSet<>(Arrays.asList(description.getNatureIds()));
			
			if (natures.add(ModelNature.NATURE_ID)) {
				String manipulatedNatures[] = natures.toArray(new String[natures.size()]);
				description.setNatureIds(manipulatedNatures);
				project.setDescription(description, null);
			}
		} catch (CoreException e) {
			String msg = "error while adding nature to project description for " + project.getName();
			ModelBuilderStatus status = new ModelBuilderStatus(msg, e);
			StatusManager.getManager().handle(status);
			throw new ExecutionException(msg, e);
		}
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		addNatureToSelection();
		return null;
	}

}
