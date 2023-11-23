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
package com.braintribe.utils.genericmodel;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.gwt.utils.genericmodel.GMCoreTools;
import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.managed.NotFoundException;
import com.braintribe.model.processing.session.impl.persistence.BasicPersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.testing.model.test.technical.features.ComplexEntity;
import com.braintribe.testing.model.test.technical.features.SimpleEntity;
import com.braintribe.testing.model.test.testtools.TestModelTestTools;

public class ConvenientStandardGMSessionTest {
	private BasicPersistenceGmSession session = null;
	private ConvenientStandardGMSession sessionConvenientGmSessionTest = null;
	private long simpleEntityId;
	private long complexEntityId;
	private final String stringPropertyValue = "String value";

	@Before
	public void setUp() throws GmSessionException {

		final IncrementalAccess access = TestModelTestTools.newSmoodAccessMemoryOnly("test", TestModelTestTools.createTestModelMetaModel());
		this.session = new BasicPersistenceGmSession();
		this.session.setIncrementalAccess(access);
		this.sessionConvenientGmSessionTest = new ConvenientStandardGMSession();
		this.sessionConvenientGmSessionTest.setIncrementalAccess(access);

		final SimpleEntity simpleEntity1 = this.session.create(SimpleEntity.T);
		simpleEntity1.setBooleanProperty(false);
		simpleEntity1.setStringProperty(this.stringPropertyValue);
		this.session.commit();
		this.simpleEntityId = simpleEntity1.getId();

		final SimpleEntity simpleEntity2 = this.session.create(SimpleEntity.T);
		simpleEntity2.setBooleanProperty(false);
		simpleEntity2.setStringProperty(this.stringPropertyValue);
		this.session.commit();

		final SimpleEntity simpleEntity3 = this.session.create(SimpleEntity.T);
		simpleEntity3.setBooleanProperty(true);
		simpleEntity3.setStringProperty(this.stringPropertyValue);
		this.session.commit();

		final ComplexEntity complexEntity = this.session.create(ComplexEntity.T);
		complexEntity.setSimpleEntityProperty(simpleEntity1);
		complexEntity.setStringProperty(this.stringPropertyValue);
		this.session.commit();
		this.complexEntityId = complexEntity.getId();
	}

	@Test
	public void testEntityById() throws Exception {
		SimpleEntity retrievedEntity = this.sessionConvenientGmSessionTest.getEntityById(SimpleEntity.T, this.simpleEntityId);
		assertThat(retrievedEntity).isNotNull();
		retrievedEntity = this.sessionConvenientGmSessionTest.getEntityById(SimpleEntity.T, this.simpleEntityId + 100000);
		assertThat(retrievedEntity).isNull();

	}

	@Test(expected = NotFoundException.class)
	public void testExistingEntityById() throws Exception {
		final SimpleEntity retrievedEntity = this.sessionConvenientGmSessionTest.getExistingEntityById(SimpleEntity.T, this.simpleEntityId);
		assertThat(retrievedEntity).isNotNull();
		this.sessionConvenientGmSessionTest.getExistingEntityById(SimpleEntity.T, this.simpleEntityId + 100);
	}

	@Test
	public void testEntityByIdWithTC() throws Exception {

		final ComplexEntity retrievedEntity = this.sessionConvenientGmSessionTest.getExistingEntityById(ComplexEntity.T, this.complexEntityId);
		assertThat(retrievedEntity).isNotNull();

		assertThat(GMCoreTools.isAbsent(retrievedEntity, "simpleEntityProperty")).isTrue();

		final TraversingCriterion traversingCriterion1 = TC.create().negation().joker().done();
		this.sessionConvenientGmSessionTest.getExistingEntityById(ComplexEntity.T, this.complexEntityId, traversingCriterion1);
		assertThat(GMCoreTools.isAbsent(retrievedEntity, "simpleEntityProperty")).isFalse();
	}

