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
package com.braintribe.codec.string;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;

public class CommaUrlEscapeCodec implements Codec<String, String> {
	
	@Override
	public String encode(String value) throws CodecException {
		if (value == null) {
			return null;
		}
		return value.replace(",", "%2C");
	}
	
	@Override
	public String decode(String encodedValue) throws CodecException {
		if (encodedValue == null) {
			return null;
		}
		return encodedValue.replace("%2C", ",");
	}
	
	@Override
	public Class<String> getValueClass() {
		return String.class;
	}
}