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

import com.braintribe.crypto.hash.HashProvider;


/**
 * <p>A {@link Supplier}/{@link Function} which generates fingerprints for {@link Key}(s).
 * 
 * <p>{@link #get()} generates a fingerprint for the key previously configured through {@link #setKey(Key)}.
 * 
 * <p>{@link #apply(Key)} generates a fingerprint for the key passed as the method argument.
 * 
 *
 */
public class KeyFingerprintProvider implements Supplier<String>, Function<Key, String> {
	
	private Function<byte[], String> hashProvider = new HashProvider();
	private byte[] keyValue;
	
	public void setHashProvider(Function<byte[], String> hashProvider) {
		Objects.requireNonNull(hashProvider, "hashProvider cannot be set to null");
		this.hashProvider = hashProvider;
	}
	
	public void setKey(Key key) {
		Objects.requireNonNull(key, "key cannot be set to null");
		this.keyValue = key.getEncoded();
	}
	
	@Override
	public String get() throws RuntimeException {
		
		if (keyValue == null) {
			throw new RuntimeException("No key is configured.");
		}
		
		return hashProvider.apply(keyValue);
	}

	@Override
	public String apply(Key key) throws RuntimeException {

		if (key == null) {
			throw new RuntimeException("Key cannot be null.");
		}
		
		return hashProvider.apply(key.getEncoded());
	}
	
}
