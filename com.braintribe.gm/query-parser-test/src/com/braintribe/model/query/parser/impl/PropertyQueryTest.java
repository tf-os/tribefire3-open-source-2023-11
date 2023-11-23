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

import com.braintribe.model.processing.query.fluent.PropertyQueryBuilder;
import com.braintribe.model.processing.query.parser.QueryParser;
import com.braintribe.model.processing.query.parser.api.GmqlParsingError;
import com.braintribe.model.processing.query.parser.api.ParsedQuery;
import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.query.Operator;
import com.braintribe.model.query.OrderingDirection;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.Query;
import com.braintribe.utils.genericmodel.GMCoreTools;

public class PropertyQueryTest extends AbstractQueryParserTest {

	@Test
	public void testPropertySignature() throws Exception {
		// @formatter:off
		PropertyQuery expectedQuery = PropertyQueryBuilder
				.forProperty(Person.class.getName(), 1, "name")
				.done();
		// @formatter:on

		String queryString = "property name of reference( " + Person.class.getName() + ",1)";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();

		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void testDistinct() throws Exception {
		PropertyQuery expectedQuery = PropertyQueryBuilder.forProperty(Person.class.getName(), 1, "name").done();
		expectedQuery.setDistinct(true);

		String queryString = "distinct property name of reference( " + Person.class.getName() + ",1)";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();

		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void testPropertySignatureWithAlias() throws Exception {
		// @formatter:off
		PropertyQuery expectedQuery = PropertyQueryBuilder
				.forProperty(Person.class.getName(), 1, "name")
				.done();
		// @formatter:on

		String queryString = "property name n of reference( " + Person.class.getName() + ",1)";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();

		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void testCondition() throws Exception {
		// @formatter:off
		PropertyQuery expectedQuery = PropertyQueryBuilder
				.forProperty(Person.class.getName(), 1, "name")
				.where()
					.property("name").comparison(Operator.like).value("firstname")
				.done();
		// @formatter:on

		String queryString = "property name n of reference( " + Person.class.getName() + ",1) where name like 'firstname' ";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();

		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void testOrderBy() throws Exception {
		// @formatter:off
		PropertyQuery expectedQuery = PropertyQueryBuilder
				.forProperty(Person.class.getName(), 1, "name")
				.orderBy("name", OrderingDirection.descending)
				.done();
		// @formatter:on

		String queryString = "property name n of reference( " + Person.class.getName() + ",1) order by name desc";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();

		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void testPagination() throws Exception {
		// @formatter:off
		PropertyQuery expectedQuery = PropertyQueryBuilder
				.forProperty(Person.class.getName(), 1, "name")
				.paging(20, 200)
				.done();
		// @formatter:on

		String queryString = "property name n of reference( " + Person.class.getName() + ",1) limit 20 offset 200";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();

		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void testConditionOrderBy() throws Exception {

		// @formatter:off
		PropertyQuery expectedQuery = PropertyQueryBuilder
				.forProperty(Person.class.getName(), 1, "name")
				.orderBy("name", OrderingDirection.descending)
				.where()
					.property("name").comparison(Operator.like).value("firstname")
				.done();
		// @formatter:on

		String queryString = "property name n of reference( " + Person.class.getName() + ",1) where name like 'firstname' order by name desc ";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();

		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void testOrderByPagination() throws Exception {

		// @formatter:off
		PropertyQuery expectedQuery = PropertyQueryBuilder
				.forProperty(Person.class.getName(), 1, "name")
				.orderBy("name", OrderingDirection.descending)
				.paging(20, 200)
				.done();
		// @formatter:on

		String queryString = "property name n of reference( " + Person.class.getName() + ",1) order by name desc limit 20 offset 200";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();

		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void testConditionOrderByPagination() throws Exception {

		// @formatter:off
		PropertyQuery expectedQuery = PropertyQueryBuilder
				.forProperty(Person.class.getName(), 1, "name")
				.orderBy("name", OrderingDirection.descending)
				.paging(20, 200)
				.where()
					.property("name").comparison(Operator.like).value("firstname")
				.done();
		// @formatter:on

		String queryString = "property name n of reference( " + Person.class.getName()
				+ ",1) where name like 'firstname' order by name desc limit 20 offset 200";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();

		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void testOrderByPaginationConditionFail() throws Exception {
		String queryString = "property name n of reference( " + Person.class.getName()
				+ ",1) order by name desc limit 20 offset 200  where name like 'firstname' ";

		List<GmqlParsingError> expectedErrorList = getExpectedError(129, 1, "mismatched input 'where' expecting <EOF>", "Where");

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedInvalidParsedQuery(parsedQuery, expectedErrorList);

	}

	@Test
	public void testPaginationConditionFail() throws Exception {
		String queryString = "property name n of reference( " + Person.class.getName() + ",1) limit 20 offset 200  where name like 'firstname' ";

		List<GmqlParsingError> expectedErrorList = getExpectedError(110, 1, "mismatched input 'where' expecting <EOF>", "Where");

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedInvalidParsedQuery(parsedQuery, expectedErrorList);

	}

	@Test
	public void testPropertySignatureWithPreliminaryEntityReferenceFail() throws Exception {
		String queryString = "property name n of reference( " + Person.class.getName()
				+ ",1, false) limit 20 offset 200  where name like 'firstname' ";

		List<GmqlParsingError> expectedErrorList = getExpectedError(
				"Only PersistentEntityReference is allowed for PropertyQuery, provided value: ~com.braintribe.model.processing.query.test.model.Person[1, ]");

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedInvalidParsedQuery(parsedQuery, expectedErrorList);
	}
}
