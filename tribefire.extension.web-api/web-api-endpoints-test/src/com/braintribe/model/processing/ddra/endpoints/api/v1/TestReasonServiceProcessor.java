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
package com.braintribe.model.processing.ddra.endpoints.api.v1;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.model.processing.ddra.endpoints.api.v1.model.TestReasoningServiceRequest;
import com.braintribe.model.processing.ddra.endpoints.api.v1.model.reason.IncompleteReason;
import com.braintribe.model.processing.ddra.endpoints.api.v1.model.reason.TestReason;
import com.braintribe.model.processing.service.api.ReasonedServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;

public class TestReasonServiceProcessor implements ReasonedServiceProcessor<TestReasoningServiceRequest, String>{

	public static final String REASON_MESSSAGE_INCOMPLETE = "Forgot whom to greet";
	public static final String REASON_MESSSAGE_EMPTY = "Not able to process";
	public static final String VALUE_INCOMPLETE = "Hello ...";
	public static final String VALUE_COMPLETE = "Hello World!";

	@Override
	public Maybe<String> processReasoned(ServiceRequestContext requestContext, TestReasoningServiceRequest request) {
		switch (request.getMaybeOption()) {
		case complete:
			return Maybe.complete(VALUE_COMPLETE);
		case incomplete:
			return Reasons.build(IncompleteReason.T) //
				.text(REASON_MESSSAGE_INCOMPLETE) //
				.enrich(r -> r.setMessage(REASON_MESSSAGE_INCOMPLETE)) //
				.toMaybe(VALUE_INCOMPLETE);
		case empty:
			return Reasons.build(TestReason.T) //
				.text(REASON_MESSSAGE_EMPTY) //
				.enrich(r -> r.setMessage(REASON_MESSSAGE_EMPTY)) //
				.toMaybe();
		default:
			throw new IllegalStateException();
		}
	}
}
