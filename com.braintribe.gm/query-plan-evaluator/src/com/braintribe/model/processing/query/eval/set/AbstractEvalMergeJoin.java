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
import com.braintribe.model.processing.query.eval.set.base.TransientGeneratorEvalTupleSet;
import com.braintribe.model.processing.query.eval.tuple.ArrayBasedTuple;
import com.braintribe.model.queryplan.set.MergeJoin;

/**
 * 
 */
public abstract class AbstractEvalMergeJoin extends TransientGeneratorEvalTupleSet {

	protected final EvalTupleSet operand;
	protected ArrayBasedTuple nextTuple;

	public AbstractEvalMergeJoin(MergeJoin tupleSet, QueryEvaluationContext context) {
		super(context);

		this.operand = context.resolveTupleSet(tupleSet.getOperand());
	}

	@Override
	public Iterator<Tuple> iterator() {
		return new IndexJoinIterator();
	}

	protected class IndexJoinIterator extends AbstractTupleIterator {
		private Iterator<Tuple> operandIterator;
		private Iterator<Tuple> joinTuplesIterator;

		protected IndexJoinIterator() {
			this.operandIterator = operand.iterator();

			this.initialize();
		}

		protected void initialize() {
			if (prepareNextOperandTuple()) {
				prepareNextValue();
			}
		}

		@Override
		protected void prepareNextValue() {
			while (!joinTuplesIterator.hasNext()) {
				if (!prepareNextOperandTuple()) {
					next = null;
					return;
				}
			}

			nextTuple.acceptValuesFrom(joinTuplesIterator.next());
			next = nextTuple;
		}

		protected boolean prepareNextOperandTuple() {
			if (operandIterator.hasNext()) {
				Tuple operandNext = operandIterator.next();

				if (operandNext instanceof ArrayBasedTuple) {
					nextTuple = (ArrayBasedTuple) operandNext;

				} else {
					singletonTuple.acceptAllValuesFrom(operandNext);
					nextTuple = singletonTuple;
				}

				joinTuplesIterator = joinTuplesFor(nextTuple);
				return true;

			} else {
				return false;
			}
		}

	}

	protected abstract Iterator<Tuple> joinTuplesFor(Tuple tuple);

}
