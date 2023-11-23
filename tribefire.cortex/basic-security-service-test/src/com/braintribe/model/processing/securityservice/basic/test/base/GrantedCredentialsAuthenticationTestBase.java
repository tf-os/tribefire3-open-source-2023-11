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

import com.braintribe.gm.model.security.reason.AuthenticationFailure;
import com.braintribe.gm.model.security.reason.InvalidCredentials;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.securityservice.OpenUserSession;
import com.braintribe.model.securityservice.OpenUserSessionResponse;
import com.braintribe.model.securityservice.credentials.Credentials;
import com.braintribe.model.securityservice.credentials.GrantedCredentials;
import com.braintribe.model.securityservice.credentials.identification.EmailIdentification;
import com.braintribe.model.securityservice.credentials.identification.UserIdentification;
import com.braintribe.model.securityservice.credentials.identification.UserNameIdentification;

/**
 * {@link com.braintribe.model.processing.securityservice.basic.SecurityServiceProcessor#openUserSession(OpenUserSession)}
 * tests based on {@link GrantedCredentials}
 * 
 */
public abstract class GrantedCredentialsAuthenticationTestBase extends SecurityServiceTest {

	protected abstract Credentials getValidGrantingCredentials();

	@Test
	public void testGrantingToUserName() throws Exception {

		Credentials grantingCredentials = getValidGrantingCredentials();

		testGrantingToUserNameIdentification(grantingCredentials);
	}

	@Test
	public void testGrantingToNullUserName() throws Exception {

		Credentials grantingCredentials = getValidGrantingCredentials();

		testFailedGrantingToUserNameIdentification(null, grantingCredentials, "null user name identification", InvalidCredentials.T);

	}

	@Test
	public void testGrantingToEmptyUserName() throws Exception {

		Credentials grantingCredentials = getValidGrantingCredentials();

		testFailedGrantingToUserNameIdentification("", grantingCredentials, "empty user name identification", InvalidCredentials.T);

	}

	@Test
	public void testGrantingToInexistentUserName() throws Exception {

		Credentials grantingCredentials = getValidGrantingCredentials();

		testFailedGrantingToUserNameIdentification("john.smith.", grantingCredentials, "inexistent user name identification", InvalidCredentials.T);

	}

	@Test
	public void testGrantingToEmail() throws Exception {

		Credentials grantingCredentials = getValidGrantingCredentials();

		testGrantingToEmailIdentification(grantingCredentials);

	}

	@Test
	public void testGrantingToNullEmail() throws Exception {

		Credentials grantingCredentials = getValidGrantingCredentials();

		testFailedGrantingToEmailIdentification(null, grantingCredentials, "null e-mail identification", InvalidCredentials.T);

	}

	@Test
	public void testGrantingToEmptyEmail() throws Exception {

		Credentials grantingCredentials = getValidGrantingCredentials();

		testFailedGrantingToUserNameIdentification("", grantingCredentials, "empty e-mail identification", InvalidCredentials.T);

	}

	@Test
	public void testGrantingToInexistentEmail() throws Exception {

		Credentials grantingCredentials = getValidGrantingCredentials();

		testFailedGrantingToUserNameIdentification("john.smith@braintribe.com.", grantingCredentials, "inexistent e-mail identification",
				InvalidCredentials.T);

	}

	@Test
	public void testNullIdentification() {

		Credentials credentials = createGrantedCredentials(null, getValidGrantingCredentials());

		OpenUserSession openUserSession = createOpenUserSession(credentials);

		testFailedAuthenticationExpectingReason(openUserSession, "null identification", InvalidCredentials.T);

	}

	protected void testGrantingToUserNameIdentification(Credentials grantingCredentials) throws Exception {

		UserNameIdentification userNameIdentification = createUserNameIdentification("john.smith");

		testGrantedCredentials(userNameIdentification, grantingCredentials);

	}

	protected void testGrantingToEmailIdentification(Credentials grantingCredentials) throws Exception {

		EmailIdentification emailIdentification = createEmailIdentification("john.smith@braintribe.com");

		testGrantedCredentials(emailIdentification, grantingCredentials);

	}

	protected OpenUserSessionResponse testGrantedCredentials(UserIdentification userIdentification, Credentials grantingCredentials)
			throws Exception {

		GrantedCredentials grantedCredentials = createGrantedCredentials(userIdentification, grantingCredentials);

		OpenUserSession authReq = createOpenUserSession(grantedCredentials);

		return testSuccessfulAuthentication(authReq);

	}

	protected void testFailedGrantingToUserNameIdentification(String userName, Credentials grantingCredentials, String context,
			EntityType<? extends AuthenticationFailure> expectedReason) throws Exception {

		UserNameIdentification userNameIdentification = createUserNameIdentification(userName);

		testFailedGrantedCredentials(userNameIdentification, grantingCredentials, context, expectedReason);

	}

	protected void testFailedGrantingToEmailIdentification(String email, Credentials grantingCredentials, String context,
			EntityType<? extends AuthenticationFailure> expectedReason) throws Exception {

		EmailIdentification emailIdentification = createEmailIdentification(email);

		testFailedGrantedCredentials(emailIdentification, grantingCredentials, context, expectedReason);

	}

	protected void testFailedGrantedCredentials(UserIdentification userIdentification, Credentials grantingCredentials, String context,
			EntityType<? extends AuthenticationFailure> expectedReason) throws Exception {

		GrantedCredentials grantedCredentials = createGrantedCredentials(userIdentification, grantingCredentials);

		OpenUserSession authReq = createOpenUserSession(grantedCredentials);

		testFailedAuthenticationExpectingReason(authReq, context, expectedReason);

	}

	protected GrantedCredentials createGrantedCredentials(UserIdentification userIdentification, Credentials grantingCredentials) {
		GrantedCredentials grantedCredentials = GrantedCredentials.T.create();
		grantedCredentials.setUserIdentification(userIdentification);
		grantedCredentials.setGrantingCredentials(grantingCredentials);
		return grantedCredentials;
	}

}
