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
package com.braintribe.model.processing.service.api;

import com.braintribe.model.service.api.ServiceRequest;

/**
 * <p>
 * A service for wrapping {@link ServiceProcessor#process(ServiceRequestContext, ServiceRequest)} calls.
 *
 * @author dirk.scheffler
 *
 * @param <P>
 *            The type of the request.
 * @param <R>
 *            The type of the response.
 */
public interface ServiceAroundProcessor<P extends ServiceRequest, R extends Object> extends ServiceInterceptorProcessor {

	/**
	 * <p>
	 * Processes the given {@link ServiceRequest} instance offering control over the in-bound request and out-bound response.
	 * 
	 * <p>
	 * Implementations delegate the call to the next processor in the chain, which might be another {@link ServiceAroundProcessor}, by calling
	 * {@link ProceedContext#proceed(ServiceRequest)} on the given {@code proceedContext}.
	 * 
	 * <p>
	 * Thus {@code ServiceAroundProcessor} implementations can override the request, override the response and change the request flow at will.
	 * 
	 * <p>
	 * e.g.:
	 * 
	 * <pre>
	 * public MyResponse process(ServiceRequestContext requestContext, MyRequest request, ProceedContext proceedContext)
	 * 		throws ServiceProcessorException {
	 * 
	 * 	if (overrideRequestCondition) {
	 * 		request = overrideRequest(request);
	 * 	}
	 * 
	 * 	MyResponse response = null;
	 * 
	 * 	if (flowChangeCondition) {
	 * 		response = callSomeThingElse(request);
	 * 	} else {
	 * 		response = proceedContext.proceed(request);
	 * 	}
	 * 
	 * 	if (overrideResponseCondition) {
	 * 		response = overrideResponse(response);
	 * 	}
	 * 
	 * 	return response;
	 * 
	 * }
	 * </pre>
	 * 
	 * @param requestContext
	 *            The {@link ServiceRequestContext} of the incoming processing request.
	 * @param request
	 *            The service request.
	 * @param proceedContext
	 *            The {@link ProceedContext} of the incoming wrapped request.
	 * @return The service response.
	 */
	R process(ServiceRequestContext requestContext, P request, ProceedContext proceedContext);

	default @Override InterceptorKind getKind() {
		return InterceptorKind.around;
	}
}
