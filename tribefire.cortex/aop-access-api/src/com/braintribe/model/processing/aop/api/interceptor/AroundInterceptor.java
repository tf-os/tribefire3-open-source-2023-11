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
package com.braintribe.model.processing.aop.api.interceptor;

import com.braintribe.model.processing.aop.api.context.AroundContext;

/**
 * the interceptor to be be called as a wrapper around the caller
 * 
 * @author dirk, pit
 * 
 * @param <I>
 *            - the input, the request
 * @param <O>
 *            - the output, the response
 */
public interface AroundInterceptor<I, O> extends Interceptor {
	/**
	 * run the interception
	 * 
	 * @param context
	 *            - the {@link AroundContext} that contains the appropriate data (and allows for stepping)
	 * @return - the output
	 */
	O run(AroundContext<I, O> context) throws InterceptionException;
}
