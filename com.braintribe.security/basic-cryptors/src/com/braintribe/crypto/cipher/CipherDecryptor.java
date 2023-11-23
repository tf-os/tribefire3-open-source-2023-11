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
package com.braintribe.crypto.cipher;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.Key;
import java.util.Arrays;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;

import com.braintribe.codec.Codec;
import com.braintribe.crypto.Cryptor;
import com.braintribe.crypto.CryptorException;
import com.braintribe.crypto.Decryptor;
import com.braintribe.crypto.Encryptor;
import com.braintribe.logging.Logger;

/**
 * <p>
 * A {@link CipherCryptor} which decrypts data.
 * 
 * @see Decryptor
 */
public class CipherDecryptor extends AbstractCipherCryptor implements Decryptor {

	private static final Logger log = Logger.getLogger(CipherDecryptor.class);

	private Key decryptingKey = null;

	/**
	 * <p>
	 * Unique constructor.
	 * 
	 * @param key
	 *            The {@link Key} to be used for decrypting data.
	 * @param transformation
	 *            The JCE {@link Cipher} transformation string to be used by this {@link Decryptor}.
	 * @param provider
	 *            The JCE provider to be used by this {@link Decryptor}.
	 * @param stringCodecs
	 *            Maps of string {@link Codec} instances to be used by this {@link Decryptor} for processing
	 *            {@link String}(s) based on {@link com.braintribe.crypto.Cryptor.Encoding}(s).
	 * @param defaultStringCharset
	 *            The default {@link Charset} to be used by this {@link Decryptor} for obtaining the byte sequence of
	 *            {@link String}(s).
	 * @param validate
	 *            Whether the configuration is eagerly validated.
	 * @throws CryptorException
	 *             If the given arguments are insufficient (or invalid) for creating a {@code CipherDecryptor}.
	 */
	public CipherDecryptor(Key key, String transformation, String provider, Map<Encoding, Codec<byte[], String>> stringCodecs, Charset defaultStringCharset, boolean validate) throws CryptorException {
		super(transformation, provider, stringCodecs, defaultStringCharset);
		this.decryptingKey = key;

		if (validate) {
			validate();
		}

	}

	@Override
	public Decryptor.Processor decrypt(byte[] input) throws CryptorException {
		validateInput(input);
		return new CipherDecryptorProcessor().forInput(input);
	}

	@Override
	public Decryptor.StringProcessor decrypt(String input) throws CryptorException {
		validateInput(input);
		return new CipherDecryptorStringProcessor().forInput(input);
	}

	@Override
	public InputStream wrap(InputStream inputStream) throws CryptorException {
		CipherInputStream cipherInputStream = new CipherInputStream(inputStream, getInitializedCipher());
		return cipherInputStream;
	}

	@Override
	public Cryptor.Matcher is(byte[] input) throws CryptorException {
		validateInput(input);
		return new CipherDecryptorMatcher().forInput(input);
	}

	@Override
	public Cryptor.StringMatcher is(String input) throws CryptorException {
		validateInput(input);
		return new CipherDecryptorStringMatcher().forInput(input);
	}

	protected byte[] decipher(byte[] input) throws CryptorException {

		boolean trace = log.isTraceEnabled();

		long t = 0;
		if (trace) {
			log.trace("Requested to decrypt a " + safeDescription(input, true));
			t = System.currentTimeMillis();
		}

		Cipher cipher = getInitializedCipher();

		byte[] output = null;

		try {
			output = cipher.doFinal(input);
		} catch (Exception e) {
			throw new CryptorException("Failed to decrypt" + (e.getMessage() != null ? ": " + e.getMessage() : ""), e);
		}

		if (trace) {
			t = System.currentTimeMillis() - t;
			log.trace("Decryption of " + safeDescription(input) + " using " + transformation + " took " + t + " ms and resulted in: " + safeDescription(output));
		}

		return output;

	}

	/**
	 * <p>
	 * Returns an initialized {@link Cipher} for this {@link CipherDecryptor}.
	 * 
	 * @return An initialized {@link Cipher} for this {@link CipherDecryptor}
	 * @throws CryptorException
	 *             If an {@link Cipher} for this {@link CipherDecryptor} fails to be created or initiaized
	 */
	protected Cipher getInitializedCipher() throws CryptorException {
		Cipher cipher = getInitializedCipher(Cipher.DECRYPT_MODE, decryptingKey);
		return cipher;
	}

	/**
	 * <p>
	 * Validates whether this {@link CipherDecryptor} is properly configured for its purpose.
	 * 
	 * <p>
	 * This method validates transformation, provider and key, by creating and initializing a disposable {@link Cipher}.
	 * 
	 * <p>
	 * As this is a costly operation, it is advised to perform this validation only when absolutely needed.
	 * 
	 * @throws CryptorException
	 *             If this {@link CipherEncryptor} configuration is not capable of creating and initializing a
	 *             {@link Cipher}.
	 */
	protected void validate() throws CryptorException {
		getInitializedCipher();
	}

	class CipherDecryptorProcessor extends StandardDecryptorProcessor {

		CipherDecryptorProcessor() {
		}

		@Override
		public Decryptor.Response result() throws CryptorException {

			byte[] decryptedBytes = decipher(input);

			return new StandardDecryptorResponse(decryptedBytes);

		}

	}

	class CipherDecryptorStringProcessor extends CipherDecryptorProcessor implements Decryptor.StringProcessor {

		String stringInput;
		Charset stringInputCharset;
		Encoding stringEncoding;

		CipherDecryptorStringProcessor() {
		}

		protected StringProcessor forInput(String hasherInput) {
			stringInput = hasherInput;
			return this;
		}

		@Override
		public StringProcessor encodedAs(Encoding encoding) throws CryptorException {
			this.stringEncoding = encoding;
			return this;
		}

		@Override
		public Decryptor.Response result() throws CryptorException {
			readEncodedString();
			return super.result();
		}

		protected void readEncodedString() throws CryptorException {
			input = decodeString(stringInput, stringEncoding != null ? stringEncoding : defaultEncoding);
		}

	}

	class CipherDecryptorMatcher extends StandardMatcher {

		CipherDecryptorMatcher() {
		}

		@Override
		public boolean equals(byte[] encryptedValue) throws CryptorException {

			byte[] decrypted = decipher(encryptedValue);

			return Arrays.equals(decrypted, cleanValue);
		}

	}

	class CipherDecryptorStringMatcher extends CipherDecryptorMatcher implements Encryptor.StringMatcher {

		String stringInput;
		Charset stringInputCharset;

		CipherDecryptorStringMatcher() {
		}

		Encryptor.StringMatcher forInput(String hasherInput) {
			stringInput = hasherInput;
			return this;
		}

		@Override
		public Encryptor.StringMatcher charset(String charsetName) throws CryptorException {
			stringInputCharset = getCharset(charsetName);
			return this;
		}

		@Override
		public Encryptor.StringMatcher charset(Charset charset) throws CryptorException {
			stringInputCharset = charset;
			return this;
		}

		@Override
		public boolean equals(byte[] encryptedValue) throws CryptorException {
			readStringBytes();
			return super.equals(encryptedValue);
		}

		@Override
		public boolean equals(String encryptedValue) throws CryptorException {
			readStringBytes();
			return super.equals(encryptedValue);
		}

		@Override
		public boolean equals(String encryptedValue, Encoding encoding) throws CryptorException {
			readStringBytes();
			return super.equals(encryptedValue, encoding);
		}

		void readStringBytes() {
			cleanValue = stringInput.getBytes(stringInputCharset != null ? stringInputCharset : defaultCharset);
		}

	}

}
