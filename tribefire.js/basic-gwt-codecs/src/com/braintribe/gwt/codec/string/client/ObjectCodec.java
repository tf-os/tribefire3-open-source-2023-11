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

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;

/**
 * This simple string codec returns a values {@link Object#toString()}
 * as encoded string value.
 * 
 * @author Dirk
 *
 */
public class ObjectCodec implements Codec<Object, String> {
	@Override
	public Object decode(String encodedValue) throws CodecException {
		return encodedValue;
	}
	
	@Override
	public String encode(Object value) throws CodecException {
		if (value != null) return value.toString();
		else return "";
	}
	
	@Override
	public Class<Object> getValueClass() {
		return Object.class;
	}
}
