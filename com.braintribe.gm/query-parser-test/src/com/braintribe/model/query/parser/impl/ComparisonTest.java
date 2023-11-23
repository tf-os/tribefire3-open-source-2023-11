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
package com.braintribe.model.query.parser.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.braintribe.model.processing.query.parser.QueryParser;
import com.braintribe.model.processing.query.parser.api.GmqlParsingError;
import com.braintribe.model.processing.query.parser.api.ParsedQuery;
import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.query.Query;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.utils.genericmodel.GMCoreTools;

public class ComparisonTest extends AbstractQueryParserTest {

	@Test
	public void testEqual() throws Exception {

		// @formatter:off
		SelectQuery expectedQuery = sq()
				.from(Person.class, "p")
				.where().property("p", "name").eq().value("name")
				.done();
		// @formatter:on

		String queryString = "select * from " + Person.class.getName() + " p where p.name = 'name'";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();
		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void testParanthesisedEqual() throws Exception {

		// @formatter:off
		SelectQuery expectedQuery = sq()
				.from(Person.class, "p")
				.where().property("p", "name").eq().value("name")
				.done();
		// @formatter:on

		String queryString = "select * from " + Person.class.getName() + " p where (p.name = 'name')";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();
		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void testEntityListIndex() throws Exception {
		String queryString = "from " + Person.class.getName() + " p where listIndex(p) > 2";

		List<GmqlParsingError> expectedErrorList = getExpectedError(
				"EntityQuery and PropertyQuery are not allowed to have aggregate or join functions");

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedInvalidParsedQuery(parsedQuery, expectedErrorList);

	}

	@Test
	public void testIn() throws Exception {

		// @formatter:off
		SelectQuery expectedQuery = sq()
				.from(Person.class, "p")
				.where().value("nick").in().property("p", "nickNames")
				.done();
		// @formatter:on

		String queryString = "select * from " + Person.class.getName() + " p where 'nick' in p.nickNames";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();
		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void testContains() throws Exception {

		// @formatter:off
		SelectQuery expectedQuery = sq()
				.from(Person.class, "p")
				.where().property("p", "nickNames").contains().value("nick")
				.done();
		// @formatter:on

		String queryString = "select * from " + Person.class.getName() + " p where p.nickNames contains 'nick'";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();
		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void testInCustomCollection() throws Exception {

		Set<Object> names = new HashSet<Object>();
		names.add("name1");
		names.add("name2");

		// @formatter:off
		SelectQuery expectedQuery = sq()
				.from(Person.class, "p")
				.where().property("p","name").in().value(names)
				.done();
		// @formatter:on

		String queryString = "select * from " + Person.class.getName() + " p where p.name  in ('name1','name2')";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();
		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void testContainsCustomCollection() throws Exception {

		Set<Object> names = new HashSet<Object>();
		names.add("name1");
		names.add("name2");

		// @formatter:off
		SelectQuery expectedQuery = sq()
				.from(Person.class, "p")
				.where().value(names).contains().property("p","name")
				.done();
		// @formatter:on

		String queryString = "select * from " + Person.class.getName() + " p where ('name1','name2') contains p.name";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();
		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}
}
