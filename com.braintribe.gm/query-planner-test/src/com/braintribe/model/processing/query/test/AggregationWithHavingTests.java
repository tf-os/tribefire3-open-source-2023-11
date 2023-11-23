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

import org.junit.Test;

import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.queryplan.filter.GreaterThanOrEqual;
import com.braintribe.model.queryplan.set.AggregatingProjection;
import com.braintribe.model.queryplan.set.FilteredSet;
import com.braintribe.model.queryplan.set.Projection;
import com.braintribe.model.queryplan.value.AggregationFunctionType;

/**
 * 
 */
public class AggregationWithHavingTests extends AbstractQueryPlannerTests {

	@Test
	public void conditionOnSelected() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.select("p", "name")
				.select().count("p", "age")
				.from(Person.T, "p")
				.having()
					.count("p", "age").ge(2)
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(FilteredSet.T)
			.whereOperand()
				.hasType(AggregatingProjection.T)
				.whereOperand().isSourceSet_(Person.T)
				.hasValues(2)
					.whereElementAt(0).isValueProperty_("name")
					.whereElementAt(1)
						.isAggregateFunction(AggregationFunctionType.count)
						.whereOperand()				
							.isValueProperty("age").whereValue().isTupleComponent_(0)
						.close()
					.close()
				.close()
			.close()
			.whereProperty("filter")
				.hasType(GreaterThanOrEqual.T)
				.whereProperty("leftOperand").isTupleComponent_(1)
				.whereProperty("rightOperand").isStaticValue_(2)
		;
		// @formatter:on
	}

	@Test
	public void conditionOnNotSelected() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.select("p", "name")
				.select().count("p", "age")
				.from(Person.T, "p")
				.having()
					.sum("p", "age").ge(50)
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
			.hasType(Projection.T)
			.whereOperand()
				.hasType(FilteredSet.T)
				.whereOperand()
					.hasType(AggregatingProjection.T)
					.whereOperand().isSourceSet_(Person.T)
					.hasValues(3)
						.whereElementAt(0).isValueProperty_("name")
						.whereElementAt(1)
							.isAggregateFunction(AggregationFunctionType.count)
							.whereOperand()				
								.isValueProperty("age").whereValue().isTupleComponent_(0)
							.close()
						.close()
						.whereElementAt(2)
							.isAggregateFunction(AggregationFunctionType.sum)
							.whereOperand()				
								.isValueProperty("age").whereValue().isTupleComponent_(0)
							.close()
						.close()
					.close() // AggregatingProjection.values
				.close() // FilteredSet.operand
				.whereProperty("filter")
					.hasType(GreaterThanOrEqual.T)
					.whereProperty("leftOperand").isTupleComponent_(2)
					.whereProperty("rightOperand").isStaticValue_(50)
				.close()
			.close()
			.hasValues(2)
		;
		// @formatter:on
	}
}
