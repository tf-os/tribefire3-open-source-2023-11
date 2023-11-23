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
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.braintribe.codec.Codec;


/**
 * <p>Provides {@link com.braintribe.codec.Codec}(s) capable of importing/exporting 
 *    {@link Key}(s) from/to byte arrays.
 * 
 * <p>The provided codec shall import and export keys in accordance with the key algorithm passed 
 *    as argument to the {@link Function#apply(Object)} method.
 * 
 *
 */
public abstract class KeyCodecProvider<T extends Key> implements Function<String, Codec<T, byte[]>> {

	private Map<String, Codec<T, byte[]>> keyExporters = new HashMap<String, Codec<T, byte[]>>();
	
	protected abstract Codec<T, byte[]> createExporter(String algorithm) throws RuntimeException;
	
	protected Codec<T, byte[]> provide(Class<T> type, String algorithm)  throws RuntimeException {
		String i = exporterId(type, algorithm);
		
		Codec<T, byte[]> exporter = keyExporters.get(i);
		
		if (exporter != null) {
			return exporter;
		}
		
		exporter = createExporter(algorithm);
		
		keyExporters.put(i, exporter);
		
		return exporter;
		
	}
	
	private String exporterId(Class<T> type, String algorithm) {
		return type.getSimpleName()+"."+algorithm;
	}
	
}
