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

import java.security.KeyPair;

import javax.crypto.SecretKey;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.crypto.cipher.CipherCryptor;
import com.braintribe.model.crypto.configuration.encryption.AsymmetricEncryptionConfiguration;
import com.braintribe.model.crypto.configuration.encryption.EncryptionConfiguration;
import com.braintribe.model.crypto.configuration.encryption.SymmetricEncryptionConfiguration;
import com.braintribe.model.processing.cryptor.basic.cipher.BasicCipherCryptorFactory;

public class CipherCryptorFactoryTest extends CryptorFactoryTestBase {

	@Test
	public void testForAllSupportedConfigurations() throws Exception {
		testForAllSupportedConfigurations(cipherCryptorFactory);
	}
	
	@Test
	public void testForAllSupportedConfigurationsThroughBuilder() throws Exception {
		testForAllSupportedConfigurationsThroughBuilder(cipherCryptorFactory);
	}
	
	@Test
	public void testForAllSupportedConfigurationsThroughBuilderWithJcaToken() throws Exception {
		testForAllSupportedConfigurationsThroughBuilderWithJcaToken(cipherCryptorFactory);
	}

	private static void testForAllSupportedConfigurations(BasicCipherCryptorFactory factory) throws Exception {
		for (EncryptionConfiguration configuration : encryptionConfigurations.values()) {
			CipherCryptor cipherCryptor = factory.getCryptor(configuration);
			Assert.assertNotNull(cipherCryptor);
			assertCryptorType(cipherCryptor, configuration);
			testCryptor(cipherCryptor);
		}
	}

	private static void testForAllSupportedConfigurationsThroughBuilder(BasicCipherCryptorFactory factory) throws Exception {
		for (EncryptionConfiguration configuration : encryptionConfigurations.values()) {
			CipherCryptor cipherCryptor = factory.builder().configuration(configuration).build();
			Assert.assertNotNull(cipherCryptor);
			assertCryptorType(cipherCryptor, configuration);
			testCryptor(cipherCryptor);
		}
	}
	
	private static void testForAllSupportedConfigurationsThroughBuilderWithJcaToken(BasicCipherCryptorFactory factory) throws Exception {
		for (EncryptionConfiguration configuration : encryptionConfigurationsStandard.values()) {

			CipherCryptor cipherCryptorA = null;
			CipherCryptor cipherCryptorB = null;
			
			if (configuration instanceof SymmetricEncryptionConfiguration) {
				SymmetricEncryptionConfiguration config = (SymmetricEncryptionConfiguration)configuration;
				SecretKey secretKey = standardSecretKeyGenerator.generate((com.braintribe.model.crypto.key.SecretKey) config.getSymmetricEncryptionToken(), config.getProvider());
				cipherCryptorA = factory.builder().key(secretKey).mode(config.getMode()).padding(config.getPadding()).provider(config.getProvider()).build();
				cipherCryptorB = factory.builder().key(secretKey).mode(config.getMode()).padding(config.getPadding()).provider(config.getProvider()).build();
			} else {
				AsymmetricEncryptionConfiguration config = (AsymmetricEncryptionConfiguration)configuration;
				KeyPair keyPair = standardKeyPairGenerator.generate((com.braintribe.model.crypto.key.KeyPair) config.getAsymmetricEncryptionToken(), config.getProvider());
				cipherCryptorA = factory.builder().keyPair(keyPair).mode(config.getMode()).padding(config.getPadding()).provider(config.getProvider()).build();
				cipherCryptorB = factory.builder().keyPair(keyPair).mode(config.getMode()).padding(config.getPadding()).provider(config.getProvider()).build();
			}

			Assert.assertNotNull(cipherCryptorA);
			Assert.assertNotNull(cipherCryptorB);
			assertCryptorType(cipherCryptorA, configuration);
			assertCryptorType(cipherCryptorB, configuration);

			testCryptors(cipherCryptorA, cipherCryptorB);
			
		}
	}

}
