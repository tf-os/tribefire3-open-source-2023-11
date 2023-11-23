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

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.ddra.endpoints.rest.v2.AbstractRestV2Test;
import com.braintribe.model.processing.ddra.endpoints.rest.v2.ioc.TestModelAccessoryFactory;
import com.braintribe.model.query.EntityQueryResult;
import com.braintribe.model.service.api.result.Failure;
import com.braintribe.testing.model.test.technical.features.AnotherComplexEntity;
import com.braintribe.testing.model.test.technical.features.CollectionEntity;
import com.braintribe.testing.model.test.technical.features.ComplexEntity;
import com.braintribe.testing.model.test.technical.features.SimpleEntity;

public class RestV2GetEntitiesHandlerTest extends AbstractRestV2Test {

	@BeforeClass
	public static void beforeClass() {
		setupServlet();

		RestV2GetEntitiesHandler handler = new RestV2GetEntitiesHandler();
		wireHandler(handler);
		handler.setModelAccessoryFactory(TestModelAccessoryFactory.testModelAccessoryFactory(access));
		addHandler("GET:entities", handler);
	}

	@Before
	public void before() {
		resetServletAccess(false);
	}

	@AfterClass
	public static void afterClass() throws IOException {
		destroy();
	}

	@Test
	public void noSessionId() {
		//@formatter:off
		requests.get(getPathEntities(SimpleEntity.T))
				.accept(JSON)
				.execute(401);
		//@formatter:on
	}

	@Test
	public void orderByAndLimit() {
		//@formatter:off
		List<GenericEntity> result = requests.get(getPathEntities(SimpleEntity.T))
				.urlParameter("sessionId", "anything")
				.urlParameter("startIndex", "1")
				.urlParameter("maxResults", "3")
				.header("gm-orderBy", "stringProperty")
				.header("gm-orderDirection", "descending")
				.accept(JSON)
				.execute(200);
		//@formatter:on

		Assert.assertEquals(3, result.size());
		assertSimpleEntity(result.get(0), "se3", 3L);
		assertSimpleEntity(result.get(1), "se2", 2L);
		assertSimpleEntity(result.get(2), "se1", 1L);
	}

	@Test
	public void noAccessExists() {
		//@formatter:off
		Failure failure = requests.get("/tribefire-services/rest/v2/entities/test.access.noexists/com.braintribe.testing.model.test.technical.features.SimpleEntity")
				.urlParameter("sessionId", "anything")
				.accept(JSON)
				.execute(500);
		//@formatter:on

		Assert.assertEquals("No access found with accessId=test.access.noexists", failure.getMessage());
	}

	@Test
	public void withWhere() {
		//@formatter:off
		List<GenericEntity> result = requests.get(getPathEntities(SimpleEntity.T))
				.urlParameter("sessionId", "anything")
				.urlParameter("maxResults", "2")
				.urlParameter("where.stringProperty", "se3")
				.accept(JSON)
				.execute(200);
		//@formatter:on

		Assert.assertEquals(1, result.size());
		assertSimpleEntity(result.get(0), "se3", 3L);
	}

	@Test
	public void withWhereParameterObject() {
		//@formatter:off
		List<GenericEntity> result = requests.get(getPathEntities(SimpleEntity.T))
				.urlParameter("sessionId", "anything")
				.urlParameter("maxResults", "2")
				.urlParameter("where.id", "-1L")
				.urlParameter("where.partition", "p0")
				.accept(JSON)
				.execute(200);
		//@formatter:on
		
		Assert.assertTrue(result.size() == 1);
	}

	@Test
	public void withWhereNoResult() {
		//@formatter:off
		List<GenericEntity> result = requests.get(getPathEntities(SimpleEntity.T))
				.urlParameter("sessionId", "anything")
				.urlParameter("maxResults", "2")
				.urlParameter("where.stringProperty", "doesNotExist")
				.accept(JSON)
				.execute(200);
		//@formatter:on

		Assert.assertTrue(result.isEmpty());
	}
	
	@Test
	public void withWhereCollectionType() {
		// Request should fail because properties for where-parameters can only be of scalar type 
		//@formatter:off
		requests.get(getPathEntities(CollectionEntity.T))
				.urlParameter("sessionId", "anything")
				.urlParameter("maxResults", "2")
				.urlParameter("where.stringList", "doesNotExist")
				.accept(JSON)
				.execute(400);
		//@formatter:on
	}

