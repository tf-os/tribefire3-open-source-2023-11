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
import com.braintribe.model.processing.query.test.model.Owner;
import com.braintribe.model.query.Query;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.utils.genericmodel.GMCoreTools;

public class JoinFunctionTest extends AbstractQueryParserTest {

	@Test
	public void testListIndexSelection() throws Exception {
		// @formatter:off
		SelectQuery expectedQuery = sq()
				.from(Owner.class, "o")
				.select("cs", "name")
				.select().listIndex("cs")
				.join("o", "companyList", "cs")
				.done();
		// @formatter:on		

		String queryString = "select cs.name,listIndex(cs) from " + Owner.class.getName() + " o join o.companyList cs ";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();
		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void testListIndexCondition() throws Exception {
		// @formatter:off
		SelectQuery expectedQuery = sq()
				.from(Owner.class, "o")
				.select("cs", "name")
				.join("o", "companyList", "cs")
				.where()
					.listIndex("cs").le(1)
				.done();
		// @formatter:on		

		String queryString = "select cs.name from " + Owner.class.getName() + " o join o.companyList cs where listIndex(cs) <= 1";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();
		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void testMapKeySelection() throws Exception {
		// @formatter:off
		SelectQuery expectedQuery = sq()
				.from(Owner.class, "o")
				.select("cs", "name")
				.select().mapKey("cs")
				.join("o", "companyMap", "cs")
				.done();
		// @formatter:on		

		String queryString = "select cs.name, mapKey(cs) from " + Owner.class.getName() + " o join o.companyMap cs ";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();
		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void testMapKeyCondition() throws Exception {
		// @formatter:off
		SelectQuery expectedQuery = sq()
				.from(Owner.class, "o")
				.select("cs", "name")
				.join("o", "companyMap", "cs")
				.where()
					.mapKey("cs").ne(1)
				.done();
		// @formatter:on		

		String queryString = "select cs.name from " + Owner.class.getName() + " o join o.companyMap cs where mapKey(cs) != 1";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();
		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}
}
