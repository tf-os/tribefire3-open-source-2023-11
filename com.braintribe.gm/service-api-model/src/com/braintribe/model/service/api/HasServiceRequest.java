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
package com.braintribe.model.service.api;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * HasServiceRequest is a base type for service requests that wrap a service request in order to execute them in a specific way (e.g. unicast, multicast, async) 
 */
@Abstract
public interface HasServiceRequest extends GenericEntity {
	
	EntityType<HasServiceRequest> T = EntityTypes.T(HasServiceRequest.class);

	/**
	 * The wrapped service request
	 */
	ServiceRequest getServiceRequest();
	void setServiceRequest(ServiceRequest serviceRequest);

	// TODO: find out if this needs to be removed because there seems to be neither use nor understanding of it  
	default AuthorizedRequest authorizedRequest() {
		ServiceRequest request = getServiceRequest();
		if (request.requiresAuthentication()) {
			return (AuthorizedRequest) request;
		}
		return null;
	}
	
}
