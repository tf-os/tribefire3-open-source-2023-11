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
package com.braintribe.model.processing.webrpc.client;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.Key;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.braintribe.crypto.AbstractCryptor;
import com.braintribe.crypto.CryptorException;
import com.braintribe.crypto.Decryptor;
import com.braintribe.model.processing.rpc.commons.api.GmRpcException;
import com.braintribe.model.processing.rpc.commons.api.crypto.RpcClientCryptoContext;

/**
 * <p>
 * A {@link RpcClientCryptoContext} based on {@link com.braintribe.codec.Codec}(s) and
 * {@link Supplier}(s) rather than {@code CryptoModelProcessingApi} components.
 * 
 * @deprecated Cryptographic capabilities were removed from the RPC layer. This interface is now obsolete and will be
 *             removed in a future version.
 */
@Deprecated
public class GmWebRpcClientCryptoContext implements RpcClientCryptoContext {

	private String clientId;
	private Codec<byte[], byte[]> clientDecryptor;
	private Function<String, Codec<Key, byte[]>> responseKeyCodecProvider;
	private Codec<byte[], String> base64Codec;

	public GmWebRpcClientCryptoContext() {
	}

	@Required
	@Configurable
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	@Required
	@Configurable
	public void setClientDecryptor(Codec<byte[], byte[]> clientDecryptor) {
		this.clientDecryptor = clientDecryptor;
	}

	@Required
	@Configurable
	public void setResponseKeyCodecProvider(Function<String, Codec<Key, byte[]>> responseKeyCodecProvider) {
		this.responseKeyCodecProvider = responseKeyCodecProvider;
	}

	@Required
	@Configurable
	public void setBase64Codec(Codec<byte[], String> base64Codec) {
		this.base64Codec = base64Codec;
	}

	@Override
	public String getClientId() {
		return clientId;
	}

	@Override
	public Key importKey(String encodedKey, String keyAlgorithm) throws GmRpcException {

		byte[] keyDecoded = null;

		try {
			keyDecoded = base64Codec.decode(encodedKey);
		} catch (CodecException e) {
			throw new GmRpcException("Client [ " + clientId + " ] failed to decode the response key string: " + e.getMessage(), e);
		}

		byte[] keyDecrypted = null;
		try {
			keyDecrypted = clientDecryptor.decode(keyDecoded);
		} catch (CodecException e) {
			throw new GmRpcException("Client [ " + clientId + " ] failed to decrypt the response key: " + e.getMessage(), e);
		}

		Codec<Key, byte[]> keyCodec = null;
		try {
			keyCodec = responseKeyCodecProvider.apply(keyAlgorithm);
		} catch (Exception e) {
			throw new GmRpcException("Failed to retrieve a codec for [ " + keyAlgorithm + " ] key: " + e.getMessage(), e);
		}

		try {
			return keyCodec.decode(keyDecrypted);
		} catch (CodecException e) {
			throw new GmRpcException("Client [ " + clientId + " ] failed to decode response key: " + e.getMessage(), e);
		}

	}

	@Override
	public Decryptor responseDecryptor(final String encodedKey, final String keyAlgorithm) throws GmRpcException {

		final Key key = importKey(encodedKey, keyAlgorithm);

		return new ResponseDecryptor(key);

	}

	protected static class ResponseDecryptor extends AbstractCryptor implements Decryptor {

		private Key key;

		protected ResponseDecryptor(Key key) {
			this.key = key;
		}

		@Override
		public Matcher is(byte[] input) throws CryptorException, UnsupportedOperationException {
			throw new UnsupportedOperationException("This decryptor does not support matching");
		}

		@Override
		public StringMatcher is(String input) throws CryptorException, UnsupportedOperationException {
			throw new UnsupportedOperationException("This decryptor does not support matching");
		}

		@Override
		public Decryptor.Processor decrypt(final byte[] input) throws CryptorException {

			return new Decryptor.Processor() {

				@Override
				public Decryptor.Response result() throws CryptorException {

					return new Decryptor.Response() {

						@Override
						public String asString() throws CryptorException {
							throw new UnsupportedOperationException("This decryptor does not handle Strings");
						}

						@Override
						public byte[] asBytes() throws CryptorException {
							return decryptBlock(input);
						}

						@Override
						public String asString(String charsetName) throws CryptorException {
							throw new UnsupportedOperationException("This decryptor does not handle Strings");
						}

						@Override
						public String asString(Charset charset) throws CryptorException {
							throw new UnsupportedOperationException("This decryptor does not handle Strings");
						}

					};

				}

			};

		}

		@Override
		public StringProcessor decrypt(String input) throws CryptorException {
			throw new UnsupportedOperationException("This decryptor does not handle Strings");
		}

		@Override
		public InputStream wrap(InputStream inputStream) throws CryptorException {
			CipherInputStream cipherInputStream = new CipherInputStream(inputStream, getInitializedCipher());
			return cipherInputStream;
		}

		private byte[] decryptBlock(byte[] encrypted) throws CryptorException {
			try {
				Cipher cipher = getInitializedCipher();
				return cipher.doFinal(encrypted);
			} catch (Exception e) {
				throw CryptorException.wrap("Failed to decrypt input", e);
			}
		}

		private Cipher getInitializedCipher() throws CryptorException {
			try {
				Cipher cipher = Cipher.getInstance(key.getAlgorithm());
				cipher.init(Cipher.DECRYPT_MODE, key);
				return cipher;
			} catch (Exception e) {
				throw CryptorException.wrap("Failed to initialize Cipher", e);
			}
		}

	}

}
