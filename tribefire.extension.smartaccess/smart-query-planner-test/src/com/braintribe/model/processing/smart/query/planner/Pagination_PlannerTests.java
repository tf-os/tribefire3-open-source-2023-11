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

import java.util.Date;

import org.junit.Test;

import com.braintribe.model.processing.query.smart.test.model.accessA.CompanyA;
import com.braintribe.model.processing.query.smart.test.model.accessA.PersonA;
import com.braintribe.model.processing.query.smart.test.model.accessB.PersonB;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartPersonA;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartPersonB;
import com.braintribe.model.processing.smart.query.planner.base.AbstractSmartQueryPlannerTests;
import com.braintribe.model.query.Operator;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.queryplan.filter.ValueComparison;
import com.braintribe.model.queryplan.set.FilteredSet;
import com.braintribe.model.queryplan.set.OrderedSet;
import com.braintribe.model.queryplan.set.PaginatedSet;
import com.braintribe.model.queryplan.set.Projection;
import com.braintribe.model.smartqueryplan.set.DelegateQueryJoin;

/**
 * We are testing that pagination, even if cannot be delegate, can be optimized - such that not all the data is
 * retrieved if we know we might not need it due to pagination.
 */
public class Pagination_PlannerTests extends AbstractSmartQueryPlannerTests {

	// TODO FIX evaluation tests
	
