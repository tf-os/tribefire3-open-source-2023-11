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

import static com.braintribe.utils.lcd.CollectionTools2.asMap;

import org.junit.Test;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.query.smart.test.model.accessA.constant.ConstantPropEntityA;
import com.braintribe.model.processing.query.smart.test.model.accessA.constant.ConstantPropEntityA2;
import com.braintribe.model.processing.query.smart.test.model.accessA.constant.ConstantPropEntitySubA;
import com.braintribe.model.processing.query.smart.test.model.smart.constant.SmartConstantPropEntity;
import com.braintribe.model.processing.query.smart.test.model.smart.constant.SmartConstantPropEntityA;
import com.braintribe.model.processing.query.smart.test.model.smart.constant.SmartConstantPropEntityA2;
import com.braintribe.model.processing.query.smart.test.model.smart.constant.SmartConstantPropEntitySubA;
import com.braintribe.model.processing.smart.query.planner.base.AbstractSmartQueryPlannerTests;
import com.braintribe.model.query.Operator;
import com.braintribe.model.query.PropertyOperand;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.functions.EntitySignature;
import com.braintribe.model.queryplan.set.Concatenation;
import com.braintribe.model.queryplan.set.Projection;

/**
 * 
 */
public class ConstantProperty_PlannerTests extends AbstractSmartQueryPlannerTests {

