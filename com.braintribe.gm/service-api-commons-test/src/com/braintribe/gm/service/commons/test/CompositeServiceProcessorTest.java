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
package com.braintribe.gm.service.commons.test;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.gm.service.commons.test.model.ServiceRequest1;
import com.braintribe.gm.service.commons.test.model.ServiceRequest2;
import com.braintribe.gm.service.commons.test.model.ServiceRequest3;
import com.braintribe.gm.service.commons.test.model.ServiceRequest4;
import com.braintribe.gm.service.commons.test.model.UnknownServiceRequest;
import com.braintribe.gm.service.commons.test.processing.ServiceProcessor1;
import com.braintribe.gm.service.commons.test.processing.ServiceProcessor2;
import com.braintribe.gm.service.commons.test.processing.ServiceProcessor3;
import com.braintribe.model.processing.service.api.CompositeException;
import com.braintribe.model.processing.service.common.CompositeServiceProcessor;
import com.braintribe.model.service.api.CompositeRequest;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.result.CompositeResponse;

/**
 * <p>
 * {@link CompositeServiceProcessor} tests.
 * 
 */
public abstract class CompositeServiceProcessorTest extends CompositeServiceProcessorTestBase {

	public abstract boolean parallelize();

	@Test
	public void testNonCompositeService1AgainstCompositeAwareExpert() throws Exception {

		ServiceRequest request = ServiceRequest1.T.create();

		Object response = evaluate(request);

		Assert.assertNotNull(response);
		Assert.assertTrue(response instanceof String);

	}

	@Test
	public void testNonCompositeService2AgainstCompositeAwareExpert() throws Exception {

		String sessionId = validSessionId();

		ServiceRequest2 request = ServiceRequest2.T.create();
		request.setSessionId(sessionId);
		request.setValidateAuthorizationContext(true);

		Object response = evaluate(request);

		Assert.assertNotNull(response);
		Assert.assertTrue(response instanceof Long);

	}

	@Test
	public void testNonCompositeService3AgainstCompositeAwareExpert() throws Exception {

		String sessionId = validSessionId();

		ServiceRequest3 request = ServiceRequest3.T.create();
		request.setSessionId(sessionId);
		request.setValidateAuthorizationContext(true);

		Object response = evaluate(request);

		Assert.assertNotNull(response);
		Assert.assertTrue(response instanceof Integer);

	}

	@Test
	public void testRequest() throws Exception {

		String sessionId = validSessionId();

		CompositeRequest request = CompositeRequest.T.create();
		request.setSessionId(sessionId);
		request.setParallelize(parallelize());

		ServiceRequest1 request1 = ServiceRequest1.T.create();
		ServiceRequest2 request2 = ServiceRequest2.T.create();
		ServiceRequest3 request3 = ServiceRequest3.T.create();

		request.getRequests().add(request1);
		request.getRequests().add(request2);
		request.getRequests().add(request3);

		CompositeResponse response = evaluate(request);

		assertSuccess(response, ServiceProcessor1.RETURN, ServiceProcessor2.RETURN, ServiceProcessor3.RETURN);

		Assert.assertEquals(sessionId, request2.getSessionId());
		Assert.assertEquals(sessionId, request3.getSessionId());

	}

	@Test
	public void testRequestContinuingOnError() throws Exception {

		CompositeRequest request = createCompositeRequest();
		request.setContinueOnFailure(true);

		ServiceRequest1 request1a = ServiceRequest1.T.create();
		ServiceRequest2 request1b = ServiceRequest2.T.create();
		ServiceRequest3 request1c = ServiceRequest3.T.create();

		request.getRequests().add(request1a);
		request.getRequests().add(request1b);
		request.getRequests().add(request1c);

		CompositeResponse response = evaluate(request);

		assertSuccess(response, ServiceProcessor1.RETURN, ServiceProcessor2.RETURN, ServiceProcessor3.RETURN);

	}

	@Test
	public void testRequestWithFailureAmongResults() throws Throwable {

		CompositeRequest request = createCompositeRequest();

		ServiceRequest1 request1a = ServiceRequest1.T.create();
		ServiceRequest2 request1b = ServiceRequest2.T.create();
		ServiceRequest3 request1c = ServiceRequest3.T.create();

		request1b.setForceException(true);

		request.getRequests().add(request1a);
		request.getRequests().add(request1b);
		request.getRequests().add(request1c);

		evaluateAndAssertException(request, ServiceProcessor2.EXCEPTION_TYPE);

	}

	@Test
	public void testRequestWithFailureAmongResultsContinuingOnError() throws Throwable {

		CompositeRequest request = createCompositeRequest();
		request.setContinueOnFailure(true);

		ServiceRequest1 request1a = ServiceRequest1.T.create();
		ServiceRequest2 request1b = ServiceRequest2.T.create();
		ServiceRequest3 request1c = ServiceRequest3.T.create();

		request1b.setForceException(true);

		request.getRequests().add(request1a);
		request.getRequests().add(request1b);
		request.getRequests().add(request1c);

		CompositeResponse response = evaluate(request);

		assertComposite(response, 3);

		assertSuccess(response, 0, ServiceProcessor1.RETURN);
		assertFailure(response, 1, ServiceProcessor2.EXCEPTION_TYPE);
		assertSuccess(response, 2, ServiceProcessor3.RETURN);

	}

	@Test
	public void testRequestWithFailureOnFirst() throws Exception {

		CompositeRequest request = createCompositeRequest();

		ServiceRequest1 request1a = ServiceRequest1.T.create();
		ServiceRequest2 request1b = ServiceRequest2.T.create();
		ServiceRequest3 request1c = ServiceRequest3.T.create();

		request1a.setForceException(true);

		request.getRequests().add(request1a);
		request.getRequests().add(request1b);
		request.getRequests().add(request1c);

		evaluateAndAssertException(request, ServiceProcessor1.EXCEPTION_TYPE);

	}

	@Test
	public void testRequestWithFailureOnFirstContinuingOnError() throws Exception {

		CompositeRequest request = createCompositeRequest();
		request.setContinueOnFailure(true);

		ServiceRequest1 request1a = ServiceRequest1.T.create();
		ServiceRequest2 request1b = ServiceRequest2.T.create();
		ServiceRequest3 request1c = ServiceRequest3.T.create();

		request1a.setForceException(true);

		request.getRequests().add(request1a);
		request.getRequests().add(request1b);
		request.getRequests().add(request1c);

		CompositeResponse response = evaluate(request);

		assertComposite(response, 3);

		assertFailure(response, 0, ServiceProcessor1.EXCEPTION_TYPE);
		assertSuccess(response, 1, ServiceProcessor2.RETURN);
		assertSuccess(response, 2, ServiceProcessor3.RETURN);

	}

	@Test
	public void testRequestWithMultipleFailures() throws Exception {

		CompositeRequest request = createCompositeRequest();

		ServiceRequest1 request1a = ServiceRequest1.T.create();
		ServiceRequest2 request1b = ServiceRequest2.T.create();
		ServiceRequest3 request1c = ServiceRequest3.T.create();

		request1a.setForceException(true);
		request1c.setForceException(true);

		request.getRequests().add(request1a);
		request.getRequests().add(request1b);
		request.getRequests().add(request1c);

		if (parallelize()) {
			evaluateAndAssertExceptions(request, ServiceProcessor1.EXCEPTION_TYPE, ServiceProcessor3.EXCEPTION_TYPE);
		} else {
			evaluateAndAssertException(request, ServiceProcessor1.EXCEPTION_TYPE);
		}

	}

	@Test
	public void testRequestWithMultipleFailuresContinuingOnError() throws Exception {

		CompositeRequest request = createCompositeRequest();
		request.setContinueOnFailure(true);

		ServiceRequest1 request1a = ServiceRequest1.T.create();
		ServiceRequest2 request1b = ServiceRequest2.T.create();
		ServiceRequest3 request1c = ServiceRequest3.T.create();

		request1a.setForceException(true);
		request1c.setForceException(true);

		request.getRequests().add(request1a);
		request.getRequests().add(request1b);
		request.getRequests().add(request1c);

		CompositeResponse response = evaluate(request);

		assertComposite(response, 3);

		assertFailure(response, 0, ServiceProcessor1.EXCEPTION_TYPE);
		assertSuccess(response, 1, ServiceProcessor2.RETURN);
		assertFailure(response, 2, ServiceProcessor3.EXCEPTION_TYPE);

	}

	@Test
	public void testRequestWithOnlyFailures() throws Exception {

		CompositeRequest request = createCompositeRequest();

		ServiceRequest1 request1a = ServiceRequest1.T.create();
		ServiceRequest2 request1b = ServiceRequest2.T.create();
		ServiceRequest3 request1c = ServiceRequest3.T.create();

		request1a.setForceException(true);
		request1b.setForceException(true);
		request1c.setForceException(true);

		request.getRequests().add(request1a);
		request.getRequests().add(request1b);
		request.getRequests().add(request1c);

		if (parallelize()) {
			evaluateAndAssertExceptions(request, ServiceProcessor1.EXCEPTION_TYPE, ServiceProcessor2.EXCEPTION_TYPE, ServiceProcessor3.EXCEPTION_TYPE);
		} else {
			evaluateAndAssertException(request, ServiceProcessor1.EXCEPTION_TYPE);
		}

	}

