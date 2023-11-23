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
package com.braintribe.model.processing.query.test.tools;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.Assert;

import com.braintribe.model.query.EntityQueryResult;
import com.braintribe.model.query.QueryResult;
import com.braintribe.model.query.SelectQueryResult;
import com.braintribe.model.record.ListRecord;

/**
 * This class allows doing assertions on a {@link QueryResult}.
 * <p>
 * There are two ways how to do the assertions and they should not be mixed.
 * 
 * <b>Without ordering</b>
 * 
 * <pre>
 * QueryResultAssert qra = new QueryResultAssert(queryResult);
 * qra.assertContains(a, b, c);
 * qra.assertContains(x, y, z);
 * qra.assertNoMoreResults();
 * </pre>
 * 
 * <b>With ordering</b>
 * 
 * <pre>
 * QueryResultAssert qra = new QueryResultAssert(queryResult);
 * qra.assertNext(a, b, c);
 * qra.assertNext(x, y, z);
 * qra.assertNoMoreResults();
 * </pre>
 * 
 * In addition to this, you can {@link #assertHasMore(boolean)} and also explicitly verify some tuple is not part of the result with
 * {@link #assertNotContains(Object)}.
 * 
 * @author peter.gazdik
 */
public class QueryResultAssert {

	public final List<?> resultList;
	public final Map<Object, Integer> results;
	public final boolean hasMore;

	private int position;

	public QueryResultAssert(SelectQueryResult qr) {
		this(qr.getResults(), qr);
	}

	public QueryResultAssert(EntityQueryResult qr) {
		this(qr.getEntities(), qr);
	}

	public QueryResultAssert(List<?> resultList) {
		this.resultList = resultList;
		this.results = QueryTestTools.processResult(resultList);
		this.hasMore = false;
	}

	public QueryResultAssert(List<?> resultList, QueryResult qr) {
		this.resultList = resultList;
		this.results = QueryTestTools.processResult(resultList);
		this.hasMore = qr.getHasMore();
	}

	public void assertContains(Object o, Object... os) {
		assertContains(QueryTestTools.asList(o, os));
	}

	public void assertContains(Object o) {
		if (!results.containsKey(o))
			throw new RuntimeException("Object not found in the results: " + o + ". Results: " + results);

		Integer i = results.remove(o);

		if (i > 1)
			results.put(o, i - 1);
	}

	public void assertNotContains(Object o) {
		if (results.containsKey(o))
			throw new RuntimeException("Object found in the results, but should not have been: " + o + " . Results: " + results);
	}

	public void assertNext(Object o, Object... os) {
		List<?> expected = QueryTestTools.asList(o, os);
		if (position >= resultList.size())
			Assert.fail("No other tuple present in the query result. Expected: " + expected);

		List<?> actual = ((ListRecord) resultList.get(position++)).getValues();
		if (!actual.equals(expected))
			Assert.fail("Object not found in the result: " + expected);
	}

	public void assertNext(Object o) {
		assertThat(resultList.get(position++)).as("Object not found in the result: " + o).isEqualTo(o);
	}

	public void assertHasMore(boolean expected) {
		assertThat(hasMore).as("Wrong 'hasMore' flag!").isEqualTo(expected);
	}

	public void assertNoMoreResults() {
		if (position > 0)
			assertThat(resultList).as("No more tuples in the result set expected!").hasSize(position);
		else
			assertThat(results).as("No more tuples in the result set expected!").isEmpty();
	}

}
