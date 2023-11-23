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
package com.braintribe.model.processing.cryptor.basic.cipher.key.generator;

import java.security.KeyPair;

import com.braintribe.model.crypto.key.encoded.EncodedKeyPair;
import com.braintribe.model.processing.crypto.token.generator.EncryptionTokenGeneratorException;
import com.braintribe.model.processing.crypto.token.generator.KeyPairGenerator;

/**
 * <p>
 * A {@link KeyPairGenerator} which generates {@link KeyPair}(s) based on {@link EncodedKeyPair}(s).
 * 
 * <p>
 * This generator enriches the given {@link EncodedKeyPair} instances with the generated key's material.
 * 
 */
public class EncodedKeyPairGenerator extends EncodedKeyGenerator implements KeyPairGenerator<EncodedKeyPair> {

	@Override
	public KeyPair generate(EncodedKeyPair encryptionToken, String provider) throws EncryptionTokenGeneratorException {

		KeyPair keyPair = generateKeyPair(encryptionToken.getKeyAlgorithm(), provider, encryptionToken);

		if (encryptionToken.getPublicKey() != null && keyPair.getPublic() != null) {
			export(keyPair.getPublic(), encryptionToken.getPublicKey());
		}

		if (encryptionToken.getPrivateKey() != null && keyPair.getPrivate() != null) {
			export(keyPair.getPrivate(), encryptionToken.getPrivateKey());
		}

		return keyPair;
	}

}
