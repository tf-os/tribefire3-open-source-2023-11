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

/**
 * ReasonedServicePostProcessor is  trait like interface that adapts the {@link Maybe} returning
 * {@link #processReasoned(ServiceRequestContext, Object)} to the {@link ServicePostProcessor#process(ServiceRequestContext, Object)}
 * method.
 * 
 * @author Dirk Scheffler
 */
public interface ReasonedServicePostProcessor<R> extends ServicePostProcessor<R> {
	@Override
	default Object process(ServiceRequestContext requestContext, R response) {
		return getOrTunnel(processReasoned(requestContext, response));
	}
	
	Maybe<? extends R> processReasoned(ServiceRequestContext requestContext, R response); 
}
