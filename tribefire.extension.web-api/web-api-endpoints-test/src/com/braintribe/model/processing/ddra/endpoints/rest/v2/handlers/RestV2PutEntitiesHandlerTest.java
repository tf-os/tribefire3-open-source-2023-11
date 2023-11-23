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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

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

public class RestV2PutEntitiesHandlerTest extends AbstractRestV2Test {

	@BeforeClass
	public static void beforeClass() {
		setupServlet();

		RestV2PutEntitiesHandler handler = new RestV2PutEntitiesHandler();
		wireHandler(handler);
		handler.setSessionFactory(sessionFactory);
		addHandler("PUT:entities", handler);
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
		requests.put(getPathEntities(SimpleEntity.T, 1))
				.accept(JSON)
				.contentType(JSON)
				.body("{ \"booleanProperty\": true, \"stringProperty\": \"test\" }")
				.execute(401);
		//@formatter:on
	}

	@Test
	public void noBody() {
		//@formatter:off
		requests.put(getPathEntities(SimpleEntity.T))
				.urlParameter("sessionId", "anything")
				.accept(JSON)
				.contentType(JSON)
				.execute(400);
		//@formatter:on
	}

	@Test
	public void emptyBody() {
		//@formatter:off
		requests.put(getPathEntities(SimpleEntity.T))
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
		requests.put(getPathEntities(SimpleEntity.T))
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
		requests.put(getPathEntities(SimpleEntity.T))
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
		Object id = requests.put(getPathEntities(SimpleEntity.T))
				.urlParameter("sessionId", "anything")
				.accept(JSON)
				.contentType(JSON)
				.body("{ \"booleanProperty\": true, \"stringProperty\": \"" + stringProperty + "\", \"id\": { \"_type\":\"long\", \"value\": 42 } }") 
				.execute(200);
		//@formatter:on

		Assert.assertTrue(id instanceof Long);
		SimpleEntity entity = getEntity(SimpleEntity.T, "stringProperty", stringProperty);
		Assert.assertTrue(entity.getBooleanProperty());
		Assert.assertEquals(stringProperty, entity.getStringProperty());
		Assert.assertEquals(Long.valueOf(42), entity.getId());
	}

	@Test
	public void idAndPartitionInBodyEntityDoesNotExist() {
		String stringProperty = "Created with partition in body via REST!";
		//@formatter:off
		Object id = requests.put(getPathEntities(SimpleEntity.T))
				.urlParameter("sessionId", "anything")
				.accept(JSON)
				.contentType(JSON)
				.body("{ \"booleanProperty\": true, \"stringProperty\": \"" + stringProperty + "\", \"id\": { \"_type\":\"long\", \"value\": 45 }, \"partition\": \"p45\" }")
				.execute(200);
		//@formatter:on

		Assert.assertTrue(id instanceof Long);
		SimpleEntity entity = getEntity(SimpleEntity.T, "stringProperty", stringProperty);
		Assert.assertTrue(entity.getBooleanProperty());
		Assert.assertEquals(stringProperty, entity.getStringProperty());
		Assert.assertEquals(Long.valueOf(45), entity.getId());
		Assert.assertEquals("p45", entity.getPartition());
	}

	@Test
	public void idInBodyEntityUpdate() {
		//@formatter:off
		requests.put(getPathEntities(SimpleEntity.T))
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
		Object id = requests.put(getPathEntities(SimpleEntity.T, 12345))
				.urlParameter("sessionId", "anything")
				.accept(JSON)
				.contentType(JSON)
				.body("{ \"booleanProperty\": true, \"stringProperty\": \"" + stringProperty + "\" }") 
				.execute(200);
		//@formatter:on

		Assert.assertTrue(id instanceof Long);
		SimpleEntity entity = getEntity(SimpleEntity.T, "stringProperty", stringProperty);
		Assert.assertTrue(entity.getBooleanProperty());
		Assert.assertEquals(stringProperty, entity.getStringProperty());
		Assert.assertEquals(Long.valueOf(12345), entity.getId());
	}

	@Test
	public void idAndPartitionInUrlEntityDoesNotExist() {
		String stringProperty = "Created with partition via REST!";
		//@formatter:off
		Object id = requests.put(getPathEntities(SimpleEntity.T, 2222, "s1"))
				.urlParameter("sessionId", "anything")
				.accept(JSON)
				.contentType(JSON)
				.body("{ \"booleanProperty\": true, \"stringProperty\": \"" + stringProperty + "\" }")
				.execute(200);
		//@formatter:on

		Assert.assertTrue(id instanceof Long);
		SimpleEntity entity = getEntity(SimpleEntity.T, "stringProperty", stringProperty);
		Assert.assertTrue(entity.getBooleanProperty());
		Assert.assertEquals(stringProperty, entity.getStringProperty());
		Assert.assertEquals(Long.valueOf(2222), entity.getId());
		Assert.assertEquals("s1", entity.getPartition());
	}

