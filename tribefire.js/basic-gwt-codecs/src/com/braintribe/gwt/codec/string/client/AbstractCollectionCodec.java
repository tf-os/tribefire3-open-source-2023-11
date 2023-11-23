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

import java.util.Collection;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.braintribe.gwt.ioc.client.Configurable;

public abstract class AbstractCollectionCodec<C extends Collection<T>, T> implements Codec<C, String> {
	private Codec<T, String> elementCodec;
	private Codec<String, String> escapeCodec = new PassThroughCodec<String>(String.class);
	private String delimiter = ",";
	private Class<C> collectionClass;

	public AbstractCollectionCodec(Class<C> collectionClass, Codec<T, String> elementCodec) {
		super();
		this.elementCodec = elementCodec;
		this.collectionClass = collectionClass;
	}
	
	public AbstractCollectionCodec(Class<C> collectionClass) {
		this.collectionClass = collectionClass;
	}
	
	@Configurable
	public void setElementCodec(Codec<T, String> elementCodec) {
		this.elementCodec = elementCodec;
	}
	
	@Configurable
	public void setEscapeCodec(Codec<String, String> escapeCodec) {
		this.escapeCodec = escapeCodec;
	}
	
	@Configurable
	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}
	
	protected abstract C createCollection();

	@Override
	public C decode(String s) throws CodecException {
		if (s == null || s.trim().length() == 0)
			return createCollection();

		String[] escapedValues = s.split(delimiter);
		C values = createCollection();

		for (int i = 0; i < escapedValues.length; i++) {
			String encodedValue = escapeCodec.decode(escapedValues[i]);
			values.add(elementCodec.decode(encodedValue));
		}
		return values;
	}

	@Override
	public String encode(C obj) throws CodecException {
		if (obj == null) return "";
		
		StringBuilder encodedList = new StringBuilder();

		for (T value : obj) {
			if (encodedList.length() > 0) 
				encodedList.append(delimiter);
			String encodedValue = elementCodec.encode(value);
			encodedList.append(escapeCodec.encode(encodedValue));
		}

		return encodedList.toString();
	}

	@SuppressWarnings("cast")
	@Override
	public Class<C> getValueClass() {
		return (Class<C>) collectionClass;
	}
}
