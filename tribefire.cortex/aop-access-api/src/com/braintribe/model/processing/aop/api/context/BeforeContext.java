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
package com.braintribe.model.processing.aop.api.context;

/**
 * {@link Context} interface for before advice 
 * 
 * @author dirk, pit
 *
 */
public interface BeforeContext<I,O> extends Context<I> {
	/**
	 * skip the complete interception, while returning the response given
	 * @param response - the response to give 
	 */
	void skip( O response);
	/**
	 * call all subsequent interceptors with this request 
	 * @param request - the request to pass to any subsequent interceptors 
	 */
	void overrideRequest( I request);		
}