	@Test
	public void withCascasdedOrderBy() {
		//@formatter:off
		List<GenericEntity> result = requests.get(getPathEntities(ComplexEntity.T))
				.urlParameter("sessionId", "anything")
				.urlParameter("orderBy", "integerProperty")
				.urlParameter("orderDirection", "descending")
				.urlParameter("entityRecurrenceDepth", "0")
				.urlParameter("typeExplicitness", "always")
				.header("gm-orderBy", "stringProperty")
				.header("gm-orderDirection", "descending")
				.accept(JSON)
				.execute(200);
		//@formatter:on

		int index = 0;
		for (int i = 2; i >= 0; i--) {
			for (int j = 2; j >= 0; j--) {
				GenericEntity entity = result.get(index);
				assertComplexEntity(entity, 8 - index, i, String.valueOf(j));
				index++;
			}
		}
	}

	@Test
	public void byId() {
		//@formatter:off
		SimpleEntity result = requests.get(getPathEntities(SimpleEntity.T, 2))
				.urlParameter("sessionId", "anything")
				.accept(JSON)
				.execute(200);
		//@formatter:on

		assertSimpleEntity(result, "se2", 2L);
	}

	@Test
	public void byIdNotFound() {
		//@formatter:off
		requests.get(getPathEntities(SimpleEntity.T, 999))
				.urlParameter("sessionId", "anything")
				.accept(JSON)
				.execute(404);
		//@formatter:on
	}

	@Test
	public void byIdAndPartition() {
		//@formatter:off
		SimpleEntity result = requests.get(getPathEntities(SimpleEntity.T, 2, "p0"))
				.urlParameter("sessionId", "anything")
				.accept(JSON)
				.execute(200);
		//@formatter:on

		assertSimpleEntity(result, "se2", 2L);
	}

	@Test
	public void byIdAndPartitionNoFound() {
		//@formatter:off
		requests.get(getPathEntities(SimpleEntity.T, 2, "ThisPartitionDoesNotExists"))
				.urlParameter("sessionId", "anything")
				.accept(JSON)
				.execute(404);
		//@formatter:on
	}

	@Test
	public void byIdMultipleResults() {
		//@formatter:off
		requests.get(getPathEntities(SimpleEntity.T, -1))
				.urlParameter("sessionId", "anything")
				.accept(JSON)
				.execute(400);
		//@formatter:on
	}

	@Test
	public void bySimpleName() {
		//@formatter:off
		SimpleEntity result = requests.get(getPathEntities("SimpleEntity", 2, "p0"))
				.urlParameter("sessionId", "anything")
				.accept(JSON)
				.execute(200);
		//@formatter:on

		assertSimpleEntity(result, "se2", 2L);
	}

	@Test
	public void bySimpleNameAmbiguous() {
		//@formatter:off
		requests.get(getPathEntities("DuplicateSimpleNameEntity", 2, "p0"))
				.urlParameter("sessionId", "anything")
				.accept(JSON)
				.execute(400);
		//@formatter:on
	}

	@Test
	public void withProjectionFirstResult() {
		//@formatter:off
		Object result = requests.get(getPathEntities(SimpleEntity.T))
				.urlParameter("sessionId", "anything")
				.urlParameter("projection", "firstResult")
				.accept(JSON)
				.execute(200);
		//@formatter:on

		Assert.assertTrue(result instanceof SimpleEntity);
	}

	@Test
	public void withProjectionResults() {
		//@formatter:off
		Object result = requests.get(getPathEntities(SimpleEntity.T))
				.urlParameter("sessionId", "anything")
				.urlParameter("projection", "results")
				.accept(JSON)
				.execute(200);
		//@formatter:on

		Assert.assertTrue(result instanceof List);
	}

	@Test
	public void withProjectionEnvelope() {
		//@formatter:off
		Object result = requests.get(getPathEntities(SimpleEntity.T))
				.urlParameter("sessionId", "anything")
				.urlParameter("projection", "envelope")
				.accept(JSON)
				.execute(200);
		//@formatter:on

		Assert.assertTrue(result instanceof EntityQueryResult);
	}