	@Test
	public void testRequestWithOnlyFailuresContinuingOnError() throws Exception {

		CompositeRequest request = createCompositeRequest();
		request.setContinueOnFailure(true);

		ServiceRequest1 request1a = ServiceRequest1.T.create();
		ServiceRequest2 request1b = ServiceRequest2.T.create();
		ServiceRequest3 request1c = ServiceRequest3.T.create();

		request1a.setForceException(true);
		request1b.setForceException(true);
		request1c.setForceException(true);

		request.getRequests().add(request1a);
		request.getRequests().add(request1b);
		request.getRequests().add(request1c);

		CompositeResponse response = evaluate(request);

		assertComposite(response, 3);

		assertFailure(response, 0, ServiceProcessor1.EXCEPTION_TYPE);
		assertFailure(response, 1, ServiceProcessor2.EXCEPTION_TYPE);
		assertFailure(response, 2, ServiceProcessor3.EXCEPTION_TYPE);

	}

	@Test
	public void testAuthorizingRequestsWithDistinctSessions() throws Exception {

		String sessionId1 = validSessionId();
		String sessionId2 = validSessionId();
		String sessionId3 = validSessionId();

		CompositeRequest request = CompositeRequest.T.create();
		request.setParallelize(parallelize());
		request.setSessionId(sessionId1);

		ServiceRequest1 request1 = ServiceRequest1.T.create();
		ServiceRequest2 request2 = ServiceRequest2.T.create();
		ServiceRequest3 request3 = ServiceRequest3.T.create();

		request2.setValidateAuthorizationContext(true);
		request3.setValidateAuthorizationContext(true);

		request2.setSessionId(sessionId2);
		request3.setSessionId(sessionId3);

		request.getRequests().add(request1);
		request.getRequests().add(request2);
		request.getRequests().add(request3);

		CompositeResponse response = evaluate(request);

		assertComposite(response, 3);
		assertSuccess(response, 0, String.class);
		assertSuccess(response, 1, Long.class);
		assertSuccess(response, 2, Integer.class);

		Assert.assertEquals(sessionId1, request.getSessionId());
		Assert.assertEquals(sessionId2, request2.getSessionId());
		Assert.assertEquals(sessionId3, request3.getSessionId());

	}

	@Test
	public void testNestedRequest() throws Exception {

		CompositeRequest root = createCompositeRequest();

		ServiceRequest1 in_0 = ServiceRequest1.T.create();
		ServiceRequest2 in_1 = ServiceRequest2.T.create();
		ServiceRequest3 in_2 = ServiceRequest3.T.create();
		ServiceRequest3 in_3 = ServiceRequest3.T.create();
		CompositeRequest in_4 = CompositeRequest.T.create();
		CompositeRequest in_5 = CompositeRequest.T.create();
		in_1.setValidateAuthorizationContext(true);
		in_2.setValidateAuthorizationContext(true);
		in_3.setValidateAuthorizationContext(true);
		in_4.setParallelize(parallelize());
		in_5.setParallelize(parallelize());

		ServiceRequest1 in_4_0 = ServiceRequest1.T.create();
		ServiceRequest2 in_4_1 = ServiceRequest2.T.create();
		ServiceRequest3 in_5_0 = ServiceRequest3.T.create();
		ServiceRequest3 in_5_1 = ServiceRequest3.T.create();
		CompositeRequest in_4_2 = CompositeRequest.T.create();
		CompositeRequest in_5_2 = CompositeRequest.T.create();
		in_4_1.setValidateAuthorizationContext(true);
		in_5_0.setValidateAuthorizationContext(true);
		in_5_1.setValidateAuthorizationContext(true);
		in_4_2.setParallelize(parallelize());
		in_5_2.setParallelize(parallelize());

		ServiceRequest1 in_4_2_0 = ServiceRequest1.T.create();
		ServiceRequest2 in_4_2_1 = ServiceRequest2.T.create();
		ServiceRequest3 in_5_2_0 = ServiceRequest3.T.create();
		ServiceRequest3 in_5_2_1 = ServiceRequest3.T.create();
		CompositeRequest in_4_2_2 = CompositeRequest.T.create();
		CompositeRequest in_5_2_2 = CompositeRequest.T.create();
		in_4_2_1.setValidateAuthorizationContext(true);
		in_5_2_0.setValidateAuthorizationContext(true);
		in_5_2_1.setValidateAuthorizationContext(true);
		in_4_2_2.setParallelize(parallelize());
		in_5_2_2.setParallelize(parallelize());

		ServiceRequest1 in_4_2_2_0 = ServiceRequest1.T.create();
		ServiceRequest2 in_4_2_2_1 = ServiceRequest2.T.create();
		ServiceRequest3 in_5_2_2_0 = ServiceRequest3.T.create();
		ServiceRequest3 in_5_2_2_1 = ServiceRequest3.T.create();
		in_4_2_2_1.setValidateAuthorizationContext(true);
		in_5_2_2_0.setValidateAuthorizationContext(true);
		in_5_2_2_1.setValidateAuthorizationContext(true);

		root.getRequests().add(in_0);
		root.getRequests().add(in_1);
		root.getRequests().add(in_2);
		root.getRequests().add(in_3);
		root.getRequests().add(in_4);
		root.getRequests().add(in_5);

		in_4.getRequests().add(in_4_0);
		in_4.getRequests().add(in_4_1);
		in_4.getRequests().add(in_4_2);
		in_5.getRequests().add(in_5_0);
		in_5.getRequests().add(in_5_1);
		in_5.getRequests().add(in_5_2);

		in_4_2.getRequests().add(in_4_2_0);
		in_4_2.getRequests().add(in_4_2_1);
		in_4_2.getRequests().add(in_4_2_2);
		in_5_2.getRequests().add(in_5_2_0);
		in_5_2.getRequests().add(in_5_2_1);
		in_5_2.getRequests().add(in_5_2_2);

		in_4_2_2.getRequests().add(in_4_2_2_0);
		in_4_2_2.getRequests().add(in_4_2_2_1);
		in_5_2_2.getRequests().add(in_5_2_2_0);
		in_5_2_2.getRequests().add(in_5_2_2_1);

		Object responseObject = evaluate(root);

		CompositeResponse response = assertComposite(responseObject, 6);

		assertSuccess(response, 0, String.class);
		assertSuccess(response, 1, Long.class);
		assertSuccess(response, 2, Integer.class);
		assertSuccess(response, 3, Integer.class);
		CompositeResponse out_4 = assertComposite(response, 4, 3);
		CompositeResponse out_5 = assertComposite(response, 5, 3);

		assertSuccess(out_4, 0, String.class);
		assertSuccess(out_4, 1, Long.class);
		CompositeResponse out_4_2 = assertComposite(out_4, 2, 3);
		assertSuccess(out_5, 0, Integer.class);
		assertSuccess(out_5, 1, Integer.class);
		CompositeResponse out_5_2 = assertComposite(out_5, 2, 3);

		assertSuccess(out_4_2, 0, String.class);
		assertSuccess(out_4_2, 1, Long.class);
		CompositeResponse out_4_2_2 = assertComposite(out_4_2, 2, 2);
		assertSuccess(out_5_2, 0, Integer.class);
		assertSuccess(out_5_2, 1, Integer.class);
		CompositeResponse out_5_2_2 = assertComposite(out_5_2, 2, 2);

		assertSuccess(out_4_2_2, 0, String.class);
		assertSuccess(out_4_2_2, 1, Long.class);
		assertSuccess(out_5_2_2, 0, Integer.class);
		assertSuccess(out_5_2_2, 1, Integer.class);

	}

