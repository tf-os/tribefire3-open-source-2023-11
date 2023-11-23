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

import org.junit.Test;

import com.braintribe.model.processing.query.parser.QueryParser;
import com.braintribe.model.processing.query.parser.api.ParsedQuery;
import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.query.Query;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.utils.genericmodel.GMCoreTools;

public class StringFunctionTest extends AbstractQueryParserTest {

	@Test
	public void testLower() throws Exception {

		// @formatter:off
		SelectQuery expectedQuery = sq()
				.from(Person.class, "p")
				.where()
					.asString().lower().property("p", "name").eq().value("n")
				.done();
		// @formatter:on

		String queryString = "select * from " + Person.class.getName() + " p where toString(lower(p.name)) = 'n'";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();
		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void testUpper() throws Exception {

		// @formatter:off
		SelectQuery expectedQuery = sq()
				.from(Person.class, "p")
				.where()
					.asString().upper().property("p", "name").eq().value("n")
				.done();
		// @formatter:on

		String queryString = "select * from " + Person.class.getName() + " p where toString(upper(p.name)) = 'n'";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();
		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void testConcatenate() throws Exception {

		// @formatter:off
		SelectQuery expectedQuery = sq()
				.from(Person.class, "p")
				.where()
					.concatenate()
						.lower().property("p", "name")
						.upper().property("p","indexedName")
						.value("q")
					.close()
					.eq().value("n")
				.done();
		// @formatter:on

		String queryString = "select * from " + Person.class.getName() + " p where concatenation(lower(p.name),upper(p.indexedName),'q') = 'n'";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();
		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void testNestedStringFunctions() throws Exception {

		// @formatter:off
		SelectQuery expectedQuery = sq()
				.from(Person.class, "p")
				.where()
					.asString().lower().upper().property("p", "name").eq().value("n")
				.done();
		// @formatter:on

		String queryString = "select * from " + Person.class.getName() + " p where toString(lower(upper(p.name))) = 'n'";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();
		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void testLocalize() throws Exception {

		// @formatter:off
		SelectQuery expectedQuery = sq()
				.from(Person.class, "p")
				.select("p")
				.select().localize("pt").property("p", "localizedString")
				.done();
		// @formatter:on

		String queryString = "select p, localize(p.localizedString,'pt') from " + Person.class.getName() + " p";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();
		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}
}
