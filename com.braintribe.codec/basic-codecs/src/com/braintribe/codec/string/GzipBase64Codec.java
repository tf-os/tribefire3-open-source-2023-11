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
import com.braintribe.utils.Base64;
import com.braintribe.utils.lcd.StringTools;

public class GzipBase64Codec implements Codec<String,String> {

	protected GzipCodec gzipCodec = new GzipCodec();

	@Override
	public String encode(String value) throws CodecException {
		if (value == null) {
			return null;
		}

		byte[] valueBytes = null;
		try {
			valueBytes = value.getBytes("UTF-8");
		} catch(Exception e) {
			throw new CodecException("Could not get the bytes of String "+StringTools.getFirstNCharacters(value, 100), e);
		}

		byte[] zipBytes = this.gzipCodec.encode(valueBytes);

		String base64Encoded = Base64.encodeBytes(zipBytes);

		return base64Encoded;
	}

	@Override
	public String decode(String encodedValue) throws CodecException {
		if (encodedValue == null) {
			return null;
		}

		try {
			byte[] encodedBytes = encodedValue.getBytes("UTF-8");
			byte[] decodedBytes = Base64.decode(encodedBytes, 0, encodedBytes.length);

			byte[] unzippedBytes = this.gzipCodec.decode(decodedBytes);

			String decodedString = new String(unzippedBytes, "UTF-8");

			return decodedString;
		} catch(Exception e) {
			throw new CodecException("Could not decode "+StringTools.getFirstNCharacters(encodedValue, 100), e);
		}
	}

	@Override
	public Class<String> getValueClass() {
		return String.class;
	}

}
