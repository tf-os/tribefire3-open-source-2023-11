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

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static com.braintribe.utils.lcd.CollectionTools2.first;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;

import com.braintribe.model.access.hibernate.base.HibernateAccessRecyclingTestBase;
import com.braintribe.model.access.hibernate.base.HibernateBaseModelTestBase;
import com.braintribe.model.access.hibernate.base.model.simple.BasicCollectionEntity;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.query.SelectQuery;

/**
 * Testing querying related to collectionsBasic tests for entity with scalar properties only.
 * 
 * @see HibernateAccessRecyclingTestBase
 * 
 * @author peter.gazdik
 */
public class Collections_HbmTest extends HibernateBaseModelTestBase {

	private static final String ENTITY_TO_DELETE_NAME = "entityToDelete";
	private static final String MAP_OWNER_NAME = "mapOwner";

	@Test
	public void queryOwnerByMapKey() throws Exception {
		BasicCollectionEntity keyEntity = newCe(ENTITY_TO_DELETE_NAME);

		BasicCollectionEntity mapOwner = newCe(MAP_OWNER_NAME);
		mapOwner.getEntityToString().put(keyEntity, "value");

		commitAndReset();

		SelectQuery query = new SelectQueryBuilder() //
				.select("e") //
				.from(BasicCollectionEntity.T, "e") //
				/**/.join("e", "entityToString", "map") //
				.where() //
				/**/.mapKey("map").eq().entity(keyEntity) //
				.done();

		List<BasicCollectionEntity> entities = session.query().select(query).list();

		assertThat(entities).hasSize(1);

		BasicCollectionEntity e = first(entities);
		assertThat(e.getName()).isEqualTo(MAP_OWNER_NAME);
	}

	private void commitAndReset() {
		session.commit();
		resetGmSession();
	}

	private BasicCollectionEntity newCe(String name) {
		BasicCollectionEntity bse = session.create(BasicCollectionEntity.T);
		bse.setName(name);
		return bse;
	}

}
