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

import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.generic.reflection.IntegerType;
import com.braintribe.model.generic.reflection.TypeCode;
import com.braintribe.model.generic.tools.GmValueCodec;

@SuppressWarnings("unusable-by-js")
public class IntegerTypeImpl extends AbstractSimpleType implements IntegerType {
	public static final IntegerTypeImpl INSTANCE = new IntegerTypeImpl();

	private IntegerTypeImpl() {
		super(Integer.class);
	}

	@Override
	public boolean isNumber() {
		return true;
	}

	@Override
	public String getTypeName() {
		return "integer";
	}

	@Override
	public TypeCode getTypeCode() {
		return TypeCode.integerType;
	}

	@Override
	public Object getDefaultValue() {
		return 0;
	}

	@Override
	public Class<?> getPrimitiveJavaType() {
		return int.class;
	}

	@Override
	public <T> T instanceFromString(String s) throws GenericModelException {
		return (T) Integer.valueOf(s);
	}

	@Override
	public String instanceToGmString(Object value) throws GenericModelException {
		return GmValueCodec.integerToGmString((Integer) value);
	}

	@Override
	public Object instanceFromGmString(String encodedValue) {
		return GmValueCodec.integerFromGmString(encodedValue);
	}

}
