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
package com.braintribe.model.processing.aop.impl.aspect;

import java.util.ArrayList;
import java.util.List;

import com.braintribe.model.processing.aop.api.interceptor.AfterInterceptor;
import com.braintribe.model.processing.aop.api.interceptor.AroundInterceptor;
import com.braintribe.model.processing.aop.api.interceptor.BeforeInterceptor;

public class JoinPointConfiguration {

	public List<BeforeInterceptor<?, ?>> beforeInterceptors = new ArrayList<BeforeInterceptor<?,?>>();
	public List<AroundInterceptor<?, ?>> aroundInterceptors = new ArrayList<AroundInterceptor<?,?>>();
	public List<AfterInterceptor<?, ?>> afterInterceptors = new ArrayList<AfterInterceptor<?,?>>();
	
}
