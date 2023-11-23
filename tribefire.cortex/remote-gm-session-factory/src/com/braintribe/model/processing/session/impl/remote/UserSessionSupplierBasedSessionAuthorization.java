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
package com.braintribe.model.processing.session.impl.remote;

import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import com.braintribe.model.processing.session.api.persistence.auth.SessionAuthorization;
import com.braintribe.model.usersession.UserSession;

/**
 * @author peter.gazdik
 */
public class UserSessionSupplierBasedSessionAuthorization implements SessionAuthorization {

	private final Supplier<UserSession> userSessionSuplier;

	public UserSessionSupplierBasedSessionAuthorization(Supplier<UserSession> userSessionSuplier) {
		Objects.requireNonNull(userSessionSuplier, "userSessionSuplier must not be null");

		this.userSessionSuplier = userSessionSuplier;
	}

	@Override
	public Set<String> getUserRoles() {
		UserSession userSession = userSessionSuplier.get();
		return userSession != null ? userSession.getEffectiveRoles() : null;
	}

	@Override
	public String getUserId() {
		UserSession userSession = userSessionSuplier.get();
		return userSession != null ? userSession.getUser().getId() : null;
	}

	@Override
	public String getUserName() {
		UserSession userSession = userSessionSuplier.get();
		return userSession != null ? userSession.getUser().getName() : null;
	}

	@Override
	public String getSessionId() {
		UserSession userSession = userSessionSuplier.get();
		return userSession != null ? userSession.getSessionId() : null;
	}

}
