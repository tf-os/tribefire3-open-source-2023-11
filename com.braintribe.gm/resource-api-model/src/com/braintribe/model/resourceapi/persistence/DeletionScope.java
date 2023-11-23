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
package com.braintribe.model.resourceapi.persistence;

import com.braintribe.model.generic.base.EnumBase;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.EnumTypes;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.source.ResourceSource;

/**
 * A resource consists of a {@link Resource} entity, which itself has a {@link ResourceSource}, which again points to
 * the actual binary data of the resource. When you want to delete a resource, this enum lets you specify what part of
 * it you actually want to delete.
 * 
 * @author Neidhart.Orlich
 *
 */
public enum DeletionScope implements EnumBase {
	/**
	 * Delete the binary content of the resource but keep the {@link ResourceSource} and the {@link Resource} entity
	 */
	binary,
	/**
	 * Delete the binary content and the {@link ResourceSource} of the resource but keep the {@link Resource} entity
	 */
	source,
	/**
	 * Delete all: the binary content, the {@link ResourceSource} and the {@link Resource} entity itself.
	 */
	resource;

	public static final EnumType T = EnumTypes.T(DeletionScope.class);

	@Override
	public EnumType type() {
		return T;
	}
	
}