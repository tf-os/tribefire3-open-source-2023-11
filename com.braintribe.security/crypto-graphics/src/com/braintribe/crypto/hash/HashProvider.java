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

import java.security.MessageDigest;
import java.util.function.Function;



/**
 * <p>Provides a hash computation (hexadecimal representation) for a given byte array.
 * 
 * <p>By default, the digest algorithm used is SHA-256.
 * 
 * <p>The digest algorithm to be used may be changed through {@link #setDigestAlgorithm(String)}.
 * 
 *
 */
public class HashProvider implements Function<byte[], String> {
	
	final protected static char[] hexArray = "0123456789abcdef".toCharArray();
	
	private String digestAlgorithm = "SHA-256";
	private char separator = ':';

	/**
	 * <p>Sets the digest algorithm used to compute the hash of the inputs.
	 * 
	 * <p>By default, {@code SHA-256} will be used. 
	 * 
	 * @param digestAlgorithm The digest algorithm used to compute the hash of the inputs
	 */
	public void setDigestAlgorithm(String digestAlgorithm) {
		this.digestAlgorithm = digestAlgorithm;
	}

	/**
	 * <p>Gets the digest algorithm used to compute the hash of the inputs.
	 * 
	 * @return The digest algorithm used to compute the hash of the inputs.
	 */
	public String getDigestAlgorithm() {
		return this.digestAlgorithm;
	}

	/**
	 * <p>Defines the character used to separate the bytes in the resulting hexadecimal representation String.
	 * 
	 * <p>By default, {@code :} will be used. 
	 * 
	 * <p>Set to {@code \u0000} to use no separator.
	 * 
	 * @param separator The character used to separate the bytes in the resulting hexadecimal representation String
	 */
	public void setSeparator(char separator) {
		this.separator = separator;
	}
	
	@Override
	public String apply(byte[] value) throws RuntimeException {
		return generate(value);
	}
	
	protected String generate(byte[] source) throws RuntimeException {
		
		if (digestAlgorithm != null) {
			
			MessageDigest digest = null;
			try {
				digest = MessageDigest.getInstance(digestAlgorithm);
			} catch (Exception e) {
				throw new RuntimeException("Failed to obtain a MessageDigest"+(e.getMessage() != null ? ": "+e.getMessage() : ""), e);
			}
			
			digest.reset();
			digest.update(source);
			source = digest.digest();
		}
		
		if (separator != '\u0000') {
			return toHex(source, separator);
		} else {
			return toHex(source);
		}
	}
	
	protected static String toHex(byte[] s, char separator) {
		char[] f = new char[s.length * 3 - 1];
		for (int j = 0; j < s.length; j++) {
			int v = s[j] & 0xFF;
			f[j * 3] = hexArray[v >>> 4];
			f[j * 3 + 1] = hexArray[v & 0x0F];
			if (j != s.length-1) {
				f[j * 3 + 2] = separator;
			}
		}
		return new String(f);
	}
	
	protected static String toHex(byte[] s) {
		char[] f = new char[s.length * 2];
	    for (int j = 0; j < s.length; j++) {
	        int v = s[j] & 0xFF;
	        f[j * 2] = hexArray[v >>> 4];
	        f[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(f);
	}
	
}
