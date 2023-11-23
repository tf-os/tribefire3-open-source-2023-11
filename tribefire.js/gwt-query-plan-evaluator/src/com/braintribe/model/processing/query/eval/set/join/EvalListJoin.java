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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import com.braintribe.model.processing.query.eval.api.QueryEvaluationContext;
import com.braintribe.model.processing.query.eval.api.Tuple;
import com.braintribe.model.processing.query.eval.tuple.ArrayBasedTuple;
import com.braintribe.model.queryplan.set.join.JoinedListIndex;
import com.braintribe.model.queryplan.set.join.ListJoin;

/**
 * 
 */
public class EvalListJoin extends AbstractEvalPropertyJoin {

	protected final JoinedListIndex listIndex;

	public EvalListJoin(ListJoin join, QueryEvaluationContext context) {
		super(join, context);

		this.listIndex = join.getListIndex();
	}

	@Override
	public Iterator<Tuple> iterator() {
		return new ListIterator();
	}

	protected class ListIterator extends AbstractPropertyJoinIterator {
		private Iterator<?> indexedValuesIterator;
		private int counter;

		@Override
		protected void onNewOperandTuple$LeftInner(ArrayBasedTuple tuple) {
			Collection<?> c = context.resolveValue(tuple, valueProperty);
			indexedValuesIterator = c != null ? c.iterator() : Collections.emptySet().iterator();
			counter = 0;
		}

		@Override
		protected boolean hasNextJoinedValue$LeftInner() {
			return indexedValuesIterator.hasNext();
		}

		@Override
		protected void setNextJoinedValue$LeftInner(ArrayBasedTuple tuple) {
			tuple.setValueDirectly(listIndex.getIndex(), counter++);
			setRightValue(tuple, indexedValuesIterator.next());
		}

		@Override
		protected void setNextJoinedValueAsVoid(ArrayBasedTuple tuple) {
			tuple.setValueDirectly(listIndex.getIndex(), null);
			tuple.setValueDirectly(componentPosition, null);
		}

	}

}