	@Test
	public void idInUrlCreateAndUpdateEntity() {
		requests.put(getPathEntities(SimpleEntity.T, 7)).urlParameter("sessionId", "anything").accept(JSON).contentType(JSON)
				.body("{ \"booleanProperty\": true, \"stringProperty\": \"simple value\", \"partition\": \"test.access\" }").execute(200);

		requests.put(getPathEntities(SimpleEntity.T, 7)).urlParameter("sessionId", "anything").accept(JSON).contentType(JSON)
				.body("{ \"booleanProperty\": false, \"stringProperty\": \"updated value\", \"partition\": \"test.access\" }").execute(200);

		SimpleEntity entity = getEntity(SimpleEntity.T, "id", Long.valueOf(7));
		Assert.assertNotNull(entity);
		Assert.assertFalse(entity.getBooleanProperty());
		Assert.assertEquals("updated value", entity.getStringProperty());
	}

	@Test
	public void idAndPartitionInUrlUpdateEntity() {
		SimpleEntity entity1 = getEntity(SimpleEntity.T, "id", Long.valueOf(1));
		Assert.assertTrue(entity1.getBooleanProperty());
		Assert.assertEquals("se1", entity1.getStringProperty());

		requests.put(getPathEntities(SimpleEntity.T, 1, "p0")).urlParameter("sessionId", "anything").accept(JSON).contentType(JSON)
				.body("{ \"booleanProperty\": false, \"stringProperty\": \"updated value\"}").execute(200);

		SimpleEntity entity = getEntity(SimpleEntity.T, "id", Long.valueOf(1));
		Assert.assertFalse(entity.getBooleanProperty());
		Assert.assertEquals("updated value", entity.getStringProperty());
	}

	@Test
	public void multipleEntitiesUpdateNotAllowed() {
		String stringProperty = "Created via REST!";
		//@formatter:off
		Failure failure = requests.put(getPathEntities(SimpleEntity.T))
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
		Failure failure = requests.put(getPathEntities(SimpleEntity.T))
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
		requests.put(getPathEntities("SimpleEntity", 12345))
				.urlParameter("sessionId", "anything")
				.accept(JSON)
				.contentType(JSON)
				.body("{ \"booleanProperty\": true, \"stringProperty\": \"" + stringProperty + "\" }") 
				.execute(200);
		//@formatter:on

		SimpleEntity entity = getEntity(SimpleEntity.T, "stringProperty", stringProperty);
		Assert.assertTrue(entity.getBooleanProperty());
		Assert.assertEquals(stringProperty, entity.getStringProperty());
		Assert.assertEquals(Long.valueOf(12345), entity.getId());
	}

	@Test
	public void entitySimpleNameAmbiguous() {
		//@formatter:off
		requests.put(getPathEntities("DuplicateSimpleNameEntity", 12345))
				.urlParameter("sessionId", "anything")
				.accept(JSON)
				.contentType(JSON)
				.body("{ \"booleanProperty\": true, \"stringProperty\": \"value\" }") 
				.execute(400);
		//@formatter:on
	}

	@Test
	public void withProjectionEnvelope() {
		Object result = putWithProjection("envelope");
		Assert.assertTrue(result instanceof ManipulationResponse);
	}

	@Test
	public void withProjectionIdInfo() {
		long result = putWithProjection("idInfo");
		Assert.assertEquals(12345, result);
	}

	@Test
	public void withProjectionReferenceInfo() {
		Object result = putWithProjection("referenceInfo");
		Assert.assertTrue(result instanceof PersistentEntityReference);
	}

	@Test
	public void withProjectionSuccess() {
		boolean success = putWithProjection("success");
		Assert.assertTrue(success);
	}

	@Test
	public void withProjectionLocationInfo() {
		String result = putWithProjection("locationInfo");
		String expected = server.getContextUrl().toString() + "/rest/v2/entities/test.access/" + SimpleEntity.class.getName() + "/12345";
		Assert.assertEquals(expected, result);
	}

