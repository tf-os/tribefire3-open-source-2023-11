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

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.security.Key;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;

import com.braintribe.codec.Codec;
import com.braintribe.crypto.Cryptor;
import com.braintribe.crypto.CryptorException;
import com.braintribe.crypto.Encryptor;
import com.braintribe.logging.Logger;

/**
 * <p>
 * A {@link CipherCryptor} which encrypts data.
 *
 * @see Encryptor
 */
public class CipherEncryptor extends AbstractCipherCryptor implements Encryptor {

	private static final Logger log = Logger.getLogger(CipherEncryptor.class);

	private Key encryptingKey = null;

	private boolean deterministic = true;

	/**
	 * <p>
	 * Unique constructor.
	 * 
	 * @param key
	 *            The {@link Key} to be used for encrypting data.
	 * @param transformation
	 *            The JCE {@link Cipher} transformation string to be used by this {@link Encryptor}.
	 * @param provider
	 *            The JCE provider to be used by this {@link Encryptor}.
	 * @param stringCodecs
	 *            Maps of string {@link Codec} instances to be used by this {@link Encryptor} for processing
	 *            {@link String}(s) based on {@link com.braintribe.crypto.Cryptor.Encoding}(s).
	 * @param defaultStringCharset
	 *            The default {@link Charset} to be used by this {@link Encryptor} for obtaining the byte sequence of
	 *            {@link String}(s).
	 * @param validate
	 *            Whether the configuration is eagerly validated.
	 * @throws CryptorException
	 *             If the given arguments are insufficient (or invalid) for creating a {@code CipherEncryptor}.
	 */
	public CipherEncryptor(Key key, String transformation, String provider, Map<Encoding, Codec<byte[], String>> stringCodecs, Charset defaultStringCharset, boolean validate) throws CryptorException {
		super(transformation, provider, stringCodecs, defaultStringCharset);
		this.encryptingKey = key;

		if (key instanceof PublicKey) {
			deterministic = false;
		}

		if (validate) {
			validate();
		}

	}

	@Override
	public Encryptor.Processor encrypt(byte[] input) throws CryptorException {
		validateInput(input);
		return new CipherEncryptorProcessor().forInput(input);
	}

	@Override
	public Encryptor.StringProcessor encrypt(String input) throws CryptorException {
		validateInput(input);
		return new CipherEncryptorStringProcessor().forInput(input);
	}

	@Override
	public OutputStream wrap(OutputStream outputStream) throws CryptorException {
		CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, getInitializedCipher());
		return cipherOutputStream;
	}

	@Override
	public Cryptor.Matcher is(byte[] input) throws CryptorException {
		validateInput(input);
		if (this.isDeterministic()) {
			return new CipherEncryptorMatcher().forInput(input);
		}
		throw new UnsupportedOperationException("Matching is not supported for non-deterministic, unidirectional Cipher-based encryptors");
	}

	@Override
	public Cryptor.StringMatcher is(String input) throws CryptorException {
		validateInput(input);
		if (this.isDeterministic()) {
			return new CipherEncryptorStringMatcher().forInput(input);
		}
		throw new UnsupportedOperationException("Matching is not supported for non-deterministic, unidirectional Cipher-based encryptors");
	}

	protected byte[] compute(byte[] input) throws CryptorException {

		boolean trace = log.isTraceEnabled();

		long t = 0;
		if (trace) {
			log.trace("Requested to encrypt a " + safeDescription(input));
			t = System.currentTimeMillis();
		}

		byte[] output = null;

		Cipher cipher = getInitializedCipher();
		try {
			output = cipher.doFinal(input);
		} catch (Exception e) {
			throw new CryptorException("Failed to encrypt" + (e.getMessage() != null ? ": " + e.getMessage() : ""), e);
		}

		if (trace) {
			t = System.currentTimeMillis() - t;
			log.trace("Decryption of " + safeDescription(input) + " using " + transformation + " took " + t + " ms and resulted in: " + safeDescription(output));
		}

		return output;

	}

	/**
	 * <p>
	 * Returns an initialized {@link Cipher} for this {@link CipherEncryptor}.
	 * 
	 * @return An initialized {@link Cipher} for this {@link CipherEncryptor}
	 * @throws CryptorException
	 *             If an {@link Cipher} for this {@link CipherEncryptor} fails to be created or initiaized
	 */
	protected Cipher getInitializedCipher() throws CryptorException {
		Cipher cipher = getInitializedCipher(Cipher.ENCRYPT_MODE, encryptingKey);
		return cipher;
	}

	/**
	 * <p>
	 * Validates whether this {@link CipherEncryptor} is properly configured for its purpose.
	 * 
	 * <p>
	 * This method validates transformation, provider and key, by creating and initializing a disposable {@link Cipher}.
	 * 
	 * <p>
	 * As this is a costly operation, it is advised to perform this validation only when providers or transformations
	 * other than those defined by the Java Cryptography Architecture (JCA) are used and a fail-fast behavior for the
	 * component is preferred.
	 * 
	 * @throws CryptorException
	 *             If this {@link CipherEncryptor} configuration is not capable of creating and initializing a
	 *             {@link Cipher}.
	 */
	protected void validate() throws CryptorException {
		getInitializedCipher();
	}

	@Override
	public boolean isDeterministic() {
		return deterministic;
	}

	class CipherEncryptorProcessor extends StandardEncryptorProcessor {

		CipherEncryptorProcessor() {
		}

		@Override
		public Encryptor.Processor withSaltFrom(byte[] encryptedValue) throws CryptorException {
			return this;
		}

		byte[] getSalt() {
			return null;
		}

		@Override
		public Encryptor.Response result() throws CryptorException {

			byte[] encryptedBytes = compute(input);

			return new StandardEncryptorResponse(encryptedBytes);

		}

	}

	class CipherEncryptorStringProcessor extends CipherEncryptorProcessor implements Encryptor.StringProcessor {

		String stringInput;
		Charset stringInputCharset;

		CipherEncryptorStringProcessor() {
		}

		protected StringProcessor forInput(String hasherInput) {
			stringInput = hasherInput;
			return this;
		}

		@Override
		public StringProcessor charset(String charsetName) throws CryptorException {
			stringInputCharset = getCharset(charsetName);
			return this;
		}

		@Override
		public StringProcessor charset(Charset charset) throws CryptorException {
			stringInputCharset = charset;
			return this;
		}

		@Override
		public Encryptor.Response result() throws CryptorException {
			readStringBytes();
			return super.result();
		}

		protected void readStringBytes() {
			input = stringInput.getBytes(stringInputCharset != null ? stringInputCharset : defaultCharset);
		}

	}

	class CipherEncryptorMatcher extends StandardMatcher {

		CipherEncryptorMatcher() {
		}

		@Override
		public boolean equals(byte[] encryptedValue) throws CryptorException {

			byte[] reencrypted = compute(cleanValue);

			return Arrays.equals(encryptedValue, reencrypted);
		}

	}

	class CipherEncryptorStringMatcher extends CipherEncryptorMatcher implements Encryptor.StringMatcher {

		String stringInput;
		Charset stringInputCharset;

		CipherEncryptorStringMatcher() {
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
