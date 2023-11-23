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
import java.util.LinkedHashMap;
import java.util.Map;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNull;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;

public class MapJsonCodec<K, V> implements Codec<Map<K, V>, JSONValue> {
	private Codec<K, JSONValue> keyCodec;
	private Codec<V, JSONValue> valueCodec;
	
	public MapJsonCodec(Codec<K, JSONValue> keyCodec, Codec<V, JSONValue> valueCodec) {
		this.keyCodec = keyCodec;
		this.valueCodec = valueCodec;
	}
	
	@Override
	public Map<K, V> decode(JSONValue encodedValue) throws CodecException {
		if (encodedValue == null || encodedValue.isNull() != null)
			return null;
		
		if (keyCodec.getValueClass() == String.class) {
			JSONObject jsonObject = (JSONObject)encodedValue;
			Map<String, V> map = new HashMap<String, V>();
			
			for (String key: jsonObject.keySet()) {
				JSONValue encodedElement = jsonObject.get(key);
				V value = valueCodec.decode(encodedElement);
				map.put(key, value);
			}
			
			Map<K, V> result = (Map<K, V>)map;
			
			return result;
		}
		else {
			JSONArray entries = (JSONArray)encodedValue;
			Map<K, V> map = new LinkedHashMap<K, V>();
			
			for (int i = 0; i < entries.size(); i++) {
				JSONObject jsonEntry = entries.get(i).isObject();
				JSONValue jsonKey = jsonEntry.get("key");
				JSONValue jsonValue = jsonEntry.get("value");
				K mapKey = keyCodec.decode(jsonKey);
				V mapValue = valueCodec.decode(jsonValue);
				map.put(mapKey, mapValue);
			}
			
			return map;
		}
	}
	
	@Override
	public JSONValue encode(Map<K, V> value) throws CodecException {
		if (value == null) return JSONNull.getInstance();
		
		if (keyCodec.getValueClass() == String.class) {
			JSONObject jsonObject = new JSONObject();
			
			for (Map.Entry<K, V> entry: value.entrySet()) {
				V mapValue = entry.getValue();
				JSONValue jsonValue = valueCodec.encode(mapValue);
				JSONValue jsonKey = keyCodec.encode(entry.getKey());
				jsonObject.put(jsonKey.isString().stringValue(), jsonValue);
			}
			
			return jsonObject;
		}
		else {
			JSONArray entries = new JSONArray();
			int i = 0;
			for (Map.Entry<K, V> entry: value.entrySet()) {
				JSONObject jsonEntry = new JSONObject();
				K mapKey = entry.getKey();
				V mapValue = entry.getValue();
				JSONValue jsonKey = keyCodec.encode(mapKey);
				JSONValue jsonValue = valueCodec.encode(mapValue);
				
				jsonEntry.put("key", jsonKey);
				jsonEntry.put("value", jsonValue);
				entries.set(i++, jsonEntry);
			}
			return entries;
		}
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Class<Map<K,V>> getValueClass() {
		return (Class) Map.class;
	}

}
