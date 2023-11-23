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

import com.braintribe.model.processing.query.smart.test.model.accessA.CompositeIkpaEntityA;
import com.braintribe.model.processing.query.smart.test.model.accessA.PersonA;
import com.braintribe.model.processing.query.smart.test.model.accessB.ItemB;
import com.braintribe.model.processing.query.smart.test.model.smart.CompositeIkpaEntity;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartItem;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartPersonA;
import com.braintribe.model.processing.smart.query.planner.CollectionSelection_InverseKeyProperty_PlannerTests;
import com.braintribe.model.query.SelectQuery;

/**
 * 
 */
public class CollectionSelection_InverseKeyProperty_Tests extends AbstractSmartQueryTests {

	// ##########################################################################
	// ## . . . . . Delegate -> Simple ; Smart -> Set<GenericEntity> . . . . . ##
	// ##########################################################################

	/** @see CollectionSelection_InverseKeyProperty_PlannerTests#simpleSetQuery() */
	@Test
	public void simpleSetQuery() {
		PersonA p1 = bA.personA("person1").create();
		PersonA p2 = bA.personA("person2").create();

		ItemB it1 = bB.item("hammer").multiOwnerName("person1").create();
		ItemB it2 = bB.item("saw").multiOwnerName("person1").create();
		ItemB it3 = bB.item("shield").multiOwnerName("person2").create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.class, "p")
				.select("p", "nameA")
				.select("p", "inverseKeyItemSet")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(p1.getNameA(), smartItem(it1));
		assertResultContains(p1.getNameA(), smartItem(it2));
		assertResultContains(p2.getNameA(), smartItem(it3));
		assertNoMoreResults();
	}

	/** @see CollectionSelection_InverseKeyProperty_PlannerTests#simpleSetQuery_Composite() */
	@Test
	public void simpleSetQuery_Composite() {
		PersonA p1 = bA.personA("person1").create();
		PersonA p2 = bA.personA("person2").create();

		CompositeIkpaEntityA c1 = bA.compositeIkpaEntityA().personData_Set(p1).create();
		CompositeIkpaEntityA c2 = bA.compositeIkpaEntityA().personData_Set(p1).create();
		CompositeIkpaEntityA c3 = bA.compositeIkpaEntityA().personData_Set(p2).create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.class, "p")
				.select("p", "nameA")
				.select("p", "compositeIkpaEntitySet")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(p1.getNameA(), smartCompositeIkpa(c1));
		assertResultContains(p1.getNameA(), smartCompositeIkpa(c2));
		assertResultContains(p2.getNameA(), smartCompositeIkpa(c3));
		assertNoMoreResults();
	}

	/** @see CollectionSelection_InverseKeyProperty_PlannerTests#simpleSetQuery_Composite_ExternalDqj() */
	@Test
	public void simpleSetQuery_Composite_ExternalDqj() {
		PersonA p1 = bA.personA("person1").create();
		PersonA p2 = bA.personA("person2").create();

		CompositeIkpaEntityA c1 = bA.compositeIkpaEntityA().personData_Set(p1).create();
		CompositeIkpaEntityA c2 = bA.compositeIkpaEntityA().personData_Set(p1).create();
		CompositeIkpaEntityA c3 = bA.compositeIkpaEntityA().personData_Set(p2).create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.class, "p")
				.select("p", "nameA")
				.select("p", "compositeIkpaEntitySetExternalDqj")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(p1.getNameA(), smartCompositeIkpa(c1));
		assertResultContains(p1.getNameA(), smartCompositeIkpa(c2));
		assertResultContains(p2.getNameA(), smartCompositeIkpa(c3));
		assertNoMoreResults();
	}
	
	/** @see CollectionSelection_InverseKeyProperty_PlannerTests#queryWithSetCondition() */
	@Test
	public void queryWithSetCondition() {
		PersonA p;
		p = bA.personA("person1").create();
		p = bA.personA("person2").create();

		ItemB it;
		it = bB.item("hammer").multiOwnerName("person1").create();
		it = bB.item("saw").multiOwnerName("person2").create();
		it = bB.item("shield").multiOwnerName("person2").create();

		SmartPersonA sp = smartPerson(p);
		SmartItem si = smartItem(it);

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.class, "p")
				.select("p")
				.where()
					.entity(si).in().property("p", "inverseKeyItemSet")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(sp);
		assertNoMoreResults();
	}

	/** @see CollectionSelection_InverseKeyProperty_PlannerTests#queryWithSetCondition_Composite() */
	@Test
	public void queryWithSetCondition_Composite() {
		PersonA p1 = bA.personA("person1").create();
		PersonA p2 = bA.personA("person2").create();

		CompositeIkpaEntityA c;
		c = bA.compositeIkpaEntityA().personData_Set(p1).create();
		c = bA.compositeIkpaEntityA().personData_Set(p1).create();
		c = bA.compositeIkpaEntityA().personData_Set(p2).create();

		SmartPersonA sp2 = smartPerson(p2);
		CompositeIkpaEntity sc = smartCompositeIkpa(c);

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.class, "p")
				.select("p")
				.where()
					.entity(sc).in().property("p", "compositeIkpaEntitySet")
				.done();
		// @formatter:on

		evaluate(selectQuery);
		// @formatter:on

		assertResultContains(sp2);
		assertNoMoreResults();
	}

	/** @see CollectionSelection_InverseKeyProperty_PlannerTests#queryWithSetCondition_Composite_ExternalDqj() */
	@Test
	public void queryWithSetCondition_Composite_ExternalDqj() {
		PersonA p1 = bA.personA("person1").create();
		PersonA p2 = bA.personA("person2").create();

		CompositeIkpaEntityA c;
		c = bA.compositeIkpaEntityA().personData_Set(p1).create();
		c = bA.compositeIkpaEntityA().personData_Set(p1).create();
		c = bA.compositeIkpaEntityA().personData_Set(p2).create();

		SmartPersonA sp2 = smartPerson(p2);
		CompositeIkpaEntity sc = smartCompositeIkpa(c);

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.class, "p")
				.select("p")
				.where()
					.entity(sc).in().property("p", "compositeIkpaEntitySetExternalDqj")
				.done();
		// @formatter:on

		evaluate(selectQuery);
		// @formatter:on

		assertResultContains(sp2);
		assertNoMoreResults();
	}

	/** @see CollectionSelection_InverseKeyProperty_PlannerTests#setQueryWithSetCondition() */
	@Test
	@SuppressWarnings("unused")
	public void setQueryWithSetCondition() {
		PersonA p;
		p = bA.personA("person1").create();
		p = bA.personA("person2").create();

		ItemB it1 = bB.item("hammer").multiOwnerName("person1").create();
		ItemB it2 = bB.item("saw").multiOwnerName("person2").create();
		ItemB it3 = bB.item("shield").multiOwnerName("person2").create();

		SmartPersonA sp = smartPerson(p);

		SmartItem si2 = smartItem(it2);
		SmartItem si3 = smartItem(it3);

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.class, "p")
				.select("p")
				.select("p", "inverseKeyItemSet")
				.where()
					.entity(si3).in().property("p", "inverseKeyItemSet")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(sp, si2);
		assertResultContains(sp, si3);
		assertNoMoreResults();
	}

	// ##########################################################################
	// ## . . . . . Delegate -> Set<Simple> ; Smart -> GenericEntity . . . . . ##
	// ##########################################################################

	/** @see CollectionSelection_InverseKeyProperty_PlannerTests#simpleEntityQuery() */
	@Test
	public void simpleEntityQuery() {
		bA.personA("person1").create();
		bA.personA("person2").create();
		bA.personA("person3").create();

		ItemB it1 = bB.item("hammer").sharedOwnerNames("person1", "person2").create();
		ItemB it2 = bB.item("saw").sharedOwnerNames("person3").create();

		SmartItem si1 = smartItem(it1);
		SmartItem si2 = smartItem(it2);

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.class, "p")
				.select("p", "inverseKeySharedItem")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(si1);
		assertResultContains(si1);
		assertResultContains(si2);
		assertNoMoreResults();
	}

	/** @see CollectionSelection_InverseKeyProperty_PlannerTests#queryWithEntityCondition() */
	@SuppressWarnings("unused")
	@Test
	public void queryWithEntityCondition() {
		PersonA p1 = bA.personA("person1").create();
		PersonA p2 = bA.personA("person2").create();
		PersonA p3 = bA.personA("person3").create();

		ItemB it1 = bB.item("hammer").sharedOwnerNames("person1", "person2").create();
		ItemB it2 = bB.item("saw").sharedOwnerNames("person3").create();

		SmartItem si1 = smartItem(it1);
		SmartItem si2 = smartItem(it2);

		SmartPersonA sp1 = smartPerson(p1);
		SmartPersonA sp2 = smartPerson(p2);

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.class, "p")
				.select("p")
				.where()
					.property("p", "inverseKeySharedItem").eq().entity(si1)
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(sp1);
		assertResultContains(sp2);
		assertNoMoreResults();
	}

	/** @see CollectionSelection_InverseKeyProperty_PlannerTests#queryWithEntityCondition_AndSelectingInverseKeyProperty() */
	@SuppressWarnings("unused")
	@Test
	public void queryWithEntityCondition_AndSelectingInverseKeyProperty() {
		PersonA p1 = bA.personA("person1").create();
		PersonA p2 = bA.personA("person2").create();
		PersonA p3 = bA.personA("person3").create();

		SmartPersonA sp1 = smartPerson(p1);
		SmartPersonA sp2 = smartPerson(p2);

		ItemB it1 = bB.item("hammer").sharedOwnerNames("person1", "person2").create();
		ItemB it2 = bB.item("saw").sharedOwnerNames("person3").create();

		SmartItem si1 = smartItem(it1);
		SmartItem si2 = smartItem(it2);

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.class, "p")
				.select("p")
				.select("p", "inverseKeySharedItem")
				.where()
					.property("p", "inverseKeySharedItem").eq().entity(si1)
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(sp1, si1);
		assertResultContains(sp2, si1);
		assertNoMoreResults();
	}

	// ##########################################################################
	// ## . . . . Delegate -> Set<Simple> ; Smart -> Set<GenericEntity> . . . .##
	// ##########################################################################

	/** @see CollectionSelection_InverseKeyProperty_PlannerTests#simpleInverseSetQuery() */
	@Test
	public void simpleInverseSetQuery() {
		PersonA p1 = bA.personA("person1").create();
		PersonA p2 = bA.personA("person2").create();
		PersonA p3 = bA.personA("person3").create();

		SmartPersonA sp1 = smartPerson(p1);
		SmartPersonA sp2 = smartPerson(p2);
		SmartPersonA sp3 = smartPerson(p3);

		ItemB it1 = bB.item("hammer").multiSharedOwnerNames("person1", "person2").create();
		ItemB it2 = bB.item("saw").multiSharedOwnerNames("person2", "person3").create();
		ItemB it3 = bB.item("shield").multiSharedOwnerNames("person3").create();

		SmartItem si1 = smartItem(it1);
		SmartItem si2 = smartItem(it2);
		SmartItem si3 = smartItem(it3);

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.class, "p")
				.select("p")
				.select("p", "inverseKeyMultiSharedItemSet")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(sp1, si1);
		assertResultContains(sp2, si1);
		assertResultContains(sp2, si2);
		assertResultContains(sp3, si2);
		assertResultContains(sp3, si3);
		assertNoMoreResults();
	}

	/** @see CollectionSelection_InverseKeyProperty_PlannerTests#queryWithInverseSetCondition() */
	@SuppressWarnings("unused")
	@Test
	public void queryWithInverseSetCondition() {
		PersonA p1 = bA.personA("person1").create();
		PersonA p2 = bA.personA("person2").create();
		PersonA p3 = bA.personA("person3").create();

		SmartPersonA sp1 = smartPerson(p1);
		SmartPersonA sp2 = smartPerson(p2);
		SmartPersonA sp3 = smartPerson(p3);

		ItemB it1 = bB.item("hammer").multiSharedOwnerNames("person1", "person2").create();
		ItemB it2 = bB.item("saw").multiSharedOwnerNames("person2", "person3").create();
		ItemB it3 = bB.item("shield").multiSharedOwnerNames("person3").create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.class, "p")
				.select("p")
				.where()
					.entity(smartItem(it2)).in().property("p", "inverseKeyMultiSharedItemSet")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(sp2);
		assertResultContains(sp3);
		assertNoMoreResults();
	}

	/** @see CollectionSelection_InverseKeyProperty_PlannerTests#inverseSetQueryWithInverseSetCondition() */
	@SuppressWarnings("unused")
	@Test
	public void inverseSetQueryWithInverseSetCondition() {
		PersonA p1 = bA.personA("person1").create();
		PersonA p2 = bA.personA("person2").create();
		PersonA p3 = bA.personA("person3").create();

		SmartPersonA sp1 = smartPerson(p1);
		SmartPersonA sp2 = smartPerson(p2);
		SmartPersonA sp3 = smartPerson(p3);

		ItemB it1 = bB.item("hammer").multiSharedOwnerNames("person1", "person2").create();
		ItemB it2 = bB.item("saw").multiSharedOwnerNames("person2", "person3").create();
		ItemB it3 = bB.item("shield").multiSharedOwnerNames("person3").create();

		SmartItem si1 = smartItem(it1);
		SmartItem si2 = smartItem(it2);
		SmartItem si3 = smartItem(it3);

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.class, "p")
				.select("p")
				.select("p", "inverseKeyMultiSharedItemSet")
				.where()
					.entity(si2).in().property("p", "inverseKeyMultiSharedItemSet")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(sp2, si1);
		assertResultContains(sp2, si2);
		assertResultContains(sp3, si2);
		assertResultContains(sp3, si3);
		assertNoMoreResults();
	}
}
