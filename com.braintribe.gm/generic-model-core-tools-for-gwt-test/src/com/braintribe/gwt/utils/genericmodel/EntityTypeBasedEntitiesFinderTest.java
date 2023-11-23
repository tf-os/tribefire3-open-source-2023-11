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
package com.braintribe.gwt.utils.genericmodel;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.processing.session.impl.persistence.BasicPersistenceGmSession;
import com.braintribe.testing.model.test.technical.features.CollectionEntity;
import com.braintribe.testing.model.test.technical.features.ComplexEntity;
import com.braintribe.testing.model.test.technical.features.SimpleEntity;
import com.braintribe.testing.model.test.technical.features.SimpleTypesEntity;
import com.braintribe.testing.model.test.testtools.TestModelTestTools;

public class EntityTypeBasedEntitiesFinderTest {

	private BasicPersistenceGmSession session = null;

	private final int expectedSizeForSimpleEntities = 3;
	private final int expectedSizeForComplexEntities = 1;

	@Before
	public void setUp() throws GmSessionException {

		final IncrementalAccess access = TestModelTestTools.newSmoodAccessMemoryOnly("test", TestModelTestTools.createTestModelMetaModel());
		this.session = new BasicPersistenceGmSession();
		this.session.setIncrementalAccess(access);

		final SimpleEntity simpleEntity1 = this.session.create(SimpleEntity.T);
		simpleEntity1.setBooleanProperty(false);
		simpleEntity1.setStringProperty("StringProperty1");

		final SimpleEntity simpleEntity2 = this.session.create(SimpleEntity.T);
		simpleEntity2.setBooleanProperty(true);
		simpleEntity2.setStringProperty("StringProperty2");

		final SimpleEntity simpleEntity3 = this.session.create(SimpleEntity.T);
		simpleEntity3.setBooleanProperty(false);
		simpleEntity3.setStringProperty("StringProperty3");

		final ComplexEntity complexEntity = this.session.create(ComplexEntity.T);
		complexEntity.setSimpleEntityProperty(simpleEntity1);
		complexEntity.setBooleanProperty(true);
		complexEntity.setStringProperty("StringPropertC1");

		final SimpleTypesEntity simpleTypesEntity = this.session.create(SimpleTypesEntity.T);
		simpleTypesEntity.setBooleanProperty(true);
		simpleTypesEntity.setDateProperty(new Date());
		simpleTypesEntity.setStringProperty("StringPropertSTE1");

		final SimpleTypesEntity simpleTypesEntity2 = this.session.create(SimpleTypesEntity.T);
		simpleTypesEntity2.setBooleanProperty(true);
		simpleTypesEntity2.setDateProperty(new Date());
		simpleTypesEntity2.setStringProperty("StringPropertSTE2");

		final CollectionEntity collectionEntity = this.session.create(CollectionEntity.T);
		final List<SimpleEntity> simpleEntityList = new ArrayList<SimpleEntity>();
		simpleEntityList.add(simpleEntity1);
		simpleEntityList.add(simpleEntity2);
		collectionEntity.setSimpleEntityList(simpleEntityList);
		this.session.commit();
	}

	@Test
	public void testTypeBasedEntitiesFinderForOneType() {
		final HashSet<Class<? extends GenericEntity>> entityTypes = new HashSet<Class<? extends GenericEntity>>();
		entityTypes.add(SimpleEntity.class);

		final EntityTypeBasedEntitiesFinder entitiesFinder = new EntityTypeBasedEntitiesFinder();
		entitiesFinder.setEntityTypes(entityTypes);

		final Set<GenericEntity> foundEntities = entitiesFinder.findEntities(this.session);
		assertThat(foundEntities.size()).as("The size of the founded entities")
				.isEqualTo(this.expectedSizeForSimpleEntities);
	}

	@Test
	public void testTypeBasedEntitiesFinderForTwoTypes() {
		final HashSet<Class<? extends GenericEntity>> entityTypes = new HashSet<Class<? extends GenericEntity>>();
		entityTypes.add(SimpleEntity.class);
		entityTypes.add(ComplexEntity.class);

		final EntityTypeBasedEntitiesFinder entitiesFinder = new EntityTypeBasedEntitiesFinder();
		entitiesFinder.setEntityTypes(entityTypes);

		final Set<GenericEntity> foundEntities = entitiesFinder.findEntities(this.session);
		assertThat(foundEntities.size()).as("The size of the founded entities")
				.isEqualTo(this.expectedSizeForSimpleEntities + this.expectedSizeForComplexEntities);
	}
}
