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

import com.braintribe.gm.model.security.reason.InvalidCredentials;
import com.braintribe.model.processing.securityservice.api.exceptions.AuthenticationException;
import com.braintribe.model.securityservice.OpenUserSession;
import com.braintribe.model.securityservice.OpenUserSessionResponse;
import com.braintribe.model.securityservice.credentials.Credentials;
import com.braintribe.model.securityservice.credentials.UserPasswordCredentials;
import com.braintribe.model.securityservice.credentials.identification.EmailIdentification;
import com.braintribe.model.securityservice.credentials.identification.UserNameIdentification;
import com.braintribe.model.usersession.UserSession;

/**
 * {@link com.braintribe.model.processing.securityservice.basic.SecurityServiceProcessor#openUserSession(OpenUserSession)}
 * tests based on {@link UserPasswordCredentials}
 * 
 */
public class UserPasswordCredentialsAuthenticationTestBase extends SecurityServiceTest {

	@Test
	public void testLocale() {

		String locale = "en";

		OpenUserSession authReq = createUserNameIdentificationPasswordOpenSessionReq("robert.taylor", "3333");
		authReq.setLocale(locale);

		OpenUserSessionResponse response = testSuccessfulAuthentication(authReq);
		Assert.assertEquals(locale, response.getUserSession().locale());
	}

	@Test
	public void testNoLocale() {

		OpenUserSession authReq = createUserNameIdentificationPasswordOpenSessionReq("robert.taylor", "3333");

		OpenUserSessionResponse response = testSuccessfulAuthentication(authReq);
		Assert.assertTrue(response.getUserSession().locale() == null);
	}

	@Test
	public void testUserName() {

		OpenUserSession authReq = createUserNameIdentificationPasswordOpenSessionReq("robert.taylor", "3333");

		testSuccessfulAuthentication(authReq);

	}

	@Test
	public void testUserNameNullPassword() {

		String context = "with null password";

		OpenUserSession authReq = createUserNameIdentificationPasswordOpenSessionReq("robert.taylor", null);

		testFailedAuthenticationExpectingReason(authReq, context, InvalidCredentials.T);

	}

	@Test
	public void testUserNameEmptyPassword() {

		String context = "with empty password";

		OpenUserSession authReq = createUserNameIdentificationPasswordOpenSessionReq("robert.taylor", empty);

		testFailedAuthenticationExpectingReason(authReq, context, InvalidCredentials.T);

	}

	@Test
	public void testUserNameInvalidPassword() {

		String context = "with invalid password";

		OpenUserSession authReq = createUserNameIdentificationPasswordOpenSessionReq("robert.taylor", "3333\t");

		testFailedAuthenticationExpectingReason(authReq, context, InvalidCredentials.T);

	}

	@Test
	public void testEmail() {

		OpenUserSession authReq = createEmailIdentificationPasswordOpenSessionReq("steven.brown@braintribe.com", "4444");

		testSuccessfulAuthentication(authReq);

	}

	@Test
	public void testEmailNullPassword() {

		String context = "with null password";

		OpenUserSession authReq = createEmailIdentificationPasswordOpenSessionReq("robert.taylor@braintribe", null);

		testFailedAuthenticationExpectingReason(authReq, context, InvalidCredentials.T);

	}

	@Test
	public void testEmailEmptyPassword() {

		String context = "with empty password";

		OpenUserSession authReq = createEmailIdentificationPasswordOpenSessionReq("robert.taylor@braintribe", empty);

		testFailedAuthenticationExpectingReason(authReq, context, InvalidCredentials.T);

	}

	@Test
	public void testEmailInvalidPassword() {

		String context = "with invalid password";

		OpenUserSession authReq = createEmailIdentificationPasswordOpenSessionReq("robert.taylor@braintribe", "3333 ");

		testFailedAuthenticationExpectingReason(authReq, context, InvalidCredentials.T);

	}

	@Test
	public void testNullUserName() throws Exception {

		String context = "with null username";

		OpenUserSession authReq = createUserNameIdentificationPasswordOpenSessionReq(null, "3333");

		testFailedAuthenticationExpectingReason(authReq, context, InvalidCredentials.T);

	}

	@Test
	public void testEmptyUserName() throws Exception {

		String context = "with empty username";

		OpenUserSession authReq = createUserNameIdentificationPasswordOpenSessionReq(empty, "3333");

		testFailedAuthenticationExpectingReason(authReq, context, InvalidCredentials.T);

	}

