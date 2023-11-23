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

import java.util.Objects;
import java.util.function.Consumer;

import jsinterop.annotations.JsType;

/**
 * @author peter.gazdik
 */
@FunctionalInterface
@JsType(namespace = "$tf.util")
public interface CheckedTriConsumer<T, U, V, E extends Throwable> {

	void accept(T t, U u, V v) throws E;

	/**
	 * Similar to {@link Consumer#andThen(Consumer)}
	 */
	default CheckedTriConsumer<T, U, V, E> andThen(CheckedTriConsumer<? super T, ? super U, ? super V, ? extends E> after) {
		Objects.requireNonNull(after);
		return (t, u, v) -> {
			accept(t, u, v);
			after.accept(t, u, v);
		};
	}

}
