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

import java.util.HashSet;
import java.util.Set;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNull;
import com.google.gwt.json.client.JSONValue;

public class SetJsonCodec<T> implements Codec<Set<T>, JSONValue> {
	private Codec<T, JSONValue> elementCodec;
	
	public SetJsonCodec(Codec<T, JSONValue> elementCodec) {
		this.elementCodec = elementCodec;
	}
	
	@Override
	public Set<T> decode(JSONValue encodedValue) throws CodecException {
		if (encodedValue == null || encodedValue.isNull() != null)
			return null;
		
		Set<T> set = new HashSet<T>();
		if (encodedValue instanceof JSONArray) {
			JSONArray array = (JSONArray)encodedValue;
			
			for (int i = 0; i < array.size(); i++) {
				JSONValue encodedElement = array.get(i);
				set.add(elementCodec.decode(encodedElement));
			}
		}
		
		return set;
	}
	
	@Override
	public JSONValue encode(Set<T> value) throws CodecException {
		if (value == null) return JSONNull.getInstance();
		
		JSONArray array = new JSONArray();
		
		int i = 0;
		for (T element: value) {
			array.set(i++, elementCodec.encode(element));
		}
		
		return array;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Class<Set<T>> getValueClass() {
		return (Class) Set.class;
	}

}
