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

import java.math.BigDecimal;

import com.braintribe.model.generic.reflection.DecimalType;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.generic.reflection.TypeCode;
import com.braintribe.model.generic.tools.GmValueCodec;

public class DecimalTypeImpl extends AbstractSimpleType implements DecimalType {
	public static final DecimalTypeImpl INSTANCE = new DecimalTypeImpl();

	private DecimalTypeImpl() {
		super(BigDecimal.class);
	}

	@Override
	public boolean isNumber() {
		return true;
	}

	@Override
	public String getTypeName() {
		return "decimal";
	}

	@Override
	public TypeCode getTypeCode() {
		return TypeCode.decimalType;
	}

	@Override
	public Object getDefaultValue() {
		return null;
	}

	@Override
	public Class<?> getPrimitiveJavaType() {
		return null;
	}

	@Override
	public <T> T instanceFromString(String s) throws GenericModelException {
		if (s.length() == 1) {
			return (T) BigDecimal.valueOf(s.charAt(0) - '0');
		}

		if ("10".equals(s)) {
			return (T) BigDecimal.TEN;
		}

		return (T) new BigDecimal(s);
	}

	@Override
	public String instanceToGmString(Object value) throws GenericModelException {
		return GmValueCodec.decimalToGmString((BigDecimal) value);
	}

	@Override
	public Object instanceFromGmString(String encodedValue) {
		return GmValueCodec.decimalFromGmString(encodedValue);
	}

}
