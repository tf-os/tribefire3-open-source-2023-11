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

import com.braintribe.gwt.async.client.Future;
import com.braintribe.model.securityservice.credentials.Credentials;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * This interface show us all methods needed for the SecurityService
 *
 */
public interface SecurityService {
	void addSessionListener(SessionListener sessionListener);
	void removeSessionListener(SessionListener sessionListener);
	Future<Boolean> loginWithExistingSession(String username, String sessionId);
	Future<Boolean> loginSSO();
	Future<Void> loginWithExistingSession(Session session);
	void login(String username, String password, AsyncCallback<Session> asyncCallback);
	void logout(AsyncCallback<Boolean> asyncCallback);
	void logout(boolean silent, AsyncCallback<Boolean> asyncCallback);
	Session getSession() throws SecurityServiceException;
	Future<Boolean> changePassword(String oldPassword, String newPassword);
	
	/**
	 * Rechecks the current user password.
	 */
	Future<Boolean> recheckUserPassword(String password);
	Future<Boolean> login(Credentials credentials);
}
