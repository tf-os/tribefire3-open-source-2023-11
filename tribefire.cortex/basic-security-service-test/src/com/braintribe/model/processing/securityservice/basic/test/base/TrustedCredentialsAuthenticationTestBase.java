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
import com.braintribe.model.processing.securityservice.api.exceptions.AuthenticationException;
import com.braintribe.model.securityservice.OpenUserSession;
import com.braintribe.model.securityservice.OpenUserSessionResponse;
import com.braintribe.model.securityservice.credentials.TrustedCredentials;
import com.braintribe.model.securityservice.credentials.identification.EmailIdentification;
import com.braintribe.model.securityservice.credentials.identification.UserIdentification;
import com.braintribe.model.securityservice.credentials.identification.UserNameIdentification;
import com.braintribe.model.usersession.UserSession;

/**
 * {@link OpenUserSession} tests based on {@link TrustedCredentials}
 * 
 */
public class TrustedCredentialsAuthenticationTestBase extends SecurityServiceTest {

	@Test
	public void testUserNameIdentification() throws Exception {

		OpenUserSession authReq = createUserNameIdentificationAuthReq("john.smith");

		setTrusted(true);
		testSuccessfulAuthentication(authReq);
	}

	@Test
	public void testUserNameIdentificationFromUntrustedOrigin() throws Exception {

		OpenUserSession authReq = createUserNameIdentificationAuthReq("john.smith");

		setTrusted(false);

		String context = "from untrusted origin";

		testFailedAuthenticationExpectingReason(authReq, context, InvalidCredentials.T);

	}

	@Test
	public void testEmailIdentification() throws Exception {

		OpenUserSession authReq = createEmailIdentificationAuthReq("john.smith@braintribe.com");

		setTrusted(true);

		testSuccessfulAuthentication(authReq);

	}

	@Test
	public void testEmailIdentificationFromUntrustedOrigin() throws Exception {

		OpenUserSession authReq = createEmailIdentificationAuthReq("john.smith@braintribe.com");

		setTrusted(false);

		String context = "from untrusted origin";

		testFailedAuthenticationExpectingReason(authReq, context, InvalidCredentials.T);
	}

	@Test
	public void testInternalUserNameIdentificationFromUntrustedOrigin() throws Exception {

		OpenUserSession authReq = createUserNameIdentificationAuthReq("internal");

		setTrusted(false);

		String context = "from untrusted origin";

		testFailedAuthenticationExpectingReason(authReq, context, InvalidCredentials.T);

	}

	@Test
	public void testNullUserName() throws Exception {

		OpenUserSession authReq = createUserNameIdentificationAuthReq(null);

		setTrusted(true);

		String context = "from untrusted origin";

		testFailedAuthenticationExpectingReason(authReq, context, InvalidCredentials.T);

	}

	@Test
	public void testEmptyUserName() throws Exception {

		OpenUserSession authReq = createUserNameIdentificationAuthReq(empty);

		setTrusted(true);

		String context = "from untrusted origin";

		testFailedAuthenticationExpectingReason(authReq, context, InvalidCredentials.T);

	}

	@Test
	public void testInexistentUserName() throws Exception {

		OpenUserSession authReq = createUserNameIdentificationAuthReq(random);

		setTrusted(true);

		String context = "from untrusted origin";

		testFailedAuthenticationExpectingReason(authReq, context, InvalidCredentials.T);

	}

	@Test
	public void testNullEmail() throws Exception {

		OpenUserSession authReq = createEmailIdentificationAuthReq(null);

		setTrusted(true);

		String context = "from untrusted origin";

		testFailedAuthenticationExpectingReason(authReq, context, InvalidCredentials.T);

	}

	@Test
	public void testEmptyEmail() throws Exception {

		OpenUserSession authReq = createEmailIdentificationAuthReq(empty);

		setTrusted(true);

		String context = "from untrusted origin";

		testFailedAuthenticationExpectingReason(authReq, context, InvalidCredentials.T);
	}

	@Test
	public void testInexistentEmail() throws Exception {

		OpenUserSession authReq = createEmailIdentificationAuthReq(random);

		setTrusted(true);

		String context = "from untrusted origin";

		testFailedAuthenticationExpectingReason(authReq, context, InvalidCredentials.T);

	}

	/**
	 * Targets:
	 * {@link com.braintribe.model.processing.securityservice.basic.SecurityServiceProcessor#openUserSession(OpenUserSession)}
	 * 
	 * <p>
	 * Input: {@link TrustedCredentials} provided with valid {@link UserNameIdentification} accessible through
	 * {@link TrustedCredentials#getUserIdentification()}
	 * 
	 * <p>
	 * Assertions:
	 * <ul>
	 * <li>{@link OpenUserSessionResponse} shall be returned with a valid {@link UserSession} accessible through
	 * {@link OpenUserSessionResponse#getUserSession()}
	 * <li>{@link UserSession#getUser()} from {@link OpenUserSessionResponse#getUserSession()} must match the user
	 * identification provided by {@link TrustedCredentials#getUserIdentification()}
	 * <li>No exceptions shall be thrown
	 * </ul>
	 */
	@Test
	public void testSuccessfulUserNameIdentificationAuthentication() {

		setTrusted(true);

		testSuccessfulUserNameIdentAuthentication("john.smith");
		testSuccessfulUserNameIdentAuthentication("mary.williams");
		testSuccessfulUserNameIdentAuthentication("robert.taylor");
		testSuccessfulUserNameIdentAuthentication("steven.brown");
	}

