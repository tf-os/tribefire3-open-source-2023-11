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

import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * Request types derived from  DispatchableRequest allow to choose an individual processor per request using the serviceId property to select it. 
 * The DispatchableRequest can be compared with an instance bound method in an object oriented programming language while a non dispatchable request
 * can be compared with a static method.
 * 
 * @author Dirk Scheffler
 *
 */
@Abstract
public interface DispatchableRequest extends ServiceRequest {

	EntityType<DispatchableRequest> T = EntityTypes.T(DispatchableRequest.class);

	/**
	 * The optional id of the processor that is to be used to process the request.
	 */
	String getServiceId();
	void setServiceId(String serviceId);

	@Override
	default boolean dispatchable() {
		return true;
	}

}
