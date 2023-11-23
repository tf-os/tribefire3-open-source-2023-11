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
package com.braintribe.model.processing.query.support;

import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.List;

import com.braintribe.model.processing.query.eval.api.EvalTupleSet;
import com.braintribe.model.processing.query.eval.api.Tuple;
import com.braintribe.model.processing.query.eval.set.HasMoreAwareSet;
import com.braintribe.model.processing.query.tools.QueryResults;
import com.braintribe.model.query.EntityQueryResult;
import com.braintribe.model.query.PropertyQueryResult;
import com.braintribe.model.query.SelectQueryResult;
import com.braintribe.model.record.ListRecord;
import com.braintribe.model.record.Record;

/**
 * 
 */
public class QueryResultBuilder {

	public static SelectQueryResult buildQueryResult(EvalTupleSet tuples, int tupleSize) {
		List<Object> results = buildQueryResultRows(tuples, tupleSize);
		boolean hasMore = hasMore(tuples);

		return QueryResults.selectQueryResult(results, hasMore);
	}

	private static List<Object> buildQueryResultRows(EvalTupleSet tuples, int tupleSize) {
		List<Object> result = newList();

		if (tupleSize == 1)
			for (Tuple tuple : tuples)
				result.add(tuple.getValue(0));
		else
			for (Tuple tuple : tuples)
				result.add(asRow(tuple, tupleSize));

		return result;
	}

	private static Record asRow(Tuple tuple, int size) {
		List<Object> values = newList(size);

		for (int i = 0; i < size; i++)
			values.add(tuple.getValue(i));

		ListRecord result = ListRecord.T.create();
		result.setValues(values);

		return result;
	}

	private static boolean hasMore(EvalTupleSet tuples) {
		return tuples instanceof HasMoreAwareSet && ((HasMoreAwareSet) tuples).hasMore();
	}

	public static EntityQueryResult buildEntityQueryResult(List<?> entities, boolean hasMore) {
		return QueryResults.entityQueryResult(entities, hasMore);

	}

	public static PropertyQueryResult buildPropertyQueryResult(Object value, boolean hasMore) {
		return QueryResults.propertyQueryResult(value, hasMore);
	}

}
