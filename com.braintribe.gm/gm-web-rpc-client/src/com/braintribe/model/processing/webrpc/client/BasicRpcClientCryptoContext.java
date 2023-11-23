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

import java.security.Key;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.codec.Codec;
import com.braintribe.crypto.Cryptor;
import com.braintribe.crypto.Cryptor.Encoding;
import com.braintribe.crypto.Decryptor;
import com.braintribe.model.crypto.configuration.encryption.EncryptionConfiguration;
import com.braintribe.model.crypto.configuration.encryption.SymmetricEncryptionConfiguration;
import com.braintribe.model.processing.crypto.factory.CipherCryptorFactory;
import com.braintribe.model.processing.crypto.factory.CryptorFactoryException;
import com.braintribe.model.processing.crypto.token.KeyCodecProvider;
import com.braintribe.model.processing.rpc.commons.api.GmRpcException;
import com.braintribe.model.processing.rpc.commons.api.crypto.RpcClientCryptoContext;

/**
 * <p>
 * A {@link RpcClientCryptoContext} which leverages on {@code CryptoModelProcessingApi} components.
 * 
 * @deprecated Cryptographic capabilities were removed from the RPC layer. This interface is now obsolete and will be
 *             removed in a future version.
 */
@Deprecated
public class BasicRpcClientCryptoContext implements RpcClientCryptoContext {

	private String clientId;
	private KeyCodecProvider<? extends Key> keyCodecProvider;
	private Decryptor clientDecryptor;
	private CipherCryptorFactory<? extends EncryptionConfiguration, ? extends Cryptor> cryptorFactory;

	private String mode = null;
	private String padding = null;
	private String provider = null;

	public BasicRpcClientCryptoContext() {
	}

	@Required
	@Configurable
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	@Configurable
	public void setSymmetricEncryptionConfiguration(SymmetricEncryptionConfiguration symmetricEncryptionConfiguration) {
		if (symmetricEncryptionConfiguration != null) {
			mode = symmetricEncryptionConfiguration.getMode();
			padding = symmetricEncryptionConfiguration.getPadding();
			provider = symmetricEncryptionConfiguration.getProvider();
		}
	}

	@Required
	@Configurable
	public <T extends Key> void setKeyCodecProvider(KeyCodecProvider<T> keyCodecProvider) {
		this.keyCodecProvider = keyCodecProvider;
	}

	@Required
	@Configurable
	public void setClientDecryptor(Decryptor clientDecryptor) {
		this.clientDecryptor = clientDecryptor;
	}

	@Required
	@Configurable
	public void setCryptorFactory(CipherCryptorFactory<? extends EncryptionConfiguration, ? extends Cryptor> cryptorFactory) {
		this.cryptorFactory = cryptorFactory;
	}

	@Override
	public String getClientId() {
		return clientId;
	}

	@Override
	public Decryptor responseDecryptor(String encodedKey, String keyAlgorithm) throws GmRpcException {

		Key key = importEncryptedKey(encodedKey, keyAlgorithm);

		Decryptor decryptor = null;
		try {
			decryptor = cryptorFactory.builder().key(key).mode(mode).padding(padding).provider(provider).build(Decryptor.class);
		} catch (CryptorFactoryException e) {
			throw new GmRpcException("Failed to create a " + keyAlgorithm + " Decryptor" + (e.getMessage() != null ? ": " + e.getMessage() : ""), e);
		}

		return decryptor;

	}

	@Override
	public Key importKey(String encodedKey, String keyAlgorithm) throws GmRpcException {
		return importEncryptedKey(encodedKey, keyAlgorithm);
	}

	private Key importEncryptedKey(String encodedKey, String keyAlgorithm) throws GmRpcException {

		byte[] keyDecrypted = null;
		try {
			keyDecrypted = clientDecryptor.decrypt(encodedKey).encodedAs(Encoding.base64).result().asBytes();
		} catch (Exception e) {
			throw new GmRpcException("Client [ " + clientId + " ] failed to decrypt the response key" + (e.getMessage() != null ? ": " + e.getMessage() : ""), e);
		}

		Codec<? extends Key, byte[]> keyCodec = null;
		try {
			keyCodec = keyCodecProvider.getKeyCodec(keyAlgorithm);
		} catch (Exception e) {
			throw new GmRpcException("Failed to retrieve a codec for [ " + keyAlgorithm + " ] key" + (e.getMessage() != null ? ": " + e.getMessage() : ""), e);
		}

		Key key = null;
		try {
			key = keyCodec.decode(keyDecrypted);
		} catch (Exception e) {
			throw new GmRpcException("Client [ " + clientId + " ] failed to decode response key" + (e.getMessage() != null ? ": " + e.getMessage() : ""), e);
		}

		return key;

	}

}
