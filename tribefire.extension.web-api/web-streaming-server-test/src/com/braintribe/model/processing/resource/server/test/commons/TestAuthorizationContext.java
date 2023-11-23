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
package com.braintribe.model.processing.resource.server.test.commons;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.braintribe.model.usersession.UserSession;

public class TestAuthorizationContext implements Supplier<String>, Consumer<Throwable> {
	
	protected ThreadLocal<List<Throwable>> notifiedExceptions = new ThreadLocal<List<Throwable>>();
	protected Supplier<UserSession> userSessionProvider;
	private boolean invalidated;

	public void setUserSessionProvider(Supplier<UserSession> userSessionProvider) {
		this.userSessionProvider = userSessionProvider;
	}

	@Override
	public String get() throws RuntimeException {
		if (invalidated) {
			return "invalid";
		}
		UserSession userSession = provideUserSession();
		return (userSession != null) ? userSession.getSessionId() : null;
	}
	
	@Override
	public void accept(Throwable object) throws RuntimeException {

		if (notifiedExceptions.get() == null) {
			notifiedExceptions.set(new ArrayList<Throwable>());
		}

		notifiedExceptions.get().add(object);
		
		reset();

	}

	protected UserSession provideUserSession() throws RuntimeException {
		UserSession userSession = userSessionProvider.get();
		return userSession;
	}
	
	public List<Throwable> getNotifiedFailures() {
		return notifiedExceptions.get();
	}
	
	public void invalidate() {
		invalidated = true;
	}
	
	public void reset() {
		invalidated = false;
	}

}
