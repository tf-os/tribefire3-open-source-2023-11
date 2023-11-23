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
package com.braintribe.codec.string;

import java.lang.reflect.Array;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;

public class ArrayCodec<T> implements Codec<T[], String> {
	private Codec<T, String> elementCodec;
	private Codec<String, String> escapeCodec = new PassThroughCodec<String>(String.class);
	private String delimiter = ",";
	private Class<T[]> arrayType;

	public ArrayCodec(Class<T[]> arrayType, Codec<T, String> elementCodec) {
		super();
		this.elementCodec = elementCodec;
		this.arrayType = arrayType;
	}
	
	public void setEscapeCodec(Codec<String, String> escapeCodec) {
		this.escapeCodec = escapeCodec;
	}
	
	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	@Override
	@SuppressWarnings("unchecked")
	public T[] decode(String s) throws CodecException {
		if (s == null || s.trim().length() == 0)
			return (T[])Array.newInstance(arrayType.getComponentType(), 0);
		
		String[] escapedValues = s.split(delimiter);
		T values[] = (T[])Array.newInstance(arrayType.getComponentType(), escapedValues.length);
		
		for (int i = 0; i < escapedValues.length; i++) {
			String encodedValue = escapeCodec.decode(escapedValues[i]);
			values[i] = elementCodec.decode(encodedValue);
		}
		
		return values;
	}

	@Override
	public String encode(T[] obj) throws CodecException {
		StringBuilder encodedList = new StringBuilder();

		for (T value : obj) {
			if (encodedList.length() > 0) 
				encodedList.append(delimiter);
			String encodedValue = elementCodec.encode(value);
			encodedList.append(escapeCodec.encode(encodedValue));
		}

		return encodedList.toString();
	}

	@Override
	public Class<T[]> getValueClass() {
		return arrayType;
	}
}
