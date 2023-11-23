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
package com.braintribe.crypto;

import java.nio.charset.Charset;

/**
 * <p>
 * A component capable of performing cryptographic operations.
 * 
 */
public interface Cryptor {

	enum Encoding {
		base64, hex
	}

	/**
	 * <p>
	 * Enables encryption operations on this {@code Cryptor}, if supported.
	 * 
	 * @return A {@code Encryptor} instance.
	 * @throws UnsupportedOperationException
	 *             If the {@code Cryptor} does not support encrypting.
	 */
	Encryptor forEncrypting();

	/**
	 * <p>
	 * Enables decryption operations on this {@code Cryptor}, if supported.
	 * 
	 * @return A {@code Decryptor} instance.
	 * @throws UnsupportedOperationException
	 *             If the {@code Cryptor} does not support decrypting.
	 */
	Decryptor forDecrypting();

	/**
	 * <p>
	 * Obtains a {@link Encryptor.Matcher} for matching the given input against encrypted values.
	 * 
	 * @param input
	 *            The input to be compared with the encrypted value.
	 * @return A {@link Cryptor.Matcher} for matching the given input against encrypted values.
	 * @throws CryptorException
	 *             If the creation of a {@link Cryptor.Matcher} for matching the given input fails.
	 * @throws UnsupportedOperationException
	 *             If the {@code Cryptor} does not support matching.
	 */
	Cryptor.Matcher is(byte[] input) throws CryptorException, UnsupportedOperationException;

	/**
	 * <p>
	 * Obtains a {@link Encryptor.Matcher} for matching the given input against encrypted values.
	 * 
	 * 
	 * @param input
	 *            The input to be compared with the encrypted value.
	 * @return A {@link Cryptor.Matcher} for matching the given input against encrypted values.
	 * @throws CryptorException
	 *             If the creation of a {@link Cryptor.Matcher} for matching the given input fails.
	 * @throws UnsupportedOperationException
	 *             If the {@code Cryptor} does not support matching.
	 */
	Cryptor.StringMatcher is(String input) throws CryptorException, UnsupportedOperationException;

	interface Matcher {

		boolean equals(byte[] encryptedValue) throws CryptorException;

		boolean equals(String encryptedValue) throws CryptorException;

		boolean equals(String encryptedValue, Cryptor.Encoding encoding) throws CryptorException;

	}

	interface StringMatcher extends Matcher {

		StringMatcher charset(String charsetName) throws CryptorException;

		StringMatcher charset(Charset charset) throws CryptorException;

	}

	interface Processor<T extends Response> {

		T result() throws CryptorException;

	}

	interface Response {

		String asString() throws CryptorException;

		byte[] asBytes() throws CryptorException;

	}

}
