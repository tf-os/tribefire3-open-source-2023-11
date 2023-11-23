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
package com.braintribe.model.processing.smart.query.planner.tools;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.braintribe.model.processing.query.planner.builder.TupleSetBuilder;
import com.braintribe.model.queryplan.set.SortCriterion;
import com.braintribe.model.queryplan.set.StaticSet;
import com.braintribe.model.queryplan.set.TupleSet;
import com.braintribe.model.smartqueryplan.SmartQueryPlan;
import com.braintribe.model.smartqueryplan.set.OrderedConcatenation;

/**
 * 
 */
public class SmartTupleSetBuilder {

	public static TupleSet concatenation(List<TupleSet> tupleSets, List<SortCriterion> sortCriteria, int tupleSize) {
		if (sortCriteria.isEmpty())
			return TupleSetBuilder.concatenation(tupleSets, tupleSize);

		Iterator<TupleSet> it = tupleSets.iterator();

		return concatenation(it.next(), it, sortCriteria, tupleSize);
	}

	private static TupleSet concatenation(TupleSet first, Iterator<TupleSet> it, List<SortCriterion> sortCriteria, int tupleSize) {
		if (!it.hasNext())
			return first;

		OrderedConcatenation result = OrderedConcatenation.T.createPlain();
		result.setSortCriteria(sortCriteria);
		result.setFirstOperand(first);
		result.setTupleSize(tupleSize);
		result.setSecondOperand(concatenation(it.next(), it, sortCriteria, tupleSize));

		return result;
	}

	public static SmartQueryPlan queryPlan(TupleSet tupleSet, int totalComponents) {
		SmartQueryPlan result = SmartQueryPlan.T.createPlain();
		result.setTotalComponentCount(totalComponents);
		result.setTupleSet(tupleSet);

		return result;
	}

	public static SmartQueryPlan emptyPlan() {
		StaticSet emptyTupleSet = StaticSet.T.createPlain();
		emptyTupleSet.setValues(Collections.emptySet());

		return queryPlan(emptyTupleSet, 0);
	}

}
