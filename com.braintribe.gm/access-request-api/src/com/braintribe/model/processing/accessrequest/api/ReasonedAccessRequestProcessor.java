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
package com.braintribe.model.processing.accessrequest.api;

import static com.braintribe.gm.model.reason.UnsatisfiedMaybeTunneling.getOrTunnel;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.accessapi.AccessRequest;

/**
 * ReasonedAccessRequestProcessor is  trait like interface that adapts the {@link Maybe} returning
 * {@link #processReasoned(AccessRequestContext)} to the {@link ReasonedAccessRequestProcessor#process(AccessRequestContext)}
 * method.
 * 
 * @author Dirk Scheffler
 */
public interface ReasonedAccessRequestProcessor<P extends AccessRequest, R> extends AccessRequestProcessor<P, R> {
	default R process(AccessRequestContext<P> context) {
		return getOrTunnel(processReasoned(context));
	}
	
	Maybe<? extends R> processReasoned(AccessRequestContext<P> context);
}