	@Test
	public void testNestedRequestWithRequestsWithDistinctSessions() throws Exception {

		String sessionId = validSessionId();

		CompositeRequest root = CompositeRequest.T.create();
		root.setParallelize(parallelize());
		root.setSessionId(sessionId);

		ServiceRequest1 in_0 = ServiceRequest1.T.create();
		ServiceRequest2 in_1 = ServiceRequest2.T.create();
		ServiceRequest3 in_2 = ServiceRequest3.T.create();
		ServiceRequest3 in_3 = ServiceRequest3.T.create();
		CompositeRequest in_4 = CompositeRequest.T.create();
		CompositeRequest in_5 = CompositeRequest.T.create();
		in_4.setParallelize(parallelize());
		in_5.setParallelize(parallelize());

		ServiceRequest1 in_4_0 = ServiceRequest1.T.create();
		ServiceRequest2 in_4_1 = ServiceRequest2.T.create();
		ServiceRequest3 in_5_0 = ServiceRequest3.T.create();
		ServiceRequest3 in_5_1 = ServiceRequest3.T.create();
		CompositeRequest in_4_2 = CompositeRequest.T.create();
		CompositeRequest in_5_2 = CompositeRequest.T.create();
		in_4_2.setParallelize(parallelize());
		in_5_2.setParallelize(parallelize());

		ServiceRequest1 in_4_2_0 = ServiceRequest1.T.create();
		ServiceRequest2 in_4_2_1 = ServiceRequest2.T.create();
		ServiceRequest3 in_5_2_0 = ServiceRequest3.T.create();
		ServiceRequest3 in_5_2_1 = ServiceRequest3.T.create();
		CompositeRequest in_4_2_2 = CompositeRequest.T.create();
		CompositeRequest in_5_2_2 = CompositeRequest.T.create();
		in_4_2_2.setParallelize(parallelize());
		in_5_2_2.setParallelize(parallelize());

		ServiceRequest1 in_4_2_2_0 = ServiceRequest1.T.create();
		ServiceRequest2 in_4_2_2_1 = ServiceRequest2.T.create();
		ServiceRequest3 in_5_2_2_0 = ServiceRequest3.T.create();
		ServiceRequest3 in_5_2_2_1 = ServiceRequest3.T.create();

		root.getRequests().add(in_0);
		root.getRequests().add(in_1);
		root.getRequests().add(in_2);
		root.getRequests().add(in_3);
		root.getRequests().add(in_4);
		root.getRequests().add(in_5);

		in_4.getRequests().add(in_4_0);
		in_4.getRequests().add(in_4_1);
		in_4.getRequests().add(in_4_2);
		in_5.getRequests().add(in_5_0);
		in_5.getRequests().add(in_5_1);
		in_5.getRequests().add(in_5_2);

		in_4_2.getRequests().add(in_4_2_0);
		in_4_2.getRequests().add(in_4_2_1);
		in_4_2.getRequests().add(in_4_2_2);
		in_5_2.getRequests().add(in_5_2_0);
		in_5_2.getRequests().add(in_5_2_1);
		in_5_2.getRequests().add(in_5_2_2);

		in_4_2_2.getRequests().add(in_4_2_2_0);
		in_4_2_2.getRequests().add(in_4_2_2_1);
		in_5_2_2.getRequests().add(in_5_2_2_0);
		in_5_2_2.getRequests().add(in_5_2_2_1);

		String sessionIdin_1 = validSessionId();
		String sessionIdin_5_0 = validSessionId();
		String sessionIdin_5_2_1 = validSessionId();
		in_1.setSessionId(sessionIdin_1);
		in_5_0.setSessionId(sessionIdin_5_0);
		in_5_2_1.setSessionId(sessionIdin_5_2_1);
		in_1.setValidateAuthorizationContext(true);
		in_5_0.setValidateAuthorizationContext(true);
		in_5_2_1.setValidateAuthorizationContext(true);

		Object responseObject = evaluate(root);

		CompositeResponse response = assertComposite(responseObject, 6);

		assertSuccess(response, 0, String.class);
		assertSuccess(response, 1, Long.class);
		assertSuccess(response, 2, Integer.class);
		assertSuccess(response, 3, Integer.class);
		CompositeResponse out_4 = assertComposite(response, 4, 3);
		CompositeResponse out_5 = assertComposite(response, 5, 3);

		assertSuccess(out_4, 0, String.class);
		assertSuccess(out_4, 1, Long.class);
		CompositeResponse out_4_2 = assertComposite(out_4, 2, 3);
		assertSuccess(out_5, 0, Integer.class);
		assertSuccess(out_5, 1, Integer.class);
		CompositeResponse out_5_2 = assertComposite(out_5, 2, 3);

		assertSuccess(out_4_2, 0, String.class);
		assertSuccess(out_4_2, 1, Long.class);
		CompositeResponse out_4_2_2 = assertComposite(out_4_2, 2, 2);
		assertSuccess(out_5_2, 0, Integer.class);
		assertSuccess(out_5_2, 1, Integer.class);
		CompositeResponse out_5_2_2 = assertComposite(out_5_2, 2, 2);

		assertSuccess(out_4_2_2, 0, String.class);
		assertSuccess(out_4_2_2, 1, Long.class);
		assertSuccess(out_5_2_2, 0, Integer.class);
		assertSuccess(out_5_2_2, 1, Integer.class);

		Assert.assertEquals(sessionIdin_1, in_1.getSessionId());
		Assert.assertEquals(sessionIdin_5_0, in_5_0.getSessionId());
		Assert.assertEquals(sessionIdin_5_2_1, in_5_2_1.getSessionId());

	}

	@Test
	public void testNestedRequestWithFailureAmongResultsOnFirstLevel() throws Exception {

		String sessionId = validSessionId();

		CompositeRequest root = CompositeRequest.T.create();
		root.setParallelize(parallelize());
		root.setSessionId(sessionId);

		ServiceRequest1 in_0 = ServiceRequest1.T.create();
		ServiceRequest2 in_1 = ServiceRequest2.T.create();
		ServiceRequest3 in_2 = ServiceRequest3.T.create();
		ServiceRequest3 in_3 = ServiceRequest3.T.create();
		CompositeRequest in_4 = CompositeRequest.T.create();
		CompositeRequest in_5 = CompositeRequest.T.create();
		in_4.setParallelize(parallelize());
		in_5.setParallelize(parallelize());

		ServiceRequest1 in_4_0 = ServiceRequest1.T.create();
		ServiceRequest2 in_4_1 = ServiceRequest2.T.create();
		ServiceRequest3 in_5_0 = ServiceRequest3.T.create();
		ServiceRequest3 in_5_1 = ServiceRequest3.T.create();

		root.getRequests().add(in_0);
		root.getRequests().add(in_1);
		root.getRequests().add(in_2);
		root.getRequests().add(in_3);
		root.getRequests().add(in_4);
		root.getRequests().add(in_5);

		in_4.getRequests().add(in_4_0);
		in_4.getRequests().add(in_4_1);
		in_5.getRequests().add(in_5_0);
		in_5.getRequests().add(in_5_1);

		in_3.setForceException(true);

		evaluateAndAssertException(root, ServiceProcessor3.EXCEPTION_TYPE);

	}

	@Test
	public void testNestedRequestWithFailureAmongResultsOnFirstLevelContinuingOnError() throws Exception {

		String sessionId = validSessionId();

		CompositeRequest root = CompositeRequest.T.create();
		root.setParallelize(parallelize());
		root.setSessionId(sessionId);
		root.setContinueOnFailure(true);

		ServiceRequest1 in_0 = ServiceRequest1.T.create();
		ServiceRequest2 in_1 = ServiceRequest2.T.create();
		ServiceRequest3 in_2 = ServiceRequest3.T.create();
		ServiceRequest3 in_3 = ServiceRequest3.T.create();
		CompositeRequest in_4 = CompositeRequest.T.create();
		CompositeRequest in_5 = CompositeRequest.T.create();
		in_4.setParallelize(parallelize());
		in_5.setParallelize(parallelize());

		ServiceRequest1 in_4_0 = ServiceRequest1.T.create();
		ServiceRequest2 in_4_1 = ServiceRequest2.T.create();
		ServiceRequest3 in_5_0 = ServiceRequest3.T.create();
		ServiceRequest3 in_5_1 = ServiceRequest3.T.create();
		CompositeRequest in_4_2 = CompositeRequest.T.create();
		CompositeRequest in_5_2 = CompositeRequest.T.create();
		in_4_2.setParallelize(parallelize());
		in_5_2.setParallelize(parallelize());

		ServiceRequest1 in_4_2_0 = ServiceRequest1.T.create();
		ServiceRequest2 in_4_2_1 = ServiceRequest2.T.create();
		ServiceRequest3 in_5_2_0 = ServiceRequest3.T.create();
		ServiceRequest3 in_5_2_1 = ServiceRequest3.T.create();
		CompositeRequest in_4_2_2 = CompositeRequest.T.create();
		CompositeRequest in_5_2_2 = CompositeRequest.T.create();
		in_4_2_2.setParallelize(parallelize());
		in_5_2_2.setParallelize(parallelize());

		ServiceRequest1 in_4_2_2_0 = ServiceRequest1.T.create();
		ServiceRequest2 in_4_2_2_1 = ServiceRequest2.T.create();
		ServiceRequest3 in_5_2_2_0 = ServiceRequest3.T.create();
		ServiceRequest3 in_5_2_2_1 = ServiceRequest3.T.create();

		root.getRequests().add(in_0);
		root.getRequests().add(in_1);
		root.getRequests().add(in_2);
		root.getRequests().add(in_3);
		root.getRequests().add(in_4);
		root.getRequests().add(in_5);

		in_4.getRequests().add(in_4_0);
		in_4.getRequests().add(in_4_1);
		in_4.getRequests().add(in_4_2);
		in_5.getRequests().add(in_5_0);
		in_5.getRequests().add(in_5_1);
		in_5.getRequests().add(in_5_2);

		in_4_2.getRequests().add(in_4_2_0);
		in_4_2.getRequests().add(in_4_2_1);
		in_4_2.getRequests().add(in_4_2_2);
		in_5_2.getRequests().add(in_5_2_0);
		in_5_2.getRequests().add(in_5_2_1);
		in_5_2.getRequests().add(in_5_2_2);

		in_4_2_2.getRequests().add(in_4_2_2_0);
		in_4_2_2.getRequests().add(in_4_2_2_1);
		in_5_2_2.getRequests().add(in_5_2_2_0);
		in_5_2_2.getRequests().add(in_5_2_2_1);

		in_1.setForceException(true);

		CompositeResponse result = (CompositeResponse) evaluate(root);

		assertComposite(result, 6);

		assertSuccess(result, 0, String.class);
		assertFailure(result, 1, Exception.class);
		assertSuccess(result, 2, Integer.class);
		assertSuccess(result, 3, Integer.class);

		CompositeResponse out_4 = assertComposite(result, 4, 3);
		CompositeResponse out_5 = assertComposite(result, 5, 3);

		assertSuccess(out_4, 0, String.class);
		assertSuccess(out_4, 1, Long.class);
		CompositeResponse out_4_2 = assertComposite(out_4, 2, 3);

		assertSuccess(out_5, 0, Integer.class);
		assertSuccess(out_5, 1, Integer.class);
		CompositeResponse out_5_2 = assertComposite(out_5, 2, 3);

		assertSuccess(out_4_2, 0, String.class);
		assertSuccess(out_4_2, 1, Long.class);
		CompositeResponse out_4_2_2 = assertComposite(out_4_2, 2, 2);

		assertSuccess(out_5_2, 0, Integer.class);
		assertSuccess(out_5_2, 1, Integer.class);
		CompositeResponse out_5_2_2 = assertComposite(out_5_2, 2, 2);

		assertSuccess(out_4_2_2, 0, String.class);
		assertSuccess(out_4_2_2, 1, Long.class);

		assertSuccess(out_5_2_2, 0, Integer.class);
		assertSuccess(out_5_2_2, 1, Integer.class);

	}

