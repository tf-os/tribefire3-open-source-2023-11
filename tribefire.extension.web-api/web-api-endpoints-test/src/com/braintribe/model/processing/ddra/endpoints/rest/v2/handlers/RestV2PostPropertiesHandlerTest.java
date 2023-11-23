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
import com.braintribe.testing.model.test.technical.features.CollectionEntity;
import com.braintribe.testing.model.test.technical.features.ComplexEntity;
import com.braintribe.utils.MapTools;
import com.braintribe.utils.lcd.CollectionTools;
import org.junit.*;

import java.io.IOException;

public class RestV2PostPropertiesHandlerTest extends AbstractRestV2Test {

	@BeforeClass
	public static void beforeClass() {
		setupServlet();

		RestV2PostPropertiesHandler handler = new RestV2PostPropertiesHandler();
		wireHandler(handler);
		addHandler("POST:properties", handler);
	}

	@AfterClass
	public static void afterClass() throws IOException {
		destroy();
	}

	@Before
	public void before() {
		resetServletAccess(true);
	}

	@Test
	public void noSessionId() {
		//@formatter:off
		requests.post(getPathProperties(CollectionEntity.T, 0, "stringList"))
				.accept(JSON)
				.contentType(JSON)
				.body("[]")
				.execute(401);
		//@formatter:on
	}

	@Test
	public void entityNotFound() {
		//@formatter:off
		requests.post(getPathProperties(CollectionEntity.T, 12345, "stringList"))
				.urlParameter("sessionId", "a session ID")
				.accept(JSON)
				.contentType(JSON)
				.body("\"test\"")
//				.body("[]") // fail!
				.execute(404);
		//@formatter:on
	}

	@Test
	public void propertyNotFound() {
		//@formatter:off
		requests.post(getPathProperties(CollectionEntity.T, 0, "thisPropertyDoesNotExist"))
				.urlParameter("sessionId", "a session ID")
				.accept(JSON)
				.contentType(JSON)
				.body("\"test\"")
				.execute(404);
		//@formatter:on
	}

	@Test
	public void invalidPropertyType() {
		//@formatter:off
		requests.post(getPathProperties(ComplexEntity.T, 0, "stringProperty"))
				.accept(JSON)
				.contentType(JSON)
				.body("\"value\"")
				.execute(400);
		//@formatter:on
	}

	@Test
	public void listPropertyWithEntitySimpleName() {
		setProperty(CollectionEntity.T, 0, "stringList", CollectionTools.getList("1", "2"));
		//@formatter:off
		boolean success = requests.post(getPathProperties("CollectionEntity", 0, "stringList"))
				.urlParameter("sessionId", "anything")
				.accept(JSON)
				.contentType(JSON)
				.body("\"3\"")
				.execute(200);
		//@formatter:on

		CollectionEntity entity = getEntity(CollectionEntity.T, "id", 0L);
		Assert.assertEquals(CollectionTools.getList("1", "2", "3"), entity.getStringList());
		Assert.assertTrue(success);
	}

	@Test
	public void listPropertyWithEntitySimpleNameAmbiguous() {
		//@formatter:off
		requests.post(getPathProperties("DuplicateSimpleNameEntity", 0, "stringProperty"))
				.urlParameter("sessionId", "anything")
				.accept(JSON)
				.contentType(JSON)
				.body("\"3\"")
				.execute(400);
		//@formatter:on
	}

	@Test
	public void listPropertySingleValue() {
		setProperty(CollectionEntity.T, 0, "stringList", CollectionTools.getList("1", "2"));
		//@formatter:off
		boolean success = requests.post(getPathProperties(CollectionEntity.T, 0, "stringList"))
				.urlParameter("sessionId", "anything")
				.accept(JSON)
				.contentType(JSON)
				.body("\"3\"")
				.execute(200);
		//@formatter:on

		CollectionEntity entity = getEntity(CollectionEntity.T, "id", 0L);
		Assert.assertEquals(CollectionTools.getList("1", "2", "3"), entity.getStringList());
		Assert.assertTrue(success);
	}

