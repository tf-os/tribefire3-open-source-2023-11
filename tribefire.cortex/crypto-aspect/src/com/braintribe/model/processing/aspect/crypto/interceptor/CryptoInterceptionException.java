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
package com.braintribe.model.processing.aspect.crypto.interceptor;

import com.braintribe.model.processing.aop.api.interceptor.InterceptionException;

/**
 * <p>
 * An {@link InterceptionException} meant to distinguish exceptions 
 * thrown by {@link CryptoInterceptor}(s) from those thrown by other 
 * {@link com.braintribe.model.processing.aop.api.interceptor.AroundInterceptor} 
 * in the chain.
 * 
 * <p>
 * Therefore {@link InterceptionException} coming from the invocation chain are
 * not to be wrapped in this exception.
 * 
 *
 */
public class CryptoInterceptionException extends InterceptionException {

	private static final long serialVersionUID = 1L;

	public CryptoInterceptionException(String message, Throwable cause) {
		super(message, cause);
	}

	public CryptoInterceptionException(String message) {
		super(message);
	}

	public CryptoInterceptionException(Throwable cause) {
		super(cause);
	}

}
