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

import java.util.function.Supplier;

/**
 * In case you just want to give a value without any special {@link #release()} handling, use {@link Holder}.
 */
public interface ManagedValue<T> extends Supplier<T> {

	void release();

	static <V> ManagedValue<V> of(V value) {
		return new Holder<>(value);
	}

	static <V> ManagedValue<V> fromSuplier(Supplier<? extends V> delegate) {
		return new ManagedValue<V>() {
			@Override
			public V get() {
				return delegate.get();
			}

			@Override
			public void release() {
				/* NOOP */
			}
		};
	}

}
