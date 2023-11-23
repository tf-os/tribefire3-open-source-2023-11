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

import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.braintribe.crypto.Cryptor;
import com.braintribe.crypto.Cryptor.Encoding;
import com.braintribe.crypto.CryptorException;
import com.braintribe.crypto.hash.Hasher;
import com.braintribe.model.crypto.configuration.hashing.HashingConfiguration;
import com.braintribe.model.processing.aspect.crypto.test.TestBase;
import com.braintribe.model.processing.aspect.crypto.test.commons.TestDataProvider;
import com.braintribe.model.processing.cryptor.basic.hash.BasicHasherFactory;
import com.braintribe.utils.IOTools;

public class HasherTest extends TestBase {

	@Test
	public void testLab2() throws Exception {

		HashingConfiguration config = HashingConfiguration.T.create();
		config.setAlgorithm("SHA-256");
		config.setRandomSaltSize(16);

		Hasher hasher = hasherFactory.getCryptor(config);

		String encrypted1 = hasher.encrypt("cortex").result().asString();
		String encrypted2 = hasher.encrypt("cortex").result().asString();

		System.out.println(encrypted1);
		System.out.println(encrypted2);

		boolean r1 = hasher.is("cortex").equals(encrypted1);
		boolean r2 = hasher.is("cortex").equals(encrypted2);

		System.out.println("Result is : " + r1);
		System.out.println("Result is : " + r2);

	}

	@Test
	public void testLab() throws Exception {

		HashingConfiguration config = HashingConfiguration.T.create();
		config.setAlgorithm("SHA-256");
		config.setRandomSaltSize(16);

		Hasher hasher = hasherFactory.getCryptor(config);

		String encrypted1 = hasher.encrypt("cortex").result().asString();
		String encrypted2 = hasher.encrypt("cortex").result().asString();

		System.out.println(encrypted1);
		System.out.println(encrypted2);

		boolean r1 = hasher.is("cortex").equals(encrypted1);
		boolean r2 = hasher.is("cortex").equals(encrypted2);

		System.out.println("Result is : " + r1);
		System.out.println("Result is : " + r2);

	}

	// ########################
	// ## .. MD5 tests ..... ##
	// ########################

	// without salt

	@Test
	public void testMd5HashOnHexadecimalString() throws Exception {
		HashingConfiguration config = getHashingConfiguration("MD5", false);
		Cryptor cryptor = hasherFactory.newCryptor(config);
		testStringCrypting(cryptor, "MD5", Cryptor.Encoding.hex, false, TestDataProvider.inputAString);
	}

	@Test
	public void testMd5HashOnBase64String() throws Exception {
		HashingConfiguration config = getHashingConfiguration("MD5", false);
		Cryptor cryptor = hasherFactory.newCryptor(config);
		testStringCrypting(cryptor, "MD5", Cryptor.Encoding.base64, false, TestDataProvider.inputAString);
	}

	@Test
	public void testMd5HashOnByteArray() throws Exception {
		HashingConfiguration config = getHashingConfiguration("MD5", false);
		Cryptor cryptor = hasherFactory.newCryptor(config);
		testBytesCrypting(cryptor, "MD5", false, TestDataProvider.inputA);
	}

	@Test
	public void testMd5HashStreaming() throws Exception {
		HashingConfiguration config = getHashingConfiguration("MD5", false);
		Cryptor cryptor = hasherFactory.newCryptor(config);
		testStreamedCrypting(cryptor, "MD5", false, TestDataProvider.inputA);
	}

	// ##########################
	// ## .. SHA-1 tests ..... ##
	// ##########################

	// without salt

	@Test
	public void testSha1HashOnHexadecimalString() throws Exception {
		HashingConfiguration config = getHashingConfiguration("SHA-1", false);
		Cryptor cryptor = hasherFactory.newCryptor(config);
		testStringCrypting(cryptor, "SHA-1", Cryptor.Encoding.hex, false, TestDataProvider.inputAString);
	}

	@Test
	public void testSha1HashOnBase64String() throws Exception {
		HashingConfiguration config = getHashingConfiguration("SHA-1", false);
		Cryptor cryptor = hasherFactory.newCryptor(config);
		testStringCrypting(cryptor, "SHA-1", Cryptor.Encoding.base64, false, TestDataProvider.inputAString);
	}

	@Test
	public void testSha1HashOnByteArray() throws Exception {
		HashingConfiguration config = getHashingConfiguration("SHA-1", false);
		Cryptor cryptor = hasherFactory.newCryptor(config);
		testBytesCrypting(cryptor, "SHA-1", false, TestDataProvider.inputA);
	}

