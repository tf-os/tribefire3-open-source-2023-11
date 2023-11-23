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
package com.braintribe.model.access.smart.test.query;

import com.braintribe.model.access.ModelAccessException;
import com.braintribe.model.access.smart.test.base.AbstractSmartAccessTests;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.processing.query.test.tools.QueryResultAssert;
import com.braintribe.model.processing.query.test.tools.QueryTestTools;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.EntityQueryResult;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SelectQueryResult;

/**
 * 
 */
public class AbstractSmartQueryTests extends AbstractSmartAccessTests {

	protected QueryResultAssert result;

	protected SelectQueryBuilder query() {
		return new SelectQueryBuilder();
	}

	protected void evaluate(SelectQuery query) {
		SelectQueryResult queryResult = executeQuery(query);
		queryResult = mergeQueryResultToLocalSession(queryResult);
		result = new QueryResultAssert(queryResult);
	}

	protected void evaluate(EntityQuery query) {
		EntityQueryResult queryResult = executeQuery(query);
		queryResult = mergeQueryResultToLocalSession(queryResult);
		result = new QueryResultAssert(queryResult);
	}

	protected <T> T mergeQueryResultToLocalSession(T t) {
		try {
			return localSession.merge().doFor(t);
		} catch (GmSessionException e) {
			throw new RuntimeException("Error while merging query result to local session.", e);
		}
	}

	protected SelectQueryResult executeQuery(SelectQuery query) {
		try {
			return smartAccess.query(query);

		} catch (ModelAccessException e) {
			throw new RuntimeException("SelectQuery could not be evaluated.", e);
		}
	}

	protected EntityQueryResult executeQuery(EntityQuery query) {
		try {
			return smartAccess.queryEntities(query);

		} catch (ModelAccessException e) {
			throw new RuntimeException("EntityQuery could not be evaluated.", e);
		}
	}

	protected void assertResultContains(Object o, Object... os) {
		assertResultContains(QueryTestTools.asList(o, os));
	}

	protected void assertResultContains(Object o) {
		result.assertContains(o);
	}

	protected void assertResultNotContains(Object o) {
		result.assertNotContains(o);
	}

	protected void assertNextResult(Object o, Object... os) {
		result.assertNext(o, os);
	}

	protected void assertNextResult(Object o) {
		result.assertNext(o);
	}

	protected void assertHasMoreFlag(boolean expected) {
		result.assertHasMore(expected);
	}

	protected void assertNoMoreResults() {
		result.assertNoMoreResults();
	}

}
