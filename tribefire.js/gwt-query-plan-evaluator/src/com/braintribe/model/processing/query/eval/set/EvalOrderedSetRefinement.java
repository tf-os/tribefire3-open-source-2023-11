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

import static com.braintribe.model.processing.query.eval.tools.QueryEvaluationTools.emptyTupleIterator;
import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.braintribe.model.processing.query.eval.api.EvalTupleSet;
import com.braintribe.model.processing.query.eval.api.QueryEvaluationContext;
import com.braintribe.model.processing.query.eval.api.Tuple;
import com.braintribe.model.processing.query.eval.set.base.AbstractEvalTupleSet;
import com.braintribe.model.processing.query.eval.tools.TupleComparator;
import com.braintribe.model.queryplan.set.OrderedSetRefinement;
import com.braintribe.model.queryplan.value.Value;

/**
 * 
 */
public class EvalOrderedSetRefinement extends AbstractEvalTupleSet {


	protected final EvalTupleSet operandTupleSet;
	protected final OrderedSetRefinement orderedSet;
	protected final List<Value> groupValues;

	public EvalOrderedSetRefinement(OrderedSetRefinement orderedSet, QueryEvaluationContext context) {
		super(context);

		this.operandTupleSet = context.resolveTupleSet(orderedSet.getOperand());
		this.orderedSet = orderedSet;
		this.groupValues = orderedSet.getGroupValues();
	}

	@Override
	public Iterator<Tuple> iterator() {
		return new ExtendedOrderedSetIterator();
	}

	private class ExtendedOrderedSetIterator extends AbstractTupleIterator {

		private final Iterator<Tuple> operandIterator;

		private Tuple nextBulkFirstTuple;
		private Iterator<Tuple> currentBulkIterator = emptyTupleIterator();

		public ExtendedOrderedSetIterator() {
			operandIterator = operandTupleSet.iterator();
			nextBulkFirstTuple = operandIterator.hasNext() ? operandIterator.next() : null;
			
			prepareNextValue();
		}

		@Override
		protected void prepareNextValue() {
			if (currentBulkIterator.hasNext()) {
				next = currentBulkIterator.next();
				return;
			}

			if (operandIterator.hasNext() || nextBulkFirstTuple != null) {
				prepareNextBulk();
				next = currentBulkIterator.next();
				return;
			}

			next = null;
		}

		private void prepareNextBulk() {
			List<Tuple> tuples = loadEquivalentTuples();
			Collections.sort(tuples, new TupleComparator(orderedSet.getSortCriteria(), context));
			currentBulkIterator = tuples.iterator();
		}

		private List<Tuple> loadEquivalentTuples() {
			List<Object> groupValues = computeGroupValues(nextBulkFirstTuple);

			List<Tuple> result = newList();

			do {
				result.add(nextBulkFirstTuple.detachedCopy());

				if (operandIterator.hasNext()) {
					nextBulkFirstTuple = operandIterator.next();

				} else {
					nextBulkFirstTuple = null;
					return result;
				}

			} while (groupValues.equals(computeGroupValues(nextBulkFirstTuple)));

			return result;
		}

		private List<Object> computeGroupValues(Tuple tuple) {
			List<Object> result = newList(groupValues.size());

			for (Value value: groupValues)
				result.add(context.resolveValue(tuple, value));

			return result;
		}
	}

}
