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

import com.braintribe.model.processing.query.test.model.Owner;
import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.query.SelectQuery;

/**
 * 
 */
public class AggregationQueryTests extends AbstractSelectQueryTests {

	@Test
	public void simpleCount() {
		b.person("p1").age(10).create();
		b.person("p1").age(20).create();
		b.person("p2").age(30).create();
		b.person("p2").age(40).create();
		b.person("p2").age(50).create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.select().count("p", "age")
				.select("p", "name")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(2L, "p1");
		assertResultContains(3L, "p2");
		assertNoMoreResults();
	}

	@Test
	public void simpleSum() {
		b.person("p1").age(10).create();
		b.person("p1").age(20).create();
		b.person("p2").age(30).create();
		b.person("p2").age(40).create();
		b.person("p2").age(50).create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.select().sum("p", "age")
				.select("p", "name")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(30, "p1");
		assertResultContains(120, "p2");
		assertNoMoreResults();
	}

	@Test
	public void simpleMin() {
		b.person("p1").age(10).create();
		b.person("p1").age(20).create();
		b.person("p2").age(30).create();
		b.person("p2").age(40).create();
		b.person("p2").age(50).create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.select().min("p", "age")
				.select("p", "name")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(10, "p1");
		assertResultContains(30, "p2");
		assertNoMoreResults();
	}

	@Test
	public void simpleMax() {
		b.person("p1").age(10).create();
		b.person("p1").age(20).create();
		b.person("p2").age(30).create();
		b.person("p2").age(40).create();
		b.person("p2").age(50).create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.select().max("p", "age")
				.select("p", "name")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(20, "p1");
		assertResultContains(50, "p2");
		assertNoMoreResults();
	}

	@Test
	public void simpleAvg() {
		b.person("p1").age(10).create();
		b.person("p1").age(20).create();
		b.person("p2").age(30).create();
		b.person("p2").age(40).create();
		b.person("p2").age(50).create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.select().avg("p", "age")
				.select("p", "name")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(15, "p1");
		assertResultContains(40, "p2");
		assertNoMoreResults();
	}

	@Test
	public void explicitNonSelectedGroupBy() {
		b.person("p1").age(10).create();
		b.person("p1").age(20).create();
		b.person("p2").age(30).create();
		b.person("p2").age(40).create();
		b.person("p2").age(50).create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.select().count("p", "age")
				.groupBy("p", "name")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(2L);
		assertResultContains(3L);
		assertNoMoreResults();
	}

	@Test
	public void explicitGroupBy() {
		b.person("p1").age(10).create();
		b.person("p1").age(20).create();
		b.person("p2").age(30).create();
		b.person("p2").age(40).create();
		b.person("p2").age(50).create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.select().count("p", "age")
				.select("p", "name")
				.groupBy("p", "name")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(2L, "p1");
		assertResultContains(3L, "p2");
		assertNoMoreResults();
	}

	@Test
	public void explicitGroupByWithAFunction() {
		b.owner("o1").create();
		b.owner("o2").create();
		b.person("p1").create();
		b.person("p2").create();
		b.person("p3").create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.select()
					.count("p", "id")
				.select()
					.entitySignature().entity("p")
				.groupBy()
					.entitySignature().entity("p")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(2L, Owner.class.getName());
		assertResultContains(3L, Person.class.getName());
		assertNoMoreResults();
	}

}