	@Test
	public void nestedEntityParentAndChildCreated() {
		String stringPropertyParent = "new created complex entity with child";
		String stringPropertyChild = "new another entity child";

		ComplexEntity entity = ComplexEntity.T.create();
		AnotherComplexEntity anotherComplexEntityProperty = AnotherComplexEntity.T.create();

		entity.setId(30L);
		entity.setStringProperty(stringPropertyParent);
		entity.setIntegerProperty(30);
		entity.setBooleanProperty(true);
		entity.setAnotherComplexEntityProperty(anotherComplexEntityProperty);

		anotherComplexEntityProperty.setId(31L);
		anotherComplexEntityProperty.setIntegerProperty(31);
		anotherComplexEntityProperty.setStringProperty(stringPropertyChild);

		ByteArrayOutputStream out = new ByteArrayOutputStream(8192);
		JsonStreamMarshaller marshaller = new JsonStreamMarshaller();

		marshaller.marshall(out, entity, GmSerializationOptions.deriveDefaults().outputPrettiness(OutputPrettiness.high).build());
		requests.put(getPathEntities(ComplexEntity.T)).urlParameter("sessionId", "anything").accept(JSON).contentType(JSON).body(out.toString())
				.execute(200);

		ComplexEntity parent = getEntity(ComplexEntity.T, "id", 30L);
		AnotherComplexEntity child = getEntity(AnotherComplexEntity.T, "id", 31L);

		Assert.assertEquals(stringPropertyParent, parent.getStringProperty());
		Assert.assertEquals(stringPropertyChild, child.getStringProperty());
		Assert.assertTrue(Objects.equals(parent.getAnotherComplexEntityProperty().getId(), child.getId()));
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
		requests.put(getPathEntities(ComplexEntity.T, 1)).urlParameter("sessionId", "anything").accept(JSON).contentType(JSON).body(out.toString())
				.execute(200);

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
		requests.put(getPathEntities(ComplexEntity.T, 13)).urlParameter("sessionId", "anything").accept(JSON).contentType(JSON).body(out.toString())
				.execute(200);

		ComplexEntity createdEntity = getEntity(ComplexEntity.T, "id", Long.valueOf(13));

		Assert.assertEquals("new value", createdEntity.getStringProperty());
		Assert.assertEquals("entity1", createdEntity.getAnotherComplexEntityProperty().getStringProperty());
		Assert.assertEquals("entity2", createdEntity.getAnotherComplexEntityProperty().getAnotherComplexEntityProperty().getStringProperty());
	}

	@Test
	public void nestedEntityParentCreatedAndChildNestedUpdatedBodyId() {
		ComplexEntity entity = ComplexEntity.T.create();

		ByteArrayOutputStream out = new ByteArrayOutputStream(8192);
		JsonStreamMarshaller marshaller = new JsonStreamMarshaller();

		entity.setId(14L);
		entity.setBooleanProperty(true);
		entity.setStringProperty("new value");

		AnotherComplexEntity entity1 = getEntity(AnotherComplexEntity.T, "id", 0L);
		entity1.setStringProperty("entity14");
		AnotherComplexEntity entity2 = getEntity(AnotherComplexEntity.T, "id", 1L);
		entity2.setStringProperty("entity24");
		entity1.setAnotherComplexEntityProperty(entity2);
		entity.setAnotherComplexEntityProperty(entity1);

		marshaller.marshall(out, entity, GmSerializationOptions.deriveDefaults().outputPrettiness(OutputPrettiness.high).build());
		requests.put(getPathEntities(ComplexEntity.T)).urlParameter("sessionId", "anything").accept(JSON).contentType(JSON).body(out.toString())
				.execute(200);

		ComplexEntity createdEntity = getEntity(ComplexEntity.T, "id", Long.valueOf(14));

		Assert.assertEquals("new value", createdEntity.getStringProperty());
		Assert.assertEquals("entity14", createdEntity.getAnotherComplexEntityProperty().getStringProperty());
		Assert.assertEquals("entity24", createdEntity.getAnotherComplexEntityProperty().getAnotherComplexEntityProperty().getStringProperty());
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
		requests.put(getPathEntities(ComplexEntity.T, 1)).urlParameter("sessionId", "anything").accept(JSON).contentType(JSON).body(out.toString())
				.execute(200);

		ComplexEntity updatedEntity = getEntity(ComplexEntity.T, "id", Long.valueOf(1));
		AnotherComplexEntity createdChildEntity = getEntity(AnotherComplexEntity.T, "id", Long.valueOf(10));

		Assert.assertNotNull(createdChildEntity.getId());
		Assert.assertEquals(stringPropertyAnotherCE, createdChildEntity.getStringProperty());
		Assert.assertEquals("new value", updatedEntity.getStringProperty());
		Assert.assertEquals(stringPropertyAnotherCE, updatedEntity.getAnotherComplexEntityProperty().getStringProperty());
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
		Failure failure = requests.put(getPathEntities(ComplexEntity.T)).urlParameter("sessionId", "anything").accept(JSON).contentType(JSON)
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
		Failure failure1 = requests.put(getPathEntities(ComplexEntity.T)).urlParameter("sessionId", "anything").accept(JSON).contentType(JSON)
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
		Failure failure3 = requests.put(getPathEntities(ComplexEntity.T)).urlParameter("sessionId", "anything").accept(JSON).contentType(JSON)
				.body(out.toString()).execute(400);

		Assert.assertEquals("The entity must be fully identified (ID provided) either in the URL path, or in the payload.", failure3.getMessage());
	}

	private <T> T putWithProjection(String projection) {
		//@formatter:off
		return requests.put(getPathEntities(SimpleEntity.T, 12345))
				.urlParameter("sessionId", "anything")
				.urlParameter("projection", projection)
				.accept(JSON)
				.contentType(JSON)
				.body("{ \"booleanProperty\": true }") 
				.execute(200);
		//@formatter:on

	}
}
