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
package com.braintribe.model.processing.ddra.endpoints.rest.v2.handlers;

import java.io.IOException;

import com.braintribe.ddra.endpoints.api.rest.v2.RestV2EndpointContext;
import com.braintribe.model.ddra.endpoints.v2.DdraManipulateEntitiesEndpoint;

public class RestV2PutEntitiesHandler extends AbstractManipulateEntitiesHandler {

	@Override
	public void handle(RestV2EndpointContext<DdraManipulateEntitiesEndpoint> context) throws IOException {
		createOrUpdate(context, true, false, false);
	}

}