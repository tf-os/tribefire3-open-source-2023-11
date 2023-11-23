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

import java.math.BigDecimal;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.google.gwt.json.client.JSONNull;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

/**
 * A codec for {@link BigDecimal}s.
 * 
 * @author michael.lafite
 */
public class BigDecimalJsonCodec implements Codec<BigDecimal, JSONValue> {

	@Override
	public BigDecimal decode(JSONValue jsonValue) throws CodecException {
		if (jsonValue == null || jsonValue instanceof JSONNull)
			return null;
		
		JSONString jsonString = jsonValue.isString();
		
		if (jsonString == null)
			throw new CodecException("found wrong json type when decoding decimal type: " + jsonValue.getClass());
		
		String valueAsString = jsonString.stringValue();
		String valueAsTrimmedString = valueAsString.trim();

		if (valueAsTrimmedString.length() > 0) {
			return new BigDecimal(valueAsTrimmedString);
		}
		return null;
	}

	@Override
	public JSONValue encode(BigDecimal obj) throws CodecException {
		if (obj != null) {
			return new JSONString(obj.toString());
		}
		else {
			return JSONNull.getInstance();
		}
	}

	@Override
	public Class<BigDecimal> getValueClass() {
		return BigDecimal.class;
	}
}
