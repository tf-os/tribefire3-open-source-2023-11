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

import org.junit.Test;

import com.braintribe.gm.model.security.reason.InvalidCredentials;
import com.braintribe.gm.model.security.reason.SessionExpired;
import com.braintribe.model.securityservice.OpenUserSession;
import com.braintribe.model.securityservice.credentials.Credentials;
import com.braintribe.model.securityservice.credentials.ExistingSessionCredentials;
import com.braintribe.model.securityservice.credentials.GrantedCredentials;
import com.braintribe.model.securityservice.credentials.identification.UserIdentification;
import com.braintribe.model.usersession.UserSession;

/**
 * {@link com.braintribe.model.processing.securityservice.basic.SecurityServiceProcessor#openUserSession(OpenUserSession)}
 * tests based on {@link GrantedCredentials} with {@link ExistingSessionCredentials} as granting credentials.
 * 
 */
public class GrantingExistingSessionCredentialsAuthenticationTestBase extends GrantedCredentialsAuthenticationTestBase {

	@Test
	public void testGrantingToUserNameNullSessionId() throws Exception {

		ExistingSessionCredentials grantingExistingSessionCredentials = ExistingSessionCredentials.T.create();
		grantingExistingSessionCredentials.setExistingSessionId(null);

		testFailedGrantingToUserNameIdentification("john.smith", grantingExistingSessionCredentials, "null session id", InvalidCredentials.T);

	}

	@Test
	public void testGrantingToUserNameInexistentSessionId() throws Exception {

		UserSession grantingSession = openSession();

		ExistingSessionCredentials grantingExistingSessionCredentials = ExistingSessionCredentials.T.create();
		grantingExistingSessionCredentials.setExistingSessionId(grantingSession.getSessionId().substring(1));

		testFailedGrantingToUserNameIdentification("john.smith", grantingExistingSessionCredentials, "inexistent session id", InvalidCredentials.T);

	}

	@Test
	public void testGrantingToUserNameInvalidatedSessionId() throws Exception {

		UserSession grantingSession = openSession();

		logout(grantingSession.getSessionId());

		ExistingSessionCredentials grantingExistingSessionCredentials = ExistingSessionCredentials.T.create();
		grantingExistingSessionCredentials.setExistingSessionId(grantingSession.getSessionId());

		testFailedGrantingToUserNameIdentification("john.smith", grantingExistingSessionCredentials, "invalidated session id", InvalidCredentials.T);

	}

	@Test
	public void testGrantingToUserNameExpiredSessionId() throws Exception {
		testExpiredGrantingSession(createUserNameIdentification("john.smith"));
	}

	@Test
	public void testGrantingToEmailNullSessionId() throws Exception {

		ExistingSessionCredentials grantingExistingSessionCredentials = ExistingSessionCredentials.T.create();
		grantingExistingSessionCredentials.setExistingSessionId(null);

		testFailedGrantingToEmailIdentification("john.smith@braintribe.com", grantingExistingSessionCredentials, "null session id",
				InvalidCredentials.T);

	}

	@Test
	public void testGrantingToEmailInexistentSessionId() throws Exception {

		UserSession grantingSession = openSession();

		ExistingSessionCredentials grantingExistingSessionCredentials = ExistingSessionCredentials.T.create();
		grantingExistingSessionCredentials.setExistingSessionId(grantingSession.getSessionId().substring(1));

		testFailedGrantingToEmailIdentification("john.smith@braintribe.com", grantingExistingSessionCredentials, "inexistent session id",
				InvalidCredentials.T);

	}

	@Test
	public void testGrantingToEmailInvalidatedSessionId() throws Exception {

		UserSession grantingSession = openSession();

		logout(grantingSession.getSessionId());

		ExistingSessionCredentials grantingExistingSessionCredentials = ExistingSessionCredentials.T.create();
		grantingExistingSessionCredentials.setExistingSessionId(grantingSession.getSessionId());

		testFailedGrantingToEmailIdentification("john.smith@braintribe.com", grantingExistingSessionCredentials, "invalidated session id",
				InvalidCredentials.T);

	}

	@Test
	public void testGrantingToEmailExpiredSessionId() throws Exception {
		testExpiredGrantingSession(createEmailIdentification("john.smith@braintribe.com"));
	}

	protected void testExpiredGrantingSession(UserIdentification identification) throws Exception {

		if (!testConfig.getEnableExpiration()) {
			System.out.println("suppressed test as testConfig.getEnableExpiration() is " + testConfig.getEnableExpiration());
			return;
		}

		String context = "expired granting session id";

		UserSession grantingSession1 = openSession();
		UserSession grantingSession2 = openSession();

		Thread.sleep(21000);

		ExistingSessionCredentials grantingExistingSessionCredentials1 = ExistingSessionCredentials.T.create();
		ExistingSessionCredentials grantingExistingSessionCredentials2 = ExistingSessionCredentials.T.create();

		grantingExistingSessionCredentials1.setExistingSessionId(grantingSession1.getSessionId());
		grantingExistingSessionCredentials2.setExistingSessionId(grantingSession2.getSessionId());

		GrantedCredentials grantedCredentials1 = createGrantedCredentials(identification, grantingExistingSessionCredentials1);
		GrantedCredentials grantedCredentials2 = createGrantedCredentials(identification, grantingExistingSessionCredentials2);

		OpenUserSession authReq1 = createOpenUserSession(grantedCredentials1);
		OpenUserSession authReq2 = createOpenUserSession(grantedCredentials2);

		testFailedAuthenticationExpectingReason(authReq1, context, SessionExpired.T);
		testFailedAuthenticationExpectingReason(authReq2, context, SessionExpired.T);
	}

	@Override
	protected Credentials getValidGrantingCredentials() {

		UserSession grantingSession = openSession();

		ExistingSessionCredentials grantingExistingSessionCredentials = ExistingSessionCredentials.T.create();
		grantingExistingSessionCredentials.setExistingSessionId(grantingSession.getSessionId());

		return grantingExistingSessionCredentials;

	}

}