	@Test
	public void testNestedRequestWithFailureAmongResultsOnSecondLevel() throws Exception {

		String sessionId = validSessionId();

		CompositeRequest root = CompositeRequest.T.create();
		root.setParallelize(parallelize());
		root.setSessionId(sessionId);

		ServiceRequest1 in_0 = ServiceRequest1.T.create();
		ServiceRequest2 in_1 = ServiceRequest2.T.create();
		ServiceRequest3 in_2 = ServiceRequest3.T.create();
		ServiceRequest3 in_3 = ServiceRequest3.T.create();
		CompositeRequest in_4 = CompositeRequest.T.create();
		CompositeRequest in_5 = CompositeRequest.T.create();
		in_4.setParallelize(parallelize());
		in_5.setParallelize(parallelize());

		ServiceRequest1 in_4_0 = ServiceRequest1.T.create();
		ServiceRequest2 in_4_1 = ServiceRequest2.T.create();
		ServiceRequest3 in_5_0 = ServiceRequest3.T.create();
		ServiceRequest3 in_5_1 = ServiceRequest3.T.create();
		CompositeRequest in_4_2 = CompositeRequest.T.create();
		CompositeRequest in_5_2 = CompositeRequest.T.create();
		in_4_2.setParallelize(parallelize());
		in_5_2.setParallelize(parallelize());

		ServiceRequest1 in_4_2_0 = ServiceRequest1.T.create();
		ServiceRequest2 in_4_2_1 = ServiceRequest2.T.create();
		ServiceRequest3 in_5_2_0 = ServiceRequest3.T.create();
		ServiceRequest3 in_5_2_1 = ServiceRequest3.T.create();
		CompositeRequest in_4_2_2 = CompositeRequest.T.create();
		CompositeRequest in_5_2_2 = CompositeRequest.T.create();
		in_4_2_2.setParallelize(parallelize());
		in_5_2_2.setParallelize(parallelize());

		ServiceRequest1 in_4_2_2_0 = ServiceRequest1.T.create();
		ServiceRequest2 in_4_2_2_1 = ServiceRequest2.T.create();
		ServiceRequest3 in_5_2_2_0 = ServiceRequest3.T.create();
		ServiceRequest3 in_5_2_2_1 = ServiceRequest3.T.create();

		root.getRequests().add(in_0);
		root.getRequests().add(in_1);
		root.getRequests().add(in_2);
		root.getRequests().add(in_3);
		root.getRequests().add(in_4);
		root.getRequests().add(in_5);

		in_4.getRequests().add(in_4_0);
		in_4.getRequests().add(in_4_1);
		in_4.getRequests().add(in_4_2);
		in_5.getRequests().add(in_5_0);
		in_5.getRequests().add(in_5_1);
		in_5.getRequests().add(in_5_2);

		in_4_2.getRequests().add(in_4_2_0);
		in_4_2.getRequests().add(in_4_2_1);
		in_4_2.getRequests().add(in_4_2_2);
		in_5_2.getRequests().add(in_5_2_0);
		in_5_2.getRequests().add(in_5_2_1);
		in_5_2.getRequests().add(in_5_2_2);

		in_4_2_2.getRequests().add(in_4_2_2_0);
		in_4_2_2.getRequests().add(in_4_2_2_1);
		in_5_2_2.getRequests().add(in_5_2_2_0);
		in_5_2_2.getRequests().add(in_5_2_2_1);

		in_4_1.setForceException(true);

		evaluateAndAssertException(root, ServiceProcessor2.EXCEPTION_TYPE);

	}

	@Test
	public void testNestedRequestWithFailureAmongResultsOnSecondLevelContinuingOnError() throws Exception {

		String sessionId = validSessionId();

		CompositeRequest root = CompositeRequest.T.create();
		root.setParallelize(parallelize());
		root.setSessionId(sessionId);
		root.setContinueOnFailure(true);

		ServiceRequest1 in_0 = ServiceRequest1.T.create();
		ServiceRequest2 in_1 = ServiceRequest2.T.create();
		ServiceRequest3 in_2 = ServiceRequest3.T.create();
		ServiceRequest3 in_3 = ServiceRequest3.T.create();
		CompositeRequest in_4 = CompositeRequest.T.create();
		CompositeRequest in_5 = CompositeRequest.T.create();
		in_4.setParallelize(parallelize());
		in_5.setParallelize(parallelize());
		in_4.setContinueOnFailure(true);

		ServiceRequest1 in_4_0 = ServiceRequest1.T.create();
		ServiceRequest2 in_4_1 = ServiceRequest2.T.create();
		ServiceRequest3 in_5_0 = ServiceRequest3.T.create();
		ServiceRequest3 in_5_1 = ServiceRequest3.T.create();
		CompositeRequest in_4_2 = CompositeRequest.T.create();
		CompositeRequest in_5_2 = CompositeRequest.T.create();
		in_4_2.setParallelize(parallelize());
		in_5_2.setParallelize(parallelize());

		ServiceRequest1 in_4_2_0 = ServiceRequest1.T.create();
		ServiceRequest2 in_4_2_1 = ServiceRequest2.T.create();
		ServiceRequest3 in_5_2_0 = ServiceRequest3.T.create();
		ServiceRequest3 in_5_2_1 = ServiceRequest3.T.create();
		CompositeRequest in_4_2_2 = CompositeRequest.T.create();
		CompositeRequest in_5_2_2 = CompositeRequest.T.create();
		in_4_2_2.setParallelize(parallelize());
		in_5_2_2.setParallelize(parallelize());

		ServiceRequest1 in_4_2_2_0 = ServiceRequest1.T.create();
		ServiceRequest2 in_4_2_2_1 = ServiceRequest2.T.create();
		ServiceRequest3 in_5_2_2_0 = ServiceRequest3.T.create();
		ServiceRequest3 in_5_2_2_1 = ServiceRequest3.T.create();

		root.getRequests().add(in_0);
		root.getRequests().add(in_1);
		root.getRequests().add(in_2);
		root.getRequests().add(in_3);
		root.getRequests().add(in_4);
		root.getRequests().add(in_5);

		in_4.getRequests().add(in_4_0);
		in_4.getRequests().add(in_4_1);
		in_4.getRequests().add(in_4_2);
		in_5.getRequests().add(in_5_0);
		in_5.getRequests().add(in_5_1);
		in_5.getRequests().add(in_5_2);

		in_4_2.getRequests().add(in_4_2_0);
		in_4_2.getRequests().add(in_4_2_1);
		in_4_2.getRequests().add(in_4_2_2);
		in_5_2.getRequests().add(in_5_2_0);
		in_5_2.getRequests().add(in_5_2_1);
		in_5_2.getRequests().add(in_5_2_2);

		in_4_2_2.getRequests().add(in_4_2_2_0);
		in_4_2_2.getRequests().add(in_4_2_2_1);
		in_5_2_2.getRequests().add(in_5_2_2_0);
		in_5_2_2.getRequests().add(in_5_2_2_1);

		in_4_1.setForceException(true);

		CompositeResponse result = (CompositeResponse) evaluate(root);

		assertComposite(result, 6);

		assertSuccess(result, 0, String.class);
		assertSuccess(result, 1, Long.class);
		assertSuccess(result, 2, Integer.class);
		assertSuccess(result, 3, Integer.class);

		CompositeResponse out_4 = assertComposite(result, 4, 3);
		CompositeResponse out_5 = assertComposite(result, 5, 3);

		assertSuccess(out_4, 0, String.class);
		assertFailure(out_4, 1, ServiceProcessor2.EXCEPTION_TYPE);
		CompositeResponse out_4_2 = assertComposite(out_4, 2, 3);

		assertSuccess(out_5, 0, Integer.class);
		assertSuccess(out_5, 1, Integer.class);
		CompositeResponse out_5_2 = assertComposite(out_5, 2, 3);

		assertSuccess(out_4_2, 0, String.class);
		assertSuccess(out_4_2, 1, Long.class);
		CompositeResponse out_4_2_2 = assertComposite(out_4_2, 2, 2);

		assertSuccess(out_5_2, 0, Integer.class);
		assertSuccess(out_5_2, 1, Integer.class);
		CompositeResponse out_5_2_2 = assertComposite(out_5_2, 2, 2);

		assertSuccess(out_4_2_2, 0, String.class);
		assertSuccess(out_4_2_2, 1, Long.class);

		assertSuccess(out_5_2_2, 0, Integer.class);
		assertSuccess(out_5_2_2, 1, Integer.class);

	}

