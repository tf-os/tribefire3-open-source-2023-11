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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.braintribe.model.processing.query.eval.api.EvalTupleSet;
import com.braintribe.model.processing.query.eval.api.QueryEvaluationContext;
import com.braintribe.model.processing.query.eval.api.Tuple;
import com.braintribe.model.processing.query.eval.set.base.TransientGeneratorEvalTupleSet;
import com.braintribe.model.processing.query.eval.tools.QueryEvaluationTools;
import com.braintribe.model.queryplan.set.CartesianProduct;

/**
 * 
 */
public class EvalCartesianProduct extends TransientGeneratorEvalTupleSet {

	protected final List<EvalTupleSet> operands;

	public EvalCartesianProduct(CartesianProduct cartesianProduct, QueryEvaluationContext context) {
		super(context);
		this.operands = QueryEvaluationTools.resolveTupleSets(cartesianProduct.getOperands(), context);
	}

	@Override
	public Iterator<Tuple> iterator() {
		return new CartesianProductIterator();
	}

	protected class CartesianProductIterator extends AbstractTupleIterator {

		protected List<Iterator<Tuple>> iterators;
		protected List<Tuple> nexts;

		public CartesianProductIterator() {
			iterators = new ArrayList<Iterator<Tuple>>(operands.size());
			nexts = new ArrayList<Tuple>(operands.size());

			for (EvalTupleSet operand: operands) {
				Iterator<Tuple> it = operand.iterator();

				if (!it.hasNext()) {
					return;
				}

				iterators.add(it);
				nexts.add(it.next());
			}

			next = singletonTuple;
			buildNext();
		}

		@Override
		protected void prepareNextValue() {
			if (moveIterators()) {
				buildNext();
			} else {
				next = null;
			}
		}

		protected boolean moveIterators() {
			return moveIterators(iterators.size() - 1);
		}

		private boolean moveIterators(int position) {
			if (position < 0) {
				return false;
			}

			Iterator<Tuple> it = iterators.get(position);
			if (it.hasNext()) {
				nexts.set(position, it.next());
				return true;
			}

			if (!moveIterators(position - 1)) {
				return false;
			}

			it = cast(operands.get(position).iterator());

			iterators.set(position, it);
			nexts.set(position, it.next());

			return true;
		}

		/* this code assumes the iterator provides at least two elements, because otherwise the Cartesian product would
		 * make no sense */
		private void buildNext() {
			Iterator<Tuple> it = nexts.iterator();

			/* Note that we set the AbstractTupleIterator.tuple = singletonTuple in the constructor, so this is dealing
			 * with the right tuple instance */
			singletonTuple.acceptAllValuesFrom(it.next());

			do {
				singletonTuple.acceptValuesFrom(it.next());

			} while (it.hasNext());
		}

	}

}
