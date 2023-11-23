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
package com.braintribe.model.query.smart.processing.eval.set;

import java.util.Iterator;

import com.braintribe.model.processing.query.eval.api.Tuple;
import com.braintribe.model.processing.query.eval.set.HasMoreAwareSet;
import com.braintribe.model.processing.query.eval.set.base.TransientGeneratorEvalTupleSet;
import com.braintribe.model.processing.smartquery.eval.api.SmartQueryEvaluationContext;
import com.braintribe.model.smartqueryplan.ScalarMapping;
import com.braintribe.model.smartqueryplan.set.StaticTuple;

public class EvalStaticTuple extends TransientGeneratorEvalTupleSet implements HasMoreAwareSet {

	protected final StaticTuple staticTuple;
	protected final SmartQueryEvaluationContext smartContext;

	public EvalStaticTuple(StaticTuple staticTuple, SmartQueryEvaluationContext context) {
		super(context);

		this.staticTuple = staticTuple;
		this.smartContext = context;

	}

	@Override
	public boolean hasMore() {
		return false;
	}

	@Override
	public Iterator<Tuple> iterator() {
		return new EvalTupleIterator();
	}

	private class EvalTupleIterator implements Iterator<Tuple> {
		boolean hasNext = !staticTuple.getScalarMappings().isEmpty();

		@Override
		public boolean hasNext() {
			return hasNext;
		}

		@Override
		public Tuple next() {
			for (ScalarMapping mapping: staticTuple.getScalarMappings()) {
				Object resolvedValue = context.resolveValue(null, mapping.getSourceValue());
				singletonTuple.setValueDirectly(mapping.getTupleComponentIndex(), resolvedValue);
			}

			hasNext = false;

			return singletonTuple;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("Cannot remove a tuple from a tuple set!");
		}
	}
}