	@Test
	public void testSha1HashStreaming() throws Exception {
		HashingConfiguration config = getHashingConfiguration("SHA-1", false);
		Cryptor cryptor = hasherFactory.newCryptor(config);
		testStreamedCrypting(cryptor, "SHA-1", false, TestDataProvider.inputA);
	}

	// ##########################
	// ## .. SHA-256 tests ... ##
	// ##########################

	// without salt

	@Test
	public void testSha256HashOnHexadecimalString() throws Exception {
		HashingConfiguration config = getHashingConfiguration("SHA-256", false);
		Cryptor cryptor = hasherFactory.newCryptor(config);
		testStringCrypting(cryptor, "SHA-256", Cryptor.Encoding.hex, false, TestDataProvider.inputAString);
	}

	@Test
	public void testSha256HashOnBase64String() throws Exception {
		HashingConfiguration config = getHashingConfiguration("SHA-256", false);
		Cryptor cryptor = hasherFactory.newCryptor(config);
		testStringCrypting(cryptor, "SHA-256", Cryptor.Encoding.base64, false, TestDataProvider.inputAString);
	}

	@Test
	public void testSha256HashOnByteArray() throws Exception {
		HashingConfiguration config = getHashingConfiguration("SHA-256", false);
		Cryptor cryptor = hasherFactory.newCryptor(config);
		testBytesCrypting(cryptor, "SHA-256", false, TestDataProvider.inputA);
	}

	@Test
	public void testSha256HashStreaming() throws Exception {
		HashingConfiguration config = getHashingConfiguration("SHA-256", false);
		Cryptor cryptor = hasherFactory.newCryptor(config);
		testStreamedCrypting(cryptor, "SHA-256", false, TestDataProvider.inputA);
	}

	// ############################
	// ## .. General tests ..... ##
	// ############################

	@Test
	public void testStreamedCryptingForAllSupportedConfigurations() throws Exception {
		testStreamedCryptingForAllSupportedConfigurations(hasherFactory);
	}

	@Test
	public void testBytesCryptingForAllSupportedConfigurations() throws Exception {
		testBytesCryptingForAllSupportedConfigurations(hasherFactory);
	}

	@Test
	public void testStringCryptingForAllSupportedConfigurationsUsingBase64() throws Exception {
		testStringCryptingForAllSupportedConfigurations(hasherFactory, Cryptor.Encoding.base64);
	}

	@Test
	public void testStringCryptingForAllSupportedConfigurationsUsingHex() throws Exception {
		testStringCryptingForAllSupportedConfigurations(hasherFactory, Cryptor.Encoding.hex);
	}

	@Test
	public void testNulllCryptingForAllSupportedConfigurations() throws Exception {
		for (HashingConfiguration configuration : hashingConfigurations.values()) {
			try {
				testNullCrypting(hasherFactory.newCryptor(configuration));
				Assert.fail("IllegalArgumentException should have been thrown for the null input");
			} catch (IllegalArgumentException expected) {
				// no action
			}
		}
	}

	@Test(expected = CryptorException.class)
	public void testUnsupportedConfiguration() throws Exception {
		HashingConfiguration config = createHashingConfiguration("MD4", false);
		Hasher hasher = hasherFactory.newCryptor(config);
		hasher.encrypt(TestDataProvider.inputAString).result();
	}

	protected static void testStreamedCryptingForAllSupportedConfigurations(BasicHasherFactory factory) throws Exception {
		for (HashingConfiguration configuration : hashingConfigurations.values()) {
			if (configuration.getPublicSalt() == null)
				testStreamedCrypting(factory.newCryptor(configuration), configuration.getAlgorithm(), false, TestDataProvider.inputA);
		}
	}

	protected static void testBytesCryptingForAllSupportedConfigurations(BasicHasherFactory factory) throws Exception {
		for (HashingConfiguration configuration : hashingConfigurations.values()) {
			if (configuration.getPublicSalt() == null)
				testBytesCrypting(factory.newCryptor(configuration), configuration.getAlgorithm(), false, TestDataProvider.inputA);
		}
	}

	protected static void testStringCryptingForAllSupportedConfigurations(BasicHasherFactory factory, Cryptor.Encoding stringEncoding) throws Exception {
		for (HashingConfiguration configuration : hashingConfigurations.values()) {
			if (configuration.getPublicSalt() == null)
				testStringCrypting(factory.newCryptor(configuration), configuration.getAlgorithm(), stringEncoding, false, TestDataProvider.inputAString);
		}
	}

