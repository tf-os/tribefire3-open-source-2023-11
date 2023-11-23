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
package com.braintribe.gm.security.test.auth;

import java.util.UUID;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.security.reason.AuthenticationFailure;
import com.braintribe.gm.security.test.auth.model.ServiceRequest1;
import com.braintribe.gm.security.test.auth.model.ServiceRequest2;
import com.braintribe.gm.security.test.auth.processing.ServiceProcessor2;
import com.braintribe.gm.security.test.auth.wire.AuthorizingServiceProcessorTestWireModule;
import com.braintribe.gm.security.test.auth.wire.contract.AuthorizingServiceProcessorTestContract;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.securityservice.OpenUserSession;
import com.braintribe.model.securityservice.credentials.UserPasswordCredentials;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

/**
 * <p>
 * 
 */
public class AuthorizingServiceProcessorTest {

	protected static WireContext<AuthorizingServiceProcessorTestContract> context;
	protected static Evaluator<ServiceRequest> evaluator;
	protected static AuthorizingServiceProcessorTestContract testContract;

	@BeforeClass
	public static void beforeClass() {
		context = Wire.context(AuthorizingServiceProcessorTestWireModule.INSTANCE);
		testContract = context.contract();
		evaluator = testContract.evaluator();
	}

	@AfterClass
	public static void afterClass() {
		context.shutdown();
	}

	public static String validSessionId() {
		OpenUserSession openUserSession = OpenUserSession.T.create();

		openUserSession.setCredentials(UserPasswordCredentials.forUserName("tester", "7357"));

		return openUserSession.eval(evaluator).get().getUserSession().getSessionId();
	}

	@Test
	public void testAuthorizationFreeRequest() throws Exception {
		String result = ServiceRequest1.T.create().eval(evaluator).get();
		Assert.assertNotNull("Call with non-AuthorizedRequest should have been Successful", result);
	}

	@Test
	public void testSuccessfulAuthorization() throws Exception {

		ServiceRequest2 request = ServiceRequest2.T.create();
		request.setSessionId(validSessionId());
		request.setValidateAuthorizationContext(true);

		Long result = request.eval(evaluator).get();

		Assert.assertEquals(ServiceProcessor2.RETURN, result);
	}

	@Test
	public void testSuccessfulAuthorizationWithMetadata() throws Exception {
		ServiceRequest2 request = ServiceRequest2.T.create();
		request.getMetaData().put("sessionId", validSessionId());
		request.setValidateAuthorizationContext(true);

		Long result = request.eval(evaluator).get();

		Assert.assertEquals(ServiceProcessor2.RETURN, result);
	}

	@Test
	public void testUnsuccessfulAuthorization() throws Exception {

		ServiceRequest2 request = ServiceRequest2.T.create();
		request.setSessionId(UUID.randomUUID().toString());
		request.setValidateAuthorizationContext(true);

		Maybe<Long> result = request.eval(evaluator).getReasoned();

		Assert.assertTrue(result.isUnsatisfiedBy(AuthenticationFailure.T));
	}

	@Test
	public void testUnsuccessfulAuthorizationWithMetadata() throws Exception {

		ServiceRequest2 request = ServiceRequest2.T.create();
		request.getMetaData().put("sessionId", UUID.randomUUID().toString());
		request.setValidateAuthorizationContext(true);

		Maybe<Long> result = request.eval(evaluator).getReasoned();

		Assert.assertTrue(result.isUnsatisfiedBy(AuthenticationFailure.T));
	}
}
