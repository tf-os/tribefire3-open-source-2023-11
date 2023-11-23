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

import com.braintribe.model.processing.query.eval.api.QueryEvaluationContext;
import com.braintribe.model.processing.query.eval.api.Tuple;
import com.braintribe.model.processing.query.eval.tuple.ArrayBasedTuple;
import com.braintribe.model.queryplan.set.join.Join;

/**
 * 
 */
public abstract class AbstractEvalIndexJoin extends AbstractEvalJoin {

	protected final int joinIndex;

	public AbstractEvalIndexJoin(Join join, QueryEvaluationContext context) {
		super(join, context);

		this.joinIndex = join.getIndex();
	}

	@Override
	public Iterator<Tuple> iterator() {
		return new IndexJoinIterator();
	}

	protected class IndexJoinIterator extends AbstractJoinIterator {
		private Iterator<Tuple> joinTuplesIterator;

		protected IndexJoinIterator() {
			super.initialize();
		}

		@Override
		protected void onNewOperandTuple(ArrayBasedTuple tuple) {
			joinTuplesIterator = joinTuplesFor(tuple);
		}

		@Override
		protected boolean hasNextJoinedValue() {
			return joinTuplesIterator.hasNext();
		}

		@Override
		protected void setNextJoinedValue(ArrayBasedTuple tuple) {
			tuple.setValueDirectly(componentPosition, joinTuplesIterator.next().getValue(joinIndex));
		}

	}

	abstract protected Iterator<Tuple> joinTuplesFor(Tuple tuple);

}
