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

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.security.reason.SessionNotFound;
import com.braintribe.model.processing.securityservice.api.DeletedSessionInfo;
import com.braintribe.model.processing.securityservice.api.UserSessionService;
import com.braintribe.model.user.User;
import com.braintribe.model.usersession.UserSession;

/**
 * <p>
 * Tests on {@link UserSessionService} method(s) used for {@link UserSession} deletion.
 * 
 * <ul>
 * <li>{@link UserSessionService#deleteUserSession(String)}
 * </ul>
 * 
 */
public abstract class UserSessionDeletionTest extends UserSessionServiceTestBase {

	@Test
	public void testDeletion_NullSessionId() throws Exception {
		Maybe<DeletedSessionInfo> session = userSessionService.deleteUserSession(null);
		Assert.assertTrue(session.isUnsatisfiedBy(SessionNotFound.T));
	}

	@Test
	public void testDeletion() throws Exception {
		User user1 = getUser(UserConfig.user);
		User user2 = getUser(UserConfig.userWithRoles);
		User user3 = getUser(UserConfig.userWithGroups);

		UserSession userSession1 = userSessionService.createUserSession(user1, null, null, null, null, null, null, null, false).get();
		UserSession userSession2 = userSessionService.createUserSession(user2, null, null, null, null, null, null, null, false).get();
		UserSession userSession3 = userSessionService.createUserSession(user3, null, null, null, null, null, null, null, false).get();

		UserSession deletedUserSession1 = userSessionService.deleteUserSession(userSession1.getSessionId()).get().userSession();

		Assert.assertNotNull(deletedUserSession1);
		Assert.assertEquals(deletedUserSession1.getSessionId(), userSession1.getSessionId());

		Assert.assertTrue(userSessionService.findUserSession(userSession1.getSessionId()).isUnsatisfiedBy(SessionNotFound.T));
		Assert.assertNotNull(userSessionService.findUserSession(userSession2.getSessionId()).get());
		Assert.assertNotNull(userSessionService.findUserSession(userSession3.getSessionId()).get());

		UserSession deletedUserSession2 = userSessionService.deleteUserSession(userSession2.getSessionId()).get().userSession();

		Assert.assertNotNull(deletedUserSession2);
		Assert.assertEquals(deletedUserSession2.getSessionId(), userSession2.getSessionId());

		Assertions.assertThat(userSessionService.findUserSession(userSession2.getSessionId()).isUnsatisfiedBy(SessionNotFound.T)).isTrue();
		Assertions.assertThat(userSessionService.findUserSession(userSession3.getSessionId()).isSatisfied()).isTrue();

		UserSession deletedUserSession3 = userSessionService.deleteUserSession(userSession3.getSessionId()).get().userSession();

		Assert.assertNotNull(deletedUserSession3);
		Assert.assertEquals(deletedUserSession3.getSessionId(), userSession3.getSessionId());

		Assert.assertTrue(userSessionService.findUserSession(userSession3.getSessionId()).isUnsatisfiedBy(SessionNotFound.T));
	}

}
