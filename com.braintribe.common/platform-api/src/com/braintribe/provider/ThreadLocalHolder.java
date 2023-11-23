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
package com.braintribe.provider;

/**
 * Implementation of the {@link Hub} interface that stores the value in a {@link ThreadLocal} variable.
 *
 * @author roman.kurmanowytsch
 *
 * @param <T>
 *            The type of the object that should be stored in this Hub.
 */
public class ThreadLocalHolder<T> implements Hub<T>, ManagedValue<T> {

	private ThreadLocal<T> tl = new ThreadLocal<>();

	@Override
	public T get() {
		return tl.get();
	}

	@Override
	public void accept(T value) {
		if (value == null) {
			tl.remove();
		} else {
			tl.set(value);
		}
	}

	@Override
	public void release() {
		tl.remove();
	}

}
