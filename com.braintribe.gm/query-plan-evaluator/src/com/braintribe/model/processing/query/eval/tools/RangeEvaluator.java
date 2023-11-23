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
package com.braintribe.model.processing.query.eval.tools;

import com.braintribe.model.processing.query.eval.api.QueryEvaluationContext;
import com.braintribe.model.processing.query.eval.api.RuntimeQueryEvaluationException;
import com.braintribe.model.processing.query.eval.api.Tuple;
import com.braintribe.model.queryplan.value.range.Range;
import com.braintribe.model.queryplan.value.range.RangeIntersection;
import com.braintribe.model.queryplan.value.range.SimpleRange;

/**
 * 
 */
public class RangeEvaluator {

	public static EvalRange evaluate(Range range, Tuple tuple, QueryEvaluationContext context) {
		switch (range.rangeType()) {
			case intersection:
				return evaluate((RangeIntersection) range, tuple, context);
			case simple:
				return evaluate((SimpleRange) range, tuple, context);
		}

		throw new RuntimeQueryEvaluationException("Unsupported Range: " + range + " of type: " + range.rangeType());
	}

	private static EvalRange evaluate(RangeIntersection range, Tuple tuple, QueryEvaluationContext context) {
		EvalRange result = new EvalRange();

		for (SimpleRange sr: range.getRanges())
			adjustBounds(result, sr, tuple, context);

		return result;

	}

	private static EvalRange evaluate(SimpleRange range, Tuple tuple, QueryEvaluationContext context) {
		EvalRange result = new EvalRange();
		adjustBounds(result, range, tuple, context);

		return result;
	}

	private static void adjustBounds(EvalRange result, SimpleRange sr, Tuple tuple, QueryEvaluationContext context) {
		if (sr.getLowerBound() != null) {
			Object value = context.resolveValue(tuple, sr.getLowerBound());

			if (result.lowerInclusive == null) {
				setLowerBound(result, value, sr.getLowerInclusive());

			} else {
				int cmp = ObjectComparator.compare(result.lowerBound, value);
				if (cmp < 0)
					setLowerBound(result, value, sr.getLowerInclusive());

				if (cmp == 0 && !sr.getLowerInclusive()) {
					result.lowerInclusive = false;
				}
			}
		}

		if (sr.getUpperBound() != null) {
			Object value = context.resolveValue(tuple, sr.getUpperBound());

			if (result.upperInclusive == null) {
				setUpperBound(result, value, sr.getUpperInclusive());

			} else {
				int cmp = ObjectComparator.compare(result.upperBound, value);
				if (cmp > 0)
					setUpperBound(result, value, sr.getUpperInclusive());

				if (cmp == 0 && !sr.getUpperInclusive()) {
					result.upperInclusive = false;
				}
			}
		}

	}

	private static void setLowerBound(EvalRange result, Object value, boolean inclusive) {
		result.lowerBound = value;
		result.lowerInclusive = inclusive;
	}

	private static void setUpperBound(EvalRange result, Object value, boolean inclusive) {
		result.upperBound = value;
		result.upperInclusive = inclusive;
	}
}
