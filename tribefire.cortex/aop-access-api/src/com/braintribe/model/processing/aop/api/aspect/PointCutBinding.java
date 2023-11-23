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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.braintribe.model.processing.aop.api.interceptor.Interceptor;

/**
 * a {@link PointCutBinding} brings all together:<br/>
 * <li>the {@link AccessJoinPoint} the declares the method of the access to be wrapped </li>
 * <li>the {@link Advice} that declares how the point cut's to be called</li>
 * <li> and the list of {@link Interceptor}'s that are appropriate of the point cut</li> 
 * @author pit, dirk
 * 
 *  
 */
public class PointCutBinding {
	
	private AccessJoinPoint joinPoint;
	private Advice advice;	
	private List<Interceptor> interceptors;
	
	public PointCutBinding (AccessJoinPoint method, Advice advice, Interceptor... interceptor) {
		this.joinPoint = method;
		this.advice = advice;
		this.interceptors = new ArrayList<Interceptor>(Arrays.asList(interceptor));
	}

	public AccessJoinPoint getJoinPoint() {
		return joinPoint;
	}

	public Advice getAdvice() {
		return advice;
	}

	public List<Interceptor> getInterceptors() {
		return interceptors;
	}
	
	public void addInterceptor( Interceptor interceptor) {
		interceptors.add( interceptor);
	}
	
}
	
	
