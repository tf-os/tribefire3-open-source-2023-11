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
package com.braintribe.devrock.artifactcontainer.control.project;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;

/**
 * @author pit
 *
 */
public class AccessibleProjectsController {
	
	private Set<IProject> projects = null;
	
	private Set<IProject> buildList() {
		Set<IProject> result = new HashSet<IProject>();
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();	
		for (IProject project : root.getProjects()) {
			if (project.isAccessible())
				result.add( project);
		}
		return result;
	}
	
	private boolean compareSets( Set<IProject> one, Set<IProject> two) {
		if (one.size() != two.size())
			return false;
		
		if (two.containsAll( one))
			return true;
		
		return false;
	}
	
	public boolean accessibleProjectsChanged() {
		Set<IProject> projectlist = buildList();
		if (projects == null) {
			projects = projectlist;
			return true;
		}
		boolean retval = compareSets( projects, projectlist);
		projects = projectlist;
		
		return !retval;
	}
	
}
