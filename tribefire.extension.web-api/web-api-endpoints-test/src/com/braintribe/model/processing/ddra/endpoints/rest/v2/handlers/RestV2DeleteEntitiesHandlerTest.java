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
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.braintribe.model.access.smood.basic.SmoodAccess;
import com.braintribe.model.accessapi.ManipulationResponse;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.DeleteMode;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.ddra.endpoints.TestHttpRequest;
import com.braintribe.model.processing.ddra.endpoints.rest.v2.AbstractRestV2Test;
import com.braintribe.model.processing.ddra.endpoints.rest.v2.ioc.TestModelAccessoryFactory;
import com.braintribe.model.service.api.result.Failure;
import com.braintribe.testing.model.test.technical.features.AnotherComplexEntity;
import com.braintribe.testing.model.test.technical.features.SimpleEntity;

public class RestV2DeleteEntitiesHandlerTest extends AbstractRestV2Test {

	@BeforeClass
	public static void beforeClass() {
		setupServlet();

		RestV2DeleteEntitiesHandler handler = new RestV2DeleteEntitiesHandler();
		wireHandler(handler);
		handler.setModelAccessoryFactory(TestModelAccessoryFactory.testModelAccessoryFactory(access));
		addHandler("DELETE:entities", handler);
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
		requests.delete(getPathEntities(SimpleEntity.T, 1))
				.accept(JSON)
				.execute(401);
		//@formatter:on
	}

	@Test
	public void byIdAndPartitionExisting() {
		List<GenericEntity> entities = getEntities(SimpleEntity.T, "id", 2L);
		Assert.assertEquals(1, entities.size());

		//@formatter:off
		requests.delete(getPathEntities(SimpleEntity.T, 2, "p0"))
				.urlParameter("sessionId", "anything")
				.accept(JSON)
				.execute(200);
		//@formatter:on

		entities = getEntities(SimpleEntity.T, "id", Long.valueOf(2));
		Assert.assertTrue(entities.isEmpty());
	}

	@Test
	public void byIdWithoutPartitionExisting() {
		List<GenericEntity> entities = getEntities(SimpleEntity.T, "id", 2L);
		Assert.assertEquals(1, entities.size());

		//@formatter:off
		requests.delete(getPathEntities(SimpleEntity.T, 3))
				.urlParameter("sessionId", "anything")
				.accept(JSON)
				.execute(200);
		//@formatter:on

		entities = getEntities(SimpleEntity.T, "id", Long.valueOf(3));
		Assert.assertTrue(entities.isEmpty());
	}

	@Test
	public void withProjectionCount() {
		//@formatter:off
		int count = requests.delete(getPathEntities(SimpleEntity.T, 2, "p0"))
				.urlParameter("sessionId", "anything")
				.urlParameter("projection", "count")
				.accept(JSON)
				.execute(200);
		//@formatter:on

		Assert.assertEquals(1, count);
	}

	@Test
	public void BUG_withProjectionEnvelope() {
		Object response = deleteWithProjection("envelope", true);
		Assert.assertTrue(response instanceof ManipulationResponse);
		resetServletAccess(true);
		response = deleteWithProjection("envelope", false);
		Assert.assertTrue(response instanceof ManipulationResponse);
	}
	
	private <T> T deleteWithProjection(String projection, boolean idInUrl) {
		String path = idInUrl ? getPathEntities(SimpleEntity.T, 2) : getPathEntities(SimpleEntity.T);
		
		TestHttpRequest request = requests.delete(path)
				.urlParameter("sessionId", "anything")
				.urlParameter("projection", projection)
				.accept(JSON);
		
		if(!idInUrl) {
			request.urlParameter("where.id", "2L");
			request.urlParameter("allowMultipleDelete", "true");
		    return request.execute(200);
		} else {
		    return request.execute(200);
		}

	}
	
