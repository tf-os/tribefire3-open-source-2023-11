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

import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.crypto.Cryptor;
import com.braintribe.crypto.Decryptor;
import com.braintribe.crypto.Encryptor;
import com.braintribe.model.crypto.configuration.encryption.EncryptionConfiguration;
import com.braintribe.model.processing.aspect.crypto.test.TestBase;

public class CipherCryptorProfilingTest extends TestBase {

	private static final byte[] symmetricEncryptionTestData = generateTestData(1024 * 256);
	private static final byte[] asymmetricEncryptionTestData = generateTestData(244);

	@Test
	public void testAesEncryption() throws Exception {
		EncryptionConfiguration config = getEncryptionConfiguration("AES", "hex");
		testStressedRoundtrip(config, symmetricEncryptionTestData, 50, false, true);
	}

	@Test
	public void testAesStreamedEncryption() throws Exception {
		EncryptionConfiguration config = getEncryptionConfiguration("AES", "hex");
		testStressedRoundtrip(config, symmetricEncryptionTestData, 50, true, true);
	}

	@Test
	public void testDesEncryption() throws Exception {
		EncryptionConfiguration config = getEncryptionConfiguration("DES", "hex");
		testStressedRoundtrip(config, symmetricEncryptionTestData, 50, false, true);
	}

	@Test
	public void testDesStreamedEncryption() throws Exception {
		EncryptionConfiguration config = getEncryptionConfiguration("DES", "hex");
		testStressedRoundtrip(config, symmetricEncryptionTestData, 50, true, true);
	}

	@Test
	public void testDesEdeEncryption() throws Exception {
		EncryptionConfiguration config = getEncryptionConfiguration("DESede", "hex");
		testStressedRoundtrip(config, symmetricEncryptionTestData, 50, false, true);
	}

	@Test
	public void testDesEdeStreamedEncryption() throws Exception {
		EncryptionConfiguration config = getEncryptionConfiguration("DESede", "hex");
		testStressedRoundtrip(config, symmetricEncryptionTestData, 50, true, true);
	}

	@Test
	public void testRsaEncryption() throws Exception {
		EncryptionConfiguration config = getEncryptionConfiguration("RSA", "hex");
		testStressedRoundtrip(config, asymmetricEncryptionTestData, 50, false, true);
	}

	@Test
	public void testRsaStreamedEncryption() throws Exception {
		EncryptionConfiguration config = getEncryptionConfiguration("RSA", "hex");
		testStressedRoundtrip(config, asymmetricEncryptionTestData, 50, true, true);
	}

	protected void testStressedRoundtrip(EncryptionConfiguration config, byte[] testData, int runs, boolean streaming, boolean assertData) throws Exception {

		Cryptor cipherCryptor = cipherCryptorFactory.getCryptor(config);

		long n = System.currentTimeMillis();
		for (int i = runs; i > 0; i--) {
			if (streaming) {
				byte[] encryptedData = encryptStreaming((Encryptor) cipherCryptor, testData);
				byte[] decryptedData = decryptStreaming((Decryptor) cipherCryptor, encryptedData);
				if (assertData && (i == runs || i == 1)) {
					Assert.assertArrayEquals("Decryption unexpected for " + config.getAlgorithm(), testData, decryptedData);
				}
			} else {
				byte[] encryptedData = ((Encryptor) cipherCryptor).encrypt(testData).result().asBytes();
				byte[] decryptedData = ((Decryptor) cipherCryptor).decrypt(encryptedData).result().asBytes();
				if (assertData && (i == runs || i == 1)) {
					Assert.assertArrayEquals("Decryption unexpected for " + config.getAlgorithm(), testData, decryptedData);
				}
			}
		}

		n = System.currentTimeMillis() - n;
		System.out.println("Ran " + runs + " roundtrips of " + config.getAlgorithm() + " encryption of " + testData.length + " bytes in " + n + " ms" + (streaming ? ", using streaming" : ""));

	}

	protected static byte[] generateTestData(int size) {
		byte[] testData = new byte[size];
		new Random().nextBytes(testData);
		return testData;
	}

}
