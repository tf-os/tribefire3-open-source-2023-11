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
package com.braintribe.model.processing.vde.impl.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.braintribe.gm.model.svd.EvaluateRequest;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.common.ConfigurableDispatchingServiceProcessor;
import com.braintribe.model.processing.service.impl.ConfigurableServiceRequestEvaluator;
import com.braintribe.model.processing.vde.evaluator.api.aspects.RequestEvaluatorAspect;
import com.braintribe.model.processing.vde.impl.service.model.VdeTestRequest;
import com.braintribe.model.processing.vde.test.VdeTest;

/**
 * @author peter.gazdik
 */
public class EvaluateRequestVdeTest extends VdeTest {

	private static ConfigurableDispatchingServiceProcessor dispatcher = new ConfigurableDispatchingServiceProcessor();
	private static ConfigurableServiceRequestEvaluator evaluator = new ConfigurableServiceRequestEvaluator();

	static {
		dispatcher.register(VdeTestRequest.T, EvaluateRequestVdeTest::eval);

		evaluator.setServiceProcessor(dispatcher);
	}

	private static String eval(@SuppressWarnings("unused") ServiceRequestContext requestContext, VdeTestRequest request) {
		return request.getParameter().toUpperCase();
	}

	@Test
	public void testEval() throws Exception {
		VdeTestRequest request = VdeTestRequest.T.create();
		request.setParameter("hello");

		EvaluateRequest evalRequest = EvaluateRequest.T.create();
		evalRequest.setRequest(request);

		Object result = evaluateWith(RequestEvaluatorAspect.class, evaluator, evalRequest);
		assertThat(result).isEqualTo("HELLO");
	}

}
