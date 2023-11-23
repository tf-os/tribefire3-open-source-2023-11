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

import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.OutputPrettiness;
import com.braintribe.codec.marshaller.json.JsonStreamMarshaller;
import com.braintribe.model.accessapi.ManipulationResponse;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.processing.ddra.endpoints.rest.v2.AbstractRestV2Test;
import com.braintribe.model.service.api.result.Failure;
import com.braintribe.testing.model.test.technical.features.AnotherComplexEntity;
import com.braintribe.testing.model.test.technical.features.ComplexEntity;
import com.braintribe.testing.model.test.technical.features.SimpleEntity;
import org.junit.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class RestV2PatchEntitiesHandlerTest extends AbstractRestV2Test {

	@BeforeClass
	public static void beforeClass() {
		setupServlet();

		RestV2PatchEntitiesHandler handler = new RestV2PatchEntitiesHandler();
		wireHandler(handler);
		handler.setSessionFactory(sessionFactory);
		addHandler("PATCH:entities", handler);
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
		requests.patch(getPathEntities(SimpleEntity.T, 1))
				.accept(JSON)
				.contentType(JSON)
				.body("{ \"booleanProperty\": true, \"stringProperty\": \"test\" }")
				.execute(401);
		//@formatter:on
	}

	@Test
	public void noBody() {
		//@formatter:off
		requests.patch(getPathEntities(SimpleEntity.T))
				.urlParameter("sessionId", "anything")
				.accept(JSON)
				.contentType(JSON)
				.execute(400);
		//@formatter:on
	}

	@Test
	public void emptyBody() {
		//@formatter:off
		requests.patch(getPathEntities(SimpleEntity.T))
				.urlParameter("sessionId", "anything")
				.accept(JSON)
				.contentType(JSON)
				.body("")
				.execute(400);
		//@formatter:on
	}

	@Test
	public void invalidBody() {
		//@formatter:off
		requests.patch(getPathEntities(SimpleEntity.T))
				.urlParameter("sessionId", "anything")
				.accept(JSON)
				.contentType(JSON)
				.body("I bet you check this after reading the other one, heh? ;)")
				.execute(400);
		//@formatter:on
	}

	@Test
	public void noId() {
		//@formatter:off
		requests.patch(getPathEntities(SimpleEntity.T))
				.urlParameter("sessionId", "anything")
				.accept(JSON)
				.contentType(JSON)
				.body("{ \"booleanProperty\": true, \"stringProperty\": \"aaa\" }") 
				.execute(400);
		//@formatter:on
	}

	@Test
	public void idInBodyEntityDoesNotExist() {
		String stringProperty = "Created via REST!";
		//@formatter:off
		requests.patch(getPathEntities(SimpleEntity.T))
				.urlParameter("sessionId", "anything")
				.accept(JSON)
				.contentType(JSON)
				.body("{ \"booleanProperty\": true, \"stringProperty\": \"" + stringProperty + "\", \"id\": { \"_type\":\"long\", \"value\": 42 } }") 
				.execute(400);
		//@formatter:on
	}

	@Test
	public void idInBodyEntityUpdate() {
		//@formatter:off
		requests.patch(getPathEntities(SimpleEntity.T))
				.urlParameter("sessionId", "anything")
				.accept(JSON)
				.contentType(JSON)
				.body("{ \"booleanProperty\": false, \"stringProperty\": \"updated value\", \"id\": { \"_type\":\"long\", \"value\": 1 }, \"partition\": \"p0\" }")
				.execute(200);
		//@formatter:on

		SimpleEntity entity = getEntity(SimpleEntity.T, "id", Long.valueOf(1));
		Assert.assertFalse(entity.getBooleanProperty());
		Assert.assertEquals("updated value", entity.getStringProperty());
	}

	@Test
	public void idInUrlEntityDoesNotExist() {
		String stringProperty = "Created via REST!";
		//@formatter:off
		requests.patch(getPathEntities(SimpleEntity.T, 12345))
				.urlParameter("sessionId", "anything")
				.accept(JSON)
				.contentType(JSON)
				.body("{ \"booleanProperty\": true, \"stringProperty\": \"" + stringProperty + "\" }") 
				.execute(404);
		//@formatter:on
	}

	@Test
	public void idInUrlUpdateEntity() {
		requests.patch(getPathEntities(SimpleEntity.T, 1))
				.urlParameter("sessionId", "anything")
				.accept(JSON)
				.contentType(JSON)
				.body("{ \"booleanProperty\": false, \"stringProperty\": \"updated value\", \"partition\": \"p0\" }")
				.execute(200);

		SimpleEntity entity = getEntity(SimpleEntity.T, "id", Long.valueOf(1));
		Assert.assertNotNull(entity);
		Assert.assertFalse(entity.getBooleanProperty());
		Assert.assertEquals("updated value", entity.getStringProperty());
	}

	@Test
	public void idAndPartitionInUrlUpdateEntity() {
		requests.patch(getPathEntities(SimpleEntity.T, 1, "p0"))
				.urlParameter("sessionId", "anything")
				.accept(JSON)
				.contentType(JSON)
				.body("{ \"booleanProperty\": false, \"stringProperty\": \"updated value\"}")
				.execute(200);

		SimpleEntity entity = getEntity(SimpleEntity.T, "id", Long.valueOf(1));
		Assert.assertNotNull(entity);
		Assert.assertFalse(entity.getBooleanProperty());
		Assert.assertEquals("updated value", entity.getStringProperty());
	}

	@Test
	public void idAndWrongPartitionInUrlUpdateEntity() {
		requests.patch(getPathEntities(SimpleEntity.T, 1, "p99"))
				.urlParameter("sessionId", "anything")
				.accept(JSON)
				.contentType(JSON)
				.body("{ \"booleanProperty\": false, \"stringProperty\": \"updated value\"}")
				.execute(404);
	}

	@Test
	public void multipleEntitiesUpdateNotAllowed() {
		String stringProperty = "Created via REST!";
		//@formatter:off
		Failure failure = requests.patch(getPathEntities(SimpleEntity.T))
				.urlParameter("sessionId", "anything")
				.urlParameter("listEntitiesRequest", "true")
				.accept(JSON)
				.contentType(JSON)
				.body("[{ \"id\": {\"_type\": \"long\", \"value\": \"1\"}, \"booleanProperty\": true, \"stringProperty\": \"" + stringProperty + "\" }, "
						+ "{\"id\": {\"_type\": \"long\", \"value\": \"2\"}, \"booleanProperty\": true, \"stringProperty\": \"" + stringProperty + "\" }]")
				.execute(400);
		//@formatter:on

		Assert.assertEquals("The body must only contain one entity.", failure.getMessage());
	}

	@Test
	public void multipleEntitiesCreateNotAllowed() {
		String stringProperty = "Created via REST!";
		//@formatter:off
		Failure failure = requests.patch(getPathEntities(SimpleEntity.T))
				.urlParameter("sessionId", "anything")
				.urlParameter("listEntitiesRequest", "true")
				.accept(JSON)
				.contentType(JSON)
				.body("[{ \"id\": {\"_type\": \"long\", \"value\": \"91\"}, \"booleanProperty\": true, \"stringProperty\": \"" + stringProperty + "\" }, "
						+ "{\"id\": {\"_type\": \"long\", \"value\": \"92\"}, \"booleanProperty\": true, \"stringProperty\": \"" + stringProperty + "\" }]")
				.execute(400);
		//@formatter:on

		Assert.assertEquals("The body must only contain one entity.", failure.getMessage());
	}

	@Test
	public void entitySimpleName() {
		String stringProperty = "Created via REST with simple name!";
		//@formatter:off
		requests.patch(getPathEntities("SimpleEntity", 1))
				.urlParameter("sessionId", "anything")
				.accept(JSON)
				.contentType(JSON)
				.body("{ \"booleanProperty\": true, \"partition\": \"p0\", \"stringProperty\": \"" + stringProperty + "\" }")
				.execute(200);
		//@formatter:on

		SimpleEntity entity = getEntity(SimpleEntity.T, "stringProperty", stringProperty);
		Assert.assertTrue(entity.getBooleanProperty());
		Assert.assertEquals(stringProperty, entity.getStringProperty());
		Assert.assertEquals(Long.valueOf(1), entity.getId());
	}

	@Test
	public void entitySimpleNameAmbiguous() {
		//@formatter:off
		requests.patch(getPathEntities("DuplicateSimpleNameEntity", 12345))
				.urlParameter("sessionId", "anything")
				.accept(JSON)
				.contentType(JSON)
				.body("{ \"booleanProperty\": true, \"stringProperty\": \"value\" }") 
				.execute(400);
		//@formatter:on
	}

	@Test
	public void nestedEntityParentAndChildWithIdsNoExisting() {
		String stringPropertyParent = "new created complex entity with child";
		String stringPropertyChild = "new another entity child";

		ComplexEntity entity = ComplexEntity.T.create();
		AnotherComplexEntity anotherComplexEntityProperty = AnotherComplexEntity.T.create();

		entity.setId(40L);
		entity.setStringProperty(stringPropertyParent);
		entity.setAnotherComplexEntityProperty(anotherComplexEntityProperty);

		anotherComplexEntityProperty.setId(41L);
		anotherComplexEntityProperty.setStringProperty(stringPropertyChild);

		ByteArrayOutputStream out = new ByteArrayOutputStream(8192);
		JsonStreamMarshaller marshaller = new JsonStreamMarshaller();

		marshaller.marshall(out, entity, GmSerializationOptions.deriveDefaults().outputPrettiness(OutputPrettiness.high).build());
		Failure failure = requests.patch(getPathEntities(ComplexEntity.T)).urlParameter("sessionId", "anything").accept(JSON).contentType(JSON)
				.body(out.toString()).execute(400);

		Assert.assertEquals("No entity of type com.braintribe.testing.model.test.technical.features.AnotherComplexEntity[41, *] found!", failure.getMessage());
	}

	@Test
	public void nestedEntityUpdateParentAndChild() {
		ComplexEntity entity = getEntity(ComplexEntity.T, "id", Long.valueOf(1));
		Assert.assertNull(entity.getBooleanProperty());
		Assert.assertEquals("1", entity.getStringProperty());

		ByteArrayOutputStream out = new ByteArrayOutputStream(8192);
		JsonStreamMarshaller marshaller = new JsonStreamMarshaller();

		entity.setBooleanProperty(true);
		entity.setStringProperty("new value");
		entity.getAnotherComplexEntityProperty().setStringProperty("new value on another complex property");
		entity.getAnotherComplexEntityProperty().getAnotherComplexEntityProperty().setStringProperty("new value another another complex property");

		marshaller.marshall(out, entity, GmSerializationOptions.deriveDefaults().outputPrettiness(OutputPrettiness.high).build());
		requests.patch(getPathEntities(ComplexEntity.T, 1)).urlParameter("sessionId", "anything").accept(JSON).contentType(JSON)
				.body(out.toString()).execute(200);

		ComplexEntity updatedEntity = getEntity(ComplexEntity.T, "id", Long.valueOf(1));
		Assert.assertEquals("new value", updatedEntity.getStringProperty());
		Assert.assertEquals("new value on another complex property", updatedEntity.getAnotherComplexEntityProperty().getStringProperty());
		Assert.assertEquals("new value another another complex property",
				updatedEntity.getAnotherComplexEntityProperty().getAnotherComplexEntityProperty().getStringProperty());
	}

	@Test
	public void nestedEntityParentCreatedAndChildUpdatedUrlId() {
		ComplexEntity entity = ComplexEntity.T.create();

		ByteArrayOutputStream out = new ByteArrayOutputStream(8192);
		JsonStreamMarshaller marshaller = new JsonStreamMarshaller();

		entity.setId(null);
		entity.setBooleanProperty(true);
		entity.setStringProperty("new value");

		AnotherComplexEntity entity1 = getEntity(AnotherComplexEntity.T, "id", 1L);
		entity1.setStringProperty("entity1");
		AnotherComplexEntity entity2 = getEntity(AnotherComplexEntity.T, "id", 2L);
		entity2.setStringProperty("entity2");
		entity1.setAnotherComplexEntityProperty(entity2);
		entity.setAnotherComplexEntityProperty(entity1);

		marshaller.marshall(out, entity, GmSerializationOptions.deriveDefaults().outputPrettiness(OutputPrettiness.high).build());
		Failure failure = requests.patch(getPathEntities(ComplexEntity.T, 15)).urlParameter("sessionId", "anything").accept(JSON).contentType(JSON)
				.body(out.toString()).execute(404);

		Assert.assertEquals("No entity of type com.braintribe.testing.model.test.technical.features.ComplexEntity[15, *] found!", failure.getMessage());
	}

	@Test
	public void nestedEntityParentCreatedAndChildNestedUpdatedBodyId() {
		ComplexEntity entity = ComplexEntity.T.create();

		ByteArrayOutputStream out = new ByteArrayOutputStream(8192);
		JsonStreamMarshaller marshaller = new JsonStreamMarshaller();

		entity.setId(16L);
		entity.setBooleanProperty(true);
		entity.setStringProperty("new value");

		AnotherComplexEntity entity1 = getEntity(AnotherComplexEntity.T, "id", 0L);
		entity1.setStringProperty("entity14");
		AnotherComplexEntity entity2 = getEntity(AnotherComplexEntity.T, "id", 1L);
		entity2.setStringProperty("entity24");
		entity1.setAnotherComplexEntityProperty(entity2);
		entity.setAnotherComplexEntityProperty(entity1);

		marshaller.marshall(out, entity, GmSerializationOptions.deriveDefaults().outputPrettiness(OutputPrettiness.high).build());
		Failure failure = requests.patch(getPathEntities(ComplexEntity.T)).urlParameter("sessionId", "anything").accept(JSON).contentType(JSON)
				.body(out.toString()).execute(400);

		Assert.assertEquals("No entity of type com.braintribe.testing.model.test.technical.features.ComplexEntity[16, *] found!", failure.getMessage());
	}

	@Test
	public void nestedEntityParentUpdateAndChildCreatedWithId() {
		ComplexEntity entity = getEntity(ComplexEntity.T, "id", Long.valueOf(1));
		Assert.assertNull(entity.getBooleanProperty());
		Assert.assertEquals("1", entity.getStringProperty());

		ByteArrayOutputStream out = new ByteArrayOutputStream(8192);
		JsonStreamMarshaller marshaller = new JsonStreamMarshaller();

		entity.setBooleanProperty(true);
		entity.setStringProperty("new value");

		AnotherComplexEntity complexEntity = AnotherComplexEntity.T.create();
		complexEntity.setId(10L);
		String stringPropertyAnotherCE = "new another complex entity";
		complexEntity.setStringProperty(stringPropertyAnotherCE);

		entity.setAnotherComplexEntityProperty(complexEntity);

		marshaller.marshall(out, entity, GmSerializationOptions.deriveDefaults().outputPrettiness(OutputPrettiness.high).build());
		Failure failure = requests.patch(getPathEntities(ComplexEntity.T, 1)).urlParameter("sessionId", "anything").accept(JSON).contentType(JSON)
				.body(out.toString()).execute(404);

		Assert.assertEquals("No entity of type com.braintribe.testing.model.test.technical.features.AnotherComplexEntity[10, *] found!", failure.getMessage());
	}

	@Test
	public void nestedEntityParentAndChildMissingIds() {
		ComplexEntity entity = getEntity(ComplexEntity.T, "id", Long.valueOf(1));
		Assert.assertNull(entity.getBooleanProperty());
		Assert.assertEquals("1", entity.getStringProperty());

		ByteArrayOutputStream out = new ByteArrayOutputStream(8192);
		JsonStreamMarshaller marshaller = new JsonStreamMarshaller();

		entity.setId(null);
		entity.setBooleanProperty(true);
		entity.setStringProperty("new value");

		AnotherComplexEntity complexEntity = AnotherComplexEntity.T.create();
		complexEntity.setIntegerProperty(20);
		complexEntity.setStringProperty("20");

		entity.setAnotherComplexEntityProperty(complexEntity);

		marshaller.marshall(out, entity, GmSerializationOptions.deriveDefaults().outputPrettiness(OutputPrettiness.high).build());
		Failure failure = requests.patch(getPathEntities(ComplexEntity.T)).urlParameter("sessionId", "anything").accept(JSON).contentType(JSON)
				.body(out.toString()).execute(400);

		Assert.assertEquals("The entity must be fully identified (ID provided) either in the URL path, or in the payload.", failure.getMessage());

	}

	@Test
	public void nestedEntityParentWithIdAndChildIdMissing() {
		ComplexEntity entity = getEntity(ComplexEntity.T, "id", Long.valueOf(1));
		Assert.assertNull(entity.getBooleanProperty());
		Assert.assertEquals("1", entity.getStringProperty());

		ByteArrayOutputStream out = new ByteArrayOutputStream(8192);
		JsonStreamMarshaller marshaller = new JsonStreamMarshaller();

		entity.setId(1);
		entity.setBooleanProperty(true);
		entity.setStringProperty("new value");

		AnotherComplexEntity complexEntity = AnotherComplexEntity.T.create();
		complexEntity.setIntegerProperty(20);
		complexEntity.setStringProperty("20");

		entity.setAnotherComplexEntityProperty(complexEntity);

		marshaller.marshall(out, entity, GmSerializationOptions.deriveDefaults().outputPrettiness(OutputPrettiness.high).build());
		Failure failure1 = requests.patch(getPathEntities(ComplexEntity.T)).urlParameter("sessionId", "anything").accept(JSON).contentType(JSON)
				.body(out.toString()).execute(400);

		Assert.assertEquals("The entity must be fully identified (ID provided) either in the URL path, or in the payload.", failure1.getMessage());

	}

	@Test
	public void nestedEntityParentIdMissingAndChildWithIds() {
		ComplexEntity entity = getEntity(ComplexEntity.T, "id", Long.valueOf(1));
		Assert.assertNull(entity.getBooleanProperty());
		Assert.assertEquals("1", entity.getStringProperty());

		ByteArrayOutputStream out = new ByteArrayOutputStream(8192);
		JsonStreamMarshaller marshaller = new JsonStreamMarshaller();

		entity.setId(null);
		entity.setBooleanProperty(true);
		entity.setStringProperty("new value");

		AnotherComplexEntity complexEntity = AnotherComplexEntity.T.create();
		complexEntity.setIntegerProperty(20);
		complexEntity.setStringProperty("20");
		complexEntity.setId(0L);

		entity.setAnotherComplexEntityProperty(complexEntity);

		marshaller.marshall(out, entity, GmSerializationOptions.deriveDefaults().outputPrettiness(OutputPrettiness.high).build());
		Failure failure3 = requests.patch(getPathEntities(ComplexEntity.T))
				.urlParameter("sessionId", "anything")
				.accept(JSON)
				.contentType(JSON)
				.body(out.toString()).execute(400);

		Assert.assertEquals("The entity must be fully identified (ID provided) either in the URL path, or in the payload.", failure3.getMessage());
	}

	@Test
	public void withProjectionEnvelope() {
		Object result = patchWithProjection("envelope");
		Assert.assertTrue(result instanceof ManipulationResponse);
	}

	@Test
	public void withProjectionIdInfo() {
		long result = patchWithProjection("idInfo");
		Assert.assertEquals(1, result);
	}

	@Test
	public void withProjectionReferenceInfo() {
		Object result = patchWithProjection("referenceInfo");
		Assert.assertTrue(result instanceof PersistentEntityReference);
	}

	@Test
	public void withProjectionSuccess() {
		boolean success = patchWithProjection("success");
		Assert.assertTrue(success);
	}

	@Test
	public void withProjectionLocationInfo() {
		String result = patchWithProjection("locationInfo");
		String expected = server.getContextUrl().toString() + "/rest/v2/entities/test.access/" + SimpleEntity.class.getName() + "/1";
		Assert.assertEquals(expected, result);
	}

	private <T> T patchWithProjection(String projection) {
		//@formatter:off
		return requests.patch(getPathEntities(SimpleEntity.T, 1))
				.urlParameter("sessionId", "anything")
				.urlParameter("projection", projection)
				.accept(JSON)
				.contentType(JSON)
				.body("{ \"booleanProperty\": true, \"partition\":\"p0\" }")
				.execute(200);
		//@formatter:on
	}
}
