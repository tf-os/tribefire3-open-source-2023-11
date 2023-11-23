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
package com.braintribe.model.processing.ddra.endpoints.rest.v2;

import com.braintribe.model.processing.ddra.endpoints.rest.v2.handlers.RestV2GetEntitiesHandler;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

public class RestV2RestServletTest extends AbstractRestV2Test {

	@BeforeClass
	public static void beforeClass() {
		setupServlet();

		RestV2GetEntitiesHandler handler = new RestV2GetEntitiesHandler();
		handler.setEvaluator(evaluator);
		handler.setMarshallerRegistry(marshallerRegistry);
		addHandler("GET:entities", handler);
	}

	@AfterClass
	public static void afterClass() throws IOException {
		destroy();
	}

	@Test
	public void unsupportedUrl() {
		requests.get("/tribefire-services/rest/v2/incorrect/test.access/testEntity").accept(JSON).execute(404);
	}
}
