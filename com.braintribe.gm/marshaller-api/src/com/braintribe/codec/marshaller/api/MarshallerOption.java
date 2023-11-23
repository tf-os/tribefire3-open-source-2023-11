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
package com.braintribe.codec.marshaller.api;

import com.braintribe.common.attribute.TypeSafeAttribute;

/**
 * This interface marks a {@link TypeSafeAttribute} which is usually meant to be supported by certain
 * {@link Marshaller}s. It has rather semantic than functional meaning and might for example be used to search for
 * marshalling related attributes with your IDE.
 */
public interface MarshallerOption<T> extends TypeSafeAttribute<T> {
	// Marker interface to mark attributes that are meant to be used as marshaller option
}
