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
package com.braintribe.cc.lcd;

public class HashComparatorImmutableEqProxy<T> implements EqProxy<T> {

	private HashingComparator<? super T> comparator;
	private T subject;
	private int hash;

	public HashComparatorImmutableEqProxy(HashingComparator<? super T> comparator, T subject) {
		super();
		this.comparator = comparator;
		this.subject = subject;
		this.hash = comparator.computeHash(subject);
	}

	@Override
	public T get() {
		return subject;
	}

	@Override
	public int hashCode() {
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		return comparator.compare(subject, ((EqProxy<T>) obj).get());
	}
}