	@Test
	public void listPropertySingleValue2() {
		resetServletAccess(false);

		setProperty(CollectionEntity.T, 0, "stringList", CollectionTools.getList("1", "2"));
		//@formatter:off
		Failure failure = requests.post(getPathProperties(CollectionEntity.T, 0, "wrong.partition", "stringList"))
				.urlParameter("sessionId", "anything")
				.accept(JSON)
				.contentType(JSON)
				.body("\"3\"")
				.execute(404);
		//@formatter:on

		Assert.assertNotNull(failure);
		Assert.assertEquals("No entity of type com.braintribe.testing.model.test.technical.features.CollectionEntity[0, wrong.partition] found!",
				failure.getMessage());
	}

	@Test
	public void listPropertyValueList() {
		setProperty(CollectionEntity.T, 0, "stringList", CollectionTools.getList("1", "2"));
		//@formatter:off
		boolean success = requests.post(getPathProperties(CollectionEntity.T, 0, "stringList"))
				.urlParameter("sessionId", "anything")
				.accept(JSON)
				.contentType(JSON)
				.body("[\"3\", \"4\"]")
				.execute(200);
		//@formatter:on

		CollectionEntity entity = getEntity(CollectionEntity.T, "id", 0L);
		Assert.assertEquals(CollectionTools.getList("1", "2", "3", "4"), entity.getStringList());
		Assert.assertTrue(success);
	}

	@Test
	public void listPropertyValueMap() {
		setProperty(CollectionEntity.T, 0, "stringList", CollectionTools.getList("1", "2", "3"));
		//@formatter:off
		boolean success = requests.post(getPathProperties(CollectionEntity.T, 0, "stringList"))
				.urlParameter("sessionId", "anything")
				.accept(JSON)
				.contentType(JSON)
				.body("{ \"_type\": \"map\", \"value\": [{ \"key\": 1, \"value\": \"4\" }, { \"key\": 3, \"value\": \"5\"} , { \"key\": 17, \"value\": \"6\"} ] }")
				.execute(200);
		//@formatter:on

		CollectionEntity entity = getEntity(CollectionEntity.T, "id", 0L);
		Assert.assertEquals(CollectionTools.getList("1", "4", "2", "5", "3", "6"), entity.getStringList());
		Assert.assertTrue(success);
	}

	@Test
	public void setPropertySingleValue() {
		setProperty(CollectionEntity.T, 0, "integerSet", CollectionTools.getSet(1, 2, 3));

		//@formatter:off
		boolean success = requests.post(getPathProperties(CollectionEntity.T, 0, "integerSet"))
				.urlParameter("sessionId", "anything")
				.accept(JSON)
				.contentType(JSON)
				.body("4")
				.execute(200);
		//@formatter:on

		CollectionEntity entity = getEntity(CollectionEntity.T, "id", 0L);
		Assert.assertEquals(CollectionTools.getSet(1, 2, 3, 4), entity.getIntegerSet());
		Assert.assertTrue(success);
	}

	@Test
	public void setPropertyValueList() {
		setProperty(CollectionEntity.T, 0, "integerSet", CollectionTools.getSet(1, 2, 3));

		//@formatter:off
		boolean success = requests.post(getPathProperties(CollectionEntity.T, 0, "integerSet"))
				.urlParameter("sessionId", "anything")
				.accept(JSON)
				.contentType(JSON)
				.body("[3, 4, 5]")
				.execute(200);
		//@formatter:on

		CollectionEntity entity = getEntity(CollectionEntity.T, "id", 0L);
		Assert.assertEquals(CollectionTools.getSet(1, 2, 3, 4, 5), entity.getIntegerSet());
		Assert.assertTrue(success);
	}

	@Test
	public void mapPropertyValueMap() {
		setProperty(CollectionEntity.T, 0, "integerToIntegerMap", MapTools.getMap(0, 0, 1, 1, 2, 2));

		//@formatter:off
		boolean success = requests.post(getPathProperties(CollectionEntity.T, 0, "integerToIntegerMap"))
				.urlParameter("sessionId", "anything")
				.accept(JSON)
				.contentType(JSON)
				.body("{ \"_type\": \"map\", \"value\": [{ \"key\": 1, \"value\": 2 }, { \"key\": 3, \"value\": 4} , { \"key\": 5, \"value\": 6} ] }")
				.execute(200);
		//@formatter:on

		CollectionEntity entity = getEntity(CollectionEntity.T, "id", 0L);
		Assert.assertEquals(MapTools.getMap(0, 0, 1, 2, 2, 2, 3, 4, 5, 6), entity.getIntegerToIntegerMap());
		Assert.assertTrue(success);
	}

