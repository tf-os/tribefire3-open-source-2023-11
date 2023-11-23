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

import static com.braintribe.utils.lcd.CollectionTools2.asSet;
import static com.braintribe.utils.lcd.CollectionTools2.first;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;

import com.braintribe.model.access.hibernate.base.HibernateAccessRecyclingTestBase;
import com.braintribe.model.access.hibernate.base.model.acl.AclHaTestEntity;
import com.braintribe.model.access.hibernate.hql.SelectHqlBuilderTest;
import com.braintribe.model.acl.Acl;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.query.EntityQuery;

/**
 * Basic tests for queries that occur when ACL is used, testing the correctness of the query.
 * <p>
 * This exists because there is a special optimization for this kind of queries - see also {@link SelectHqlBuilderTest#aclTest()}.
 * 
 * @see SelectHqlBuilderTest#aclTest()
 * @see com.braintribe.model.access.hibernate.hql.DisjunctedInOptimizer
 * @see HibernateAccessRecyclingTestBase
 * 
 * @author peter.gazdik
 */
public class Acl_HbmTest extends HibernateAccessRecyclingTestBase {

	@Test
	public void storesAndLoadsScalarEntity() throws Exception {
		createAclEntities();

		List<GenericEntity> entities = accessDriver.query(queryAclEntity()).getEntities();
		assertThat(entities).hasSize(1);

		AclHaTestEntity visible = first(entities);
		assertThat(visible.getName()).startsWith("Ada");
	}

	// Query for user with roles "R1" and "R2"
	private EntityQuery queryAclEntity() {
		String ACC = "acl.accessibility";

		// @formatter:off
		return EntityQueryBuilder.from(AclHaTestEntity.T) //
				.where()
					.conjunction()
						.disjunction()
						.property("name").eq("WHATEVER") // irrelevant condition to make the query less trivial
							.value("R1").in().property(ACC)
							.value("R2").in().property(ACC)
						.close()
						.negation()
							.disjunction()
								.value("!R1").in().property(ACC)
								.value("!R2").in().property(ACC)
							.close()
					.close()
					.done();
		// @formatter:on
	}

	private void createAclEntities() {
		// visible - user has role "R1"
		createAclEntity("Ada 'Computer' Lovelace", "R1", "X", "!Y");

		// hidden - user has role "R2"
		createAclEntity("Abraham 'Cylinder' Lincoln", "R1", "!R2");
	}

	private AclHaTestEntity createAclEntity(String name, String... accessibility) {
		Acl acl = session.create(Acl.T);
		// Granted/Denied
		acl.setAccessibility(asSet(accessibility));

		AclHaTestEntity entity = session.create(AclHaTestEntity.T);
		entity.setName(name);
		entity.setAcl(acl);

		session.commit();

		return entity;
	}

	@Override
	protected GmMetaModel model() {
		return HibernateAccessRecyclingTestBase.hibernateModels.acl();
	}

}
