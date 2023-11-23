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
import com.braintribe.model.query.OrderingDirection;
import com.braintribe.model.query.Query;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.utils.genericmodel.GMCoreTools;

public class OrderByTest extends AbstractQueryParserTest {

	@Test
	public void testOrderByDefault() throws Exception {

		// @formatter:off
		SelectQuery expectedQuery = sq()
				.from(Person.class, "p")
				.orderBy().property("name")
				.done();
		// @formatter:on

		String queryString = "select * from " + Person.class.getName() + " p order by p.name";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();
		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void testOrderByAsc() throws Exception {

		// @formatter:off
		SelectQuery expectedQuery = sq()
				.from(Person.class, "p")
				.orderBy(OrderingDirection.ascending).property("name")
				.done();
		// @formatter:on

		String queryString = "select * from " + Person.class.getName() + " p order by p.name asc";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();
		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void testOrderByDesc() throws Exception {

		// @formatter:off
		SelectQuery expectedQuery = sq()
				.from(Person.class, "p")
				.orderBy(OrderingDirection.descending).property("name")
				.done();
		// @formatter:on

		String queryString = "select * from " + Person.class.getName() + " p order by p.name desc";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();
		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void testMultipleOrderBy() throws Exception {

		// @formatter:off
		SelectQuery expectedQuery = sq()
				.from(Person.class, "p")
				.orderByCascade()
					.dir(OrderingDirection.descending).property("p", "company.name")
					.dir(OrderingDirection.ascending).value(45) 
					.dir(OrderingDirection.ascending).property("p", "name")
				.close()	
				.done();
		// @formatter:on

		String queryString = "select * from " + Person.class.getName() + " p order by p.company.name desc, 45, p.name asc";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();
		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void orderByAggregateFunction() throws Exception {

		// @formatter:off
		SelectQuery expectedQuery = sq()
				.select("p", "name")
				.select().count("p", "age")
				.from(Person.class, "p")
				.orderBy()
					.count("p", "age")
				.done();
		// @formatter:on

		String queryString = "select p.name, count(p.age) from " + Person.class.getName() + " p order by count(p.age)";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();
		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

	@Test
	public void orderByAggregateFunction_ExplicitGroupBy() throws Exception {

		// @formatter:off
		SelectQuery expectedQuery = sq()
				.select("p", "name")
				.select().count("p", "age")
				.from(Person.class, "p")
				.groupBy().property("p", "name")
				.orderBy()
					.count("p", "age")
				.done();
		// @formatter:on

		String queryString = "select p.name, count(p.age) from " + Person.class.getName() + " p group by p.name order by count(p.age)";

		ParsedQuery parsedQuery = QueryParser.parse(queryString);
		validatedParsedQuery(parsedQuery);
		Query actualQuery = parsedQuery.getQuery();
		GMCoreTools.checkDescription(actualQuery, expectedQuery);
	}

}
