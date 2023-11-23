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

import com.braintribe.model.processing.query.eval.set.EvalPaginatedSet;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.query.smart.test.model.accessA.CarA;
import com.braintribe.model.processing.query.smart.test.model.accessA.CompanyA;
import com.braintribe.model.processing.query.smart.test.model.accessA.PersonA;
import com.braintribe.model.processing.query.smart.test.model.accessA.special.BookA;
import com.braintribe.model.processing.query.smart.test.model.accessB.special.BookB;
import com.braintribe.model.processing.query.smart.test.model.smart.Car;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartPersonA;
import com.braintribe.model.processing.query.smart.test.model.smart.special.SmartPublication;
import com.braintribe.model.processing.smart.query.planner.OrderBy_PlannerTests;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.smart.processing.eval.set.EvalOrderedConcatenation;
import com.braintribe.model.queryplan.set.PaginatedSet;

/**
 * 
 */
public class OrderBy_Tests extends AbstractSmartQueryTests {

	/** @see OrderBy_PlannerTests#defaultSort() */
	@Test
	public void defaultSort() {
		PersonA p1 = bA.personA("aa").create();
		PersonA p2 = bA.personA("bb").create();
		PersonA p3 = bA.personA("cc").create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(SmartPersonA.class, "p")
				.select("p")
				.orderBy().property("p", "nameA")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertNextResult(smartPerson(p1));
		assertNextResult(smartPerson(p2));
		assertNextResult(smartPerson(p3));
		assertNoMoreResults();
	}

	/** @see OrderBy_PlannerTests#defaultSort() */
	@Test
	public void compoundSortEntity() {
		CompanyA c1 = bA.company("c_aa").create();
		CompanyA c2 = bA.company("c_bb").create();
		CompanyA c3 = bA.company("c_cc").create();

		PersonA p1 = bA.personA("aa").companyA(c1).create();
		PersonA p2 = bA.personA("bb").companyA(c2).create();
		PersonA p3 = bA.personA("cc").companyA(c3).create();

		// @formatter:off
		EntityQuery selectQuery = EntityQueryBuilder
				.from(SmartPersonA.class)
				.orderBy().property("companyA.nameA")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertNextResult(smartPerson(p1));
		assertNextResult(smartPerson(p2));
		assertNextResult(smartPerson(p3));
		assertNoMoreResults();
	}

	/** @see OrderBy_PlannerTests#defaultSort_WithDqj() */
	@Test
	public void defaultSort_WithDqj() {
		PersonA p1 = bA.personA("aa").companyNameA("cA").create();
		PersonA p2 = bA.personA("bb").companyNameA("cB").create();

		CompanyA c1 = bA.company("cA").create();
		CompanyA c2 = bA.company("cB").create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(SmartPersonA.class, "p")
					.join("p", "keyCompanyA", "c")
				.orderByCascade()
					.property("p", "nameA")
				.close()
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertNextResult(smartPerson(p1), smartCompany(c1));
		assertNextResult(smartPerson(p2), smartCompany(c2));
		assertNoMoreResults();
	}

	/** @see OrderBy_PlannerTests#defaultSort_NotFullyDelegatable() */
	@Test
	public void defaultSort_NotFullyDelegatable() {
		PersonA pa1 = bA.personA("aa1").companyNameA("cA").create();
		PersonA pa2 = bA.personA("aa2").companyNameA("cA").create();
		PersonA pb1 = bA.personA("bb1").companyNameA("cB").create();
		PersonA pb2 = bA.personA("bb2").companyNameA("cB").create();

		CompanyA c1 = bA.company("cA").create();
		CompanyA c2 = bA.company("cB").create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(SmartPersonA.class, "p")
					.join("p", "keyCompanyA", "c")
				.orderByCascade()
					.property("p", "nameA")
					.property("c", "nameA")
				.close()
				.done();
		// @formatter:on

		evaluate(selectQuery);
		assertNextResult(smartPerson(pa1), smartCompany(c1));
		assertNextResult(smartPerson(pa2), smartCompany(c1));
		assertNextResult(smartPerson(pb1), smartCompany(c2));
		assertNextResult(smartPerson(pb2), smartCompany(c2));
		assertNoMoreResults();
	}

	/** @see OrderBy_PlannerTests#simpleSortByNonSelectedProperty() */
	@Test
	public void simpleSortByNonSelectedProperty() {
		bA.personA("aa").companyNameA("c1").create();
		bA.personA("bb").companyNameA("c2").create();
		bA.personA("cc").companyNameA("c3").create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(SmartPersonA.class, "p")
				.select("p", "companyNameA")
				.orderBy().property("p", "nameA")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertNextResult("c1");
		assertNextResult("c2");
		assertNextResult("c3");
		assertNoMoreResults();
	}

