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
package com.braintribe.model.processing.web.rest;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;

/**
 * This interface is used by the HttpRequestEntityDecoder when properties of type "object" when decoding
 * HttpServletRequest.
 * 
 *
 */
@FunctionalInterface
public interface PropertyTypeResolver {

	/**
	 * Resolves the actual type of the given property.
	 * 
	 * @param entityType
	 *            the entityType, never {@code null}
	 * @param property
	 *            the the property to resolve the type for, never {@code null}
	 * 
	 * @return the type, must not be {@code null}
	 */
	GenericModelType resolvePropertyType(EntityType<?> entityType, Property property);
}
