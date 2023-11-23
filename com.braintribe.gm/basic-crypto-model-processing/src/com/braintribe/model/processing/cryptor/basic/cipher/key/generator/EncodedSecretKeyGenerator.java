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

import javax.crypto.SecretKey;

import com.braintribe.model.crypto.key.encoded.EncodedSecretKey;
import com.braintribe.model.processing.crypto.token.generator.EncryptionTokenGeneratorException;
import com.braintribe.model.processing.crypto.token.generator.KeyPairGenerator;
import com.braintribe.model.processing.crypto.token.generator.SecretKeyGenerator;

/**
 * <p>
 * A {@link KeyPairGenerator} which generates {@link SecretKey}(s) based on {@link EncodedSecretKey}(s).
 * 
 * <p>
 * This generator enriches the given {@link EncodedSecretKey} instances with the generated key's material.
 * 
 */
public class EncodedSecretKeyGenerator extends EncodedKeyGenerator implements SecretKeyGenerator<EncodedSecretKey, SecretKey> {

	@Override
	public SecretKey generate(EncodedSecretKey encryptionToken, String provider) throws EncryptionTokenGeneratorException {

		SecretKey secretKey = generateSecretKey(encryptionToken.getKeyAlgorithm(), provider, encryptionToken);

		export(secretKey, encryptionToken);

		return secretKey;

	}

}
