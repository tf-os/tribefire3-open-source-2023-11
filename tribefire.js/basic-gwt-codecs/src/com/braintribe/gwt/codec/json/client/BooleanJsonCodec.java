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
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNull;
import com.google.gwt.json.client.JSONValue;

public class BooleanJsonCodec implements Codec<Boolean, JSONValue> {
	@Override
	public JSONValue encode(Boolean value) throws CodecException {
		if (value == null) return JSONNull.getInstance();
		else return JSONBoolean.getInstance(value);
	}
	
	@Override
	public Boolean decode(JSONValue jsonValue) throws CodecException {
		if (jsonValue == null || jsonValue.isNull() != null) return null;
		else {
			JSONBoolean booleanValue = jsonValue.isBoolean();
			if (booleanValue == null) 
				throw new CodecException("illegal type");
			
			return booleanValue.booleanValue();
		}
	}
	
	@Override
	public Class<Boolean> getValueClass() {
		return Boolean.class;
	}
}