	/**
	 * Targets:
	 * {@link com.braintribe.model.processing.securityservice.basic.SecurityServiceProcessor#openUserSession(OpenUserSession)}
	 * 
	 * <p>
	 * Input: {@link TrustedCredentials} provided with valid {@link EmailIdentification} accessible through
	 * {@link TrustedCredentials#getUserIdentification()}
	 * 
	 * <p>
	 * Assertions:
	 * <ul>
	 * <li>{@link OpenUserSessionResponse} shall be returned with a valid {@link UserSession} accessible through
	 * {@link OpenUserSessionResponse#getUserSession()}
	 * <li>{@link UserSession#getUser()} from {@link OpenUserSessionResponse#getUserSession()} must match the user
	 * identification provided by {@link TrustedCredentials#getUserIdentification()}
	 * <li>No exceptions shall be thrown
	 * </ul>
	 */
	@Test
	public void testSuccessfulEmailIdentificationAuthentication() {

		setTrusted(true);

		testSuccessfulEmailIdentAuthentication("john.smith@braintribe.com");
		testSuccessfulEmailIdentAuthentication("mary.williams@braintribe.com");
		testSuccessfulEmailIdentAuthentication("robert.taylor@braintribe.com");
		testSuccessfulEmailIdentAuthentication("steven.brown@braintribe.com");
	}

	/**
	 * Targets:
	 * {@link com.braintribe.model.processing.securityservice.basic.SecurityServiceProcessor#openUserSession(OpenUserSession)}
	 * 
	 * <p>
	 * Input: {@link TrustedCredentials} provided with invalid/inexistent {@link UserNameIdentification} accessible through
	 * {@link TrustedCredentials#getUserIdentification()}
	 * 
	 * <p>
	 * Assertions:
	 * <ul>
	 * <li>{@link AuthenticationException} exception shall be thrown</li>
	 * </ul>
	 */
	@Test
	public void testInexistentUserNameIdentificationAuthentication() {

		setTrusted(true);

		testInexistentUserNameIdentAuthentication("robert.taylor\t");
		testInexistentUserNameIdentAuthentication("\trobert.taylor\t");
		testInexistentUserNameIdentAuthentication("\trobert.taylor");
		testInexistentUserNameIdentAuthentication("robert.taylor2");
		testInexistentUserNameIdentAuthentication("ROBERT.TAYLOR");
		testInexistentUserNameIdentAuthentication("robert.taylor@braintribe.com");
	}

	@Test
	public void testInvalidUserNameIdentificationAuthentication() {

		setTrusted(true);

		testInvalidEmailIdentAuthentication(null);
		testInvalidEmailIdentAuthentication(empty);
	}

	/**
	 * Targets:
	 * {@link com.braintribe.model.processing.securityservice.basic.SecurityServiceProcessor#openUserSession(OpenUserSession)}
	 * 
	 * <p>
	 * Input: {@link TrustedCredentials} provided with invalid/inexistent {@link EmailIdentification} accessible through
	 * {@link TrustedCredentials#getUserIdentification()}
	 * 
	 * <p>
	 * Assertions:
	 * <ul>
	 * <li>{@link AuthenticationException} exception shall be thrown</li>
	 * </ul>
	 */
	@Test
	public void testInexistentEmailIdentificationAuthentication() {

		setTrusted(true);

		testInexistentEmailIdentAuthentication("john.smith@braintribe.com\t");
		testInexistentEmailIdentAuthentication("\tjohn.smith@braintribe.com\t");
		testInexistentEmailIdentAuthentication("\tjohn.smith@braintribe.com");
		testInexistentEmailIdentAuthentication("john.smith2@braintribe.com");
		testInexistentEmailIdentAuthentication("JOHN.SMITH@BRAINTRIBE.COM");
		testInexistentEmailIdentAuthentication("john.smith");
	}

	@Test
	public void testInvalidEmailIdentAuthentication() {

		setTrusted(true);

		testInvalidEmailIdentAuthentication(null);
		testInvalidEmailIdentAuthentication(empty);

	}

	private void testSuccessfulUserNameIdentAuthentication(String userName) {
		testSuccessfulAuthentication(createUserNameIdentificationAuthReq(userName));
	}

	private void testSuccessfulEmailIdentAuthentication(String email) {
		testSuccessfulAuthentication(createEmailIdentificationAuthReq(email));
	}

	private void testInexistentUserNameIdentAuthentication(String userName) {
		testFailedAuthenticationExpectingReason(createUserNameIdentificationAuthReq(userName), "inexistent user name: " + identificationDesc(userName)
				+ " should had been considered an invalid/inexistent user name identification.", InvalidCredentials.T);
	}

	private void testInvalidEmailIdentAuthentication(String email) {
		testFailedAuthenticationExpectingReason(createEmailIdentificationAuthReq(email),
				"invalid e-mail: " + identificationDesc(email) + " should had been considered an invalid e-mail identification.",
				InvalidCredentials.T);
	}

	private void testInexistentEmailIdentAuthentication(String email) {
		testFailedAuthenticationExpectingReason(createEmailIdentificationAuthReq(email),
				"inexistent e-mail: " + identificationDesc(email) + " should had been considered an inexistent e-mail identification.",
				InvalidCredentials.T);
	}

	private static TrustedCredentials createTrustedCredentials(UserIdentification identification) {
		TrustedCredentials credentials = TrustedCredentials.T.create();
		credentials.setUserIdentification(identification);
		return credentials;
	}

	private OpenUserSession createUserNameIdentificationAuthReq(String userName) {
		return createOpenUserSession(createTrustedCredentials(createUserNameIdentification(userName)));
	}

	private OpenUserSession createEmailIdentificationAuthReq(String email) {
		return createOpenUserSession(createTrustedCredentials(createEmailIdentification(email)));
	}

}
