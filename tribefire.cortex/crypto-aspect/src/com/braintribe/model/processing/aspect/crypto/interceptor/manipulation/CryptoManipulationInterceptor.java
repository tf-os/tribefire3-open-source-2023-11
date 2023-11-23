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
package com.braintribe.model.processing.aspect.crypto.interceptor.manipulation;

import com.braintribe.model.accessapi.ManipulationRequest;
import com.braintribe.model.accessapi.ManipulationResponse;
import com.braintribe.model.processing.aop.api.context.AroundContext;
import com.braintribe.model.processing.aspect.crypto.interceptor.CryptoInterceptor;
import com.braintribe.model.processing.aspect.crypto.interceptor.CryptoInterceptorConfiguration;

/**
 * <p>
 * A {@link CryptoInterceptor} which processes {@link CryptoManipulationInterceptorProcessor} instances.
 * 
 */
public class CryptoManipulationInterceptor extends CryptoInterceptor<ManipulationRequest, ManipulationResponse, CryptoManipulationInterceptorProcessor> {

	public CryptoManipulationInterceptor(CryptoInterceptorConfiguration cryptoInterceptorConfiguration) {
		super(cryptoInterceptorConfiguration);
	}

	@Override
	protected CryptoManipulationInterceptorProcessor createCryptoInterceptorContext(AroundContext<ManipulationRequest, ManipulationResponse> context) {
		return new CryptoManipulationInterceptorProcessor(cryptoInterceptorConfiguration, context);
	}

}
