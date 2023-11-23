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

import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.query.parser.QueryParser;
import com.braintribe.model.processing.query.parser.api.GmqlParsingError;
import com.braintribe.model.processing.query.parser.api.ParsedQuery;
import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.Query;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.utils.genericmodel.GMCoreTools;

public class SourcePropertyTest extends AbstractQueryParserTest {

	@Test
	public void testSelectionSingle() throws Exception {

		// @formatter:off
		SelectQuery expectedQuery = sq().from(Person.class, "p").select("p").done();
		// @formatter:on

		String queryString = "select p from " + Person.class.getName() + " p";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();
		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void testSelectionExtended() throws Exception {

		// @formatter:off
		SelectQuery expectedQuery = sq().from(Person.class, "p").select("p", "name").done();
		// @formatter:on

		String queryString = "select p.name from " + Person.class.getName() + " p";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();
		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void testSelectionKeyword() throws Exception {

		// @formatter:off
		SelectQuery expectedQuery = sq().from(Person.class, "p").select("p", "name.from") // doesn't
																							// exist,
																							// just
																							// for
																							// testing
				.done();
		// @formatter:on

		String queryString = "select p.name.\"from\" from " + Person.class.getName() + " p";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();
		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void testSelectConditionSingle() throws Exception {

		// @formatter:off
		SelectQuery expectedQuery = sq().from(Person.class, "p").where().entity("p").ne().value(null).done();
		// @formatter:on

		String queryString = "select * from " + Person.class.getName() + " p where p != null";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();
		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void testSelectConditionSingleFail() throws Exception {

		// // @formatter:off
		// SelectQuery expectedQuery = sq()
		// .from(Person.class, "p")
		// .where()
		// .entity("c").ne().value(null)
		// .done();
		// // @formatter:on

		String queryString = "select * from " + Person.class.getName() + " p where c != null";

		List<GmqlParsingError> expectedErrorList = getExpectedError("Source expected and not registered, provided alias: c");

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedInvalidParsedQuery(parsedQuery, expectedErrorList);

	}

	@Test
	public void testSelectConditionExtended() throws Exception {

		// @formatter:off
		SelectQuery expectedQuery = sq().from(Person.class, "p").where().property("p", "name").ne().value(null).done();
		// @formatter:on

		String queryString = "select * from " + Person.class.getName() + " p where p.name != null";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();
		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void testSelectConditionKeyword() throws Exception {

		// @formatter:off
		SelectQuery expectedQuery = sq().from(Person.class, "p").where().property("p", "name.from").ne().value(null).done();
		// @formatter:on

		String queryString = "select * from " + Person.class.getName() + " p where p.name.\"from\" != null";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();
		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void testSelectConditionExtendedFail() throws Exception {

		// // @formatter:off
		// SelectQuery expectedQuery = sq()
		// .from(Person.class, "p")
		// .where()
		// .property("c","name.from").ne().value(null)
		// .done();
		// // @formatter:on

		String queryString = "select * from " + Person.class.getName() + " p where c.name != null";

		List<GmqlParsingError> expectedErrorList = getExpectedError("Source expected and not registered, provided alias: c");

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedInvalidParsedQuery(parsedQuery, expectedErrorList);
	}

	@Test
	public void testEntityConditionSingle() throws Exception {

		// @formatter:off
		EntityQuery expectedQuery = EntityQueryBuilder.from(Person.class).where().property(null, null).ne().value(null).done();
		// @formatter:on

		String queryString = "from " + Person.class.getName() + " p where p != null";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();
		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void testEntityConditionExtended() throws Exception {

		// @formatter:off
		EntityQuery expectedQuery = EntityQueryBuilder.from(Person.class).where().property(null, "name").ne().value(null).done();
		// @formatter:on

		String queryString = "from " + Person.class.getName() + " p where p.name != null";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();
		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void testEntityConditionExtendedNoAlias() throws Exception {

		// @formatter:off
		EntityQuery expectedQuery = EntityQueryBuilder.from(Person.class).where().property(null, "x.name").ne().value(null).done();
		// @formatter:on

		String queryString = "from " + Person.class.getName() + " p where x.name != null";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();
		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void testEntityConditionKeyword() throws Exception {

		// @formatter:off
		EntityQuery expectedQuery = EntityQueryBuilder.from(Person.class).where().property(null, "name.from").ne().value(null).done();
		// @formatter:on

		String queryString = "from " + Person.class.getName() + " p where p.name.\"from\" != null";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();
		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void testSelectConditionUnknownAliasFail() throws Exception {

		String queryString = "select a.x from b.y";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		List<GmqlParsingError> expectedErrorList = getExpectedError(
				"Unresolved source link: SourceLink[someId,globalId=null,id=null,joinType=null,joins=set[size=0],name=\"a\",partition=null,property=null,source=null], with alias: a");
		validatedInvalidParsedQuery(parsedQuery, expectedErrorList);
	}

}
