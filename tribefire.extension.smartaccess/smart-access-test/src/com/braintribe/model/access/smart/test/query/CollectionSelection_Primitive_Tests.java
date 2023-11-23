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
package com.braintribe.model.access.smart.test.query;

import org.junit.Test;

import com.braintribe.model.processing.query.smart.test.model.smart.SmartPersonA;
import com.braintribe.model.processing.smart.query.planner.CollectionSelection_Primitive_PlannerTests;
import com.braintribe.model.query.JoinType;
import com.braintribe.model.query.SelectQuery;

/**
 * 
 */
public class CollectionSelection_Primitive_Tests extends AbstractSmartQueryTests {

	/** @see CollectionSelection_Primitive_PlannerTests#simpleSetQuery() */
	@Test
	public void simpleEmptySetQuery() {
		bA.personA("p").create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.class, "p")
				.select("p", "nickNamesSetA")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(null);
		assertNoMoreResults();
	}

	/** @see CollectionSelection_Primitive_PlannerTests#simpleSetQuery() */
	@Test
	public void innerJoinEmptySetQuery() {
		bA.personA("a").create();
		bA.personA("b").create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.class, "p")
				.join("p", "nickNamesSetA", "n", JoinType.inner)
				.select("n")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertNoMoreResults();
	}

	/** @see CollectionSelection_Primitive_PlannerTests#simpleSetQuery() */
	@Test
	public void simpleSetQuery() {
		bA.personA("p").nickNamesA("p", "pp", "ppp").create();
		bA.personA("q").nickNamesA("q", "qq", "qqq").create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.class, "p")
				.select("p", "nickNamesSetA")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains("p");
		assertResultContains("pp");
		assertResultContains("ppp");
		assertResultContains("q");
		assertResultContains("qq");
		assertResultContains("qqq");
		assertNoMoreResults();
	}

	@Test
	public void collectionIsLeftJoined() {
		bA.personA("p").create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.class, "p")
				.select("p", "nameA")
				.select("p", "nickNamesSetA")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains("p", (Object) null);
		assertNoMoreResults();
	}

	@Test
	public void collectionIsInnerJoinedExplicitly() {
		bA.personA("p").create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.class, "p")
				.join("p", "nickNamesSetA", "n", JoinType.inner)
				.select("p", "nameA")
				.select("n")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertNoMoreResults();
	}

	/** @see CollectionSelection_Primitive_PlannerTests#simpleListWithIndexQuery() */
	@Test
	public void simpleListWithIndexQuery() {
		bA.personA("p").nickNamesA("p", "pp", "ppp").create();
		bA.personA("q").nickNamesA("q", "qq", "qqq").create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.class, "p")
				.join("p", "nickNamesListA", "n")
					.select().listIndex("n")
					.select("n")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(0, "p");
		assertResultContains(1, "pp");
		assertResultContains(2, "ppp");
		assertResultContains(0, "q");
		assertResultContains(1, "qq");
		assertResultContains(2, "qqq");
		assertNoMoreResults();
	}

	/** @see CollectionSelection_Primitive_PlannerTests#simpleMapWithKeyQuery() */
	@Test
	public void simpleMapWithKeyQuery() {
		bA.personA("p").nickNamesA("p", "pp", "ppp").create();
		bA.personA("q").nickNamesA("q", "qq", "qqq").create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.class, "p")
				.join("p", "nickNamesMapA", "n")
					.select().mapKey("n")
					.select("n")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		// see builder on how the nickNamesMapA is constructed
		assertResultContains(2, "p");
		assertResultContains(4, "pp");
		assertResultContains(6, "ppp");
		assertResultContains(2, "q");
		assertResultContains(4, "qq");
		assertResultContains(6, "qqq");
		assertNoMoreResults();
	}
}
