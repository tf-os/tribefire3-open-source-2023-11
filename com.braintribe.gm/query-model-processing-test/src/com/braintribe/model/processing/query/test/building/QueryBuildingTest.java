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
package com.braintribe.model.processing.query.test.building;

import org.junit.Test;

import com.braintribe.model.processing.query.building.EntityQueries;
import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.query.CascadedOrdering;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.Ordering;
import com.braintribe.model.query.OrderingDirection;
import com.braintribe.testing.junit.assertions.assertj.core.api.Assertions;

public class QueryBuildingTest {

	@Test
	public void testMultiOrdering() {
		EntityQuery query = TestEntityQueries.multiOrdering();

		Ordering ordering = query.getOrdering();

		Assertions.assertThat(ordering).isInstanceOf(CascadedOrdering.class);

		CascadedOrdering cascadedOrdering = (CascadedOrdering) ordering;
		Assertions.assertThat(cascadedOrdering.getOrderings().size()).isEqualTo(2);
	}

	private static class TestEntityQueries extends EntityQueries {
		public static EntityQuery multiOrdering() {
			return from(Person.T) //
					.orderBy(OrderingDirection.ascending, property("indexedName")) //
					.orderBy(OrderingDirection.ascending, property("companyName"));
		}
	}

}
