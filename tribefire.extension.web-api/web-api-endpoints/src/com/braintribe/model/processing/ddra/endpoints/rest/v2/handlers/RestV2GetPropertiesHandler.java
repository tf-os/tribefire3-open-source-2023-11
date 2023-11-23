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
import com.braintribe.model.accessapi.QueryProperty;
import com.braintribe.model.ddra.endpoints.v2.DdraGetPropertiesEndpoint;
import com.braintribe.model.ddra.endpoints.v2.DdraUrlPathParameters;
import com.braintribe.model.processing.query.fluent.PropertyQueryBuilder;
import com.braintribe.model.processing.web.rest.HttpExceptions;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.PropertyQueryResult;

public class RestV2GetPropertiesHandler extends AbstractQueryingHandler<DdraGetPropertiesEndpoint> {

	@Override
	public void handle(RestV2EndpointContext<DdraGetPropertiesEndpoint> context) throws IOException {
		DdraUrlPathParameters parameters = context.getParameters();
		DdraGetPropertiesEndpoint endpoint = decode(context);

		// @formatter-on
		PropertyQuery query = PropertyQueryBuilder.forProperty(context.getEntityType(), 
					parameters.getEntityId(), parameters.getEntityPartition(), parameters.getProperty())
				.tc(traversingCriteriasMap.getCriterion(endpoint.getComputedDepth()))
				.done();
		// @formatter-on
		query.setNoAbsenceInformation(endpoint.getNoAbsenceInformation());
		
		QueryProperty request = QueryProperty.T.create();
		request.setSessionId(endpoint.getSessionId());
		request.setServiceId(parameters.getAccessId());
		request.setQuery(query);
		
		PropertyQueryResult result = evaluateQueryRequest(request, endpoint, true);
		boolean full = query.getTraversingCriterion() != null;
		writeResponse(context, project(endpoint, result), full);
	}
	
	private Object project(DdraGetPropertiesEndpoint endpoint, PropertyQueryResult value) {
		switch(endpoint.getProjection()) {
			case envelope:
				return value;
			case value:
				return value.getPropertyValue();
			default:
				HttpExceptions.internalServerError("Unexpected endpoint projection %s", endpoint.getProjection());
				return null;
		}
	}

	@Override
	protected DdraGetPropertiesEndpoint createEndpoint() {
		return DdraGetPropertiesEndpoint.T.create();
	}
}
