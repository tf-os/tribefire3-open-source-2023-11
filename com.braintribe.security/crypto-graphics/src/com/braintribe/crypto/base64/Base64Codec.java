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
package com.braintribe.crypto.base64;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;

public class Base64Codec implements Codec<byte[], String> {
	
	private int options = Base64.DONT_BREAK_LINES;

	@Override
	public String encode(byte[] value) throws CodecException {
		try {
			return Base64.encodeBytes(value, options);
		} catch (Exception e) {
			throw new CodecException("Failed to base64 encode: "+e.getMessage(), e);
		}
	}

	@Override
	public byte[] decode(String encodedValue) throws CodecException {
		try {
			return Base64.decode(encodedValue, options);
		} catch (Exception e) {
			throw new CodecException("Failed to base64 decode: "+e.getMessage(), e);
		}
	}

	@Override
	public Class<byte[]> getValueClass() {
		return byte[].class;
	}

}