	@Test
	public void testInexistentUserName() throws Exception {

		String context = "with inexistent username";

		OpenUserSession authReq = createUserNameIdentificationPasswordOpenSessionReq(random, "3333");

		testFailedAuthenticationExpectingReason(authReq, context, InvalidCredentials.T);

	}

	@Test
	public void testNullEmail() throws Exception {

		String context = "with null e-mail";

		OpenUserSession authReq = createEmailIdentificationPasswordOpenSessionReq(null, "3333");

		testFailedAuthenticationExpectingReason(authReq, context, InvalidCredentials.T);

	}

	@Test
	public void testEmptyEmail() throws Exception {

		String context = "with empty e-mail";

		OpenUserSession authReq = createEmailIdentificationPasswordOpenSessionReq(empty, "3333");

		testFailedAuthenticationExpectingReason(authReq, context, InvalidCredentials.T);

	}

	@Test
	public void testInexistentEmail() throws Exception {

		String context = "with inexistent e-mail";

		OpenUserSession authReq = createEmailIdentificationPasswordOpenSessionReq(random, "3333");

		testFailedAuthenticationExpectingReason(authReq, context, InvalidCredentials.T);

	}

	@Test
	public void testNullIdentification() {

		Credentials credentials = createUserPasswordCredentials(null, "non-null-password");

		OpenUserSession openUserSession = createOpenUserSession(credentials);

		testFailedAuthenticationExpectingReason(openUserSession, "null identification", InvalidCredentials.T);

	}

	/**
	 * Targets:
	 * {@link com.braintribe.model.processing.securityservice.basic.SecurityServiceProcessor#openUserSession(OpenUserSession)}
	 * 
	 * <p>
	 * Input: {@link UserPasswordCredentials} containing existing valid {@link UserNameIdentification} and password
	 * combination
	 * 
	 * <p>
	 * Assertions:
	 * <ul>
	 * <li>{@link OpenUserSessionResponse} shall be returned with a valid {@link UserSession} accessible through
	 * {@link OpenUserSessionResponse#getUserSession()}
	 * <li>{@link UserSession#getUser()} from {@link OpenUserSessionResponse#getUserSession()} must match the user
	 * identification provided by {@link UserPasswordCredentials#getUserIdentification()}
	 * <li>No exceptions shall be thrown
	 * </ul>
	 */
	@Test
	public void testSuccessfulUserNameIdentificationAuthentication() {
		testSuccessfulUserNameIdentificationAuth("john.smith", "1111");
		testSuccessfulUserNameIdentificationAuth("mary.williams", "2222");
		testSuccessfulUserNameIdentificationAuth("robert.taylor", "3333");
		testSuccessfulUserNameIdentificationAuth("steven.brown", "4444");
	}

	/**
	 * Targets:
	 * {@link com.braintribe.model.processing.securityservice.basic.SecurityServiceProcessor#openUserSession(OpenUserSession)}
	 * 
	 * <p>
	 * Input: {@link UserPasswordCredentials} containing existing valid {@link EmailIdentification} and password combination
	 * 
	 * <p>
	 * Assertions:
	 * <ul>
	 * <li>{@link OpenUserSessionResponse} shall be returned with a valid {@link UserSession} accessible through
	 * {@link OpenUserSessionResponse#getUserSession()}
	 * <li>{@link UserSession#getUser()} from {@link OpenUserSessionResponse#getUserSession()} must match the user
	 * identification provided by {@link UserPasswordCredentials#getUserIdentification()}
	 * <li>No exceptions shall be thrown
	 * </ul>
	 */
	@Test
	public void testSuccessfulEmailIdentificationAuthentication() {
		testSuccessfulEmailIdentificationAuth("john.smith@braintribe.com", "1111");
		testSuccessfulEmailIdentificationAuth("mary.williams@braintribe.com", "2222");
		testSuccessfulEmailIdentificationAuth("robert.taylor@braintribe.com", "3333");
		testSuccessfulEmailIdentificationAuth("steven.brown@braintribe.com", "4444");
	}

