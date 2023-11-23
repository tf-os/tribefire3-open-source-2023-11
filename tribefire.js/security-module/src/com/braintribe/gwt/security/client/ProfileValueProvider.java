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
import java.util.function.Supplier;

import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.Required;


public class ProfileValueProvider implements Supplier<String> {
	private String profileName;
	private SecurityService securityService;
	
	@Configurable @Required
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}
	
	@Configurable @Required
	public void setProfileName(String profileName) {
		this.profileName = profileName;
	}
	
	@Override
	public String get() throws RuntimeException {
		try {
			Session session = securityService.getSession();
			Map<String, String> profileData = session != null? session.getProfileData(): null;
			return profileData != null? profileData.get(profileName): null;
		} catch (SecurityServiceException e) {
			throw new RuntimeException("error while accessing session", e);
		}
	}
}
