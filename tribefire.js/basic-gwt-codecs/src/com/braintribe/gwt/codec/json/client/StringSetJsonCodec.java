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
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

public class StringSetJsonCodec implements Codec<Set<String>, JSONArray> {
	@Override
	public Set<String> decode(JSONArray encodedValue) throws CodecException {
		Set<String> strings = new HashSet<String>();
		for (int i = 0; i < encodedValue.size(); i++) {
			JSONValue value = encodedValue.get(i);
			JSONString string = value.isString();
			if (string != null) {
				strings.add(string.stringValue());
			}
		}
		return strings;
	}
	
	@Override
	public JSONArray encode(Set<String> value) throws CodecException {
		JSONArray array = new JSONArray();
		int index = 0;
		for (String string: value) {
			array.set(index++, new JSONString(string));
		}
		return array;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Class<Set<String>> getValueClass() {
		return (Class) Set.class;
	}

}
