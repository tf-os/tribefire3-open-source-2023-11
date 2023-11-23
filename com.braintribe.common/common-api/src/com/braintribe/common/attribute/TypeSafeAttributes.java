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
package com.braintribe.common.attribute;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Supplier;

import jsinterop.annotations.JsType;
import jsinterop.context.JsInteropNamespaces;

/**
 * TypeSafeAttributes
 * 
 * @author Dirk Scheffler
 *
 */
@JsType(namespace = JsInteropNamespaces.attr)
public interface TypeSafeAttributes {

	default <A extends TypeSafeAttribute<V>, V> V getAttribute(Class<A> attribute) {
		return findAttribute(attribute).orElseThrow(() -> new NoSuchElementException("No value found for attribute: " + attribute.getName()));
	}

	default <A extends TypeSafeAttribute<V>, V> V findOrNull(Class<A> attribute) {
		return findOrDefault(attribute, null);
	}

	default <A extends TypeSafeAttribute<V>, V> V findOrDefault(Class<A> attribute, V defaultValue) {
		return findAttribute(attribute).orElse(defaultValue);
	}

	default <A extends TypeSafeAttribute<V>, V> V findOrSupply(Class<A> attribute, Supplier<V> defaultValueSupplier) {
		return findAttribute(attribute).orElseGet(defaultValueSupplier);
	}

	<A extends TypeSafeAttribute<V>, V> Optional<V> findAttribute(Class<A> attribute);
}
