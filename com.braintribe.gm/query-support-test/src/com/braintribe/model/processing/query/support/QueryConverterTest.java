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
package com.braintribe.model.processing.query.support;

import org.junit.Test;

import com.braintribe.model.processing.query.fluent.PropertyQueryBuilder;
import com.braintribe.model.processing.query.test.model.Company;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SimpleOrdering;
import com.braintribe.model.query.functions.ListIndex;
import com.braintribe.utils.junit.assertions.BtAssertions;

/**
 * Tests for {@link QueryConverter}
 */
public class QueryConverterTest {

	@Test
	public void convertsListQuery() throws Exception {
		PropertyQuery query = PropertyQueryBuilder.forProperty(Company.T, 1L, "persons").done();

		SelectQuery selectQuery = QueryConverter.convertPropertyQuery(query);

		SimpleOrdering ordering = (SimpleOrdering) selectQuery.getOrdering();

		BtAssertions.assertThat(ordering).isNotNull();
		BtAssertions.assertThat(ordering.getOrderBy()).isInstanceOf(ListIndex.class);
	}

}
