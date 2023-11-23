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
package com.braintribe.model.processing.aop.api.aspect;

import com.braintribe.model.processing.aop.api.interceptor.AfterInterceptor;
import com.braintribe.model.processing.aop.api.interceptor.AroundInterceptor;
import com.braintribe.model.processing.aop.api.interceptor.BeforeInterceptor;
import com.braintribe.model.processing.aop.api.interceptor.Interceptor;

/**
 * this the context to be used to add the different point cuts 
 * 
 * @author pit, dirk
 *
 */
public interface PointCutConfigurationContext {
	/**
	 * add a fully built binding 
	 * @param pointCutBinding - the fully qualified {@link PointCutBinding}
	 */
	void addPointCutBinding( PointCutBinding pointCutBinding);
	/**
	 * add a binding with the three parameters 
	 * @param joinPoint - the {@link AccessJoinPoint} the defines the method 
	 * @param advice - the {@link Advice} that defines when 
	 * @param interceptor - the {@link Interceptor} to be called 
	 */
	void addPointCutBinding( AccessJoinPoint joinPoint, Advice advice, Interceptor... interceptor);
	
	/**
	 * add a binding for the before {@link Advice}
	 * @param joinPoint - the {@link AccessJoinPoint} that the defines the method
	 * @param interceptor - one or more {@link BeforeInterceptor}s to be called 
	 */
	void addPointCutBinding( AccessJoinPoint joinPoint, BeforeInterceptor<?, ?>... interceptor);
	/**
	 * add a binding for the around {@link Advice}
	 * @param joinPoint - the {@link AccessJoinPoint} that the defines the method
	 * @param interceptor - one or more {@link AroundInterceptor}s to be called 
	 */
	void addPointCutBinding( AccessJoinPoint joinPoint, AroundInterceptor<?, ?>... interceptor);
	/**
	 * add a binding for the after {@link Advice}
	 * @param joinPoint - the {@link AccessJoinPoint} that the defines the method
	 * @param interceptor - one or more {@link AfterInterceptor}s to be called 
	 */
	void addPointCutBinding( AccessJoinPoint joinPoint, AfterInterceptor<?, ?>... interceptor);
	
}
