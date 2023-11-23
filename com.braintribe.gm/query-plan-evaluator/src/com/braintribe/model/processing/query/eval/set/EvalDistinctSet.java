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
import com.braintribe.model.queryplan.set.DistinctSet;

/**
 * 
 */
public class EvalDistinctSet extends AbstractEvalTupleSet {

	protected final EvalTupleSet evalOperand;
	protected final int tupleSize;

	public EvalDistinctSet(DistinctSet distinctSet, QueryEvaluationContext context) {
		super(context);

		int _tupleSize = distinctSet.getTupleSize();

		this.evalOperand = context.resolveTupleSet(distinctSet.getOperand());
		this.tupleSize = _tupleSize == 0 ? context.resultComponentsCount() : _tupleSize;
	}

	@Override
	public Iterator<Tuple> iterator() {
		return new DistinctSetIterator();
	}

	protected class DistinctSetIterator extends AbstractTupleIterator {
		protected final Set<Tuple> alreadyReturnedTuples;
		protected final Iterator<Tuple> delegateIterator;

		public DistinctSetIterator() {
			alreadyReturnedTuples = QueryEvaluationTools.tupleHashSet(tupleSize);
			delegateIterator = evalOperand.iterator();

			next = delegateIterator.hasNext() ? delegateIterator.next() : null;
		}

		@Override
		protected int totalComponentsCount() {
			return tupleSize;
		}

		@Override
		protected void prepareNextValue() {
			alreadyReturnedTuples.add(next.detachedCopy());

			while (delegateIterator.hasNext()) {
				Tuple tuple = delegateIterator.next();
				if (!alreadyReturnedTuples.contains(tuple)) {
					next = tuple;
					return;
				}
			}
			next = null;
		}
	}

}
