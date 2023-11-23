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
package com.braintribe.model.processing.securityservice.commons.provider;

import java.util.function.Supplier;

import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.usersession.UserSession;


/**
 * Abstraction for {@link Supplier}(s) delegating to a {@code Provider<UserSession>}.
 * 
 */
public abstract class UserSessionProviderDelegatingProvider<T extends UserSession> {

	private static Logger log = Logger.getLogger(UserSessionProviderDelegatingProvider.class);

	protected Supplier<T> userSessionProvider;

	@Required
	public void setUserSessionProvider(Supplier<T> userSessionProvider) {
		this.userSessionProvider = userSessionProvider;
	}

	/**
	 * See {@link #provideUserSession(Logger)}
	 */
	protected T provideUserSession() throws RuntimeException {
		return provideUserSession(log);
	}

	/**
	 * <p>
	 * Provides a {@link UserSession} via {@link UserSessionProviderDelegatingProvider#userSessionProvider}.
	 * 
	 * <p>
	 * Meant to reduce redundant logging in {@link UserSessionProviderDelegatingProvider} sub classes, which still could access {@link #userSessionProvider} directly if needed.
	 * 
	 * @return The {@link UserSession} provided by {@link UserSessionProviderDelegatingProvider#userSessionProvider}.
	 */
	protected T provideUserSession(Logger callerLogger) throws RuntimeException {
		T userSession = userSessionProvider.get();
		if (callerLogger.isTraceEnabled()) {
			callerLogger.trace("Provider ["+userSessionProvider+"] provided user session ["+userSession+"]");
		}
		return userSession;
	}

}
