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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Map;

import com.braintribe.codec.Codec;
import com.braintribe.crypto.BaseCryptor;
import com.braintribe.crypto.Cryptor;
import com.braintribe.crypto.CryptorException;
import com.braintribe.crypto.Encryptor;
import com.braintribe.logging.Logger;

/**
 * <p>
 * A {@link com.braintribe.crypto.Cryptor} implementation which encrypts data based on an one-way hash
 * function algorithm.
 * 
 * @see Encryptor
 */
public class Hasher extends BaseCryptor implements Encryptor {

	private static final Logger log = Logger.getLogger(Hasher.class);

	private String digestAlgorithm;
	private SaltProvider saltProvider;
	private int randomSaltSize = 0;
	private byte[] fixedSalt;
	private boolean deterministic = true;

	/**
	 * <p>
	 * Unique constructor.
	 * 
	 * @param digestAlgorithm
	 *            The one-way hash function algorithm used to encrypt data.
	 * @param stringCodecs
	 *            Maps of string {@link Codec} instances to be used by this {@link Encryptor} for processing
	 *            {@link String}(s) based on {@link com.braintribe.crypto.Cryptor.Encoding}(s).
	 * @param defaultStringCharset
	 *            The default {@link Charset} to be used by this {@link Encryptor} for obtaining the byte sequence of
	 *            {@link String}(s).
	 * @throws CryptorException
	 *             If the given arguments are insufficient (or invalid) for creating a {@code CipherEncryptor}.
	 */
	public Hasher(String digestAlgorithm, SaltProvider saltProvider, Map<Encoding, Codec<byte[], String>> stringCodecs, Charset defaultStringCharset) throws CryptorException {
		super(stringCodecs, null, defaultStringCharset);

		if (digestAlgorithm == null || digestAlgorithm.trim().isEmpty()) {
			throw new CryptorException("Cannot build a " + Hasher.class.getName() + " without a digest algorithm");
		}

		this.digestAlgorithm = digestAlgorithm;
		this.saltProvider = saltProvider;
		
		if (saltProvider != null) {
			if (saltProvider.isRandom()) {
				randomSaltSize = saltProvider.getSaltSize();
				deterministic = false;
			} else {
				fixedSalt = saltProvider.getSalt();
			}
			
		}

	}

	@Override
	public Encryptor.Processor encrypt(byte[] input) throws CryptorException {
		validateInput(input);
		return new HasherProcessor().forInput(input);
	}

	@Override
	public Encryptor.StringProcessor encrypt(String input) throws CryptorException {
		validateInput(input);
		return new HasherStringProcessor().forInput(input);
	}

	@Override
	public OutputStream wrap(OutputStream outputStream) throws CryptorException {

		MessageDigest digest = getMessageDigest();

		HasherOutputStream digestOutputStream = new HasherOutputStream(outputStream, digest);

		byte[] salt = getSalt();

		if (salt != null) {
			try {
				if (randomSaltSize > 0) {
					digestOutputStream.disable();
					digestOutputStream.write(salt);
					digestOutputStream.enable();
				}
				digestOutputStream.write(salt);
			} catch (Exception e) {
				throw CryptorException.wrap("Failed to write salt to output stream", e);
			}
		}

		return digestOutputStream;
	}

	@Override
	public Cryptor.Matcher is(byte[] input) throws CryptorException {
		validateInput(input);
		return new HasherMatcher().forInput(input);
	}

	@Override
	public Cryptor.StringMatcher is(String input) throws CryptorException {
		validateInput(input);
		return new HasherStringMatcher().forInput(input);
	}

	@Override
	public boolean isDeterministic() {
		return deterministic;
	}

	private byte[] digest(byte[] input, byte[] salt) throws CryptorException {
		
		boolean trace = log.isTraceEnabled();

		long t = 0;
		if (trace) {
			t = System.currentTimeMillis();
			log.trace("Requested to hash a " + safeDescription(input));
		}

		MessageDigest digest = getMessageDigest();

		if (salt != null) {
			digest.update(salt);
		}

		digest.update(input);

		byte[] output = digest.digest();
		
		//salt is prepended to the digested result only if this hasher works with random salts
		if (salt != null && randomSaltSize > 0) {
			output = concat(salt, output);
		}
		
		if (trace) {
			t = System.currentTimeMillis() - t;
			log.trace("Hashing of " + safeDescription(input) + " using " + digestAlgorithm + " took "+t+" ms and resulted in: " + safeDescription(output, true));
		}

		return output;

	}

