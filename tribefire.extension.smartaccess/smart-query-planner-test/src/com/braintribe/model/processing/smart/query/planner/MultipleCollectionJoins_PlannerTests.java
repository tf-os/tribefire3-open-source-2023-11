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

import com.braintribe.model.processing.query.smart.test.model.accessA.PersonA;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartPersonA;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartPersonB;
import com.braintribe.model.processing.smart.query.planner.base.AbstractSmartQueryPlannerTests;
import com.braintribe.model.processing.smart.query.planner.core.combination.DelegateQueryBuilder;
import com.braintribe.model.query.Operator;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.functions.ListIndex;
import com.braintribe.model.queryplan.filter.Equality;
import com.braintribe.model.queryplan.set.CartesianProduct;
import com.braintribe.model.queryplan.set.FilteredSet;
import com.braintribe.model.queryplan.set.Projection;

/**
 * 
 */
public class MultipleCollectionJoins_PlannerTests extends AbstractSmartQueryPlannerTests {

	/** {@code select n1, n2 from SmartPersonA p join p.nickNamesSetA n1 join p.nickNamesSetA n2} */
	@Test
	public void doulbeExplicitSetJoin() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
					.join("p", "nickNamesSetA", "n1")
					.join("p", "nickNamesSetA", "n2")
				.select("n1")
				.select("n2")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
		.hasType(Projection.T)
			.whereProperty("operand")
				.isDelegateQuerySet("accessA")
				.whereProperty("scalarMappings").isListWithSize(2).close() // retrieving both n1 and n2
				.whereDelegateQuery()
					.whereProperty("selections").isListWithSize(2)
						.whereElementAt(0).isSourceOnlyPropertyOperand()
							.whereSource().isJoin("nickNamesSetA").whereSource().isFrom(PersonA.T)
						.close(3) // closing whereElement and whereSource (2x) 
						.whereElementAt(1).isSourceOnlyPropertyOperand()
							.whereSource().isJoin("nickNamesSetA").whereSource().isFrom(PersonA.T)
				.endQuery()
			.close()
			.whereProperty("values").isListWithSize(2) // selecting both n1 and n2
		;
		// @formatter:on
	}

	/** {@code select n1, p.nickNamesSetA from SmartPersonA p join p.nickNamesSetA n1} */
	@Test
	public void explicitAndImplicitSetJoin() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
					.join("p", "nickNamesSetA", "n1")
				.select("n1")
				.select("p", "nickNamesSetA")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
		.hasType(Projection.T)
			.whereProperty("operand")
				.isDelegateQuerySet("accessA")
				.whereProperty("scalarMappings").isListWithSize(2).close() // retrieving both n1 and p.nickNamesSetA
				.whereDelegateQuery()
					.whereProperty("selections").isListWithSize(2)
						.whereElementAt(0).isSourceOnlyPropertyOperand()
							.whereSource().isJoin("nickNamesSetA").whereSource().isFrom(PersonA.T)
						.close(3) // closing whereElement and whereSource (2x) 
						.whereElementAt(1).isSourceOnlyPropertyOperand()
							.whereSource().isJoin("nickNamesSetA").whereSource().isFrom(PersonA.T)
				.endQuery()
			.close()
			.whereProperty("values").isListWithSize(2) // selecting both n1 and and p.nickNamesSetA
		;
		// @formatter:on
	}

	/** {@code select n1, p.nickNamesSetA from SmartPersonA p join p.nickNamesSetA n1 where 'Bob' in p.nickNamesSetA} */
	@Test
	public void explicitAndImplicitSetJoin_WithCondition() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
					.join("p", "nickNamesSetA", "n1")
				.select("n1")
				.select("p", "nickNamesSetA")
				.where()
					.value("Bob").in().property("p", "nickNamesSetA")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
		.hasType(Projection.T)
			.whereProperty("operand")
				.isDelegateQuerySet("accessA")
				.whereProperty("scalarMappings").isListWithSize(2).close() // retrieving both n1 and p.nickNamesSetA
				.whereDelegateQuery()
					.whereSelection(2)
						.whereElementAt(0).isSourceOnlyPropertyOperand()
							.whereSource().isJoin("nickNamesSetA").whereSource().isFrom(PersonA.T)
						.close(3) // closing whereElement and whereSource (2x) 
						.whereElementAt(1).isSourceOnlyPropertyOperand()
							.whereSource().isJoin("nickNamesSetA").whereSource().isFrom(PersonA.T)
					.whereCondition()
						.isSingleOperandConjunctionAndOperand()
							.isValueComparison(Operator.in)
								.whereProperty("leftOperand").is_("Bob")
								.whereProperty("rightOperand")
									.isPropertyOperand("nickNamesSetA")
									.whereSource().isFrom(PersonA.T)
				.endQuery()
			.close()
			.whereProperty("values").isListWithSize(2) // selecting both n1 and and p.nickNamesSetA
		;
		// @formatter:on
	}

	/** {@code select n1, p.nickNamesSetA from SmartPersonA p join p.nickNamesSetA n1 where 'Bob' in p.nickNamesSetA and n1 = 'Robbie'} */
	@Test
	public void explicitAndImplicitSetJoin_WithExplicitAndImplicitCondition() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
					.join("p", "nickNamesSetA", "n1")
				.select("n1")
				.select("p", "nickNamesSetA")
				.where()
					.conjunction()
						.value("Bob").in().property("p", "nickNamesSetA")
						.entity("n1").eq("Robbie")
					.close()
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
		.hasType(Projection.T)
			.whereProperty("operand")
				.isDelegateQuerySet("accessA")
				.whereProperty("scalarMappings").isListWithSize(2).close() // retrieving both n1 and p.nickNamesSetA
				.whereDelegateQuery()
					.whereSelection(2)
						.whereElementAt(0).isSourceOnlyPropertyOperand()
							.whereSource().isJoin("nickNamesSetA").whereSource().isFrom(PersonA.T)
					.whereCondition()
						.isConjunction(2)
							.whenOrderedBy("operator")
							.whereElementAt(0)
								.isValueComparison(Operator.equal)
									.whereProperty("leftOperand")
										.isSourceOnlyPropertyOperand()
										.whereSource().isJoin("nickNamesSetA")
										.whereSource().isFrom(PersonA.T)
									.close(3)
									.whereProperty("rightOperand").is_("Robbie")
								.close()
							.whereElementAt(1)
								.isValueComparison(Operator.in)
									.whereProperty("leftOperand").is_("Bob")
									.whereProperty("rightOperand")
										.isPropertyOperand("nickNamesSetA")
										.whereSource().isFrom(PersonA.T)
				.endQuery()
			.close()
			.whereProperty("values").isListWithSize(2) // selecting both n1 only
		;
		// @formatter:on
	}

	/** {@code select n1 from SmartPersonA p join p.nickNamesSetA n1, SmartPersonB pb where pb.nameB in p.nickNamesSetA} */
	@Test
	public void explicitSetJoin_WithNonDelegatableCondition() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
					.join("p", "nickNamesSetA", "n1")
				.from(SmartPersonB.T, "pb")
				.select("n1")
				.where()
					.property("pb", "nameB").in().property("p", "nickNamesSetA")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
		.hasType(Projection.T)
			.whereProperty("operand")
				.hasType(FilteredSet.T)
				.whereProperty("operand")
					.hasType(CartesianProduct.T)
					.whereProperty("operands").isListWithSize(2)
						.whereElementAt(0)
							.isDelegateQuerySet("accessA")
							.whereProperty("scalarMappings").isListWithSize(2).close() // retrieving both n1 and p.nickNamesSetA
							.whereDelegateQuery()
								.whereSelection(2)
									.whereElementAt(0).isSourceOnlyPropertyOperand()
										.whereSource().isJoin("nickNamesSetA").whereSource().isFrom(PersonA.T)
									.close(3)
									.whereElementAt(1).isSourceOnlyPropertyOperand()
										.whereSource().isJoin("nickNamesSetA").whereSource().isFrom(PersonA.T)
									.close(3)
								.endQuery()
							.close()
						.close()
					.close()
				.whereProperty("filter")
					.hasType(Equality.T)
					.whereProperty("leftOperand").isTupleComponent_(2)
					// right operand is either 0 or 1
				.close()
			.close()
			.whereProperty("values").isListWithSize(1).close() // selecting both n1 and and p.nickNamesSetA
		;
		// @formatter:on
	}

	/** {@code select n1 from SmartPersonA p join p.nickNamesSetA n1, SmartPersonB pb where pb.nameB in p.nickNamesSetA and 'Bob' in p.nickNamesSetA } */
	@Test
	public void explicitSetJoin_WithNonDelegatable_AndAlsoDelegatable_Condition() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
					.join("p", "nickNamesSetA", "n1")
				.from(SmartPersonB.T, "pb")
				.select("n1")
				.where()
					.conjunction()
						.property("pb", "nameB").in().property("p", "nickNamesSetA")
						.value("Bob").in().property("p", "nickNamesSetA")
					.close()
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
		.hasType(Projection.T)
			.whereProperty("operand")
				.hasType(FilteredSet.T)
				.whereProperty("operand")
					.hasType(CartesianProduct.T)
					.whereProperty("operands").isListWithSize(2)
						.whereElementAt(0)
							.isDelegateQuerySet("accessA")
							.whereProperty("scalarMappings").isListWithSize(2).close() // retrieving both n1 and p.nickNamesSetA
							.whereDelegateQuery()
								.whereSelection(2)
									.whereElementAt(0).isSourceOnlyPropertyOperand()
										.whereSource().isJoin("nickNamesSetA").whereSource().isFrom(PersonA.T)
									.close(3)
									.whereElementAt(1).isSourceOnlyPropertyOperand()
										.whereSource().isJoin("nickNamesSetA").whereSource().isFrom(PersonA.T)
									.close(3)
								.whereCondition()
									.isSingleOperandConjunctionAndOperand()
										.isValueComparison(Operator.in)
											.whereProperty("leftOperand").is_("Bob")
											.whereProperty("rightOperand")
												.isPropertyOperand("nickNamesSetA")
												.whereSource().isFrom(PersonA.T)
								.endQuery()
							.close()
						.close()
					.close()
				.whereProperty("filter")
					.hasType(Equality.T)
					.whereProperty("leftOperand").isTupleComponent_(2)
					// right operand is either 0 or 1
				.close()
			.close()
			.whereProperty("values").isListWithSize(1).close() // selecting both n1 and and p.nickNamesSetA
		;
		// @formatter:on
	}

	// ###################################################
	// ## . . . . . Join Functions Conditions . . . . . ##
	// ###################################################

	/** {@code select n1 from SmartPersonA p join p.nickNamesSetA n1, SmartPersonB pb where pb.ageB = index(n1)} */
	@Test
	public void explicitListJoin_WithDelegatable_ListIndexCondition() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
					.join("p", "nickNamesListA", "n1")
				.select("n1")
				.where()
					.listIndex("n1").eq(1)
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
		.hasType(Projection.T)
			.whereProperty("operand")
				.isDelegateQuerySet("accessA")
				.whereProperty("scalarMappings").isListWithSize(1).close() // retrieving both n1 and p.nickNamesSetA
				.whereDelegateQuery()
					.whereSelection(1)
						.whereElementAt(0).isSourceOnlyPropertyOperand()
							.whereSource().isJoin("nickNamesListA").whereSource().isFrom(PersonA.T)
						.close(3)
						.whereElementAt(0).isSourceOnlyPropertyOperand()
							.whereSource().isJoin("nickNamesListA").whereSource().isFrom(PersonA.T)
						.close(3)
					.whereCondition().isSingleOperandConjunctionAndOperand()
							.isValueComparison(Operator.equal)
								.whereProperty("leftOperand").hasType(ListIndex.T).close()
								.whereProperty("rightOperand").is_(1)
				.endQuery()
			.close()
			.whereProperty("values").isListWithSize(1).whereElementAt(0).isTupleComponent_(0)
		;
		// @formatter:on
	}

	/**
	 * {@code select n1 from SmartPersonA p join p.nickNamesSetA n1, SmartPersonB pb where index(n1) = index(n1)}
	 * 
	 * Same as {@link #explicitListJoin_WithDelegatable_ListIndexCondition()}, but we force that list-index is also
	 * delegated as right operand (Just to test the {@link DelegateQueryBuilder}).
	 * */
	@Test
	public void explicitListJoin_WithDelegatable_ListIndexCondition_CheckingIndexAsRightOperandIsEncodec() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
					.join("p", "nickNamesListA", "n1")
				.select("n1")
				.where()
					.listIndex("n1").eq().listIndex("n1")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
		.hasType(Projection.T)
			.whereProperty("operand")
				.isDelegateQuerySet("accessA")
				.whereDelegateQuery()
					.whereCondition().isSingleOperandConjunctionAndOperand()
						.isValueComparison(Operator.equal)
							.whereProperty("leftOperand").hasType(ListIndex.T).close()
							.whereProperty("rightOperand").hasType(ListIndex.T).close()
		;
		// @formatter:on
	}

	/** {@code select n1 from SmartPersonA p join p.nickNamesSetA n1, SmartPersonB pb where pb.ageB = index(n1)} */
	@Test
	public void explicitListJoin_WithNonDelegatable_ListIndexCondition() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
					.join("p", "nickNamesListA", "n1")
				.from(SmartPersonB.T, "pb")
				.select("n1")
				.where()
					.listIndex("n1").eq().property("pb", "ageB")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
		.hasType(Projection.T)
			.whereProperty("operand")
				.hasType(FilteredSet.T)
				.whereProperty("operand")
					.hasType(CartesianProduct.T)
					.whereProperty("operands").isListWithSize(2)
						.whereElementAt(0)
							.isDelegateQuerySet("accessA")
							.whereProperty("scalarMappings").isListWithSize(2).close() // retrieving both n1 and p.nickNamesSetA
							.whereDelegateQuery()
								.whereSelection(2)
									.whereElementAt(0).isSourceOnlyPropertyOperand()
										.whereSource().isJoin("nickNamesListA").whereSource().isFrom(PersonA.T)
									.close(3)
									.whereElementAt(1).hasType(ListIndex.T)
										.whereProperty("join").isJoin("nickNamesListA").whereSource().isFrom(PersonA.T)
									.close(3)
								.endQuery()
							.close()
						.close()
					.close()
				.whereProperty("filter")
					.hasType(Equality.T)
					.whereProperty("leftOperand").isTupleComponent_(1)
					.whereProperty("rightOperand").isTupleComponent_(2)
					// right operand is either 0 or 1
				.close()
			.close()
			.whereProperty("values").isListWithSize(1).whereElementAt(0).isTupleComponent_(0)
		;
		// @formatter:on
	}

	/** {@code select n1 from SmartPersonA p join p.nickNamesSetA n1, SmartPersonB pb where pb.ageB = index(n1) and 'Bob' in pb.nickNamesListA } */
	@Test
	public void explicitListJoin_WithNonDelegatableListIndex_AndOtherDelegatable_Condition() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
					.join("p", "nickNamesListA", "n1")
				.from(SmartPersonB.T, "pb")
				.select("n1")
				.where()
					.conjunction()
						.listIndex("n1").eq().property("pb", "ageB")
						.value("Bob").in().property("p", "nickNamesListA")
					.close()
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
		.hasType(Projection.T)
			.whereProperty("operand")
				.hasType(FilteredSet.T)
				.whereProperty("operand")
					.hasType(CartesianProduct.T)
					.whereProperty("operands").isListWithSize(2)
						.whereElementAt(0)
							.isDelegateQuerySet("accessA")
							.whereProperty("scalarMappings").isListWithSize(2).close() // retrieving both n1 and p.nickNamesSetA
							.whereDelegateQuery()
								.whereSelection(2)
									.whereElementAt(0).isSourceOnlyPropertyOperand()
										.whereSource().isJoin("nickNamesListA").whereSource().isFrom(PersonA.T)
									.close(3)
									.whereElementAt(1).hasType(ListIndex.T)
										.whereProperty("join").isJoin("nickNamesListA").whereSource().isFrom(PersonA.T)
									.close(3)
								.whereCondition()
									.isSingleOperandConjunctionAndOperand()
										.isValueComparison(Operator.in)
											.whereProperty("leftOperand").is_("Bob")
											.whereProperty("rightOperand")
												.isPropertyOperand("nickNamesListA")
												.whereSource().isFrom(PersonA.T)
								.endQuery()
							.close()
						.close()
					.close()
				.whereProperty("filter")
					.hasType(Equality.T)
					.whereProperty("leftOperand").isTupleComponent_(1)
					.whereProperty("rightOperand").isTupleComponent_(2)
					// right operand is either 0 or 1
				.close()
			.close()
			.whereProperty("values").isListWithSize(1).whereElementAt(0).isTupleComponent_(0)
		;
		// @formatter:on
	}
}
