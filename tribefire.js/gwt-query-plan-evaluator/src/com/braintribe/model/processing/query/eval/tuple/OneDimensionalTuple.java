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
package com.braintribe.model.processing.query.eval.tuple;

import com.braintribe.model.processing.query.eval.api.RuntimeQueryEvaluationException;
import com.braintribe.model.processing.query.eval.api.Tuple;

/**
 * Just a memory optimization for the most simple types of tuples. Not sure whether this is even needed.
 */
public class OneDimensionalTuple implements Tuple {

	protected Object data;
	protected final int position;

	public OneDimensionalTuple(int position) {
		this.position = position;
	}

	public void setValueDirectly(int index, Object value) {
		if (position != index)
			throw new RuntimeQueryEvaluationException(
					"Cannot set value on position '" + index + "'. This tuple represents only the position: " + position);

		data = value;
	}

	@Override
	public Object getValue(int index) {
		return position == index ? data : null;
	}

	@Override
	public Tuple detachedCopy() {
		DetachedTuple result = new DetachedTuple(position);
		result.data = this.data;

		return result;
	}

	protected static class DetachedTuple extends OneDimensionalTuple {
		DetachedTuple(int position) {
			super(position);
		}

		@Override
		public Tuple detachedCopy() {
			return this;
		}
	}

}
