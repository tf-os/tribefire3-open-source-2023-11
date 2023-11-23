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
import com.braintribe.model.processing.smart.query.planner.EntitySelection_LinkProperty_PlannerTests;
import com.braintribe.model.query.SelectQuery;

/**
 * @see EntitySelection_LinkProperty_PlannerTests
 */
public class EntitySelection_LinkProperty_Tests extends AbstractSmartQueryTests {

	private ItemB i1, i2, i3;
	private PersonA p1, p2, p3;
	private SmartItem si1, si2, si3;

	/** @see EntitySelection_LinkProperty_PlannerTests#simpleEntityQuery() */
	@Test
	public void simpleEntityQuery() {
		prepareData();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.class, "p")
				.select("p", "linkItem")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(si1);
		assertResultContains(si2);
		assertResultContains(si3);
		assertResultContains(null);
		assertNoMoreResults();
	}

	/** @see EntitySelection_LinkProperty_PlannerTests#queryWithDelegatableEntityCondition() */
	@Test
	public void queryWithDelegatableEntityCondition() {
		prepareData();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.class, "p")
				.select("p", "nameA")
				.where()
					.property("p", "linkItem").eq().entity(si2)
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(p2.getNameA());
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

		bB.personItemLink(p1, i1);
		bB.personItemLink(p2, i2);
		bB.personItemLink(p3, i3);

		// #######################################
		// ## . . . . Testing left join . . . . ##
		// #######################################

		/* This is the entity for which we want the resolved link item to be null */
		bA.personA("noValuePerson").create();

		/* This tests the problem that if we create dqj condition with null value (from ItemB i where i.nameB = null), this entity will be
		 * returned. But in such case we want to omit the condition and simply let it be null due to being a left join. */
		bB.item(null).create();
	}

}
