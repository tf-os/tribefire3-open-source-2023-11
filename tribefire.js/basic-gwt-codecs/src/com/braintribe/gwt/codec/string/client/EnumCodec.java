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
package com.braintribe.gwt.codec.string.client;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.Required;

public class EnumCodec<T extends Enum<T>> implements Codec<T, String> {
	private Class<T> enumClass;

	@Configurable @Required
	public void setEnumClass(Class<T> enumClass) {
		this.enumClass = enumClass;
	}
	
	@Override
	public String encode(T value) throws CodecException {
		if (value == null) return "";
		else return value.toString();
	}
	
	@Override
	public T decode(String encodedValue) throws CodecException {
		if (encodedValue == null || encodedValue.length() == 0)
			return null;
		
		try {
			T enumValue = Enum.valueOf(enumClass, encodedValue);
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
