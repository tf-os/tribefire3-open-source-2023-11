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
import java.util.List;
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
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.processing.ddra.endpoints.rest.v2.AbstractRestV2Test;
import com.braintribe.model.service.api.result.Failure;
import com.braintribe.testing.model.test.technical.features.AnotherComplexEntity;
import com.braintribe.testing.model.test.technical.features.ComplexEntity;
import com.braintribe.testing.model.test.technical.features.SimpleEntity;

public class RestV2PostEntitiesHandlerTest extends AbstractRestV2Test {

	@BeforeClass
	public static void beforeClass() {
		setupServlet();

		RestV2PostEntitiesHandler handler = new RestV2PostEntitiesHandler();
		wireHandler(handler);
		handler.setSessionFactory(sessionFactory);
		addHandler("POST:entities", handler);
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
		requests.post(getPathEntities(SimpleEntity.T))
				.accept(JSON)
				.contentType(JSON)
				.body("{ \"booleanProperty\": true, \"stringProperty\": \"test\" }")
				.execute(401);
		//@formatter:on
	}

	@Test
	public void noBody() {
		//@formatter:off
		requests.post(getPathEntities(SimpleEntity.T))
				.urlParameter("sessionId", "anything")
				.accept(JSON)
				.contentType(JSON)
				.execute(400);
		//@formatter:on
	}

	@Test
	public void emptyBody() {
		//@formatter:off
		requests.post(getPathEntities(SimpleEntity.T))
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
		requests.post(getPathEntities(SimpleEntity.T))
				.urlParameter("sessionId", "anything")
				.accept(JSON)
				.contentType(JSON)
				.body("Hello there! I am glad SOMEONE is ready my tests ;) Hope you enjoy the experience...")
				.execute(400);
		//@formatter:on
	}

	@Test
	public void noId() {
		String stringProperty = "Created via REST!";
		//@formatter:off
		Object id = requests.post(getPathEntities(SimpleEntity.T))
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
	}

	@Test
	public void noIdMultipleEntitiesCreate() {
		String stringProperty = "Created via REST!";
		//@formatter:off
		List ids = requests.post(getPathEntities(SimpleEntity.T))
				.urlParameter("sessionId", "anything")
				.urlParameter("listEntitiesRequest", "true")
				.accept(JSON)
				.contentType(JSON)
				.body("[{ \"booleanProperty\": true, \"stringProperty\": \"" + stringProperty + "\" }, { \"booleanProperty\": true, \"stringProperty\": \"" + stringProperty + "\" }]")
				.execute(200);
		//@formatter:on

		Assert.assertEquals(2, ids.size());
	}

	@Test
	public void noIdMultipleEntitiesCreateHeaderParam() {
		String stringProperty = "Created via REST!";
		//@formatter:off
		List ids = requests.post(getPathEntities(SimpleEntity.T))
				.urlParameter("sessionId", "anything")
				.header("gm-list-entities-request", "true")
				.accept(JSON)
				.contentType(JSON)
				.body("[{ \"booleanProperty\": true, \"stringProperty\": \"" + stringProperty + "\" }, { \"booleanProperty\": true, \"stringProperty\": \"" + stringProperty + "\" }]")
				.execute(200);
		//@formatter:on

		Assert.assertEquals(2, ids.size());
	}

	@Test
	public void noIdMultipleEntitiesUpdate() {
		List<GenericEntity> entities = getEntities(SimpleEntity.T);
		String stringProperty = "Created via REST!";
		//@formatter:off
		List ids = requests.post(getPathEntities(SimpleEntity.T))
				.urlParameter("sessionId", "anything")
				.urlParameter("listEntitiesRequest", "true")
				.accept(JSON)
				.contentType(JSON)
				.body("[{ \"id\": {\"_type\": \"long\", \"value\": \"1\"}, \"booleanProperty\": true, \"stringProperty\": \"" + stringProperty + "\" }, "
						+ "{\"id\": {\"_type\": \"long\", \"value\": \"2\"}, \"booleanProperty\": true, \"stringProperty\": \"" + stringProperty + "\" }]")
				.execute(200);
		//@formatter:on

		Assert.assertEquals(2, ids.size());
		List<GenericEntity> entities2 = getEntities(SimpleEntity.T);
		Assert.assertEquals(entities.size(), entities2.size());
	}

	@Test
	public void noIdSimpleEntityName() {
		String stringProperty = "Created with simple name!";
		//@formatter:off
		Object id = requests.post(getPathEntities("SimpleEntity"))
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
	}

	@Test
	public void ambiguousSimpleEntityName() {
		//@formatter:off
		requests.post(getPathEntities("DuplicateSimpleNameEntity"))
				.urlParameter("sessionId", "anything")
				.accept(JSON)
				.contentType(JSON)
				.body("{ \"booleanProperty\": true, \"stringProperty\": \"value\" }")
				.execute(400);
		//@formatter:on
	}

