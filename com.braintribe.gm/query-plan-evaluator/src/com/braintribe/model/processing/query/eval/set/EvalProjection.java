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

import static com.braintribe.model.processing.query.eval.tools.QueryEvaluationTools.moreHas;

import java.util.Iterator;
import java.util.List;

import com.braintribe.model.processing.query.eval.api.EvalTupleSet;
import com.braintribe.model.processing.query.eval.api.QueryEvaluationContext;
import com.braintribe.model.processing.query.eval.api.Tuple;
import com.braintribe.model.processing.query.eval.set.base.AbstractEvalTupleSet;
import com.braintribe.model.processing.query.eval.tuple.ArrayBasedTuple;
import com.braintribe.model.queryplan.set.Projection;
import com.braintribe.model.queryplan.value.Value;

/**
 * 
 */
public class EvalProjection extends AbstractEvalTupleSet implements HasMoreAwareSet {

	protected final EvalTupleSet evalOperand;
	protected final List<Value> values;
	protected final ArrayBasedTuple singletonTuple;

	public EvalProjection(Projection projection, QueryEvaluationContext context) {
		super(context);

		this.evalOperand = context.resolveTupleSet(projection.getOperand());
		this.values = projection.getValues();
		this.singletonTuple = new ArrayBasedTuple(values.size());
	}

	@Override
	public boolean hasMore() {
		return moreHas(evalOperand);
	}

	@Override
	public Iterator<Tuple> iterator() {
		return new ProjectionIterator();
	}

	protected class ProjectionIterator extends AbstractTupleIterator {

		protected Iterator<Tuple> delegateIterator;

		public ProjectionIterator() {
			delegateIterator = evalOperand.iterator();
			prepareNextValue();
		}

		@Override
		protected int totalComponentsCount() {
			return values.size();
		}

		@Override
		protected void prepareNextValue() {
			if (delegateIterator.hasNext()) {
				Tuple tuple = delegateIterator.next();

				int index = 0;
				for (Value value: values) {
					Object resolvedValue = context.resolveValue(tuple, value);
					singletonTuple.setValueDirectly(index++, resolvedValue);
				}

				next = singletonTuple;

			} else {
				next = null;
			}
		}

	}
}
