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
package com.braintribe.model.processing.aop.impl.context;

import java.util.Collections;
import java.util.List;

import com.braintribe.model.processing.aop.api.aspect.Advice;
import com.braintribe.model.processing.aop.api.aspect.Caller;
import com.braintribe.model.processing.aop.api.context.AroundContext;
import com.braintribe.model.processing.aop.api.interceptor.AroundInterceptor;

/**
 * @author gunther.schenk
 */
public class AroundContextImpl<I,O> extends AbstractContextImpl<I> implements AroundContext<I, O>{
	
	private List<AroundInterceptor<I,O>> interceptors;
	private Caller<I, O> actualCaller;
	private int interceptorIndex = 0;
	private boolean isRequestOverridden = false;

	public void setInterceptorIndex(int interceptorIndex) {
		this.interceptorIndex = interceptorIndex;
	}
	
	// **************************************************************************
	// Interface methods
	// **************************************************************************

	@Override
	public O proceed(I request) {
		overrideRequest(request);
		return proceed();
	}
	
	@Override
	public O proceed() {
		O result = null;
		if (interceptorIndex < getInterceptors().size()) {
			AroundInterceptor<I, O> interceptor = getInterceptors().get(interceptorIndex);
			try {
				interceptorIndex++;
				result = interceptor.run(this);
				commitIfNecessary();
			} finally {
				interceptorIndex--;
			}
		} else {
			result = actualCaller.call(request, this);
		}
		
		return result;
	}

	public List<AroundInterceptor<I, O>> getInterceptors() {
		if (interceptors == null)
			return Collections.emptyList();
		return interceptors;
	}
	
	public void setInterceptors(List<AroundInterceptor<I, O>> interceptors) {
		this.interceptors = interceptors;
		
	}
	
	public void setActualCaller(Caller<I, O> caller) {
		this.actualCaller = caller;
	}

	@Override
	public Advice getAdvice() {
		return Advice.around;
	}

	@Override
	public void overrideRequest(I request) {
		this.request = request;
		this.isRequestOverridden = true;
	}
	
	@Override
	public String getProceedIdentification() {
		return String.valueOf(interceptorIndex);
	}
	
	public boolean isRequestOverridden() {
		return isRequestOverridden;
	}
}
