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

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;

public class HashCodec implements Codec<String,String> {

	protected String algorithm = "SHA-1";
	
	public HashCodec(String algorithm) {
		if (algorithm != null && !algorithm.isEmpty()) {
			this.algorithm = algorithm;
		}
	}
	public HashCodec() {
	}
	
	@Override
	public String encode(String value) throws CodecException {
		MessageDigest messageDigest = null;
		try {
			messageDigest = MessageDigest.getInstance(this.algorithm);
		} catch (NoSuchAlgorithmException e) {
			throw new CodecException("Could not get the "+this.algorithm+" message digest.", e);
		}
		messageDigest.reset();
		messageDigest.update(value.getBytes(Charset.forName("UTF8")));
		final byte[] resultByte = messageDigest.digest();
		StringBuffer sb = new StringBuffer();
		for (byte b : resultByte) {
			sb.append(String.format("%02x", b & 0xff));
		}
		return sb.toString();
	}

	@Override
	public String decode(String encodedValue) throws CodecException {
		throw new CodecException("Cannot decode an MD5 hashsum.");
	}

	@Override
	public Class<String> getValueClass() {
		return String.class;
	}

}
