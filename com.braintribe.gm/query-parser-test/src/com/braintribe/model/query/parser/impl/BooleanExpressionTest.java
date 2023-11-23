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

public class BooleanExpressionTest extends AbstractQueryParserTest {

	@Test
	public void testDisjunction() throws Exception {

		// @formatter:off
		SelectQuery expectedQuery = sq()
				.from(Person.class, "p")
				.where()
					.disjunction()
						.property("p", "name").eq().property("p","indexedName")
						.property("p", "company").eq().property("p","indexedCompany")
					.close()
				.done();
		// @formatter:on

		String queryString = "select * from " + Person.class.getName() + " p where p.name = p.indexedName or p.company = p.indexedCompany";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();
		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void testConjunction() throws Exception {

		// @formatter:off
		SelectQuery expectedQuery = sq()
				.from(Person.class, "p")
				.where()
					.conjunction()
						.property("p", "name").eq().property("p","indexedName")
						.property("p", "company").eq().property("p","indexedCompany")
					.close()
				.done();
		// @formatter:on

		String queryString = "select * from " + Person.class.getName() + " p where p.name = p.indexedName and p.company = p.indexedCompany";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();
		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void testNegation() throws Exception {

		// @formatter:off
		SelectQuery expectedQuery = sq()
				.from(Person.class, "p")
				.where()
					.negation()
						.property("p", "name").eq().property("p","indexedName")
				.done();
		// @formatter:on

		String queryString = "select * from " + Person.class.getName() + " p where not p.name = p.indexedName ";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();
		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void testConjunctionDisjunctionDefaultPrecedence() throws Exception {

		// @formatter:off
		SelectQuery expectedQuery = sq()
				.from(Person.class, "p")
				.where()
					.disjunction()
						.property("p", "name").eq().property("p","indexedName")
						.conjunction()
							.property("p", "phoneNumber").ne().property("p","name")
							.property("p", "company").eq().property("p","indexedCompany")
						.close()
					.close()
				.done();
		// @formatter:on

		String queryString = "select * from " + Person.class.getName()
				+ " p where p.name = p.indexedName or p.phoneNumber != p.name and p.company = p.indexedCompany";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();
		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void testConjunctionDisjunctionParenthesisPrecedence() throws Exception {

		// @formatter:off
		SelectQuery expectedQuery = sq()
				.from(Person.class, "p")
				.where()
					.conjunction()
						.disjunction()
							.property("p", "name").eq().property("p","indexedName")
							.property("p", "phoneNumber").ne().property("p","name")
						.close()
						.property("p", "company").eq().property("p","indexedCompany")
					.close()
				.done();
		// @formatter:on

		String queryString = "select * from " + Person.class.getName()
				+ " p where (p.name = p.indexedName or p.phoneNumber != p.name )and p.company = p.indexedCompany";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();
		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void testConjunctionDisjunctionNegationDefaultPrecedence() throws Exception {

		// @formatter:off
		SelectQuery expectedQuery = sq()
				.from(Person.class, "p")
				.where()
					.disjunction()
						.property("p", "name").eq().property("p","indexedName")
						.conjunction()
							.property("p", "phoneNumber").ne().property("p","name")
							.negation()
								.property("p", "company").eq().property("p","indexedCompany")
						.close()
					.close()
				.done();
		// @formatter:on

		String queryString = "select * from " + Person.class.getName()
				+ " p where p.name = p.indexedName or p.phoneNumber != p.name and not p.company = p.indexedCompany";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();
		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void testConjunctionDisjunctionNegationParenthesisPrecedence() throws Exception {

		// @formatter:off
		SelectQuery expectedQuery = sq()
				.from(Person.class, "p")
				.where()
					.conjunction()
						.negation()
							.property("p", "name").eq().property("p","indexedName")
						.negation()	
							.disjunction()
								.property("p", "phoneNumber").ne().property("p","name")
								.property("p", "company").eq().property("p","indexedCompany")
						.close()
					.close()
				.done();
		// @formatter:on

		String queryString = "select * from " + Person.class.getName()
				+ " p where not p.name = p.indexedName and not (p.phoneNumber != p.name or p.company = p.indexedCompany)";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();
		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}
}