	/** This can be optimized using bulks, cause we simply return first ten results and that's it */
	@Test
	public void bulksDueToPagination() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(SmartPersonA.T, "p")
					.join("p", "keyCompanyExternalDqj", "c")
				.limit(10)
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(PaginatedSet.T).whereProperty("operand")
				.hasType(Projection.T).whereProperty("operand")
					.hasType(DelegateQueryJoin.T)
					.whereProperty("materializedSet").isDelegateQuerySet("accessA")
						.whereDelegateQuery()
							.whereFroms(1).whereElementAt(0).isFrom(CompanyA.T)
							.withNoRestriction()
							.whereOrdering().isSimpleOrderingWhereValue(false).isPropertyOperand("id")
						.endQuery()
						.whereProperty("batchSize").isNotNull().close()
					.close()
					.whereProperty("querySet")
						.whereDelegateQuery()
							.whereFroms(1).whereElementAt(0).isFrom(PersonA.T)
							.withouthOrdering()
						.endQuery()
					.close()
				.close()
			.close()
			.whereProperty("limit").is_(10)
			.whereProperty("offset").is_(0)
		;
		// @formatter:on
	}

	/** This can be optimized using bulks, cause we simply return first ten results and that's it */
	@Test
	public void bulksDueToPagination_WithSimpleCollection() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(SmartPersonA.T, "p")
					.join("p", "nickNamesSetA", "n")
					.join("p", "keyCompanyExternalDqj", "c")
				.where()
					// forcing Person to be first (materialized side of DQJ)
					.property("p", "nameA").like("p*")
				.limit(10)
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(PaginatedSet.T).whereProperty("operand")
				.hasType(Projection.T).whereProperty("operand")
					.hasType(DelegateQueryJoin.T)
					.whereProperty("materializedSet").isDelegateQuerySet("accessA")
						.whereDelegateQuery()
							.whereFroms(1)
								.whereElementAt(0).isFrom(PersonA.T)
							.withRestrictionWithoutPaging()
							.whereOrdering().isCascadeOrdering().whenOrderedBy("orderBy.propertyName")
								.whereElementAt(0).isSimpleOrderingWhereValue(false).isSourceOnlyPropertyOperand().whereSource().isJoin("nickNamesSetA").close(3)
								.whereElementAt(1).isSimpleOrderingWhereValue(false).isPropertyOperand("id").close(2)
						.endQuery()
						.whereProperty("batchSize").isNotNull().close()
					.close()
					.whereProperty("querySet")
						.whereDelegateQuery()
							.whereFroms(1).whereElementAt(0).isFrom(CompanyA.T)
							.withouthOrdering()
						.endQuery()
					.close()
				.close()
			.close()
			.whereProperty("limit").is_(10)
			.whereProperty("offset").is_(0)
		;
		// @formatter:on
	}

	/** Similar to simple case ({@link #bulksDueToPagination()}), but with conditions (that do not change anything) */
	@Test
	public void bulksDueToPagination_WhenConditions() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(SmartPersonA.T, "p")
					.join("p", "keyCompanyExternalDqj", "c")
				.where()
					.conjunction()
						.property("p", "nameA").like("P*")
						.property("c", "nameA").like("C*")
					.close()
				.limit(10)
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(PaginatedSet.T).whereProperty("operand")
				.hasType(Projection.T).whereProperty("operand")
					.hasType(DelegateQueryJoin.T)
					.whereProperty("materializedSet").isDelegateQuerySet("accessA")
						.whereDelegateQuery()
							.whereFroms(1).whereElementAt(0).isFrom(CompanyA.T)
							.withRestrictionWithoutPaging()
							.whereCondition().isConjunction(1)
								.whereElementAt(0).isValueComparison(Operator.like)
							.whereOrdering().isSimpleOrderingWhereValue(false).isPropertyOperand("id")
						.endQuery()
						.whereProperty("batchSize").isNotNull().close()
					.close()
					.whereProperty("querySet")
						.whereDelegateQuery()
							.whereFroms(1).whereElementAt(0).isFrom(PersonA.T)
							.whereCondition().isConjunction(2)
								.whereElementAt(0).isValueComparison(Operator.like).close()
								.whereElementAt(1).isDisjunction(1)
							.withouthOrdering()
						.endQuery()
					.close()
				.close()
			.close()
			.whereProperty("limit").is_(10)
			.whereProperty("offset").is_(0)
		;
		// @formatter:on
	}

	/** Similar to simple case ({@link #bulksDueToPagination()}), but with ordering (that doesn't change anything) */
	@Test
	public void bulksDueToPagination_WhenOrdered() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(SmartPersonA.T, "p")
					.join("p", "keyCompanyExternalDqj", "c")
				.orderBy().property("p", "nameA")
				.limit(10)
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(PaginatedSet.T).whereProperty("operand")
				.hasType(Projection.T).whereProperty("operand")
					.hasType(DelegateQueryJoin.T)
					.whereProperty("materializedSet").isDelegateQuerySet("accessA")
						.whereDelegateQuery()
							.whereFroms(1).whereElementAt(0).isFrom(PersonA.T)
							.withNoRestriction()
							.whereOrdering().isCascadeOrdering()
								.whereElementAt(0).isSimpleOrderingWhereValue(false).isPropertyOperand("nameA").close(2)
								.whereElementAt(1).isSimpleOrderingWhereValue(false).isPropertyOperand("id").close(2)
						.endQuery()
						.whereProperty("batchSize").isNotNull().close()
					.close()
					.whereProperty("querySet")
						.whereDelegateQuery()
							.whereFroms(1).whereElementAt(0).isFrom(CompanyA.T)
							.withouthOrdering()
						.endQuery()
					.close()
				.close()
			.close()
			.whereProperty("limit").is_(10)
			.whereProperty("offset").is_(0)
		;
		// @formatter:on
	}

	/** This cannot be optimized, we simply need all the data to find out what the first 10 results are. */
	@Test
	public void noBulksDueToTheNeedOfBeingSorted() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(SmartPersonA.T, "p")
					.join("p", "keyCompanyExternalDqj", "c")
				.where()
					.property("c", "nameA").like("C*")
				.orderBy().property("p", "nameA")
				.paging(10, 5)
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(PaginatedSet.T).whereProperty("operand")
				.hasType(Projection.T).whereProperty("operand")
					.hasType(OrderedSet.T).whereProperty("operand")
						.hasType(DelegateQueryJoin.T)
						.whereProperty("materializedSet").isDelegateQuerySet("accessA")
							.whereDelegateQuery()
								.whereFroms(1).whereElementAt(0).isFrom(CompanyA.T)
								.whereCondition().isConjunction(1)
									.whereElementAt(0).isValueComparison(Operator.like)
								.withouthOrdering()
							.endQuery()
							.whereProperty("batchSize").isNull().close()
						.close()
						.whereProperty("querySet")
							.whereDelegateQuery()
								.whereFroms(1).whereElementAt(0).isFrom(PersonA.T)
								.withouthOrdering()
							.endQuery()
						.close()
					.close()
					.whereProperty("sortCriteria").isListWithSize(1)
						.whereElementAt(0).isSortCriteriaAndValue(false).isTupleComponent_(6).close(3)
			.close()
			.whereProperty("limit").is_(10)
			.whereProperty("offset").is_(5)
		;
		// @formatter:on
	}

	/**
	 * Note that this plan is actually not correct. When ordering by id which is converted, we should actually order on
	 * smart level, after conversion is applied. This would however be extremely non-performant, cause we would have to
	 * load all the entities and only then apply the sorting.
	 */
	@Test
	public void bulksDueToNonDelegatableCondition() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonB.T, "p")
				.select("p")
				.where()
					.property("p", "convertedBirthDate").ge(new Date())
				.orderBy().property("p", "id")
				.paging(10, 5)
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(PaginatedSet.T).whereProperty("operand")
				.hasType(Projection.T).whereProperty("operand")
					.hasType(FilteredSet.T).whereProperty("operand")
						.isDelegateQuerySet("accessB")
							.whereDelegateQuery()
								.whereFroms(1).whereElementAt(0).isFrom(PersonB.T)
								.whereOrdering()
									.isSimpleOrderingWhereValue(false).isPropertyOperand("id")
							.endQuery()
							.whereProperty("batchSize").is_(100)
						.close()
					.whereProperty("filter").hasType(ValueComparison.T).close()
				.close()
			.close()
			.whereProperty("limit").is_(10)
			.whereProperty("offset").is_(5)
		;
		// @formatter:on
	}
}
