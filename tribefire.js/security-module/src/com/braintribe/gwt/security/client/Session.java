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
package com.braintribe.gwt.security.client;

import java.util.Map;
import java.util.Set;

/**
 * This class holds the session informations.
 *
 */
public class Session {
	private String username;
	private String id;
	private Set<String> roles;
	private String fullName;
	private Map<String, String> profileData;
	
	public Session(String username, String id) {
		super();
		this.username = username;
		this.id = id;
	}
	
	public void setProfileData(Map<String, String> profileData) {
		this.profileData = profileData;
	}
	
	public Map<String, String> getProfileData() {
		return profileData;
	}
	
	public void setRoles(Set<String> roles) {
		this.roles = roles;
	}
	
	public Set<String> getRoles() {
		return roles;
	}

	public String getUsername() {
		return username;
	}

	public String getId() {
		return id;
	}
	
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	
	public String getFullName() {
		return fullName;
	}
	
}
