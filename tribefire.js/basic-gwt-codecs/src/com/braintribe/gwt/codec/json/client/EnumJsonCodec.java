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
package com.braintribe.gwt.codec.json.client;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.Required;
import com.google.gwt.json.client.JSONNull;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

public class EnumJsonCodec<T extends Enum<T>> implements Codec<T, JSONValue> {
	private Class<T> enumClass;

	@Configurable @Required
	public void setEnumClass(Class<T> enumClass) {
		this.enumClass = enumClass;
	}
	
	@Override
	public JSONValue encode(T value) throws CodecException {
		if (value == null) return JSONNull.getInstance();
		else return new JSONString(value.toString());
	}
	
	@Override
	public T decode(JSONValue jsonValue) throws CodecException {
		if (jsonValue == null || jsonValue.isNull() != null)
			return null;
		
		JSONString jsonString = jsonValue.isString();
		
		if (jsonString == null)
			throw new CodecException("invalid type. must be string");

		try {
			T enumValue = Enum.valueOf(enumClass, jsonString.stringValue());
			return enumValue;
		} catch (Exception e) {
			throw new CodecException("error while creating enum from string", e);
		}
	}
	
	@Override
	public Class<T> getValueClass() {
		return enumClass;
	}
}
