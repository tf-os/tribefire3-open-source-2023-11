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
package com.braintribe.model.processing.aop.api.service;

import java.util.Set;

import com.braintribe.model.aopaccessapi.AccessAspectAfterInterceptorRequest;
import com.braintribe.model.aopaccessapi.AccessAspectAfterInterceptorResponse;
import com.braintribe.model.aopaccessapi.AccessAspectAroundInterceptorRequest;
import com.braintribe.model.aopaccessapi.AccessAspectAroundInterceptorResponse;
import com.braintribe.model.aopaccessapi.AccessAspectBeforeInterceptorRequest;
import com.braintribe.model.aopaccessapi.AccessAspectBeforeInterceptorResponse;
import com.braintribe.model.extensiondeployment.meta.AccessPointCut;
import com.braintribe.model.processing.aop.api.aspect.Advice;

/**
 * the interface for the handling service<br/>
 * there are two implementations of it,
 * <li>externalizing implementation: translates the calls into a dispatchable (rpc, mq) content - see MasterCartridge</li>
 * <li>internalizing implementation: translates the dispatchable content into calls - see CartridgeBase</li>
 * @author pit, dirk
 *
 */
public interface AccessAspectService {

	/**
	 * run the interceptors hooked to the before {@link Advice}
	 * @param request - the {@link AccessAspectBeforeInterceptorRequest} that contains the appropriate request
	 * @return - the {@link AccessAspectBeforeInterceptorResponse} that contains the respective response
	 */
	AccessAspectBeforeInterceptorResponse runBeforeInterceptor( AccessAspectBeforeInterceptorRequest request) throws AccessAspectServiceException;

	/**
	 * run the interceptors hooked to the around {@link Advice}
	 * @param request - the {@link AccessAspectAroundInterceptorRequest} that contains the appropriate request
	 * @return - the {@link AccessAspectAroundInterceptorResponse} that contains the respective response
	 */
	AccessAspectAroundInterceptorResponse runAroundInterceptor( AccessAspectAroundInterceptorRequest request) throws AccessAspectServiceException;

	/**
	 * run the interceptors hooked to the after {@link Advice}
	 * @param request - the {@link AccessAspectAfterInterceptorRequest} that contains the appropriate request
	 * @return - the {@link AccessAspectAfterInterceptorResponse} that contains the respective response
	 */

	AccessAspectAfterInterceptorResponse runAfterInterceptor( AccessAspectAfterInterceptorRequest request) throws AccessAspectServiceException;

	/**
	 * delivers the pointcuts from service.
	 * @return Set of pointcuts
	 */
	Set<AccessPointCut> getPointCuts() throws AccessAspectServiceException;

}
