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

import java.util.HashMap;
import java.util.Map;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.google.gwt.json.client.JSONNull;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;

public class StringMapJsonCodec<T> implements Codec<Map<String, T>, JSONValue> {
	private Codec<T, JSONValue> elementCodec;
	
	public StringMapJsonCodec(Codec<T, JSONValue> elementCodec) {
		this.elementCodec = elementCodec;
	}
	
	@Override
	public Map<String, T> decode(JSONValue encodedValue) throws CodecException {
		JSONObject jsonObject = (JSONObject)encodedValue;
		Map<String, T> map = new HashMap<String, T>();
		
		for (String key: jsonObject.keySet()) {
			JSONValue encodedElement = jsonObject.get(key);
			T value = elementCodec.decode(encodedElement);
			map.put(key, value);
		}
		
		return map;
	}
	
	@Override
	public JSONValue encode(Map<String, T> value) throws CodecException {
		if (value == null) return JSONNull.getInstance();
		
		JSONObject jsonObject = new JSONObject();
		
		for (Map.Entry<String, T> entry: value.entrySet()) {
			T mapValue = entry.getValue();
			JSONValue jsonValue = elementCodec.encode(mapValue);
			jsonObject.put(entry.getKey(), jsonValue);
		}
		
		return jsonObject;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Class<Map<String,T>> getValueClass() {
		return (Class) Map.class;
	}

}
