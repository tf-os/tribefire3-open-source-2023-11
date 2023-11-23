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
package com.braintribe.model.processing.service.common;

import com.braintribe.model.processing.service.api.ProceedContext;
import com.braintribe.model.processing.service.api.ServiceAroundProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.api.ServiceRequestSummaryLogger;
import com.braintribe.model.service.api.ServiceRequest;

public class ElapsedTimeMeasuringInterceptor implements ServiceAroundProcessor<ServiceRequest, Object> {
	
	public static final ElapsedTimeMeasuringInterceptor INSTANCE = new ElapsedTimeMeasuringInterceptor();

	private ElapsedTimeMeasuringInterceptor() {
	}
	
	@Override
	public Object process(ServiceRequestContext context, ServiceRequest request, ProceedContext proceedContext) {
		ServiceRequestSummaryLogger summaryLogger = context.summaryLogger();
		String summaryStep = summaryLogger.isEnabled() ? request.entityType().getShortName() + " processing" : null;

		try {

			if (summaryStep != null) {
				summaryLogger.startTimer(summaryStep);
			}

			Object result = proceedContext.proceed(request);
			return result;

		} finally {
			if (summaryStep != null) {
				summaryLogger.stopTimer(summaryStep);
			}
		}
	}
}
