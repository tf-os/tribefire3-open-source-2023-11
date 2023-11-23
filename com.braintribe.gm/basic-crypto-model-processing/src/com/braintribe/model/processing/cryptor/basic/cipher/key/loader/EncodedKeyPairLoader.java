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
package com.braintribe.model.processing.cryptor.basic.cipher.key.loader;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import com.braintribe.model.crypto.key.encoded.EncodedKeyPair;
import com.braintribe.model.crypto.key.encoded.EncodedPrivateKey;
import com.braintribe.model.crypto.key.encoded.EncodedPublicKey;
import com.braintribe.model.processing.crypto.token.loader.EncryptionTokenLoaderException;
import com.braintribe.model.processing.crypto.token.loader.KeyPairLoader;

/**
 * TODO: document.
 * 
 */
public class EncodedKeyPairLoader extends EncodedKeyLoader implements KeyPairLoader<EncodedKeyPair> {

	@Override
	public KeyPair load(EncodedKeyPair encodedKeyPair) throws EncryptionTokenLoaderException {

		if (encodedKeyPair == null) {
			throw new IllegalArgumentException("Key pair argument cannot be null");
		}

		EncodedPublicKey encodedPublicKey = encodedKeyPair.getPublicKey();
		EncodedPrivateKey encodedPrivateKey = encodedKeyPair.getPrivateKey();

		if (encodedPublicKey == null && encodedPrivateKey == null) {
			throw new IllegalArgumentException("Key pair argument has neither public nor private key");
		}

		PublicKey publicKey = null;
		PrivateKey privateKey = null;

		if (encodedPublicKey != null) {
			publicKey = loadKey(PublicKey.class, encodedPublicKey);
		}

		if (encodedPrivateKey != null) {
			privateKey = loadKey(PrivateKey.class, encodedPrivateKey);
		}

		return new KeyPair(publicKey, privateKey);

	}

}
