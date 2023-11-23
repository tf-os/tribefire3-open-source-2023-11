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

import com.braintribe.model.processing.query.test.QueryFunctionTests;
import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.query.SelectQuery;

/**
 * 
 */
public class QueryFunctionQueryTests extends AbstractSelectQueryTests {

	/** @see QueryFunctionTests#asStringCondition() */
	@Test
	public void asStringCondition() {
		Person p1 = b.person("Jack").create();
		Person p2 = b.person("John").create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "person")
				.where()
					.asString().property("person", "id").like("*")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(p1);
		assertResultContains(p2);
		assertNoMoreResults();
	}

	@Test
	public void ilikeWithEscapeCharacter() {
		Person p;
		p = b.person("Jack").create();
		p = b.person("John\\John").create();

		// @formatter:off
		SelectQuery selectQuery = query()
				.from(Person.T, "p")
				.where()
					.property("p", "name").ilike("john\\\\john")
				.done();
		// @formatter:on

		evaluate(selectQuery);

		assertResultContains(p);
		assertNoMoreResults();
	}
}
