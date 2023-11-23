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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.security.reason.AuthenticationFailure;
import com.braintribe.gm.model.security.reason.InvalidSession;
import com.braintribe.gm.model.usersession.PersistenceUserSession;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.securityservice.api.exceptions.SecurityServiceException;
import com.braintribe.model.processing.securityservice.basic.SecurityServiceProcessor;
import com.braintribe.model.processing.securityservice.basic.test.common.AccessDataInitializer;
import com.braintribe.model.processing.securityservice.basic.test.common.TestConfig;
import com.braintribe.model.processing.securityservice.basic.test.wire.SecurityServiceTestWireModule;
import com.braintribe.model.processing.securityservice.basic.test.wire.contract.TestContract;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.securityservice.Logout;
import com.braintribe.model.securityservice.OpenUserSession;
import com.braintribe.model.securityservice.OpenUserSessionResponse;
import com.braintribe.model.securityservice.ValidateUserSession;
import com.braintribe.model.securityservice.credentials.AbstractUserIdentificationCredentials;
import com.braintribe.model.securityservice.credentials.Credentials;
import com.braintribe.model.securityservice.credentials.UserPasswordCredentials;
import com.braintribe.model.securityservice.credentials.identification.EmailIdentification;
import com.braintribe.model.securityservice.credentials.identification.UserIdentification;
import com.braintribe.model.securityservice.credentials.identification.UserNameIdentification;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.user.Group;
import com.braintribe.model.user.User;
import com.braintribe.model.usersession.UserSession;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

/**
 * Base class for Security Service's tests.
 * 
 *
 */
public abstract class SecurityServiceTest {

	protected static TestConfig testConfig;
	// protected static SecurityService securityService;
	protected static Evaluator<ServiceRequest> evaluator;
	protected static SecurityServiceProcessor securityServiceProcessor;
	protected static AccessDataInitializer dataInitializer;

	protected static final String empty = "  \t  ";
	protected static final String random = UUID.randomUUID().toString();
	private static final DateFormat logDf = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss.SSS");

	protected static WireContext<TestContract> context;

	protected void setTrusted(boolean trusted) {
		contract().setTrusted(trusted);
	}

	// ============================= //
	// ======== LIFE CYCLE ========= //
	// ============================= //

	@BeforeClass
	public static void initialize() throws Exception {

		context = Wire.context(SecurityServiceTestWireModule.INSTANCE);
		testConfig = context.contract().testConfig();

		assignBeans();

	}

	@AfterClass
	public static void destroy() throws Exception {
		if (context != null) {
			context.shutdown();
		}
	}

	protected static TestContract contract() {
		return context.contract();
	}

	protected static void assignBeans() {
		context.contract().authGmSession();
		securityServiceProcessor = context.contract().securityServiceProcessor();
		evaluator = context.contract().requestEvaluator();
		dataInitializer = context.contract().dataInitializer();
	}

	public String generateRandomString() {
		return UUID.randomUUID().toString();
	}

	/**
	 * Returns a string representation for the given {@code identification}
	 */
	protected String identificationDesc(String identification) {
		if (identification == null)
			return null;
		if (identification.trim().isEmpty())
			return "empty string";
		return "\"" + identification + "\"";
	}

	/**
	 * Creates a OpenUserSession for the given {@code credentials}
	 */
	protected OpenUserSession createOpenUserSession(Credentials credentials) {
		OpenUserSession request = OpenUserSession.T.create();
		request.setCredentials(credentials);
		return request;
	}

	/**
	 * Creates a UserNameIdentification with the given {@code userName}
	 */
	protected UserNameIdentification createUserNameIdentification(String userName) {
		UserNameIdentification identification = UserNameIdentification.T.create();
		identification.setUserName(userName);
		return identification;
	}

	/**
	 * Creates a EmailIdentification with the given {@code email}
	 */
	protected EmailIdentification createEmailIdentification(String email) {
		EmailIdentification identification = EmailIdentification.T.create();
		identification.setEmail(email);
		return identification;
	}

