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
package com.braintribe.codec.marshaller.html.model;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.generic.reflection.TypeCode;

/**
 * @author Christina Wilpernig
 * @author Neidhart.Orlich
 */

public interface HtmlValue extends GenericEntity {

	final EntityType<HtmlValue> T = EntityTypes.T(HtmlValue.class);

	public Object getValue();
	public void setValue(Object value);

	public String getTypeSignature();
	public void setTypeSignature(String typeSignature);
	
	public String getExpectedTypeSignature();
	public void setExpectedTypeSignature(String typeSignature);
	
	// TODO not needed yet
	default TypeCode getObjectTypeCode() {
		return GMF.getTypeReflection().getType(getValue()).getTypeCode();
	}
	
	default RenderType renderType() {
		return RenderType.simple;
	}
	
	default boolean linkable() {
		return false;
	}
	
	default String getRenderedString() {
		Object value = getValue();
		TypeCode objectTypeCode = getObjectTypeCode();
		
		switch(objectTypeCode) {
			case stringType:
				return value != null ? value.toString() : "";
			case objectType:
				return "";
			case booleanType:
			case dateType:
			case floatType:
			case integerType:
			case longType:
			case decimalType:
			case doubleType:
			case enumType:
				return value.toString();
			default:
				throw new IllegalStateException("No rendered string available for type " + objectTypeCode);
		}
	}
//	
//	default String simpleTypeName() {
//		int lastDotIndex = getTypeSignature().lastIndexOf('.');
//		return getTypeSignature().substring(lastDotIndex + 1);
//	}
	
	default String typePackageName() {
		int lastDotIndex = getTypeSignature().lastIndexOf('.');
		return getTypeSignature().substring(0, lastDotIndex);
	}

}
