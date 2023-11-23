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

import com.braintribe.model.processing.query.smart.test.model.accessA.CompositeKpaEntityA;
import com.braintribe.model.processing.query.smart.test.model.accessA.PersonA;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartPersonA;
import com.braintribe.model.processing.smart.query.planner.EntitySelection_CompositeKpa_PlannerTests;
import com.braintribe.model.query.SelectQuery;

/**
 * @see EntitySelection_CompositeKpa_PlannerTests
 */
public class EntitySelection_CompositeKpa_Tests extends AbstractSmartQueryTests {

	/** @see EntitySelection_CompositeKpa_PlannerTests#selectCompositeKpaEntity() */
	@Test
	public void selectCompositeKpaEntity() {
		PersonA p1 = bA.personA("p1").compositeId(1L).compositeName("pp1").compositeCompanyName("c1").create();
		PersonA p2 = bA.personA("p2").compositeId(2L).compositeName("pp2").compositeCompanyName("c2").create();
		PersonA p3 = bA.personA("p3").compositeId(3L).compositeName("pp3").compositeCompanyName("c3").create();

		CompositeKpaEntityA c1 = bA.compositeKpaEntityA().personData(p1).description("d1").create();
		CompositeKpaEntityA c2 = bA.compositeKpaEntityA().personData(p2).description("d2").create();
		CompositeKpaEntityA c3 = bA.compositeKpaEntityA().personData(p3).description("d3").create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.class, "p")
				.select("p", "nameA")
				.select("p", "compositeKpaEntity")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains("p1", smartCompositeKpa(c1));
		assertResultContains("p2", smartCompositeKpa(c2));
		assertResultContains("p3", smartCompositeKpa(c3));
		assertNoMoreResults();
	}

	/** @see EntitySelection_CompositeKpa_PlannerTests#conditionOnCompositeKpaEntity_ExternalDqj() */
	@Test
	public void conditionOnCompositeKpaEntity() {
		PersonA p1 = bA.personA("p1").compositeId(1L).compositeName("pp1").compositeCompanyName("c1").create();
		PersonA p2 = bA.personA("p2").compositeId(2L).compositeName("pp2").compositeCompanyName("c2").create();
		PersonA p3 = bA.personA("p3").compositeId(3L).compositeName("pp3").compositeCompanyName("c3").create();

		CompositeKpaEntityA c;
		c = bA.compositeKpaEntityA().personData(p1).description("d1").create();
		c = bA.compositeKpaEntityA().personData(p2).description("d2").create();
		c = bA.compositeKpaEntityA().personData(p3).description("d3").create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.class, "p")
				.select("p", "nameA")
				.where()
					.property("p", "compositeKpaEntity").eq().entity(smartCompositeKpa(c))
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains("p3");
		assertNoMoreResults();
	}

}