	@Test
	public void idInBodyEntityDoesNotExist() {
		//@formatter:off
		requests.post(getPathEntities(SimpleEntity.T))
				.urlParameter("sessionId", "anything")
				.accept(JSON)
				.contentType(JSON)
				.body("{ \"booleanProperty\": true, \"stringProperty\": \"test\", \"id\": { \"_type\":\"long\", \"value\": 42 } }") 
				.execute(400);
		//@formatter:on
	}

	@Test
	public void idInBodyEntityUpdate() {
		//@formatter:off
		requests.post(getPathEntities(SimpleEntity.T))
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
		//@formatter:off
		requests.post(getPathEntities(SimpleEntity.T, 12345))
				.urlParameter("sessionId", "anything")
				.accept(JSON)
				.contentType(JSON)
				.body("{ \"booleanProperty\": true, \"stringProperty\": \"test\" }") 
				.execute(404);
		//@formatter:on
	}

	@Test
	public void idInUrlUpdateEntity() {
		SimpleEntity entity1 = getEntity(SimpleEntity.T, "id", Long.valueOf(1));
		Assert.assertTrue(entity1.getBooleanProperty());
		Assert.assertEquals("se1", entity1.getStringProperty());

		requests.post(getPathEntities(SimpleEntity.T, 1)).urlParameter("sessionId", "anything").accept(JSON).contentType(JSON)
				.body("{ \"booleanProperty\": false, \"stringProperty\": \"updated value\", \"partition\": \"p0\" }").execute(200);

		SimpleEntity entity = getEntity(SimpleEntity.T, "id", Long.valueOf(1));
		Assert.assertFalse(entity.getBooleanProperty());
		Assert.assertEquals("updated value", entity.getStringProperty());
	}

	@Test
	public void idAndPartitionInUrlUpdateEntity() {
		SimpleEntity entity1 = getEntity(SimpleEntity.T, "id", Long.valueOf(1));
		Assert.assertTrue(entity1.getBooleanProperty());
		Assert.assertEquals("se1", entity1.getStringProperty());

		requests.post(getPathEntities(SimpleEntity.T, 1, "p0")).urlParameter("sessionId", "anything").accept(JSON).contentType(JSON)
				.body("{ \"booleanProperty\": false, \"stringProperty\": \"updated value\"}").execute(200);

		SimpleEntity entity = getEntity(SimpleEntity.T, "id", Long.valueOf(1));
		Assert.assertFalse(entity.getBooleanProperty());
		Assert.assertEquals("updated value", entity.getStringProperty());
	}

	@Test
	public void nestedEntityParentAndChildCreated() {
		ComplexEntity entity = ComplexEntity.T.create();
		String stringPropertyParent = "new created complex entity with child";
		String stringPropertyChild = "new another entity child";

		entity.setStringProperty(stringPropertyParent);
		entity.setIntegerProperty(20);
		entity.setBooleanProperty(true);

		AnotherComplexEntity anotherComplexEntityProperty = AnotherComplexEntity.T.create();
		anotherComplexEntityProperty.setIntegerProperty(20);
		anotherComplexEntityProperty.setStringProperty(stringPropertyChild);

		entity.setAnotherComplexEntityProperty(anotherComplexEntityProperty);

		ByteArrayOutputStream out = new ByteArrayOutputStream(8192);
		JsonStreamMarshaller marshaller = new JsonStreamMarshaller();

		marshaller.marshall(out, entity, GmSerializationOptions.deriveDefaults().outputPrettiness(OutputPrettiness.high).build());
		Object result = requests.post(getPathEntities(ComplexEntity.T)).urlParameter("sessionId", "anything").accept(JSON).contentType(JSON)
				.body(out.toString()).execute(200);

		ComplexEntity parent = getEntity(ComplexEntity.T, "stringProperty", stringPropertyParent);
		AnotherComplexEntity child = getEntity(AnotherComplexEntity.T, "stringProperty", stringPropertyChild);

		Assert.assertNotNull(parent.getId());
		Assert.assertNotNull(child.getId());
		Assert.assertEquals(parent.getId(), result);
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
		requests.post(getPathEntities(ComplexEntity.T, 1)).urlParameter("sessionId", "anything").accept(JSON).contentType(JSON).body(out.toString())
				.execute(200);

		ComplexEntity updatedEntity = getEntity(ComplexEntity.T, "id", Long.valueOf(1));
		Assert.assertEquals("new value", updatedEntity.getStringProperty());
		Assert.assertEquals("new value on another complex property", updatedEntity.getAnotherComplexEntityProperty().getStringProperty());
		Assert.assertEquals("new value another another complex property",
				updatedEntity.getAnotherComplexEntityProperty().getAnotherComplexEntityProperty().getStringProperty());
	}

