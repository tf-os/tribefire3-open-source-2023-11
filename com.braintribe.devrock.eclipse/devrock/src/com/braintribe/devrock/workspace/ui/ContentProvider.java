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
package com.braintribe.devrock.workspace.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;

import com.braintribe.cfg.Configurable;
import com.braintribe.devrock.eclipse.model.workspace.Project;
import com.braintribe.devrock.eclipse.model.workspace.WorkingSet;
import com.braintribe.devrock.eclipse.model.workspace.Workspace;

/**
 * simple {@link ITreeContentProvider} for the selective workspace importer UI
 * @author pit
 *
 */
public class ContentProvider implements ITreeContentProvider {
	
	private Workspace workspace;
	private Workspace[] elements = new Workspace[1];

	public ContentProvider() {	
	}
	
	@Configurable
	public void setWorkspace(Workspace workspace) {
		this.workspace = workspace;
		this.elements[0] = workspace;
	}

	@Override
	public Object[] getChildren(Object arg0) {

		if (arg0 instanceof Workspace) {
			Workspace workspace = (Workspace) arg0;			 
			List<WorkingSet> workingSets = workspace.getWorkingSets();
			List<Project> projects = workspace.getProjects();
			
			List<Object> result  = new ArrayList<>( workingSets.size() + projects.size());
			result.addAll( workingSets);
			result.addAll(projects);
			return result.toArray();
		}
		else if (arg0 instanceof WorkingSet) {
			WorkingSet workingSet = (WorkingSet) arg0;
			return workingSet.getProjects().toArray();			
		}
		else if (arg0 instanceof Project) {
			return new Object[0]; 
		}
		else {
			return new Object[0];
		}
	}

	@Override
	public Object[] getElements(Object arg0) {
		if (arg0 != null) {
			return getChildren(arg0);
		}
		return null;
	}

	@Override
	public Object getParent(Object arg0) {
		return null;
	}

	@Override
	public boolean hasChildren(Object arg0) {
		if (arg0 instanceof Workspace) {
			Workspace workspace = (Workspace) arg0;
			return workspace.getWorkingSets().size() > 0 || workspace.getProjects().size() > 0; 
		}
		else if (arg0 instanceof WorkingSet) {
			WorkingSet workingSet = (WorkingSet) arg0;
			return workingSet.getProjects().size() > 0;
		}
		return false;
	}

}
