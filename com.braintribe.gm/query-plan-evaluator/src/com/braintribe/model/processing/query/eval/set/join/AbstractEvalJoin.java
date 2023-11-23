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
package com.braintribe.model.processing.query.eval.set.join;

import java.util.Iterator;

import com.braintribe.model.processing.query.eval.api.EvalTupleSet;
import com.braintribe.model.processing.query.eval.api.QueryEvaluationContext;
import com.braintribe.model.processing.query.eval.api.Tuple;
import com.braintribe.model.processing.query.eval.set.base.TransientGeneratorEvalTupleSet;
import com.braintribe.model.processing.query.eval.set.join.AbstractEvalIndexJoin.IndexJoinIterator;
import com.braintribe.model.processing.query.eval.set.join.AbstractEvalPropertyJoin.AbstractPropertyJoinIterator;
import com.braintribe.model.processing.query.eval.set.join.AbstractEvalPropertyJoin.AbstractPropertyJoinLeftOuterSupportingIterator;
import com.braintribe.model.processing.query.eval.tuple.ArrayBasedTuple;
import com.braintribe.model.queryplan.set.join.Join;

/**
 * 
 */
public abstract class AbstractEvalJoin extends TransientGeneratorEvalTupleSet {

	protected final EvalTupleSet operand;
	protected final int componentPosition;
	protected ArrayBasedTuple nextTuple;

	public AbstractEvalJoin(Join join, QueryEvaluationContext context) {
		super(context);

		this.operand = context.resolveTupleSet(join.getOperand());
		this.componentPosition = join.getIndex();
	}

	/**
	 * Base class for join iterators.
	 * 
	 * @see IndexJoinIterator
	 * @see AbstractPropertyJoinIterator
	 * @see AbstractPropertyJoinLeftOuterSupportingIterator
	 */
	protected abstract class AbstractJoinIterator extends AbstractTupleIterator {
		protected Iterator<Tuple> operandIterator;

		protected AbstractJoinIterator() {
			operandIterator = operand.iterator();
		}

		protected void initialize() {
			if (prepareNextOperandTuple())
				prepareNextValue();
		}

		@Override
		protected void prepareNextValue() {
			while (!hasNextJoinedValue()) {
				if (!prepareNextOperandTuple()) {
					next = null;
					return;
				}
			}

			setNextJoinedValue(nextTuple);
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

				onNewOperandTuple(nextTuple);
				return true;

			} else {
				return false;
			}
		}

		protected abstract void onNewOperandTuple(ArrayBasedTuple tuple);

		protected abstract boolean hasNextJoinedValue();

		protected abstract void setNextJoinedValue(ArrayBasedTuple tuple);
	}

}
