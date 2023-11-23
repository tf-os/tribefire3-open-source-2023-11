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

import java.util.function.Supplier;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.model.processing.securityservice.api.UserSessionScope;
import com.braintribe.model.processing.securityservice.api.UserSessionScoping;
import com.braintribe.model.processing.securityservice.api.UserSessionScopingBuilder;
import com.braintribe.model.processing.securityservice.api.exceptions.InvalidCredentialsException;
import com.braintribe.model.processing.securityservice.commons.scope.StandardUserSessionScoping;
import com.braintribe.model.processing.service.common.context.UserSessionStack;
import com.braintribe.model.securityservice.credentials.Credentials;
import com.braintribe.model.securityservice.credentials.UserPasswordCredentials;
import com.braintribe.model.securityservice.credentials.identification.UserNameIdentification;
import com.braintribe.model.usersession.UserSession;

public class UserSessionScopingTestBase extends SecurityServiceTest {

	private final UserSessionStack userSessionStack = new UserSessionStack();

	@Test
	public void testPushPopWithUserPasswordCredentials() throws Exception {

		UserSessionScoping userSessionScoping = getUserSessionScoping();

		Credentials credentials = getValidUserPasswordCredentials();

		UserSessionScope userSessionScope = userSessionScoping.forCredentials(credentials).push();

		testPushPop(userSessionScope);

	}

	@Test
	public void testRunWithUserPasswordCredentials() throws Exception {

		UserSessionScoping userSessionScoping = getUserSessionScoping();

		Credentials credentials = getValidUserPasswordCredentials();

		UserSessionScopingBuilder userSessionScopingBuilder = userSessionScoping.forCredentials(credentials);

		testRun(userSessionScopingBuilder);

	}

	@Test(expected = InvalidCredentialsException.class)
	public void testPushPopWithInvalidUserPasswordCredentials() throws Exception {

		UserSessionScoping userSessionScoping = getUserSessionScoping();

		Credentials credentials = getInvalidUserPasswordCredentials();

		UserSessionScope userSessionScope = userSessionScoping.forCredentials(credentials).push();

		testPushPop(userSessionScope);

	}

	@Test(expected = InvalidCredentialsException.class)
	public void testRunWithInvalidUserPasswordCredentials() throws Exception {

		UserSessionScoping userSessionScoping = getUserSessionScoping();

		Credentials credentials = getInvalidUserPasswordCredentials();

		UserSessionScopingBuilder userSessionScopingBuilder = userSessionScoping.forCredentials(credentials);

		testRun(userSessionScopingBuilder);

	}

	@Test(expected = NullPointerException.class)
	public void testPushPopWithNullCredentials() throws Exception {

		UserSessionScoping userSessionScoping = getUserSessionScopingWithDefault();

		UserSessionScope userSessionScope = userSessionScoping.forCredentials(null).push();

		testPushPop(userSessionScope);

	}

	@Test(expected = NullPointerException.class)
	public void testRunWithNullCredentials() throws Exception {

		UserSessionScoping userSessionScoping = getUserSessionScopingWithDefault();

		UserSessionScopingBuilder userSessionScopingBuilder = userSessionScoping.forCredentials(null);

		testRun(userSessionScopingBuilder);

	}

	@Test
	public void testPushPopWithDefault() throws Exception {

		UserSessionScoping userSessionScoping = getUserSessionScopingWithDefault();

		UserSessionScope userSessionScope = userSessionScoping.forDefaultUser().push();

		testPushPop(userSessionScope);

	}

	@Test
	public void testRunWithDefault() throws Exception {

		UserSessionScoping userSessionScoping = getUserSessionScopingWithDefault();

		UserSessionScopingBuilder userSessionScopingBuilder = userSessionScoping.forDefaultUser();

		testRun(userSessionScopingBuilder);

	}

	@Test
	public void testForDefaultUserWithoutDefault() throws Exception {

		UserSessionScoping userSessionScoping = getUserSessionScoping();

		try {
			userSessionScoping.forDefaultUser();
			Assert.fail("forDefaultUser() should have failed if called in a UserSessionScoping without default user session provider");
		} catch (Throwable t) {
			System.out.println("Expected " + t.getClass().getName() + (t.getMessage() != null ? ": " + t.getMessage() : ""));
		}

	}

	@Test
	public void testNullCredentialsWithoutDefault() throws Exception {

		UserSessionScoping userSessionScoping = getUserSessionScoping();

		try {
			userSessionScoping.forCredentials(null);
			Assert.fail("forCredentials(null) should have failed if called in a UserSessionScoping without default user session provider");
		} catch (Throwable t) {
			System.out.println("Expected " + t.getClass().getName() + (t.getMessage() != null ? ": " + t.getMessage() : ""));
		}

	}

	protected void testRun(UserSessionScopingBuilder userSessionScopingBuilder) throws Exception {

		userSessionScopingBuilder.runInScope(new Runnable() {
			@Override
			public void run() {
				try {
					Assert.assertNotNull("Failed to provide pushed UserSession", userSessionStack.peek());
				} catch (RuntimeException e) {
					Assert.fail(e.getMessage());
				}
			}
		});

		UserSession threadLocalUserSession = userSessionStack.peek();

		Assert.assertNull("ThreadLocal still held UserSession after runInScope() call", threadLocalUserSession);

	}

	protected void testPushPop(UserSessionScope userSessionScope) throws Exception {

		Assert.assertNotNull("Failed to provide pushed UserSession", userSessionStack.get());

		UserSession getterUserSession = userSessionScope.getUserSession();

		Assert.assertNotNull("getUserSession() returned null", getterUserSession);

		Assert.assertNotNull("Failed to provide pushed UserSession after getUserSession() was called", userSessionStack.get());

		UserSession poppedUserSession = userSessionScope.pop();

		Assert.assertNotNull("pop() returned null", poppedUserSession);

		UserSession threadLocalUserSession = userSessionStack.get();

		Assert.assertNull("ThreadLocal still held UserSession after pop() call", threadLocalUserSession);

	}

	protected UserSessionScoping getUserSessionScoping() {
		StandardUserSessionScoping basicUserSessionScoping = new StandardUserSessionScoping();
		basicUserSessionScoping.setRequestEvaluator(context.contract().requestEvaluator());
		basicUserSessionScoping.setUserSessionStack(userSessionStack);
		return basicUserSessionScoping;
	}

	protected UserSessionScoping getUserSessionScopingWithDefault() {
		StandardUserSessionScoping basicUserSessionScoping = new StandardUserSessionScoping();
		basicUserSessionScoping.setRequestEvaluator(context.contract().requestEvaluator());
		basicUserSessionScoping.setUserSessionStack(userSessionStack);
		basicUserSessionScoping.setDefaultUserSessionSupplier(new Supplier<UserSession>() {

			@Override
			public UserSession get() throws RuntimeException {
				return UserSessionScopingTestBase.super.openSession();
			}

		});

		return basicUserSessionScoping;
	}

	private Credentials getValidUserPasswordCredentials() {
		return createUserPasswordCredentials("cortex", "cortex");
	}

	private Credentials getInvalidUserPasswordCredentials() {
		return createUserPasswordCredentials("unknown", "false");
	}

	private static Credentials createUserPasswordCredentials(String user, String password) {
		UserNameIdentification identification = UserNameIdentification.T.create();
		identification.setUserName(user);
		UserPasswordCredentials credentials = UserPasswordCredentials.T.create();
		credentials.setUserIdentification(identification);
		credentials.setPassword(password);
		return credentials;
	}

}
