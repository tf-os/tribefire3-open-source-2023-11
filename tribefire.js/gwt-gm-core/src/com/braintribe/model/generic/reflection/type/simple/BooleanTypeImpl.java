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
package com.braintribe.model.generic.reflection.type.simple;

import com.braintribe.model.generic.reflection.BooleanType;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.generic.reflection.TypeCode;
import com.braintribe.model.generic.tools.GmValueCodec;

@SuppressWarnings("unusable-by-js")
public class BooleanTypeImpl extends AbstractSimpleType implements BooleanType {
	public static final BooleanTypeImpl INSTANCE = new BooleanTypeImpl();

	private BooleanTypeImpl() {
		super(Boolean.class);
	}

	@Override
	public String getTypeName() {
		return "boolean";
	}

	@Override
	public TypeCode getTypeCode() {
		return TypeCode.booleanType;
	}

	@Override
	public Object getDefaultValue() {
		return Boolean.FALSE;
	}

	@Override
	public Class<?> getPrimitiveJavaType() {
		return boolean.class;
	}

	@Override
	public <T> T instanceFromString(String encodedValue) throws GenericModelException {
		return (T) Boolean.valueOf(encodedValue);
	}

	@Override
	public String instanceToGmString(Object value) throws GenericModelException {
		return GmValueCodec.booleanToGmString((Boolean) value);
	}

	@Override
	public Object instanceFromGmString(String encodedValue) {
		return GmValueCodec.booleanFromGmString(encodedValue);
	}

}
