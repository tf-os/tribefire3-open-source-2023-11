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

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.model.processing.securityservice.api.UserSessionService;
import com.braintribe.model.time.TimeSpan;
import com.braintribe.model.time.TimeUnit;
import com.braintribe.model.user.User;
import com.braintribe.model.usersession.UserSession;
import com.braintribe.model.usersession.UserSessionType;

/**
 * <p>
 * Tests on {@link UserSessionService} method(s) used for {@link UserSession} touch.
 * 
 * <ul>
 * <li>{@link UserSessionService#touchUserSession(String)}
 * </ul>
 * 
 */
public abstract class UserSessionTouchingTest extends UserSessionServiceTestBase {

	@Test
	public void testTouchUserSession() throws Exception {
		User user1 = getUser(UserConfig.user);
		User user2 = getUser(UserConfig.userWithRoles);

		TimeSpan maxIdleTime = TimeSpan.T.create();
		maxIdleTime.setValue(1);
		maxIdleTime.setUnit(TimeUnit.day);

		UserSession userSession1 = userSessionService
				.createUserSession(user1, UserSessionType.normal, maxIdleTime, null, null, defaultInternetAddress, defaultProperties, null, false)
				.get();
		UserSession userSession2 = userSessionService
				.createUserSession(user2, UserSessionType.internal, maxIdleTime, null, null, defaultInternetAddress, defaultProperties, null, false)
				.get();

		Thread.sleep(10);

		Date lastAccessDate = new Date();
		Date expiryDate = new Date(lastAccessDate.getTime() + maxIdleTime.toLongMillies());

		userSessionService.touchUserSession(userSession1.getSessionId(), lastAccessDate, expiryDate);
		userSessionService.touchUserSession(userSession2.getSessionId(), lastAccessDate, expiryDate);

		UserSession foundUserSession1 = userSessionService.findUserSession(userSession1.getSessionId()).get();
		UserSession foundUserSession2 = userSessionService.findUserSession(userSession2.getSessionId()).get();

		Assert.assertTrue(userSession1.getLastAccessedDate().before(foundUserSession1.getLastAccessedDate()));
		Assert.assertTrue(userSession1.getExpiryDate().before(foundUserSession1.getExpiryDate()));
		Assert.assertTrue(userSession2.getLastAccessedDate().before(foundUserSession2.getLastAccessedDate()));
		Assert.assertTrue(userSession2.getExpiryDate().before(foundUserSession2.getExpiryDate()));
	}

}
