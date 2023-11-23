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

import org.junit.Test;

import com.braintribe.model.access.hibernate.base.HibernateAccessRecyclingTestBase;
import com.braintribe.model.access.hibernate.base.HibernateBaseModelTestBase;
import com.braintribe.model.access.hibernate.base.model.simple.BasicEntity;
import com.braintribe.model.access.hibernate.base.model.simple.BasicScalarEntity;

/**
 * This just shows how these tests work.
 * 
 * @see HibernateAccessRecyclingTestBase
 * 
 * @author peter.gazdik
 */
public class Aggregation_HbmTest extends HibernateBaseModelTestBase {

	@Test
	public void groupBySimple() throws Exception {
		bse("A", "MAN", 1);
		bse("B", "MAN", 2);
		bse("C", "WOMAN", 3);
		bse("D", "WOMAN", 4);
		bse("E", "WOMAN", 5);
		session.commit();

		runSelectQuery(from(BasicScalarEntity.T, "be") //
				.select("be", BasicScalarEntity.stringValue) //
				.select().count("be", BasicScalarEntity.name) //
				.select().sum("be", BasicScalarEntity.integerValue) //
				.done() //
		);

		qra.assertContains("MAN", 2L, 3L);
		qra.assertContains("WOMAN", 3L, 12L);
		qra.assertNoMoreResults();
	}

	@Test
	public void simpleGroupByDoneAutomatically() throws Exception {
		BasicScalarEntity a = bse("A", "MAN", 1);
		BasicScalarEntity c = bse("C", "WOMAN", 3);

		be("BSE-1", a, 1);
		be("BSE-2", a, 2);
		be("BSE-3", c, 3);
		be("BSE-4", c, 4);
		be("BSE-5", c, 5);
		session.commit();

		runSelectQuery(from(BasicEntity.T, "be") //
				.select("be", BasicEntity.scalarEntity) //
				.select().count("be", BasicEntity.name) //
				.select().sum("be", BasicEntity.integerValue) //
				.done() //
		);

		qra.assertContains(a, 2L, 3L);
		qra.assertContains(c, 3L, 12L);
		qra.assertNoMoreResults();
	}

	@Test
	public void groupByWithHaving() throws Exception {
		bse("A", "MAN", 1);
		bse("B", "MAN", 2);
		bse("C", "WOMAN", 3);
		bse("D", "WOMAN", 4);
		bse("E", "WOMAN", 5);
		session.commit();

		runSelectQuery(from(BasicScalarEntity.T, "be") //
				.select("be", BasicScalarEntity.stringValue) //
				.select().count("be", BasicScalarEntity.name) //
				.select().sum("be", BasicScalarEntity.integerValue) //
				.having()
					.count("be", BasicScalarEntity.name).ge(3L)
				.done() //
		);

		qra.assertContains("WOMAN", 3L, 12L);
		qra.assertNoMoreResults();
	}

	private BasicScalarEntity bse(String name, String stringValue, int integerValue) {
		BasicScalarEntity bse = createBse(name);
		bse.setStringValue(stringValue);
		bse.setIntegerValue(integerValue);

		return bse;
	}

	private BasicEntity be(String name, BasicScalarEntity bse, int integerValue) {
		BasicEntity be = createBe(name);
		be.setScalarEntity(bse);
		be.setIntegerValue(integerValue);

		return be;
	}

}
