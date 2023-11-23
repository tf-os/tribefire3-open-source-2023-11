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
package com.braintribe.model.processing.query.planner.builder;


import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.List;
import java.util.Set;

import com.braintribe.model.processing.query.planner.context.QueryPlannerContext;
import com.braintribe.model.processing.query.planner.tools.Bound;
import com.braintribe.model.queryplan.value.range.RangeIntersection;
import com.braintribe.model.queryplan.value.range.SimpleRange;

/**
 * 
 */
public class RangeBuilder {

	public static RangeIntersection rangeForBounds(List<Bound> lowerBounds, List<Bound> upperBounds, QueryPlannerContext context) {
		RangeIntersection result = RangeIntersection.T.create();
		Set<SimpleRange> ranges = newSet();
		result.setRanges(ranges);

		for (Bound lowerBound: lowerBounds)
			ranges.add(RangeBuilder.rangeForBound(lowerBound, true, context));

		for (Bound upperBound: upperBounds)
			ranges.add(RangeBuilder.rangeForBound(upperBound, false, context));

		return result;
	}

	private static SimpleRange rangeForBound(Bound bound, boolean lower, QueryPlannerContext context) {
		if (lower)
			return rangeForBounds(bound, null, context);
		else
			return rangeForBounds(null, bound, context);
	}

	public static SimpleRange rangeForBounds(Bound lowerBound, Bound upperBound, QueryPlannerContext context) {
		SimpleRange result = SimpleRange.T.create();

		if (lowerBound != null) {
			result.setLowerBound(context.convertOperand(lowerBound.value));
			result.setLowerInclusive(lowerBound.inclusive);
		}

		if (upperBound != null) {
			result.setUpperBound(context.convertOperand(upperBound.value));
			result.setUpperInclusive(upperBound.inclusive);
		}

		return result;
	}

}