	@Test
	public void testNestedRequestWithFailureAmongResultsOnThirdLevel() throws Exception {

		String sessionId = validSessionId();

		CompositeRequest root = CompositeRequest.T.create();
		root.setParallelize(parallelize());
		root.setSessionId(sessionId);

		ServiceRequest1 in_0 = ServiceRequest1.T.create();
		ServiceRequest2 in_1 = ServiceRequest2.T.create();
		ServiceRequest3 in_2 = ServiceRequest3.T.create();
		ServiceRequest3 in_3 = ServiceRequest3.T.create();
		CompositeRequest in_4 = CompositeRequest.T.create();
		CompositeRequest in_5 = CompositeRequest.T.create();
		in_4.setParallelize(parallelize());
		in_5.setParallelize(parallelize());

		ServiceRequest1 in_4_0 = ServiceRequest1.T.create();
		ServiceRequest2 in_4_1 = ServiceRequest2.T.create();
		CompositeRequest in_4_2 = CompositeRequest.T.create();
		in_4_2.setParallelize(parallelize());

		ServiceRequest3 in_5_0 = ServiceRequest3.T.create();
		ServiceRequest3 in_5_1 = ServiceRequest3.T.create();
		CompositeRequest in_5_2 = CompositeRequest.T.create();
		in_5_2.setParallelize(parallelize());

		ServiceRequest1 in_4_2_0 = ServiceRequest1.T.create();
		ServiceRequest2 in_4_2_1 = ServiceRequest2.T.create();
		CompositeRequest in_4_2_2 = CompositeRequest.T.create();
		in_4_2_2.setParallelize(parallelize());

		ServiceRequest3 in_5_2_0 = ServiceRequest3.T.create();
		ServiceRequest3 in_5_2_1 = ServiceRequest3.T.create();
		CompositeRequest in_5_2_2 = CompositeRequest.T.create();
		in_5_2_2.setParallelize(parallelize());

		ServiceRequest1 in_4_2_2_0 = ServiceRequest1.T.create();
		ServiceRequest2 in_4_2_2_1 = ServiceRequest2.T.create();
		ServiceRequest3 in_5_2_2_0 = ServiceRequest3.T.create();
		ServiceRequest3 in_5_2_2_1 = ServiceRequest3.T.create();

		root.getRequests().add(in_0);
		root.getRequests().add(in_1);
		root.getRequests().add(in_2);
		root.getRequests().add(in_3);
		root.getRequests().add(in_4);
		root.getRequests().add(in_5);

		in_4.getRequests().add(in_4_0);
		in_4.getRequests().add(in_4_1);
		in_4.getRequests().add(in_4_2);
		in_5.getRequests().add(in_5_0);
		in_5.getRequests().add(in_5_1);
		in_5.getRequests().add(in_5_2);

		in_4_2.getRequests().add(in_4_2_0);
		in_4_2.getRequests().add(in_4_2_1);
		in_4_2.getRequests().add(in_4_2_2);
		in_5_2.getRequests().add(in_5_2_0);
		in_5_2.getRequests().add(in_5_2_1);
		in_5_2.getRequests().add(in_5_2_2);

		in_4_2_2.getRequests().add(in_4_2_2_0);
		in_4_2_2.getRequests().add(in_4_2_2_1);
		in_5_2_2.getRequests().add(in_5_2_2_0);
		in_5_2_2.getRequests().add(in_5_2_2_1);

		in_4_2_1.setForceException(true);

		evaluateAndAssertException(root, ServiceProcessor2.EXCEPTION_TYPE);

	}

	@Test
	public void testNestedRequestWithFailureAmongResultsOnThirdLevelContinuingOnError() throws Exception {

		String sessionId = validSessionId();

		CompositeRequest root = CompositeRequest.T.create();
		root.setParallelize(parallelize());
		root.setSessionId(sessionId);
		root.setContinueOnFailure(true);

		ServiceRequest1 in_0 = ServiceRequest1.T.create();
		ServiceRequest2 in_1 = ServiceRequest2.T.create();
		ServiceRequest3 in_2 = ServiceRequest3.T.create();
		ServiceRequest3 in_3 = ServiceRequest3.T.create();
		CompositeRequest in_4 = CompositeRequest.T.create();
		CompositeRequest in_5 = CompositeRequest.T.create();
		in_4.setParallelize(parallelize());
		in_5.setParallelize(parallelize());
		in_4.setContinueOnFailure(true);

		ServiceRequest1 in_4_0 = ServiceRequest1.T.create();
		ServiceRequest2 in_4_1 = ServiceRequest2.T.create();
		CompositeRequest in_4_2 = CompositeRequest.T.create();
		in_4_2.setParallelize(parallelize());
		in_4_2.setContinueOnFailure(true);

		ServiceRequest3 in_5_0 = ServiceRequest3.T.create();
		ServiceRequest3 in_5_1 = ServiceRequest3.T.create();
		CompositeRequest in_5_2 = CompositeRequest.T.create();
		in_5_2.setParallelize(parallelize());

		ServiceRequest1 in_4_2_0 = ServiceRequest1.T.create();
		ServiceRequest2 in_4_2_1 = ServiceRequest2.T.create();
		CompositeRequest in_4_2_2 = CompositeRequest.T.create();
		in_4_2_2.setParallelize(parallelize());

		ServiceRequest3 in_5_2_0 = ServiceRequest3.T.create();
		ServiceRequest3 in_5_2_1 = ServiceRequest3.T.create();
		CompositeRequest in_5_2_2 = CompositeRequest.T.create();
		in_5_2_2.setParallelize(parallelize());

		ServiceRequest1 in_4_2_2_0 = ServiceRequest1.T.create();
		ServiceRequest2 in_4_2_2_1 = ServiceRequest2.T.create();
		ServiceRequest3 in_5_2_2_0 = ServiceRequest3.T.create();
		ServiceRequest3 in_5_2_2_1 = ServiceRequest3.T.create();

		root.getRequests().add(in_0);
		root.getRequests().add(in_1);
		root.getRequests().add(in_2);
		root.getRequests().add(in_3);
		root.getRequests().add(in_4);
		root.getRequests().add(in_5);

		in_4.getRequests().add(in_4_0);
		in_4.getRequests().add(in_4_1);
		in_4.getRequests().add(in_4_2);
		in_5.getRequests().add(in_5_0);
		in_5.getRequests().add(in_5_1);
		in_5.getRequests().add(in_5_2);

		in_4_2.getRequests().add(in_4_2_0);
		in_4_2.getRequests().add(in_4_2_1);
		in_4_2.getRequests().add(in_4_2_2);
		in_5_2.getRequests().add(in_5_2_0);
		in_5_2.getRequests().add(in_5_2_1);
		in_5_2.getRequests().add(in_5_2_2);

		in_4_2_2.getRequests().add(in_4_2_2_0);
		in_4_2_2.getRequests().add(in_4_2_2_1);
		in_5_2_2.getRequests().add(in_5_2_2_0);
		in_5_2_2.getRequests().add(in_5_2_2_1);

		in_4_2_1.setForceException(true);

		CompositeResponse result = (CompositeResponse) evaluate(root);

		assertComposite(result, 6);

		assertSuccess(result, 0, String.class);
		assertSuccess(result, 1, Long.class);
		assertSuccess(result, 2, Integer.class);
		assertSuccess(result, 3, Integer.class);

		CompositeResponse out_4 = assertComposite(result, 4, 3);
		CompositeResponse out_5 = assertComposite(result, 5, 3);

		assertSuccess(out_4, 0, String.class);
		assertSuccess(out_4, 1, Long.class);
		CompositeResponse out_4_2 = assertComposite(out_4, 2, 3);

		assertSuccess(out_5, 0, Integer.class);
		assertSuccess(out_5, 1, Integer.class);
		CompositeResponse out_5_2 = assertComposite(out_5, 2, 3);

		assertSuccess(out_4_2, 0, String.class);
		assertFailure(out_4_2, 1, ServiceProcessor2.EXCEPTION_TYPE);
		CompositeResponse out_4_2_2 = assertComposite(out_4_2, 2, 2);

		assertSuccess(out_5_2, 0, Integer.class);
		assertSuccess(out_5_2, 1, Integer.class);
		CompositeResponse out_5_2_2 = assertComposite(out_5_2, 2, 2);

		assertSuccess(out_4_2_2, 0, String.class);
		assertSuccess(out_4_2_2, 1, Long.class);

		assertSuccess(out_5_2_2, 0, Integer.class);
		assertSuccess(out_5_2_2, 1, Integer.class);

	}

