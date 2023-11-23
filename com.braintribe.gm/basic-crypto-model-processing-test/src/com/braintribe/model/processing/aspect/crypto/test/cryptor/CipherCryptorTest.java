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
package com.braintribe.model.processing.aspect.crypto.test.cryptor;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.crypto.Cryptor;
import com.braintribe.model.crypto.configuration.encryption.EncryptionConfiguration;
import com.braintribe.model.processing.aspect.crypto.test.TestBase;
import com.braintribe.model.processing.aspect.crypto.test.commons.TestDataProvider;
import com.braintribe.model.processing.cryptor.basic.cipher.BasicCipherCryptorFactory;

public class CipherCryptorTest extends TestBase {

	// ############################
	// ## .. General tests ..... ##
	// ############################

	@Test
	public void testStreamedCryptingForAllSupportedConfigurations() throws Exception {
		testStreamedCryptingForAllSupportedConfigurations(cipherCryptorFactory);
	}

	@Test
	public void testBytesCryptingForAllSupportedConfigurations() throws Exception {
		testBytesCryptingForAllSupportedConfigurations(cipherCryptorFactory);
	}

	@Test
	public void testStringCryptingForAllSupportedConfigurationsUsingBase64() throws Exception {
		testStringCryptingForAllSupportedConfigurations(cipherCryptorFactory, Cryptor.Encoding.base64);
	}

	@Test
	public void testStringCryptingForAllSupportedConfigurationsUsingHex() throws Exception {
		testStringCryptingForAllSupportedConfigurations(cipherCryptorFactory, Cryptor.Encoding.hex);
	}

	@Test
	public void testNulllCryptingForAllSupportedConfigurations() throws Exception {
		for (EncryptionConfiguration configuration : encryptionConfigurations.values()) {
			try {
				testNullCrypting(cipherCryptorFactory.getCryptor(configuration));
				Assert.fail("IllegalArgumentException should have been thrown for the null input");
			} catch (IllegalArgumentException expected) {
				// no action
			}
		}
	}

	protected static void testStreamedCryptingForAllSupportedConfigurations(BasicCipherCryptorFactory factory) throws Exception {
		for (EncryptionConfiguration configuration : encryptionConfigurations.values()) {
			testStreamedCrypting(factory.getCryptor(configuration), configuration.getAlgorithm(), false, TestDataProvider.inputA);
		}
	}

	protected static void testBytesCryptingForAllSupportedConfigurations(BasicCipherCryptorFactory factory) throws Exception {
		for (EncryptionConfiguration configuration : encryptionConfigurations.values()) {
			testBytesCrypting(factory.getCryptor(configuration), configuration.getAlgorithm(), false, TestDataProvider.inputA);
		}
	}

	protected static void testStringCryptingForAllSupportedConfigurations(BasicCipherCryptorFactory factory, Cryptor.Encoding stringEncoding) throws Exception {
		for (EncryptionConfiguration configuration : encryptionConfigurations.values()) {
			testStringCrypting(factory.getCryptor(configuration), configuration.getAlgorithm(), stringEncoding, false, TestDataProvider.inputAString);
		}
	}
}