	/**
	 * Targets:
	 * {@link com.braintribe.model.processing.securityservice.basic.SecurityServiceProcessor#openUserSession(OpenUserSession)}
	 * 
	 * <p>
	 * Input: {@link UserPasswordCredentials} containing invalid/inexistent {@link UserNameIdentification}
	 * 
	 * <p>
	 * Assertions:
	 * <ul>
	 * <li>{@link AuthenticationException} exception shall be thrown</li>
	 * </ul>
	 */
	@Test
	public void testInexistentUserNameIdentificationAuthentication() {
		testInexistentUserNameIdentificationAuth("robert.taylor\t", "3333");
		testInexistentUserNameIdentificationAuth("\trobert.taylor\t", "3333");
		testInexistentUserNameIdentificationAuth("\trobert.taylor", "3333");
		testInexistentUserNameIdentificationAuth("robert.taylor2", "3333");
		testInexistentUserNameIdentificationAuth("ROBERT.TAYLOR", "3333");
		testInexistentUserNameIdentificationAuth("robert.taylor@braintribe.com", "3333");
	}

	/**
	 * Targets:
	 * {@link com.braintribe.model.processing.securityservice.basic.SecurityServiceProcessor#openUserSession(OpenUserSession)}
	 * 
	 * <p>
	 * Input: {@link UserPasswordCredentials} containing invalid/inexistent {@link EmailIdentification}
	 * 
	 * <p>
	 * Assertions:
	 * <ul>
	 * <li>{@link AuthenticationException} exception shall be thrown</li>
	 * </ul>
	 */
	@Test
	public void testInexistentEmailIdentificationAuthentication() {
		testInexistentEmailIdentificationAuth("robert.taylor@braintribe.com\t", "3333");
		testInexistentEmailIdentificationAuth("\trobert.taylor@braintribe.com\t", "3333");
		testInexistentEmailIdentificationAuth("\trobert.taylor@braintribe.com", "3333");
		testInexistentEmailIdentificationAuth("robert.taylor@braintribe.com2", "3333");
		testInexistentEmailIdentificationAuth("ROBERT.TAYLOR@braintribe.com", "3333");
		testInexistentEmailIdentificationAuth("robert.taylor", "3333");
	}

	/**
	 * Targets:
	 * {@link com.braintribe.model.processing.securityservice.basic.SecurityServiceProcessor#openUserSession(OpenUserSession)}
	 * 
	 * <p>
	 * Input: {@link UserPasswordCredentials} containing valid existent {@link UserNameIdentification} but invalid password
	 * 
	 * <p>
	 * Assertions:
	 * <ul>
	 * <li>{@link AuthenticationException} exception shall be thrown</li>
	 * </ul>
	 */
	@Test
	public void testInvalidPasswordUserNameIdentificationAuthentication() {
		testInvalidPasswordUserNameIdentificationAuth("john.smith", empty);
		testInvalidPasswordUserNameIdentificationAuth("john.smith", "WRONG");
		testInvalidPasswordUserNameIdentificationAuth("john.smith", "1111\t");
		testInvalidPasswordUserNameIdentificationAuth("john.smith", "\t1111 ");
		testInvalidPasswordUserNameIdentificationAuth("john.smith", "\t1111");

		testInvalidPasswordUserNameIdentificationAuth("robert.taylor", empty);
		testInvalidPasswordUserNameIdentificationAuth("robert.taylor", "WRONG");
		testInvalidPasswordUserNameIdentificationAuth("robert.taylor", "3333\t");
		testInvalidPasswordUserNameIdentificationAuth("robert.taylor", "\t3333\t");
		testInvalidPasswordUserNameIdentificationAuth("robert.taylor", "\t3333");

		testInvalidPasswordUserNameIdentificationAuth("john.smith", "2222");
		testInvalidPasswordUserNameIdentificationAuth("john.smith", "3333");
		testInvalidPasswordUserNameIdentificationAuth("john.smith", "4444");

		testInvalidPasswordUserNameIdentificationAuth("mary.williams", "1111");
		testInvalidPasswordUserNameIdentificationAuth("mary.williams", "3333");
		testInvalidPasswordUserNameIdentificationAuth("mary.williams", "4444");

		testInvalidPasswordUserNameIdentificationAuth("robert.taylor", "1111");
		testInvalidPasswordUserNameIdentificationAuth("robert.taylor", "2222");
		testInvalidPasswordUserNameIdentificationAuth("robert.taylor", "4444");

		testInvalidPasswordUserNameIdentificationAuth("steven.brown", "1111");
		testInvalidPasswordUserNameIdentificationAuth("steven.brown", "2222");
		testInvalidPasswordUserNameIdentificationAuth("steven.brown", "3333");

	}

