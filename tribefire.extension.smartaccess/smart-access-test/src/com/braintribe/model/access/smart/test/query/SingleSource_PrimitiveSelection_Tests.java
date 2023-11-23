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

import com.braintribe.model.processing.query.smart.test.model.accessB.ItemB;
import com.braintribe.model.processing.query.smart.test.model.accessB.ItemTypeB;
import com.braintribe.model.processing.query.smart.test.model.smart.ItemType;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartItem;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartPersonA;
import com.braintribe.model.processing.smart.query.planner.SingleSource_PrimitiveSelection_PlannerTests;
import com.braintribe.model.query.SelectQuery;

/**
 * 
 */
public class SingleSource_PrimitiveSelection_Tests extends AbstractSmartQueryTests {

	/** @see SingleSource_PrimitiveSelection_PlannerTests#simpleQuery() */
	@Test
	public void simpleQuery() {
		bA.personA("p1").create();
		bA.personA("p2").create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.class, "p")
				.select("p", "nameA")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains("p1");
		assertResultContains("p2");
		assertNoMoreResults();
	}

	/** @see SingleSource_PrimitiveSelection_PlannerTests#simpleQuery_Enum() */
	@Test
	public void simpleQuery_Enum() {
		bB.item("i1").type(ItemTypeB.ELECTRONICS_B).create();
		bB.item("i2").type(ItemTypeB.TOOL_B).create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartItem.class, "i")
				.select("i", "itemType")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(ItemType.ELECTRONICS);
		assertResultContains(ItemType.TOOL);
		assertNoMoreResults();
	}

	@Test
	public void simpleQuery_EnumCollection() {
		bB.item("i1").types(ItemTypeB.ELECTRONICS_B, ItemTypeB.TOOL_B).create();
		bB.item("i2").types(ItemTypeB.MAGIC_B, ItemTypeB.INVISIBLE_B).create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartItem.class, "i")
				.select("i", "itemTypes")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(ItemType.ELECTRONICS);
		assertResultContains(ItemType.TOOL);
		assertResultContains(ItemType.MAGIC);
		assertResultContains(ItemType.INVISIBLE);
		assertNoMoreResults();
	}

	/** @see SingleSource_PrimitiveSelection_PlannerTests#simpleQuery_Enum() */
	@Test
	public void simpleQuery_EntityWithEnum() {
		ItemB i1 = bB.item("i1").type(ItemTypeB.ELECTRONICS_B).create();
		ItemB i2 = bB.item("i2").type(ItemTypeB.TOOL_B).create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartItem.class, "i")
				.select("i")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(smartItem(i1));
		assertResultContains(smartItem(i2));
		assertNoMoreResults();
	}

	/** @see SingleSource_PrimitiveSelection_PlannerTests#simpleDqjQuery() */
	@Test
	public void simpleDqjQuery() {
		bA.personA("pa1").parentB("pb1").create();
		bA.personA("pa2").parentB("pb2").create();

		bB.personB("pb1").companyNameB("c1").create();
		bB.personB("pb2").companyNameB("c2").create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.class, "p")
				.select("p", "nameA")
				.select("p", "smartParentB.companyNameB")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains("pa1", "c1");
		assertResultContains("pa2", "c2");
		assertNoMoreResults();
	}

	/** @see SingleSource_PrimitiveSelection_PlannerTests#simpleQueryWithCondition() */
	@Test
	public void simpleQueryWithCondition() {
		bA.personA("nameA1").create();
		bA.personA("nameA2").create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.class, "p")
				.select("p", "nameA")
				.where()
					.property("p", "nameA").eq("nameA1")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains("nameA1");
		assertNoMoreResults();
	}

	/** @see SingleSource_PrimitiveSelection_PlannerTests#simpleQueryWithConditionNonSelectedProps() */
	@Test
	public void simpleQueryWithConditionNonSelectedProps() {
		bA.personA("nameA1").companyNameA("companyNameA1").create();
		bA.personA("nameA2").companyNameA("companyNameA2").create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.class, "p")
				.select("p", "nameA")
				.where()
					.property("p", "companyNameA").eq("companyNameA1")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains("nameA1");
		assertNoMoreResults();
	}

	/** @see SingleSource_PrimitiveSelection_PlannerTests#selectUnmappedProperty_Simple() */
	@Test
	public void selectUnmappedProperty() {
		bA.personA("p1").create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.class, "p")
				.select("p", "nameA")
				.select("p", "unmappedString")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains("p1", (Object) null);
		assertNoMoreResults();
	}

}
