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

import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * <p>
 * {@link com.braintribe.crypto.Cryptor} for decrypting data.
 * 
 */
public interface Decryptor extends Cryptor {

	/**
	 * <p>
	 * Returns a {@link Decryptor.Processor} for encrypting the given input.
	 * 
	 * <p>
	 * <b>Examples</b>
	 * 
	 * <p>
	 * <i>From bytes to bytes:</i>
	 * 
	 * <pre>
	 * {@code
	 * Decryptor decryptor = ...
	 * byte[] data = ...
	 * byte[] decryptedData = decryptor.decrypt(data).result().asBytes();
	 * }
	 * </pre>
	 * 
	 * <p>
	 * <i>From bytes to String:</i>
	 * 
	 * <pre>
	 * {@code
	 * Decryptor decryptor = ...
	 * byte[] data = ...
	 * String decryptedData = decryptor.decrypt(data).result().asString();
	 * }
	 * </pre>
	 * 
	 * @param input
	 *            The input to be processed by the returned {@link Decryptor.Processor}.
	 * @return The {@link Decryptor.Processor} for decrypting the given input.
	 * @throws CryptorException
	 *             If the creation of a {@link Decryptor.Processor} for decrypting the given input fails.
	 */
	Decryptor.Processor decrypt(byte[] input) throws CryptorException;

	/**
	 * <p>
	 * Returns a {@link Decryptor.StringProcessor} for encrypting the given String input.
	 * 
	 * <p>
	 * <b>Examples</b>
	 * 
	 * <p>
	 * <i>From String to bytes:</i>
	 * 
	 * <pre>
	 * {@code
	 * Decryptor decryptor = ...
	 * String data = ...
	 * byte[] decryptedData = decryptor.decrypt(data).result().asBytes();
	 * }
	 * </pre>
	 * 
	 * <p>
	 * <i>From String to String:</i>
	 * 
	 * <pre>
	 * {@code
	 * Decryptor decryptor = ...
	 * String data = ...
	 * String decryptedData = decryptor.decrypt(data).result().asString();
	 * }
	 * </pre>
	 * 
	 * @param input
	 *            The input to be processed by the returned {@link Decryptor.StringProcessor}.
	 * @return The {@link Decryptor.StringProcessor} for decrypting the given input.
	 * @throws CryptorException
	 *             If the creation of a {@link Decryptor.StringProcessor} for decrypting the given input fails.
	 */
	Decryptor.StringProcessor decrypt(String input) throws CryptorException;

	/**
	 * <p>
	 * Wraps a {@link InputStream} for decrypting data.
	 * 
	 * @param inputStream
	 *            The {@code InputStream} to be wrapped for decrypting data
	 * @return A {@code InputStream} wrapped for decrypting data from the given {@code InputStream}
	 * @throws CryptorException
	 *             If the wrapping of the given {@code InputStream} for decrypting fails.
	 */
	InputStream wrap(InputStream inputStream) throws CryptorException;

	/**
	 * <p>
	 * A {@link com.braintribe.crypto.Cryptor.Processor} 
	 * for {@link com.braintribe.crypto.Decryptor}(s).
	 * 
	 */
	public interface Processor extends Cryptor.Processor<Decryptor.Response> {

		@Override
		Decryptor.Response result() throws CryptorException;

	}

	/**
	 * <p>
	 * A String-handling {@link com.braintribe.crypto.Cryptor.Processor} 
	 * for {@link com.braintribe.crypto.Decryptor}(s).
	 * 
	 */
	public interface StringProcessor extends Processor {

		Decryptor.StringProcessor encodedAs(Cryptor.Encoding encoding) throws CryptorException;

	}

	/**
	 * <p>
	 * A {@link com.braintribe.crypto.Cryptor.Response} with specialized methods 
	 * for obtaining data decrypted by a {@link com.braintribe.crypto.Decryptor}.
	 * 
	 */
	public interface Response extends Cryptor.Response {

		String asString(String charsetName) throws CryptorException;

		String asString(Charset charset) throws CryptorException;

	}

}