	@Test
	public void testEntityByProperty() throws Exception {
		SimpleEntity retrievedEntity = this.sessionConvenientGmSessionTest.getEntityByProperty(SimpleEntity.class, "stringProperty",
				this.stringPropertyValue);
		assertThat(retrievedEntity).isNotNull();

		retrievedEntity = this.sessionConvenientGmSessionTest.getEntityByProperty(SimpleEntity.class, "stringProperty",
				"Simple's string property that does not exist");
		assertThat(retrievedEntity).isNull();
	}

	@Test(expected = Exception.class)
	public void testExistingEntityByProperty() throws Exception {
		final SimpleEntity retrievedEntity = this.sessionConvenientGmSessionTest.getExistingEntityByProperty(SimpleEntity.class, "stringProperty",
				this.stringPropertyValue);
		assertThat(retrievedEntity).isNotNull();

		this.sessionConvenientGmSessionTest.getExistingEntityByProperty(SimpleEntity.class, "stringProperty",
				"Simple's string property that does not exist");
	}

	@Test
	public void testEntitiesByProperty() throws Exception {
		List<SimpleEntity> retrievedEntities = this.sessionConvenientGmSessionTest.getEntitiesByProperty(SimpleEntity.class, "stringProperty",
				this.stringPropertyValue);
		assertThat(retrievedEntities).hasSize(3);

		retrievedEntities = this.sessionConvenientGmSessionTest.getEntitiesByProperty(SimpleEntity.class, "stringProperty",
				"Simple's string property that does not exist");
		assertThat(retrievedEntities).hasSize(0);
		assertThat(retrievedEntities).isEmpty();
		assertThat(retrievedEntities).isNotNull();
	}

	@Test
	public void testEntitiesByProperties() throws Exception {
		final Map<String, Object> propertyNamesAndValues = new HashMap<String, Object>();
		propertyNamesAndValues.put("stringProperty", this.stringPropertyValue);
		propertyNamesAndValues.put("booleanProperty", false);

		final List<SimpleEntity> simpleEntities = this.sessionConvenientGmSessionTest.getEntitiesByProperties(SimpleEntity.class,
				propertyNamesAndValues);
		assertThat(simpleEntities).hasSize(2);
	}

	@Test
	public void testEntitiesByQuery() throws Exception {

		final EntityQuery entityQuery = EntityQueryBuilder.from(SimpleEntity.class).where().property("stringProperty").eq(this.stringPropertyValue)
				.done();

		final List<SimpleEntity> retrievedEntities = this.sessionConvenientGmSessionTest.getEntitiesByQuery(entityQuery);
		assertThat(retrievedEntities).hasSize(3);
	}

	@Test
	public void testEntityByQuery() throws Exception {

		EntityQuery entityQuery = EntityQueryBuilder.from(SimpleEntity.class).where().property("stringProperty").eq(this.stringPropertyValue).done();

		SimpleEntity retrievedEntity = this.sessionConvenientGmSessionTest.getEntityByQuery(entityQuery);
		assertThat(retrievedEntity).isNotNull();

		entityQuery = EntityQueryBuilder.from(SimpleEntity.class).where().property("stringProperty")
				.eq("Simple's string property that does not exist").done();

		retrievedEntity = this.sessionConvenientGmSessionTest.getEntityByQuery(entityQuery);
		assertThat(retrievedEntity).isNull();
	}

	@Test(expected = Exception.class)
	public void testExistingEntityByQuery() throws Exception {

		EntityQuery entityQuery = EntityQueryBuilder.from(SimpleEntity.class).where().property("stringProperty").eq(this.stringPropertyValue).done();

		final SimpleEntity retrievedEntity = this.sessionConvenientGmSessionTest.getExistingEntityByQuery(entityQuery);
		assertThat(retrievedEntity).isNotNull();

		entityQuery = EntityQueryBuilder.from(SimpleEntity.class).where().property("stringProperty")
				.eq("Simple's string property that does not exist").done();

		this.sessionConvenientGmSessionTest.getExistingEntityByQuery(entityQuery);

	}
}
