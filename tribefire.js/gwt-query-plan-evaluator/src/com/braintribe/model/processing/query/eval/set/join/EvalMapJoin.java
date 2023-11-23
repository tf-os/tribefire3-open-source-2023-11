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

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import com.braintribe.model.processing.query.eval.api.QueryEvaluationContext;
import com.braintribe.model.processing.query.eval.api.Tuple;
import com.braintribe.model.processing.query.eval.tuple.ArrayBasedTuple;
import com.braintribe.model.queryplan.set.join.JoinedMapKey;
import com.braintribe.model.queryplan.set.join.MapJoin;

/**
 * 
 */
public class EvalMapJoin extends AbstractEvalPropertyJoin {

	protected final JoinedMapKey mapKey;

	public EvalMapJoin(MapJoin join, QueryEvaluationContext context) {
		super(join, context);

		this.mapKey = join.getMapKey();
	}

	@Override
	public Iterator<Tuple> iterator() {
		return new MapIterator();
	}

	protected class MapIterator extends AbstractPropertyJoinIterator {
		private Iterator<Map.Entry<Object, Object>> entriesIterator;

		@Override
		protected void onNewOperandTuple$LeftInner(ArrayBasedTuple tuple) {
			Map<Object, Object> m = context.resolveValue(tuple, valueProperty);
			m = m != null ? m : Collections.emptyMap();
			entriesIterator = m.entrySet().iterator();
		}

		@Override
		protected boolean hasNextJoinedValue$LeftInner() {
			return entriesIterator.hasNext();
		}

		@Override
		protected void setNextJoinedValue$LeftInner(ArrayBasedTuple tuple) {
			Map.Entry<?, ?> nextEntry = entriesIterator.next();

			tuple.setValueDirectly(mapKey.getIndex(), nextEntry.getKey());
			setRightValue(tuple, nextEntry.getValue());
		}

		@Override
		protected void setNextJoinedValueAsVoid(ArrayBasedTuple tuple) {
			tuple.setValueDirectly(mapKey.getIndex(), null);
			tuple.setValueDirectly(componentPosition, null);
		}
	}

}