	@Test
	public void removeListPropertyValueMap() {
		setProperty(CollectionEntity.T, 0, "stringList", CollectionTools.getList("1", "2", "3", "4", "5"));

		//@formatter:off
		boolean success = requests.post(getPathProperties(CollectionEntity.T, 0, "stringList"))
				.urlParameter("sessionId", "anything")
				.urlParameter("remove", "true")
				.accept(JSON)
				.contentType(JSON)
				.body("{ \"_type\": \"map\", \"value\": [{ \"key\": 2, \"value\": \"2\" }, { \"key\": 4, \"value\": \"4\"} ] }")
				.execute(200);
		//@formatter:on

		CollectionEntity entity = getEntity(CollectionEntity.T, "id", 0L);
		Assert.assertEquals(CollectionTools.getList("1", "3", "5"), entity.getStringList());
		Assert.assertTrue(success);
	}

	@Test
	public void removeSetPropertySingleValue() {
		setProperty(CollectionEntity.T, 0, "integerSet", CollectionTools.getSet(1, 2, 3, 4, 5));

		//@formatter:off
		boolean success = requests.post(getPathProperties(CollectionEntity.T, 0, "integerSet"))
				.urlParameter("sessionId", "anything")
				.urlParameter("remove", "true")
				.accept(JSON)
				.contentType(JSON)
				.body("4")
				.execute(200);
		//@formatter:on

		CollectionEntity entity = getEntity(CollectionEntity.T, "id", 0L);
		Assert.assertEquals(CollectionTools.getSet(1, 2, 3, 5), entity.getIntegerSet());
		Assert.assertTrue(success);
	}

	@Test
	public void removeSetPropertyValueList() {
		setProperty(CollectionEntity.T, 0, "integerSet", CollectionTools.getSet(1, 2, 3, 4, 5));

		//@formatter:off
		boolean success = requests.post(getPathProperties(CollectionEntity.T, 0, "integerSet"))
				.urlParameter("sessionId", "anything")
				.urlParameter("remove", "true")
				.accept(JSON)
				.contentType(JSON)
				.body("[2, 4]")
				.execute(200);
		//@formatter:on

		CollectionEntity entity = getEntity(CollectionEntity.T, "id", 0L);
		Assert.assertEquals(CollectionTools.getSet(1, 3, 5), entity.getIntegerSet());
		Assert.assertTrue(success);
	}

	@Test
	public void removeMapPropertyValueMap() {
		setProperty(CollectionEntity.T, 0, "integerToIntegerMap", MapTools.getMap(0, 10, 1, 11, 2, 12, 3, 13, 4, 14, 5, 15));

		//@formatter:off
		boolean success = requests.post(getPathProperties(CollectionEntity.T, 0, "integerToIntegerMap"))
				.urlParameter("sessionId", "anything")
				.urlParameter("remove", "true")
				.accept(JSON)
				.contentType(JSON)
				.body("{ \"_type\": \"map\", \"value\": [{ \"key\": 2, \"value\": 12 }, { \"key\": 4, \"value\": 14} ] }")
				.execute(200);
		//@formatter:on

		CollectionEntity entity = getEntity(CollectionEntity.T, "id", 0L);
		Assert.assertEquals(MapTools.getMap(0, 10, 1, 11, 3, 13, 5, 15), entity.getIntegerToIntegerMap());
		Assert.assertTrue(success);
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
		setProperty(CollectionEntity.T, 0, "stringList", CollectionTools.getList("1", "2"));
		//@formatter:off
		return requests.post(getPathProperties("CollectionEntity", 0, "stringList"))
				.urlParameter("sessionId", "anything")
				.urlParameter("projection", projection)
				.accept(JSON)
				.contentType(JSON)
				.body("\"3\"")
				.execute(200);
		//@formatter:on
	}
}