	@Test
	public void byTypeSignature() {
		List<GenericEntity> entities = getEntities(SimpleEntity.T);
		int entitiesCount = entities.size();
		Assert.assertFalse(entities.isEmpty());

		//@formatter:off
		int count = requests.delete(getPathEntities(SimpleEntity.T))
				.urlParameter("sessionId", "anything")
				.urlParameter("allowMultipleDelete", "true")
				.accept(JSON)
				.execute(200);
		//@formatter:on

		entities = getEntities(SimpleEntity.T);
		Assert.assertTrue(entities.isEmpty());
		Assert.assertEquals(entitiesCount, count);
	}

	@Test
	public void byTypeSignatureNotAllowMultipleDeleteFalse() {
		List<GenericEntity> entities = getEntities(SimpleEntity.T);
		Assert.assertFalse(entities.isEmpty());

		//@formatter:off
		Failure failure = requests.delete(getPathEntities(SimpleEntity.T))
				.urlParameter("sessionId", "anything")
				.urlParameter("allowMultipleDelete", "false")
				.accept(JSON)
				.execute(412);
		//@formatter:on

		entities = getEntities(SimpleEntity.T);
		Assert.assertFalse(entities.isEmpty());
		Assert.assertEquals(
				"The flag 'allowMultipleDelete' must be 'true' to perform operation. This is due prevention of unintentional deletion group of entities.",
				failure.getMessage());
	}

	@Test
	public void byIdNotExisting() {
		//@formatter:off
		requests.delete(getPathEntities(SimpleEntity.T, 99, "p0"))
				.urlParameter("sessionId", "anything")
				.accept(JSON)
				.execute(404);
		//@formatter:on
	}

	@Test
	public void whereConditionExisting() {
		List<GenericEntity> entities = getEntities(SimpleEntity.T, "stringProperty", "se1");
		Assert.assertEquals(1, entities.size());

		//@formatter:off
		int count = requests.delete(getPathEntities(SimpleEntity.T))
				.urlParameter("sessionId", "anything")
				.urlParameter("where.stringProperty", "se1")
				.urlParameter("allowMultipleDelete", "true")
				.accept(JSON)
				.execute(200);
		//@formatter:on

		entities = getEntities(SimpleEntity.T, "stringProperty", "se1");
		Assert.assertTrue(entities.isEmpty());
		Assert.assertEquals(1, count);
	}

	@Test
	public void whereConditionExistingAllowMultipleDeleteFalse() {
		List<GenericEntity> entities = getEntities(SimpleEntity.T, "stringProperty", "se1");
		Assert.assertEquals(1, entities.size());

		//@formatter:off
		Failure failure = requests.delete(getPathEntities(SimpleEntity.T))
				.urlParameter("sessionId", "anything")
				.urlParameter("where.stringProperty", "se1")
				.urlParameter("allowMultipleDelete", "fasle")
				.accept(JSON)
				.execute(412);
		//@formatter:on

		entities = getEntities(SimpleEntity.T, "stringProperty", "se1");
		Assert.assertFalse(entities.isEmpty());
		Assert.assertEquals(
				"The flag 'allowMultipleDelete' must be 'true' to perform operation. This is due prevention of unintentional deletion group of entities.",
				failure.getMessage());
	}

	@Test
	public void withWhereParameterObject() {
		//@formatter:off
		int result = requests.delete(getPathEntities(SimpleEntity.T))
				.urlParameter("where.id", "0L")
				.urlParameter("endpoint.allowMultipleDelete", "true")
				.urlParameter("sessionId", "anything")
				.accept(JSON)
				.execute(200);
		//@formatter:on

		Assert.assertEquals(result, 1);
	}

	@Test
	public void bySimpleName() {
		List<GenericEntity> entities = getEntities(SimpleEntity.T);
		Assert.assertFalse(entities.isEmpty());

		//@formatter:off
		requests.delete(getPathEntities("SimpleEntity"))
				.urlParameter("sessionId", "anything")
				.urlParameter("allowMultipleDelete", "true")
				.accept(JSON)
				.execute(200);
		//@formatter:on

		entities = getEntities(SimpleEntity.T);
		Assert.assertTrue(entities.isEmpty());
	}

