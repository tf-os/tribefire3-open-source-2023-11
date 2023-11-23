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

import com.braintribe.model.accessapi.ManipulationResponse;
import com.braintribe.model.ddra.endpoints.v2.DdraManipulatePropertiesProjection;
import com.braintribe.model.ddra.endpoints.v2.RestV2Endpoint;
import com.braintribe.model.processing.web.rest.HttpExceptions;

public abstract class AbstractManipulationPropertiesHandler<E extends RestV2Endpoint> extends AbstractRestV2Handler<E> {

	protected Object project(DdraManipulatePropertiesProjection projection, ManipulationResponse response) {
		switch(projection) {
			case envelope:
				return response;
			case success:
				return true;
			default:
				HttpExceptions.internalServerError("Unexpected projection %s", projection);
				return null;
		}
	}

}
