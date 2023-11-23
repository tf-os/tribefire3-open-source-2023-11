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

import java.util.List;

import org.junit.Test;

import com.braintribe.model.processing.query.parser.QueryParser;
import com.braintribe.model.processing.query.parser.api.GmqlParsingError;
import com.braintribe.model.processing.query.parser.api.ParsedQuery;
import com.braintribe.model.processing.query.test.model.Company;
import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.query.Query;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.utils.genericmodel.GMCoreTools;
import com.braintribe.utils.junit.assertions.BtAssertions;

public class SelectQueryTest extends AbstractQueryParserTest {

	@Test
	public void testSingleSelectSingleFrom() throws Exception {

		// @formatter:off
		SelectQuery expectedQuery = sq()
				.from(Person.class, "p")
				.select("p", "name")
				.done();
		// @formatter:on

		String queryString = "SELECT p.name FROM " + Person.class.getName() + " p";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();

		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void testMultipleSelectSingleFrom() throws Exception {

		// @formatter:off
		SelectQuery expectedQuery = sq()
				.from(Person.class, "p")
				.select("p", "name")
				.select("p", "indexedUniqueName")
				.done();
		// @formatter:on

		String queryString = "select p.name, p.indexedUniqueName from " + Person.class.getName() + " p";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();

		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void testMultipleSelectMultipleFrom() throws Exception {

		// @formatter:off
		SelectQuery expectedQuery = sq()
				.from(Person.class, "p")
				.from(Company.class, "c")
				.select("p", "name")
				.select("p", "indexedUniqueName")
				.select("c", "address")
				.done();
		// @formatter:on

		String queryString = "select p.name, p.indexedUniqueName, c.address from " + Person.class.getName() + " p, " + Company.class.getName() + " c";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();

		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void testSingleSelectSingleFromImplicitAlias() throws Exception {

		// @formatter:off
		SelectQuery expectedQuery = sq()
				.from(Person.class, "Person")
				.select("Person", "name")
				.done();
		// @formatter:on

		String queryString = "select Person.name from " + Person.class.getName();

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();

		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void testSingleSelectSingleFromSingleIdentifier() throws Exception {

		// @formatter:off
		SelectQuery expectedQuery = sq()
				.from("com","com") // doesn't exist as an entity
				.done();
		// @formatter:on

		String queryString = "select * from com";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();

		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void testDistinct() throws Exception {

		// @formatter:off
		SelectQuery expectedQuery = sq()
				.from(Person.class, "p")
				.select("p", "name")
				.distinct(true)
				.done();
		// @formatter:on

		String queryString = "select distinct p.name from " + Person.class.getName() + " p";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();

		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void testSingleSelectMultipleFromFail() throws Exception {

//		// @formatter:off
//		SelectQuery expectedQuery = sq()
//				.from(Person.class, "p")
//				.from(Person.class, "p")
//				.select("p", "name")
//				.done();
//		// @formatter:on

		String queryString = "select p.name from " + Person.class.getName() + " p, " + Person.class.getName() + " p";

		List<GmqlParsingError> expectedErrorList = getExpectedError("The alias p is already defined.");

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedInvalidParsedQuery(parsedQuery, expectedErrorList);
	}

	@Test
	public void testSingleSelectSingleFromFail() throws Exception {

//		// @formatter:off
//		SelectQuery expectedQuery = sq()
//				.from(Person.class, "p")
//				.select("c", "name")
//				.done();
//		// @formatter:on

		String queryString = "select c.name from " + Person.class.getName() + " p";

		List<GmqlParsingError> expectedErrorList = getExpectedError(
				"Unresolved source link: SourceLink[@194706439,globalId=null,id=null,joinType=null,joins=set[size=0],name=\"c\",partition=null,property=null,source=null], with alias: c");

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedInvalidParsedQuery(parsedQuery, expectedErrorList);
	}

	@Test
	public void testSingleSelectSingleFromTypo() throws Exception {

		// misspelled from
		String queryString = "select p.name fom " + Person.class.getName() + " p";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);

		// TODO update to reflect the actual expected errors
		BtAssertions.assertThat(parsedQuery).isNotNull();
		BtAssertions.assertThat(parsedQuery.getErrorList()).isNotNull();
		BtAssertions.assertThat(parsedQuery.getErrorList().isEmpty()).isEqualTo(false);
		BtAssertions.assertThat(parsedQuery.getQuery()).isNull();
		BtAssertions.assertThat(parsedQuery.getSourcesRegistry()).isNotNull();
		BtAssertions.assertThat(parsedQuery.getSourcesRegistry()).isEmpty();

	}

	@Test
	public void testSingleSelectTypo() throws Exception {

		String queryString = "elect id from com.braintribe.model.user.User";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		List<GmqlParsingError> expectedErrorList = getExpectedError(0, 1, "no viable alternative at input 'elect'", "StandardIdentifier");

		validatedInvalidParsedQuery(parsedQuery, expectedErrorList);
	}
}
