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
import com.braintribe.model.processing.ddra.endpoints.rest.v2.AbstractRestV2Test;
import com.braintribe.model.service.api.result.Failure;
import com.braintribe.testing.model.test.technical.features.ComplexEntity;
import com.braintribe.testing.model.test.technical.features.SimpleEntity;
import org.junit.*;

import java.io.IOException;

public class RestV2PutPropertiesHandlerTest extends AbstractRestV2Test {

	@BeforeClass
	public static void beforeClass() {
		setupServlet();

		RestV2PutPropertiesHandler handler = new RestV2PutPropertiesHandler();
		wireHandler(handler);
		addHandler("PUT:properties", handler);
	}

	@AfterClass
	public static void afterClass() throws IOException {
		destroy();
	}

	@Before
	public void before() {
		resetServletAccess();
	}

	@Test
	public void noSessionId() {
		//@formatter:off
		requests.put(getPathProperties(SimpleEntity.T, 0, "p0", "stringProperty"))
				.accept(JSON)
				.contentType(JSON)
				.body("\"New Value\"")
				.execute(401);
		//@formatter:on
	}

	@Test
	public void noBody() {
		//@formatter:off
		requests.put(getPathProperties(SimpleEntity.T, 0, "stringProperty"))
				.urlParameter("sessionId", "anything")
				.accept(JSON)
				.contentType(JSON)
				.execute(200);
		//@formatter:on
	}

	@Test
	public void setPropertyWithEntitySimpleName() {
		//@formatter:off
		requests.put(getPathProperties("SimpleEntity", 0, "p0", "stringProperty"))
				.urlParameter("sessionId", "anything")
				.accept(JSON)
				.contentType(JSON)
				.body("\"New Value\"")
				.execute(200);
		//@formatter:on

		SimpleEntity entity = getEntity(SimpleEntity.T, "stringProperty", "New Value");
		Assert.assertEquals(Long.valueOf(0), entity.getId());
		Assert.assertEquals("p0", entity.getPartition());
		Assert.assertEquals("New Value", entity.getStringProperty());
	}

	@Test
	public void setPropertyWithEntitySimpleNameAmbiguous() {
		//@formatter:off
		requests.put(getPathProperties("DuplicateSimpleNameEntity", 0, "p0", "stringProperty"))
				.urlParameter("sessionId", "anything")
				.accept(JSON)
				.contentType(JSON)
				.body("\"New Value\"")
				.execute(400);
		//@formatter:on
	}

	@Test
	public void setStringProperty() {
		//@formatter:off
		boolean success = requests.put(getPathProperties(SimpleEntity.T, 0, "p0", "stringProperty"))
				.urlParameter("sessionId", "anything")
				.accept(JSON)
				.contentType(JSON)
				.body("\"New Value\"")
				.execute(200);
		//@formatter:on

		Assert.assertTrue(success);

		SimpleEntity entity = getEntity(SimpleEntity.T, "stringProperty", "New Value");
		Assert.assertEquals(Long.valueOf(0), entity.getId());
		Assert.assertEquals("p0", entity.getPartition());
		Assert.assertEquals("New Value", entity.getStringProperty());
	}

	@Test
	public void setStringPropertyWrongPartition() {
		//@formatter:off
		Failure failure = requests.put(getPathProperties(SimpleEntity.T, 0, "wrong.partition", "stringProperty"))
				.urlParameter("sessionId", "anything")
				.accept(JSON)
				.contentType(JSON)
				.body("\"New Value\"")
				.execute(404);
		//@formatter:on

		Assert.assertNotNull(failure);
		Assert.assertEquals("No entity of type com.braintribe.testing.model.test.technical.features.SimpleEntity[0, wrong.partition] found!",
				failure.getMessage());
	}

