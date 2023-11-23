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

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Holders hold a property. This property is set via the constructor, but can also be set via the {@link #accept(Object)} method.
 *
 * @author michel.docouto
 *
 * @param <E>
 *            The type
 */
public class Holder<E> implements Hub<E>, ManagedValue<E> {

	protected E value;

	public Holder() {
	}

	public Holder(E object) {
		this.value = object;
	}

	public static <V> Holder<V> of(V value) {
		return new Holder<>(value);
	}

	public static <V> Holder<V> fromSuplier(Supplier<? extends V> delegate) {
		return new Holder<V>() {
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

	@Override
	public void accept(E object) {
		this.value = object;
	}

	@Override
	public E get() {
		return value;
	}

	@Override
	public void release() {
		value = null;
	}

	/**
	 * Sets the value of this holder. This method is kept here because some Spring-based tests rely on this setter method.
	 *
	 * @deprecated use {@link Consumer#accept(Object)} interface
	 */
	@Deprecated
	public void setObject(E object) {
		this.value = object;
	}
}
