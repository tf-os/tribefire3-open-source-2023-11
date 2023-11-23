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
package com.braintribe.codec.marshaller.crypto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.util.Objects;
import java.util.function.Function;

import javax.crypto.Cipher;

import com.braintribe.codec.marshaller.api.CryptoMarshallerDecorator;
import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.crypto.Cryptor;
import com.braintribe.crypto.Decryptor;
import com.braintribe.crypto.Encryptor;
import com.braintribe.utils.IOTools;

public class BasicCryptoMarshallerDecorator implements CryptoMarshallerDecorator {

	private Key key;
	private String cipherTransformation;
	private Function<String, String> cipherTransformationProvider;

	public BasicCryptoMarshallerDecorator() {
	}

	public void setKey(Key key) {
		this.key = key;
	}

	public void setCipherTransformation(String cipherTransformation) {
		this.cipherTransformation = cipherTransformation;
	}

	public void setCipherTransformationProvider(Function<String, String> cipherTransformationProvider) {
		this.cipherTransformationProvider = cipherTransformationProvider;
	}

	@Override
	public Marshaller decorate(Marshaller delegate) throws MarshallException {
		return new CryptoMarshaller(delegate, this.key, this.cipherTransformation, this.cipherTransformationProvider);
	}

	@Override
	public Marshaller decorate(Marshaller delegate, Key _key) throws MarshallException {
		return new CryptoMarshaller(delegate, _key, this.cipherTransformation, this.cipherTransformationProvider);
	}

	@Override
	public Marshaller decorate(Marshaller delegate, Key _key, String _cipherTransformation) throws MarshallException {
		return new CryptoMarshaller(delegate, _key, _cipherTransformation, this.cipherTransformationProvider);
	}

	@Override
	public Marshaller decorate(Marshaller delegate, Cryptor cryptor) throws MarshallException {
		return new CryptorBasedMarshaller(delegate, cryptor);
	}

	/**
	 * A {@link Key} based {@link Marshaller} decorator.
	 * 
	 *
	 */
	private static class CryptoMarshaller implements Marshaller {

		private Marshaller delegate;
		private Key key;
		private String cipherTransformation;

		public CryptoMarshaller(Marshaller delegate, Key key, String cipherTransformation, Function<String, String> cipherTransformationProvider)
				throws MarshallException {

			Objects.requireNonNull(delegate, "delegate Marshaller must not be null");
			Objects.requireNonNull(key, "key must not be null");

			this.delegate = delegate;
			this.key = key;

			// decide which transformation to use:

			if (cipherTransformation != null) {
				this.cipherTransformation = cipherTransformation;
			} else if (cipherTransformationProvider != null) {
				try {
					this.cipherTransformation = cipherTransformationProvider.apply(key.getAlgorithm());
				} catch (RuntimeException e) {
					throw new MarshallException("Failed to obtain a preffered transformation for the key algorithm []: " + e.getMessage(), e);
				}
			} else {
				this.cipherTransformation = key.getAlgorithm();
			}

		}

		@Override
		public void marshall(OutputStream out, Object value, GmSerializationOptions options) throws MarshallException {

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			delegate.marshall(baos, value, options);

			byte[] encrypted = encrypt(baos.toByteArray());

			try {
				IOTools.pump(new ByteArrayInputStream(encrypted), out);
			} catch (IOException e) {
				throw new MarshallException("Failed to write to output: " + e.getMessage(), e);
			}

		}

		@Override
		public Object unmarshall(InputStream in, GmDeserializationOptions options) throws MarshallException {

			byte[] encrypted = null;
			try {
				encrypted = IOTools.inputStreamToByteArray(in);
			} catch (IOException e) {
				throw new MarshallException("Failed to read from input: " + e.getMessage(), e);
			}

			InputStream decryptedInput = new ByteArrayInputStream(decrypt(encrypted));

			return delegate.unmarshall(decryptedInput, options);

		}

		@Override
		public void marshall(OutputStream out, Object value) throws MarshallException {
			marshall(out, value, GmSerializationOptions.deriveDefaults().build());
		}

		@Override
		public Object unmarshall(InputStream in) throws MarshallException {
			return unmarshall(in, GmDeserializationOptions.deriveDefaults().build());
		}

		private byte[] encrypt(byte[] decrypted) throws MarshallException {
			try {
				Cipher cipher = Cipher.getInstance(cipherTransformation);
				cipher.init(Cipher.ENCRYPT_MODE, key);
				return cipher.doFinal(decrypted);
			} catch (Exception e) {
				throw new MarshallException("Failed to encrypt input: " + e.getMessage(), e);
			}
		}

		private byte[] decrypt(byte[] encrypted) throws MarshallException {
			try {
				Cipher cipher = Cipher.getInstance(cipherTransformation);
				cipher.init(Cipher.DECRYPT_MODE, key);
				return cipher.doFinal(encrypted);
			} catch (Exception e) {
				throw new MarshallException("Failed to decrypt input: " + e.getMessage(), e);
			}
		}

	}

	private static class CryptorBasedMarshaller implements Marshaller {

		private Marshaller delegate;
		private Class<? extends Cryptor> cryptorType;
		private Encryptor encryptor;
		private Decryptor decryptor;

		public CryptorBasedMarshaller(Marshaller delegate, Cryptor cryptor) {

			Objects.requireNonNull(delegate, "delegate Marshaller must not be null");
			Objects.requireNonNull(cryptor, "cryptor must not be null");

			this.delegate = delegate;

			this.cryptorType = cryptor.getClass();
			this.encryptor = (cryptor instanceof Encryptor) ? (Encryptor) cryptor : null;
			this.decryptor = (cryptor instanceof Decryptor) ? (Decryptor) cryptor : null;

		}

		@Override
		public void marshall(OutputStream out, Object value, GmSerializationOptions options) throws MarshallException {

			if (encryptor == null) {
				throw new MarshallException("This marshaller is based on a cryptor which doesn't encrypt: " + cryptorType);
			}

			try {
				out = encryptor.wrap(out);
			} catch (Exception e) {
				throw new MarshallException("Failed to wrap output stream: " + e.getMessage(), e);
			}

			delegate.marshall(out, value, options);

		}

		@Override
		public Object unmarshall(InputStream in, GmDeserializationOptions options) throws MarshallException {

			if (decryptor == null) {
				throw new MarshallException("This marshaller is based on a cryptor which doesn't decrypt: " + cryptorType);
			}

			try {
				in = decryptor.wrap(in);
			} catch (Exception e) {
				throw new MarshallException("Failed to wrap input stream: " + e.getMessage(), e);
			}

			return delegate.unmarshall(in, options);

		}

		@Override
		public void marshall(OutputStream out, Object value) throws MarshallException {
			marshall(out, value, GmSerializationOptions.deriveDefaults().build());
		}

		@Override
		public Object unmarshall(InputStream in) throws MarshallException {
			return unmarshall(in, GmDeserializationOptions.deriveDefaults().build());
		}

	}

}
