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
package com.braintribe.model.processing.securityservice.basic;

import java.util.function.Supplier;

public class LazyHolder<T> implements Supplier<T> {
	private T value;
	private boolean initialized = false;
	private Supplier<T> supplier;

	public LazyHolder(Supplier<T> supplier) {
		this.supplier = supplier;
	}

	public static <T> LazyHolder<T> from(Supplier<T> supplier) {
		return new LazyHolder<>(supplier);
	}

	@Override
	public T get() {
		if (!initialized) {
			value = supplier.get();
			initialized = true;
		}

		return value;
	}

	public boolean isInitialized() {
		return initialized;
	}
}