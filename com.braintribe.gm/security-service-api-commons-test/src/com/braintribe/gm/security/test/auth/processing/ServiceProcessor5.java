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
package com.braintribe.gm.security.test.auth.processing;

import com.braintribe.gm.security.test.auth.model.ServiceRequest5;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceProcessorException;
import com.braintribe.model.processing.service.api.ServiceRequestContext;

public class ServiceProcessor5 extends AuthorizedServiceProcessorBase implements ServiceProcessor<ServiceRequest5, String> {

	public String identification;

	public ServiceProcessor5() {
	}

	public ServiceProcessor5(String identification) {
		this.identification = identification+"/service";
	}

	@Override
	public String process(ServiceRequestContext requestContext, ServiceRequest5 request) throws ServiceProcessorException {
		if (identification != null) {
			if (identification.equals(request.getServiceId())) {
				return "deployed-"+request.getServiceId();
			} else {
				throw new ServiceProcessorException("Incoming identification \"" + request.getServiceId() + "\" differs from expected " + identification);
			}
		} else {
			return request.getServiceId();
		}
	}

}