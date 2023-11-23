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
package com.braintribe.model.processing.session.impl.persistence.auth;

import java.util.Set;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.model.processing.session.api.persistence.auth.SessionAuthorization;

public class BasicSessionAuthorization implements SessionAuthorization {

	private String userId;
	private String userName;
	private String sessionId;
	private Set<String> userRoles;
	
	@Required @Configurable
	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	@Required @Configurable
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	@Required @Configurable
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	
	@Required @Configurable
	public void setUserRoles(Set<String> userRoles) {
		this.userRoles = userRoles;
	}
	
	@Override
	public String getUserId() {
		return userId;
	}

	@Override
	public String getUserName() {
		return userName;
	}
	
	@Override
	public String getSessionId() {
		return sessionId;
	}

	@Override
	public Set<String> getUserRoles() {
		return userRoles;
	}

}
