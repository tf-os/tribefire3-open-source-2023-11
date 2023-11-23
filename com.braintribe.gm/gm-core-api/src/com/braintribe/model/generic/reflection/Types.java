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

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GmCoreApiInteropNamespaces;

import jsinterop.annotations.JsType;

/**
 * All the possible essential {@link CollectionType} instances in form of static fields. In this case this means that given types are parameterized
 * with Objects (i.e. {@link BaseType}).
 */
@JsType(namespace = GmCoreApiInteropNamespaces.reflection)
public interface Types extends EssentialTypes {

	static ListType listOf(GenericModelType elementType) {
		return GMF.getTypeReflection().getListType(elementType);
	}

	static SetType setOf(GenericModelType elementType) {
		return GMF.getTypeReflection().getSetType(elementType);
	}

	static MapType mapOf(GenericModelType keyType, GenericModelType valueType) {
		return GMF.getTypeReflection().getMapType(keyType, valueType);
	}

}
