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
package com.braintribe.model.processing.query.test;

import static com.braintribe.model.query.OrderingDirection.ascending;
import static com.braintribe.model.query.OrderingDirection.descending;

import org.junit.Test;

import com.braintribe.model.processing.query.test.model.Address;
import com.braintribe.model.processing.query.test.model.Company;
import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.queryplan.index.GeneratedMetricIndex;
import com.braintribe.model.queryplan.set.CartesianProduct;
import com.braintribe.model.queryplan.set.FilteredSet;
import com.braintribe.model.queryplan.set.MergeLookupJoin;
import com.braintribe.model.queryplan.set.MergeRangeJoin;
import com.braintribe.model.queryplan.set.OrderedSet;
import com.braintribe.model.queryplan.set.OrderedSetRefinement;
import com.braintribe.model.queryplan.set.Projection;
import com.braintribe.model.queryplan.set.SortCriterion;
import com.braintribe.model.queryplan.set.join.IndexLookupJoin;

/**
 * 
 */
public class OrderByIndexedTests extends AbstractQueryPlannerTests {

	@Test
	public void orderById() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.orderBy(ascending).property("p", "id")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T)
			.whereOperand()
				.isIndexOrderedSet_(Person.T, "id", false)
		;
		// @formatter:on
	}

	@Test
	public void orderById_Desc() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.orderBy(descending).property("p", "id")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T)
			.whereOperand()
				.isIndexOrderedSet_(Person.T, "id", true)
		;
		// @formatter:on
	}

	@Test
	public void orderByIndexedAndOther() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.orderByCascade()
					.property("p", "indexedInteger")
					.property("p", "name")
				.close()
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T)
			.whereOperand()
				.hasType(OrderedSetRefinement.T)
				.whereOperand().isIndexOrderedSet_(Person.T, "indexedInteger", false)
				.whereProperty("sortCriteria").isListWithSize(1)
					.whereElementAt(0)
						.hasType(SortCriterion.T)
						.whereProperty("descending").isFalse_()
						.whereValue().isValueProperty_("name")
					.close() // closes sortCriteria[0]
				.close() // closes sortCriteria
				.whereProperty("groupValues").isListWithSize(1)
					.whereElementAt(0)
						.isValueProperty_("indexedInteger")
					
		;
		// @formatter:on
	}

	@Test
	public void orderByIndexed_WithNonIndexedCondition() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.where()
					.property("p", "companyName").eq("Tesla")
				.orderBy(ascending).property("p", "id")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T)
			.whereOperand()
				.hasType(FilteredSet.T)
				.whereOperand()
					.isIndexOrderedSet_(Person.T, "id", false)
		;
		// @formatter:on
	}

	@Test
	public void orderByIndexed_WithNonIndexedCondition_Desc() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.where()
					.property("p", "companyName").eq("Tesla")
				.orderBy(descending).property("p", "id")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T)
			.whereOperand()
				.hasType(FilteredSet.T)
				.whereOperand()
					.isIndexOrderedSet_(Person.T, "id", true)
		;
		// @formatter:on
	}

	@Test
	public void orderByIndexed_WithIndexedCondition() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.where()
					.property("p", "indexedInteger").eq(666)
				.orderBy(descending).property("p", "id")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T)
			.whereOperand()
				.hasType(OrderedSet.T)
				.whereOperand()
					.isIndexSubSet(Person.T, "indexedInteger")
		;
		// @formatter:on
	}

	@Test
	public void orderByIndexed_CartesianProduct() {
		// @formatter:off
		SelectQuery selectQuery = query()
				// random order, the final inside CP should be P C A, as is the ordering
				.from(Address.T, "a")
				.from(Person.T, "p")
				.from(Company.T, "c")
				.orderByCascade()
					.property("p", "id")
					.property("c", "id")
					.property("a", "id")
				.close()
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T)
			.whereOperand()
				.hasType(CartesianProduct.T)
					.whereProperty("operands").isListWithSize(3)
						.whereElementAt(0)
							.isIndexOrderedSet_(Person.T, "id", false)
						.whereElementAt(1)
							.isIndexOrderedSet_(Company.T, "id", false)
						.whereElementAt(2)
							.isIndexOrderedSet_(Address.T, "id", false)
						.close()
		;
		// @formatter:on
	}

	// ################################################
	// ## . . . . . . . . IndexJoins . . . . . . . . ##
	// ################################################

	@Test
	public void orderByIndexed_WithIndexJoin_OrderByJoinedSide_NoIos() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Company.T, "c")
				.from(Person.T, "p")
				.where()
					.property("p", "indexedName").eq().property("c", "name") // this makes no semantic sense of course..
				.orderBy(ascending).property("p", "id")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T)
			.whereOperand()
				.hasType(OrderedSet.T)
				.whereOperand()
					.hasType(IndexLookupJoin.T)
					.whereOperand()
						.isSourceSet_(Company.T)
		;
		// @formatter:on
	}

	@Test
	public void orderByIndexed_WithIndexJoin_OrderBySourceSide_YesIos() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Company.T, "c")
				.from(Person.T, "p")
				.where()
					.property("p", "indexedName").eq().property("c", "name") // this makes no semantic sense of course..
				.orderBy(ascending).property("c", "id")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T)
			.whereOperand()
				.hasType(IndexLookupJoin.T)
				.whereOperand()
					.isIndexOrderedSet_(Company.T, "id", false)
		;
		// @formatter:on
	}

	// ################################################
	// ## . . . . . . . . MergeJoins . . . . . . . . ##
	// ################################################

	@Test
	public void orderByIndexed_WithMergeJoin_OrderByOneSide_YesIos() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Company.T, "c")
				.from(Person.T, "p")
				.where()
					.property("p", "companyName").eq().property("c", "name")
				.orderBy(ascending).property("c", "id")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T)
			.whereOperand()
				.hasType(MergeLookupJoin.T)
				.whereOperand().isIndexOrderedSet_(Company.T, "id", false)
				.whereProperty("otherOperand")
					.isSourceSet_(Person.T)
		;
		// @formatter:on
	}

	@Test
	public void orderByIndexed_WithMergeJoin_OrderByOtherSide_YesIos() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Company.T, "c")
				.from(Person.T, "p")
				.where()
					.property("p", "companyName").eq().property("c", "name")
				.orderBy(ascending).property("p", "id")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T)
			.whereOperand()
				.hasType(MergeLookupJoin.T)
				.whereOperand().isIndexOrderedSet_(Person.T, "id", false)
				.whereProperty("otherOperand")
					.isSourceSet_(Company.T)
		;
		// @formatter:on
	}

	@Test
	public void orderByIndexed_WithMergeLookupJoin_OrderByBothSides_SomeIos() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Company.T, "c")
				.from(Person.T, "p")
				.where()
					.property("p", "companyName").eq().property("c", "name")
				.orderByCascade()
					.dir(ascending).property("p", "indexedName")
					.dir(ascending).property("c", "id")
				.close()
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T)
			.whereOperand()
				.hasType(OrderedSetRefinement.T)
				.whereOperand()
					.hasType(MergeLookupJoin.T)
					.whereOperand().isIndexOrderedSet_(Person.T, "indexedName", false)
				.close()
				.whereProperty("sortCriteria").isListWithSize(1)
					.whereElementAt(0)
						.hasType(SortCriterion.T)
						.whereProperty("descending").isFalse_()
						.whereValue().isValueProperty_("id")
					.close() // closes sortCriteria[0]
				.close() // closes sortCriteria
				.whereProperty("groupValues").isListWithSize(1)
					.whereElementAt(0)
						.isValueProperty_("indexedName")
					
		;
		// @formatter:on
	}

	@Test
	public void orderByIndexed_WithMergeLookupJoinAndCartesianProduct_OrderByBothSides_SomeIos() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Company.T, "c")
				.from(Person.T, "p")
				.from(Address.T, "a")
				.where()
					.property("p", "companyName").eq().property("c", "name")
				.orderByCascade()
					.dir(ascending).property("p", "indexedName")
					.dir(ascending).property("c", "id")
					.dir(descending).property("a", "id")
				.close()
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T)
			.whereOperand()
				.hasType(OrderedSetRefinement.T)
				.whereOperand()
					.hasType(CartesianProduct.T)
					.whereProperty("operands").isListWithSize(2)
						.whereElementAt(0)
							.hasType(MergeLookupJoin.T)
							.whereOperand().isIndexOrderedSet_(Person.T, "indexedName", false)
						.close()
						.whereElementAt(1)
							.isSourceSet_(Address.T)
					.close()
				.close()
				.whereProperty("sortCriteria").isListWithSize(2)
					.whereElementAt(0)
						.hasType(SortCriterion.T)
						.whereProperty("descending").isFalse_()
						.whereValue().isValueProperty_("id")
					.close() // closes sortCriteria[0]
					.whereElementAt(1)
						.hasType(SortCriterion.T)
						.whereProperty("descending").isTrue_()
						.whereValue().isValueProperty_("id")
					.close() // closes sortCriteria[1]
				.close() // closes sortCriteria
				.whereProperty("groupValues").isListWithSize(1)
					.whereElementAt(0)
						.isValueProperty_("indexedName")
					
		;
		// @formatter:on
	}

	@Test
	public void orderByIndexed_WithMergeRangeJoin_OrderByBothSides_SomeIos() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.from(Company.T, "c")
				.where()
					.property("p", "companyName").gt().property("c", "name")
				.orderByCascade()
					.dir(ascending).property("p", "indexedName")
					.dir(ascending).property("c", "id")
				.close()
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T)
			.whereOperand()
				.hasType(OrderedSetRefinement.T)
				.whereOperand()
					.hasType(MergeRangeJoin.T)
					.whereOperand().isIndexOrderedSet_(Person.T, "indexedName", false)
					.whereProperty("index")
						.hasType(GeneratedMetricIndex.T)
						.whereOperand().isSourceSet_(Company.T)
					.close() // index
				.close() // OrderedSetRefinement.operand
				.whereProperty("sortCriteria").isListWithSize(1)
					.whereElementAt(0)
						.hasType(SortCriterion.T)
						.whereProperty("descending").isFalse_()
						.whereValue().isValueProperty_("id")
					.close() // closes sortCriteria[0]
				.close() // closes sortCriteria
				.whereProperty("groupValues").isListWithSize(1)
					.whereElementAt(0)
						.isValueProperty_("indexedName")
					
		;
		// @formatter:on
	}

}