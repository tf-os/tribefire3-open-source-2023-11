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
package com.braintribe.model.processing.query.test.stringifier;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.query.SelectQuery;

/**
 *
 */
public class QueryFunctionQueryTests extends AbstractSelectQueryTests {

	@Test
	public void asStringCondition() {
		// @formatter:off
		 SelectQuery selectQuery = query()
				.from(Person.class, "_Person")
				.where()
				.asString().property("_Person", "id").like("*")
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery);
		Assert.assertTrue(queryString.equalsIgnoreCase(
				"select * from com.braintribe.model.processing.query.test.model.Person _Person where toString(_Person.id) like '*'"));
	}

	@Test
	public void concatenateCondition() {
		// @formatter:off
		 SelectQuery selectQuery = query()
				.from(Person.class, "_Person")
				.where()
				.concatenate()
				.asString().property("_Person", "id")
				.value(" ")
				.property("_Person", "name")
				.close().like("*")
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery);
		Assert.assertTrue(queryString.equalsIgnoreCase(
				"select * from com.braintribe.model.processing.query.test.model.Person _Person where concatenation(toString(_Person.id), ' ', _Person.name) like '*'"));
	}

	@Test
	public void upperLowerCondition() {
		// @formatter:off
		 SelectQuery selectQuery = query()
				.from(Person.class, "_Person")
				.where()
				.conjunction()
				.asString().upper().property("_Person", "name").eq("JOHN")
				.value("jonny").in().asString().lower().property("_Person", "nicknames")
				.close()
				.done();
		// @formatter:on

		String queryString = stringify(selectQuery);
		Assert.assertTrue(queryString.equalsIgnoreCase(
				"select * from com.braintribe.model.processing.query.test.model.Person _Person where (toString(upper(_Person.name)) = 'JOHN' and 'jonny' in toString(lower(_Person.nicknames)))"));
	}
}
