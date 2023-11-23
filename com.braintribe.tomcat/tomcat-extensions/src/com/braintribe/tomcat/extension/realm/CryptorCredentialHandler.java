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
package com.braintribe.tomcat.extension.realm;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

import org.apache.catalina.CredentialHandler;

public class CryptorCredentialHandler implements CredentialHandler {

	private int saltLength = 16;

	public CryptorCredentialHandler(int saltLength) {
		this.saltLength = saltLength;
	}

	@Override
	public boolean matches(String inputCredentials, String storedCredentials) {

		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Could not access a SHA-56 hasher expert.", e);
		}

		byte[] storedCredentialBytes = Base64.getDecoder().decode(storedCredentials);
		byte[] salt = extractSalt(storedCredentialBytes);

		if (salt != null) {
			digest.update(salt);
		}

		digest.update(inputCredentials.getBytes(StandardCharsets.UTF_8));

		byte[] output = digest.digest();

		// salt is prepended to the digested result only if this hasher works with random salts
		if (salt != null && saltLength > 0) {
			output = concat(salt, output);
		}

		return Arrays.equals(storedCredentialBytes, output);
	}

	private byte[] extractSalt(byte[] encryptedValue) {

		if (saltLength < 1) {
			return null; // Shouldn't return null?
		}

		byte[] salt = Arrays.copyOf(encryptedValue, saltLength);

		return salt;
	}

	private static byte[] concat(byte[] first, byte[] second) {
		byte[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}
	@Override
	public String mutate(String inputCredentials) {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Could not access a SHA-56 hasher expert.", e);
		}
		byte[] hash = digest.digest(inputCredentials.getBytes(StandardCharsets.UTF_8));
		String encodedInput = Base64.getEncoder().encodeToString(hash);
		return encodedInput;
	}

}