	@Test
	public void testNestedRequestWithFailureAmongResultsOnThirdLevelWithMixedContinueOnErrorSettings() throws Exception {

		String sessionId = validSessionId();

		CompositeRequest root = CompositeRequest.T.create();
		root.setParallelize(parallelize());
		root.setSessionId(sessionId);
		root.setContinueOnFailure(true);

		ServiceRequest1 in_0 = ServiceRequest1.T.create();
		ServiceRequest2 in_1 = ServiceRequest2.T.create();
		ServiceRequest3 in_2 = ServiceRequest3.T.create();
		ServiceRequest3 in_3 = ServiceRequest3.T.create();
		CompositeRequest in_4 = CompositeRequest.T.create();
		CompositeRequest in_5 = CompositeRequest.T.create();
		in_4.setParallelize(parallelize());
		in_5.setParallelize(parallelize());

		ServiceRequest1 in_4_0 = ServiceRequest1.T.create();
		ServiceRequest2 in_4_1 = ServiceRequest2.T.create();
		CompositeRequest in_4_2 = CompositeRequest.T.create();
		in_4_2.setParallelize(parallelize());
		in_4_2.setContinueOnFailure(true);

		ServiceRequest3 in_5_0 = ServiceRequest3.T.create();
		ServiceRequest3 in_5_1 = ServiceRequest3.T.create();
		CompositeRequest in_5_2 = CompositeRequest.T.create();
		in_5_2.setParallelize(parallelize());
		in_5_2.setContinueOnFailure(true);

		ServiceRequest1 in_4_2_0 = ServiceRequest1.T.create();
		ServiceRequest2 in_4_2_1 = ServiceRequest2.T.create();
		CompositeRequest in_4_2_2 = CompositeRequest.T.create();
		in_4_2_2.setParallelize(parallelize());
		in_4_2_2.setContinueOnFailure(true);

		ServiceRequest3 in_5_2_0 = ServiceRequest3.T.create();
		ServiceRequest3 in_5_2_1 = ServiceRequest3.T.create();
		CompositeRequest in_5_2_2 = CompositeRequest.T.create();
		in_5_2_2.setParallelize(parallelize());
		in_5_2_2.setContinueOnFailure(true);

		ServiceRequest1 in_4_2_2_0 = ServiceRequest1.T.create();
		ServiceRequest2 in_4_2_2_1 = ServiceRequest2.T.create();
		ServiceRequest3 in_5_2_2_0 = ServiceRequest3.T.create();
		ServiceRequest3 in_5_2_2_1 = ServiceRequest3.T.create();

		root.getRequests().add(in_0);
		root.getRequests().add(in_1);
		root.getRequests().add(in_2);
		root.getRequests().add(in_3);
		root.getRequests().add(in_4);
		root.getRequests().add(in_5);

		in_4.getRequests().add(in_4_0);
		in_4.getRequests().add(in_4_1);
		in_4.getRequests().add(in_4_2);
		in_5.getRequests().add(in_5_0);
		in_5.getRequests().add(in_5_1);
		in_5.getRequests().add(in_5_2);

		in_4_2.getRequests().add(in_4_2_0);
		in_4_2.getRequests().add(in_4_2_1);
		in_4_2.getRequests().add(in_4_2_2);
		in_5_2.getRequests().add(in_5_2_0);
		in_5_2.getRequests().add(in_5_2_1);
		in_5_2.getRequests().add(in_5_2_2);

		in_4_2_2.getRequests().add(in_4_2_2_0);
		in_4_2_2.getRequests().add(in_4_2_2_1);
		in_5_2_2.getRequests().add(in_5_2_2_0);
		in_5_2_2.getRequests().add(in_5_2_2_1);

		in_4_2_1.setForceException(true);

		CompositeResponse result = (CompositeResponse) evaluate(root);

		assertComposite(result, 6);

		assertSuccess(result, 0, String.class);
		assertSuccess(result, 1, Long.class);
		assertSuccess(result, 2, Integer.class);
		assertSuccess(result, 3, Integer.class);

		CompositeResponse out_4 = assertComposite(result, 4, 3);
		CompositeResponse out_5 = assertComposite(result, 5, 3);

		assertSuccess(out_4, 0, String.class);
		assertSuccess(out_4, 1, Long.class);
		CompositeResponse out_4_2 = assertComposite(out_4, 2, 3);

		assertSuccess(out_5, 0, Integer.class);
		assertSuccess(out_5, 1, Integer.class);
		CompositeResponse out_5_2 = assertComposite(out_5, 2, 3);

		assertSuccess(out_4_2, 0, String.class);
		assertFailure(out_4_2, 1, ServiceProcessor2.EXCEPTION_TYPE);
		CompositeResponse out_4_2_2 = assertComposite(out_4_2, 2, 2);

		assertSuccess(out_5_2, 0, Integer.class);
		assertSuccess(out_5_2, 1, Integer.class);
		CompositeResponse out_5_2_2 = assertComposite(out_5_2, 2, 2);

		assertSuccess(out_4_2_2, 0, String.class);
		assertSuccess(out_4_2_2, 1, Long.class);

		assertSuccess(out_5_2_2, 0, Integer.class);
		assertSuccess(out_5_2_2, 1, Integer.class);

	}

	@Test
	public void testNestedRequestWithFailureAmongResultsOnAllLevels() throws Exception {

		String sessionId = validSessionId();

		CompositeRequest root = CompositeRequest.T.create();
		root.setParallelize(parallelize());
		root.setSessionId(sessionId);

		ServiceRequest1 in_0 = ServiceRequest1.T.create();
		ServiceRequest2 in_1 = ServiceRequest2.T.create();
		ServiceRequest3 in_2 = ServiceRequest3.T.create();
		ServiceRequest3 in_3 = ServiceRequest3.T.create();
		CompositeRequest in_4 = CompositeRequest.T.create();
		CompositeRequest in_5 = CompositeRequest.T.create();
		in_4.setParallelize(parallelize());
		in_5.setParallelize(parallelize());

		ServiceRequest1 in_4_0 = ServiceRequest1.T.create();
		ServiceRequest2 in_4_1 = ServiceRequest2.T.create();
		ServiceRequest3 in_5_0 = ServiceRequest3.T.create();
		ServiceRequest3 in_5_1 = ServiceRequest3.T.create();
		CompositeRequest in_4_2 = CompositeRequest.T.create();
		CompositeRequest in_5_2 = CompositeRequest.T.create();
		in_4_2.setParallelize(parallelize());
		in_5_2.setParallelize(parallelize());

		ServiceRequest1 in_4_2_0 = ServiceRequest1.T.create();
		ServiceRequest2 in_4_2_1 = ServiceRequest2.T.create();
		ServiceRequest3 in_5_2_0 = ServiceRequest3.T.create();
		ServiceRequest3 in_5_2_1 = ServiceRequest3.T.create();
		CompositeRequest in_4_2_2 = CompositeRequest.T.create();
		CompositeRequest in_5_2_2 = CompositeRequest.T.create();
		in_4_2_2.setParallelize(parallelize());
		in_5_2_2.setParallelize(parallelize());

		ServiceRequest1 in_4_2_2_0 = ServiceRequest1.T.create();
		ServiceRequest2 in_4_2_2_1 = ServiceRequest2.T.create();
		ServiceRequest3 in_5_2_2_0 = ServiceRequest3.T.create();
		ServiceRequest3 in_5_2_2_1 = ServiceRequest3.T.create();

		root.getRequests().add(in_0);
		root.getRequests().add(in_1);
		root.getRequests().add(in_2);
		root.getRequests().add(in_3);
		root.getRequests().add(in_4);
		root.getRequests().add(in_5);

		in_4.getRequests().add(in_4_0);
		in_4.getRequests().add(in_4_1);
		in_4.getRequests().add(in_4_2);
		in_5.getRequests().add(in_5_0);
		in_5.getRequests().add(in_5_1);
		in_5.getRequests().add(in_5_2);

		in_4_2.getRequests().add(in_4_2_0);
		in_4_2.getRequests().add(in_4_2_1);
		in_4_2.getRequests().add(in_4_2_2);
		in_5_2.getRequests().add(in_5_2_0);
		in_5_2.getRequests().add(in_5_2_1);
		in_5_2.getRequests().add(in_5_2_2);

		in_4_2_2.getRequests().add(in_4_2_2_0);
		in_4_2_2.getRequests().add(in_4_2_2_1);
		in_5_2_2.getRequests().add(in_5_2_2_0);
		in_5_2_2.getRequests().add(in_5_2_2_1);

		in_1.setForceException(true);
		in_4_1.setForceException(true);
		in_5_2_1.setForceException(true);
		in_5_2_2_0.setForceException(true);

		if (parallelize()) {
			evaluateAndAssertExceptions(root, ServiceProcessor2.EXCEPTION_TYPE, ServiceProcessor2.EXCEPTION_TYPE, CompositeException.class);
		} else {
			evaluateAndAssertException(root, ServiceProcessor2.EXCEPTION_TYPE);
		}

	}

