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

import com.braintribe.model.processing.query.test.CollectionRelatedTests;
import com.braintribe.model.processing.query.test.model.Color;
import com.braintribe.model.processing.query.test.model.Company;
import com.braintribe.model.processing.query.test.model.Owner;
import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.query.SelectQuery;

/**
 * 
 */
public class CollectionRelatedQueryTests extends AbstractSelectQueryTests {

	/** @see CollectionRelatedTests#selectingCollection_Directly() */
	@Test
	public void selectingEmptyCollection_Directly() {
		b.person("robert").create();
		b.person("william").create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.select("n")
				.from(Person.T, "person")
					.join("person", "nicknames", "n")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertNoMoreResults();
	}

	@Test
	public void selectingEmptyCollection_Directly_LeftJoin() {
		b.person("robert").create();
		b.person("william").create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.select("person", "nicknames")
				.from(Person.T, "person")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(null);
		assertResultContains(null);
		assertNoMoreResults();
	}

	/** @see CollectionRelatedTests#selectingCollection_Directly() */
	@Test
	public void selectingCollection_Directly() {
		b.person("robert").nicknames("rob", "bob").create();
		b.person("william").nicknames("will", "bill").create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.select("person", "nicknames")
				.from(Person.T, "person")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains("rob");
		assertResultContains("bob");
		assertResultContains("will");
		assertResultContains("bill");
		assertNoMoreResults();
	}

	/** @see CollectionRelatedTests#selectingCollection_ByJoining() */
	@Test
	public void selectingCollection_ByJoining() {
		b.person("robert").nicknames("rob", "bob").create();
		b.person("william").nicknames("will", "bill").create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.select("n")
				.from(Person.T, "p")
					.join("p", "nicknames", "n")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains("rob");
		assertResultContains("bob");
		assertResultContains("will");
		assertResultContains("bill");
		assertNoMoreResults();
	}

	/** @see CollectionRelatedTests#selectingCollection_DirectlyAndByJoining() */
	@Test
	public void selectingCollection_DirectlyAndByJoining() {
		b.person("robert").nicknames("rob", "bob").create();
		b.person("william").nicknames("will", "bill").create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.select("p", "nicknames")
				.from(Person.T, "p")
					.join("p", "nicknames", "n")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains("rob");
		assertResultContains("bob");
		assertResultContains("will");
		assertResultContains("bill");

		assertResultContains("rob");
		assertResultContains("bob");
		assertResultContains("will");
		assertResultContains("bill");

		assertNoMoreResults();
	}

	private void assertContainsAllCombinations(String[] nicks) {
		for (String n1 : nicks) {
			for (String n2 : nicks) {
				assertResultContains(n1, n2);
			}
		}
	}

	/** @see CollectionRelatedTests#selectingCollection_DirectlyTwiceCausingCartesianProduct() */
	@Test
	public void selectingCollection_DirectlyTwiceCausingCartesianProduct() {
		b.person("robert").nicknames("rob", "bob").create();
		b.person("william").nicknames("will", "bill").create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.select("p", "nicknames")
				.select("p", "nicknames")
				.from(Person.T, "p")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertContainsAllCombinations(new String[] { "rob", "bob" });
		assertContainsAllCombinations(new String[] { "will", "bill" });
		assertNoMoreResults();
	}

	/** @see CollectionRelatedTests#selectingCollectionUsingInCondition() */
	@Test
	public void selectingCollectionUsingInCondition() {
		b.person("robert").nicknames("rob", "bob").create();
		b.person("william").nicknames("will", "bill").create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
					.select("p", "name")
					.where()
						.value("bob").in().property("p", "nicknames")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains("robert");
		assertNoMoreResults();
	}

	@Test
	public void queryEntitiesInCondition_WithEnums() {
		Company c = b.company("C1").colors(Color.BLUE).create();

		// @formatter:off
		SelectQuery selectQuery = query()
					.select("c")
					.from(Company.T, "c")
					.where()
						.value(Color.BLUE).in().property("c", "colors")
					.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(c);
		assertNoMoreResults();
	}

	/** @see CollectionRelatedTests#joiningWithConditionOnMapKey() */
	@Test
	public void joiningWithConditionOnMapKey() {
		/* ignored = */ b.company("Not Part Of Result").create();
		Company cheap = b.company("Cheap Company").create();
		Company expensive = b.company("Expensive Company").create();

		b.owner("robert").addToCompanyValueMap(cheap, 10).addToCompanyValueMap(expensive, 1000).create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Owner.T, "o")
					.join("o", "companyValueMap", "map")
				.from(Company.class, "c")
				.select("c", "name")
				.where()
					.mapKey("map").eq().entity("c")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(cheap.getName());
		assertResultContains(expensive.getName());
		assertNoMoreResults();
	}

}
