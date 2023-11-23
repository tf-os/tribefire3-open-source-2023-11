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
package com.braintribe.model.processing.aop.common;

import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.List;

import com.braintribe.model.processing.aop.api.interceptor.AfterInterceptor;
import com.braintribe.model.processing.aop.api.interceptor.AroundInterceptor;
import com.braintribe.model.processing.aop.api.interceptor.BeforeInterceptor;
import com.braintribe.model.processing.aop.api.interceptor.Interceptor;

/**
 * a little container for the different interceptors 
 * 
 * @author pit, dirk
 */
public class JoinPointConfiguration {

	public List<BeforeInterceptor<?, ?>> beforeInterceptors = newList();
	public List<AroundInterceptor<?, ?>> aroundInterceptors = newList();
	public List<AfterInterceptor<?, ?>> afterInterceptors = newList();

	/** Convenience to make handling this class easier. */
	public static <E extends Interceptor> List<E> castInterceptors(List<? extends Interceptor> list) {
		return (List<E>) list;
	}


	
}
