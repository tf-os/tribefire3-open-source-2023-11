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
import com.braintribe.model.processing.query.smart.test.model.accessB.special.BookB;
import com.braintribe.model.processing.query.smart.test.model.smart.special.SmartBookB;
import com.braintribe.model.processing.query.smart.test.model.smart.special.SmartReaderA;
import com.braintribe.model.processing.smart.query.planner.UnmappedPropertyType_PlannerTests;
import com.braintribe.model.query.SelectQuery;

/**
 * @see UnmappedPropertyType_PlannerTests
 */
public class UnmappedPropertyType_Tests extends AbstractSmartQueryTests {

	/** @see UnmappedPropertyType_PlannerTests#selectUnmappedTypeProperty() */
	@Test
	public void selectUnmappedTypeProperty() {
		BookB b = bB.bookB("bb").create();
		bA.readerA("r").favoritePublicationTitle(b.getTitleB()).create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.select("r", "favoritePublication")
				.from(SmartReaderA.class, "r")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(smartBookB(b));
		assertNoMoreResults();
	}

	/** @see UnmappedPropertyType_PlannerTests#selectUnmappedTypeProperty_Set() */
	@Test
	public void selectUnmappedTypeProperty_Set() {
		BookB b1 = bB.bookB("bb1").create();
		BookB b2 = bB.bookB("bb2").create();
		bA.readerA("r").favoritePublicationTitles(b1.getTitleB(), b2.getTitleB()).create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.select("r", "favoritePublications")
				.from(SmartReaderA.class, "r")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(smartBookB(b1));
		assertResultContains(smartBookB(b2));
		assertNoMoreResults();
	}

	/** @see UnmappedPropertyType_PlannerTests#selectUnmappedTypePropertyWithSignatureCondition() */
	@Test
	public void selectUnmappedTypePropertyWithSignatureCondition() {
		BookB b = bB.bookB("bb").create();
		bA.readerA("r").favoritePublicationTitle(b.getTitleB()).create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.select("r", "favoritePublication")
				.from(SmartReaderA.class, "r")
				.where()
					.entitySignature("r", "favoritePublication").eq(SmartBookB.class.getName())
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(smartBookB(b));
		assertNoMoreResults();
	}

	/** @see UnmappedPropertyType_PlannerTests#selectUnmappedTypePropertyWithEntityCondition() */
	@Test
	public void selectUnmappedTypePropertyWithEntityCondition() {
		BookB b = bB.bookB("bb").create();
		bA.readerA("r").favoritePublicationTitle(b.getTitleB()).create();

		SmartBookB sb = smartBookB(b);

		// @formatter:off
		SelectQuery selectQuery = query()		
				.select("r", "favoritePublication")
				.from(SmartReaderA.class, "r")
				.where()
					.property("r", "favoritePublication").eq().entity(sb)
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(sb);
		assertNoMoreResults();
	}

	/** @see UnmappedPropertyType_PlannerTests#selectAsIsMappedPropertyAndNotSmartReferenceUseCase() */
	@Test
	public void selectAsIsMappedPropertyAndNotSmartReferenceUseCase() {
		ManualA m = bA.manualA("m").create();
		bA.readerA("r").favoriteManual(m).create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.select("r", "favoriteManual")
				.from(SmartReaderA.class, "r")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(smartManualA(m));
		assertNoMoreResults();
	}

	@Test
	public void selectIkpaPropertyWhereKeyPropertyIsUnmappedSmartOne() {
		BookB b = bB.bookB("bb").create();
		ReaderA r = bA.readerA("r").ikpaPublicationTitle(b.getTitleB()).create();

		SelectQuery selectQuery = query()		
				.select("b", "favoriteReader")
				.from(SmartBookB.class, "b")
				.done();

		evaluate(selectQuery);

		assertResultContains(smartReaderA(r));
		assertNoMoreResults();
	}

}
