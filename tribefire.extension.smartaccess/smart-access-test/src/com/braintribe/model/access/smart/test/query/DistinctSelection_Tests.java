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
import org.junit.experimental.categories.Category;

import com.braintribe.model.processing.query.smart.test.model.accessA.CompanyA;
import com.braintribe.model.processing.query.smart.test.model.accessA.PersonA;
import com.braintribe.model.processing.query.smart.test.model.accessB.ItemB;
import com.braintribe.model.processing.query.smart.test.model.smart.Company;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartItem;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartPersonA;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.testing.category.KnownIssue;

/**
 * 
 */
public class DistinctSelection_Tests extends AbstractSmartQueryTests {

	@Test
	public void simpleProperty() {
		bA.personA("p1").create();
		bA.personA("p1").create();
		bA.personA("p2").create();
		bA.personA("p2").create();
		bA.personA("p2").create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(SmartPersonA.class, "p")
				.select("p", "nameA")
				.distinct()
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains("p1");
		assertResultContains("p2");
		assertNoMoreResults();
	}

	@Test
	public void listProperty() {
		bA.personA("p").nickNamesA("n1", "n1", "n2", "n2", "n2").create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.select("p", "nickNamesListA")
				.from(SmartPersonA.class, "p")
				.distinct()
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains("n1");
		assertResultContains("n2");
		assertNoMoreResults();
	}

	@Test
	public void setProperty() {
		bA.personA("p1").nickNamesA("n1").create();
		bA.personA("p2").nickNamesA("n1", "n2").create();
		bA.personA("p3").nickNamesA("n2").create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.select("p", "nickNamesSetA")
				.from(SmartPersonA.class, "p")
				.distinct()
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains("n1");
		assertResultContains("n2");
		assertNoMoreResults();
	}

	@Test
	@Category(KnownIssue.class)
	public void EXPECTED_TO_FAIL_kpaListProperty() {
		CompanyA c1 = bA.company("c1").create();
		CompanyA c2 = bA.company("c2").create();

		bA.personA("p").companyNames("c1", "c1", "c2", "c2", "c2").create();

		Company sc1 = smartCompany(c1);
		Company sc2 = smartCompany(c2);

		// @formatter:off
		SelectQuery selectQuery = query()
				.select("p", "keyCompanyListA")
				.from(SmartPersonA.class, "p")
				.distinct()
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(sc1);
		assertResultContains(sc2);
		assertNoMoreResults();
	}
	
	@Test
	public void kpaListProperty_ExternalDqj() {
		CompanyA c1 = bA.company("c1").create();
		CompanyA c2 = bA.company("c2").create();
		
		bA.personA("p").companyNames("c1", "c1", "c2", "c2", "c2").create();
		
		Company sc1 = smartCompany(c1);
		Company sc2 = smartCompany(c2);
		
		// @formatter:off
		SelectQuery selectQuery = query()
				.select("p", "keyCompanyListExternalDqj")
				.from(SmartPersonA.class, "p")
				.distinct()
				.done();
		// @formatter:on
		
		evaluate(selectQuery);
		
		assertResultContains(sc1);
		assertResultContains(sc2);
		assertNoMoreResults();
	}

	@Test
	public void lpaListProperty() {
		ItemB i1 = bB.item("i1").create();
		ItemB i2 = bB.item("i2").create();

		PersonA p = bA.personA("p1").create();

		bB.personItemOrderedLink(p, i1, 0);
		bB.personItemOrderedLink(p, i1, 1);
		bB.personItemOrderedLink(p, i2, 2);
		bB.personItemOrderedLink(p, i2, 3);
		bB.personItemOrderedLink(p, i2, 4);

		SmartItem si1 = smartItem(i1);
		SmartItem si2 = smartItem(i2);

		// @formatter:off
		SelectQuery selectQuery = query()
				.select("p", "orderedLinkItems")
				.from(SmartPersonA.class, "p")
				.distinct()
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(si1);
		assertResultContains(si2);
		assertNoMoreResults();
	}

}
