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
package com.braintribe.common.equalscheck;

import java.util.Comparator;

import com.braintribe.common.lcd.equalscheck.ComparatorBasedEqualsCheck;

/**
 * Default implementation of {@link ComparatorBasedEqualsCheck}.
 *
 * @author michael.lafite
 *
 * @param <T>
 *            the type of the objects to check for equality (and also the type of the objects that may be compared by the comparator this check is
 *            based on)
 */
public class DefaultComparatorBasedEqualsCheck<T> implements ComparatorBasedEqualsCheck<T> {

	private final Comparator<T> delegate;

	private final Class<T> comparedType;

	public DefaultComparatorBasedEqualsCheck(final Comparator<T> delegate, final Class<T> comparedType) {
		this.delegate = delegate;
		this.comparedType = comparedType;
	}

	/**
	 * The comparator passed to the {@link DefaultComparatorBasedEqualsCheck#DefaultComparatorBasedEqualsCheck(Comparator, Class) constructor}.
	 */
	public Comparator<T> getDelegate() {
		return this.delegate;
	}

	/**
	 * Returns <code>true</code>, if the result of {@link #compare(Object, Object)} is <code>0</code>, otherwise <code>false</code>. The method also
	 * returns <code>false</code>, if <code>object2</code> is not an instance of <code>T</code>.
	 */
	@Override
	public boolean equals(final T object1, final Object object2) {
		if (object1 == null) {
			return (object2 == null);
		}

		if (!this.comparedType.isInstance(object2)) {
			return false;
		}
		return compare(object1, this.comparedType.cast(object2)) == 0;
	}

	/**
	 * Delegates to the {@link #getDelegate() delegate comparator}.
	 */

	@Override
	public int compare(final T object1, final T object2) {
		return getDelegate().compare(object1, object2);
	}
}
