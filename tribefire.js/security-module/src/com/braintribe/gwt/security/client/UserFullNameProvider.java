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

import java.util.function.Supplier;

import com.braintribe.gwt.ioc.client.Required;


/**
 * This provider provides the full name of the user, if available, or the userName.
 * @author michel.docouto
 *
 */
public class UserFullNameProvider implements Supplier<String> {
	
	private SecurityService securityService;
	
	/**
	 * Configures the required {@link SecurityService} used for getting the user info.
	 */
	@Required
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	@Override
	public String get() throws RuntimeException {
		Session session = null;
		try {
			session = securityService.getSession();
		} catch (SecurityServiceException e) {
			throw new RuntimeException("error while accessing session", e);
		}
		
		if (session != null) {
			return session.getFullName() == null || session.getFullName().isEmpty() ? session.getUsername() : session.getFullName();
		} else {
			return "";
		}
	}

}
