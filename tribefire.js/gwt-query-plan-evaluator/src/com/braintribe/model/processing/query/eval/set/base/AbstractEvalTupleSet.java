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
package com.braintribe.model.processing.query.eval.set.base;

import java.util.Iterator;

import com.braintribe.model.processing.query.eval.api.EvalTupleSet;
import com.braintribe.model.processing.query.eval.api.QueryEvaluationContext;
import com.braintribe.model.processing.query.eval.api.Tuple;
import com.braintribe.model.processing.query.eval.tuple.ArrayBasedTuple;

/**
 * 
 */
public abstract class AbstractEvalTupleSet implements EvalTupleSet {

	protected QueryEvaluationContext context;

	protected AbstractEvalTupleSet(QueryEvaluationContext context) {
		this.context = context;
	}

	protected <T> T cast(Object o) {
		return (T) o;
	}

	protected abstract class AbstractTupleIterator implements Iterator<Tuple> {

		protected Tuple next;
		private final ArrayBasedTuple result = new ArrayBasedTuple(totalComponentsCount());

		protected int totalComponentsCount() {
			return context.totalComponentsCount();
		}

		@Override
		public boolean hasNext() {
			return next != null;
		}

		@Override
		public Tuple next() {
			if (!hasNext())
				throw new IllegalStateException("No next found for iterator!");

			result.acceptAllValuesFrom(next);
			prepareNextValue();

			return result;
		}

		protected abstract void prepareNextValue();

		@Override
		public final void remove() {
			throw new UnsupportedOperationException("Cannot remove a tuple from a tuple set!");
		}

	}

}
