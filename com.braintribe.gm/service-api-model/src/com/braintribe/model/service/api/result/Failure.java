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
package com.braintribe.model.service.api.result;

import java.util.List;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.ServiceRequest;

public interface Failure extends ServiceResult {

	EntityType<Failure> T = EntityTypes.T(Failure.class);

	String getType();
	void setType(String type);

	String getMessage();
	void setMessage(String message);

	String getDetails();
	void setDetails(String details);
	
	String getTracebackId();
	void setTracebackId(String tracebackId);
	
	Failure getCause();
	void setCause(Failure cause);

	List<Failure> getSuppressed();
	void setSuppressed(List<Failure> suppressed);

	ServiceRequest getNotification();
	void setNotification(ServiceRequest value);

	@Override
	default ServiceResultType resultType() {
		return ServiceResultType.failure;
	}

	@Override
	default Failure asFailure() {
		return this;
	}

}
