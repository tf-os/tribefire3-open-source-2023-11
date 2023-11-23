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

import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;

import com.braintribe.model.maven.settings.Mirror;
import com.braintribe.model.maven.settings.Profile;
import com.braintribe.model.maven.settings.Repository;
import com.braintribe.model.maven.settings.Server;

public class MavenPreferencesTreeLabelProvider extends LabelProvider implements IStyledLabelProvider, ToolTipProvider {
	
	private MavenPreferencesTreeRegistry registry;
	private String key;
	
		
	public MavenPreferencesTreeLabelProvider(MavenPreferencesTreeRegistry registry, String key) {
		this.key = key;
		this.registry = registry;
	}	

	@Override
	public StyledString getStyledText(Object suspect) {
		if (suspect instanceof Profile) {
			if (key.equals( "Profile")) {
				Profile profile = (Profile)suspect;
				StyledString styledString;
				if (registry.isProfileActive(profile)) {
					styledString = new StyledString( profile.getId());
				}
				else {
					styledString = new StyledString( profile.getId(), StyledString.QUALIFIER_STYLER);
				}
				String origin = registry.getOriginOfProfile(profile);
				if (origin != null) {
					styledString.append( " (" + origin + ")", StyledString.COUNTER_STYLER);
				}
				return styledString;
			}
		}
		else if (suspect instanceof Repository) {
			Repository repository = (Repository) suspect;
			if (key.equals( "Profile")) 
				return new StyledString();
			
			if (key.equalsIgnoreCase("Repository"))
				return new StyledString( repository.getId());
			
			if (key.equalsIgnoreCase("Url")) {
				Mirror mirror = registry.getMirror( repository);
				if (mirror == null) {
					return new StyledString( repository.getUrl());
				}
				else {
					return new StyledString( mirror.getUrl(), StyledString.COUNTER_STYLER);
				}
			}
			
			if (key.equalsIgnoreCase("Mirror")) {
				Mirror mirror = registry.getMirror( repository);
				if (mirror != null)
					return new StyledString( mirror.getId());
			}
			if (key.equalsIgnoreCase("Server")) {
				Server server = registry.getServer( repository);
				if (server != null) 
					return new StyledString( server.getId());
			}
			
		}
		return new StyledString();
	}

	@Override
	public String getToolTipText(Object element) {	
		if (key.equals( "Profile")) {
			Profile profile = (Profile) element;
			String origin = registry.getOriginOfProfile(profile);
			return profile.getId() + " from " + origin;						
		}
		if (element instanceof Repository) {
			Repository repository = (Repository) element;
			if (key.equals( "Profile")) 
				return null;
			
			if (key.equalsIgnoreCase("Repository")) {
				String name = repository.getName();
				String id = repository.getId();
				if (name != null)
					return name + "(id)";
				else
					return id;
			}
				
			if (key.equalsIgnoreCase("Url")) {
				return repository.getUrl();
			}
			
			if (key.equalsIgnoreCase("Mirror")) {
				Mirror mirror = registry.getMirror( repository);
				if (mirror != null) {
					return mirror.getId();
				}
			}
			if (key.equalsIgnoreCase("Server")) {
				Server server = registry.getServer( repository);
				if (server != null) 
					return server.getId();
			}
		}
		return null;
	}

	@Override
	public Image getImage(Object element) {
		if (element instanceof Profile) {
			
		}
		
		return super.getImage(element);
	}
	
	

}
