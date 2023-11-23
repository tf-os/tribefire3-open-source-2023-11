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

import java.util.Objects;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.ThreadRenamer;
import com.braintribe.model.processing.service.api.ProceedContext;
import com.braintribe.model.processing.service.api.ServiceAroundProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.service.api.ServiceRequest;

public class ThreadNamingInterceptor implements ServiceAroundProcessor<ServiceRequest, Object> {
	private ThreadRenamer threadRenamer = ThreadRenamer.NO_OP;

	@Required
	@Configurable
	public void setThreadRenamer(ThreadRenamer threadRenamer) {
		Objects.requireNonNull(threadRenamer, "threadRenamer cannot be set to null");
		this.threadRenamer = threadRenamer;
	}

	@Override
	public Object process(ServiceRequestContext context, ServiceRequest request, ProceedContext proceedContext) {
		threadRenamer.push(() -> "eval(" + threadNamePart(request) + ")");

		try {
			Object result = proceedContext.proceed(request);
			return result;

		} finally {
			threadRenamer.pop();
		}
	}
	
	private static String threadNamePart(ServiceRequest request) {
		if (request == null) {
			return "null";
		}
		return request.entityType().getShortName();
	}

}
