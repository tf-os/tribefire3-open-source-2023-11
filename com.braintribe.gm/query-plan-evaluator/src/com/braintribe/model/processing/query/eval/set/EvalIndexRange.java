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
package com.braintribe.model.processing.query.eval.set;

import java.util.Iterator;

import com.braintribe.model.processing.query.eval.api.EvalTupleSet;
import com.braintribe.model.processing.query.eval.api.QueryEvaluationContext;
import com.braintribe.model.processing.query.eval.api.Tuple;
import com.braintribe.model.queryplan.set.IndexRange;
import com.braintribe.model.queryplan.value.Value;
import com.braintribe.model.queryplan.value.range.SimpleRange;

/**
 * 
 */
public class EvalIndexRange implements EvalTupleSet {

	protected final Iterable<Tuple> tuples;

	public EvalIndexRange(IndexRange indexRange, QueryEvaluationContext context) {
		SimpleRange range = indexRange.getRange();

		Object lowerBound = resolveValue(range.getLowerBound(), context);
		Object upperBound = resolveValue(range.getUpperBound(), context);

		Boolean lowerInclusive = resolveInclusive(range.getLowerBound(), range.getLowerInclusive());
		Boolean upperInclusive = resolveInclusive(range.getUpperBound(), range.getUpperInclusive());

		tuples = context.getIndexRange(indexRange.getMetricIndex(), lowerBound, lowerInclusive, upperBound, upperInclusive);
	}

	private Object resolveValue(Value v, QueryEvaluationContext context) {
		// the value better be a StaticValue
		return v != null ? context.resolveValue(null, v) : null;
	}

	private Boolean resolveInclusive(Value v, boolean inclusive) {
		return v != null ? inclusive : null;
	}

	@Override
	public Iterator<Tuple> iterator() {
		return tuples.iterator();
	}

}
