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
package com.braintribe.model.processing.rpc.test.service.processor.failure;

import com.braintribe.common.ExceptionBuilder;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceProcessorException;
import com.braintribe.model.processing.service.api.ServiceRequestContext;

public class FailureTestServiceProcessor implements ServiceProcessor<FailureTestServiceProcessorRequest, Boolean> {

	@Override
	public Boolean process(ServiceRequestContext requestContext, FailureTestServiceProcessorRequest request)
			throws ServiceProcessorException {

		if (request.getExceptionType() == null) {
			throw new ServiceProcessorException("SPE with no cause");
		}

		Throwable createException = null;
		try {
			createException = ExceptionBuilder.createException(request.getExceptionType(),
					"Implementation thrown " + request.getExceptionType());

		} catch (Exception e) {
			throw new ServiceProcessorException("Failed to build " + request.getExceptionType(), e);
		}

		if (createException == null) {
			throw new ServiceProcessorException("null instead of " + request.getExceptionType());
		}

		if (createException instanceof RuntimeException) {
			throw (RuntimeException) createException;
		}

		if (createException instanceof Error) {
			throw (Error) createException;
		}

		throw new ServiceProcessorException(createException.getMessage(), createException);

	}

}
