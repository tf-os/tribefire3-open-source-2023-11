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
package com.braintribe.devrock.mungojerry.preferences.maven;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;

import com.braintribe.build.artifact.representations.RepresentationException;
import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenSettingsReader;
import com.braintribe.devrock.mungojerry.plugin.Mungojerry;
import com.braintribe.model.maven.settings.Mirror;
import com.braintribe.model.maven.settings.Profile;
import com.braintribe.model.maven.settings.Repository;
import com.braintribe.model.maven.settings.Server;

public class MavenPreferencesTreeRegistry {

	private MavenSettingsReader reader;
	private Map<Repository, Profile> repositoryOwnerMap;
		
	public void setup(MavenSettingsReader reader) {
		this.reader = reader;
		repositoryOwnerMap = new HashMap<Repository, Profile>();
		try {
			List<Profile> profiles = reader.getAllProfiles();
			for (Profile profile : profiles) {
				List<Repository> repositories = getRepositoriesOfProfile(profile);
				for (Repository repository : repositories) {
					repositoryOwnerMap.put(repository, profile);
				}
			}
		} catch (RepresentationException e) {
			Mungojerry.log(IStatus.ERROR, "Cannot retrieve repositories as [" + e.getMessage() + "]");
		}		
	}
		
	public boolean isProfileActive( Profile profile) {
		try {
			return reader.isProfileActive( profile);
		} catch (RepresentationException e) {
			return false;
		}
	}
	public Profile getProfileOfRepository( Repository repository) {
		return repositoryOwnerMap.get(repository);
	}

	public String getOriginOfProfile( Profile profile) {
		try {
			return reader.getProperty( profile.getId(), "MC_ORIGIN");
		} catch (RepresentationException e) {
			return null;
		}
	}
	public List<Repository> getRepositoriesOfProfile( Profile profile) {
		return profile.getRepositories();		
	}
	
	public boolean getDynamicUpdatePolicySupport( Repository repository) {
		Profile profile = getProfileOfRepository(repository);
		if (profile == null)
			return false;
		try {
			return reader.isDynamicRepository(profile, repository);
		} catch (RepresentationException e) {
			return false;
		}	
	}
	
	public Mirror getMirror( Repository repository) {
		try {
			return reader.getMirror( repository.getId(), repository.getUrl());
		} catch (RepresentationException e) {
			return null;
		}
	}
	
	public Server getServer( Repository repository){
		Mirror mirror = getMirror(repository);
		if (mirror != null) {
			try {
				return reader.getServerById( mirror.getId());
			} catch (RepresentationException e) {
				return null;
			}
		}
		return null;
	}
}
