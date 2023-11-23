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
package com.braintribe.model.access.smood.distributed.codec;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.braintribe.utils.Base64;
import com.braintribe.utils.IOTools;

public class DefaultJavaClassCodec implements Codec<InputStream,String>{

	@Override
	public String encode(InputStream inputStream) throws CodecException {
		byte[] bytes = null;
		try {
			bytes = IOTools.slurpBytes(inputStream);
		} catch (Exception e) {
			throw new CodecException("Could not read from InputStream.", e);
		}
		String encodedString = null;
		try {
			encodedString = Base64.encodeBytes(bytes);
		} catch(Exception e) {
			throw new CodecException("Could not Base64 encode bytes.", e);
		}
		return encodedString;
	}

	@Override
	public InputStream decode(String encodedValue) throws CodecException {
		if (encodedValue == null) {
			throw new CodecException("The encoded value is null.");
		}
		byte[] bytes = null;
		try { 
			bytes = Base64.decode(encodedValue);
		} catch(Exception e) {
			throw new CodecException("Could not decode encoded value", e);
		}
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		return bais;
	}

	@Override
	public Class<InputStream> getValueClass() {
		return InputStream.class;
	}

}
