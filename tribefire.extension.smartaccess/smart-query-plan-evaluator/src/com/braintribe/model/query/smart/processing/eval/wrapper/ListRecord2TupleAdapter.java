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
package com.braintribe.model.query.smart.processing.eval.wrapper;

import com.braintribe.model.processing.query.eval.api.Tuple;
import com.braintribe.model.record.ListRecord;

public class ListRecord2TupleAdapter implements Tuple {

	private final ListRecord listRecord;
	private final int maxIndex;

	public ListRecord2TupleAdapter(ListRecord listRecord) {
		this.listRecord = listRecord;
		this.maxIndex = listRecord.getValues().size();
	}

	@Override
	public Object getValue(int index) {
		if ((index >= 0) && (index < maxIndex)) {
			return listRecord.getValues().get(index);

		}

		return null;
	}

	@Override
	public Tuple detachedCopy() {
		DetachedTuple result = new DetachedTuple(this.listRecord);
		return result;
	}

	@Override
	public String toString() {
		return "Tuple" + (listRecord.getValues());
	}

	protected static class DetachedTuple extends ListRecord2TupleAdapter {
		DetachedTuple(ListRecord record) {
			super(record);
		}

		@Override
		public Tuple detachedCopy() {
			return this;
		}
	}

}