	@Test
	public void testNestedRequestWithFailureAmongResultsOnAllLevelsContinuingOnError() throws Exception {

		String sessionId = validSessionId();

		CompositeRequest root = CompositeRequest.T.create();
		root.setParallelize(parallelize());
		root.setSessionId(sessionId);
		root.setContinueOnFailure(true);

		ServiceRequest1 in_0 = ServiceRequest1.T.create();
		ServiceRequest2 in_1 = ServiceRequest2.T.create();
		ServiceRequest3 in_2 = ServiceRequest3.T.create();
		ServiceRequest3 in_3 = ServiceRequest3.T.create();
		CompositeRequest in_4 = CompositeRequest.T.create();
		CompositeRequest in_5 = CompositeRequest.T.create();
		in_4.setParallelize(parallelize());
		in_5.setParallelize(parallelize());
		in_4.setContinueOnFailure(true);
		in_5.setContinueOnFailure(true);

		ServiceRequest1 in_4_0 = ServiceRequest1.T.create();
		ServiceRequest2 in_4_1 = ServiceRequest2.T.create();
		ServiceRequest3 in_5_0 = ServiceRequest3.T.create();
		ServiceRequest3 in_5_1 = ServiceRequest3.T.create();
		CompositeRequest in_4_2 = CompositeRequest.T.create();
		CompositeRequest in_5_2 = CompositeRequest.T.create();
		in_4_2.setParallelize(parallelize());
		in_5_2.setParallelize(parallelize());
		in_4_2.setContinueOnFailure(true);
		in_5_2.setContinueOnFailure(true);

		ServiceRequest1 in_4_2_0 = ServiceRequest1.T.create();
		ServiceRequest2 in_4_2_1 = ServiceRequest2.T.create();
		ServiceRequest3 in_5_2_0 = ServiceRequest3.T.create();
		ServiceRequest3 in_5_2_1 = ServiceRequest3.T.create();
		CompositeRequest in_4_2_2 = CompositeRequest.T.create();
		CompositeRequest in_5_2_2 = CompositeRequest.T.create();
		in_4_2_2.setParallelize(parallelize());
		in_5_2_2.setParallelize(parallelize());
		in_5_2_2.setContinueOnFailure(true);

		ServiceRequest1 in_4_2_2_0 = ServiceRequest1.T.create();
		ServiceRequest2 in_4_2_2_1 = ServiceRequest2.T.create();
		ServiceRequest3 in_5_2_2_0 = ServiceRequest3.T.create();
		ServiceRequest3 in_5_2_2_1 = ServiceRequest3.T.create();

		root.getRequests().add(in_0);
		root.getRequests().add(in_1);
		root.getRequests().add(in_2);
		root.getRequests().add(in_3);
		root.getRequests().add(in_4);
		root.getRequests().add(in_5);

		in_4.getRequests().add(in_4_0);
		in_4.getRequests().add(in_4_1);
		in_4.getRequests().add(in_4_2);
		in_5.getRequests().add(in_5_0);
		in_5.getRequests().add(in_5_1);
		in_5.getRequests().add(in_5_2);

		in_4_2.getRequests().add(in_4_2_0);
		in_4_2.getRequests().add(in_4_2_1);
		in_4_2.getRequests().add(in_4_2_2);
		in_5_2.getRequests().add(in_5_2_0);
		in_5_2.getRequests().add(in_5_2_1);
		in_5_2.getRequests().add(in_5_2_2);

		in_4_2_2.getRequests().add(in_4_2_2_0);
		in_4_2_2.getRequests().add(in_4_2_2_1);
		in_5_2_2.getRequests().add(in_5_2_2_0);
		in_5_2_2.getRequests().add(in_5_2_2_1);

		in_1.setForceException(true);
		in_4_1.setForceException(true);
		in_5_2_1.setForceException(true);
		in_5_2_2_0.setForceException(true);

		CompositeResponse result = (CompositeResponse) evaluate(root);

		assertComposite(result, 6);

		assertSuccess(result, 0, String.class);
		assertFailure(result, 1, ServiceProcessor2.EXCEPTION_TYPE);
		assertSuccess(result, 2, Integer.class);
		assertSuccess(result, 3, Integer.class);

		CompositeResponse out_4 = assertComposite(result, 4, 3);
		CompositeResponse out_5 = assertComposite(result, 5, 3);

		assertSuccess(out_4, 0, String.class);
		assertFailure(out_4, 1, ServiceProcessor2.EXCEPTION_TYPE);
		CompositeResponse out_4_2 = assertComposite(out_4, 2, 3);

		assertSuccess(out_5, 0, Integer.class);
		assertSuccess(out_5, 1, Integer.class);
		CompositeResponse out_5_2 = assertComposite(out_5, 2, 3);

		assertSuccess(out_4_2, 0, String.class);
		assertSuccess(out_4_2, 1, Long.class);
		CompositeResponse out_4_2_2 = assertComposite(out_4_2, 2, 2);

		assertSuccess(out_5_2, 0, Integer.class);
		assertFailure(out_5_2, 1, ServiceProcessor3.EXCEPTION_TYPE);
		CompositeResponse out_5_2_2 = assertComposite(out_5_2, 2, 2);

		assertSuccess(out_4_2_2, 0, String.class);
		assertSuccess(out_4_2_2, 1, Long.class);

		assertFailure(out_5_2_2, 0, ServiceProcessor3.EXCEPTION_TYPE);
		assertSuccess(out_5_2_2, 1, Integer.class);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testNestedRequestWithCircularReference() throws Exception {

		CompositeRequest root = createCompositeRequest();

		ServiceRequest1 in_0 = ServiceRequest1.T.create();
		ServiceRequest1 in_1 = ServiceRequest1.T.create();
		ServiceRequest1 in_2 = ServiceRequest1.T.create();
		ServiceRequest1 in_3 = ServiceRequest1.T.create();
		CompositeRequest in_4 = CompositeRequest.T.create();
		CompositeRequest in_5 = CompositeRequest.T.create();
		in_4.setParallelize(parallelize());
		in_5.setParallelize(parallelize());

		ServiceRequest1 in_4_0 = ServiceRequest1.T.create();
		ServiceRequest1 in_4_1 = ServiceRequest1.T.create();
		ServiceRequest1 in_5_0 = ServiceRequest1.T.create();
		ServiceRequest1 in_5_1 = ServiceRequest1.T.create();
		CompositeRequest in_4_2 = CompositeRequest.T.create();
		CompositeRequest in_5_2 = CompositeRequest.T.create();
		in_4_2.setParallelize(parallelize());
		in_5_2.setParallelize(parallelize());

		ServiceRequest1 in_4_2_0 = ServiceRequest1.T.create();
		ServiceRequest1 in_4_2_1 = ServiceRequest1.T.create();
		ServiceRequest1 in_5_2_0 = ServiceRequest1.T.create();
		ServiceRequest1 in_5_2_1 = ServiceRequest1.T.create();
		CompositeRequest in_4_2_2 = CompositeRequest.T.create();
		CompositeRequest in_5_2_2 = CompositeRequest.T.create();
		in_4_2_2.setParallelize(parallelize());
		in_5_2_2.setParallelize(parallelize());

		ServiceRequest1 in_4_2_2_0 = ServiceRequest1.T.create();
		ServiceRequest1 in_4_2_2_1 = ServiceRequest1.T.create();
		ServiceRequest1 in_5_2_2_0 = ServiceRequest1.T.create();
		ServiceRequest1 in_5_2_2_1 = ServiceRequest1.T.create();

		root.getRequests().add(in_0);
		root.getRequests().add(in_1);
		root.getRequests().add(in_2);
		root.getRequests().add(in_3);
		root.getRequests().add(in_4);
		root.getRequests().add(in_5);

		in_4.getRequests().add(in_4_0);
		in_4.getRequests().add(in_4_1);
		in_4.getRequests().add(in_4_2);
		in_5.getRequests().add(in_5_0);
		in_5.getRequests().add(in_5_1);
		in_5.getRequests().add(in_5_2);

		in_4_2.getRequests().add(in_4_2_0);
		in_4_2.getRequests().add(in_4_2_1);
		in_4_2.getRequests().add(in_4_2_2);
		in_5_2.getRequests().add(in_5_2_0);
		in_5_2.getRequests().add(in_5_2_1);
		in_5_2.getRequests().add(in_5_2_2);

		in_4_2_2.getRequests().add(in_4_2_2_0);
		in_4_2_2.getRequests().add(in_4_2_2_1);
		in_5_2_2.getRequests().add(in_5_2_2_0);
		in_5_2_2.getRequests().add(in_5_2_2_1);

		// Circular references

		in_4_2.getRequests().add(in_5);
		in_5_2.getRequests().add(in_4);

		in_5_2_2.getRequests().add(root);
		in_5_2_2.getRequests().add(in_4);
		in_5_2_2.getRequests().add(in_4_2);
		in_5_2_2.getRequests().add(in_5);
		in_5_2_2.getRequests().add(in_5_2);

		in_4_2_2.getRequests().add(root);
		in_4_2_2.getRequests().add(in_4);
		in_4_2_2.getRequests().add(in_4_2);
		in_4_2_2.getRequests().add(in_5);
		in_4_2_2.getRequests().add(in_5_2);

		evaluate(root);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testNestedRequestWithCircularReferenceSimple() throws Exception {

		CompositeRequest root = createCompositeRequest();

		ServiceRequest1 in_0 = ServiceRequest1.T.create();
		ServiceRequest1 in_1 = ServiceRequest1.T.create();
		ServiceRequest1 in_2 = ServiceRequest1.T.create();
		ServiceRequest1 in_3 = ServiceRequest1.T.create();
		CompositeRequest in_4 = CompositeRequest.T.create();
		CompositeRequest in_5 = CompositeRequest.T.create();
		in_4.setParallelize(parallelize());
		in_5.setParallelize(parallelize());

		ServiceRequest1 in_4_0 = ServiceRequest1.T.create();
		ServiceRequest1 in_4_1 = ServiceRequest1.T.create();
		ServiceRequest1 in_5_0 = ServiceRequest1.T.create();
		ServiceRequest1 in_5_1 = ServiceRequest1.T.create();
		CompositeRequest in_4_2 = CompositeRequest.T.create();
		CompositeRequest in_5_2 = CompositeRequest.T.create();
		in_4_2.setParallelize(parallelize());
		in_5_2.setParallelize(parallelize());

		ServiceRequest1 in_4_2_0 = ServiceRequest1.T.create();
		ServiceRequest1 in_4_2_1 = ServiceRequest1.T.create();
		ServiceRequest1 in_5_2_0 = ServiceRequest1.T.create();
		ServiceRequest1 in_5_2_1 = ServiceRequest1.T.create();

		root.getRequests().add(in_0);
		root.getRequests().add(in_1);
		root.getRequests().add(in_2);
		root.getRequests().add(in_3);
		root.getRequests().add(in_4);
		root.getRequests().add(in_5);

		in_4.getRequests().add(in_4_0);
		in_4.getRequests().add(in_4_1);
		in_4.getRequests().add(in_4_2);
		in_5.getRequests().add(in_5_0);
		in_5.getRequests().add(in_5_1);
		in_5.getRequests().add(in_5_2);

		in_4_2.getRequests().add(in_4_2_0);
		in_4_2.getRequests().add(in_4_2_1);
		in_5_2.getRequests().add(in_5_2_0);
		in_5_2.getRequests().add(in_5_2_1);

		// Circular references

		in_4_2.getRequests().add(in_5);
		in_5_2.getRequests().add(in_4);

		evaluate(root);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testNestedRequestWithCircularReferencedRoot() throws Exception {

		CompositeRequest root = createCompositeRequest();

		ServiceRequest1 in_0 = ServiceRequest1.T.create();
		ServiceRequest1 in_1 = ServiceRequest1.T.create();
		ServiceRequest1 in_2 = ServiceRequest1.T.create();
		ServiceRequest1 in_3 = ServiceRequest1.T.create();
		CompositeRequest in_4 = CompositeRequest.T.create();
		CompositeRequest in_5 = CompositeRequest.T.create();
		in_4.setParallelize(parallelize());
		in_5.setParallelize(parallelize());

		ServiceRequest1 in_4_0 = ServiceRequest1.T.create();
		ServiceRequest1 in_4_1 = ServiceRequest1.T.create();
		ServiceRequest1 in_5_0 = ServiceRequest1.T.create();
		ServiceRequest1 in_5_1 = ServiceRequest1.T.create();
		CompositeRequest in_4_2 = CompositeRequest.T.create();
		CompositeRequest in_5_2 = CompositeRequest.T.create();
		in_4_2.setParallelize(parallelize());
		in_5_2.setParallelize(parallelize());

		ServiceRequest1 in_4_2_0 = ServiceRequest1.T.create();
		ServiceRequest1 in_4_2_1 = ServiceRequest1.T.create();
		ServiceRequest1 in_5_2_0 = ServiceRequest1.T.create();
		ServiceRequest1 in_5_2_1 = ServiceRequest1.T.create();

		root.getRequests().add(in_0);
		root.getRequests().add(in_1);
		root.getRequests().add(in_2);
		root.getRequests().add(in_3);
		root.getRequests().add(in_4);
		root.getRequests().add(in_5);

		in_4.getRequests().add(in_4_0);
		in_4.getRequests().add(in_4_1);
		in_4.getRequests().add(in_4_2);
		in_5.getRequests().add(in_5_0);
		in_5.getRequests().add(in_5_1);
		in_5.getRequests().add(in_5_2);

		in_4_2.getRequests().add(in_4_2_0);
		in_4_2.getRequests().add(in_4_2_1);
		in_5_2.getRequests().add(in_5_2_0);
		in_5_2.getRequests().add(in_5_2_1);

		// Circular references

		in_4_2.getRequests().add(in_5);
		in_5_2.getRequests().add(root);

		evaluate(root);

	}

	@Test
	public void testNestedDuplicatedRequests() throws Exception {

		CompositeRequest root = createCompositeRequest();

		ServiceRequest1 in_0and1 = ServiceRequest1.T.create();
		ServiceRequest1 in_2and3 = ServiceRequest1.T.create();
		CompositeRequest in_4and5 = CompositeRequest.T.create();
		CompositeRequest in_6and7 = CompositeRequest.T.create();
		in_4and5.setParallelize(parallelize());
		in_6and7.setParallelize(parallelize());

		ServiceRequest1 in_4and5_0 = ServiceRequest1.T.create();
		ServiceRequest1 in_4and5_1 = ServiceRequest1.T.create();
		CompositeRequest in_4and5_2 = CompositeRequest.T.create();
		in_4and5_2.setParallelize(parallelize());

		ServiceRequest1 in_6and7_0 = ServiceRequest1.T.create();
		ServiceRequest1 in_6and7_1 = ServiceRequest1.T.create();
		CompositeRequest in_6and7_2 = CompositeRequest.T.create();
		in_6and7_2.setParallelize(parallelize());

		ServiceRequest1 in_4and5_2_0 = ServiceRequest1.T.create();
		ServiceRequest2 in_4and5_2_1 = ServiceRequest2.T.create();
		ServiceRequest3 in_4and5_2_2and3 = ServiceRequest3.T.create();
		ServiceRequest3 in_6and7_2_0 = ServiceRequest3.T.create();
		ServiceRequest4 in_6and7_2_1 = ServiceRequest4.T.create();
		ServiceRequest1 in_6and7_2_2and3 = ServiceRequest1.T.create();

		in_4and5_2_1.setSessionId(validSessionId());
		in_4and5_2_2and3.setSessionId(validSessionId());
		in_6and7_2_0.setSessionId(validSessionId());
		in_6and7_2_1.setSessionId(validSessionId());

		root.getRequests().add(in_0and1);
		root.getRequests().add(in_0and1);
		root.getRequests().add(in_2and3);
		root.getRequests().add(in_2and3);
		root.getRequests().add(in_4and5);
		root.getRequests().add(in_4and5);
		root.getRequests().add(in_6and7);
		root.getRequests().add(in_6and7);

		in_4and5.getRequests().add(in_4and5_0);
		in_4and5.getRequests().add(in_4and5_1);
		in_4and5.getRequests().add(in_4and5_2);

		in_6and7.getRequests().add(in_6and7_0);
		in_6and7.getRequests().add(in_6and7_1);
		in_6and7.getRequests().add(in_6and7_2);

		in_4and5_2.getRequests().add(in_4and5_2_0);
		in_4and5_2.getRequests().add(in_4and5_2_1);
		in_4and5_2.getRequests().add(in_4and5_2_2and3);
		in_4and5_2.getRequests().add(in_4and5_2_2and3);

		in_6and7_2.getRequests().add(in_6and7_2_0);
		in_6and7_2.getRequests().add(in_6and7_2_1);
		in_6and7_2.getRequests().add(in_6and7_2_2and3);
		in_6and7_2.getRequests().add(in_6and7_2_2and3);

		Object responseObject = evaluate(root);

		CompositeResponse result = assertComposite(responseObject, 8);

		assertSuccess(result, 0, String.class);
		assertSuccess(result, 1, String.class);
		assertSuccess(result, 2, String.class);
		assertSuccess(result, 3, String.class);
		CompositeResponse out_4 = assertComposite(result, 4, 3);
		CompositeResponse out_5 = assertComposite(result, 5, 3);
		CompositeResponse out_6 = assertComposite(result, 6, 3);
		CompositeResponse out_7 = assertComposite(result, 7, 3);

		assertSuccess(out_4, 0, String.class);
		assertSuccess(out_4, 1, String.class);
		CompositeResponse out_4_2 = assertComposite(out_4, 2, 4);

		assertSuccess(out_5, 0, String.class);
		assertSuccess(out_5, 1, String.class);
		CompositeResponse out_5_2 = assertComposite(out_5, 2, 4);

		assertSuccess(out_6, 0, String.class);
		assertSuccess(out_6, 1, String.class);
		CompositeResponse out_6_2 = assertComposite(out_6, 2, 4);

		assertSuccess(out_7, 0, String.class);
		assertSuccess(out_7, 1, String.class);
		CompositeResponse out_7_2 = assertComposite(out_7, 2, 4);

		assertSuccess(out_4_2, 0, String.class);
		assertSuccess(out_4_2, 1, Long.class);
		assertSuccess(out_4_2, 2, Integer.class);
		assertSuccess(out_4_2, 3, Integer.class);

		assertSuccess(out_5_2, 0, String.class);
		assertSuccess(out_5_2, 1, Long.class);
		assertSuccess(out_5_2, 2, Integer.class);
		assertSuccess(out_5_2, 3, Integer.class);

		assertSuccess(out_6_2, 0, Integer.class);
		assertSuccess(out_6_2, 1, Integer.class);
		assertSuccess(out_6_2, 2, String.class);
		assertSuccess(out_6_2, 3, String.class);

		assertSuccess(out_7_2, 0, Integer.class);
		assertSuccess(out_7_2, 1, Integer.class);
		assertSuccess(out_7_2, 2, String.class);
		assertSuccess(out_7_2, 3, String.class);

	}

	@Test(expected = UnsupportedOperationException.class)
	public void testUnregisteredDenotation() throws Exception {

		CompositeRequest root = createCompositeRequest();
		root.setContinueOnFailure(false);

		ServiceRequest1 in1 = ServiceRequest1.T.create();
		ServiceRequest1 in2 = ServiceRequest1.T.create();
		UnknownServiceRequest unknown = UnknownServiceRequest.T.create();

		root.getRequests().add(in1);
		root.getRequests().add(in2);
		root.getRequests().add(unknown);

		evaluate(root);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testNullDelegates() throws Exception {

		CompositeRequest root = createCompositeRequest();

		ServiceRequest1 in1 = ServiceRequest1.T.create();
		ServiceRequest1 in2 = ServiceRequest1.T.create();

		root.getRequests().add(in1);
		root.getRequests().add(in2);
		root.getRequests().add(null);

		evaluate(root);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testNullSingleDelegate() throws Exception {

		CompositeRequest root = createCompositeRequest();

		root.getRequests().add(null);

		evaluate(root);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testNoDelegates() throws Exception {

		CompositeRequest root = createCompositeRequest();

		evaluate(root);

	}

	protected CompositeRequest createCompositeRequest() {
		return createCompositeRequest(true);
	}

	protected CompositeRequest createCompositeRequest(boolean authorized) {
		CompositeRequest compositeRequest = CompositeRequest.T.create();
		compositeRequest.setParallelize(parallelize());
		if (authorized) {
			String sessionId = validSessionId();
			compositeRequest.setSessionId(sessionId);
		}
		return compositeRequest;
	}

}
