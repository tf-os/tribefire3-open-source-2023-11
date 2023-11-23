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
import java.math.BigDecimal;

import com.braintribe.ddra.endpoints.api.rest.v2.RestV2EndpointContext;
import com.braintribe.exception.HttpException;
import com.braintribe.model.accessapi.ManipulationRequest;
import com.braintribe.model.accessapi.ManipulationResponse;
import com.braintribe.model.ddra.endpoints.v2.DdraDeletePropertiesEndpoint;
import com.braintribe.model.ddra.endpoints.v2.DdraUrlPathParameters;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.ClearCollectionManipulation;
import com.braintribe.model.generic.manipulation.PropertyManipulation;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.processing.web.rest.HttpExceptions;

public class RestV2DeletePropertiesHandler extends AbstractRestV2Handler<DdraDeletePropertiesEndpoint> {

	@Override
	public void handle(RestV2EndpointContext<DdraDeletePropertiesEndpoint> context) throws IOException {
		DdraUrlPathParameters parameters = context.getParameters();
		DdraDeletePropertiesEndpoint endpoint = decode(context);

		ManipulationRequest request = createManipulationRequestFor(parameters, endpoint);
		computeManipulation(context, request);

		ManipulationResponse response = evaluateServiceRequest(request, true);
		writeResponse(context, project(endpoint, response), false);
	}
	
	private Object project(DdraDeletePropertiesEndpoint endpoint, ManipulationResponse response) {
		switch(endpoint.getProjection()) {
			case envelope:
				return response;
			case success:
				return true;
			default:
				HttpExceptions.internalServerError("Unexpected projection %s", endpoint.getProjection());
				return null;
		}
	}

	private void computeManipulation(RestV2EndpointContext<?> context, ManipulationRequest request) {
		PropertyManipulation manipulation = getManipulation(context);
		computeOwnerForPropertyManipulation(manipulation, context);
		request.setManipulation(manipulation);
	}
	
	private PropertyManipulation getManipulation(RestV2EndpointContext<?> context) {
		GenericModelType type = context.getProperty().getType();
		if(type.isCollection()) {
			return ClearCollectionManipulation.T.create();
		} else {
			ChangeValueManipulation manipulation = ChangeValueManipulation.T.create();
			manipulation.setNewValue(getNullValueFor(type));
			return manipulation;
		}
	}
	
	private Object getNullValueFor(GenericModelType type) {
		switch(type.getTypeCode()) {
			case booleanType:
				return false;
			case decimalType:
				return BigDecimal.ZERO;
			case doubleType:
				return 0.0;
			case floatType:
				return 0f;
			case integerType:
				return 0;
			case longType:
				return 0L;
			case dateType:
			case entityType:
			case enumType:
			case objectType:
			case stringType:
				return null;
			case listType:
			case setType:
			case mapType:
			default:
				throw new HttpException(500, "Unexpected type: " + type.getTypeName());
		}
	}

	@Override
	protected DdraDeletePropertiesEndpoint createEndpoint() {
		return DdraDeletePropertiesEndpoint.T.create();
	}
}
