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
import com.braintribe.model.processing.query.smart.test.model.smart.Company;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartPersonA;
import com.braintribe.model.processing.smart.query.planner.CollectionSelection_KeyProperty_PlannerTests;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.testing.category.KnownIssue;

/**
 * 
 */
public class CollectionSelection_KeyProperty_Tests extends AbstractSmartQueryTests {

	/** @see CollectionSelection_KeyProperty_PlannerTests#EXPECTED_TO_FAIL_simpleSetQuery() */
	@Test
	@Category(KnownIssue.class)
	public void EXPECTED_TO_FAIL_simpleSetQuery() {
		CompanyA c1 = bA.company("c1").create();
		CompanyA c2 = bA.company("c2").create();
		CompanyA c3 = bA.company("c3").create();

		bA.personA("john").companyNames(c1.getNameA(), c2.getNameA()).create();
		bA.personA("jack").companyNames(c3.getNameA()).create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.class, "p")
				.select("p", "keyCompanySetA")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(smartCompany(c1));
		assertResultContains(smartCompany(c2));
		assertResultContains(smartCompany(c3));
		assertNoMoreResults();
	}

	/** @see CollectionSelection_KeyProperty_PlannerTests#simpleSetQuery_ExternalDqj() */
	@Test
	public void simpleSetQuery_ExternalDqj() {
		CompanyA c1 = bA.company("c1").create();
		CompanyA c2 = bA.company("c2").create();
		CompanyA c3 = bA.company("c3").create();

		bA.personA("john").companyNames(c1.getNameA(), c2.getNameA()).create();
		bA.personA("jack").companyNames(c3.getNameA()).create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.class, "p")
				.select("p", "keyCompanySetExternalDqj")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(smartCompany(c1));
		assertResultContains(smartCompany(c2));
		assertResultContains(smartCompany(c3));
		assertNoMoreResults();
	}

	/** @see CollectionSelection_KeyProperty_PlannerTests#EXPECTED_TO_FAIL_queryWithDelegatableSetCondition() */
	@Test
	@Category(KnownIssue.class)
	public void EXPECTED_TO_FAIL_queryWithDelegatableSetCondition() {
		CompanyA c1 = bA.company("c1").create();
		CompanyA c2 = bA.company("c2").create();

		bA.personA("john").companyNames(c1.getNameA(), c2.getNameA()).create();
		bA.personA("jack").companyNames(c2.getNameA()).create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.class, "p")
				.select("p", "nameA")
				.where()
					.entity(smartCompany(c1)).in().property("p", "keyCompanySetA")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains("john");
		assertNoMoreResults();
	}

	/** @see CollectionSelection_KeyProperty_PlannerTests#queryWithDelegatableSetCondition_ExternalDqj() */
	@Test
	public void queryWithDelegatableSetCondition_ExternalDqj() {
		CompanyA c1 = bA.company("c1").create();
		CompanyA c2 = bA.company("c2").create();

		bA.personA("john").companyNames(c1.getNameA(), c2.getNameA()).create();
		bA.personA("jack").companyNames(c2.getNameA()).create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.class, "p")
				.select("p", "nameA")
				.where()
					.entity(smartCompany(c1)).in().property("p", "keyCompanySetExternalDqj")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains("john");
		assertNoMoreResults();
	}

	/** @see CollectionSelection_KeyProperty_PlannerTests#EXPECTED_TO_FAIL_setQueryWithDelegatableSetCondition() */
	@Test
	@Category(KnownIssue.class)
	public void EXPECTED_TO_FAIL_setQueryWithDelegatableSetCondition() {
		CompanyA c1 = bA.company("c1").create();
		CompanyA c2 = bA.company("c2").create();

		bA.personA("john").companyNames(c1.getNameA(), c2.getNameA()).create();
		bA.personA("jack").companyNames(c2.getNameA()).create();

		Company cc1 = smartCompany(c1);
		Company cc2 = smartCompany(c2);

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.class, "p")
				.select("p", "nameA")
				.select("p", "keyCompanySetA")
				.where()
					.entity(cc1).in().property("p", "keyCompanySetA")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains("john", cc1);
		assertResultContains("john", cc2);
		assertNoMoreResults();
	}

	/** @see CollectionSelection_KeyProperty_PlannerTests#setQueryWithDelegatableSetCondition_ExternalDqj() */
	@Test
	public void setQueryWithDelegatableSetCondition_ExternalDqj() {
		CompanyA c1 = bA.company("c1").create();
		CompanyA c2 = bA.company("c2").create();

		bA.personA("john").companyNames(c1.getNameA(), c2.getNameA()).create();
		bA.personA("jack").companyNames(c2.getNameA()).create();

		Company cc1 = smartCompany(c1);
		Company cc2 = smartCompany(c2);

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.class, "p")
				.select("p", "nameA")
				.select("p", "keyCompanySetExternalDqj")
				.where()
					.entity(cc1).in().property("p", "keyCompanySetExternalDqj")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains("john", cc1);
		assertResultContains("john", cc2);
		assertNoMoreResults();
	}

	/** @see CollectionSelection_KeyProperty_PlannerTests#EXPECTED_TO_FAIL_simpleListQuery() */
	@Test
	@Category(KnownIssue.class)
	public void EXPECTED_TO_FAIL_simpleListQuery() {
		CompanyA c1 = bA.company("c1").create();
		CompanyA c2 = bA.company("c2").create();
		CompanyA c3 = bA.company("c3").create();

		bA.personA("john").companyNames(c1.getNameA(), c2.getNameA(), c1.getNameA()).create();
		bA.personA("jack").companyNames(c3.getNameA()).create();

		Company sc1 = smartCompany(c1);
		Company sc2 = smartCompany(c2);
		Company sc3 = smartCompany(c3);

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.class, "p")
				.select("p", "keyCompanyListA")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(sc1);
		assertResultContains(sc2);
		assertResultContains(sc1);
		assertResultContains(sc3);
		assertNoMoreResults();
	}

	/** @see CollectionSelection_KeyProperty_PlannerTests#simpleListQuery_ExternalDqj() */
	@Test
	public void simpleListQuery_ExternalDqj() {
		CompanyA c1 = bA.company("c1").create();
		CompanyA c2 = bA.company("c2").create();
		CompanyA c3 = bA.company("c3").create();

		bA.personA("john").companyNames(c1.getNameA(), c2.getNameA(), c1.getNameA()).create();
		bA.personA("jack").companyNames(c3.getNameA()).create();

		Company sc1 = smartCompany(c1);
		Company sc2 = smartCompany(c2);
		Company sc3 = smartCompany(c3);

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.class, "p")
				.select("p", "keyCompanyListExternalDqj")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(sc1);
		assertResultContains(sc2);
		assertResultContains(sc1);
		assertResultContains(sc3);
		assertNoMoreResults();
	}

	/** @see CollectionSelection_KeyProperty_PlannerTests#EXPECTED_TO_FAIL_listQueryWithListIndex() */
	@Test
	@Category(KnownIssue.class)
	public void EXPECTED_TO_FAIL_listQueryWithListIndex() {
		CompanyA c1 = bA.company("c1").create();
		CompanyA c2 = bA.company("c2").create();
		CompanyA c3 = bA.company("c3").create();

		bA.personA("john").companyNames(c1.getNameA(), c2.getNameA(), c1.getNameA()).create();
		bA.personA("jack").companyNames(c3.getNameA()).create();

		Company sc1 = smartCompany(c1);
		Company sc2 = smartCompany(c2);
		Company sc3 = smartCompany(c3);

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.class, "p")
					.join("p", "keyCompanyListA", "e")
				.select().listIndex("e")
				.select("e")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(0, sc1);
		assertResultContains(1, sc2);
		assertResultContains(2, sc1);
		assertResultContains(0, sc3);
		assertNoMoreResults();
	}

	/** @see CollectionSelection_KeyProperty_PlannerTests#listQueryWithListIndex_ExternalDqj() */
	@Test
	public void listQueryWithListIndex_ExternalDqj() {
		CompanyA c1 = bA.company("c1").create();
		CompanyA c2 = bA.company("c2").create();
		CompanyA c3 = bA.company("c3").create();

		bA.personA("john").companyNames(c1.getNameA(), c2.getNameA(), c1.getNameA()).create();
		bA.personA("jack").companyNames(c3.getNameA()).create();

		Company sc1 = smartCompany(c1);
		Company sc2 = smartCompany(c2);
		Company sc3 = smartCompany(c3);

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.class, "p")
					.join("p", "keyCompanyListExternalDqj", "e")
				.select().listIndex("e")
				.select("e")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(0, sc1);
		assertResultContains(1, sc2);
		assertResultContains(2, sc1);
		assertResultContains(0, sc3);
		assertNoMoreResults();
	}
	/** @see CollectionSelection_KeyProperty_PlannerTests#EXPECTED_TO_FAIL_simpleMapQuery() */
	@Test
	@Category(KnownIssue.class)
	public void EXPECTED_TO_FAIL_simpleMapQuery() {
		CompanyA c1 = bA.company("c1").create();
		CompanyA c2 = bA.company("c2").create();
		CompanyA c3 = bA.company("c3").create();

		bA.personA("john").addFriendCompanyName("john F1", c1).addFriendCompanyName("john F2", c2).create();
		bA.personA("jack").addFriendCompanyName("jack F1", c3).create();

		Company cc1 = smartCompany(c1);
		Company cc2 = smartCompany(c2);
		Company cc3 = smartCompany(c3);

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.class, "p")
					.join("p", "keyFriendEmployerA", "o")
				.select().mapKey("o")
				.select("o")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains("john F1", cc1);
		assertResultContains("john F2", cc2);
		assertResultContains("jack F1", cc3);
		assertNoMoreResults();
	}

	/** @see CollectionSelection_KeyProperty_PlannerTests#simpleMapQuery_ExternalDqj() */
	@Test
	public void simpleMapQuery_ExternalDqj() {
		CompanyA c1 = bA.company("c1").create();
		CompanyA c2 = bA.company("c2").create();
		CompanyA c3 = bA.company("c3").create();

		bA.personA("john").addFriendCompanyName("john F1", c1).addFriendCompanyName("john F2", c2).create();
		bA.personA("jack").addFriendCompanyName("jack F1", c3).create();

		Company cc1 = smartCompany(c1);
		Company cc2 = smartCompany(c2);
		Company cc3 = smartCompany(c3);

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.class, "p")
				.join("p", "keyFriendEmployerExternalDqj", "o")
				.select().mapKey("o")
				.select("o")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains("john F1", cc1);
		assertResultContains("john F2", cc2);
		assertResultContains("jack F1", cc3);
		assertNoMoreResults();
	}
}
