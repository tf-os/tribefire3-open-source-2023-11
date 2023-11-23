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
import com.braintribe.model.query.functions.value.AsString;
import com.braintribe.model.queryplan.filter.Like;
import com.braintribe.model.queryplan.set.FilteredSet;
import com.braintribe.model.queryplan.set.Projection;
import com.braintribe.model.queryplan.value.QueryFunctionValue;

/**
 * 
 */
public class QueryFunctionTests extends AbstractQueryPlannerTests {

	@Test
	public void asStringCondition() {
		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.where()
					.asString().property("p", "id").like(".*")
				.done();
		// @formatter:on

		runTest(selectQuery);

		// @formatter:off
		assertQueryPlan().hasType(Projection.T)
			.whereOperand().hasType(FilteredSet.T)
				.whereOperand().isSourceSet_(Person.T)
				.whereProperty("filter").hasType(Like.T)
					.whereProperty("leftOperand").hasType(QueryFunctionValue.T)
						.whereProperty("queryFunction").hasType(AsString.T) .close()
					.close()
					.whereProperty("rightOperand").isStaticValue_(".*")
		;
		// @formatter:on
	}

}
