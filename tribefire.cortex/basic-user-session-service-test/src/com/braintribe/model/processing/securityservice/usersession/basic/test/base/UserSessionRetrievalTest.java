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
package com.braintribe.model.processing.securityservice.usersession.basic.test.base;

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.gm.model.security.reason.SessionNotFound;
import com.braintribe.model.processing.securityservice.api.UserSessionService;
import com.braintribe.model.user.User;
import com.braintribe.model.usersession.UserSession;
import com.braintribe.model.usersession.UserSessionType;

/**
 * <p>
 * Tests on {@link UserSessionService} method(s) used for {@link UserSession} retrieval.
 * 
 * <ul>
 * <li>{@link UserSessionService#findUserSession(String)}
 * <li>{@link UserSessionService#findTouchUserSession(String)}
 * </ul>
 * 
 */
public abstract class UserSessionRetrievalTest extends UserSessionServiceTestBase {

	@Test
	public void testFindUserSession_NullSessionId() throws Exception {
		Assert.assertTrue(userSessionService.findUserSession(null).isUnsatisfiedBy(SessionNotFound.T));
	}

	@Test
	public void testFindUserSession() throws Exception {
		User user1 = getUser(UserConfig.user);
		User user2 = getUser(UserConfig.userWithRoles);
		Set<String> expectedEffectiveRoles1 = getUserExpectedEffectiveRoles(UserConfig.user);
		Set<String> expectedEffectiveRoles2 = getUserExpectedEffectiveRoles(UserConfig.userWithRoles);

		UserSession userSession1 = userSessionService
				.createUserSession(user1, UserSessionType.normal, null, null, null, defaultInternetAddress, defaultProperties, null, false).get();
		UserSession userSession2 = userSessionService
				.createUserSession(user2, UserSessionType.internal, null, null, null, defaultInternetAddress, defaultProperties, null, false).get();

		UserSession foundUserSession1 = userSessionService.findUserSession(userSession1.getSessionId()).get();
		UserSession foundUserSession2 = userSessionService.findUserSession(userSession2.getSessionId()).get();

		assertUserSession(foundUserSession1, null, user1, UserSessionType.normal, defaultInternetAddress, expectedEffectiveRoles1, defaultProperties);
		assertUserSession(foundUserSession2, null, user2, UserSessionType.internal, defaultInternetAddress, expectedEffectiveRoles2,
				defaultProperties);
	}

}
