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
package com.braintribe.devrock.artifactcontainer.plugin.preferences.maven;

import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.model.maven.settings.Profile;
import com.braintribe.model.maven.settings.Repository;

public class MavenPreferencesTreeContentProvider implements ITreeContentProvider {

	private MavenPreferencesTreeRegistry registry;
	
	@Configurable @Required
	public void setRegistry(MavenPreferencesTreeRegistry registry) {
		this.registry = registry;
	}
	
	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
	}

	@Override
	public Object[] getChildren(Object arg0) {
		Profile profile = (Profile) arg0;		
		List<Repository> repositories= registry.getRepositoriesOfProfile(profile);
		if (repositories == null)
			return null;		
		return repositories.toArray();				
	}

	@Override
	public Object[] getElements(Object arg0) {
		@SuppressWarnings("unchecked")
		List<Profile> profiles = (List<Profile>) arg0;
		return profiles.toArray();
	}

	@Override
	public Object getParent(Object arg0) {
		if (arg0 instanceof Repository) {
			Repository repository = (Repository) arg0;
			return registry.getProfileOfRepository(repository);
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object arg0) {
		if (arg0 instanceof Profile) {
			Profile profile = (Profile) arg0;
			if (getChildren(profile) != null) {
				return true;			
			}
		}
		return false;
	}

	
}
