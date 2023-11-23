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

import com.braintribe.model.processing.ddra.endpoints.rest.v2.AbstractRestV2Test;
import com.braintribe.model.query.PropertyQueryResult;
import com.braintribe.testing.model.test.technical.features.SimpleEntity;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

public class RestV2GetPropertiesHandlerTest extends AbstractRestV2Test {

	@BeforeClass
	public static void beforeClass() {
		setupServlet();

		RestV2GetPropertiesHandler handler = new RestV2GetPropertiesHandler();
		wireHandler(handler);
		addHandler("GET:properties", handler);
	}

	@AfterClass
	public static void afterClass() throws IOException {
		destroy();
	}

	@Test
	public void simpleProperty() {
		//@formatter:off
		String value = requests.get()
				.path(getPathProperties(SimpleEntity.T, 4, "p0", "stringProperty"))
				.urlParameter("sessionId", "anything")
				.accept(JSON)
				.execute(200);
		//@formatter:on

		Assert.assertEquals("se4", value);
	}

	@Test
	public void byEntitySimpleName() {
		//@formatter:off
		String value = requests.get()
				.path(getPathProperties("SimpleEntity", 4, "p0", "stringProperty"))
				.urlParameter("sessionId", "anything")
				.accept(JSON)
				.execute(200);
		//@formatter:on

		Assert.assertEquals("se4", value);
	}

	@Test
	public void byEntitySimpleNameAmbiguous() {
		//@formatter:off
		requests.get()
				.path(getPathProperties("DuplicateSimpleNameEntity", 4, "p0", "stringProperty"))
				.urlParameter("sessionId", "anything")
				.accept(JSON)
				.execute(400);
		//@formatter:on
	}

	@Test
	public void propertyDoesNotExist() {
		//@formatter:off
		requests.get()
				.path(getPathProperties(SimpleEntity.T, 4, "p0", "thisPropertyDoesNotExist"))
				.urlParameter("sessionId", "anything")
				.accept(JSON)
				.execute(404);
		//@formatter:on
	}

	@Test
	public void noSessionId() {
		//@formatter:off
		requests.get()
				.path(getPathProperties(SimpleEntity.T, 4, "p0", "stringProperty"))
				.accept(JSON)
				.execute(401);
		//@formatter:on

	}

	@Test
	public void withProjectionValue() {
		//@formatter:off
		Object value = requests.get()
				.path(getPathProperties(SimpleEntity.T, 4, "p0", "stringProperty"))
				.urlParameter("sessionId", "anything")
				.urlParameter("projection", "value")
				.accept(JSON)
				.execute(200);
		//@formatter:on

		Assert.assertTrue(value instanceof String);
	}

	@Test
	public void withProjectionEnvelope() {
		//@formatter:off
		Object value = requests.get()
				.path(getPathProperties(SimpleEntity.T, 4, "p0", "stringProperty"))
				.urlParameter("sessionId", "anything")
				.urlParameter("projection", "envelope")
				.accept(JSON)
				.execute(200);
		//@formatter:on

		Assert.assertTrue(value instanceof PropertyQueryResult);
	}
}
