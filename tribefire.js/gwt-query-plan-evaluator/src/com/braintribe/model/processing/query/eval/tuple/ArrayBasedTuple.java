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

import java.util.Arrays;

import com.braintribe.model.processing.query.eval.api.Tuple;

/**
 * 
 */
public class ArrayBasedTuple implements Tuple {

	protected Object[] data;

	public ArrayBasedTuple(int size) {
		this.data = new Object[size];
	}

	public ArrayBasedTuple(Object[] data) {
		this.data = data;
	}

	public void setValueDirectly(int position, Object value) {
		data[position] = value;
	}

	@Override
	public Object getValue(int index) {
		return data[index];
	}

	public void acceptAllValuesFrom(Tuple other) {
		for (int i = 0; i < data.length; i++) {
			this.data[i] = other.getValue(i);
		}
	}

	public void acceptValuesFrom(Tuple other) {
		for (int i = 0; i < data.length; i++) {
			Object value = other.getValue(i);
			if (value != null)
				this.data[i] = value;
		}
	}

	public void clear() {
		for (int i = 0; i < data.length; i++)
			this.data[i] = null;
	}

	@Override
	public String toString() {
		return "TUPLE" + Arrays.toString(data);
	}

	@Override
	public Tuple detachedCopy() {
		DetachedTuple result = new DetachedTuple(this.data);
		this.data = new Object[this.data.length];

		return result;
	}

	protected static class DetachedTuple extends ArrayBasedTuple {
		DetachedTuple(Object[] data) {
			super(data);
		}

		@Override
		public Tuple detachedCopy() {
			return this;
		}
	}

}