	@Test
	public void byIdWithProjectionFirstResult() {
		//@formatter:off
		Object result = requests.get(getPathEntities(SimpleEntity.T, 2))
				.urlParameter("sessionId", "anything")
				.urlParameter("projection", "firstResult")
				.accept(JSON)
				.execute(200);
		//@formatter:on

		Assert.assertTrue(result instanceof SimpleEntity);
	}

	@Test
	public void byIdWithProjectionResults() {
		//@formatter:off
		Object result = requests.get(getPathEntities(SimpleEntity.T, 2))
				.urlParameter("sessionId", "anything")
				.urlParameter("projection", "results")
				.accept(JSON)
				.execute(200);
		//@formatter:on

		Assert.assertTrue(result instanceof List);
	}

	@Test
	public void byIdWithProjectionEnvelope() {
		//@formatter:off
		Object result = requests.get(getPathEntities(SimpleEntity.T, 2))
				.urlParameter("sessionId", "anything")
				.urlParameter("projection", "envelope")
				.accept(JSON)
				.execute(200);
		//@formatter:on

		Assert.assertTrue(result instanceof EntityQueryResult);
	}

	@Test
	public void withDepthShallow() {
		//@formatter:off
		List<ComplexEntity> results = requests.get(getPathEntities(ComplexEntity.T))
				.urlParameter("sessionId", "anything")
				.urlParameter("depth", "shallow")
				.accept(JSON)
				.execute(200);
		//@formatter:on

		ComplexEntity entity = results.get(0);
		Assert.assertNull(entity.getAnotherComplexEntityProperty());
	}

	@Test
	public void withDepth0() {
		//@formatter:off
		List<ComplexEntity> results = requests.get(getPathEntities(ComplexEntity.T))
				.urlParameter("sessionId", "anything")
				.urlParameter("depth", "0")
				.accept(JSON)
				.execute(200);
		//@formatter:on

		ComplexEntity entity = results.get(0);
		Assert.assertNotNull(entity.getStringProperty());
		Assert.assertNull(entity.getAnotherComplexEntityProperty());
	}

	@Test
	public void withDepth1() {
		//@formatter:off
		List<ComplexEntity> results = requests.get(getPathEntities(ComplexEntity.T))
				.urlParameter("sessionId", "anything")
				.urlParameter("depth", "1")
				.urlParameter("entityRecurrenceDepth", "0")
				.urlParameter("typeExplicitness", "always")
				.accept(JSON)
				.execute(200);
		//@formatter:on

		ComplexEntity entity = results.get(0);
		AnotherComplexEntity entity1 = entity.getAnotherComplexEntityProperty();
		Assert.assertNull(entity1.getAnotherComplexEntityProperty());
	}

	@Test
	public void withDepth2() {
		//@formatter:off
		List<ComplexEntity> results = requests.get(getPathEntities(ComplexEntity.T))
				.urlParameter("sessionId", "anything")
				.urlParameter("depth", "2")
				.urlParameter("entityRecurrenceDepth", "0")
				.urlParameter("typeExplicitness", "always")
				.accept(JSON)
				.execute(200);
		//@formatter:on

		ComplexEntity entity = results.get(0);
		AnotherComplexEntity entity1 = entity.getAnotherComplexEntityProperty();
		AnotherComplexEntity entity2 = entity1.getAnotherComplexEntityProperty();
		Assert.assertNull(entity2.getAnotherComplexEntityProperty());
	}

	@Test
	public void withDepthReachable() {
		//@formatter:off
		List<ComplexEntity> results = requests.get(getPathEntities(ComplexEntity.T))
				.urlParameter("sessionId", "anything")
				.urlParameter("depth", "reachable")
				.accept(JSON)
				.execute(200);
		//@formatter:on

		ComplexEntity entity = results.get(0);
		AnotherComplexEntity entity1 = entity.getAnotherComplexEntityProperty();
		AnotherComplexEntity entity2 = entity1.getAnotherComplexEntityProperty();
		AnotherComplexEntity entity3 = entity2.getAnotherComplexEntityProperty();
		AnotherComplexEntity entity4 = entity3.getAnotherComplexEntityProperty();
		Assert.assertNull(entity4.getAnotherComplexEntityProperty());
	}
}
