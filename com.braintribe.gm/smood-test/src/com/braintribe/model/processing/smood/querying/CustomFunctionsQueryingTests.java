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
package com.braintribe.model.processing.smood.querying;

import org.junit.Test;

import com.braintribe.model.processing.query.test.CustomFunctionsTests;
import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.query.SelectQuery;

/**
 * @see CustomFunctionsTests
 */
public class CustomFunctionsQueryingTests extends AbstractSelectQueryTests {

	/** @see CustomFunctionsTests#customFunctionSimpleCondition() */
	@Test
	public void customFunctionSimple() {
		Person p;
		p = b.person("Jack Ma").companyName("Alibaba").create();
		p = b.person("John Smith").companyName("Microsoft").create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.where()
					.concatenate()
						.property("p", "name")
						.value(":")
						.property("p", "companyName")
					.close().eq("John Smith:Microsoft")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertNextResult(p);
		assertNoMoreResults();
	}

}
