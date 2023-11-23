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
import com.braintribe.model.processing.query.smart.test.model.accessB.PersonItemLink;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartItem;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartPersonA;
import com.braintribe.model.processing.smart.query.planner.base.AbstractSmartQueryPlannerTests;
import com.braintribe.model.query.Operator;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.queryplan.set.Projection;
import com.braintribe.model.queryplan.value.QueryFunctionValue;
import com.braintribe.model.smartqueryplan.set.DelegateQueryJoin;

/**
 * 
 */
public class EntitySelection_LinkProperty_PlannerTests extends AbstractSmartQueryPlannerTests {

	@Test
	public void simpleEntityQuery() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
				.select("p", "linkItem")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T)
			.whereProperty("operand")
				.isDelegateQueryJoin_Left()
				.whereProperty("materializedSet")
					.isDelegateQueryJoin_Left()
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
								.whereElementAt(0).isPropertyOperand("itemName").whereSource().isFrom(PersonItemLink.T).close(2)
								.whereElementAt(1).isPropertyOperand("personName").whereSource().isFrom(PersonItemLink.T).close(2)
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
	public void queryWithDelegatableEntityCondition() {
		// @formatter:off
		SelectQuery selectQuery = query()		
				.from(SmartPersonA.T, "p")
				.select("p", "nameA")
				.where()
					.property("p", "linkItem").eq().entity(item(99L))
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
									.whereProperty("leftOperand").isSourceOnlyPropertyOperand().close()
									.whereProperty("rightOperand").isReference_(ItemB.T, 99L, accessIdB)
						.endQuery()
					.close()
					.whereProperty("querySet")
						.isDelegateQuerySet("accessB")
						.whereDelegateQuery()
							.whereSelection(2)
								.whereElementAt(0).isPropertyOperand("itemName").whereSource().isFrom(PersonItemLink.T).close(2)
								.whereElementAt(1).isPropertyOperand("personName").whereSource().isFrom(PersonItemLink.T).close(2)
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

}
