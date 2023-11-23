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
package com.braintribe.model.processing.smart.query.planner;

import org.junit.Test;

import com.braintribe.model.processing.query.smart.test.model.accessA.CarA;
import com.braintribe.model.processing.query.smart.test.model.accessA.CompanyA;
import com.braintribe.model.processing.query.smart.test.model.accessA.FlyingCarA;
import com.braintribe.model.processing.query.smart.test.model.accessA.PersonA;
import com.braintribe.model.processing.query.smart.test.model.smart.Car;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartPersonA;
import com.braintribe.model.processing.smart.query.planner.base.AbstractSmartQueryPlannerTests;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.queryplan.set.OrderedSet;
import com.braintribe.model.queryplan.set.Projection;
import com.braintribe.model.smartqueryplan.set.DelegateQueryJoin;

/**
 * 
 */
public class OrderBy_PlannerTests extends AbstractSmartQueryPlannerTests {

	// #######################################
	// ## . . . . . Plain Sorting . . . . . ##
	// #######################################

	@Test
	public void defaultSort() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(SmartPersonA.T, "p")
				.orderBy().property("p", "nameA")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T)
			.whereProperty("operand").isDelegateQuerySet("accessA")
				.whereDelegateQuery()
						.whereFroms(1).whereElementAt(0).isFrom(PersonA.T)
						.whereOrdering().isSimpleOrderingWhereValue(false).isPropertyOperand("nameA")
		;
		// @formatter:on
	}

	@Test
	public void defaultSort_WithDqj() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(SmartPersonA.T, "p")
					.join("p", "keyCompanyExternalDqj", "c")
				.orderByCascade()
					.property("p", "nameA")
				.close()
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereProperty("operand")
				.hasType(DelegateQueryJoin.T)
				.whereProperty("materializedSet").isDelegateQuerySet("accessA")
					.whereDelegateQuery()
						.whereFroms(1).whereElementAt(0).isFrom(PersonA.T)
						.whereOrdering().isSimpleOrderingWhereValue(false).isPropertyOperand("nameA")
					.endQuery()
				.close()
				.whereProperty("querySet")
				.close()
		;
		// @formatter:on
	}

	@Test
	public void defaultSort_NotFullyDelegatable() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(SmartPersonA.T, "p")
					.join("p", "keyCompanyExternalDqj", "c")
				.orderByCascade()
					.property("p", "nameA")
					.property("c", "nameA")
				.close()
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereProperty("operand")
				.hasType(OrderedSet.T)
				.whereProperty("operand")
					.hasType(DelegateQueryJoin.T)
					.whereProperty("materializedSet").isDelegateQuerySet("accessA")
						.whereDelegateQuery()
							.whereFroms(1).whereElementAt(0).isFrom(PersonA.T)
							.whereOrdering().isSimpleOrderingWhereValue(false).isPropertyOperand("nameA")
						.endQuery()
					.close()
					.whereProperty("querySet")
						.whereDelegateQuery()
							.whereFroms(1).whereElementAt(0).isFrom(CompanyA.T)
							.withouthOrdering()
						.endQuery()
					.close()
				.close()
				.whereProperty("sortCriteria").isListWithSize(1)
					// companyNameA, id, nameA => c.nameA == p.companyNameA has position 0
					.whereElementAt(0).isSortCriteriaAndValue(false).isTupleComponent_(0).close(2)
				.whereProperty("groupValues").isListWithSize(1)
					.whereElementAt(0).isTupleComponent_(3)
				
		;
		// @formatter:on
	}

	/* TODO OPTIMIZE - no need to also select p.nameA if we are not using it for anything */
	@Test
	public void simpleSortByNonSelectedProperty() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(SmartPersonA.T, "p")
				.select("p", "companyNameA")
				.orderBy().property("p", "nameA")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereProperty("operand")
				.isDelegateQuerySet("accessA")
					.whereDelegateQuery()
						.whereSelection(2)
						.whereFroms(1).whereElementAt(0).isFrom(PersonA.T)
						.whereOrdering().isSimpleOrderingWhereValue(false).isPropertyOperand("nameA")
					.endQuery()
		;
		// @formatter:on
	}

	@Test
	public void simpleSortByDqjPropertyWhereDqjCanBeAvoided() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(SmartPersonA.T, "p")
					.join("p", "keyCompanyA", "c")
				.select("c", "nameA")
				.orderBy().property("c", "nameA") 
				.done();
		// @formatter:on

		runTest(selectQuery);

		/* TODO OPTIMIZE this right now does a DQJ, left side is Company ordered by CompanyA.name, right is Person. But
		 * CompanyA.nameA is our correlation property, so we could simply do just one query - select * from PersonA p
		 * order by p.companyNameA. */
	}

	@Test
	public void simpleSortByNonSelectedProperty_WithDqj() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(SmartPersonA.T, "p")
					.join("p", "keyCompanyExternalDqj", "c")
				.select("c", "nameA")
				.where().property("c", "nameA").like("*llc*") // this forces that we first select Company and only then DQJ Person to it
				.orderBy().property("p", "nameA") // this means we will also retrieve the Person.nameA property, though only for sorting
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereProperty("operand")
				.hasType(OrderedSet.T)
				.whereProperty("operand")
					.hasType(DelegateQueryJoin.T)
					.whereProperty("materializedSet").isDelegateQuerySet("accessA")
						.whereDelegateQuery()
							.whereFroms(1).whereElementAt(0).isFrom(CompanyA.T)
							.withouthOrdering()
						.endQuery()
					.close()
					.whereProperty("querySet")
						.whereDelegateQuery()
							.whereFroms(1).whereElementAt(0).isFrom(PersonA.T)
							.withouthOrdering()
						.endQuery()
					.close()
				.close()
				.whereProperty("sortCriteria").isListWithSize(1)
					// CompanyA.nameA, PersonA.nameA => PersonA.nameA has position 1
					.whereElementAt(0).isSortCriteriaAndValue(false).isTupleComponent_(1).close()
		;
		// @formatter:on
	}

	// #######################################
	// ## . . . Sorting With Pagination . . ##
	// #######################################

	@Test
	public void orderByWithPagination() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(SmartPersonA.T, "p")
				.orderBy().property("p", "nameA")
				.paging(2, 1)
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereProperty("operand")
				.isDelegateQuerySet("accessA")
					.whereDelegateQuery()
						.whereFroms(1).whereElementAt(0).isFrom(PersonA.T)
						.whereOrdering().isSimpleOrderingWhereValue(false).isPropertyOperand("nameA")
						.withPagination(2, 1)
					.endQuery()
		;
		// @formatter:on
	}

	@Test
	public void paginationWhenSelectingPolymorphicEntity() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(Car.T, "c")
				.select("c")
				.orderBy().property("c", "serialNumber")
				.limit(1)
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereProperty("operand")
				.hasType(DelegateQueryJoin.T)
				.whereProperty("materializedSet").isDelegateQuerySet("accessA")
					.whereDelegateQuery()
						.whereFroms(1).whereElementAt(0).isFrom(CarA.T)
						.whereOrdering()
							.isSimpleOrderingWhereValue(false).isPropertyOperand("serialNumber")
						.withPagination(1, 0)
					.endQuery()
					.whereProperty("batchSize").isNull().close()
				.close()
				.whereProperty("querySet")
					.whereDelegateQuery()
						.whereFroms(1).whereElementAt(0).isFrom(FlyingCarA.T)
						.withouthOrdering()
					.endQuery()
				.close()
		;
		// @formatter:on
	}
}
