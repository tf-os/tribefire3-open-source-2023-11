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
package com.braintribe.model.processing.cryptor.basic.cipher.key.codec;

import java.security.Key;
import java.util.HashMap;
import java.util.Map;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.braintribe.model.processing.crypto.token.KeyCodecProvider;
import com.braintribe.model.processing.crypto.token.loader.EncryptionTokenLoaderException;

/**
 * <p>
 * Provides common functionalities to {@link KeyCodecProvider} implementations.
 * 
 */
public abstract class AbstractKeyCodecProvider<T extends Key> implements KeyCodecProvider<T> {

	private Map<String, Codec<T, byte[]>> cachedKeyCodecs = new HashMap<>();
	
	protected abstract Codec<T, byte[]> createKeyCodec(String algorithm) throws EncryptionTokenLoaderException;
	
	protected Codec<T, byte[]> getKeyCodec(Class<T> type, String algorithm) throws EncryptionTokenLoaderException {
		
		String i = cachedKeyCodecId(type, algorithm);
		
		Codec<T, byte[]> codec = cachedKeyCodecs.get(i);
		
		if (codec != null) {
			return codec;
		}
		
		codec = createKeyCodec(algorithm);
		
		cachedKeyCodecs.put(i, codec);
		
		return codec;
		
	}
	
	private String cachedKeyCodecId(Class<T> type, String algorithm) {
		return type.getSimpleName()+"."+algorithm;
	}
	
	public static CodecException asCodecException(String message, Exception e) {
		if (e.getMessage() != null) {
			message = ": "+e.getMessage();
		}
		return new CodecException(message, e);
	}
	
}
