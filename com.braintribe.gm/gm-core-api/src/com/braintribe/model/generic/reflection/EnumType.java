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
import com.braintribe.model.generic.value.EnumReference;

import jsinterop.annotations.JsType;

@JsType(namespace = GmCoreApiInteropNamespaces.reflection)
@SuppressWarnings("unusable-by-js")
public interface EnumType extends CustomType, ScalarType {

	@Override
	Class<? extends Enum<?>> getJavaType();

	Enum<? extends Enum<?>>[] getEnumValues();

	Enum<? extends Enum<?>> getEnumValue(String name);

	Enum<? extends Enum<?>> findEnumValue(String name);

	<E extends Enum<E>> E getInstance(String value);

	EnumReference getEnumReference(Enum<?> enumConstant);

}