	protected UserPasswordCredentials createUserPasswordCredentials(UserIdentification identification, String password) {
		UserPasswordCredentials credentials = UserPasswordCredentials.T.create();
		credentials.setUserIdentification(identification);
		credentials.setPassword(password);
		return credentials;
	}

	protected OpenUserSession createUserNameIdentificationPasswordOpenSessionReq(String userName, String password) {
		return createOpenUserSession(createUserPasswordCredentials(createUserNameIdentification(userName), password));
	}

	protected OpenUserSession createEmailIdentificationPasswordOpenSessionReq(String email, String password) {
		return createOpenUserSession(createUserPasswordCredentials(createEmailIdentification(email), password));
	}

	/**
	 * Performs a username/password authentication, returning a valid UserSession to be used in subsequent security service
	 * related tests.
	 */
	protected UserSession openSession() {
		return testSuccessfulAuthentication(createUserNameIdentificationPasswordOpenSessionReq("cortex", "cortex")).getUserSession();
	}

	/**
	 * Performs a username/password authentication for the given username, returning a valid UserSession to be used in
	 * subsequent security service related tests.
	 */
	protected UserSession openSession(String username) {
		User testUser = dataInitializer.getUser(username);
		return testSuccessfulAuthentication(createUserNameIdentificationPasswordOpenSessionReq(testUser.getName(), testUser.getPassword()))
				.getUserSession();
	}

	/**
	 * <p>
	 * Performs a username/password authentication for the given username, returning a valid UserSession to be used in
	 * subsequent security service related tests.
	 * 
	 * <p>
	 * Unlike {@link #openSession(String)}, this method does not performs assertions on the authentication results, making
	 * it more suitable for heavy load tests.
	 * 
	 */
	protected UserSession quickOpenSession(String username) {
		User testUser = dataInitializer.getUser(username);
		return testSuccessfulAuthentication(createUserNameIdentificationPasswordOpenSessionReq(testUser.getName(), testUser.getPassword()), false)
				.getUserSession();
	}

	protected OpenUserSessionResponse testSuccessfulAuthentication(OpenUserSession OpenUserSession, boolean assertResponse) {
		OpenUserSessionResponse authenticationResponse = null;
		try {
			authenticationResponse = OpenUserSession.eval(evaluator).get();
		} catch (SecurityServiceException e) {
			e.printStackTrace();
			Assert.fail("unexpected authentication failure: " + e.getMessage());
		}
		if (assertResponse) {
			assertSuccessfulAuthentication(OpenUserSession, authenticationResponse);
		}
		return authenticationResponse;
	}

	protected OpenUserSessionResponse testSuccessfulAuthenticationViaServiceProcessor(OpenUserSession OpenUserSession, boolean assertResponse) {
		OpenUserSessionResponse authenticationResponse = null;
		try {
			authenticationResponse = OpenUserSession.eval(contract().requestEvaluator()).get();
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("unexpected authentication failure: " + e.getMessage());
		}
		if (assertResponse) {
			assertSuccessfulAuthentication(OpenUserSession, authenticationResponse);
		}
		return authenticationResponse;
	}

	/**
	 * <p>
	 * Authenticates with the given OpenUserSession, asserting that it will be successful
	 */
	protected OpenUserSessionResponse testSuccessfulAuthentication(OpenUserSession OpenUserSession) {
		return testSuccessfulAuthentication(OpenUserSession, true);
	}

	/**
	 * <p>
	 * Authenticates with the given OpenUserSession via
	 * {@link SecurityServiceProcessor#process(com.braintribe.model.processing.service.api.ServiceRequestContext, com.braintribe.model.securityservice.SecurityRequest)},
	 * asserting that it will be successful.
	 */
	protected OpenUserSessionResponse testSuccessfulAuthenticationViaServiceProcessor(OpenUserSession OpenUserSession) {
		return testSuccessfulAuthenticationViaServiceProcessor(OpenUserSession, true);
	}

