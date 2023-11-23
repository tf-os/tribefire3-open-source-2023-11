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

import com.braintribe.model.processing.query.smart.test.model.accessA.PersonA;
import com.braintribe.model.processing.query.smart.test.model.accessB.ItemB;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartItem;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartPersonA;
import com.braintribe.model.processing.smart.query.planner.CollectionSelection_LinkProperty_PlannerTests;
import com.braintribe.model.query.SelectQuery;

/**
 * @see CollectionSelection_LinkProperty_PlannerTests
 */
public class CollectionSelection_LinkProperty_Tests extends AbstractSmartQueryTests {

	private ItemB i1, i2, i3;
	private PersonA p1, p2, p3;
	private SmartItem si1, si2, si3;

	/** @see CollectionSelection_LinkProperty_PlannerTests#simpleSetQuery() */
	@Test
	public void simpleSetQuery() {
		prepareData();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.class, "p")
				.select("p", "linkItems")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(si1);
		assertResultContains(si2);
		assertResultContains(si2);
		assertResultContains(si3);
		assertResultContains(si3);
		assertResultContains(si3);
		assertNoMoreResults();
	}

	/** @see CollectionSelection_LinkProperty_PlannerTests#queryWithDelegatableSetCondition() */
	@Test
	public void queryWithDelegatableSetCondition() {
		prepareData();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.class, "p")
				.select("p", "nameA")
				.where()
					.entity(si2).in().property("p", "linkItems")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(p1.getNameA());
		assertResultContains(p2.getNameA());
		assertNoMoreResults();
	}

	/** @see CollectionSelection_LinkProperty_PlannerTests#setQueryWithDelegatableSetCondition() */
	@Test
	public void setQueryWithDelegatableSetCondition() {
		prepareData();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.class, "p")
				.select("p", "nameA")
				.select("p", "linkItems")
				.where()
					.entity(si2).in().property("p", "linkItems")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(p1.getNameA(), si1);
		assertResultContains(p1.getNameA(), si2);
		assertResultContains(p1.getNameA(), si3);
		assertResultContains(p2.getNameA(), si2);
		assertResultContains(p2.getNameA(), si3);
		assertNoMoreResults();
	}

	/** @see CollectionSelection_LinkProperty_PlannerTests#simpleListQuery() */
	@Test
	public void simpleListQuery() {
		prepareData();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.class, "p")
				.select("p", "orderedLinkItems")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(si1);
		assertResultContains(si2);
		assertResultContains(si2);
		assertResultContains(si3);
		assertResultContains(si3);
		assertResultContains(si3);
		assertNoMoreResults();
	}

	/** @see CollectionSelection_LinkProperty_PlannerTests#listQueryWithListIndex() */
	@Test
	public void listQueryWithListIndex() {
		prepareData();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.class, "p")
					.join("p", "orderedLinkItems", "i")
				.select().listIndex("i")
				.select("i")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(0, si1);
		assertResultContains(0, si2);
		assertResultContains(1, si2);
		assertResultContains(0, si3);
		assertResultContains(1, si3);
		assertResultContains(2, si3);
		assertNoMoreResults();
	}

	/** @see CollectionSelection_LinkProperty_PlannerTests#listQueryWithListIndexCondition() */
	@Test
	public void listQueryWithListIndexCondition() {
		prepareData();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.class, "p")
					.join("p", "orderedLinkItems", "i")
				.select().listIndex("i")
				.select("i")
				.where()
					.listIndex("i").ge(1)
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(1, si2);
		assertResultContains(1, si3);
		assertResultContains(2, si3);
		assertNoMoreResults();
	}

	/** @see CollectionSelection_LinkProperty_PlannerTests#listQueryWithListIndexCondition_NonDelegateable() */
	@Test
	public void listQueryWithListIndexCondition_NonDelegateable() {
		prepareData();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.class, "p")
					.join("p", "orderedLinkItems", "i")
				.select().listIndex("i")
				.select("i")
				.where()
					.disjunction()
						.listIndex("i").ge(1)
						.property("p", "nameA").like("*3")
					.close()
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(1, si2); // p1: index >= 1
		assertResultContains(2, si3); // p1: index >= 1
		assertResultContains(1, si3); // p2: index >= 1
		assertResultContains(0, si3); // p3: nameA like '*3'
		assertNoMoreResults();
	}

	/**
	 * Each of the three persons contains items with numerical value greater or equal than their own.
	 */
	private void prepareData() {
		i1 = bB.item("i1").create();
		i2 = bB.item("i2").create();
		i3 = bB.item("i3").create();

		p1 = bA.personA("p1").create();
		p2 = bA.personA("p2").create();
		p3 = bA.personA("p3").create();

		si1 = smartItem(i1);
		si2 = smartItem(i2);
		si3 = smartItem(i3);

		// set
		bB.personItemSetLink(p1, i1);
		bB.personItemSetLink(p1, i2);
		bB.personItemSetLink(p1, i3);

		bB.personItemSetLink(p2, i2);
		bB.personItemSetLink(p2, i3);

		bB.personItemSetLink(p3, i3);

		// list
		bB.personItemOrderedLink(p1, i1, 0);
		bB.personItemOrderedLink(p1, i2, 1);
		bB.personItemOrderedLink(p1, i3, 2);

		bB.personItemOrderedLink(p2, i2, 0);
		bB.personItemOrderedLink(p2, i3, 1);

		bB.personItemOrderedLink(p3, i3, 0);
	}

}
