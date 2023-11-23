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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.braintribe.ddra.endpoints.api.rest.v2.RestV2EndpointContext;
import com.braintribe.model.ddra.endpoints.v2.RestV2Endpoint;

public interface RestV2Handler<E extends RestV2Endpoint> {

	void handle(RestV2EndpointContext<E> context) throws IOException;
	
	RestV2EndpointContext<E> createContext(HttpServletRequest request, HttpServletResponse response);
}