	/**
	 * Authenticates with the given AuthenticationRequest twice, asserting that both will fail.
	 * 
	 */
	protected void testFailedAuthenticationExpectingReason(OpenUserSession authenticationRequest, String context,
			EntityType<? extends AuthenticationFailure> expectedReason) {
		Maybe<? extends OpenUserSessionResponse> responseMaybe = authenticationRequest.eval(evaluator).getReasoned();

		if (responseMaybe.isUnsatisfiedBy(expectedReason)) {
			return;
		}

		Assert.assertTrue(
				"Authentication (" + context + ") failed returning unexpected message type "
						+ responseMaybe.whyUnsatisfied().entityType().getTypeSignature() + " whilst expected is " + expectedReason.getTypeSignature(),
				responseMaybe.isUnsatisfiedBy(expectedReason));
	}

	/**
	 * Common assertions over the state of the given OpenUserSessionResponse returned from a successful authentication
	 * request based on the given OpenUserSession
	 */
	protected void assertSuccessfulAuthentication(OpenUserSession authReq, OpenUserSessionResponse authRes) {

		Assert.assertNotNull("returned AuthenticationRequest is null", authReq);
		Assert.assertNotNull("returned AuthenticationResponse is null", authRes);
		Assert.assertNotNull("returned AuthenticationResponse.userSession is null", authRes.getUserSession());

		assertUserSession(authRes.getUserSession());
		assertUserIdentification(authReq, authRes.getUserSession().getUser());
	}

	protected void assertEquals(String message, User user1, User user2) {

		if (user1 == null && user2 == null)
			return;

		assertObjNullState(user1, user2);

		Assert.assertTrue(message + ": ids differs",
				user1.getId() == user2.getId() || (user1.getId() != null && user1.getId().equals(user2.getId())));
		Assert.assertEquals(message + ": name differs", user1.getName(), user2.getName());
		Assert.assertEquals(message + ": first names differs", user1.getFirstName(), user2.getFirstName());
		Assert.assertEquals(message + ": last names differs", user1.getLastName(), user2.getLastName());
		Assert.assertEquals(message + ": e-mails differs", user1.getEmail(), user2.getEmail());

	}

	protected void assertEquals(String message, UserSession userSession1, UserSession userSession2) {

		if (userSession1 == null && userSession2 == null)
			return;

		assertObjNullState(userSession1, userSession2);

		Assert.assertEquals(message + ": session ids differs", userSession1.getSessionId(), userSession2.getSessionId());
		// Assert.assertEquals(message+": user ids differs", userSession1.getUser().getId(), userSession2.getUser().getId());
	}

	/**
	 * Assert that the {@code userIdentification} matches the given {@code user}
	 */
	protected void assertUserIdentification(UserIdentification userIdentification, User user) {
		if (userIdentification instanceof UserNameIdentification)
			assertUserNameIdentification((UserNameIdentification) userIdentification, user);
		if (userIdentification instanceof EmailIdentification)
			assertEmailIdentification((EmailIdentification) userIdentification, user);
	}

	protected void assertEntityProperty(GenericEntity entity, String propertyName, Object propertyValue) {

		Object fetchedValue = entity.entityType().getProperty(propertyName).get(entity);

		if (propertyValue == null && fetchedValue == null)
			return;

		assertObjNullState(propertyValue, fetchedValue);
		Assert.assertEquals(String.format("'%s.%s' value found is '%s' but was expected to be '%s'", entity.getClass().getSimpleName(), propertyName,
				fetchedValue, propertyValue), propertyValue, fetchedValue);
	}