	// #################################
	// ## .. Fingerprinting tests ... ##
	// #################################

	@Ignore
	@Test
	public void testFingerprintingFromFile() throws Exception {

		String path = "YOUR_PATH_HERE/public-RSA-2048.key";

		String expectedFingerprint = "66:0a:82:03:36:80:d5:e7:63:c6:d4:8f:1f:8c:bf:90:f9:0b:3b:c6:2a:c3:18:f6:ba:ed:0b:f7:5d:60:37:e1";

		testFingerprintingFromFile(path, "SHA-256", Encoding.hex, expectedFingerprint);

	}

	@Test
	public void testFingerprintingFromBytes() throws Exception {

		byte[] keyMaterial = { 48, -126, 1, 34, 48, 13, 6, 9, 42, -122, 72, -122, -9, 13, 1, 1, 1, 5, 0, 3, -126, 1, 15, 0, 48, -126, 1, 10, 2, -126, 1, 1, 0, -37, -7, 22, 126, 5, 114, 56, -113, 30, -72, -23, -39, -86, -13, 119, 1, 81, -115, -3, -92, 42, -38, 114, 55, -91, 101, 80, -39, -60, -33, 20, 7, 27, -98, -58, 101, -105, -104, 37, 8, 89, 11, -24, 61, -8, 54, -65, -70, 80, 126, -100, 87,
				-53, 69, 28, 88, -50, 52, -110, -54, -126, 25, -50, -97, 38, 54, 62, 124, 80, 99, 26, 101, -52, -120, 28, -88, -80, -11, 28, 116, -122, 47, -123, 73, 27, 32, -51, 78, 40, -121, 58, -42, -128, -44, -49, 1, 53, -41, 99, 47, -70, 101, -125, -13, 47, -103, 68, 120, -72, 107, 45, -29, 111, 15, 39, -97, -14, -44, 3, -7, 118, -92, -26, 48, -99, -59, -121, -79, 45, 123, -107, -72, -79,
				-18, -101, -7, -128, 52, -9, -98, -104, -65, -107, 22, 103, 58, 22, 101, -90, 84, -103, -14, 81, -43, 78, 38, -17, -90, 54, 28, 38, -117, 114, -79, -78, -90, 76, 97, -128, 50, 80, 6, 123, 53, -89, 78, -42, -115, 17, 123, 64, -62, -40, 81, 3, -124, -41, -90, 98, 80, 112, -121, -16, 103, -79, 45, 71, -103, -57, -33, -121, -71, 33, 65, 28, -2, -90, 116, 11, -28, -26, -44, 31, -101,
				41, -102, 105, -12, 0, 51, 2, -1, 28, 36, -73, -56, 51, 23, -125, -53, -49, 109, 55, 111, 16, 45, 85, 126, -94, -89, -73, -79, -68, 118, -70, 36, 25, 70, 61, -101, -104, -24, -53, -29, -93, 73, 2, 3, 1, 0, 1 };

		String expectedFingerprint = "66:0a:82:03:36:80:d5:e7:63:c6:d4:8f:1f:8c:bf:90:f9:0b:3b:c6:2a:c3:18:f6:ba:ed:0b:f7:5d:60:37:e1";

		testFingerprinting(keyMaterial, "SHA-256", Encoding.hex, expectedFingerprint);

	}

	protected static void testFingerprintingFromFile(String keyMaterialPath, String hashingConfig, Encoding fingerprintEncoding, String expectedFingerprint) throws Exception {

		byte[] keyMaterial = IOTools.slurpBytes(Files.newInputStream(Paths.get(keyMaterialPath)));

		testFingerprinting(keyMaterial, hashingConfig, fingerprintEncoding, expectedFingerprint);

	}

	protected static void testFingerprinting(byte[] keyMaterial, String hashingConfig, Encoding fingerprintEncoding, String expectedFinterprint) throws Exception {

		Hasher hasher = hasherFactory.getCryptor(getHashingConfiguration(hashingConfig, false));

		String fingerprint = hasher.encrypt(keyMaterial).result().asString(fingerprintEncoding);

		// Apply the colon notation
		String colonNotationFingerprint = fingerprint.replaceAll("(?<=..)(..)", ":$1");
		
		if (expectedFinterprint != null) {
			Assert.assertEquals("Fingerprints didn't match", expectedFinterprint, colonNotationFingerprint);
		} else  {
			System.out.println("Generated fingerprint is : " + colonNotationFingerprint);
		}

	}

}