	@Test
	public void nestedEntityParentIdNotExistsUrlId() {
		ComplexEntity entity = getEntity(ComplexEntity.T, "id", Long.valueOf(1));
		Assert.assertNull(entity.getBooleanProperty());
		Assert.assertEquals("1", entity.getStringProperty());

		ByteArrayOutputStream out = new ByteArrayOutputStream(8192);
		JsonStreamMarshaller marshaller = new JsonStreamMarshaller();

		entity.setId(null);
		entity.setBooleanProperty(true);
		entity.setStringProperty("new value");
		entity.getAnotherComplexEntityProperty().setStringProperty("new value on another complex property");
		entity.getAnotherComplexEntityProperty().getAnotherComplexEntityProperty().setStringProperty("new value another another complex property");

		marshaller.marshall(out, entity, GmSerializationOptions.deriveDefaults().outputPrettiness(OutputPrettiness.high).build());
		Failure failure = requests.post(getPathEntities(ComplexEntity.T, 10)).urlParameter("sessionId", "anything").accept(JSON).contentType(JSON)
				.body(out.toString()).execute(404);

		Assert.assertEquals("No entity of type com.braintribe.testing.model.test.technical.features.ComplexEntity[10, test.access] found!",
				failure.getMessage());
	}

	@Test
	public void nestedEntityParentIdNotExistsBodyId() {
		ComplexEntity entity = getEntity(ComplexEntity.T, "id", Long.valueOf(1));
		Assert.assertNull(entity.getBooleanProperty());
		Assert.assertEquals("1", entity.getStringProperty());

		ByteArrayOutputStream out = new ByteArrayOutputStream(8192);
		JsonStreamMarshaller marshaller = new JsonStreamMarshaller();

		entity.setId(10);
		entity.setBooleanProperty(true);
		entity.setStringProperty("new value");
		entity.getAnotherComplexEntityProperty().setStringProperty("new value on another complex property");
		entity.getAnotherComplexEntityProperty().getAnotherComplexEntityProperty().setStringProperty("new value another another complex property");

		marshaller.marshall(out, entity, GmSerializationOptions.deriveDefaults().outputPrettiness(OutputPrettiness.high).build());
		Failure failure = requests.post(getPathEntities(ComplexEntity.T)).urlParameter("sessionId", "anything").accept(JSON).contentType(JSON)
				.body(out.toString()).execute(400);

		Assert.assertEquals("No entity of type com.braintribe.testing.model.test.technical.features.ComplexEntity[10, test.access] found!",
				failure.getMessage());
	}

	@Test
	public void nestedEntityParentUpdatedAndChildCreated() {
		ComplexEntity entity = getEntity(ComplexEntity.T, "id", Long.valueOf(1));
		Assert.assertNull(entity.getBooleanProperty());
		Assert.assertEquals("1", entity.getStringProperty());

		ByteArrayOutputStream out = new ByteArrayOutputStream(8192);
		JsonStreamMarshaller marshaller = new JsonStreamMarshaller();

		entity.setBooleanProperty(true);
		entity.setStringProperty("new value");

		AnotherComplexEntity complexEntity = AnotherComplexEntity.T.create();
		complexEntity.setStringProperty("new another complex entity");

		entity.setAnotherComplexEntityProperty(complexEntity);

		marshaller.marshall(out, entity, GmSerializationOptions.deriveDefaults().outputPrettiness(OutputPrettiness.high).build());
		requests.post(getPathEntities(ComplexEntity.T, 1)).urlParameter("sessionId", "anything").accept(JSON).contentType(JSON).body(out.toString())
				.execute(200);

		ComplexEntity updatedEntity = getEntity(ComplexEntity.T, "id", Long.valueOf(1));

		Assert.assertEquals("new value", updatedEntity.getStringProperty());
		Assert.assertEquals("new another complex entity", updatedEntity.getAnotherComplexEntityProperty().getStringProperty());
	}

	@Test
	public void nestedEntityParentUpdateAndChildWithIdDoesNotExists() {
		ComplexEntity entity = getEntity(ComplexEntity.T, "id", Long.valueOf(1));
		Assert.assertNull(entity.getBooleanProperty());
		Assert.assertEquals("1", entity.getStringProperty());

		ByteArrayOutputStream out = new ByteArrayOutputStream(8192);
		JsonStreamMarshaller marshaller = new JsonStreamMarshaller();

		entity.setBooleanProperty(true);
		entity.setStringProperty("new value");

		AnotherComplexEntity complexEntity = AnotherComplexEntity.T.create();
		complexEntity.setIntegerProperty(20);
		complexEntity.setStringProperty("20");
		complexEntity.setId(20L);

		entity.setAnotherComplexEntityProperty(complexEntity);

		marshaller.marshall(out, entity, GmSerializationOptions.deriveDefaults().outputPrettiness(OutputPrettiness.high).build());
		Failure failure = requests.post(getPathEntities(ComplexEntity.T, 1)).urlParameter("sessionId", "anything").accept(JSON).contentType(JSON)
				.body(out.toString()).execute(404);

		Assert.assertEquals("No entity of type com.braintribe.testing.model.test.technical.features.AnotherComplexEntity[20, *] found!",
				failure.getMessage());
	}

