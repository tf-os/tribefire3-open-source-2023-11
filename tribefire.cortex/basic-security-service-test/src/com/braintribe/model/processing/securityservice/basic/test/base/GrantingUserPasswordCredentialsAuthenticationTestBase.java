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
import com.braintribe.model.securityservice.OpenUserSession;
import com.braintribe.model.securityservice.credentials.Credentials;
import com.braintribe.model.securityservice.credentials.GrantedCredentials;
import com.braintribe.model.securityservice.credentials.UserPasswordCredentials;

/**
 * {@link com.braintribe.model.processing.securityservice.basic.SecurityServiceProcessor#openUserSession(OpenUserSession)}
 * tests based on {@link GrantedCredentials} with {@link UserPasswordCredentials} as granting credentials.
 * 
 */
public class GrantingUserPasswordCredentialsAuthenticationTestBase extends GrantedCredentialsAuthenticationTestBase {

	@Test
	public void testGrantingToUserNameNullUserName() throws Exception {

		Credentials grantingCredentials = createUserNamePasswordCredentials(null, "cortex");

		testFailedGrantingToUserNameIdentification("john.smith", grantingCredentials, "null granting user name", InvalidCredentials.T);

	}

	@Test
	public void testGrantingToUserNameEmptyUserName() throws Exception {

		Credentials grantingCredentials = createUserNamePasswordCredentials("", "cortex");

		testFailedGrantingToUserNameIdentification("john.smith", grantingCredentials, "empty granting user name", InvalidCredentials.T);

	}

	@Test
	public void testGrantingToUserNameInexistentUserName() throws Exception {

		Credentials grantingCredentials = createUserNamePasswordCredentials("cortex.", "cortex");

		testFailedGrantingToUserNameIdentification("john.smith", grantingCredentials, "inexistent granting user name", InvalidCredentials.T);

	}

	@Test
	public void testGrantingToUserNameNullPassword() throws Exception {

		Credentials grantingCredentials = createUserNamePasswordCredentials("cortex", null);

		testFailedGrantingToUserNameIdentification("john.smith", grantingCredentials, "null granting password", InvalidCredentials.T);

	}

	@Test
	public void testGrantingToUserNameEmptyPassword() throws Exception {

		Credentials grantingCredentials = createUserNamePasswordCredentials("cortex", "");

		testFailedGrantingToUserNameIdentification("john.smith", grantingCredentials, "empty granting password", InvalidCredentials.T);

	}

	@Test
	public void testGrantingToUserNameWrongPassword() throws Exception {

		Credentials grantingCredentials = createUserNamePasswordCredentials("cortex", "cortex.");

		testFailedGrantingToUserNameIdentification("john.smith", grantingCredentials, "wrong granting password", InvalidCredentials.T);

	}

	@Test
	public void testGrantingToEmailNullUserName() throws Exception {

		Credentials grantingCredentials = createUserNamePasswordCredentials(null, "cortex");

		testFailedGrantingToEmailIdentification("john.smith@braintribe.com", grantingCredentials, "null granting user name", InvalidCredentials.T);

	}

	@Test
	public void testGrantingToEmailEmptyUserName() throws Exception {

		Credentials grantingCredentials = createUserNamePasswordCredentials("", "cortex");

		testFailedGrantingToEmailIdentification("john.smith@braintribe.com", grantingCredentials, "empty granting user name", InvalidCredentials.T);

	}

	@Test
	public void testGrantingToEmailInexistentUserName() throws Exception {

		Credentials grantingCredentials = createUserNamePasswordCredentials("cortex.", "cortex");

		testFailedGrantingToEmailIdentification("john.smith@braintribe.com", grantingCredentials, "inexistent granting user name",
				InvalidCredentials.T);

	}

	@Test
	public void testGrantingToEmailNullPassword() throws Exception {

		Credentials grantingCredentials = createUserNamePasswordCredentials("cortex", null);

		testFailedGrantingToEmailIdentification("john.smith@braintribe.com", grantingCredentials, "null granting password", InvalidCredentials.T);

	}

	@Test
	public void testGrantingToEmailEmptyPassword() throws Exception {

		Credentials grantingCredentials = createUserNamePasswordCredentials("cortex", "");

		testFailedGrantingToEmailIdentification("john.smith@braintribe.com", grantingCredentials, "empty granting password", InvalidCredentials.T);

	}

	@Test
	public void testGrantingToEmailWrongPassword() throws Exception {

		Credentials grantingCredentials = createUserNamePasswordCredentials("cortex", "cortex.");

		testFailedGrantingToEmailIdentification("john.smith@braintribe.com", grantingCredentials, "wrong granting password", InvalidCredentials.T);

	}

	@Override
	protected Credentials getValidGrantingCredentials() {

		UserPasswordCredentials grantingUserPasswordCredentials = UserPasswordCredentials.T.create();
		grantingUserPasswordCredentials.setUserIdentification(createUserNameIdentification("cortex"));
		grantingUserPasswordCredentials.setPassword("cortex");

		return grantingUserPasswordCredentials;

	}

	protected UserPasswordCredentials createUserNamePasswordCredentials(String userName, String password) {

		UserPasswordCredentials grantingUserPasswordCredentials = UserPasswordCredentials.T.create();
		grantingUserPasswordCredentials.setUserIdentification(createUserNameIdentification(userName));
		grantingUserPasswordCredentials.setPassword(password);

		return grantingUserPasswordCredentials;

	}

	protected UserPasswordCredentials createEmailPasswordCredentials(String email, String password) {

		UserPasswordCredentials grantingUserPasswordCredentials = UserPasswordCredentials.T.create();
		grantingUserPasswordCredentials.setUserIdentification(createEmailIdentification(email));
		grantingUserPasswordCredentials.setPassword(password);

		return grantingUserPasswordCredentials;

	}

}
