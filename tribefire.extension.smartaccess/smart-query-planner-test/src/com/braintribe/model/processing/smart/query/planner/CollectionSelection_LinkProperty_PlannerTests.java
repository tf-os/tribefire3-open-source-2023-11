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

import static com.braintribe.model.processing.query.smart.test.setup.base.SmartMappingSetup.accessIdB;

import org.junit.Test;

import com.braintribe.model.processing.query.smart.test.model.accessA.PersonA;
import com.braintribe.model.processing.query.smart.test.model.accessB.ItemB;
import com.braintribe.model.processing.query.smart.test.model.accessB.PersonItemOrderedLink;
import com.braintribe.model.processing.query.smart.test.model.accessB.PersonItemSetLink;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartItem;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartPersonA;
import com.braintribe.model.processing.smart.query.planner.base.AbstractSmartQueryPlannerTests;
import com.braintribe.model.query.Operator;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.queryplan.filter.Disjunction;
import com.braintribe.model.queryplan.filter.GreaterThanOrEqual;
import com.braintribe.model.queryplan.filter.Like;
import com.braintribe.model.queryplan.set.FilteredSet;
import com.braintribe.model.queryplan.set.Projection;
import com.braintribe.model.queryplan.value.QueryFunctionValue;
import com.braintribe.model.smartqueryplan.set.DelegateQueryJoin;

/**
 * 
 */
public class CollectionSelection_LinkProperty_PlannerTests extends AbstractSmartQueryPlannerTests {

