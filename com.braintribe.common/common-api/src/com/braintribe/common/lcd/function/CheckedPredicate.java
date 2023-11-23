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
import java.util.function.Predicate;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsType;

/**
 * @author peter.gazdik
 */
@FunctionalInterface
@JsType(namespace = "$tf.util")
public interface CheckedPredicate<T, E extends Throwable> {

	boolean test(T t) throws E;

	default CheckedPredicate<T, E> negate() {
		return t -> !test(t);
	}

	default CheckedPredicate<T, E> and(CheckedPredicate<? super T, ? extends E> other) {
		Objects.requireNonNull(other);
		return t -> test(t) && other.test(t);
	}

	@JsIgnore
	default CheckedPredicate<T, E> and(Predicate<? super T> other) {
		Objects.requireNonNull(other);
		return t -> test(t) && other.test(t);
	}

	default CheckedPredicate<T, E> or(CheckedPredicate<? super T, ? extends E> other) {
		Objects.requireNonNull(other);
		return t -> test(t) || other.test(t);
	}

	@JsIgnore
	default CheckedPredicate<T, E> or(Predicate<? super T> other) {
		Objects.requireNonNull(other);
		return t -> test(t) || other.test(t);
	}

}
