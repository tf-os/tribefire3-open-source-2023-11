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
import java.util.Set;

import com.braintribe.model.processing.query.eval.api.EvalTupleSet;
import com.braintribe.model.processing.query.eval.api.QueryEvaluationContext;
import com.braintribe.model.processing.query.eval.api.Tuple;
import com.braintribe.model.processing.query.eval.set.base.AbstractEvalTupleSet;
import com.braintribe.model.processing.query.eval.tools.QueryEvaluationTools;
import com.braintribe.model.queryplan.set.Intersection;
import com.braintribe.model.queryplan.set.TupleSet;

/**
 * 
 */
public class EvalIntersection extends AbstractEvalTupleSet {

	protected final Set<Tuple> tuples;

	public EvalIntersection(Intersection intersection, QueryEvaluationContext context) {
		super(context);

		Set<Tuple> firstTuples = QueryEvaluationTools.tupleHashSet(context.totalComponentsCount());
		Set<Tuple> intersectionTuples = QueryEvaluationTools.tupleHashSet(context.totalComponentsCount());

		addTuples(intersection.getFirstOperand(), firstTuples);
		retainAll(intersection.getSecondOperand(), firstTuples, intersectionTuples);

		tuples = intersectionTuples;
	}

	private void addTuples(TupleSet tupleSet, Set<Tuple> set) {
		QueryEvaluationTools.addAllTuples(tupleSet, set, context);
	}

	private void retainAll(TupleSet tupleSet, Set<Tuple> firstTuples, Set<Tuple> intersectionTuples) {
		EvalTupleSet evalTupleSet = context.resolveTupleSet(tupleSet);

		for (Tuple tuple: evalTupleSet) {
			if (firstTuples.contains(tuple)) {
				intersectionTuples.add(tuple.detachedCopy());
			}
		}
	}

	@Override
	public Iterator<Tuple> iterator() {
		return tuples.iterator();
	}

}