	@Test
	public void simpleSetQuery() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
				.select("p", "linkItems")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T)
			.whereProperty("operand")
				.hasType(DelegateQueryJoin.T)
				.whereProperty("materializedSet")
					.hasType(DelegateQueryJoin.T)
					.whereProperty("materializedSet")
						.isDelegateQuerySet("accessA")
						.whereDelegateQuery()
							.whereSelection(1)
								.whereElementAt(0).isPropertyOperand("nameA").whereSource().isFrom(PersonA.T)
						.endQuery()
					.close()
					.whereProperty("querySet")
						.isDelegateQuerySet("accessB")
						.whereDelegateQuery()
							.whereSelection(2)
								.whereElementAt(0).isPropertyOperand("itemName").whereSource().isFrom(PersonItemSetLink.T).close(2)
								.whereElementAt(1).isPropertyOperand("personName").whereSource().isFrom(PersonItemSetLink.T).close(2)
						.endQuery()
					.close()
				.close()
				.whereProperty("querySet")
					.isDelegateQuerySet("accessB")
					.whereDelegateQuery()
						.whereSelection(5)
							.whereElementAt(1).isPropertyOperand("id").whereSource().isFrom(ItemB.T)
					.endQuery()
				.close()
			.close()
			.whereProperty("values").isListWithSize(1)
				.whereElementAt(0).hasType(QueryFunctionValue.T).whereProperty("queryFunction").isAssembleEntity(SmartItem.T)
		;
		// @formatter:on
	}

	@Test
	public void queryWithDelegatableSetCondition() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
				.select("p", "nameA")
				.where()
					.entity(item(99L)).in().property("p", "linkItems")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T)
			.whereProperty("operand")
				.hasType(DelegateQueryJoin.T)
				.whereProperty("materializedSet")
					.hasType(DelegateQueryJoin.T)
					.whereProperty("materializedSet")
						.isDelegateQuerySet("accessB")
						.whereDelegateQuery()
							.whereSelection(1)
								.whereElementAt(0).isPropertyOperand("nameB").whereSource().isFrom(ItemB.T)
							.whereCondition().isConjunction(1)
								.whereElementAt(0).isValueComparison(Operator.equal)
									.whereProperty("leftOperand").isReference_(ItemB.T, 99L, accessIdB)
									.whereProperty("rightOperand").isSourceOnlyPropertyOperand().close()
						.endQuery()
					.close()
					.whereProperty("querySet")
						.isDelegateQuerySet("accessB")
						.whereDelegateQuery()
							.whereSelection(2)
								.whereElementAt(0).isPropertyOperand("itemName").whereSource().isFrom(PersonItemSetLink.T).close(2)
								.whereElementAt(1).isPropertyOperand("personName").whereSource().isFrom(PersonItemSetLink.T).close(2)
						.endQuery()
					.close()
				.close()
				.whereProperty("querySet")
					.isDelegateQuerySet("accessA")
					.whereDelegateQuery()
						.whereSelection(1)
							.whereElementAt(0).isPropertyOperand("nameA").whereSource().isFrom(PersonA.T)
					.endQuery()
				.close()
			.close()
			.whereProperty("values").isListWithSize(1)
				.whereElementAt(0).isTupleComponent_(1)
		;
		// @formatter:on
	}

	@Test
	public void setQueryWithDelegatableSetCondition() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
				.select("p", "nameA")
				.select("p", "linkItems")
				.where()
					.entity(item(99L)).in().property("p", "linkItems")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T)
			.whereProperty("operand")
				.hasType(DelegateQueryJoin.T)
				.whereProperty("materializedSet")
					.hasType(DelegateQueryJoin.T)
					.whereProperty("materializedSet")
						.hasType(DelegateQueryJoin.T)
						.whereProperty("materializedSet")
							.hasType(DelegateQueryJoin.T)
							.whereProperty("materializedSet")
								.isDelegateQuerySet("accessB")
								.whereDelegateQuery()
									.whereSelection(1)
										.whereElementAt(0).isPropertyOperand("nameB").whereSource().isFrom(ItemB.T)
									.whereCondition().isConjunction(1)
										.whereElementAt(0).isValueComparison(Operator.equal)
											.whereProperty("leftOperand").isReference_(ItemB.T, 99L, accessIdB)
											.whereProperty("rightOperand").isSourceOnlyPropertyOperand().close()
								.endQuery()
							.close()
							.whereProperty("querySet")
								.isDelegateQuerySet("accessB")
								.whereDelegateQuery()
									.whereSelection(2)
										.whereElementAt(0).isPropertyOperand("itemName").whereSource().isFrom(PersonItemSetLink.T).close(2)
										.whereElementAt(1).isPropertyOperand("personName").whereSource().isFrom(PersonItemSetLink.T).close(2)
								.endQuery()
							.close()
						.close()
						.whereProperty("querySet")
							.isDelegateQuerySet("accessA")
							.whereDelegateQuery()
								.whereSelection(1)
									.whereElementAt(0).isPropertyOperand("nameA").whereSource().isFrom(PersonA.T)
							.endQuery()
						.close()
					.close()
					.whereProperty("querySet")
						.isDelegateQuerySet("accessB")
						.whereDelegateQuery()
							.whereSelection(2)
								.whereElementAt(0).isPropertyOperand("itemName").whereSource().isFrom(PersonItemSetLink.T).close(2)
								.whereElementAt(1).isPropertyOperand("personName").whereSource().isFrom(PersonItemSetLink.T).close(2)
						.endQuery()
					.close()
				.close()
				.whereProperty("querySet")
					.isDelegateQuerySet("accessB")
					.whereDelegateQuery()
						.whereSelection(5)
							.whereElementAt(1).isPropertyOperand("id").whereSource().isFrom(ItemB.T)
					.endQuery()
				.close()
			.close()
			.whereProperty("values").isListWithSize(2)
				.whereElementAt(0).isTupleComponent_(1)
				.whereElementAt(1).hasType(QueryFunctionValue.T).whereProperty("queryFunction").isAssembleEntity(SmartItem.T)
		;
		// @formatter:on
	}

	@Test
	public void simpleListQuery() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
				.select("p", "orderedLinkItems")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T)
			.whereProperty("operand")
				.hasType(DelegateQueryJoin.T)
				.whereProperty("materializedSet")
					.hasType(DelegateQueryJoin.T)
					.whereProperty("materializedSet")
						.isDelegateQuerySet("accessA")
						.whereDelegateQuery()
							.whereSelection(1)
								.whereElementAt(0).isPropertyOperand("nameA").whereSource().isFrom(PersonA.T)
						.endQuery()
					.close()
					.whereProperty("querySet")
						.isDelegateQuerySet("accessB")
						.whereDelegateQuery()
							.whereSelection(2)
								.whereElementAt(0).isPropertyOperand("itemName").whereSource().isFrom(PersonItemOrderedLink.T).close(2)
								.whereElementAt(1).isPropertyOperand("personName").whereSource().isFrom(PersonItemOrderedLink.T).close(2)
						.endQuery()
					.close()
				.close()
				.whereProperty("querySet")
					.isDelegateQuerySet("accessB")
					.whereDelegateQuery()
						.whereSelection(5)
							.whereElementAt(1).isPropertyOperand("id").whereSource().isFrom(ItemB.T)
					.endQuery()
				.close()
			.close()
			.whereProperty("values").isListWithSize(1)
				.whereElementAt(0).hasType(QueryFunctionValue.T).whereProperty("queryFunction").isAssembleEntity(SmartItem.T)
		;
		// @formatter:on
	}

	@Test
	public void listQueryWithListIndex() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
					.join("p", "orderedLinkItems", "i")
				.select().listIndex("i")
				.select("i")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T)
			.whereProperty("operand")
				.hasType(DelegateQueryJoin.T)
				.whereProperty("materializedSet")
					.hasType(DelegateQueryJoin.T)
					.whereProperty("materializedSet")
						.isDelegateQuerySet("accessA")
						.whereDelegateQuery()
							.whereSelection(1)
								.whereElementAt(0).isPropertyOperand("nameA").whereSource().isFrom(PersonA.T)
						.endQuery()
					.close()
					.whereProperty("querySet")
						.isDelegateQuerySet("accessB")
						.whereDelegateQuery()
							.whereSelection(3)
								.whereElementAt(0).isPropertyOperand("itemIndex").whereSource().isFrom(PersonItemOrderedLink.T).close(2)
								.whereElementAt(1).isPropertyOperand("itemName").whereSource().isFrom(PersonItemOrderedLink.T).close(2)
								.whereElementAt(2).isPropertyOperand("personName").whereSource().isFrom(PersonItemOrderedLink.T).close(2)
						.endQuery()
					.close()
				.close()
				.whereProperty("querySet")
					.isDelegateQuerySet("accessB")
					.whereDelegateQuery()
						.whereSelection(5)
							.whereElementAt(1).isPropertyOperand("id").whereSource().isFrom(ItemB.T)
					.endQuery()
				.close()
			.close()
			.whereProperty("values").isListWithSize(2)
				.whereElementAt(0).isTupleComponent_(1)
				.whereElementAt(1).hasType(QueryFunctionValue.T).whereProperty("queryFunction").isAssembleEntity(SmartItem.T)
		;
		// @formatter:on
	}

	@Test
	public void listQueryWithListIndexCondition() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
					.join("p", "orderedLinkItems", "i")
				.select().listIndex("i")
				.select("i")
				.where()
					.listIndex("i").ge(10)
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T)
			.whereProperty("operand")
				.hasType(DelegateQueryJoin.T)
				.whereProperty("materializedSet")
					.hasType(DelegateQueryJoin.T)
					.whereProperty("materializedSet")
						.isDelegateQuerySet("accessB")
						.whereDelegateQuery()
							.whereFroms(1)
								.whereElementAt(0).isFrom(PersonItemOrderedLink.T)
							.whereSelection(3)
								.whereElementAt(0).isPropertyOperand("itemIndex").close()
								.whereElementAt(1).isPropertyOperand("itemName").close()
								.whereElementAt(2).isPropertyOperand("personName").close()
							.whereCondition()
								.isConjunction(1).whereElementAt(0)
									.isValueComparison(Operator.greaterOrEqual)
										.whereProperty("leftOperand").isPropertyOperand("itemIndex").close()
										.whereProperty("rightOperand").is_(10)
						.endQuery()
					.close()
					.whereProperty("querySet")
						.isDelegateQuerySet("accessA")
						.whereDelegateQuery()
							.whereSelection(1)
								.whereElementAt(0).isPropertyOperand("nameA").whereSource().isFrom(PersonA.T)
						.endQuery()
					.close()
				.close()
				.whereProperty("querySet")
					.isDelegateQuerySet("accessB")
					.whereDelegateQuery()
						.whereSelection(5)
							.whereElementAt(1).isPropertyOperand("id").whereSource().isFrom(ItemB.T)
					.endQuery()
				.close()
			.close()
			.whereProperty("values").isListWithSize(2)
				.whereElementAt(0).isTupleComponent_(0)
				.whereElementAt(1).hasType(QueryFunctionValue.T).whereProperty("queryFunction").isAssembleEntity(SmartItem.T)
		;
		// @formatter:on
	}

	@Test
	public void listQueryWithListIndexCondition_NonDelegateable() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
					.join("p", "orderedLinkItems", "i")
				.select().listIndex("i")
				.select("i")
				.where()
					.disjunction()
						.listIndex("i").ge(10)
						.property("p", "nameA").like("* Smith")
					.close()
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T)
			.whereProperty("operand")
				.hasType(FilteredSet.T)
				.whereProperty("filter")
					.hasType(Disjunction.T)
					.whereProperty("operands").isListWithSize(2)
						.whereElementAt(0).hasType(GreaterThanOrEqual.T).close()
						.whereElementAt(1).hasType(Like.T) .close()
					.close()
				.close()
				.whereProperty("operand")
					.hasType(DelegateQueryJoin.T)
					.whereProperty("materializedSet")
						.hasType(DelegateQueryJoin.T)
						.whereProperty("materializedSet")
							.isDelegateQuerySet("accessA")
							.whereDelegateQuery()
								.whereSelection(1)
									.whereElementAt(0).isPropertyOperand("nameA").whereSource().isFrom(PersonA.T)
							.endQuery()
						.close()
						.whereProperty("querySet")
							.isDelegateQuerySet("accessB")
							.whereDelegateQuery()
								.whereFroms(1)
									.whereElementAt(0).isFrom(PersonItemOrderedLink.T)
								.whereSelection(3)
									.whereElementAt(0).isPropertyOperand("itemIndex").close()
									.whereElementAt(1).isPropertyOperand("itemName").close()
									.whereElementAt(2).isPropertyOperand("personName").close()
							.endQuery()
						.close()
					.close()
					.whereProperty("querySet")
						.isDelegateQuerySet("accessB")
						.whereDelegateQuery()
							.whereSelection(5)
								.whereElementAt(1).isPropertyOperand("id").whereSource().isFrom(ItemB.T)
						.endQuery()
					.close()
				.close()
			.close()
			.whereProperty("values").isListWithSize(2)
				.whereElementAt(0).isTupleComponent_(1)
				.whereElementAt(1).hasType(QueryFunctionValue.T).whereProperty("queryFunction").isAssembleEntity(SmartItem.T)
		;
		// @formatter:on
	}
}
