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

import com.braintribe.model.processing.query.smart.test.model.accessA.special.ManualA;
import com.braintribe.model.processing.query.smart.test.model.accessA.special.ReaderA;
import com.braintribe.model.processing.query.smart.test.model.smart.special.SmartManualA;
import com.braintribe.model.processing.query.smart.test.model.smart.special.SmartReaderA;
import com.braintribe.model.processing.smart.query.planner.UnmappedPropertyType_PlannerTests;
import com.braintribe.model.processing.smart.query.planner.UnmappedPropertyType_Weak_PlannerTests;
import com.braintribe.model.query.SelectQuery;

/**
 * @see UnmappedPropertyType_PlannerTests
 */
public class UnmappedPropertyType_Weak_Tests extends AbstractSmartQueryTests {

	/** @see UnmappedPropertyType_Weak_PlannerTests#selectUnmappedTypeProperty_Weak() */
	@Test
	public void selectUnmappedTypeProperty_Weak() {
		ManualA a = bA.manualA("ma").create();
		bA.readerA("r").favoriteManualTitle(a.getTitle()).create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.select("r", "weakFavoriteManual")
				.from(SmartReaderA.class, "r")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(smartManualA(a));
		assertNoMoreResults();
	}
	                                           
	/** @see UnmappedPropertyType_Weak_PlannerTests#selectUnmappedTypeProperty_Weak_Set() */
	@Test
	@SuppressWarnings("unused")
	public void selectUnmappedTypeProperty_Weak_Set() {
		ManualA a1 = bA.manualA("ma1").create();
		ManualA a2 = bA.manualA("ma2").create();
		ManualA a3 = bA.manualA("ma3").create();
		bA.readerA("r").favoriteManualTitles(a1.getTitle(), a2.getTitle()).create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.select("r", "weakFavoriteManuals")
				.from(SmartReaderA.class, "r")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(smartManualA(a1));
		assertResultContains(smartManualA(a2));
		assertNoMoreResults();
	}

	@Test
	@SuppressWarnings("unused")
	public void selectUnmappedTypeProperty_WeakInverse_Set() {
		ReaderA r = bA.readerA("r").create();
		ManualA a1 = bA.manualA("ma1").manualString(r.getName()).create();
		ManualA a2 = bA.manualA("ma2").manualString(r.getName()).create();
		ManualA a3 = bA.manualA("ma3").manualString("something else").create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.select("r", "weakInverseFavoriteManuals")
				.from(SmartReaderA.class, "r")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(smartManualA(a1));
		assertResultContains(smartManualA(a2));
		assertNoMoreResults();
	}
	
	/** @see UnmappedPropertyType_Weak_PlannerTests#selectUnmappedTypePropertyWithEntityCondition_Weak() */
	@Test
	public void selectUnmappedTypePropertyWithEntityCondition_Weak() {
		ManualA a = bA.manualA("ma").create();
		bA.readerA("r").favoriteManualTitle(a.getTitle()).create();

		SmartManualA sm = smartManualA(a);

		// @formatter:off
		SelectQuery selectQuery = query()		
				.select("r", "weakFavoriteManual")
				.from(SmartReaderA.class, "r")
				.where()
					.property("r", "weakFavoriteManual").eq().entity(sm)
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(sm);
		assertNoMoreResults();
	}

}
