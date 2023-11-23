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

import com.braintribe.model.processing.aop.api.context.AroundContext;
import com.braintribe.model.processing.aop.api.interceptor.AroundInterceptor;
import com.braintribe.model.processing.aop.api.interceptor.InterceptionException;

/**
 * <p>
 * A {@link AroundInterceptor} which wraps the given {@link AroundContext}(s) as specialized
 * {@link CryptoInterceptorProcessor}(s).
 * 
 *
 * @param <I>
 *            {@link AroundInterceptor} standard input.
 * @param <O>
 *            {@link AroundInterceptor} standard output.
 * @param <C>
 *            The specialized {@link CryptoInterceptorProcessor} implementation will process.
 */
public abstract class CryptoInterceptor<I, O, C extends CryptoInterceptorProcessor<O>> implements AroundInterceptor<I, O> {

	protected CryptoInterceptorConfiguration cryptoInterceptorConfiguration;

	public CryptoInterceptor(CryptoInterceptorConfiguration cryptoInterceptorConfiguration) {
		this.cryptoInterceptorConfiguration = cryptoInterceptorConfiguration;
	}

	protected abstract C createCryptoInterceptorContext(AroundContext<I, O> context) throws InterceptionException;

	@Override
	public O run(AroundContext<I, O> context) throws InterceptionException {

		validateContext(context);

		C cryptoInterceptorContext = createCryptoInterceptorContext(context);

		return cryptoInterceptorContext.proceed();

	}

	protected void validateContext(AroundContext<I, O> context) throws InterceptionException {

		if (context == null) {
			throw new InterceptionException("Invalid null context");
		}

		// A hook for future validations

	}

}
