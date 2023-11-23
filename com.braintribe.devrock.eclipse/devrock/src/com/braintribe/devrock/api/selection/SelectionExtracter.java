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
package com.braintribe.devrock.api.selection;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.IWorkbenchAdapter;

import com.braintribe.common.lcd.Pair;
import com.braintribe.provider.Box;

/**
 * tooling to extract selections (from the workspace, notably the PackageExplorer or the ProjectExplorer)
 * handles : IProject, ISelection, IWorkingSet
 * for more complex returns (actually artifacts) see {@link EnhancedSelectionExtracter}
 * 
 * 
 * added cache and hysteresis for current selection..
 * 
 * @author pit
 *
 */
public class SelectionExtracter {
	
	private static ISelection currentSelection;
	private static long lastAccess = 0;
	private static final long delay = 1000;
	private Object monitor = new Object();
	private static SelectionExtracter instance = new SelectionExtracter();  
	
	
	public static ISelection currentSelection() {
		return instance.currentCachedSelection();
	}

	private  ISelection currentCachedSelection() {
		
		synchronized (monitor) {
		
			long currentTime = System.currentTimeMillis();				
			if (currentSelection != null) {
				if ((currentTime - lastAccess) > delay) {
					currentSelection = _currentSelection();
				}			
	
			}
			else {
				currentSelection = _currentSelection();
			}
			lastAccess = currentTime;
		}
		
		return currentSelection;
	}
	
	/** @return current {@link ISelection} if available or null */
	private static ISelection _currentSelection() {
		IWorkbench iworkbench = PlatformUI.getWorkbench();

		IWorkbenchWindow activeWindow = iworkbench.getActiveWorkbenchWindow();
		if (activeWindow != null)
			return extractSelection(activeWindow);

		// The above doesn't work if say QuickImportDialog is open, the activeWindow is null.
		// We can access it this way, is probably safe as long as there is only one workbench window in our workbench. (No idea what that means.)
		if (iworkbench.getWorkbenchWindowCount() == 1)
			return extractSelectionFromInactiveWorkbenchWindow(iworkbench.getWorkbenchWindows()[0]);

		return null;
	}
	

	private static ISelection extractSelectionFromInactiveWorkbenchWindow(IWorkbenchWindow window) {
		// If we don't call it this way, we get: org.eclipse.swt.SWTException: Invalid thread access
		// there is also syncCall, added with 2022/03, so we don't use it for backwards compatibility
		Box<ISelection> resultBox = new Box<>();
		Display.getDefault().syncExec(() -> resultBox.value = extractSelection(window));
		return resultBox.value;
	}

	/**
	 * @param activeWorkbenchWindow - the currently active {@link IWorkbenchWindow} or null
	 * @return - the current {@link ISelection} from the {@link IWorkbenchWindow} passed
	 */
	public static ISelection currentSelection(IWorkbenchWindow activeWorkbenchWindow) {
		if (activeWorkbenchWindow == null) {
			IWorkbench iworkbench = PlatformUI.getWorkbench();
			activeWorkbenchWindow = iworkbench.getActiveWorkbenchWindow();
		}
		if (activeWorkbenchWindow == null)
			return null;

		return extractSelection(activeWorkbenchWindow);
	}

	private static ISelection extractSelection(IWorkbenchWindow iworkbenchwindow) {
		IWorkbenchPage page =  iworkbenchwindow.getActivePage();
		ISelection selection = page.getSelection();		
		return selection;
	}

	public static IProject currentProject() {
		return currentProject( currentSelection());
	}
	
	/**
	 * return the FIRST selected {@link IProject} from the {@link ISelection} or null if unable 
	 * @param selection - the {@link ISelection} as received from the package explorer
	 * @return - the {@link IProject} selected (or deduced) or null if unable
	 */
	public static IProject currentProject( ISelection selection) {		
		IProject project;
		if (selection instanceof IStructuredSelection) {
			for (Iterator<?> it = ((IStructuredSelection) selection).iterator(); it.hasNext();) {
				Object element = it.next();
				
				project = extractProjectFromIAdaptable(element);
				if (project != null)
					return project;							
			}
		}
		return null;
	}
	
	
	
	/**
	 * returns ALL selected {@link IProject} from an {@link ISelection}
	 * @param selection - the {@link ISelection}
	 * @return - a {@link Set} of all {@link IProject} selected in the {@link ISelection}
	 */
	public static Set<IProject> selectedProjects(ISelection selection) {		
		Set<IProject> projects = new HashSet<IProject>();
		
		if (selection instanceof IStructuredSelection) {
			for (Iterator<?> it = ((IStructuredSelection) selection).iterator(); it.hasNext();) {
				Object element = it.next();
				
				IProject project = extractProjectFromIAdaptable(element);
				if (project != null) {
					projects.add(project);
				}					
			}
		}
		return projects;
	}	
		
	
	public static Map<IProject, IWorkingSet> selectedProjectsAndWorkingSets( ) {
		
		return null;
	}
	
