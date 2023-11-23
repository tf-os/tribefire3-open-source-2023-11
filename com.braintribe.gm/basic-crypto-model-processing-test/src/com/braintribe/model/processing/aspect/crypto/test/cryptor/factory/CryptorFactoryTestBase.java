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
package com.braintribe.model.processing.aspect.crypto.test.cryptor.factory;

import org.junit.Assert;

import com.braintribe.crypto.Cryptor;
import com.braintribe.crypto.Decryptor;
import com.braintribe.crypto.Encryptor;
import com.braintribe.model.crypto.certificate.Certificate;
import com.braintribe.model.crypto.configuration.CryptoConfiguration;
import com.braintribe.model.crypto.configuration.encryption.AsymmetricEncryptionConfiguration;
import com.braintribe.model.crypto.configuration.encryption.SymmetricEncryptionConfiguration;
import com.braintribe.model.crypto.configuration.hashing.HashingConfiguration;
import com.braintribe.model.crypto.key.PrivateKey;
import com.braintribe.model.crypto.key.PublicKey;
import com.braintribe.model.crypto.key.SecretKey;
import com.braintribe.model.crypto.key.encoded.EncodedKeyPair;
import com.braintribe.model.crypto.key.keystore.KeyStoreKeyPair;
import com.braintribe.model.crypto.token.AsymmetricEncryptionToken;
import com.braintribe.model.processing.aspect.crypto.test.TestBase;

public class CryptorFactoryTestBase extends TestBase {

	protected static void assertCryptorType(Cryptor cryptor, CryptoConfiguration cryptoConfiguration) {

		if (cryptoConfiguration instanceof SymmetricEncryptionConfiguration) {
			assertIsEncryptor(cryptor);
			assertIsDecryptor(cryptor);
		} else if (cryptoConfiguration instanceof AsymmetricEncryptionConfiguration) {

			AsymmetricEncryptionConfiguration asymmetricEncryptionConfiguration = (AsymmetricEncryptionConfiguration) cryptoConfiguration;

			AsymmetricEncryptionToken asymmetricToken = asymmetricEncryptionConfiguration.getAsymmetricEncryptionToken();

			Assert.assertNotNull(asymmetricToken);

			if (asymmetricToken instanceof EncodedKeyPair) {
				EncodedKeyPair keyPair = (EncodedKeyPair) asymmetricToken;

				Assert.assertFalse(keyPair.getPublicKey() == null && keyPair.getPrivateKey() == null);

				if (keyPair.getPublicKey() != null && keyPair.getPrivateKey() != null) {
					assertIsEncryptor(cryptor);
					assertIsDecryptor(cryptor);
				} else if (keyPair.getPublicKey() != null) {
					assertIsEncryptor(cryptor);
					assertNotDecryptor(cryptor);
				} else if (keyPair.getPrivateKey() != null) {
					assertNotEncryptor(cryptor);
					assertIsDecryptor(cryptor);
				}

			} else if (asymmetricToken instanceof KeyStoreKeyPair) {
				assertIsEncryptor(cryptor);
				assertIsDecryptor(cryptor);
			} else if (asymmetricToken instanceof SecretKey) {
				assertIsEncryptor(cryptor);
				assertIsDecryptor(cryptor);
			} else if (asymmetricToken instanceof PrivateKey) {
				assertNotEncryptor(cryptor);
				assertIsDecryptor(cryptor);
			} else if (asymmetricToken instanceof PublicKey) {
				assertIsEncryptor(cryptor);
				assertNotDecryptor(cryptor);
			} else if (asymmetricToken instanceof Certificate) {
				assertIsEncryptor(cryptor);
				assertNotDecryptor(cryptor);
			}

		}
		if (cryptoConfiguration instanceof HashingConfiguration) {
			assertIsEncryptor(cryptor);
			assertNotDecryptor(cryptor);
		}

	}

	protected static void assertIsEncryptor(Cryptor cryptor) {
		Assert.assertTrue(cryptor instanceof Encryptor);
		Assert.assertNotNull(cryptor.forEncrypting());
	}

	protected static void assertNotEncryptor(Cryptor cryptor) {
		Assert.assertFalse(cryptor instanceof Encryptor);
		try {
			cryptor.forEncrypting();
		} catch (UnsupportedOperationException e) {
			return;
		}
		Assert.fail("Should have thrown UnsupportedOperationException");
	}

	protected static void assertIsDecryptor(Cryptor cryptor) {
		Assert.assertTrue(cryptor instanceof Decryptor);
		Assert.assertNotNull(cryptor.forDecrypting());
	}

	protected static void assertNotDecryptor(Cryptor cryptor) {
		Assert.assertFalse(cryptor instanceof Decryptor);
		try {
			cryptor.forDecrypting();
		} catch (UnsupportedOperationException e) {
			return;
		}
		Assert.fail("Should have thrown UnsupportedOperationException");
	}

}