	protected void assertTouched(Date lastAccessedDateBefore, Date lastAccessedDateAfter) {
		String dateDesc = String.format("date before: %s date after: %s", logDf.format(lastAccessedDateBefore), logDf.format(lastAccessedDateAfter));
		Assert.assertTrue("lastAccessedDate was not updated during session touch. " + dateDesc,
				lastAccessedDateAfter.getTime() != lastAccessedDateBefore.getTime());
		Assert.assertTrue("lastAccessedDate should had been updated to a date posterior to its previous value. " + dateDesc,
				lastAccessedDateAfter.after(lastAccessedDateBefore));
	}

	protected void assertNotTouched(Date lastAccessedDateBefore, Date lastAccessedDateAfter) {
		String dateDesc = String.format("date before: %s date after: %s", logDf.format(lastAccessedDateBefore), logDf.format(lastAccessedDateAfter));
		Assert.assertTrue("lastAccessedDate was updated during invalid session touch. " + dateDesc,
				lastAccessedDateBefore.getTime() == lastAccessedDateAfter.getTime());
	}

	protected void assertRoles(String userId, Set<String> roles) {
		Set<String> expectedRoles = dataInitializer.expectedEffectiveRoles(userId);
		Assert.assertArrayEquals(userId + " roles " + roles + " differs from the expected: " + expectedRoles, toSortedArray(expectedRoles),
				toSortedArray(roles));
	}

	/**
	 * Logs out the given user session and asserts that it has been logged out.
	 */
	protected void assertUserSessionLogOut(String sessionId) throws Exception {
		logout(sessionId);
		assertUserSessionLoggedOut(sessionId);
	}

	protected boolean logout(String sessionId) {
		Logout logout = Logout.T.create();
		logout.setSessionId(sessionId);

		Maybe<Boolean> maybe = logout.eval(evaluator).getReasoned();

		if (maybe.isSatisfied())
			return maybe.get();

		return false;
	}

	/**
	 * Asserts that the user session was logged out.
	 */
	protected void assertUserSessionLoggedOut(String sessionId) throws Exception {
		assertUserSessionDeletion(sessionId);
		Maybe<UserSession> maybe = validate(sessionId);
		Assert.assertTrue("logged out user session [ " + sessionId + " ] shouldn't have been considered valid by the security service",
				maybe.isUnsatisfiedBy(InvalidSession.T));
	}

	protected boolean isValidSession(String sessionId) {
		return validate(sessionId).isSatisfied();
	}

	protected Maybe<UserSession> validate(String sessionId) {
		ValidateUserSession validateUserSession = ValidateUserSession.T.create();
		validateUserSession.setSessionId(sessionId);
		return validateUserSession.eval(evaluator).getReasoned().cast();
	}

	/**
	 * TODO: use the right tool.
	 */
	protected void printUserSession(UserSession session, String context) {
		String h = "---------- " + context + " ----------";
		System.out.println(h);
		System.out.println("-- session: " + session);
		System.out.println("-- session.getUser(): " + session.getUser());
		System.out.println(StringUtils.repeat("-", h.length()));
	}

	/**
	 * Asserts the the given objects are in the same null state. Either all null, or none.
	 */
	private static void assertObjNullState(Object... objs) {
		if (objs == null)
			return;
		boolean notNullFound = false, nullFound = false;
		for (Object obj : objs) {
			if (obj != null)
				notNullFound = true;
			else
				nullFound = true;
			if (notNullFound && nullFound)
				Assert.fail("objects in inconsistent null state: " + objs);
		}
	}

	/**
	 * Common assertions over the state of the given UserSession returned from AuthenticationResponse upon successful
	 * authentication
	 */
	protected void assertUserSession(UserSession userSession) {

		Assert.assertNotNull("returned AuthenticationResponse.userSession.sessionId is null", userSession.getSessionId());
		Assert.assertNotNull("returned AuthenticationResponse.userSession.userId is null", userSession.getUser().getId());
		Assert.assertTrue("returned unexpected AuthenticationResponse.userSession.referenceCounter: " + userSession.getReferenceCounter(),
				userSession.getReferenceCounter() == 0);

		try {

			ValidateUserSession validateUserSession = ValidateUserSession.T.create();
			validateUserSession.setSessionId(userSession.getSessionId());
			UserSession validatedUserSession = validateUserSession.eval(evaluator).get();
			Assert.assertTrue(
					"session [ " + userSession.getSessionId()
							+ " ] returned from AuthenticationResponse.userSession was not considered valid by SecurityService().isSessionValid()",
					validatedUserSession != null);

		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("Unexpected failure while checking session validity: " + e.getMessage());
		}

		assertUser(userSession.getUser());
		assertEffectiveRoles(userSession);
	}

