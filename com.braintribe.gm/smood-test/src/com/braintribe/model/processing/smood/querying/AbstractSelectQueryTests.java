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
package com.braintribe.model.processing.smood.querying;

import java.util.List;
import java.util.Map;

import org.junit.Assert;

import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.processing.query.test.tools.QueryTestTools;
import com.braintribe.model.processing.smood.test.AbstractSmoodTests;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SelectQueryResult;
import com.braintribe.model.record.ListRecord;
import com.braintribe.utils.junit.assertions.BtAssertions;

/**
 * 
 */
public class AbstractSelectQueryTests extends AbstractSmoodTests {

	private SelectQueryResult result;
	protected Map<Object, Integer> results;
	private List<?> resultList;
	private int position;

	protected SelectQueryBuilder query() {
		return new SelectQueryBuilder();
	}

	protected void evaluate(SelectQuery query) {
		result = executeQuery(query);
		resultList = result.getResults();
		results = QueryTestTools.processResult(result.getResults());
	}

	protected SelectQueryResult executeQuery(SelectQuery query) {
		return smood.query(query);
	}

	protected void assertResultContains(Object o, Object... os) {
		assertResultContains(QueryTestTools.asList(o, os));
	}

	protected void assertResultContains(Object o) {
		Integer count = results.get(o);

		if (count == null)
			throw new RuntimeException("Object not found in the result: " + o);

		if (count == 1)
			results.remove(o);
		else
			results.put(o, count - 1);
	}

	protected void assertNextResult(Object o, Object... os) {
		List<?> actual = ((ListRecord) resultList.get(position++)).getValues();
		List<?> expected = QueryTestTools.asList(o, os);

		if (!actual.equals(expected))
			Assert.fail("Next object was expected to be : " + expected + ", but was: " + actual);
	}

	protected void assertNextResult(Object o) {
		BtAssertions.assertThat(resultList.get(position++)).as("Object not found in the result: " + o).isEqualTo(o);
	}

	protected void assertPaginationHasMore(boolean hasMore) {
		BtAssertions.assertThat(result.getHasMore()).as("Wrong pagination-hasMore value!").isEqualTo(hasMore);
	}

	protected void assertNoMoreResults() {
		if (position > 0)
			BtAssertions.assertThat(resultList).as("No more tuples in the result set expected!").hasSize(position);
		else
			BtAssertions.assertThat(results).as("No more tuples in the result set expected!").isEmpty();
	}

}
