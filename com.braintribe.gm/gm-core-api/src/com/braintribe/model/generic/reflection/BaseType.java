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
package com.braintribe.model.generic.reflection;

import com.braintribe.model.generic.GmCoreApiInteropNamespaces;
import com.braintribe.model.generic.GmPlatform;

import jsinterop.annotations.JsType;

/**
 * This represents a value of type Object (i.e. if your entity has a property of type Object, the corresponding
 * {@link Property#getType()} method would return an instance of {@linkplain BaseType}).
 * 
 * This means BaseType can only be a type of a property, or a generic parameter of a collection, but it is never
 * returned as a type of a GM value. In other words {@link GenericModelTypeReflection#getType(Object)} never returns an
 * instance of BaseType.
 */
@JsType(namespace = GmCoreApiInteropNamespaces.reflection)
public interface BaseType extends GenericModelType {
	public static final BaseType INSTANCE = GmPlatform.INSTANCE.getEssentialType(Object.class);

}