	/**
	 * Targets:
	 * {@link com.braintribe.model.processing.securityservice.basic.SecurityServiceProcessor#openUserSession(OpenUserSession)}
	 * 
	 * <p>
	 * Input: {@link UserPasswordCredentials} containing valid existent {@link EmailIdentification} but invalid password
	 * 
	 * <p>
	 * Assertions:
	 * <ul>
	 * <li>{@link AuthenticationException} exception shall be thrown</li>
	 * </ul>
	 */
	@Test
	public void testInvalidPasswordEmailIdentificationAuthentication() {
		testInvalidPasswordEmailIdentificationAuth("john.smith@braintribe.com", empty);
		testInvalidPasswordEmailIdentificationAuth("john.smith@braintribe.com", "WRONG");
		testInvalidPasswordEmailIdentificationAuth("john.smith@braintribe.com", "1111\t");
		testInvalidPasswordEmailIdentificationAuth("john.smith@braintribe.com", "\t1111 ");
		testInvalidPasswordEmailIdentificationAuth("john.smith@braintribe.com", "\t1111");

		testInvalidPasswordEmailIdentificationAuth("robert.taylor@braintribe.com", empty);
		testInvalidPasswordEmailIdentificationAuth("robert.taylor@braintribe.com", "WRONG");
		testInvalidPasswordEmailIdentificationAuth("robert.taylor@braintribe.com", "3333\t");
		testInvalidPasswordEmailIdentificationAuth("robert.taylor@braintribe.com", "\t3333\t");
		testInvalidPasswordEmailIdentificationAuth("robert.taylor@braintribe.com", "\t3333");

		testInvalidPasswordEmailIdentificationAuth("john.smith@braintribe.com", "2222");
		testInvalidPasswordEmailIdentificationAuth("john.smith@braintribe.com", "3333");
		testInvalidPasswordEmailIdentificationAuth("john.smith@braintribe.com", "4444");

		testInvalidPasswordEmailIdentificationAuth("mary.williams@braintribe.com", "1111");
		testInvalidPasswordEmailIdentificationAuth("mary.williams@braintribe.com", "3333");
		testInvalidPasswordEmailIdentificationAuth("mary.williams@braintribe.com", "4444");

		testInvalidPasswordEmailIdentificationAuth("robert.taylor@braintribe.com", "1111");
		testInvalidPasswordEmailIdentificationAuth("robert.taylor@braintribe.com", "2222");
		testInvalidPasswordEmailIdentificationAuth("robert.taylor@braintribe.com", "4444");

		testInvalidPasswordEmailIdentificationAuth("steven.brown@braintribe.com", "1111");
		testInvalidPasswordEmailIdentificationAuth("steven.brown@braintribe.com", "2222");
		testInvalidPasswordEmailIdentificationAuth("steven.brown@braintribe.com", "3333");
	}

	private void testSuccessfulUserNameIdentificationAuth(String userName, String password) {
		testSuccessfulAuthentication(createUserNameIdentificationPasswordOpenSessionReq(userName, password));
	}

	private void testSuccessfulEmailIdentificationAuth(String email, String password) {
		testSuccessfulAuthentication(createEmailIdentificationPasswordOpenSessionReq(email, password));
	}

	private void testInexistentUserNameIdentificationAuth(String userName, String password) {
		testFailedAuthenticationExpectingReason(createUserNameIdentificationPasswordOpenSessionReq(userName, password), "inexistent user name: "
				+ identificationDesc(userName) + " should had been considered an invalid/inexistent user name identification.", InvalidCredentials.T);
	}

	private void testInexistentEmailIdentificationAuth(String email, String password) {
		testFailedAuthenticationExpectingReason(createEmailIdentificationPasswordOpenSessionReq(email, password),
				"inexistent e-mail: " + identificationDesc(email) + " should had been considered an invalid/inexistent e-mail identification.",
				InvalidCredentials.T);
	}

	private void testInvalidPasswordUserNameIdentificationAuth(String userName, String password) {
		testInvalidPasswordAuthentication(createUserNameIdentificationPasswordOpenSessionReq(userName, password), userName, password);
	}

	private void testInvalidPasswordEmailIdentificationAuth(String email, String password) {
		testInvalidPasswordAuthentication(createEmailIdentificationPasswordOpenSessionReq(email, password), email, password);
	}

	private void testInvalidPasswordAuthentication(OpenUserSession authRequest, String userIdentification, String password) {
		testFailedAuthenticationExpectingReason(authRequest,
				"invalid password: given password " + identificationDesc(password)
						+ " should had been considered an invalid password since it does not match " + identificationDesc(userIdentification)
						+ "'s password.",
				InvalidCredentials.T);
	}

}
