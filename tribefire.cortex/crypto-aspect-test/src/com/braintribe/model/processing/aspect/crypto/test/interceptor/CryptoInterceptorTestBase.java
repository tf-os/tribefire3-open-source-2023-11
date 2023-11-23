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
package com.braintribe.model.processing.aspect.crypto.test.interceptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.fest.assertions.Assertions;
import org.junit.Assert;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.StandardIdentifiable;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.session.GmSession;
import com.braintribe.model.processing.aspect.crypto.test.CryptoAspectTestBase;
import com.braintribe.model.processing.aspect.crypto.test.commons.TestDataProvider;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.EntityQueryResult;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.PropertyQueryResult;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SelectQueryResult;

public class CryptoInterceptorTestBase extends CryptoAspectTestBase {
	
	public <T extends StandardIdentifiable> void testCreation(EntityType<T> entityType) throws Exception {
		
		create(entityType, TestDataProvider.inputAString);
		
		assertProperties(entityType, TestDataProvider.inputAString);
		
	}
	
	public <T extends StandardIdentifiable> void testUpdate(EntityType<T> type) throws Exception {

		T managedEntity = create(type, TestDataProvider.inputAString);
		
		assertProperties(type, TestDataProvider.inputAString);
		
		update(managedEntity, TestDataProvider.inputBString);
		
		assertProperties(type, TestDataProvider.inputBString);
		
	}
	
	public <T extends StandardIdentifiable> T create(EntityType<T> entityType, Long id, String propertiesValue) throws Exception {
		T entity = aopSession.create(entityType);
		update(entity, propertiesValue);
		entity.setId(id);
		aopSession.commit();
		return entity;
	}
	
	public <T extends StandardIdentifiable> T create(EntityType<T> entityType, String propertiesValue) throws Exception {
		T entity = create(aopSession, entityType);
		update(entity, propertiesValue);
		aopSession.commit();
		return entity;
	}
	
	public <T extends StandardIdentifiable> List<T> create(EntityType<T> entityType, String propertiesValue, int n) throws Exception {
		List<T> l = new ArrayList<>();
		for (int i = 0; i < n; i++) {
			T entity = create(aopSession, entityType);
			update(entity, propertiesValue);
			l.add(entity);
		}
		aopSession.commit();
		return l;
	}
	
	public <T extends StandardIdentifiable> List<T> create(String typeSinature, String propertiesValue, int n) throws Exception {
		List<T> l = new ArrayList<>();
		for (int i = 0; i < n; i++) {
			T entity = create(aopSession, typeSinature);
			update(entity, propertiesValue);
			l.add(entity);
		}
		aopSession.commit();
		return l;
	}
	
	public <T extends StandardIdentifiable> T create(GmSession gmSession, EntityType<T> entityType) {
		T entity = gmSession.create(entityType);
		return entity;
	}
	
	public <T extends StandardIdentifiable> T create(GmSession gmSession, String typeSignature) {
		EntityType<T> entityType = GMF.getTypeReflection().getEntityType(typeSignature);
		T entity = gmSession.create(entityType);
		return entity;
	}

	public <T extends StandardIdentifiable> T update(T entity, String propertiesValue) throws Exception {

		EntityType<T> entityType = entity.entityType();

		for (Property property : entityType.getProperties()) {
			if (property.isIdentifying() || property.isGlobalId()) {
				continue;
			}
			property.set(entity, propertiesValue);
		}
		
		aopSession.commit();

		return entity;
	}

	public <T extends StandardIdentifiable> void assertProperties(EntityType<T> type, String propertiesValue)
			throws Exception {

		T queriedEntity = queryFirst(type);

		EntityType<T> entityType = queriedEntity.entityType();

		for (Property property : entityType.getProperties()) {

			if (property.isIdentifying() || property.isGlobalId()) {
				continue;
			}

			Object newValue = property.get(queriedEntity);

			assertPropertyValue(entityType.getTypeSignature() + ":" + property.getName(), propertiesValue,
					newValue);

		}

	}

