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

import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.Required;


/**
 * This provider provides the current logged in user name.
 * @author michel.docouto
 *
 */
public class UserNameProvider implements Supplier<String> {
	private SecurityService securityService;
	private boolean exceptionOnMissingSession = false;
	
	/**
	 * Configures whether to throw an exception when the session is missing.
	 * Defaults to false.
	 */
	@Configurable
	public void setExceptionOnMissingSession(boolean exceptionOnMissingSession) {
		this.exceptionOnMissingSession = exceptionOnMissingSession;
	}
	
	/**
	 * The security service will be used to retrieve the user name.
	 */
	@Required
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}
	
	@Override
	public String get() throws RuntimeException {
		Session session;
		try {
			session = securityService.getSession();
		} catch (SecurityServiceException e) {
			throw new RuntimeException("error while accessing session", e);
		}
		
		if (session != null) return session.getUsername();
		else {
			if (exceptionOnMissingSession)
				throw new RuntimeException("missing session");
			else return "";
		}
	}

}
