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

import com.braintribe.model.securityservice.credentials.ExistingSessionCredentials;
import com.braintribe.model.securityservice.credentials.TokenCredentials;
import com.braintribe.model.securityservice.credentials.UserPasswordCredentials;

import jsinterop.annotations.JsMethod;

public interface Credentials {
	@JsMethod(namespace = SecurityModuleInteropNamespaces.credentials)
	@SuppressWarnings("unusable-by-js")
	static UserPasswordCredentials userPassword(String user, String password) {
		return UserPasswordCredentials.forUserName(user, password);
	}

	@JsMethod(namespace = SecurityModuleInteropNamespaces.credentials)
	@SuppressWarnings("unusable-by-js")
	static ExistingSessionCredentials sessionId(String sessionId) {
		return ExistingSessionCredentials.of(sessionId);
	}

	@JsMethod(namespace = SecurityModuleInteropNamespaces.credentials)
	@SuppressWarnings("unusable-by-js")
	static TokenCredentials token(String type, String token) {
		throw new UnsupportedOperationException();
	}
}