	@Test
	public void bySimpleNameAllowMultiDeleteDefaultValue() {
		List<GenericEntity> entities = getEntities(SimpleEntity.T);
		Assert.assertFalse(entities.isEmpty());

		//@formatter:off
		Failure failure = requests.delete(getPathEntities("SimpleEntity"))
				.urlParameter("sessionId", "anything")
				.accept(JSON)
				.execute(412);
		//@formatter:on

		entities = getEntities(SimpleEntity.T);
		Assert.assertFalse(entities.isEmpty());
		Assert.assertEquals(
				"The flag 'allowMultipleDelete' must be 'true' to perform operation. This is due prevention of unintentional deletion group of entities.",
				failure.getMessage());
	}

	@Test
	public void bySimpleNameIncorrectName() {
		//@formatter:off
		requests.delete(getPathEntities("NotExisting"))
				.urlParameter("sessionId", "anything")
				.accept(JSON)
				.execute(404);
		//@formatter:on
	}

	@Test
	public void bySimpleNameAndIdExisting() {
		List<GenericEntity> entities = getEntities(SimpleEntity.T, "id", 2L);
		Assert.assertEquals(1, entities.size());

		//@formatter:off
		requests.delete(getPathEntities("SimpleEntity", 2, "p0"))
				.urlParameter("sessionId", "anything")
				.accept(JSON)
				.execute(200);
		//@formatter:on

		entities = getEntities(SimpleEntity.T, "id", Long.valueOf(2));
		Assert.assertTrue(entities.isEmpty());
	}

	@Test
	public void bySimpleNameAmbiguous() {
		//@formatter:off
		requests.delete(getPathEntities("DuplicateSimpleNameEntity", 2, "p0"))
				.urlParameter("sessionId", "anything")
				.accept(JSON)
				.execute(400);
		//@formatter:on
	}

	/**
	 * Note that {@link DeleteMode#ignoreReferences} behaves exactly as {@link DeleteMode#failIfReferenced} in a {@link SmoodAccess}, because this
	 * access has to protect it's inner integrity. So saying ignoreReferences still leads to an error if a reference exists.
	 */
	@Test
	public void withDeleteModeIgnoreReferences() {
		// referenced by a mandatory property: fail
		deleteWith(DeleteMode.ignoreReferences, 500, AnotherComplexEntity.T, 2);
		// referenced by a non-mandatory property: succeed
		deleteWith(DeleteMode.ignoreReferences, 200, SimpleEntity.T, 0);
	}

	@Test
	public void withDeleteModeDropReferences() {
		deleteWith(DeleteMode.dropReferences, 200, AnotherComplexEntity.T, 2);

		AnotherComplexEntity entity = getEntity(AnotherComplexEntity.T, "id", 1L);
		Assert.assertNull(entity.getAnotherComplexEntityProperty());
	}

	@Test
	public void withDeleteModeDropReferencesIfPossible() {
		// referenced by a mandatory property: fail
		deleteWith(DeleteMode.dropReferencesIfPossible, 500, AnotherComplexEntity.T, 2);
		// referenced by a non-mandatory property: succeed
		deleteWith(DeleteMode.dropReferencesIfPossible, 200, AnotherComplexEntity.T, 0);
	}

	@Test
	public void withDeleteModeFailIfReferenced() {
		// referenced by a mandatory property: fail
		deleteWith(DeleteMode.failIfReferenced, 500, AnotherComplexEntity.T, 2);
		// referenced by a non-mandatory property: fail
		deleteWith(DeleteMode.failIfReferenced, 500, AnotherComplexEntity.T, 2);
		// not referenced: success
		deleteWith(DeleteMode.failIfReferenced, 200, SimpleEntity.T, 0);
	}

	private void deleteWith(DeleteMode deleteMode, int expectedCode, EntityType<?> entityType, long id) {
		Assert.assertEquals(1, getEntities(entityType, "id", id).size());

		//@formatter:off
		requests.delete(getPathEntities(entityType, id))
				.urlParameter("sessionId", "anything")
				.urlParameter("deleteMode", deleteMode.name())
				.accept(JSON)
				.execute(expectedCode);
		//@formatter:on

		Assert.assertEquals(expectedCode == 200 ? 0 : 1, getEntities(entityType, "id", id).size());
	}
}
