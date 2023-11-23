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

import java.util.HashMap;
import java.util.List;

import org.junit.Assert;

import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.ddra.endpoints.AbstractDdraRestServletTest;
import com.braintribe.model.processing.ddra.endpoints.ioc.TestExceptionHandlerSpace;
import com.braintribe.model.processing.ddra.endpoints.rest.v2.handlers.AbstractRestV2Handler;
import com.braintribe.model.processing.ddra.endpoints.rest.v2.handlers.RestV2Handler;
import com.braintribe.model.processing.ddra.endpoints.rest.v2.ioc.TestAccessSpace;
import com.braintribe.model.processing.ddra.endpoints.rest.v2.ioc.TestModelAccessoryFactory;
import com.braintribe.model.processing.ddra.endpoints.rest.v2.ioc.TestRestEvaluator;
import com.braintribe.model.processing.ddra.endpoints.rest.v2.ioc.TestSessionFactory;
import com.braintribe.model.processing.ddra.endpoints.rest.v2.ioc.TestTraversingCriteriaMap;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.testing.model.test.technical.features.ComplexEntity;
import com.braintribe.testing.model.test.technical.features.SimpleEntity;
import com.braintribe.testing.tools.gm.GmTestTools;

public class AbstractRestV2Test extends AbstractDdraRestServletTest {

	protected static IncrementalAccess access;
	protected static RestV2Server servlet;
	protected static TestRestEvaluator evaluator = new TestRestEvaluator();
	protected static TestSessionFactory sessionFactory = new TestSessionFactory();


	protected static void setupServlet() {
		servlet = new RestV2Server();
		servlet.setMarshallerRegistry(marshallerRegistry);
		servlet.setEvaluator(evaluator);
		servlet.setUsersSessionFactory(sessionFactory);
		servlet.setSystemSessionFactory(sessionFactory);
		servlet.setExceptionHandler(TestExceptionHandlerSpace.exceptionHandler());
		servlet.setTraversingCriteriaMap(TestTraversingCriteriaMap.traversingCriteriaMap());
		
		resetServletAccess();
		setupStandardUndertowServer("/tribefire-services", "service-servlet", servlet, "/rest/v2/*");
	}

	protected static void wireHandler(AbstractRestV2Handler<?> handler) {
		handler.setEvaluator(evaluator);
		handler.setMarshallerRegistry(marshallerRegistry);
		handler.setTraversingCriteriaMap(TestTraversingCriteriaMap.traversingCriteriaMap());
	}

	protected static void addHandler(String path, @SuppressWarnings("rawtypes") RestV2Handler handler) {
		if (servlet.getHandlers() == null) {
			servlet.setHandlers(new HashMap<>());
		}

		servlet.getHandlers().put(path, handler);
	}

	protected static void resetServletAccess() {
		resetServletAccess(false);
	}

	protected static void resetServletAccess(boolean ignorePartitions) {
		resetServletAccess(ignorePartitions, true);
	}

	protected static void resetServletAccess(boolean ignorePartitions, boolean addEntities) {
		access = TestAccessSpace.testAccess(ignorePartitions, addEntities);
		evaluator.reset(access);
		sessionFactory.reset(access);
		servlet.setModelAccessoryFactory(TestModelAccessoryFactory.testModelAccessoryFactory(access));
	}

	protected String getPathEntities(EntityType<?> entityType, Object... segments) {
		return getPath(entityType, "entities", segments);
	}

	protected String getPathEntities(String entityType, Object... segments) {
		return getPath(entityType, "entities", segments);
	}

	protected String getPathProperties(EntityType<?> entityType, Object... segments) {
		return getPath(entityType, "properties", segments);
	}

	protected String getPathProperties(String entityType, Object... segments) {
		return getPath(entityType, "properties", segments);
	}

	private String getPath(EntityType<?> entityType, String type, Object... segments) {
		return getPath(entityType.getTypeSignature(), type, segments);
	}

	private String getPath(String entityType, String type, Object... segments) {
		//@formatter:off
		StringBuilder sb = new StringBuilder()
				.append("/tribefire-services/rest/v2/")
				.append(type)
				.append("/test.access/")
				.append(entityType);
		//@formatter:on

		for (Object segment : segments) {
			if (segment != null) {
				sb.append("/").append(segment);
			}
		}

		return sb.toString();
	}

	protected List<GenericEntity> getEntities(EntityType<?> entityType) {
		return access.queryEntities(EntityQueryBuilder.from(entityType).done()).getEntities();
	}

	protected List<GenericEntity> getEntities(EntityType<?> entityType, String property, Object value) {
		return access.queryEntities(EntityQueryBuilder.from(entityType).where().property(property).eq(value).done()).getEntities();
	}

	protected <T extends GenericEntity> T getEntity(EntityType<T> entityType, String property, Object value) {
		//@formatter:off
		EntityQuery query = EntityQueryBuilder.from(entityType)
				.where().property(property).eq(value)
				.tc().negation().joker()
				.done();
		//@formatter:on

		List<GenericEntity> entities = access.queryEntities(query).getEntities();
		Assert.assertEquals(1, entities.size());
		return (T) entities.get(0);
	}

	protected <T extends GenericEntity> void setProperty(EntityType<T> entityType, int id, String property, Object value) {
		PersistenceGmSession session = GmTestTools.newSession(access);
		T entity = session.query().entities(EntityQueryBuilder.
				from(entityType).where().property("id").eq(Long.valueOf(id)).done()).unique();

		entityType.getProperty(property).set(entity, value);
		session.commit();
	}

	protected void assertSimpleEntity(Object result, String name, Long id) {
		assertSimpleEntity(result, name, id, "p0");
	}

	protected void assertSimpleEntity(Object result, String name, Long id, String partition) {
		Assert.assertTrue(result instanceof SimpleEntity);
		SimpleEntity entity = (SimpleEntity) result;
		Assert.assertEquals(name, entity.getStringProperty());
		Assert.assertEquals(id, entity.getId());
		Assert.assertEquals(partition, entity.getPartition());
		Assert.assertTrue(entity.getBooleanProperty());
	}

	protected void assertComplexEntity(Object result, long id, int intValue, String stringValue) {
		Assert.assertTrue(result instanceof ComplexEntity);
		ComplexEntity entity = (ComplexEntity) result;
		Assert.assertEquals(intValue, entity.getIntegerProperty().intValue());
		Assert.assertEquals(stringValue, entity.getStringProperty());
		Assert.assertEquals(Long.valueOf(id), entity.getId());
	}
}
