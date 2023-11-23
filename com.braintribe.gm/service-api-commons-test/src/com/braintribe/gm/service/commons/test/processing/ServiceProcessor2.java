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
package com.braintribe.gm.service.commons.test.processing;

import com.braintribe.gm.service.commons.test.model.ServiceRequest2;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;

public class ServiceProcessor2 extends AuthorizedServiceProcessorBase implements ServiceProcessor<ServiceRequest2, Long> {
	
	public static final Long RETURN = System.currentTimeMillis();
	public static final Class<? extends Exception> EXCEPTION_TYPE = IndexOutOfBoundsException.class;

	@Override
	public Long process(ServiceRequestContext context, ServiceRequest2 parameter) throws IndexOutOfBoundsException {
		if (parameter.getForceException()) {
			throw new IndexOutOfBoundsException("Enforced business exception for: " + parameter);
		}
		validate(context, parameter);
		return RETURN;
	}

}