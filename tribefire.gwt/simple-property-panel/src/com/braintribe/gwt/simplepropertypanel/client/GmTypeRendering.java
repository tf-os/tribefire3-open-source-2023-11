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
package com.braintribe.gwt.simplepropertypanel.client;

import com.braintribe.model.meta.GmBaseType;
import com.braintribe.model.meta.GmCustomType;
import com.braintribe.model.meta.GmLinearCollectionType;
import com.braintribe.model.meta.GmMapType;
import com.braintribe.model.meta.GmSimpleType;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.GmTypeKind;

public class GmTypeRendering {
	
	public static String getPropertyType(GmType propertyType, boolean key){
		if (propertyType == null)
			return "???";
		
		if (propertyType instanceof GmMapType) {
			if (key)
				return getPropertyType(((GmMapType) propertyType).getKeyType(), false);
			
			return getPropertyType(((GmMapType) propertyType).getValueType(), false);
		}
		
		if (propertyType instanceof GmLinearCollectionType)
			return getPropertyType(((GmLinearCollectionType) propertyType).getElementType(), false);
		
		if (propertyType instanceof GmSimpleType)
			return propertyType.typeKind().name().toLowerCase();
		
		if (propertyType instanceof GmBaseType)
			return "object";
		
		if (propertyType instanceof GmCustomType) {
			GmCustomType type = (GmCustomType)propertyType;
			String typeSignature = type.getTypeSignature();
			return typeSignature.substring(typeSignature.lastIndexOf(".") + 1, typeSignature.length());
		}
		
		return "???";
	}
	
	public static String getTypeName(String typeSignature) {
		return typeSignature.substring(typeSignature.lastIndexOf(".")+1, typeSignature.length());
	}
	
	public static String getTypePackage(String typeSignature) {
		return typeSignature.substring(0, typeSignature.lastIndexOf("."));
	}
	
	public static String getTypeGlobalId(GmTypeKind collectionTypeKind, GmTypeKind keyTypeKind, GmTypeKind valueTypeKind) {
		String key = keyTypeKind.name();
		if (key.equalsIgnoreCase("base"))
			key = "object";
		
		String value = "";
		if (valueTypeKind != null) {
			value = keyTypeKind.name();
			if (value.equalsIgnoreCase("base"))
				value = "object";
		}
		
		if (collectionTypeKind == GmTypeKind.MAP)
			return "type:" + collectionTypeKind.name().toLowerCase() + "<" + key.toLowerCase() + "," + value.toLowerCase()	 + ">";
		
		if (collectionTypeKind == GmTypeKind.LIST || collectionTypeKind == GmTypeKind.SET)
			return "type:" + collectionTypeKind.name().toLowerCase() + "<" + key.toLowerCase() + ">";
		
		return "type:" + key.toLowerCase();
	}

	public static String getCardinality(GmType type) {
		/*if(type instanceof GmSimpleType || type instanceof GmBaseType)
			return "SINGLE";
		else */
		if (type instanceof GmLinearCollectionType || type instanceof GmMapType)
			return type.typeKind().name();
			
		return "SINGLE";
//		return null;
	}

}