	private MessageDigest getMessageDigest() throws CryptorException {

		MessageDigest digest = null;

		try {
			digest = MessageDigest.getInstance(digestAlgorithm);
			digest.reset();
		} catch (Exception e) {
			throw CryptorException.wrap("Failed to obtain a MessageDigest instance for [" + digestAlgorithm + "]", e);
		}

		return digest;

	}

	private static byte[] concat(byte[] first, byte[] second) {
		byte[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}

	private byte[] getSalt() throws CryptorException {

		if (fixedSalt != null) {
			return fixedSalt;
		}

		if (saltProvider != null) {
			return saltProvider.getSalt();
		}

		return null;

	}

	/**
	 * <p>
	 * Extracts the salt part of an encrypted value, if any. Returns null if salts are not used or public.
	 * 
	 * @param encryptedValue Value to have the salt extracted from
	 * @return The extracted salt
	 */
	private byte[] extractSalt(byte[] encryptedValue) {
		
		if (randomSaltSize < 1) {
			return null; //Shouldn't return null?
		}

		byte[] salt = Arrays.copyOf(encryptedValue, randomSaltSize);
		
		return salt;
	}

	class HasherProcessor extends StandardEncryptorProcessor {
		
		HasherProcessor() {
		}
		
		@Override
		public Encryptor.Processor withSaltFrom(byte[] encryptedValue) {
			customSalt = extractSalt(encryptedValue);
			return this;
		}

		@Override
		public Encryptor.Response result() throws CryptorException {

			byte[] salt = customSalt != null ? customSalt : getSalt();

			byte[] encryptedBytes = digest(input, salt);

			return new StandardEncryptorResponse(encryptedBytes);
			
		}

	}
	
	class HasherStringProcessor extends HasherProcessor implements Encryptor.StringProcessor {
		
		String stringInput;
		Charset stringInputCharset;

		HasherStringProcessor() {
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

	class HasherMatcher extends StandardMatcher {

		HasherMatcher() {
		}

		@Override
		public boolean equals(byte[] encryptedValue) throws CryptorException {

			byte[] salt = extractSalt(encryptedValue);

			byte[] reencrypted = digest(cleanValue, salt);

			return Arrays.equals(encryptedValue, reencrypted);
		}

	}
	
	class HasherStringMatcher extends HasherMatcher implements Cryptor.StringMatcher {

		String stringInput;
		Charset stringInputCharset;

		HasherStringMatcher() {
		}
		
		Cryptor.StringMatcher forInput(String hasherInput) {
			stringInput = hasherInput;
			return this;
		}
		
		@Override
		public Cryptor.StringMatcher charset(String charsetName) throws CryptorException {
			stringInputCharset = getCharset(charsetName);
			return this;
		}

		@Override
		public Cryptor.StringMatcher charset(Charset charset) throws CryptorException {
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
	
	private static class HasherOutputStream extends OutputStream {
		
		private boolean enabled = true;
		private MessageDigest digest;
		private OutputStream out;

		public HasherOutputStream(OutputStream outputStream, MessageDigest digest) {
			this.out = outputStream;
			this.digest = digest;
		}

		@Override
		public void write(int b) throws IOException {
	        if (enabled) {
	            digest.update((byte)b);
	        } else {
	        	out.write((byte)b);
	        }
	    }

		@Override
	    public void write(byte[] b, int off, int len) throws IOException {
	        if (enabled) {
	            digest.update(b, off, len);
	        } else {
		        out.write(b, off, len);
	        }
	    }

		@Override
	    public void write(byte[] b) throws IOException {
	        if (enabled) {
	            digest.update(b);
	        } else {
		        out.write(b);
	        }
	    }

		@Override
		public void flush() throws IOException {
			out.write(digest.digest());
			out.flush();
		}

		@Override
		public void close() throws IOException {
			try {
				flush();
			} catch (IOException e) {
				log.trace("Ignored flush error", e);
			}
			out.close();
		}

		public void enable() {
			enabled = true;
		}

		public void disable() {
			enabled = false;
		}
	}

	public static interface SaltProvider {
		byte[] getSalt() throws CryptorException;
		int getSaltSize();
		boolean isRandom();
	}

}
