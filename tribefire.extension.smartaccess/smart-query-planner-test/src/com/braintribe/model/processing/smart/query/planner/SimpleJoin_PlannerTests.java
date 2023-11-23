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

import static com.braintribe.model.generic.GenericEntity.globalId;
import static com.braintribe.model.generic.GenericEntity.partition;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.query.smart.test.model.accessA.PersonA;
import com.braintribe.model.processing.query.smart.test.model.accessB.PersonB;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartPersonA;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartPersonB;
import com.braintribe.model.processing.smart.query.planner.base.AbstractSmartQueryPlannerTests;
import com.braintribe.model.query.Operator;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.queryplan.set.Projection;
import com.braintribe.model.queryplan.value.QueryFunctionValue;
import com.braintribe.model.smartqueryplan.set.DelegateQueryJoin;
import com.braintribe.testing.category.KnownIssue;

/**
 * 
 */
public class SimpleJoin_PlannerTests extends AbstractSmartQueryPlannerTests {

	@Test
	public void simpleJoinSelectPrimitiveProps() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.select("p", "nameA")
				.select("c", "nameA")
				.from(SmartPersonA.T, "p")
					.join("p", "companyA", "c")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
		.hasType(Projection.T)
			.whereProperty("operand")
				.isDelegateQuerySet("accessA")
				.whereDelegateQuery()
					.whereFroms(1).whereElementAt(0).isFrom(PersonA.T)
						.whereProperty("joins").isSetWithSize(1).whereFirstElement().isJoin("companyA")
					.whereSelection(2)
						.whereElementAt(0).isPropertyOperand("nameA").whereSource().isJoin("companyA").close(2)
						.whereElementAt(1).isPropertyOperand("nameA").whereSource().isFrom(PersonA.T).close(2)
				.endQuery()
				.whereProperty("scalarMappings").isListWithSize(2).close()
			.close()
			.whereProperty("values").isListWithSize(2)
				.whereElementAt(0).isTupleComponent_(1)
				.whereElementAt(1).isTupleComponent_(0)
		;
		// @formatter:on
	}

	@Test
	public void simpleJoinSelectEntities() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
				.join("p", "companyA", "c")
				.select("p")
				.select("c")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
		.hasType(Projection.T)
			.whereProperty("operand")
				.isDelegateQuerySet("accessA")
				.whereDelegateQuery()
					.whereFroms(1).whereElementAt(0).isFrom(PersonA.T)
						.whereProperty("joins").isSetWithSize(1).whereFirstElement().isJoin("companyA")
					.whereSelection(10)
						.whereElementAt(i=0).isPropertyOperand(GenericEntity.globalId).whereSource().isJoin("companyA").close(2)
						.whereElementAt(++i).isPropertyOperand("id").whereSource().isJoin("companyA").close(2)
						.whereElementAt(++i).isPropertyOperand("nameA").whereSource().isJoin("companyA").close(2)
						.whereElementAt(++i).isPropertyOperand(GenericEntity.partition).whereSource().isJoin("companyA").close(2)
						.whereElementAt(++i).isPropertyOperand("companyNameA").whereSource().isFrom(PersonA.T).close(2)
						.whereElementAt(++i).isPropertyOperand(GenericEntity.globalId).whereSource().isFrom(PersonA.T).close(2)
						.whereElementAt(++i).isPropertyOperand("id").whereSource().isFrom(PersonA.T).close(2)
						.whereElementAt(++i).isPropertyOperand("nameA").whereSource().isFrom(PersonA.T).close(2)
						.whereElementAt(++i).isPropertyOperand("nickNameX").whereSource().isFrom(PersonA.T).close(2)
						.whereElementAt(++i).isPropertyOperand(GenericEntity.partition).whereSource().isFrom(PersonA.T).close(2)
				.endQuery()
				.whereProperty("scalarMappings").isListWithSize(10).close()
			.close()
			.whereProperty("values").isListWithSize(2)
		;
		// @formatter:on
	}

	/**
	 * TODO this can be optimized - this case can actually run without doing the DQJ, because pb.nameB can be retrieved
	 * from PersonA.parentA
	 */
	@Test
	public void simpleKeyPropertyJoinSelectPrimitiveProps() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.select("pa", "nameA")
				.select("pb", "nameB")
				.from(SmartPersonA.T, "pa")
					.join("pa", "smartParentB", "pb")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereProperty("operand")
				.hasType(DelegateQueryJoin.T)
				.whereProperty("materializedSet")
					.isDelegateQuerySet("accessA")
					.whereDelegateQuery()
						.whereSelection(2)
							.whereElementAt(0).isPropertyOperand("nameA").whereSource().isFrom(PersonA.T).close(2)
							.whereElementAt(1).isPropertyOperand("parentB").whereSource().isFrom(PersonA.T).close(2)
					.endQuery()
				.close()
				.whereProperty("querySet")
					.isDelegateQuerySet("accessB")
					.whereDelegateQuery()
						.whereSelection(1)
							.whereElementAt(0).isPropertyOperand("nameB").whereSource().isFrom(PersonB.T).close(2)
					.endQuery()
				.close()
			.close()
			.whereProperty("values").isListWithSize(2)
				.whereElementAt(0).isTupleComponent_(0)
				.whereElementAt(1).isTupleComponent_(1)
		;
		// @formatter:on
	}

	@Test
	public void simpleKeyPropertyJoinSelectEntities() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.select("pa")
				.select("pb")
				.from(SmartPersonA.T, "pa")
					.join("pa", "smartParentB", "pb")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereProperty("operand")
				.hasType(DelegateQueryJoin.T)
				.whereProperty("materializedSet")
					.isDelegateQuerySet("accessA")
					.whereDelegateQuery()
						.whereSelection(7)
							.whereElementAt(i=0).isPropertyOperand("companyNameA").whereSource().isFrom(PersonA.T).close(2)
							.whereElementAt(++i).isPropertyOperand(globalId).whereSource().isFrom(PersonA.T).close(2)
							.whereElementAt(++i).isPropertyOperand("id").whereSource().isFrom(PersonA.T).close(2)
							.whereElementAt(++i).isPropertyOperand("nameA").whereSource().isFrom(PersonA.T).close(2)
							.whereElementAt(++i).isPropertyOperand("nickNameX").whereSource().isFrom(PersonA.T).close(2)
							.whereElementAt(++i).isPropertyOperand("parentB").whereSource().isFrom(PersonA.T).close(2)
							.whereElementAt(++i).isPropertyOperand(partition).whereSource().isFrom(PersonA.T).close(2)
					.endQuery()
				.close()
				.whereProperty("querySet")
					.isDelegateQuerySet("accessB")
					.whereDelegateQuery()
						.whereSelection(7)
							.whereElementAt(i=0).isPropertyOperand("ageB").whereSource().isFrom(PersonB.T).close(2)
							.whereElementAt(++i).isPropertyOperand("birthDate").whereSource().isFrom(PersonB.T).close(2)
							.whereElementAt(++i).isPropertyOperand("companyNameB").whereSource().isFrom(PersonB.T).close(2)
							.whereElementAt(++i).isPropertyOperand(globalId).whereSource().isFrom(PersonB.T).close(2)
							.whereElementAt(++i).isPropertyOperand("id").whereSource().isFrom(PersonB.T).close(2)
							.whereElementAt(++i).isPropertyOperand("nameB").whereSource().isFrom(PersonB.T).close(2)
							.whereElementAt(++i).isPropertyOperand(partition).whereSource().isFrom(PersonB.T).close(2)
					.endQuery()
				.close()
			.close()
			.whereProperty("values").isListWithSize(2)
				.whereElementAt(0).hasType(QueryFunctionValue.T).whereProperty("queryFunction").isAssembleEntity(SmartPersonA.T).close(2)
				.whereElementAt(1).hasType(QueryFunctionValue.T).whereProperty("queryFunction").isAssembleEntity(SmartPersonB.T).close(2)
		;
		// @formatter:on
	}

	@Test
	public void simpleJoinWithConditionOnJoined_SameAccess() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
					.join("p", "companyA", "c")
				.select("p")
				.select("c")
				.where()
					.property("c", "nameA").like("* LLC")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
		.hasType(Projection.T)
			.whereProperty("operand")
				.isDelegateQuerySet("accessA")
				.whereDelegateQuery()
					.whereFroms(1).whereElementAt(0).isFrom(PersonA.T)
						.whereProperty("joins").isSetWithSize(1).whereFirstElement().isJoin("companyA")
					.whereSelection(10)
						.whereElementAt(i=0).isPropertyOperand(globalId).whereSource().isJoin("companyA").close(2)
						.whereElementAt(++i).isPropertyOperand("id").whereSource().isJoin("companyA").close(2)
						.whereElementAt(++i).isPropertyOperand("nameA").whereSource().isJoin("companyA").close(2)
						.whereElementAt(++i).isPropertyOperand(partition).whereSource().isJoin("companyA").close(2)
						.whereElementAt(++i).isPropertyOperand("companyNameA").whereSource().isFrom(PersonA.T).close(2)
						.whereElementAt(++i).isPropertyOperand(globalId).whereSource().isFrom(PersonA.T).close(2)
						.whereElementAt(++i).isPropertyOperand("id").whereSource().isFrom(PersonA.T).close(2)
						.whereElementAt(++i).isPropertyOperand("nameA").whereSource().isFrom(PersonA.T).close(2)
						.whereElementAt(++i).isPropertyOperand("nickNameX").whereSource().isFrom(PersonA.T).close(2)
						.whereElementAt(++i).isPropertyOperand(partition).whereSource().isFrom(PersonA.T).close(2)
					.whereCondition().isConjunction(1)
						.whereElementAt(0).isValueComparison(Operator.like)
							.whereProperty("leftOperand").isPropertyOperand("nameA").whereSource().isJoin("companyA").close(2)
							.whereProperty("rightOperand").is_("* LLC")
					
				.endQuery()
				.whereProperty("scalarMappings").isListWithSize(10).close()
			.close()
			.whereProperty("values").isListWithSize(2)
		;
		// @formatter:on
	}

	@Test
	public void simpleJoinWithConditionOnJoind_DifferentAccess() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "a")
				.join("a", "smartParentB", "b")
				.select("a")
				.select("b")
				.where()
					.property("b", "nameB").like("* Jr.")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T).whereProperty("operand")
				.hasType(DelegateQueryJoin.T)
				.whereProperty("materializedSet")
					.isDelegateQuerySet("accessB")
					.whereDelegateQuery()
						.whereFroms(1).whereElementAt(0).isFrom(PersonB.T)
						.whereSelection(7)
							.whereElementAt(i=0).isPropertyOperand("ageB").whereSource().isFrom(PersonB.T).close(2)
							.whereElementAt(++i).isPropertyOperand("birthDate").whereSource().isFrom(PersonB.T).close(2)
							.whereElementAt(++i).isPropertyOperand("companyNameB").whereSource().isFrom(PersonB.T).close(2)
							.whereElementAt(++i).isPropertyOperand(globalId).whereSource().isFrom(PersonB.T).close(2)
							.whereElementAt(++i).isPropertyOperand("id").whereSource().isFrom(PersonB.T).close(2)
							.whereElementAt(++i).isPropertyOperand("nameB").whereSource().isFrom(PersonB.T).close(2)
							.whereElementAt(++i).isPropertyOperand(partition).whereSource().isFrom(PersonB.T).close(2)
						.whereCondition().isConjunction(1)
							.whereElementAt(0).isValueComparison(Operator.like)
								.whereProperty("leftOperand").isPropertyOperand("nameB").close()
								.whereProperty("rightOperand").is_("* Jr.")
					.endQuery()
				.close()
				.whereProperty("querySet")
					.isDelegateQuerySet("accessA")
					.whereDelegateQuery()
						.whereSelection(7)
							.whereElementAt(i=0).isPropertyOperand("companyNameA").whereSource().isFrom(PersonA.T).close(2)
							.whereElementAt(++i).isPropertyOperand(globalId).whereSource().isFrom(PersonA.T).close(2)
							.whereElementAt(++i).isPropertyOperand("id").whereSource().isFrom(PersonA.T).close(2)
							.whereElementAt(++i).isPropertyOperand("nameA").whereSource().isFrom(PersonA.T).close(2)
							.whereElementAt(++i).isPropertyOperand("nickNameX").whereSource().isFrom(PersonA.T).close(2)
							.whereElementAt(++i).isPropertyOperand("parentB").whereSource().isFrom(PersonA.T).close(2)
							.whereElementAt(++i).isPropertyOperand(partition).whereSource().isFrom(PersonA.T).close(2)
					.endQuery()
				.close()
			.close()
			.whereProperty("values").isListWithSize(2)
				.whereElementAt(0).hasType(QueryFunctionValue.T).whereProperty("queryFunction").isAssembleEntity(SmartPersonA.T).close(2)
				.whereElementAt(1).hasType(QueryFunctionValue.T).whereProperty("queryFunction").isAssembleEntity(SmartPersonB.T).close(2)
		;
		// @formatter:on
	}

	/**
	 * The problem with the current implementation is that the null condition is delegated, as it is considered to be a
	 * strictly "SmartPersonB" condition. Thus the delegate query says "select from PersonB p where p = null", which
	 * doesn't return anything, which then means every single PersonA is OK.
	 * 
	 * For some reason the test below know it has to start with AccessB and then joins to A, thus we get a correct
	 * result. I really don't know why.
	 */
	@Test
	@Category(KnownIssue.class)
	public void EXPECTED_TO_FAIL_simpleJoinWithConditionOnJoind_DifferentAccess_IsNull() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.select("pa")
				.from(SmartPersonA.T, "pa")
				.where()
					.property("pa", "smartParentB").eq(null)
				.done();
		// @formatter:on

		runTest(selectQuery);

		Assert.fail("We do not support this");
	}

	/**
	 * No assert for now, this might change when the
	 * {@link #EXPECTED_TO_FAIL_simpleJoinWithConditionOnJoind_DifferentAccess_IsNull()} is fixed
	 */
	@Test
	public void simpleJoinWithConditionOnJoind_DifferentAccess_IsNotNull() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.select("pa", "nameA")
				.from(SmartPersonA.T, "pa")
				.where()
					.negation()
						.property("pa", "smartParentB").eq(null)
				.done();
		// @formatter:on

		runTest(selectQuery);

		// no assert
	}
}
