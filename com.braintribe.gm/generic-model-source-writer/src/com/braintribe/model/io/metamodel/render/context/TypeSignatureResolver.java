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
package com.braintribe.model.io.metamodel.render.context;

import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.Map;

import com.braintribe.model.generic.reflection.SimpleType;
import com.braintribe.model.generic.reflection.SimpleTypes;
import com.braintribe.model.meta.GmBaseType;
import com.braintribe.model.meta.GmCollectionType;
import com.braintribe.model.meta.GmLinearCollectionType;
import com.braintribe.model.meta.GmListType;
import com.braintribe.model.meta.GmMapType;
import com.braintribe.model.meta.GmSimpleType;
import com.braintribe.model.meta.GmType;

/**
 * 
 */
public class TypeSignatureResolver {

	private static final Map<String, String> simpleNameToFullJavaName = newMap();
	static {
		for (SimpleType st : SimpleTypes.TYPES_SIMPLE) {
			simpleNameToFullJavaName.put(st.getTypeName(), st.getJavaType().getName());
		}
	}

	private final Map<GmType, JavaType> cache = newMap();

	public JavaType resolveJavaType(GmType gmType) {
		JavaType result = cache.get(gmType);
		if (result == null)
			cache.put(gmType, result = computeJavaTypes(gmType));

		return result;
	}

	private JavaType computeJavaTypes(GmType gmType) {
		if (gmType instanceof GmBaseType) {
			return new JavaType(Object.class.getName());

		} else if (gmType instanceof GmSimpleType) {
			return new JavaType(getSimpleTypeSignature((GmSimpleType) gmType));

		} else if (gmType instanceof GmCollectionType) {
			return getCollectionType((GmCollectionType) gmType);

		} else {
			return new JavaType(gmType.getTypeSignature());
		}
	}

	private String getSimpleTypeSignature(GmSimpleType gmSimpleType) {
		String typeSignature = gmSimpleType.getTypeSignature();
		if (simpleNameToFullJavaName.containsKey(typeSignature)) {
			return simpleNameToFullJavaName.get(typeSignature);

		} else {
			throw new IllegalArgumentException("Unsupported simple type: " + typeSignature);
		}
	}

	private JavaType getCollectionType(GmCollectionType gmType) {
		if (gmType instanceof GmLinearCollectionType) {
			String raw = gmType instanceof GmListType ? "List" : "Set";
			JavaType elementType = resolveJavaType(((GmLinearCollectionType) gmType).getElementType());

			return new JavaType("java.util." + raw, elementType.rawType);

		} else if (gmType instanceof GmMapType) {
			GmMapType mapType = (GmMapType) gmType;
			JavaType keyType = resolveJavaType(mapType.getKeyType());
			JavaType valueType = resolveJavaType(mapType.getKeyType());

			return new JavaType("java.util.Map", keyType.rawType, valueType.rawType);

		} else {
			throw new IllegalArgumentException("Unsupported collection type: " + gmType.getClass().getName());
		}
	}

}
