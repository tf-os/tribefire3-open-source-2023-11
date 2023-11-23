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
package com.braintribe.crypto.key.provider;

import java.security.Key;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;


/**
 * <p>A {@link Supplier}/{@link Function} which generates fingerprints for {@link Key}(s), based on the given base64 string representation of their encoded value.
 * 
 * <p>{@link Supplier#get()} generates a fingerprint for the key represented by the base64 string configured through {@link #setKeyString(String)}.
 * 
 * <p>{@link Function#apply(Object)} generates a fingerprint for the key represented by the base64 string passed as the method argument.
 * 
 *
 */
public class KeyStringFingerprintProvider implements Supplier<String>, Function<String, String> {
	
	private Function<byte[], String> hashProvider;
	private Codec<byte[], String> base64Codec;
	private String defaultKeyString;
	
	public void setHashProvider(Function<byte[], String> hashProvider) {
		Objects.requireNonNull(hashProvider, "hashProvider cannot be set to null");
		this.hashProvider = hashProvider;
	}

	public void setBase64Codec(Codec<byte[], String> base64Codec) {
		Objects.requireNonNull(base64Codec, "base64Codec cannot be set to null");
		this.base64Codec = base64Codec;
	}

	public void setKeyString(String keyString) {
		this.defaultKeyString = keyString;
	}

	public String getKeyString() {
		return this.defaultKeyString;
	}

	@Override
	public String apply(String keyString) throws RuntimeException {

		if (keyString == null) {
			throw new RuntimeException("No key string was provider");
		}
		if (hashProvider == null) {
			throw new RuntimeException("No hash provider was configured");
		}
		
		byte[] keyBytes;
		try {
			keyBytes = base64Codec.decode(keyString);
		} catch (CodecException e) {
			throw new RuntimeException("Failed to decode base64 input: "+e.getMessage(), e);
		}
		
		return hashProvider.apply(keyBytes);
	}

	@Override
	public String get() throws RuntimeException {
		return apply(defaultKeyString);
	}
	
}
