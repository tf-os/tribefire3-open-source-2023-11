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

import com.braintribe.model.processing.query.test.model.Company;
import com.braintribe.model.processing.query.test.model.Owner;
import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.query.OrderingDirection;
import com.braintribe.model.query.SelectQuery;

/**
 * 
 */
public class DistinctSelectionQueryTests extends AbstractSelectQueryTests {

	@Test
	public void simpleProperty() {
		b.person("p1").create();
		b.person("p1").create();
		b.person("p2").create();
		b.person("p2").create();
		b.person("p2").create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.select("p", "name")
				.from(Person.T, "p")
				.distinct()
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains("p1");
		assertResultContains("p2");
		assertNoMoreResults();
	}

	@Test
	public void simpleProperty_OrderedByOtherProperty() {
		b.person("p1").age(1).create();
		b.person("p1").age(2).create(); // third
		b.person("p2").age(3).create();
		b.person("p2").age(5).create(); // first
		b.person("p3").age(4).create(); // second

		// @formatter:off
		SelectQuery selectQuery = query()
				.select("p", "name")
				.distinct()
				.from(Person.T, "p")
				.orderBy(OrderingDirection.descending).property("p", "age")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertNextResult("p2");
		assertNextResult("p3");
		assertNextResult("p1");
		assertNoMoreResults();
	}

	@Test
	public void listProperty() {
		Company c1 = b.company("C1").create();
		Company c2 = b.company("C2").create();

		b.owner("John").addToCompanyList(c1, c1, c2, c2, c1).create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.select("o", "companyList")
				.from(Owner.T, "o")
				.distinct()
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(c1);
		assertResultContains(c2);
		assertNoMoreResults();
	}

	@Test
	public void setProperty() {
		b.person("p1").nicknames("n1").create();
		b.person("p1").nicknames("n1", "n2").create();
		b.person("p1").nicknames("n2").create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.select("p", "nicknames")
				.from(Person.T, "p")
				.distinct()
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains("n1");
		assertResultContains("n2");
		assertNoMoreResults();
	}

}
