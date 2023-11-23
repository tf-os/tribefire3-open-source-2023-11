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

import com.braintribe.model.generic.reflection.FloatType;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.generic.reflection.TypeCode;
import com.braintribe.model.generic.tools.GmValueCodec;

public class FloatTypeImpl extends AbstractSimpleType implements FloatType {
	public static final FloatTypeImpl INSTANCE = new FloatTypeImpl();

	private FloatTypeImpl() {
		super(Float.class);
	}

	@Override
	public boolean isNumber() {
		return true;
	}

	@Override
	public String getTypeName() {
		return "float";
	}

	@Override
	public TypeCode getTypeCode() {
		return TypeCode.floatType;
	}

	@Override
	public Object getDefaultValue() {
		return 0F;
	}

	@Override
	public Class<?> getPrimitiveJavaType() {
		return float.class;
	}

	@Override
	public <T> T instanceFromString(String s) throws GenericModelException {
		return (T) Float.valueOf(s);
	}

	@Override
	public String instanceToGmString(Object value) throws GenericModelException {
		return GmValueCodec.floatToGmString((Float) value);
	}

	@Override
	public Object instanceFromGmString(String encodedValue) {
		return GmValueCodec.floatFromGmString(encodedValue);
	}

}
