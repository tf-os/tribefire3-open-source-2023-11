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
package com.braintribe.model.access.hibernate.tests;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.braintribe.model.access.hibernate.base.HibernateAccessRecyclingTestBase;
import com.braintribe.model.access.hibernate.base.HibernateBaseModelTestBase;
import com.braintribe.model.access.hibernate.base.model.simple.BasicCollectionEntity;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.SelectQuery;

/**
 * Basic tests for deleting an entity which is referenced by another entity.
 * 
 * @see HibernateAccessRecyclingTestBase
 * 
 * @author peter.gazdik
 */
public class Deleting_Referenced_HbmTest extends HibernateBaseModelTestBase {

	private static final String ENTITY_TO_DELETE_NAME = "entityToDelete";
	private static final String MAP_OWNER_NAME = "mapOwner";

	@Test
	public void delete_dropReference_mapKey() throws Exception {
		BasicCollectionEntity toDelete = newCe(ENTITY_TO_DELETE_NAME);

		BasicCollectionEntity mapOwner = newCe(MAP_OWNER_NAME);
		mapOwner.getEntityToString().put(toDelete, "value");

		commitAndReset();

		BasicCollectionEntity _toDelete;
		BasicCollectionEntity _mapOwner;
		_toDelete = findsBceByProperty(BasicCollectionEntity.name, ENTITY_TO_DELETE_NAME);
		assertThat(_toDelete).isNotNull();

		_mapOwner = findsBceByProperty(BasicCollectionEntity.name, MAP_OWNER_NAME);
		assertThat(_mapOwner).isNotNull();
		assertThat(_mapOwner.getEntityToString()).isNotEmpty();

		session.deleteEntity(_toDelete);
		session.commit();

		_toDelete = findsBceByProperty(BasicCollectionEntity.name, ENTITY_TO_DELETE_NAME);
		assertThat(_toDelete).isNull();

		_mapOwner = findsBceByProperty(BasicCollectionEntity.name, MAP_OWNER_NAME);
		assertThat(_mapOwner).isNotNull();
		assertThat(_mapOwner.getEntityToString()).isEmpty();
	}

	private void commitAndReset() {
		session.commit();
		resetGmSession();
	}

	private BasicCollectionEntity findsBceByProperty(String propertyName, Object propertyValue) {
		return findsBseByProperty(propertyName, propertyValue, session);
	}

	private BasicCollectionEntity findsBseByProperty(String propertyName, Object propertyValue, PersistenceGmSession session) {
		return session.query().select(queryBseByProperty(propertyName, propertyValue)).first();

	}

	private SelectQuery queryBseByProperty(String propertyName, Object propertyValue) {
		return new SelectQueryBuilder() //
				.from(BasicCollectionEntity.T, "e") //
				.where().property("e", propertyName).eq(propertyValue) //
				.done();
	}

	private BasicCollectionEntity newCe(String name) {
		BasicCollectionEntity bse = session.create(BasicCollectionEntity.T);
		bse.setName(name);
		return bse;
	}

}
