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

import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * <p>
 * {@link com.braintribe.crypto.Cryptor} for encrypting data.
 * 
 */
public interface Encryptor extends Cryptor {

	/**
	 * <p>
	 * Returns a {@link Encryptor.Processor} for encrypting the given input.
	 * 
	 * <p>
	 * <b>Examples</b>
	 * 
	 * <p>
	 * <i>From bytes to bytes:</i>
	 * 
	 * <pre>
	 * {@code
	 * Encryptor encryptor = ...
	 * byte[] data = ...
	 * byte[] encryptedData = encryptor.encrypt(data).result().asBytes();
	 * }
	 * </pre>
	 * 
	 * <p>
	 * <i>From bytes to String:</i>
	 * 
	 * <pre>
	 * {@code
	 * Encryptor encryptor = ...
	 * byte[] data = ...
	 * String encryptedData = encryptor.encrypt(data).result().asString();
	 * }
	 * </pre>
	 * 
	 * @param input
	 *            The input to be processed by the returned {@link Encryptor.Processor}.
	 * @return The {@link Encryptor.Processor} for encrypting the given input.
	 * @throws CryptorException
	 *             If the creation of a {@link Encryptor.Processor} for encrypting the given input fails.
	 */
	Encryptor.Processor encrypt(byte[] input) throws CryptorException;

	/**
	 * <p>
	 * Returns a {@link Encryptor.StringProcessor} for encrypting the given String input.
	 * 
	 * <p>
	 * <b>Examples</b>
	 * 
	 * <p>
	 * <i>From String to bytes:</i>
	 * 
	 * <pre>
	 * {@code
	 * Encryptor encryptor = ...
	 * String data = ...
	 * byte[] encryptedData = encryptor.encrypt(data).result().asBytes();
	 * }
	 * </pre>
	 * 
	 * <p>
	 * <i>From String to String:</i>
	 * 
	 * <pre>
	 * {@code
	 * Encryptor encryptor = ...
	 * String data = ...
	 * String encryptedData = encryptor.encrypt(data).result().asString();
	 * }
	 * </pre>
	 * 
	 * @param input
	 *            The input to be processed by the returned {@link Encryptor.StringProcessor}.
	 * @return The {@link Encryptor.StringProcessor} for encrypting the given input.
	 * @throws CryptorException
	 *             If the creation of a {@link Encryptor.StringProcessor} for encrypting the given input fails.
	 */
	Encryptor.StringProcessor encrypt(String input) throws CryptorException;

	/**
	 * <p>
	 * Wraps a {@link OutputStream} for encrypting data.
	 * 
	 * @param outputStream
	 *            The {@code OutputStream} to be wrapped for encrypting data
	 * @return A {@code OutputStream} wrapped for encrypting data from the given {@code OutputStream}
	 * @throws CryptorException
	 *             If the wrapping of the given {@code OutputStream} for decrypting fails.
	 */
	OutputStream wrap(OutputStream outputStream) throws CryptorException;

	/**
	 * <p>
	 * Determines whether this {@code Encryptor} produces deterministic results.
	 * 
	 * @return Whether this {@code Encryptor} produces deterministic results.
	 */
	boolean isDeterministic();

	/**
	 * <p>
	 * A {@link com.braintribe.crypto.Cryptor.Processor} 
	 * for {@link com.braintribe.crypto.Encryptor}(s).
	 * 
	 */
	interface Processor extends Cryptor.Processor<Encryptor.Response> {

		@Override
		Encryptor.Response result() throws CryptorException;

		Encryptor.Processor withSaltFrom(byte[] encryptedValue) throws CryptorException;

	}

	/**
	 * <p>
	 * A String-handling {@link com.braintribe.crypto.Cryptor.Processor} 
	 * for {@link com.braintribe.crypto.Encryptor}(s).
	 * 
	 */
	interface StringProcessor extends Processor {

		Encryptor.StringProcessor charset(String charsetName) throws CryptorException;

		Encryptor.StringProcessor charset(Charset charset) throws CryptorException;

	}

	/**
	 * <p>
	 * A {@link com.braintribe.crypto.Cryptor.Response} with specialized methods 
	 * for obtaining data encrypted by a {@link com.braintribe.crypto.Encryptor}.
	 * 
	 */
	interface Response extends Cryptor.Response {

		String asString(Cryptor.Encoding encoding) throws CryptorException;

	}

}