	@Test
	public void nestedEntityParentCreatedAndChildExistsAndUpdated() {
		ComplexEntity entity = ComplexEntity.T.create();

		ByteArrayOutputStream out = new ByteArrayOutputStream(8192);
		JsonStreamMarshaller marshaller = new JsonStreamMarshaller();

		entity.setBooleanProperty(true);
		String stringProperty = "new parent child exists";
		entity.setStringProperty(stringProperty);

		AnotherComplexEntity complexEntity = getEntity(AnotherComplexEntity.T, "id", 1L);
		String simple_update = "simple update";
		complexEntity.setStringProperty(simple_update);
		entity.setAnotherComplexEntityProperty(complexEntity);

		marshaller.marshall(out, entity, GmSerializationOptions.deriveDefaults().outputPrettiness(OutputPrettiness.high).build());
		requests.post(getPathEntities(ComplexEntity.T)).urlParameter("sessionId", "anything").accept(JSON).contentType(JSON).body(out.toString())
				.execute(200);

		ComplexEntity createdEntity = getEntity(ComplexEntity.T, "stringProperty", stringProperty);
		AnotherComplexEntity anotherComplexEntity = getEntity(AnotherComplexEntity.T, "id", complexEntity.getId());

		Assert.assertNotNull(createdEntity.getId());
		Assert.assertNotNull(createdEntity.getAnotherComplexEntityProperty());
		Assert.assertEquals(stringProperty, createdEntity.getStringProperty());
		Assert.assertEquals(simple_update, createdEntity.getAnotherComplexEntityProperty().getStringProperty());
		Assert.assertEquals(simple_update, anotherComplexEntity.getStringProperty());
	}

	@Test
	public void complexEntityUpdateSimpleParams() {
		ComplexEntity entity1 = getEntity(ComplexEntity.T, "id", Long.valueOf(1));
		Assert.assertNull(entity1.getBooleanProperty());
		Assert.assertEquals("1", entity1.getStringProperty());

		requests.post(getPathEntities(ComplexEntity.T, 1)).urlParameter("sessionId", "anything").accept(JSON).contentType(JSON)
				.body("{ \"booleanProperty\": false, \"stringProperty\": \"updated value\", \"partition\": \"test.access\" }").execute(200);

		ComplexEntity entity = getEntity(ComplexEntity.T, "id", Long.valueOf(1));
		Assert.assertFalse(entity.getBooleanProperty());
		Assert.assertEquals("updated value", entity.getStringProperty());
	}

	@Test
	public void withProjectionEnvelope() {
		Object result = postWithProjection("envelope");
		Assert.assertTrue(result instanceof ManipulationResponse);
	}

	@Test
	public void withProjectionIdInfo() {
		Object result = postWithProjection("idInfo");
		Assert.assertTrue(result instanceof Long);
	}

	@Test
	public void withProjectionReferenceInfo() {
		Object result = postWithProjection("referenceInfo");
		Assert.assertTrue(result instanceof PersistentEntityReference);
	}

	@Test
	public void withProjectionSuccess() {
		boolean success = postWithProjection("success");
		Assert.assertTrue(success);
	}

	@Test
	public void withProjectionLocationInfo() {
		String result = postWithProjection("locationInfo");

		String entityTypeUrl = server.getContextUrl().toString() + "/rest/v2/entities/test.access/" + SimpleEntity.class.getName() + "/";
		Assert.assertTrue(result.startsWith(entityTypeUrl));
		String idString = result.replace(entityTypeUrl, "");

		// make sure that we can parse the ID.
		Long.parseLong(idString);
	}

	@Test
	public void withProjectionData() {
		SimpleEntity result = postWithProjection("data");
		Assert.assertEquals("test.access", result.getPartition());
		Assert.assertEquals("value", result.getStringProperty());
		Assert.assertEquals(true, result.getBooleanProperty());
	}

	private <T> T postWithProjection(String projection) {
		//@formatter:off
		return requests.post(getPathEntities(SimpleEntity.T))
				.urlParameter("sessionId", "anything")
				.accept(JSON)
				.contentType(JSON)
				.urlParameter("projection", projection)
				.body("{ \"booleanProperty\": true, \"stringProperty\": \"value\" }")
				.execute(200);
		//@formatter:on
	}
}
