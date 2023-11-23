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

import com.braintribe.model.processing.ddra.endpoints.api.v1.model.ResponseCodeOverridingRequest;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.api.aspect.HttpStatusCodeNotification;
import com.braintribe.model.service.api.result.Neutral;

public class ResponseCodeOverridingProcessor implements ServiceProcessor<ResponseCodeOverridingRequest, Neutral>{

	@Override
	public Neutral process(ServiceRequestContext requestContext, ResponseCodeOverridingRequest request) {
		requestContext.findAspect(HttpStatusCodeNotification.class).accept(request.getResponseCode());

		if (!request.getSucceed()) {
			throw new IllegalStateException("Processor failed on purpose.");
		}
		
		
		return Neutral.NEUTRAL;
	}
	

}
