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

import java.util.Date;

import org.junit.Test;

import com.braintribe.model.access.hibernate.base.HibernateAccessRecyclingTestBase;
import com.braintribe.model.access.hibernate.base.HibernateBaseModelTestBase;
import com.braintribe.model.access.hibernate.base.model.simple.BasicScalarEntity;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.SelectQuery;

/**
 * Basic tests for entity with scalar properties only.
 * 
 * @see HibernateAccessRecyclingTestBase
 * 
 * @author peter.gazdik
 */
public class Deleting_HbmTest extends HibernateBaseModelTestBase {

	static final String _string = "string";
	static final int _integer = 123;
	static final long _long = 9_876_543_210L;
	static final float _float = 0.123F;
	static final double _double = 1.012_345_678_9D;
	static final Date date = new Date(10_000_000L);

	@Test
	public void simpleDelete() throws Exception {
		prepareSclarEntity();

		BasicScalarEntity bse;
		bse = findsBseByProperty(BasicScalarEntity.name, "BSE 1");
		assertThat(bse).isNotNull();

		session.deleteEntity(bse);
		session.commit();

		bse = findsBseByProperty(BasicScalarEntity.name, "BSE 1");
		assertThat(bse).isNull();
	}

	private BasicScalarEntity findsBseByProperty(String propertyName, Object propertyValue) {
		return findsBseByProperty(propertyName, propertyValue, session);
	}

	private BasicScalarEntity findsBseByProperty(String propertyName, Object propertyValue, PersistenceGmSession session) {
		return session.query().select(queryBseByProperty(propertyName, propertyValue)).first();

	}

	private SelectQuery queryBseByProperty(String propertyName, Object propertyValue) {
		return new SelectQueryBuilder() //
				.from(BasicScalarEntity.T, "e") //
				.where().property("e", propertyName).eq(propertyValue) //
				.done();
	}

	private void prepareSclarEntity() {
		BasicScalarEntity bse = session.create(BasicScalarEntity.T);
		bse.setName("BSE 1");

		session.commit();

		resetGmSession();
	}

}
