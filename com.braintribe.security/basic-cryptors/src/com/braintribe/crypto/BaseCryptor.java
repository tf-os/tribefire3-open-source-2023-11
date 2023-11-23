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

import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.util.Map;

import com.braintribe.codec.Codec;

/**
 * <p>
 * An abstraction providing common properties and functionalities to the concrete {@link Cryptor}(s).
 * 
 */
public abstract class BaseCryptor extends AbstractCryptor {

	protected Map<Encoding, Codec<byte[], String>> stringCodecs;

	protected Encoding defaultEncoding = Cryptor.Encoding.base64;

	protected Charset defaultCharset = Charset.forName("UTF-8");

	protected BaseCryptor(Map<Encoding, Codec<byte[], String>> stringCodecs, Encoding encoding, Charset defaultCharset) throws CryptorException {

		if (stringCodecs == null) {
			throw new CryptorException("Cannot build a " + this.getClass().getName() + " without a map of string codecs");
		}

		this.stringCodecs = stringCodecs;

		if (encoding != null) {
			defaultEncoding = encoding;
		}

		if (defaultCharset != null) {
			this.defaultCharset = defaultCharset;
		}

	}

	protected static void validateInput(Object input) {
		if (input == null) {
			throw new IllegalArgumentException("Crytors inputs cannot be null");
		}
	}
	
	protected String encodeString(byte[] input, Encoding encoding) throws CryptorException {

		Codec<byte[], String> stringCodec = (stringCodecs != null) ? stringCodecs.get(encoding) : null;

		if (stringCodec == null) {
			throw new CryptorException("Encoding not supported: ["+encoding+"]");
		}
		
		try {
			return stringCodec.encode(input);
		} catch (Exception e) {
			throw CryptorException.wrap("Failed to encode to string as "+encoding, e);
		}
		
	}
	
	protected byte[] decodeString(String input, Encoding encoding) throws CryptorException {

		Codec<byte[], String> stringCodec = (stringCodecs != null) ? stringCodecs.get(encoding) : null;

		if (stringCodec == null) {
			throw new CryptorException("Encoding not supported: ["+encoding+"]");
		}
		
		try {
			return stringCodec.decode(input);
		} catch (Exception e) {
			throw CryptorException.wrap("Failed to decode "+encoding+" string", e);
		}
		
	}

	/**
	 * <p>
	 * Returns a safe {@code String} description of a given input subjected to encryption or decryption.
	 * 
	 * <p>
	 * This method is meant to be used by internal tracing, like exception messages and low level (trace/debug) log
	 * entries.
	 * 
	 * @param input
	 *            The input, subject to encryption or decryption, from which a {@code String} description is required
	 * @return A {@code String} description of the input provided
	 */
	public static String safeDescription(Object input) {
		return safeDescription(input, false);
	}

	/**
	 * <p>
	 * Returns a safe {@code String} description of a given input subjected to encryption or decryption.
	 * 
	 * <p>
	 * This method is meant to be used by internal tracing, like exception messages and low level (trace/debug) log
	 * entries.
	 * 
	 * @param input
	 *            The input, subject to encryption or decryption, from which a {@code String} description is required
	 * @return A {@code String} description of the input provided
	 */
	public static String safeDescription(Object input, boolean exposeStringValue) {

		if (input == null) {
			return "null value";
		}

		if (input instanceof byte[]) {
			return "sequence of " + ((byte[]) input).length + " bytes";
		}

		if (input instanceof CharSequence) {
			if (exposeStringValue) {
				return ((CharSequence) input).toString();
			} else {
				return "sequence of " + ((CharSequence) input).length() + " characters";
			}
		}

		if (input instanceof short[]) {
			return "sequence of " + ((short[]) input).length + " 16-bit integers";
		}

		if (input instanceof int[]) {
			return "sequence of " + ((int[]) input).length + " 32-bit integers";
		}

		if (input instanceof long[]) {
			return "sequence of " + ((long[]) input).length + " 64-bit integers";
		}

		if (input instanceof float[]) {
			return "sequence of " + ((float[]) input).length + " 32-bit floating point numbers";
		}

		if (input instanceof double[]) {
			return "sequence of " + ((double[]) input).length + " 64-bit floating point numbers";
		}

		try {
			int l = Array.getLength(input);
			return "sequence of " + l + " objects";
		} catch (IllegalArgumentException iae) {
			// Expected to occur as result of the array check.
		}

		return input.getClass().getSimpleName() + " object";

	}

	protected static Charset getCharset(String charsetName) throws CryptorException {
		Charset charset = null;
		try {
			charset = Charset.forName(charsetName);
		} catch (Exception e) {
			throw CryptorException.wrap("Invalid charset name", e);
		}
		return charset;
	}

	protected abstract class StandardCryptorResponse implements Cryptor.Response {

		byte[] cryptedValue;

		protected StandardCryptorResponse(byte[] cryptedValue) {
			this.cryptedValue = cryptedValue;
		}

		@Override
		public byte[] asBytes() {
			return cryptedValue;
		}

	}

	protected class StandardEncryptorResponse extends StandardCryptorResponse implements Encryptor.Response {

		public StandardEncryptorResponse(byte[] cryptedValue) {
			super(cryptedValue);
		}

		@Override
		public String asString() throws CryptorException {
			return asString(defaultEncoding);
		}

		@Override
		public String asString(Encoding encoding) throws CryptorException {
			return encodeString(cryptedValue, encoding);
		}

	}

	protected class StandardDecryptorResponse extends StandardCryptorResponse implements Decryptor.Response {

		public StandardDecryptorResponse(byte[] crytedValue) {
			super(crytedValue);
		}

		@Override
		public String asString() throws CryptorException {
			return asString(defaultCharset);
		}

		@Override
		public String asString(String charsetName) throws CryptorException {
			
			Charset charset = null;
			try {
				charset = Charset.forName(charsetName);
			} catch (Exception e) {
				throw CryptorException.wrap("Invalid charset name ["+charsetName+"]", e);
			}
			
			return asString(charset);
			
		}

		@Override
		public String asString(Charset charset) throws CryptorException {
			try {
				return new String(cryptedValue, charset);
			} catch (Exception e) {
				throw CryptorException.wrap("Failed to create string", e);
			}
		}

	}

	public abstract class StandardEncryptorProcessor implements Encryptor.Processor {

		protected byte[] input;
		protected byte[] customSalt;

		protected StandardEncryptorProcessor() {
		}

		public Encryptor.Processor forInput(byte[] encryptionInput) {
			this.input = encryptionInput;
			return this;
		}

	}

	public abstract class StandardDecryptorProcessor implements Decryptor.Processor {

		protected byte[] input;
		
		protected StandardDecryptorProcessor() {
		}

		public Decryptor.Processor forInput(byte[] decryptionInput) {
			this.input = decryptionInput;
			return this;
		}
		
	}

	public abstract class StandardMatcher implements Cryptor.Matcher {

		protected byte[] cleanValue;

		protected StandardMatcher() {
		}

		public Cryptor.Matcher forInput(byte[] cleanInput) {
			cleanValue = cleanInput;
			return this;
		}

		@Override
		public boolean equals(String encryptedValue) throws CryptorException {
			return equals(encryptedValue, defaultEncoding);
		}

		@Override
		public boolean equals(String encryptedValue, Encoding encoding) throws CryptorException {

			byte[] encrypted = decodeString(encryptedValue, encoding);

			return equals(encrypted);

		}

	}

}
