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
package com.braintribe.model.processing.query.eval.tools;

import com.braintribe.cc.lcd.HashingComparator;
import com.braintribe.model.processing.query.eval.api.Tuple;

/**
 * 
 */
public class TupleHashingComparator implements HashingComparator<Tuple> {

	private int tupleSize;

	public TupleHashingComparator(int tupleSize) {
		this.tupleSize = tupleSize;
	}

	@Override
	public boolean compare(Tuple t1, Tuple t2) {
		for (int i = 0; i < tupleSize; i++) {
			Object o1 = t1.getValue(i);
			Object o2 = t2.getValue(i);

			if (!compareValues(o1, o2)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public int computeHash(Tuple t) {
		int result = 0;

		for (int i = 0; i < tupleSize; i++) {
			Object o1 = t.getValue(i);
			result = 31 * result + hashValue(o1);
		}

		return result;
	}

	private boolean compareValues(Object o1, Object o2) {
		if (o1 == o2) {
			return true;
		}

		return o1 == null ? false : o1.equals(o2);
	}

	private int hashValue(Object o1) {
		return o1 == null ? 1 : o1.hashCode();
	}

}
