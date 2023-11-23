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

import static com.braintribe.model.query.OrderingDirection.ascending;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;

import com.braintribe.model.access.hibernate.base.HibernateAccessRecyclingTestBase;
import com.braintribe.model.access.hibernate.base.HibernateBaseModelTestBase;
import com.braintribe.model.access.hibernate.base.model.simple.BasicScalarEntity;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.query.SelectQuery;

/**
 * @see HibernateAccessRecyclingTestBase
 * 
 * @author peter.gazdik
 */
public class Pagination_HbmTest extends HibernateBaseModelTestBase {

	@Test
	public void simplePagination() throws Exception {

		createBse("BSE-1");
		createBse("BSE-2");
		createBse("BSE-3");
		createBse("BSE-4");

		session.commit();

		resetGmSession();

//		assertContainsForLimitOffset(1, 0, "BSE-1");
//		assertContainsForLimitOffset(1, 1, "BSE-2");
//		assertContainsForLimitOffset(2, 0, "BSE-1", "BSE-2");
		assertContainsForLimitOffset(0, 2, "BSE-3", "BSE-4");
//		assertContainsForLimitOffset(-5, 2, "BSE-3", "BSE-4");
	}

	private void assertContainsForLimitOffset(int limit, int offset, String... expectedNames) {
		List<String> names = queryPaginated(limit, offset);

		assertThat(names).containsExactly(expectedNames);
	}

	private List<String> queryPaginated(int limit, int offset) {
		return session.query().select(query(limit, offset)).list();
	}

	private SelectQuery query(int limit, int offset) {
		return new SelectQueryBuilder() //
				.select("e", "name") //
				.from(BasicScalarEntity.T, "e") //
				.orderBy(ascending).property("e", "name") //
				.paging(limit, offset) //
				.done();
	}

}