	/**
	 * @param element - a member data of the {@link ISelection}
	 * @return - an {@link IProject} if possible
	 */
	public static IProject extractProjectFromIAdaptable( Object element) {
		if (element instanceof IAdaptable == false) {
			return null;
		}
		IAdaptable iAdaptable = (IAdaptable) element;
		
		IProject project = null;

		// project itself
			project = iAdaptable.getAdapter(IProject.class);
			if (project != null)
				return project;
			
			// package fragment
			IPackageFragment fragment = iAdaptable.getAdapter( IPackageFragment.class);
			if (fragment != null) {														
				project = fragment.getJavaProject().getProject();
				if (project != null) {
					return project;
				}					
			}
			// resource
			IResource resource = iAdaptable.getAdapter( IResource.class);
			if (resource != null) {
				project = resource.getProject();
				if (project != null) {
					return project;
				}
			}
			// workbench adapter
			IWorkbenchAdapter workbenchAdapter = iAdaptable.getAdapter( IWorkbenchAdapter.class);
			if (workbenchAdapter != null) {
				Object obj = workbenchAdapter.getParent( iAdaptable);
				if (obj instanceof IJavaProject) {
					project = ((IJavaProject) obj).getProject();
					if (project != null) {									
						return project;
					}
				}
				else if (obj instanceof IAdaptable) {
					return extractProjectFromIAdaptable( obj);
				}
			}							
		
		return project;		
	}
	
	/**
	 * Equivalent to {@code selectedWorkingSet(currentSelection())}.
	 *
	 * @see #currentSelection()
	 * @see #selectedWorkingSet(ISelection)
	 */
	public static IWorkingSet currentlySelectedWorkingSet() {
		ISelection currentSelection = currentSelection();
		return currentSelection == null ? null : selectedWorkingSet(currentSelection);
	}

	/**
	 * return the selected or deduced {@link IWorkingSet} if any  
	 */
	public static IWorkingSet selectedWorkingSet(ISelection selection) {
		Pair<IProject,IWorkingSet> tuple = extractProjectAndWorkingset(selection);
		if (tuple == null)
			return null;
		return tuple.second;
	}
	
	public static List<IWorkingSet> selectedWorkingSets( ISelection selection) {
		List<IWorkingSet> result = new ArrayList<>();
		if (selection instanceof IStructuredSelection) {
			for (Object element : ((IStructuredSelection) selection)) {
				if (element instanceof IWorkingSet) {
					result.add( (IWorkingSet) element);
				}
			}
		}
		return result;			
	}
	
	
	/**
	 * @param workingSet
	 * @return
	 */
	public static List<IProject> projectsOfWorkingset(IWorkingSet workingSet) {
		List<IProject> result = new ArrayList<>();
		for (IAdaptable adaptable : workingSet.getElements()) {
			IProject project = adaptable.getAdapter(IProject.class);
			if (project != null) {
				result.add(project);				
			}
		}		
		return result;
	}
	
	/**
	 * @param selection - the {@link ISelection}
	 * @return - a {@link Pair} of {@link IProject} and {@link IWorkingSet} (both may be null)
	 */
	public static Pair<IProject,IWorkingSet> extractProjectAndWorkingset(ISelection selection) {
		// find if a working set is directly specified in the selection		
		if (selection instanceof IStructuredSelection)
			for (Object element : ((IStructuredSelection) selection))
				if (element instanceof IWorkingSet)
					return Pair.of( null, (IWorkingSet) element);
		
		// 
		IProject selectedProject = currentProject(selection);		
		IWorkingSetManager manager = PlatformUI.getWorkbench().getWorkingSetManager();
		IWorkingSet[] workingSets = manager.getWorkingSets();
		if (workingSets == null || workingSets.length == 0) {
			return Pair.of( selectedProject, null);
		}
		for (IWorkingSet workingSet : workingSets) {
			for (IAdaptable adaptable : workingSet.getElements()) {
				IProject project = adaptable.getAdapter(IProject.class);
				if (project != null && project == selectedProject) {
					return Pair.of( selectedProject, workingSet);
				}
			}
		}		
		return Pair.of(null, null);		
	}

	
 
	/**
	 * @param project
	 * @return
	 */
	public static IWorkingSet getOwningWorkingset(IProject project) {
		IWorkingSetManager manager = PlatformUI.getWorkbench().getWorkingSetManager();
		IWorkingSet[] workingSets = manager.getWorkingSets();
		if (workingSets == null || workingSets.length == 0) {			
			return null;
		}
		for (IWorkingSet workingSet : workingSets) {
			for (IAdaptable adaptable : workingSet.getElements()) {
				IProject suspect = adaptable.getAdapter(IProject.class);
				if (suspect != null && suspect == project) {
					return workingSet;
				}
			}
		}	
		return null;		
	}
	
	

}
