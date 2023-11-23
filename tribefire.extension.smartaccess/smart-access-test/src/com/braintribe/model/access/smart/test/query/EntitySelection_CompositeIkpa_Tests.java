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
import com.braintribe.model.processing.query.smart.test.model.smart.SmartPersonA;
import com.braintribe.model.processing.smart.query.planner.EntitySelection_CompositeIkpa_PlannerTests;
import com.braintribe.model.query.SelectQuery;

/**
 * @see EntitySelection_CompositeIkpa_PlannerTests
 */
public class EntitySelection_CompositeIkpa_Tests extends AbstractSmartQueryTests {

	/** @see EntitySelection_CompositeIkpa_PlannerTests#selectCompositeIkpaEntity() */
	@Test
	public void selectCompositeIkpaEntity() {
		PersonA p1 = bA.personA("p1").create();
		PersonA p2 = bA.personA("p2").create();
		PersonA p3 = bA.personA("p3").create();

		CompositeIkpaEntityA c1 = bA.compositeIkpaEntityA().personData(p1).description("d1").create();
		CompositeIkpaEntityA c2 = bA.compositeIkpaEntityA().personData(p2).description("d2").create();
		CompositeIkpaEntityA c3 = bA.compositeIkpaEntityA().personData(p3).description("d3").create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.class, "p")
				.select("p", "nameA")
				.select("p", "compositeIkpaEntity")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains("p1", smartCompositeIkpa(c1));
		assertResultContains("p2", smartCompositeIkpa(c2));
		assertResultContains("p3", smartCompositeIkpa(c3));
		assertNoMoreResults();
	}

	/** @see EntitySelection_CompositeIkpa_PlannerTests#conditionOnCompositeIkpaEntity_ExternalDqj() */
	@Test
	public void conditionOnCompositeIkpaEntity() {
		PersonA p1 = bA.personA("p1").create();
		PersonA p2 = bA.personA("p2").create();
		PersonA p3 = bA.personA("p3").create();

		CompositeIkpaEntityA c;
		c = bA.compositeIkpaEntityA().personData(p1).description("d1").create();
		c = bA.compositeIkpaEntityA().personData(p2).description("d2").create();
		c = bA.compositeIkpaEntityA().personData(p3).description("d3").create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.class, "p")
				.select("p", "nameA")
				.where()
					.property("p", "compositeIkpaEntity").eq().entity(smartCompositeIkpa(c))
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains("p3");
		assertNoMoreResults();
	}

}
