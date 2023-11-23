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
 * Tests for the
 * {@link com.braintribe.model.processing.securityservice.basic.SecurityServiceProcessor#isSessionValid(String)} "touch"
 * behaviour
 *
 */
public class SessionTouchTestBase extends SecurityServiceTest {

	/**
	 * Targets:
	 * {@link com.braintribe.model.processing.securityservice.basic.SecurityServiceProcessor#isSessionValid(String)}
	 * 
	 * <p>
	 * Input: sessions ids referencing valid existent {@link UserSession} objects
	 * 
	 * <p>
	 * Assertions:
	 * <ul>
	 * <li>{@link UserSession#getLastAccessedDate()} must be updated to a most present date
	 * <li>No exceptions shall be thrown
	 * </ul>
	 */
	@Test
	public void testSessionTouch() throws Exception {

		if (!testConfig.getEnableExpiration()) {
			System.out.println("suppressed test as testConfig.getEnableExpiration() is " + testConfig.getEnableExpiration());
			return;
		}

		String userSessionId = openSession().getSessionId();

		Thread.sleep(19000);

		Assert.assertTrue("session " + userSessionId + " should had been considered valid.", isValidSession(userSessionId));

		Thread.sleep(19000);

		Assert.assertTrue("session " + userSessionId + " should had been considered valid again.", isValidSession(userSessionId));

		Thread.sleep(21000);

		Assert.assertFalse("session " + userSessionId + " should had been considered invalid. ", isValidSession(userSessionId));

	}

	/**
	 * Targets:
	 * {@link com.braintribe.model.processing.securityservice.basic.SecurityServiceProcessor#isSessionValid(String)}
	 * 
	 * <p>
	 * Input: sessions ids referencing invalid existent {@link UserSession} objects
	 * 
	 * <p>
	 * Assertions:
	 * <ul>
	 * <li>{@link UserSession#getLastAccessedDate()} must remain unaltered
	 * <li>No exceptions shall be thrown
	 * </ul>
	 */
	@Test
	public void testInvalidSessionTouch() throws Exception {

		if (!testConfig.getEnableExpiration()) {
			System.out.println("suppressed test as testConfig.getEnableExpiration() is " + testConfig.getEnableExpiration());
			return;
		}

		UserSession userSession = openSession();

		logout(userSession.getSessionId());

		Thread.sleep(19000);

		Assert.assertFalse("session " + userSession + " should had been considered invalid.", isValidSession(userSession.getAccessId()));
	}

}