	/**
	 * Common assertions over the state of the given User returned from AuthenticationResponse.userSession.user upon
	 * successful authentication
	 */
	private static void assertUser(User user) {

		Assert.assertNotNull("returned AuthenticationResponse.userSession.user.id is null", user.getId());

		if (user.getGroups() != null) {
			for (Group group : user.getGroups()) {
				if (group == null || group.getUsers() == null)
					continue;
				Assert.assertTrue(
						"list of users from userSession.user[" + user.getId() + "].groups[" + group.getName() + "].users "
								+ "is supposed to be null or empty, but exists with " + group.getUsers().size() + " element(s): " + group.getUsers(),
						(group.getUsers() == null || group.getUsers().isEmpty()));
			}
		}

		Assert.assertNull("returned AuthenticationResponse.userSession.user.password is not null", user.getPassword());

	}

	/**
	 * Common assertions over the state of the given User's roles returned from AuthenticationResponse.userSession upon
	 * successful authentication
	 */
	private void assertEffectiveRoles(UserSession userSession) {
		Set<String> expectedRoles = dataInitializer.expectedEffectiveRoles(userSession.getUser().getName());

		if (expectedRoles != null) {
			Set<String> returnedRoles = userSession.getEffectiveRoles() == null ? Collections.<String> emptySet() : userSession.getEffectiveRoles();

			Assert.assertArrayEquals("effective roles returned from UserSession differs from the expected", toSortedArray(expectedRoles),
					toSortedArray(returnedRoles));
		}
	}

	/**
	 * Convert a String Set to a sorted array
	 */
	private static String[] toSortedArray(Set<String> roles) {
		if (roles == null || roles.isEmpty())
			return new String[0];
		String[] arr = roles.toArray(new String[0]);
		Arrays.sort(arr);
		return arr;
	}

	private void assertUserIdentification(OpenUserSession authReq, User user) {
		if (!(authReq.getCredentials() instanceof AbstractUserIdentificationCredentials))
			return;
		assertUserIdentification(((AbstractUserIdentificationCredentials) authReq.getCredentials()).getUserIdentification(), user);
	}

	private static void assertUserNameIdentification(UserNameIdentification userNameIdentification, User user) {
		Assert.assertEquals("username from  user differs from username given by UserNameIdentification", userNameIdentification.getUserName(),
				user.getName());
	}

	private static void assertEmailIdentification(EmailIdentification emailIdentification, User user) {
		Assert.assertEquals("email from  user differs from mail given by EmailIdentification", emailIdentification.getEmail(), user.getEmail());
	}

	/**
	 * Asserts the that the user session was deleted from the main user session database.
	 */
	private void assertUserSessionDeletion(String sessionId) throws Exception {

		EntityQuery query = EntityQueryBuilder.from(PersistenceUserSession.T).where().property(PersistenceUserSession.id).eq(sessionId).done();

		List<PersistenceUserSession> userSessions = context.contract().userSessionsGmSession().query().entities(query).list();

		Assert.assertTrue("no UserSession should have been returned from querying sessionId [ " + sessionId + " ] after it was logged out.",
				userSessions == null || userSessions.isEmpty());

	}

	protected void logExpected(Throwable t) {
		System.out.println("Expected [ " + t.getClass().getSimpleName() + " ]  thrown. Message : " + t.getMessage());
	}

}
