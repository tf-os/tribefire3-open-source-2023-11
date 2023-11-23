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
package com.braintribe.gwt.security.mock.client;

import java.util.Arrays;
import java.util.HashSet;
import java.util.function.Supplier;

import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.security.client.AbstractSecurityService;
import com.braintribe.gwt.security.client.SecurityServiceException;
import com.braintribe.gwt.security.client.Session;
import com.braintribe.gwt.security.client.SessionScope;
import com.braintribe.model.securityservice.credentials.Credentials;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * This is a mock implementation of the SecurityService
 *
 */
public class MockSecurityService extends AbstractSecurityService {
	
	@Override
	public Future<Boolean> loginWithExistingSession(String username, String sessionId) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Future<Boolean> loginSSO() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void login(String username, String password,
			AsyncCallback<Session> asyncCallback) {
		if (session != null) {
			asyncCallback.onFailure(new SecurityServiceException("a session already exists"));
			return;
		}
		session = new Session(username, Long.toHexString(System.currentTimeMillis()));
		session.setRoles(new HashSet<String>(Arrays.asList("admin")));
		onSessionCreated();
		asyncCallback.onSuccess(session);
	}
	
	@Override
	public Future<Boolean> login(Credentials credentials) {
		Future<Boolean> future = new Future<Boolean>();
		future.onFailure(new UnsupportedOperationException());
		return future;
	}

	@Override
	public void logout(AsyncCallback<Boolean> asyncCallback) {
		logout(false, asyncCallback);
	}
	
	@Override
	public void logout(boolean silent, AsyncCallback<Boolean> asyncCallback) {
		session = null;
		asyncCallback.onSuccess(true);
		onSessionClosed(silent);
	}
	
	protected void onSessionCreated() {
		createSessionScope();
		fireSessionCreated();
	}
	
	protected void onSessionClosed(boolean silent) {
		if (!silent)
			fireSessionClosed();
		removeSessionScope();
	}
		
	private void createSessionScope() {
		SessionScope scope = new SessionScope();
		SessionScope.scopeManager.openAndPushScope(scope);
	}

	private void removeSessionScope() {
		SessionScope.scopeManager.closeAndPopScope();
	}
	
	public Supplier<String> getSessionProvider() {
		return new Supplier<String>() {
			@Override
			public String get() throws RuntimeException {
				return session.getId();
			}
		};
	}

	@Override
	public Future<Boolean> changePassword(String oldPassword, String newPassword) {
		return new Future<Boolean>(true);
	}
	
	@Override
	public Future<Void> loginWithExistingSession(Session session) {
		Future<Void> future = new Future<Void>();
		future.onFailure(new UnsupportedOperationException());
		return future;
	}
	
	@Override
	public Future<Boolean> recheckUserPassword(String password) {
		return new Future<Boolean>(true);
	}
}
