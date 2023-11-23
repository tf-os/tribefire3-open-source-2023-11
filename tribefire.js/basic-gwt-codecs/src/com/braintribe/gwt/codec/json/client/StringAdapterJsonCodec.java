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
import com.braintribe.gwt.ioc.client.Required;
import com.google.gwt.json.client.JSONNull;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

/**
 * This Codec uses an inner delegate codec for encoding/decoding from JSONValue to String
 * and then to a final T object. And vice versa.
 * @author michel.docouto
 */
public class StringAdapterJsonCodec<T> implements Codec<T, JSONValue> {
	
	private Codec<T, String> delegateCodec;
	
	/**
	 * Configures the required codec for decoding/encoding String to T, and vice versa.
	 */
	@Required
	public void setDelegateCodec(Codec<T, String> delegateCodec) {
		this.delegateCodec = delegateCodec;
	}
	
	@Override
	public JSONValue encode(T value) throws CodecException {
		if (value == null) return JSONNull.getInstance();
		else return new JSONString(delegateCodec.encode(value));
	}

	@Override
	public T decode(JSONValue jsonValue) throws CodecException {
		if (jsonValue == null || jsonValue.isNull() != null) return null;
		else {
			JSONString stringValue = jsonValue.isString();
			if (stringValue == null) 
				throw new CodecException("illegal JSON type " + jsonValue);
			
			return delegateCodec.decode(stringValue.stringValue());
		}
	}

	@Override
	public Class<T> getValueClass() {
		return delegateCodec.getValueClass();
	}

}
