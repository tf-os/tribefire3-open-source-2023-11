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
package com.braintribe.model.processing.query.test.stringifier;

import org.junit.Test;

import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.testing.junit.assertions.assertj.core.api.Assertions;

/**
 *
 */
public class AggregatedProjectionQueryTests extends AbstractSelectQueryTests {

	@Test
	public void simpleCount() {
		// @formatter:off
		 SelectQuery selectQuery = query()
				.from(Person.class, "_Person")
				.select().count("_Person", "age")
				.select("_Person", "name")
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery);
		Assertions.assertThat(queryString).isEqualToIgnoringCase(
				"select count(_Person.age), _Person.name from com.braintribe.model.processing.query.test.model.Person _Person");

		// ParsedQuery parsedQuery = QueryParser.parse(queryString);
		// Assert.assertTrue(parsedQuery.getErrorList().isEmpty());
		//
		// String queryString2 = QueryStringifier.stringify(parsedQuery.getQuery());
		// Assert.assertTrue(queryString2.equalsIgnoreCase(queryString));
	}

	@Test
	public void simpleSum() {
		// @formatter:off
		 SelectQuery selectQuery = query()
				.from(Person.class, "_Person")
				.select().sum("_Person", "age")
				.select("_Person", "name")
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery);
		Assertions.assertThat(queryString)
				.isEqualToIgnoringCase("select sum(_Person.age), _Person.name from com.braintribe.model.processing.query.test.model.Person _Person");
	}

	@Test
	public void simpleMin() {
		// @formatter:off
		 SelectQuery selectQuery = query()
				.from(Person.class, "_Person")
				.select().min("_Person", "age")
				.select("_Person", "name")
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery);
		Assertions.assertThat(queryString)
				.isEqualToIgnoringCase("select min(_Person.age), _Person.name from com.braintribe.model.processing.query.test.model.Person _Person");
	}

	@Test
	public void simpleMax() {
		// @formatter:off
		 SelectQuery selectQuery = query()
				.from(Person.class, "_Person")
				.select().max("_Person", "age")
				.select("_Person", "name")
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery);
		Assertions.assertThat(queryString)
				.isEqualToIgnoringCase("select max(_Person.age), _Person.name from com.braintribe.model.processing.query.test.model.Person _Person");
	}

	@Test
	public void simpleAvg() {
		// @formatter:off
		 SelectQuery selectQuery = query()
				.from(Person.class, "_Person")
				.select().avg("_Person", "age")
				.select("_Person", "name")
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery);
		Assertions.assertThat(queryString)
				.isEqualToIgnoringCase("select avg(_Person.age), _Person.name from com.braintribe.model.processing.query.test.model.Person _Person");
	}

	@Test
	public void explicitNonSelectedGroupBy() {
		// @formatter:off
		 SelectQuery selectQuery = query()
				.from(Person.class, "_Person")
				.select().count("_Person", "age")
				.groupBy("_Person", "name")
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery);
		Assertions.assertThat(queryString).isEqualToIgnoringCase(
				"select count(_Person.age) from com.braintribe.model.processing.query.test.model.Person _Person group by _Person.name");
	}

	@Test
	public void explicitGroupBy() {
		// @formatter:off
		 SelectQuery selectQuery = query()
				.from(Person.class, "_Person")
				.select().count("_Person", "age")
				.select("_Person", "name")
				.groupBy("_Person", "name")
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery);
		Assertions.assertThat(queryString).isEqualToIgnoringCase(
				"select count(_Person.age), _Person.name from com.braintribe.model.processing.query.test.model.Person _Person group by _Person.name");
	}

	@Test
	public void explicitGroupByWithAFunction() {
		// @formatter:off
		 SelectQuery selectQuery = query()
				.from(Person.class, "_Person")
				.select()
				.count("_Person", "id")
				.select()
				.entitySignature().entity("_Person")
				.groupBy()
				.entitySignature().entity("_Person")
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery);
		Assertions.assertThat(queryString).isEqualToIgnoringCase(
				"select count(_Person.id), typeSignature(_Person) from com.braintribe.model.processing.query.test.model.Person _Person group by typeSignature(_Person)");
	}
}
