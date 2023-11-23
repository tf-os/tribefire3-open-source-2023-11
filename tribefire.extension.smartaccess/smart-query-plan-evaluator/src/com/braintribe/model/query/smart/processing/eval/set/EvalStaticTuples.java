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

import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.Iterator;
import java.util.List;

import com.braintribe.model.processing.query.eval.api.Tuple;
import com.braintribe.model.processing.query.eval.set.HasMoreAwareSet;
import com.braintribe.model.processing.query.eval.set.base.AbstractEvalTupleSet;
import com.braintribe.model.processing.smartquery.eval.api.SmartQueryEvaluationContext;
import com.braintribe.model.smartqueryplan.set.StaticTuple;
import com.braintribe.model.smartqueryplan.set.StaticTuples;

public class EvalStaticTuples extends AbstractEvalTupleSet implements HasMoreAwareSet {

	protected final List<EvalStaticTuple> evalStaticTuples = newList();

	public EvalStaticTuples(StaticTuples staticTuples, SmartQueryEvaluationContext context) {
		super(context);

		for (StaticTuple staticTuple: staticTuples.getTuples()) {
			evalStaticTuples.add(new EvalStaticTuple(staticTuple, context));
		}
	}

	@Override
	public boolean hasMore() {
		return false;
	}

	@Override
	public Iterator<Tuple> iterator() {
		return new EvalTuplesIterator();
	}

	private class EvalTuplesIterator implements Iterator<Tuple> {
		Iterator<EvalStaticTuple> staticTupleIterator = evalStaticTuples.iterator();

		@Override
		public boolean hasNext() {
			return staticTupleIterator.hasNext();
		}

		@Override
		public Tuple next() {
			return staticTupleIterator.next().iterator().next();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("Cannot remove a tuple from a tuple set!");
		}
	}
}
