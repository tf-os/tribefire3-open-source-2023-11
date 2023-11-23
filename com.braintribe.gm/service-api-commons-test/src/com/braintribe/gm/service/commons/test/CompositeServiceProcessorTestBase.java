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

import java.util.Arrays;
import java.util.stream.Collectors;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;

import com.braintribe.gm.service.commons.test.wire.ServiceApiCommonsTestWireModule;
import com.braintribe.gm.service.commons.test.wire.contract.ServiceApiCommonsTestContract;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.processing.service.api.CompositeException;
import com.braintribe.model.securityservice.OpenUserSession;
import com.braintribe.model.securityservice.credentials.UserPasswordCredentials;
import com.braintribe.model.service.api.CompositeRequest;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.result.CompositeResponse;
import com.braintribe.model.service.api.result.Failure;
import com.braintribe.model.service.api.result.ResponseEnvelope;
import com.braintribe.model.service.api.result.ServiceResult;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

public abstract class CompositeServiceProcessorTestBase {
	
	protected static WireContext<ServiceApiCommonsTestContract> context;
	protected static Evaluator<ServiceRequest> evaluator;
	protected static ServiceApiCommonsTestContract testContract;
	
	@BeforeClass
	public static void beforeClass() {
		context = Wire.context(ServiceApiCommonsTestWireModule.INSTANCE);
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


	protected static CompositeResponse assertComposite(CompositeResponse compositeResult, int index, int expectedResults) {
		ResponseEnvelope result = assertSuccess(compositeResult, index, CompositeResponse.class);
		return assertComposite(result.getResult(), expectedResults);
	}

	protected static CompositeResponse assertComposite(Object result, int expectedResults) {
		Assert.assertNotNull("Unexpected null result", result);
		Assert.assertTrue("Result is not composite: "+result, result instanceof CompositeResponse);
		CompositeResponse compositeResult = (CompositeResponse)result;
		Assert.assertNotNull(compositeResult.getResults());
		Assert.assertEquals(expectedResults, compositeResult.getResults().size());
		return compositeResult;
	}

	protected static ResponseEnvelope assertSuccess(CompositeResponse compositeResult, int index, Class<?> expectedValueType) {
		ServiceResult result = getServiceResult(compositeResult, index);
		Assert.assertNotNull("Unexpected null result", result);
		Assert.assertTrue("Result is not successful: " + result, result instanceof ResponseEnvelope);
		ResponseEnvelope standardResult = (ResponseEnvelope) result;
		if (expectedValueType == null) {
			Assert.assertNull(standardResult.getResult());
		} else {
			Assert.assertTrue("Return value " + standardResult.getResult() + " is not compatible with " + expectedValueType, expectedValueType.isInstance(standardResult.getResult()));
		}
		return standardResult;
	}

	protected static ResponseEnvelope assertSuccess(CompositeResponse compositeResult, int index, Object expectedValue) {
		ServiceResult result = getServiceResult(compositeResult, index);
		Assert.assertNotNull("Unexpected null result", result);
		Assert.assertTrue("Result is not successful: " + result, result instanceof ResponseEnvelope);
		ResponseEnvelope standardResult = (ResponseEnvelope) result;
		if (expectedValue == null) {
			Assert.assertNull(standardResult.getResult());
		} else {
			Assert.assertEquals("Unexpected return value " + standardResult.getResult() + " at index " + index, expectedValue, standardResult.getResult());
		}
		return standardResult;
	}

	protected static void assertSuccess(CompositeResponse response, Object ... expectedValues) {
		assertComposite(response, expectedValues.length);
		for (int i = 0; i < expectedValues.length; i++) {
			assertSuccess(response, i, expectedValues[i]);
		}
	}

	protected static Failure assertFailure(CompositeResponse compositeResult, int index, Class<? extends Exception> expectedExceptionType) {
		ServiceResult result = getServiceResult(compositeResult, index);
		Assert.assertNotNull("Unexpected null result", result);
		Assert.assertTrue("Result is not failure: " + result, result instanceof Failure);
		Failure failure = (Failure) result;
		Assert.assertNotNull("Failure type is null", failure.getType());
		Class<?> exceptionType = null;
		try {
			exceptionType = Class.forName(failure.getType());
		} catch (ClassNotFoundException e) {
			Assert.fail("Invalid (not found) failure type: " + failure.getType());
		}
		Assert.assertTrue("Failure type " + failure.getType() + " is not compatible with " + expectedExceptionType, expectedExceptionType.isAssignableFrom(exceptionType));
		return failure;
	}

	protected static void assertIgnored(CompositeResponse compositeResult, int index) {
		ServiceResult result = getServiceResult(compositeResult, index);
		Assert.assertNull("Unexpected non-null result. Respective request should have been ignored, but returned: "+result, result);
	}

	protected static ServiceResult getServiceResult(CompositeResponse compositeResult, int index) {

		if (index < 0 || index >= compositeResult.getResults().size()) {
			Assert.fail("Given index [" + index + "] is not present in the composite result");
		}

		ServiceResult result = compositeResult.getResults().get(index);
		return result;

	}

//	protected String validSessionId() {
//		return AuthorizingServiceProcessorTest.validSessionId();
//	}

	protected <T> T evaluate(ServiceRequest request) throws Exception {
		Object response = request.eval(evaluator).get();
		return (T) response;
	}

	protected void evaluateAndAssertException(CompositeRequest request, Class<? extends Exception> exceptionType) {
		try {
			evaluate(request);
			Assert.fail("Request should have failed with "+exceptionType.getName());
		} catch (Exception e) {
			Assert.assertEquals(exceptionType, e.getClass());
		}
	}

	protected void evaluateAndAssertExceptions(CompositeRequest request, Class<? extends Exception> ... exceptionTypes) {
		try {
			evaluate(request);
			Assert.fail("Request should have failed with "+CompositeException.class.getName());
		} catch (CompositeException e) {

			Throwable[] s = e.getSuppressed();
			Assert.assertEquals(exceptionTypes.length, s.length);
			int total = exceptionTypes.length;

			String[] suppressed = Arrays.stream(s).map(Throwable::getClass).map(Class::toString).sorted().collect(Collectors.toList()).toArray(new String[total]);

			String[] expected = Arrays.stream(exceptionTypes).map(Class::toString).sorted().collect(Collectors.toList()).toArray(new String[total]);
			
			Assert.assertArrayEquals(expected, suppressed);

		} catch (Exception e) {
			Assert.fail("Request should have failed with "+CompositeException.class.getName());
		}
	}

}
