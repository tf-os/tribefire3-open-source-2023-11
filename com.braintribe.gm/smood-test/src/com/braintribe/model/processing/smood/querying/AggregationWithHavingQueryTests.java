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
package com.braintribe.model.processing.smood.querying;

import org.junit.Test;

import com.braintribe.model.processing.query.test.AggregationWithHavingTests;
import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.query.SelectQuery;

/**
 * 
 */
public class AggregationWithHavingQueryTests extends AbstractSelectQueryTests {

	/** @see AggregationWithHavingTests#conditionOnSelected() */
	@Test
	public void conditionOnSelected() {
		b.person("p1").age(10).create();
		b.person("p2").age(20).create();
		b.person("p2").age(30).create();
		b.person("p3").age(40).create();
		b.person("p3").age(50).create();
		b.person("p3").age(60).create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.select("p", "name")
				.select().count("p", "age")
				.from(Person.T, "p")
				.having()
					.count("p", "age").ge(2L)
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains("p3", 3L);
		assertResultContains("p2", 2L);
		assertNoMoreResults();
	}

	/** @see AggregationWithHavingTests#conditionOnNotSelected() */
	@Test
	public void conditionOnNotSelected() {
		b.person("p1").age(10).create();
		b.person("p2").age(20).create();
		b.person("p2").age(30).create();
		b.person("p3").age(40).create();
		b.person("p3").age(50).create();
		b.person("p3").age(60).create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.select("p", "name")
				.select().count("p", "age")
				.from(Person.T, "p")
				.having()
					.sum("p", "age").ge(50)
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains("p3", 3L);
		assertResultContains("p2", 2L);
		assertNoMoreResults();
	}

}
