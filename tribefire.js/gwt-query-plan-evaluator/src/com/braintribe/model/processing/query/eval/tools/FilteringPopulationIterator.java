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

import java.util.Iterator;
import java.util.Set;

/**
 * An {@link Iterator} that returns items from another (delegate) iterator which are not contained within a given
 * {@link Set}.
 */
public class FilteringPopulationIterator<T> implements Iterator<T> {

	private final Iterator<? extends T> delegateIterator;
	private final Set<?> excludedItems;

	private T next;

	public FilteringPopulationIterator(Iterator<? extends T> delegateIterator, Set<?> excludedItems) {
		this.delegateIterator = delegateIterator;
		this.excludedItems = excludedItems;

		prepareNextValue();
	}

	@Override
	public boolean hasNext() {
		return next != null;
	}

	@Override
	public T next() {
		T result = next;
		prepareNextValue();

		return result;
	}

	protected void prepareNextValue() {
		while (delegateIterator.hasNext()) {
			T itNext = delegateIterator.next();

			if (!excludedItems.contains(itNext)) {
				next = itNext;
				return;
			}
		}

		next = null;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("Cannot remove a tuple from a tuple set!");
	}
}
