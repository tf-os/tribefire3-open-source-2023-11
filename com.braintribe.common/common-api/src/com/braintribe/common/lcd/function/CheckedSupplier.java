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
package com.braintribe.common.lcd.function;

import jsinterop.annotations.JsType;

/**
 * @author peter.gazdik
 */
@FunctionalInterface
@JsType(namespace = "$tf.util")
public interface CheckedSupplier<T, E extends Throwable> {

	T get() throws E;

	default T uncheckedGet() {
		try {
			return get();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Simple method to wrap supplier code into unchecked one. Example: {@code
	 * 	value1 = CheckSupplier.uncheckedGet(supplier::exceptionThrowingSupplierMethod);
	 * 	value2 = CheckSupplier.uncheckedGet(() -> supplier.exceptionThrowingSupplierMethod(arg1,...));
	 * }
	 * 
	 */
	static <T> T uncheckedGet(CheckedSupplier<T, ?> supplier) {
		return supplier.uncheckedGet();
	}

}
