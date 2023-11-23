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
package com.braintribe.gm.service.wire.common.contract;

import com.braintribe.gm.service.impl.DomainIdServiceAroundProcessor;
import com.braintribe.model.processing.securityservice.commons.service.AuthorizingServiceInterceptor;
import com.braintribe.model.processing.service.api.ServiceInterceptorProcessor;
import com.braintribe.model.processing.service.common.ElapsedTimeMeasuringInterceptor;
import com.braintribe.model.processing.service.common.ThreadNamingInterceptor;
import com.braintribe.model.processing.service.common.eval.ConfigurableServiceRequestEvaluator;
import com.braintribe.wire.api.space.WireSpace;

public interface CommonServiceProcessingContract extends WireSpace {

	/* Interceptors with these identifications are registered automatically */
	String AUTH_INTERCEPTOR_ID = "auth";
	String DOMAIN_ID_INTERCEPTOR_ID = "domain-id";
	String THREAD_NAMING_INTERCEPTOR_ID = "thread-naming";
	String TIME_MEASURING_INTERCEPTOR_ID = "time-measuring";

	/**
	 * This evaluator is pre-configured with the following {@link ServiceInterceptorProcessor interceptors}:
	 * <ul>
	 * <li>{@value #AUTH_INTERCEPTOR_ID} - {@link AuthorizingServiceInterceptor}
	 * <li>{@value #DOMAIN_ID_INTERCEPTOR_ID} - {@link DomainIdServiceAroundProcessor}
	 * <li>{@value #THREAD_NAMING_INTERCEPTOR_ID} - {@link ThreadNamingInterceptor}
	 * <li>{@value #TIME_MEASURING_INTERCEPTOR_ID} - {@link ElapsedTimeMeasuringInterceptor}
	 * </ul>
	 */
	ConfigurableServiceRequestEvaluator evaluator();
}