	public void assertPropertyValue(String propertyPath, Object originalValue, Object newValue) {

		String configKey = TestDataProvider.propertyToConfigurationMap.get(propertyPath);

		if (configKey != null) {
			
			Map<String, String> expectedValuesMap = TestDataProvider.configurationTestInputs.get(configKey);
			
			if (expectedValuesMap == null) {
				throw new RuntimeException("Test is misconfigured. No list of expected values found for the desired configuration: "+configKey);
			}

			Object expectedValue = expectedValuesMap.get(originalValue);

			Assert.assertEquals(propertyPath + " value should have matched the expected " + configKey
					+ " encrypted value: [" + expectedValue + "], but it was [" + newValue + "]", expectedValue,
					newValue);

		} else {
			Assert.assertEquals(propertyPath + " value should have matched the originally provided value ["
					+ originalValue + "], but it was [" + newValue + "]", originalValue, newValue);
		}

	}

	protected List<GenericEntity> assertReturnedEntities(EntityQuery query, int expectedCount) throws Exception {
		List<GenericEntity> entities = queryEntities(query);
		Assertions.assertThat(entities).isNotNull().as("Wrong number of entities returned from the query")
				.hasSize(expectedCount);
		return entities;
	}

	protected void assertResultIsEmpty(EntityQuery query) throws Exception {
		List<GenericEntity> entities = queryEntities(query);
		Assertions.assertThat(entities).isNotNull().isEmpty();
	}

	@SuppressWarnings("unchecked")
	protected <T extends GenericEntity> T queryFirst(EntityType<T> type) throws Exception {

		EntityQuery entityQuery = EntityQueryBuilder.from(type).done();

		EntityQueryResult entityQueryResult = access.queryEntities(entityQuery);

		return (T) entityQueryResult.getEntities().iterator().next();
	}

	@SuppressWarnings("unchecked")
	protected <T extends StandardIdentifiable> T query(T entity) throws Exception {

		EntityQuery entityQuery = EntityQueryBuilder.from(entity.getClass()).where().property("id").eq(entity.getId())
				.done();

		EntityQueryResult entityQueryResult = access.queryEntities(entityQuery);

		return (T) entityQueryResult.getEntities().iterator().next();
	}

	@SuppressWarnings("unchecked")
	protected <T extends StandardIdentifiable> T queryById(Class<T> type, Object id) throws Exception {

		EntityQuery entityQuery = EntityQueryBuilder.from(type).where().property("id").eq(id).done();

		EntityQueryResult entityQueryResult = access.queryEntities(entityQuery);

		return (T) entityQueryResult.getEntities().iterator().next();
	}

	protected List<GenericEntity> queryEntities(EntityQuery query) throws Exception {
		EntityQueryResult queryResult = access.queryEntities(query);
		return queryResult.getEntities();
	}

	protected Object queryProperty(PropertyQuery query) throws Exception {
		PropertyQueryResult result = access.queryProperty(query);
		return result.getPropertyValue();
	}

	protected List<?> query(SelectQuery query) throws Exception {
		SelectQueryResult queryResult = access.query(query);
		return queryResult.getResults();
	}

	protected List<GenericEntity> queryEntitiesWithoutAspect(EntityQuery query) throws Exception {
		EntityQueryResult queryResult = accessWithoutAspect.queryEntities(query);
		return queryResult.getEntities();
	}

	protected Object queryPropertyWithoutAspect(PropertyQuery query) throws Exception {
		PropertyQueryResult result = accessWithoutAspect.queryProperty(query);
		return result.getPropertyValue();
	}

	protected List<?> queryWithoutAspect(SelectQuery query) throws Exception {
		SelectQueryResult queryResult = accessWithoutAspect.query(query);
		return queryResult.getResults();
	}
	
}
