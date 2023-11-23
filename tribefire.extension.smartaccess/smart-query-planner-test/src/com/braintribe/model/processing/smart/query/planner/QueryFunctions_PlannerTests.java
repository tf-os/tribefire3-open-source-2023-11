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
import com.braintribe.model.processing.query.smart.test.model.accessA.FlyingCarA;
import com.braintribe.model.processing.query.smart.test.model.smart.Car;
import com.braintribe.model.processing.query.smart.test.model.smart.FlyingCar;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartPersonA;
import com.braintribe.model.processing.smart.query.planner.base.AbstractSmartQueryPlannerTests;
import com.braintribe.model.query.Operator;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.functions.EntitySignature;
import com.braintribe.model.queryplan.set.Projection;
import com.braintribe.model.queryplan.value.ConstantValue;
import com.braintribe.model.queryplan.value.QueryFunctionValue;
import com.braintribe.model.smartqueryplan.set.DelegateQueryJoin;

public class QueryFunctions_PlannerTests extends AbstractSmartQueryPlannerTests {

	// #########################################
	// ## . . . . . EntitySignature . . . . . ##
	// #########################################

	@Test
	public void signature_Final() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(SmartPersonA.T, "p")
				.select().entitySignature().entity("p")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
		.hasType(Projection.T)
			.whereProperty("operand")
				.isDelegateQuerySet("accessA")
				.whereDelegateQuery()
					.whereSelection(1)
						.whereElementAt(0).hasType(EntitySignature.T)
				.endQuery()
				.whereProperty("scalarMappings").isListWithSize(1).close()
			.close()
			.whereProperty("values").isListWithSize(1)
				.whereElementAt(0).hasType(ConstantValue.T)
					.whereProperty("value").is_(SmartPersonA.class.getName())
		;
		// @formatter:on
	}

	@Test
	public void signature_Hierarchy() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Car.T, "c")
				.select().entitySignature().entity("c")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
		.hasType(Projection.T)
			.whereProperty("operand")
				.isDelegateQuerySet("accessA")
				.whereDelegateQuery()
					.whereSelection(1)
						.whereElementAt(0).hasType(EntitySignature.T)
				.endQuery()
				.whereProperty("scalarMappings").isListWithSize(1).close()
			.close()
			.whereProperty("values").isListWithSize(1)
				.whereElementAt(0).hasType(QueryFunctionValue.T)
					.whereProperty("queryFunction").hasType(EntitySignature.T).close()
					.whereProperty("operandMappings").isMapWithSize(1)
		;
		// @formatter:on
	}

	@Test
	public void conditionOnSourceType() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Car.T, "c")
				.select().entity("c")
				.where()
					.entitySignature("c").eq(FlyingCar.class.getName())
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan(8)
			.hasType(Projection.T).whereProperty("operand")
				.hasType(DelegateQueryJoin.T)
				.whereProperty("materializedSet")
					.isDelegateQuerySet("accessA")
					.whereDelegateQuery()
						.whereFroms(1).whereElementAt(0).isFrom(CarA.T)
						.whereCondition().isConjunction(1)
								.whereElementAt(0).isValueComparison(Operator.equal)
									.whereProperty("leftOperand").hasType(EntitySignature.T).close()
									.whereProperty("rightOperand").is_(FlyingCarA.class.getName())
					.endQuery()
				.close()
				.whereProperty("querySet")
					.isDelegateQuerySet("accessA")
					.whereDelegateQuery()
						.whereFroms(1).whereElementAt(0).isFrom(FlyingCarA.T)
					.endQuery()
				.close()
			.close()
			.whereProperty("values").isListWithSize(1)
		;
		// @formatter:on
	}

	// @SuppressWarnings("unused")
	// @Test(expected = Exception.class)
	// public void selectingLocalizedValue() {
//		// @formatter:off
//		SelectQuery selectQuery = query()
//				.from(SmartItem.T, "i")
//				.select("i").select().localize("pt").property("i", "localizedNameB")
//				.done();
//		// @formatter:on
	//
	// evaluate(selectQuery);
	//
	// }

	// TODO DELEGATE AsString function
	@Test
	public void propertyAsString() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
				.select().asString().property("p", "id")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
		.hasType(Projection.T)
			.whereProperty("operand")
				.isDelegateQuerySet("accessA")
				.whereDelegateQuery()
					.whereSelection(1)
						.whereElementAt(0).isPropertyOperand("id")
				.endQuery()
				.whereProperty("scalarMappings").isListWithSize(1).close()
			.close()
			.whereProperty("values").isListWithSize(1)
				.whereElementAt(0)
		;
		// @formatter:on
	}

}
