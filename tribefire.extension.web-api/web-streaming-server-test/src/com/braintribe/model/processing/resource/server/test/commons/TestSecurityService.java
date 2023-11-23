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

import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.security.reason.InvalidCredentials;
import com.braintribe.gm.model.security.reason.SessionNotFound;
import com.braintribe.model.processing.securityservice.api.exceptions.AuthenticationException;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.impl.AbstractDispatchingServiceProcessor;
import com.braintribe.model.processing.service.impl.DispatchConfiguration;
import com.braintribe.model.securityservice.Logout;
import com.braintribe.model.securityservice.LogoutSession;
import com.braintribe.model.securityservice.OpenUserSession;
import com.braintribe.model.securityservice.OpenUserSessionResponse;
import com.braintribe.model.securityservice.SecurityRequest;
import com.braintribe.model.securityservice.ValidateUserSession;
import com.braintribe.model.securityservice.credentials.Credentials;
import com.braintribe.model.securityservice.credentials.ExistingSessionCredentials;
import com.braintribe.model.user.User;
import com.braintribe.model.usersession.UserSession;
import com.braintribe.model.usersession.UserSessionType;

public class TestSecurityService extends AbstractDispatchingServiceProcessor<SecurityRequest, Object> {

	private final Map<String, UserSession> userSessions = new ConcurrentHashMap<String, UserSession>();
	private final User testUser = createTestUser();

	public TestSecurityService() {

		UserSession userSession = UserSession.T.create();
		userSession.setCreationDate(new Date());
		userSession.setSessionId("valid-session-id");
		userSession.setType(UserSessionType.normal);
		userSession.setUser(testUser);

		userSessions.put(userSession.getSessionId(), userSession);

	}

	@Override
	protected void configureDispatching(DispatchConfiguration<SecurityRequest, Object> dispatching) {
		dispatching.registerReasoned(OpenUserSession.T, (c, r) -> openUserSession(r));
		dispatching.register(Logout.T, (c, r) -> logout(c));
		dispatching.register(LogoutSession.T, (c, r) -> logoutSession(r));
		dispatching.registerReasoned(ValidateUserSession.T, (c, r) -> validateUserSession(r));
	}

	private Maybe<OpenUserSessionResponse> openUserSession(OpenUserSession request) throws AuthenticationException {

		Credentials credentials = request.getCredentials();

		UserSession userSession = null;

		if (credentials instanceof ExistingSessionCredentials) {

			String existingSessionId = ((ExistingSessionCredentials) credentials).getExistingSessionId();

			userSession = userSessions.get(existingSessionId);

			if (userSession == null) {
				String message = "Session id is invalid: " + existingSessionId;
				return Reasons.build(InvalidCredentials.T).text(message).toMaybe();
			}

		} else {

			userSession = UserSession.T.create();
			userSession.setCreationDate(new Date());
			userSession.setSessionId(UUID.randomUUID().toString());
			userSession.setType(UserSessionType.normal);
			userSession.setUser(testUser);

			userSessions.put(userSession.getSessionId(), userSession);

		}

		OpenUserSessionResponse response = OpenUserSessionResponse.T.create();
		response.setUserSession(userSession);

		return Maybe.complete(response);
	}

	private boolean logout(ServiceRequestContext context) {
		String sessionId = context.getRequestorSessionId();
		if (sessionId == null)
			return false;

		boolean loggedOut = userSessions.remove(sessionId) != null;
		return loggedOut;
	}

	private boolean logoutSession(LogoutSession request) {
		String sessionId = request.getSessionId();
		if (sessionId == null)
			return false;

		boolean loggedOut = userSessions.remove(sessionId) != null;
		return loggedOut;
	}

	private Maybe<UserSession> validateUserSession(ValidateUserSession request) {
		UserSession userSession = userSessions.get(request.getSessionId());

		if (userSession == null) {
			return Reasons.build(SessionNotFound.T).text("Session not found: " + request.getSessionId()).toMaybe();
		}

		return Maybe.complete(userSession);
	}

	private static User createTestUser() {
		User user = User.T.create();
		user.setId(UUID.randomUUID().toString());
		user.setName("test.user");
		user.setFirstName("Test");
		user.setLastName("User");
		return user;
	}

}
