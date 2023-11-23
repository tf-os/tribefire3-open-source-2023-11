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
package com.braintribe.model.processing.securityservice.basic.test.base;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.model.usersession.UserSession;

/**
 * Tests for {@link com.braintribe.model.processing.securityservice.basic.SecurityServiceProcessor#logout(String)}
 *
 */
public class SessionLogoutTestBase extends SecurityServiceTest {

	@Test
	public void testLogout() throws Exception {

		UserSession userSession = openSession();

		testLogoutValidSession(userSession);

	}

	/**
	 * Targets: {@link com.braintribe.model.processing.securityservice.basic.SecurityServiceProcessor#logout(String)}
	 * 
	 * <p>
	 * Input: sessions ids referencing valid existent {@link UserSession} objects
	 * 
	 * <p>
	 * Assertions:
	 * <ul>
	 * <li>session id must be considered invalid by
	 * {@link com.braintribe.model.processing.securityservice.basic.SecurityServiceProcessor#isSessionValid(String)}
	 * <li>No exceptions shall be thrown
	 * </ul>
	 */
	@Test
	public void testLogoutValidSession() throws Exception {
		testLogoutValidSession(openSession());
		testLogoutValidSession(openSession());
		testLogoutValidSession(openSession());
		testLogoutValidSession(openSession());
		testLogoutValidSession(openSession());
	}

	/**
	 * Targets: {@link com.braintribe.model.processing.securityservice.basic.SecurityServiceProcessor#logout(String)}
	 * 
	 * <p>
	 * Input: sessions ids referencing invalid or inexistent {@link UserSession} objects
	 * 
	 * <p>
	 * Assertions:
	 * <ul>
	 * <li>session id must be remain invalid by
	 * {@link com.braintribe.model.processing.securityservice.basic.SecurityServiceProcessor#isSessionValid(String)}
	 * <li>No exceptions shall be thrown
	 * </ul>
	 */
	@Test
	public void testLogoutInvalidSession() {
		testLogoutInvalidSession(null);
		testLogoutInvalidSession(empty);
		testLogoutInvalidSession("  \r\t  ");
		testLogoutInvalidSession(random);
		testLogoutInvalidSession(generateRandomString());
		testLogoutInvalidSession(generateRandomString());
		testLogoutInvalidSession(generateRandomString());
		testLogoutInvalidSession(generateRandomString());
		testLogoutInvalidSession(generateRandomString());
	}

	private void testLogoutValidSession(UserSession validUserSession) {
		try {

			Assert.assertTrue(validUserSession + " should be valid right after creation", isValidSession(validUserSession.getSessionId()));

			Assert.assertTrue(validUserSession + " should had been successfully logged out", logout(validUserSession.getSessionId()));

			assertUserSessionLoggedOut(validUserSession.getSessionId());

		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("unexpected exception while logging out " + validUserSession + ": " + e.getMessage());
		}
	}

	private void testLogoutInvalidSession(String invalidSessionId) {
		try {

			Assert.assertFalse("session " + invalidSessionId + " should be initially invalid", isValidSession(invalidSessionId));

			Assert.assertFalse("logging out invalid session " + invalidSessionId + " should have returned true", logout(invalidSessionId));

			assertUserSessionLoggedOut(invalidSessionId);

		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("unexpected exception while logging out already invalid session " + invalidSessionId + ": " + e.getMessage());
		}
	}

}