	@Test
	public void setBooleanProperty() {
		SimpleEntity entityBefore = getEntity(SimpleEntity.T, "id", 0L);
		Assert.assertEquals(true, entityBefore.getBooleanProperty());

		//@formatter:off
		requests.put(getPathProperties(SimpleEntity.T, 0, "p0", "booleanProperty"))
				.urlParameter("sessionId", "anything")
				.accept(JSON)
				.contentType(JSON)
				.body("false")
				.execute(200);
		//@formatter:on

		SimpleEntity entity = getEntity(SimpleEntity.T, "id", 0L);
		Assert.assertEquals(Long.valueOf(0), entity.getId());
		Assert.assertEquals("p0", entity.getPartition());
		Assert.assertEquals(false, entity.getBooleanProperty());
	}

	@Test
	public void setEntityProperty() {
		resetServletAccess(true);

		ComplexEntity entityBefore = getEntity(ComplexEntity.T, "id", 7L);
		Assert.assertEquals(null, entityBefore.getComplexEntityProperty());

		//@formatter:off
		requests.put(getPathProperties(ComplexEntity.T, 7, "complexEntityProperty"))
				.urlParameter("sessionId", "anything")
				.accept(JSON)
				.contentType(JSON)
				.body("{ \"id\": { \"_type\": \"long\", \"value\": 3 } }")
				.execute(200);
		//@formatter:on

		ComplexEntity entity = getEntity(ComplexEntity.T, "id", 7L);
		ComplexEntity targetEntity = getEntity(ComplexEntity.T, "id", 3L);
		Assert.assertEquals(Long.valueOf(7L), entity.getId());
		Assert.assertEquals(targetEntity.getGlobalId(), entity.getComplexEntityProperty().getGlobalId());
	}

	@Test
	public void setEntityPropertyNoIdType() {
		resetServletAccess(true);

		ComplexEntity entityBefore = getEntity(ComplexEntity.T, "id", 7L);
		Assert.assertEquals(null, entityBefore.getComplexEntityProperty());

		//@formatter:off
		requests.put(getPathProperties(ComplexEntity.T, 7, "complexEntityProperty"))
				.urlParameter("sessionId", "anything")
				.accept(JSON)
				.contentType(JSON)
				.body("{ \"id\": \"3\" }")
				.execute(404);
		//@formatter:on
	}

	@Test
	public void setEntityPropertyUrlIdAndPartition() {
		resetServletAccess(true);

		ComplexEntity entityBefore = getEntity(ComplexEntity.T, "id", 8L);
		Assert.assertEquals(null, entityBefore.getComplexEntityProperty());

		//@formatter:off
		String pathProperties = getPathProperties(ComplexEntity.T, 8, "cp0", "complexEntityProperty");
		requests.put(pathProperties)
				.urlParameter("sessionId", "anything")
				.accept(JSON)
				.contentType(JSON)
				.body("{ \"id\": { \"_type\": \"long\", \"value\": 3 } }")
				.execute(200);
		//@formatter:on

		ComplexEntity entity = getEntity(ComplexEntity.T, "id", 8L);
		ComplexEntity targetEntity = getEntity(ComplexEntity.T, "id", 3L);
		Assert.assertEquals(Long.valueOf(8L), entity.getId());
		Assert.assertEquals(targetEntity.getGlobalId(), entity.getComplexEntityProperty().getGlobalId());
	}

	@Test
	public void withProjectionSuccess() {
		boolean success = deleteWithProjection("success");
		Assert.assertTrue(success);
	}

	@Test
	public void withProjectionEnvelope() {
		Object result = deleteWithProjection("envelope");
		Assert.assertTrue(result instanceof ManipulationResponse);
	}

	private <T> T deleteWithProjection(String projection) {
		//@formatter:off
		return requests.put(getPathProperties(SimpleEntity.T, 0, "p0", "stringProperty"))
				.urlParameter("sessionId", "anything")
				.urlParameter("projection", projection)
				.accept(JSON)
				.contentType(JSON)
				.body("\"New Value\"")
				.execute(200);
		//@formatter:on
	}
}
