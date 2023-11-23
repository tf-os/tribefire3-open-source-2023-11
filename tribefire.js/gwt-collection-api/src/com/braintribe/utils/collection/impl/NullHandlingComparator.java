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
package com.braintribe.utils.collection.impl;

import java.util.Comparator;

/**
 * A wrapper for a standard keyComparator, but this one is capable of comparing
 * <tt>null<tt>s, specifically in such way, that <tt>null</tt> is the smallest value.
 */
public final class NullHandlingComparator<T> implements Comparator<T> {

	private final Comparator<T> comparator;

	public NullHandlingComparator(Comparator<? super T> comparator) {
		super();

		Comparator<T> c = (Comparator<T>) comparator;

		// just in case, to avoid unnecessary useless decoration
		this.comparator = c instanceof NullHandlingComparator ? ((NullHandlingComparator<T>) c).comparator : c;
	}

	@Override
	public int compare(T a, T b) {
		if (a == null) {
			return b == null ? 0 : -1;

		} else {
			return b == null ? 1 : comparator.compare(a, b);
		}
	}

}
