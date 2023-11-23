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

import java.util.Date;

import com.braintribe.model.generic.reflection.CloningContext;
import com.braintribe.model.generic.reflection.DateType;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.generic.reflection.StrategyOnCriterionMatch;
import com.braintribe.model.generic.reflection.TypeCode;
import com.braintribe.model.generic.tools.GmValueCodec;

@SuppressWarnings("unusable-by-js")
public class DateTypeImpl extends AbstractSimpleType implements DateType {
	public static final DateTypeImpl INSTANCE = new DateTypeImpl();

	private DateTypeImpl() {
		super(java.util.Date.class);
	}

	@Override
	public String getTypeName() {
		return "date";
	}

	@Override
	public TypeCode getTypeCode() {
		return TypeCode.dateType;
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
	public <T> T instanceFromString(String encodedValue) throws GenericModelException {
		try {
			return (T) new Date(Long.parseLong(encodedValue));
		} catch (Exception e) {
			throw new GenericModelException("error while creating instance from string " + encodedValue, e);
		}
	}

	@Override
	public String instanceToString(Object value) throws GenericModelException {
		return Long.toString(((Date) value).getTime());
	}

	@Override
	public String instanceToGmString(Object value) throws GenericModelException {
		return GmValueCodec.dateToGmString((Date) value);
	}

	@Override
	public Object instanceFromGmString(String encodedValue) {
		return GmValueCodec.dateFromGmString(encodedValue);
	}

	@Override
	public Object cloneImpl(CloningContext cloningContext, Object value, StrategyOnCriterionMatch strategy) {
		return value == null ? null : new Date(((Date) value).getTime());
	}

	@Override
	public boolean isInstance(Object value) {
		return value instanceof java.util.Date;
	}

}
