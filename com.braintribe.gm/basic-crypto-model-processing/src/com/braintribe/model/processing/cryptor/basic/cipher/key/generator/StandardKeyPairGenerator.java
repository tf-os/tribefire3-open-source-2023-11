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

import com.braintribe.model.processing.crypto.token.generator.EncryptionTokenGeneratorException;
import com.braintribe.model.processing.crypto.token.generator.KeyPairGenerator;

/**
 * <p>
 * A {@link KeyPairGenerator} which generates {@link KeyPair}(s) based on
 * {@link com.braintribe.model.crypto.key.KeyPair}(s).
 * 
 */
public class StandardKeyPairGenerator extends StandardKeyGenerator implements KeyPairGenerator<com.braintribe.model.crypto.key.KeyPair> {

	@Override
	public KeyPair generate(com.braintribe.model.crypto.key.KeyPair encryptionToken, String provider) throws EncryptionTokenGeneratorException {
		KeyPair keyPair = generateKeyPair(encryptionToken.getKeyAlgorithm(), provider, encryptionToken);
		return keyPair;
	}

}
