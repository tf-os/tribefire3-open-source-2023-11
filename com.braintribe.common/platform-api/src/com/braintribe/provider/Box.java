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
 * Simple version of a {@link Holder}. The main purpose is to wrap a local variable, so it can be accessed within a lambda even if it's not final
 * (just the box itself is final
 * <p>
 * Of course, only do that if you know what you're doing.
 *
 * @author peter.gazdik
 */
public class Box<T> {

	public T value;

	public static <T> Box<T> of(T value) {
		Box<T> result = new Box<T>();
		result.value = value;

		return result;
	}
}