	/** @see OrderBy_PlannerTests#simpleSortByNonSelectedProperty_WithDqj() */
	@Test
	public void simpleSortByNonSelectedProperty_WithDqj() {
		bA.personA("aa").companyNameA("cA").create();
		bA.personA("bb").companyNameA("cB").create();
		bA.personA("cc").companyNameA("cC").create();

		bA.company("cA").create();
		bA.company("cB").create();
		bA.company("cC").create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(SmartPersonA.class, "p")
					.join("p", "keyCompanyA", "c")
				.select("c", "nameA")
				.where().property("c", "nameA").like("c**") // this forces that we first select Company and only then DQJ Person to it
				.orderBy().property("p", "nameA") // this means we will also retrieve the Person.nameA property, though only for sorting
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertNextResult("cA");
		assertNextResult("cB");
		assertNextResult("cC");
		assertNoMoreResults();
	}

	/** @see OrderBy_PlannerTests#orderByWithPagination() */
	@SuppressWarnings("unused")
	@Test
	public void orderByWithPagination() {
		PersonA p1 = bA.personA("aa").create();
		PersonA p2 = bA.personA("bb").create();
		PersonA p3 = bA.personA("cc").create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(SmartPersonA.class, "p")
				.orderBy().property("p", "nameA")
				.paging(2, 1)
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertNextResult(smartPerson(p2));
		assertNextResult(smartPerson(p3));
		assertNoMoreResults();

		assertHasMoreFlag(false);
	}

	/** Similar to {@link #orderByWithPagination()}, but hasMore expected to be true */
	@SuppressWarnings("unused")
	@Test
	public void orderByWithPagination_HasMore() {
		PersonA p1 = bA.personA("aa").create();
		PersonA p2 = bA.personA("bb").create();
		PersonA p3 = bA.personA("cc").create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(SmartPersonA.class, "p")
				.orderBy().property("p", "nameA")
				.paging(2, 0)
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertNextResult(smartPerson(p1));
		assertNextResult(smartPerson(p2));
		assertNoMoreResults();

		assertHasMoreFlag(true);
	}

	@SuppressWarnings("unused")
	@Test
	public void orderByWithPagination_Entities() {
		PersonA p1 = bA.personA("aa").create();
		PersonA p2 = bA.personA("bb").create();
		PersonA p3 = bA.personA("cc").create();

		// @formatter:off
		EntityQuery entityQuery = EntityQueryBuilder
				.from(SmartPersonA.class)
				.orderBy().property("nameA")
				.paging(2, 0)
				.done();
		// @formatter:on

		evaluate(entityQuery);

		assertNextResult(smartPerson(p1));
		assertNextResult(smartPerson(p2));
		assertNoMoreResults();

		assertHasMoreFlag(true);
	}

	/** @see OrderBy_PlannerTests#paginationWhenSelectingPolymorphicEntity() */
	@Test
	public void paginationWhenSelectingPolymorphicEntity() {
		CarA c1;
		c1 = bA.flyingCarA("flying-car-2").maxFlyingSpeed(1200).create();
		c1 = bA.carA("car-1").create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(Car.class, "c")
				.select("c")
				.orderBy().property("c", "serialNumber")
				.limit(1)
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(smartCar(c1));
		assertNoMoreResults();

		assertHasMoreFlag(true);
	}

	/**
	 * This test should work no problem, it's just here cause of he next one -
	 * {@link #paginationWhenSplittingQuery_OnlyOneDelegateGivesResuls()}.
	 */
	@SuppressWarnings("unused")
	@Test
	public void paginationWhenSplittingQuery() {
		BookA ba = bA.bookA("ba").create();
		BookB bb = bB.bookB("bb").create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPublication.class, "p")
				.select("p")
				.orderBy().property("p", "title")
				.limit(1)
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(smartBookA(ba));
		assertNoMoreResults();

		assertHasMoreFlag(true);
	}

	/**
	 * We had a bug with initial implementation of how hasMore is resolved for {@link EvalPaginatedSet}. Initially,
	 * there was only a check whether the operand iterator has more results (hasNext is still true after getting all the
	 * needed tuples). However, in case our operand is for example {@link EvalOrderedConcatenation} of two different
	 * queries, these queries might have pagination already applied. If one of the query then returns required number of
	 * results and the other returns zero, then the delegate iterator has no next, and we would evaluate to false, but
	 * the thing is, the first operand might have more results. Therefore, we have added a flag to {@link PaginatedSet}
	 * that says that the operand might already have pagination applied, so if no next entry exists, it checks whether
	 * the operand itself says it "has more".
	 * 
	 * This comment is slightly adjusted from what can be seen at
	 * {@link PaginatedSet#setOperandMayApplyPagination(boolean)}.
	 */
	@Test
	public void paginationWhenSplittingQuery_OnlyOneDelegateGivesResuls() {
		BookA ba;
		ba = bA.bookA("ba2").create();
		ba = bA.bookA("ba1").create();

		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPublication.class, "p")
				.select("p")
				.orderBy().property("p", "title")
				.limit(1)
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(smartBookA(ba));
		assertNoMoreResults();

		assertHasMoreFlag(true);
	}
}
