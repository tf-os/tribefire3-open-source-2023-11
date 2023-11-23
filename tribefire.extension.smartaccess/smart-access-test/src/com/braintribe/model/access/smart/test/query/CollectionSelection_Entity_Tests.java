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

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.query.smart.test.model.accessA.CarA;
import com.braintribe.model.processing.query.smart.test.model.accessA.CompanyA;
import com.braintribe.model.processing.query.smart.test.model.accessA.FlyingCarA;
import com.braintribe.model.processing.query.smart.test.model.accessA.PersonA;
import com.braintribe.model.processing.query.smart.test.model.smart.Company;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartPersonA;
import com.braintribe.model.processing.session.impl.persistence.BasicPersistenceGmSession;
import com.braintribe.model.processing.smart.query.planner.CollectionSelection_Entity_PlannerTests;
import com.braintribe.model.query.JoinType;
import com.braintribe.model.query.SelectQuery;

/**
 * @see CollectionSelection_Entity_PlannerTests
 */
public class CollectionSelection_Entity_Tests extends AbstractSmartQueryTests {

	/** @see CollectionSelection_Entity_PlannerTests#simpleSetQuery() */
	@Test
	public void simpleEmptySetQuery() {
		bA.personA("john").create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.class, "p")
				.select("p", "companySetA")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(null);
		assertNoMoreResults();
	}

	/** @see CollectionSelection_Entity_PlannerTests#simpleSetQuery() */
	@Test
	public void innerJoinEmptySetQuery() {
		bA.personA("john").create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.class, "p")
				.join("p", "companySetA", "c", JoinType.inner)
				.select("c")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertNoMoreResults();
	}

	/** @see CollectionSelection_Entity_PlannerTests#simpleSetQuery() */
	@Test
	public void simpleSetQuery() {
		CompanyA c1 = bA.company("c1").create();
		CompanyA c2 = bA.company("c2").create();

		bA.personA("john").companies(c1, c2).create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.class, "p")
				.select("p", "companySetA")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		Company sc1 = smartCompany(c1);
		assertResultContains(sc1);
		assertResultContains(smartCompany(c2));
		assertNoMoreResults();
		
		BasicPersistenceGmSession session = new BasicPersistenceGmSession(smartAccess);
		GenericEntity ge = session.query().entity(sc1.reference()).find();
		session.query().entity(ge).require();

		
	}

	/** @see CollectionSelection_Entity_PlannerTests#queryWithDelegatableSetCondition() */
	@Test
	public void queryWithDelegatableSetCondition() {
		CompanyA c1 = bA.company("c1").create();
		CompanyA c2 = bA.company("c2").create();

		bA.personA("john").companies(c1, c2).create();
		bA.personA("jack").companies(c2).create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.class, "p")
				.select("p", "nameA")
				.where()
					.entity(smartCompany(c1)).in().property("p", "companySetA")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains("john");
		assertNoMoreResults();
	}

	/** @see CollectionSelection_Entity_PlannerTests#setQueryWithDelegatableSetCondition() */
	@Test
	public void setQueryWithDelegatableSetCondition() {
		CompanyA c1 = bA.company("c1").create();
		CompanyA c2 = bA.company("c2").create();

		bA.personA("john").companies(c1, c2).create();
		bA.personA("jack").companies(c2).create();

		Company cc1 = smartCompany(c1);
		Company cc2 = smartCompany(c2);

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.class, "p")
				.select("p", "nameA")
				.select("p", "companySetA")
				.where()
					.entity(cc1).in().property("p", "companySetA")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains("john", cc1);
		assertResultContains("john", cc2);
		assertNoMoreResults();
	}

	/** @see CollectionSelection_Entity_PlannerTests#simpleMapQuery() */
	@Test
	public void simpleMapQuery() {
		CompanyA c1 = bA.company("c1").create();
		CompanyA c2 = bA.company("c2").create();
		CompanyA c3 = bA.company("c3").create();

		PersonA p1 = bA.personA("p1").create();
		PersonA p2 = bA.personA("p2").create();
		PersonA p3 = bA.personA("p3").create();

		bA.personA("o1").addCompanyOwner(c1, p1).addCompanyOwner(c2, p2).create();
		bA.personA("o2").addCompanyOwner(c3, p3).create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.class, "p")
					.join("p", "companyOwnerA", "o")
				.select().mapKey("o")
				.select("o")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(smartCompany(c1), smartPerson(p1));
		assertResultContains(smartCompany(c2), smartPerson(p2));
		assertResultContains(smartCompany(c3), smartPerson(p3));
		assertNoMoreResults();
	}

	/** @see CollectionSelection_Entity_PlannerTests#simpleValueMapWithPolymorphicKeyQuery() */
	@Test
	public void simpleValueMapWithPolymorphicKeyQuery() {
		CarA c1 = bA.carA("car-1").create();
		CarA c2 = bA.carA("car-2").create();
		FlyingCarA c3 = bA.flyingCarA("flying-car-2").create();

		bA.personA("p1").addCarAlias(c1, "C1").addCarAlias(c3, "C3").create();
		bA.personA("p2").addCarAlias(c2, "C2").create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.class, "p")
					.join("p", "carAliasA", "o")
				.select().mapKey("o")
				.select("o")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(smartCar(c1), "C1");
		assertResultContains(smartCar(c2), "C2");
		assertResultContains(smartFlyingCar(c3), "C3");
		assertNoMoreResults();
	}
}
