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
package com.braintribe.model.processing.securityservice.commons.service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.security.reason.InvalidCredentials;
import com.braintribe.gm.model.security.reason.SessionNotFound;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.impl.AbstractDispatchingServiceProcessor;
import com.braintribe.model.processing.service.impl.DispatchConfiguration;
import com.braintribe.model.securityservice.Logout;
import com.braintribe.model.securityservice.LogoutSession;
import com.braintribe.model.securityservice.OpenUserSession;
import com.braintribe.model.securityservice.OpenUserSessionResponse;
import com.braintribe.model.securityservice.SecurityRequest;
import com.braintribe.model.securityservice.ValidateUserSession;
import com.braintribe.model.securityservice.credentials.AbstractUserIdentificationCredentials;
import com.braintribe.model.securityservice.credentials.Credentials;
import com.braintribe.model.securityservice.credentials.identification.UserIdentification;
import com.braintribe.model.securityservice.credentials.identification.UserNameIdentification;
import com.braintribe.model.user.Role;
import com.braintribe.model.user.User;
import com.braintribe.model.usersession.UserSession;
import com.braintribe.model.usersession.UserSessionType;

public class InMemorySecurityServiceProcessor extends AbstractDispatchingServiceProcessor<SecurityRequest, Object> {
	private final Map<String, UserSession> sessions = new ConcurrentHashMap<>();

	private final Map<String, User> users = new HashMap<>();

	private final boolean identifyById = false;

	public void addUser(User user) {
		users.put(getIdentification(user), user);
	}

	private String getIdentification(User user) {
		return identifyById ? user.getId() : user.getName();
	}

	@Override
	protected void configureDispatching(DispatchConfiguration<SecurityRequest, Object> dispatching) {
		dispatching.registerReasoned(OpenUserSession.T, (c, r) -> openUserSession(c, r));
		dispatching.registerReasoned(ValidateUserSession.T, (c, r) -> validateUserSession(r));
		dispatching.register(Logout.T, this::logout);
		dispatching.register(LogoutSession.T, this::logoutSession);
	}

	@SuppressWarnings("unused")
	private Maybe<OpenUserSessionResponse> openUserSession(ServiceRequestContext requestContext, OpenUserSession request) {

		Credentials credentials = request.getCredentials();

		Objects.requireNonNull(credentials, "Missing credentials on OpenUserSession");

		if (!(credentials instanceof AbstractUserIdentificationCredentials)) {
			return Reasons.build(InvalidCredentials.T).text("Unsupported credential type: " + credentials.entityType().getTypeSignature()).toMaybe();
		}

		AbstractUserIdentificationCredentials upwdCredentials = (AbstractUserIdentificationCredentials) credentials;

		UserIdentification userIdentification = upwdCredentials.getUserIdentification();

		Objects.requireNonNull(credentials, "Missing userIdentification on UserPasswordCredentials");

		if (!(userIdentification instanceof UserNameIdentification)) {
			return Reasons.build(InvalidCredentials.T).text("Unsupported UserIdentification " + userIdentification.type().getTypeSignature())
					.toMaybe();
		}

		UserNameIdentification userNameIdentification = (UserNameIdentification) userIdentification;

		String userName = userNameIdentification.getUserName();

		Objects.requireNonNull(credentials, "Missing userName on UserNameIdentification");

		User user = users.get(userName);

		if (user == null)
			return Reasons.build(InvalidCredentials.T).text("Authentication failed due to invalid credentials").toMaybe();

		String sessionId = UUID.randomUUID().toString();

		Set<String> effectiveRoles = Stream.concat(Stream.of(user), user.getGroups().stream()) //
				.flatMap(i -> i.getRoles().stream()) //
				.map(Role::getName) //
				.collect(Collectors.toSet()); //

		Date now = new Date();

		UserSession userSession = UserSession.T.create();
		userSession.setSessionId(sessionId);
		userSession.setUser(user);
		userSession.setEffectiveRoles(effectiveRoles);
		userSession.setCreationDate(now);
		userSession.setLastAccessedDate(now);
		userSession.setType(UserSessionType.normal);

		OpenUserSessionResponse response = OpenUserSessionResponse.T.create();
		response.setUserSession(userSession);

		sessions.put(sessionId, userSession);

		return Maybe.complete(response);
	}

	private Maybe<UserSession> validateUserSession(ValidateUserSession request) {
		UserSession userSession = sessions.get(request.getSessionId());

		if (userSession == null)
			return Reasons.build(SessionNotFound.T).text("Session is invalid: " + request.getSessionId()).toMaybe();

		synchronized (userSession) {
			userSession.setLastAccessedDate(new Date());
		}

		return Maybe.complete(userSession);
	}

	private boolean logout(ServiceRequestContext context, Logout request) {
		String sessionId = context.getRequestorSessionId();
		if (sessionId == null)
			return false;

		UserSession userSession = sessions.remove(sessionId);

		if (userSession == null)
			return false;

		return true;
	}

	private boolean logoutSession(ServiceRequestContext context, LogoutSession request) {
		String sessionId = request.getSessionId();
		if (sessionId == null)
			return false;

		UserSession userSession = sessions.remove(sessionId);

		if (userSession == null)
			return false;

		return true;
	}

}