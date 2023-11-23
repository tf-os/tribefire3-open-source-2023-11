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

import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.ddra.endpoints.api.rest.v2.RestV2EndpointContext;
import com.braintribe.model.accessapi.ManipulationRequest;
import com.braintribe.model.accessapi.ManipulationResponse;
import com.braintribe.model.ddra.endpoints.v2.DdraPutPropertiesEndpoint;
import com.braintribe.model.ddra.endpoints.v2.DdraUrlPathParameters;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.web.rest.HttpExceptions;

public class RestV2PutPropertiesHandler extends AbstractManipulationPropertiesHandler<DdraPutPropertiesEndpoint> {

	@Override
	public void handle(RestV2EndpointContext<DdraPutPropertiesEndpoint> context) throws IOException {
		handlePutAndPatch(context);
	}

	public void handlePutAndPatch(RestV2EndpointContext<DdraPutPropertiesEndpoint> context) throws IOException {
		DdraUrlPathParameters parameters = context.getParameters();
		checkPropertyType(context);

		DdraPutPropertiesEndpoint endpoint = decode(context);

		ManipulationRequest request = createManipulationRequestFor(parameters, endpoint);

		ChangeValueManipulation manipulation = ChangeValueManipulation.T.create();
		request.setManipulation(manipulation);

		computeOwnerForPropertyManipulation(manipulation, context);

		GmDeserializationOptions options = GmDeserializationOptions.deriveDefaults().setInferredRootType(context.getProperty().getType()).build();

		Object newValue = getReferenceForPropertyValue(context.getProperty().getType(), unmarshallBody(context, endpoint, options),
				context.getProperty());
		manipulation.setNewValue(newValue);

		ManipulationResponse response = evaluateServiceRequest(request, true);
		writeResponse(context, project(endpoint.getProjection(), response), false);
	}

	private void checkPropertyType(RestV2EndpointContext<?> context) {
		Property property = context.getProperty();
		GenericModelType type = property.getType();
		switch (type.getTypeCode()) {
			case listType:
			case setType:
			case mapType:
				HttpExceptions.badRequest(context.getRequest().getMethod() + " for properties is only allowed for non-collection properties, "
						+ "but property %s if of type: %s", property.getName(), type.getTypeName());
				return;
			default:
				// allowed.
		}
	}

	@Override
	protected DdraPutPropertiesEndpoint createEndpoint() {
		return DdraPutPropertiesEndpoint.T.create();
	}
}
