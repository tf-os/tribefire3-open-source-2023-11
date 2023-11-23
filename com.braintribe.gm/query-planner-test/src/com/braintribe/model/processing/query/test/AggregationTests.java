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
import com.braintribe.model.queryplan.set.AggregatingProjection;
import com.braintribe.model.queryplan.value.AggregationFunctionType;
import com.braintribe.model.queryplan.value.TupleComponent;

/**
 * 
 */
public class AggregationTests extends AbstractQueryPlannerTests {

	@Test
	public void selectingCountOnly() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.select().count("p", null)
				.from(Person.T, "p")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
		.hasType(AggregatingProjection.T)
			.whereOperand().isSourceSet_(Person.T)
			.hasValues(1)
				.whereElementAt(0)
					.isAggregateFunction(AggregationFunctionType.count)
					.whereOperand()		
						.isTupleComponent_(0)
		;
		// @formatter:on
	}

	@Test
	public void selectingCountAndEntity() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.select("p")
				.select().count("p", null)
				.from(Person.T, "p")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
		.hasType(AggregatingProjection.T)
			.whereOperand().isSourceSet_(Person.T)
			.hasValues(2)
				.whereElementAt(0).hasType(TupleComponent.T).close()
				.whereElementAt(1)
					.isAggregateFunction(AggregationFunctionType.count)
					.whereOperand()
						.isTupleComponent_(0)				
		;
		// @formatter:on
	}

	@Test
	public void countEntityProperty() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.select("p")
				.select().count("p", "company")
				.from(Person.T, "p")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan()
		.hasType(AggregatingProjection.T)
			.whereOperand().isSourceSet_(Person.T)
			.hasValues(2)
				.whereElementAt(0).isTupleComponent_(0)
				.whereElementAt(1)
					.isAggregateFunction(AggregationFunctionType.count)
					.whereOperand()				
						.isValueProperty("company")
						.whereValue().isTupleComponent_(0)
		;
		// @formatter:on
	}

}