	@Test
	public void selectTheConstant() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartConstantPropEntitySubA.T, "c")
				.select("c", "name")
				.select("c", "constantValue")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan(1)
		.hasType(Projection.T)
			.whereProperty("operand")
				.isDelegateQuerySet("accessA")
				.whereProperty("scalarMappings").isListWithSize(1).close()
			.close()
			.whereProperty("values").isListWithSize(2)
				.whereElementAt(0).isTupleComponent_(0)
				.whereElementAt(1).isStaticValue_(SmartConstantPropEntitySubA.CONSTANT_VALUE_SUB)
		;
		// @formatter:on
	}

	// TODO improve assertions for AssembleEntity function
	@Test
	public void selectConstantPropertyEntity() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartConstantPropEntitySubA.T, "c")
				.select("c")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan(4)
		.hasType(Projection.T)
			.whereProperty("operand")
				.isDelegateQuerySet("accessA")
				.whereDelegateQuery()
					.whereSelection(4)
						.whereElementAt(i=0).isPropertyOperand(GenericEntity.globalId).close()
						.whereElementAt(++i).isPropertyOperand("id").close()
						.whereElementAt(++i).isPropertyOperand("name").close()
						.whereElementAt(++i).isPropertyOperand(GenericEntity.partition).close()
				.endQuery()
				.whereProperty("scalarMappings").isListWithSize(4).close()
			.close()
			.whereProperty("values").isListWithSize(1)
				.whereElementAt(0)
					.isQueryFunctionValueAndQf().isAssembleEntity(SmartConstantPropEntitySubA.T)
		;
		// @formatter:on
	}

	@Test
	public void conditionOnTheConstant_WhenTrue() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartConstantPropEntitySubA.T, "c")
				.select("c", "name")
				.where()
					.property("c", "constantValue").eq(SmartConstantPropEntitySubA.CONSTANT_VALUE_SUB)
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan(1)
		.hasType(Projection.T)
			.whereProperty("operand")
				.isDelegateQuerySet("accessA")
				.whereDelegateQuery()
					.withNoRestriction()
				.endQuery()
				.whereProperty("scalarMappings").isListWithSize(1).close()
			.close()
			.whereProperty("values").isListWithSize(1)
				.whereElementAt(0).isTupleComponent_(0)
		;
		// @formatter:on
	}

	@Test
	public void conditionOnTheConstant_WhenFalse() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartConstantPropEntitySubA.T, "c")
				.select("c", "name")
				.where()
					.property("c", "constantValue").eq("Bullshit")
				.done();
		// @formatter:on

		runTest(selectQuery);

		assertEmptyQueryPlan();
	}

	@Test
	public void selectTheConstant_Polymorphic() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartConstantPropEntityA.T, "c")
				.select("c", "name")
				.select("c", "constantValue")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan(2)
		.hasType(Projection.T)
			.whereProperty("operand")
				.isDelegateQuerySet("accessA")
				.whereProperty("scalarMappings").isListWithSize(2).close()
			.close()
			.whereProperty("values").isListWithSize(2)
				.whereElementAt(0).isTupleComponent_(0)
				.whereElementAt(1).isQueryFunctionValueAndQf()
					.isDiscriminatorValueWithRules(1).is_(
						asMap(
							ConstantPropEntityA.class.getName(), SmartConstantPropEntityA.CONSTANT_VALUE, 
							ConstantPropEntitySubA.class.getName(), SmartConstantPropEntitySubA.CONSTANT_VALUE_SUB
						)
					)					
		;
		// @formatter:on
	}

	@Test
	public void conditionOnTheConstant_Polymorphic_WhenPartiallyTrue() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartConstantPropEntityA.T, "c")
				.select("c", "name")
				.where()
					.property("c", "constantValue").eq(SmartConstantPropEntitySubA.CONSTANT_VALUE_SUB)
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan(1)
		.hasType(Projection.T)
			.whereProperty("operand")
				.isDelegateQuerySet("accessA")
				.whereDelegateQuery()
					.whereSelection(1)
						.whereElementAt(0).isPropertyOperand("name").close()
					.close()
					.whereCondition()
						.isConjunction(1).whereElementAt(0)
							.isValueComparison(Operator.equal)
							.whereLeftOperand().hasType(EntitySignature.T).close()
							.whereRightOperand().is_(ConstantPropEntitySubA.class.getName())
				.endQuery()
				.whereProperty("scalarMappings").isListWithSize(1).close()
			.close()
			.whereProperty("values").isListWithSize(1)
				.whereElementAt(0).isTupleComponent_(0)
		;
		// @formatter:on
	}

	@Test
	public void conditionOnTheConstant_Polymorphic_WithLike() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartConstantPropEntity.T, "c")
				.select("c", "name")
				.where()
					.property("c", "constantValue").like("-CONST-S*")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan(1)
		.hasType(Projection.T)
			.whereProperty("operand")
				.isDelegateQuerySet("accessA")
				.whereDelegateQuery()
					.whereSelection(1)
						.whereElementAt(0).isPropertyOperand("name").close()
					.close()
					.whereCondition()
						.isConjunction(1).whereElementAt(0)
							.isValueComparison(Operator.equal)
							.whereLeftOperand().hasType(EntitySignature.T).close()
							.whereRightOperand().is_(ConstantPropEntitySubA.class.getName())
				.endQuery()
				.whereProperty("scalarMappings").isListWithSize(1).close()
			.close()
			.whereProperty("values").isListWithSize(1)
				.whereElementAt(0).isTupleComponent_(0)
		;
		// @formatter:on
	}

	/**
	 * There was a bug which needed this use-case to be tested - originally we would have empty sets (StaticSet with empty values) in the
	 * query plan, in which case the tuple size might be computed wrong.
	 */
	@Test
	public void conditionOnTheConstant_WithPagination() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartConstantPropEntity.T, "c")
				.select("c")
				.where()
					.property("c", "constantValue").eq(SmartConstantPropEntityA2.CONSTANT_VALUE)
				.paging(10, 0)
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan(4)
		.isPaginatedSetWithOperand(10, 0)
			.hasType(Projection.T)
				.whereProperty("operand")
					.isDelegateQuerySet("accessA")
					.whereDelegateQuery()
						.whereSelection(4)
							.whereElementAt(i=0).isPropertyOperand(GenericEntity.globalId).close()
							.whereElementAt(++i).isPropertyOperand("id").close()
							.whereElementAt(++i).isPropertyOperand("name").close()
							.whereElementAt(++i).isPropertyOperand(GenericEntity.partition).close()
						.close()
						.withNoRestriction()
					.endQuery()
					.whereProperty("scalarMappings").isListWithSize(4).close()
				.close()
				.whereProperty("values").isListWithSize(1)
					.whereElementAt(0).isQueryFunctionValueAndQf()
						.isAssembleEntity(SmartConstantPropEntityA2.T)
		;
		// @formatter:on
	}

	@Test
	public void conditionOnTheConstant_Polymorphic_WhenFalse() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartConstantPropEntity.T, "c")
				.select("c", "name")
				.where()
					.property("c", "constantValue").eq("Bullshit")
				.done();
		// @formatter:on

		runTest(selectQuery);

		assertEmptyQueryPlan();
	}

	/** This is extremely unlikely and is here just for completion, as you can see the query is extremely artificial. */
	@Test
	public void conditionOnTheConstant_ComparingWithOtherOperand() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartConstantPropEntity.T, "c")
				.select("c", "name")
				.where()
					.property("c", "constantValue").eq().property("c", "name")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan(1)
		.hasType(Concatenation.T)
			.whereProperty("firstOperand").hasType(Projection.T)
				.whereProperty("operand")
					.isDelegateQuerySet("accessA")
					.whereDelegateQuery()
						.whereSelection(1)
							.whereElementAt(0).isPropertyOperand("name")
								.whereSource().isFrom(ConstantPropEntityA.T).close()
							.close()
						.close()
						.whereCondition()
							.isConjunction(1).whereElementAt(0).isDisjunction(2)
								.whereElementAt(0).isConjunction(2)
									.whereElementAt(0)
										.isValueComparison(Operator.equal)
										.whereLeftOperand().hasType(EntitySignature.T).close()
										.whereRightOperand().is_(ConstantPropEntityA.class.getName()).close()
									.whereElementAt(1)
										.isValueComparison(Operator.equal)
										.whereLeftOperand().hasType(PropertyOperand.T).close()
										.whereRightOperand().is_("-CONST-").close()
									.close(2)
								.whereElementAt(1).isConjunction(2)
									.whereElementAt(0)
										.isValueComparison(Operator.equal)
										.whereLeftOperand().hasType(EntitySignature.T).close()
										.whereRightOperand().is_(ConstantPropEntitySubA.class.getName()).close()
									.whereElementAt(1)
										.isValueComparison(Operator.equal)
										.whereLeftOperand().hasType(PropertyOperand.T).close()
										.whereRightOperand().is_("-CONST-SUB-").close()
					.endQuery()
					.whereProperty("scalarMappings").isListWithSize(1).close()
				.close()
				.whereProperty("values").isListWithSize(1)
					.whereElementAt(0).isTupleComponent_(0).close()
			.close()
			.whereProperty("secondOperand").hasType(Projection.T)
				.whereProperty("operand")
				.isDelegateQuerySet("accessA")
				.whereDelegateQuery()
					.whereSelection(1)
						.whereElementAt(0).isPropertyOperand("name")
							.whereSource().isFrom(ConstantPropEntityA2.T).close()
						.close()
					.close()
					.whereCondition()
						.isConjunction(1).whereElementAt(0)
							.isValueComparison(Operator.equal)
							.whereLeftOperand().hasType(PropertyOperand.T).close()
							.whereRightOperand().is_("-CONST_2-").close()
		;
		// @formatter:on
	}

	/** This is even more weird than the previous one, so also no optimization, just make sure it would work */
	@Test
	public void conditionOnTheConstant_ComparingWithOtherOperand1() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartConstantPropEntityA2.T, "c")
				.select("c", "name")
				.where()
					.property("c", "constantValue").eq().property("c", "constantValue")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan(1)
		.hasType(Projection.T)
			.whereProperty("operand")
				.isDelegateQuerySet("accessA")
				.whereDelegateQuery()
					.whereSelection(1)
						.whereElementAt(0).isPropertyOperand("name")
							.whereSource().isFrom(ConstantPropEntityA2.T).close()
						.close()
					.close()
					.withNoRestriction()
				.endQuery()
				.whereProperty("scalarMappings").isListWithSize(1).close()
			.close()
			.whereProperty("values").isListWithSize(1)
				.whereElementAt(0).isTupleComponent_(0)
		;
		// @formatter:on
	}

}
