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
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;

public class JsonStringCodec<T> implements Codec<T, String> {
	
	private Codec<T, JSONValue> jsonCodec;
	
	public JsonStringCodec(Codec<T, JSONValue> jsonCodec) {
		this.jsonCodec = jsonCodec;
	}
	
	protected JsonStringCodec() {
		
	}
	
	protected void setJsonCodec(Codec<T, JSONValue> jsonCodec) {
		this.jsonCodec = jsonCodec;
	}
	
	@Override
	public T decode(String encodedValue) throws CodecException {
		if (encodedValue == null || encodedValue.length() == 0)
			return null;
		
		JSONValue jsonValue = JSONParser.parseLenient(encodedValue);
		
		return jsonCodec.decode(jsonValue);
	}
	
	@Override
	public String encode(T value) throws CodecException {
		JSONValue jsonValue = jsonCodec.encode(value);
		return jsonValue.toString();
	}
	
	@Override
	public Class<T> getValueClass() {
		return jsonCodec.getValueClass();
	}
}
