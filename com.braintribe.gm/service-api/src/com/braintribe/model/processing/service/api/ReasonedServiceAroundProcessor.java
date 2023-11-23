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

import static com.braintribe.gm.model.reason.UnsatisfiedMaybeTunneling.getOrTunnel;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.service.api.ServiceRequest;

/**
 * ReasonedServiceAroundProcessor is  trait like interface that adapts the {@link Maybe} returning
 * {@link #processReasoned(ServiceRequestContext, ServiceRequest, ProceedContext)} to the {@link ServiceAroundProcessor#process(ServiceRequestContext, ServiceRequest, ProceedContext)}
 * method.
 * 
 * @author Dirk Scheffler
 */
public interface ReasonedServiceAroundProcessor<P extends ServiceRequest, R> extends ServiceAroundProcessor<P, R> {
	@Override
	default R process(ServiceRequestContext requestContext, P request, ProceedContext proceedContext) {
		return getOrTunnel(processReasoned(requestContext, request, proceedContext));
	}
	
	Maybe<? extends R> processReasoned(ServiceRequestContext context, P request, ProceedContext proceedContext);
}
