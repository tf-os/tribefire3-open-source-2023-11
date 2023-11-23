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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;

import com.braintribe.common.lcd.Pair;

/**
 * an expert to extract the currently selected project and/or the currently active working set in the package explorer
 * @author pit
 *
 */
public class TargetProviderImpl implements TargetProvider {
	IWorkingSet activeWorkingSet;
	IProject activeProject;
		
	private ISelection extractSelection(){
		IWorkbench iworkbench = PlatformUI.getWorkbench();
		IWorkbenchWindow iworkbenchwindow = iworkbench.getActiveWorkbenchWindow();
		if (iworkbenchwindow == null)
			return null;
		IWorkbenchPage page =  iworkbenchwindow.getActivePage();
		ISelection selection = page.getSelection();
		return selection;
	}
	
	private void extractTargets() {		
		  ISelection selection = extractSelection();
		  if (selection != null) {
			  activeProject = SelectionExtracter.currentProject(selection);
			  activeWorkingSet = SelectionExtracter.selectedWorkingSet(selection);
		  }
	}
	
	@Override
	public void clear() {
		activeProject = null;
		activeWorkingSet = null;
	}



	@Override
	public IWorkingSet getTargetWorkingSet() {
		if (activeWorkingSet == null) { 
			extractTargets();
		}
		return activeWorkingSet;
	}

	@Override
	public IProject getTargetProject() {
		if (activeProject == null) {
			extractTargets();
		}				
		return activeProject;
	}

	@Override
	public Pair<IProject,IWorkingSet> getSelectionTuple() {
		ISelection selection = extractSelection();
		if (selection == null)
			return null;
		return SelectionExtracter.extractProjectAndWorkingset(selection);
	}
	@Override
	public Set<IProject> getTargetProjects() {
		ISelection selection = extractSelection();
		if (selection == null)
			return new HashSet<IProject>();		
		return SelectionExtracter.selectedProjects(selection);
	}
	
	

}
