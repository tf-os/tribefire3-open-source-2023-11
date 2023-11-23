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
package com.braintribe.model.processing.rpc.commons.impl.logging;

import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.common.attribute.AttributeContext;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.processing.service.api.ServiceRequestSummaryLogger;
import com.braintribe.model.processing.service.api.aspect.RequestedEndpointAspect;
import com.braintribe.model.processing.service.api.aspect.RequestorAddressAspect;
import com.braintribe.model.processing.service.api.aspect.RequestorUserNameAspect;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.utils.collection.impl.AttributeContexts;

/**
 * {@link RpcServiceRequestSummaryLogger} tests.
 * 
 */
public class RpcServiceRequestSummaryLoggerTest {

	private static Logger log = Logger.getLogger(RpcServiceRequestSummaryLoggerTest.class);

	private static final String STEP_A = "Test Execution Step A";
	private static final String STEP_B = "Test Execution Step B";

	private static final String ENDPOINT = "Test Requested Endpoint";
	private static final String ADDRESS = "Test Requestor Address";
	private static final String USER = "Test Requestor User Name";

	@Test
	public void testSequence() {

		AttributeContext testContext = testContext();

		ServiceRequestSummaryLogger logger = new RpcServiceRequestSummaryLogger(log, testContext, null);

		logger.startTimer(STEP_A);
		logger.stopTimer(STEP_A);
		logger.startTimer(STEP_B);
		logger.stopTimer(STEP_B);
		logger.stopTimer();

		String summary = logger.summary(this, TestRequest.T.create());

		System.out.println(summary);

		assertContextProperties(testContext, summary);

		Pattern stepsPattern = stepsPattern(1, STEP_A, 1, STEP_B);

		assertPattern(summary, stepsPattern);

	}

	@Test
	public void testNested() {

		AttributeContext testContext = testContext();

		ServiceRequestSummaryLogger logger = new RpcServiceRequestSummaryLogger(log, testContext, null);

		logger.startTimer(STEP_A);
		logger.startTimer(STEP_B);
		logger.stopTimer(STEP_B);
		logger.stopTimer(STEP_A);
		logger.stopTimer();

		String summary = logger.summary(this, TestRequest.T.create());

		System.out.println(summary);

		assertContextProperties(testContext, summary);

		Pattern stepsPattern = stepsPattern(1, STEP_A, 2, STEP_B);

		assertPattern(summary, stepsPattern);

	}

	@Test
	public void testNestedSequence() {

		AttributeContext testContext = testContext();

		ServiceRequestSummaryLogger logger = new RpcServiceRequestSummaryLogger(log, testContext, null);

		logger.startTimer(STEP_A);
		logger.startTimer(STEP_B);
		logger.stopTimer(STEP_B);
		logger.startTimer(STEP_A);
		logger.stopTimer(STEP_A);
		logger.stopTimer(STEP_A);
		logger.stopTimer();

		String summary = logger.summary(this, TestRequest.T.create());

		System.out.println(summary);

		assertContextProperties(testContext, summary);

		Pattern stepsPattern = stepsPattern(1, STEP_A, 2, STEP_B, 2, STEP_A);

		assertPattern(summary, stepsPattern);
	}

	@Test
	public void testSequenceNested() {

		AttributeContext testContext = testContext();

		ServiceRequestSummaryLogger logger = new RpcServiceRequestSummaryLogger(log, testContext, null);

		logger.startTimer(STEP_A);
		logger.startTimer(STEP_B);
		logger.stopTimer(STEP_B);
		logger.stopTimer(STEP_A);
		logger.startTimer(STEP_B);
		logger.startTimer(STEP_A);
		logger.stopTimer(STEP_A);
		logger.stopTimer(STEP_B);
		logger.stopTimer();

		String summary = logger.summary(this, TestRequest.T.create());

		System.out.println(summary);

		assertContextProperties(testContext, summary);

		Pattern stepsPattern = stepsPattern(1, STEP_A, 2, STEP_B, 1, STEP_B, 2, STEP_A);

		assertPattern(summary, stepsPattern);

	}

	public static interface TestRequest extends ServiceRequest {

		EntityType<TestRequest> T = EntityTypes.T(TestRequest.class);

	}

	private static AttributeContext testContext() {
		return AttributeContexts.attributeContext()
			.set(RequestedEndpointAspect.class, ENDPOINT)
			.set(RequestorAddressAspect.class, ADDRESS)
			.set(RequestorUserNameAspect.class, USER)
			.build();
	}

	private static void assertContextProperties(AttributeContext context, String summary) {
		Assert.assertTrue("URL property doesn't match", propertyPattern("URL", context.getAttribute(RequestedEndpointAspect.class)).matcher(summary).matches());
		Assert.assertTrue("IP property doesn't match", propertyPattern("IP", context.getAttribute(RequestorAddressAspect.class)).matcher(summary).matches());
		Assert.assertTrue("User property doesn't match", propertyPattern("User", context.getAttribute(RequestorUserNameAspect.class)).matcher(summary).matches());
	}

	private static Pattern propertyPattern(String name, String value) {
		return Pattern.compile(".*\\n{1}\\s{2}" + name + "\\s+:\\s+" + value + "\\r?\\n{1}.*", Pattern.DOTALL);
	}

	private static void assertPattern(String summary, Pattern pattern) {
		Assert.assertTrue("Pattern didn't match for the given summary. Pattern: " + pattern + " Summary: " + summary,
				pattern.matcher(summary).matches());
	}

	private static Pattern stepsPattern(Object... steps) {

		StringBuilder sb = new StringBuilder();

		sb.append(".*\\s{2}Full request\\s+:\\s+\\d+\\s+ms\\r?\\n{1}");

		for (int i = 0; i < steps.length; i++) {
			Object object = steps[i];
			if (i % 2 == 0) {
				Integer level = (Integer) object;
				level = level * 4 + 2;
				sb.append("\\s{").append(level).append("}");
			} else {
				sb.append(object).append("\\s+:\\s+\\d+\\s+ms\\r?\\n{1}");
			}
		}
		sb.append(".*");

		return Pattern.compile(sb.toString(), Pattern.DOTALL);

	}

}
