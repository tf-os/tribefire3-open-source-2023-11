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
import com.braintribe.model.queryplan.set.IndexOrderedSet;

/** {@link EvalTupleSet} for {@link IndexOrderedSet}. */
public class EvalIndexOrderedSet implements EvalTupleSet {

	protected final Iterable<Tuple> tuples;

	public EvalIndexOrderedSet(IndexOrderedSet indexOrderedSet, QueryEvaluationContext context) {
		tuples = context.getFullRange(indexOrderedSet.getMetricIndex(), indexOrderedSet.getDescending());
	}

	@Override
	public Iterator<Tuple> iterator() {
		return tuples.iterator();
	}

}
