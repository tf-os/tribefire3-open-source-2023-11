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
import com.google.gwt.json.client.JSONNull;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONValue;

public class LongJsonCodec implements Codec<Long, JSONValue> {
	@Override
	public JSONValue encode(Long value) throws CodecException {
		if (value == null) return JSONNull.getInstance();
		else return new JSONNumber(value);
	}
	
	@Override
	public Long decode(JSONValue jsonValue) throws CodecException {
		if (jsonValue == null || jsonValue.isNull() != null) return null;
		else {
			JSONNumber integerValue = jsonValue.isNumber();
			if (integerValue == null) 
				throw new CodecException("illegal JSON type " + jsonValue);
			
			return (long)integerValue.doubleValue();
		}
	}
	
	@Override
	public Class<Long> getValueClass() {
		return Long.class;
	}
}

