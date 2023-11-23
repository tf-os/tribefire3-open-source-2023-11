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
package com.braintribe.crypto.hash;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.braintribe.codec.Codec;
import com.braintribe.crypto.Cryptor.Encoding;
import com.braintribe.crypto.CryptorException;
import com.braintribe.crypto.commons.HexCodec;

/**
 * <p>
 * Provides a hash computation (hexadecimal representation) for a given byte array.
 * 
 * <p>
 * By default, the digest algorithm used is SHA-256.
 * 
 * <p>
 * The digest algorithm to be used may be configured through {@link #setDigestAlgorithm(String)}.
 * 
 *
 */
public class HashFunction implements Function<byte[], String> {

	private String digestAlgorithm = "SHA-256";
	private char separator = ':';
	private Hasher hasher;

	public void initialize() throws CryptorException {

		Map<Encoding, Codec<byte[], String>> codecs = new HashMap<>(1);
		codecs.put(Encoding.hex, new HexCodec(separator));

		hasher = new Hasher(digestAlgorithm, null, codecs, null);

	}

	/**
	 * <p>
	 * Sets the digest algorithm used to compute the hash of the inputs.
	 * 
	 * <p>
	 * By default, {@code SHA-256} will be used.
	 * 
	 * @param digestAlgorithm
	 *            The digest algorithm used to compute the hash of the inputs
	 */
	public void setDigestAlgorithm(String digestAlgorithm) {
		this.digestAlgorithm = digestAlgorithm;
	}

	/**
	 * <p>
	 * Gets the digest algorithm used to compute the hash of the inputs.
	 * 
	 * @return The digest algorithm used to compute the hash of the inputs.
	 */
	public String getDigestAlgorithm() {
		return this.digestAlgorithm;
	}

	/**
	 * <p>
	 * Defines the character used to separate the bytes in the resulting hexadecimal representation String.
	 * 
	 * <p>
	 * By default, {@code :} will be used.
	 * 
	 * <p>
	 * Set to {@code \u0000} to use no separator.
	 * 
	 * @param separator
	 *            The character used to separate the bytes in the resulting hexadecimal representation String
	 */
	public void setSeparator(char separator) {
		this.separator = separator;
	}

	@Override
	public String apply(byte[] t) {
		try {
			if (hasher == null)
				initialize();
			return hasher.encrypt(t).result().asString(Encoding.hex);
		} catch (CryptorException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

}
