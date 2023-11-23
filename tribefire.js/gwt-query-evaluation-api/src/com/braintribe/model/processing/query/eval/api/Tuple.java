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
package com.braintribe.model.processing.query.eval.api;

import com.braintribe.model.queryplan.TupleComponentPosition;
import com.braintribe.model.queryplan.set.TupleSet;

/**
 * 
 */
public interface Tuple {

	/**
	 * Returns a value for given position, which is the index from {@link TupleComponentPosition#getIndex()}.
	 */
	Object getValue(int index);

	/**
	 * Returns a copy of this {@linkplain Tuple} that is not attached to any {@link TupleSet} or anything else. This is
	 * only for optimization purposes. In standard cases, the iterators might always return the same tuple instance,
	 * just internally it's state is changed. As long as we only use the tuples for reading the values right away, this
	 * is perfect. If, however, someone wants to e.g. put these tuples from the iterator into a {@link java.util.Set},
	 * he needs to obtain a "detached" equivalent, i.e. an instance which will not be overwritten by any of the
	 * iterators later.
	 */
	Tuple detachedCopy();
}
