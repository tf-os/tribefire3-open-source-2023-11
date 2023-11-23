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

import com.braintribe.model.processing.query.smart.test.model.accessA.CarA;
import com.braintribe.model.processing.query.smart.test.model.accessA.PersonA;
import com.braintribe.model.processing.query.smart.test.model.smart.Car;
import com.braintribe.model.processing.query.smart.test.model.smart.FlyingCar;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartPersonA;
import com.braintribe.model.processing.smart.query.planner.QueryFunctions_PlannerTests;
import com.braintribe.model.query.SelectQuery;

/**
 * @see QueryFunctions_PlannerTests
 */
public class QueryFunctions_Tests extends AbstractSmartQueryTests {

	// #########################################
	// ## . . . . . EntitySignature . . . . . ##
	// #########################################

	/** @see QueryFunctions_PlannerTests#signature_Final() */
	@Test
	public void signature_Final() {
		bA.personA("p1").create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(SmartPersonA.class, "p")
				.select().entitySignature().entity("p")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(SmartPersonA.class.getName());
		assertNoMoreResults();
	}

	/** @see QueryFunctions_PlannerTests#signature_Hierarchy() */
	@Test
	public void signature_Hierarchy() {
		bA.carA("simple-car").create();
		bA.flyingCarA("flying-car").create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Car.class, "c")
				.select().entitySignature().entity("c")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(Car.class.getName());
		assertResultContains(FlyingCar.class.getName());
		assertNoMoreResults();
	}

	/** @see QueryFunctions_PlannerTests#conditionOnSourceType() */
	@Test
	public void conditionOnSourceType() {
		CarA c;
		c = bA.carA("simple-car").create();
		c = bA.flyingCarA("flying-car").create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Car.class, "c")
				.select().entity("c")
				.where()
					.entitySignature("c").eq(FlyingCar.class.getName())
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(smartFlyingCar(c));
		assertNoMoreResults();
	}

	// @SuppressWarnings("unused")
	// @Test(expected = Exception.class)
	// public void selectingLocalizedValue() {
	// ItemB i1 = bB.item("i1").localizedName("en", "yes", "pt", "sim").create();
	// ItemB i2 = bB.item("i2").localizedName("en", "good", "pt", "bom").create();
	//
//		// @formatter:off
//		SelectQuery selectQuery = query()
//				.from(SmartItem.class, "i")
//				.select("i").select().localize("pt").property("i", "localizedNameB")
//				.done();
//		// @formatter:on
	//
	// evaluate(selectQuery);
	//
	// // Does not work right now, because LocalizedString is not query-able
	//
	// assertResultContains(smartItem(i1), "sim");
	// assertResultContains(smartItem(i2), "bom");
	// }

	/** @see QueryFunctions_PlannerTests#propertyAsString() */
	@Test
	public void propertyAsString() {
		PersonA p = bA.personA("p1").create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.class, "p")
				.select().asString().property("p", "id")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains("" + p.getId());
		assertNoMoreResults();
	}

}
