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
package com.braintribe.model.processing.rpc.test.service.iface.streaming;

import java.util.Date;

import com.braintribe.model.generic.GenericEntity;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.resource.CallStreamCapture;
import com.braintribe.model.resource.Resource;


public interface StreamingTestServiceRequest extends GenericEntity {

	final EntityType<StreamingTestServiceRequest> T = EntityTypes.T(StreamingTestServiceRequest.class);

	void setResource1(Resource resource1);
	Resource getResource1();

	void setResource2(Resource resource2);
	Resource getResource2();

	void setCapture1(CallStreamCapture capture);
	CallStreamCapture getCapture1();

	void setCapture2(CallStreamCapture capture);
	CallStreamCapture getCapture2();

	void setRequestDate(Date requestDate);
	Date getRequestDate();

}
