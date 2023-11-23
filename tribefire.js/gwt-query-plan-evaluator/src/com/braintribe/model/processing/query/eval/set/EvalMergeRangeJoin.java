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

import com.braintribe.model.processing.query.eval.api.QueryEvaluationContext;
import com.braintribe.model.processing.query.eval.api.Tuple;
import com.braintribe.model.processing.query.eval.tools.EvalRange;
import com.braintribe.model.processing.query.eval.tools.RangeEvaluator;
import com.braintribe.model.queryplan.index.GeneratedMetricIndex;
import com.braintribe.model.queryplan.set.MergeRangeJoin;
import com.braintribe.model.queryplan.value.range.Range;

/**
 * 
 */
public class EvalMergeRangeJoin extends AbstractEvalMergeJoin {

	protected final GeneratedMetricIndex metricIndex;
	protected final Range range;

	public EvalMergeRangeJoin(MergeRangeJoin tupleSet, QueryEvaluationContext context) {
		super(tupleSet, context);

		this.metricIndex = tupleSet.getIndex();
		this.range = tupleSet.getRange();
	}

	@Override
	protected Iterator<Tuple> joinTuplesFor(Tuple tuple) {
		EvalRange er = RangeEvaluator.evaluate(range, tuple, context);

		return context.getIndexRange(metricIndex, er.lowerBound, er.lowerInclusive, er.upperBound, er.upperInclusive).iterator();
	}

}
