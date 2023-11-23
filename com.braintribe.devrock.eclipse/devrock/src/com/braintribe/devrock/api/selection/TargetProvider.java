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

import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.ui.IWorkingSet;

import com.braintribe.common.lcd.Pair;

/**
 * interface to deliver targets for import ... <br/>
 * actually, they reflect the current selected entry in the package explorer
 * <ul> 
 * <li>IWorkingSet</li>
 * <li>IProject</li>
 * </ul>
 * @author pit
 *
 */
public interface TargetProvider {
	
	/**
	 * returns a {@link SelectionTuple} of currently selected {@link IWorkingSet} (if any) and currently selected IProject (if any)
	 */
	Pair<IProject,IWorkingSet> getSelectionTuple();
	
	/**
	 * returns the currently selected {@link IWorkingSet} if any 
	 * @return - the currently selected {@link IWorkingSet} or null if none's selected (or cannot be derived from the current selection)
	 */
	IWorkingSet getTargetWorkingSet();
	/**
	 * returns the first currently selected {@link IProject} if any 
	 * @return - the currently selected {@link IProject} or null if none's selected (or cannot be derived from the current selection)
	 */
	IProject getTargetProject();
	
	/**
	 * returns all currently selected {@link IProject}s if any 
	 * @return - all the currently selected {@link IProject}s or null if none's selected (or cannot be derived from the current selection)
	 */
	Set<IProject> getTargetProjects();
	
	void clear(); 
}
