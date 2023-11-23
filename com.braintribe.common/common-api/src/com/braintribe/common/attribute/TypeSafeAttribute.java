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

import jsinterop.annotations.JsType;
import jsinterop.context.JsInteropNamespaces;

/**
 * This interface is the super type of all interfaces that will act as a key and type qualifier for generic attributes.
 * 
 * @author Dirk Scheffler
 *
 * @param <T> qualifies the type of the attribute's value and is used in generic methods to correlate the key with the value type.
 * 
 * @see AttributeAccessor
 */
@JsType(namespace = JsInteropNamespaces.attr)
public interface TypeSafeAttribute<T> {
	// empty
}
