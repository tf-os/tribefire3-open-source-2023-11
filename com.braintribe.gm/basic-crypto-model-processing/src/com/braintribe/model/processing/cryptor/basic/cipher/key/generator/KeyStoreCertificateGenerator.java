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

import java.security.cert.Certificate;

import com.braintribe.model.crypto.key.keystore.KeyStoreCertificate;
import com.braintribe.model.processing.crypto.token.generator.CertificateGenerator;
import com.braintribe.model.processing.crypto.token.generator.EncryptionTokenGeneratorException;

/**
 * <p>
 * A {@link KeyStoreEntryGenerator} which generates {@link Certificate}(s) based on {@link KeyStoreCertificate}(s).
 * 
 * <p>
 * This generator creates the key store entries as defined by {@link KeyStoreCertificate} specifications.
 * 
 */
public class KeyStoreCertificateGenerator extends KeyStoreEntryGenerator implements CertificateGenerator<KeyStoreCertificate, java.security.cert.Certificate> {

	@Override
	public Certificate generate(KeyStoreCertificate encryptionToken, String provider) throws EncryptionTokenGeneratorException {

		Certificate certificate = generateCertificate(encryptionToken.getKeyAlgorithm(), provider, encryptionToken);

		return certificate;
	}

}
