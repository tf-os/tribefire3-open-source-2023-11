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
package com.braintribe.model.processing.aspect.crypto.test.cryptor.provider;

import org.junit.Test;

import com.braintribe.crypto.cipher.CipherCryptor;
import com.braintribe.crypto.hash.Hasher;
import com.braintribe.model.crypto.configuration.encryption.EncryptionConfiguration;
import com.braintribe.model.crypto.configuration.hashing.HashingConfiguration;
import com.braintribe.model.meta.data.crypto.PropertyCrypting;
import com.braintribe.model.processing.aspect.crypto.test.TestBase;

public class BasicCryptorProviderTest extends TestBase {

	@Test
	public void testForAllSupportedHashingConfigurations() throws Exception {
		for (HashingConfiguration configuration : hashingConfigurations.values()) {
			PropertyCrypting propertyCrypting = createPropertyCrypting(configuration);
			Hasher hasher = cryptorProvider.provideFor(Hasher.class, propertyCrypting);
			testCryptor(hasher);
		}
	}
	
	@Test
	public void testForAllSupportedEncryptionConfigurations() throws Exception {
		for (EncryptionConfiguration configuration : encryptionConfigurations.values()) {
			PropertyCrypting propertyCrypting = createPropertyCrypting(configuration);
			CipherCryptor cipherCryptor = cryptorProvider.provideFor(CipherCryptor.class, propertyCrypting);
			testCryptor(cipherCryptor);
		}
	}
	
	
}
