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
package com.braintribe.model.processing.aspect.crypto.test.cryptor.key.generator;

import java.security.KeyPair;

import javax.crypto.SecretKey;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.model.crypto.configuration.encryption.AsymmetricEncryptionConfiguration;
import com.braintribe.model.crypto.configuration.encryption.SymmetricEncryptionConfiguration;
import com.braintribe.model.crypto.key.encoded.KeyEncodingStringFormat;
import com.braintribe.model.crypto.key.encoded.EncodedKeyPair;
import com.braintribe.model.crypto.key.encoded.EncodedPrivateKey;
import com.braintribe.model.crypto.key.encoded.EncodedPublicKey;
import com.braintribe.model.crypto.key.encoded.EncodedSecretKey;
import com.braintribe.model.processing.aspect.crypto.test.TestBase;
import com.braintribe.crypto.Cryptor;
import com.braintribe.model.processing.crypto.factory.CryptorFactoryException;

public class EncodedTokenGenerationTest extends TestBase {

	// ########################
	// ## .. RSA tests ..... ##
	// ########################

	@Test
	public void testRsaPrototypeWithBase64EncodedSpec() throws Exception {
		testAsymmetricTokenGeneration("RSA", KeyEncodingStringFormat.base64, true);
	}

	@Test
	public void testRsaPrototypeWithBase64EncodedSpecPublicKeyOnly() throws Exception {
		testAsymmetricTokenGeneration("RSA", KeyEncodingStringFormat.base64, false);
	}

	@Test
	public void testRsaPrototypeWithHexEncodedSpec() throws Exception {
		testAsymmetricTokenGeneration("RSA", KeyEncodingStringFormat.hex, true);
	}

	@Test
	public void testRsaPrototypeWithHexEncodedSpecPublicKeyOnly() throws Exception {
		testAsymmetricTokenGeneration("RSA", KeyEncodingStringFormat.hex, false);
	}

	@Test(expected = CryptorFactoryException.class)
	public void testNotGeneratedRsaPrototypeWithBase64EncodedSpec() throws Exception {
		testNotGeneratedAsymmetricToken("RSA", KeyEncodingStringFormat.base64, true);
	}

	@Test(expected = CryptorFactoryException.class)
	public void testNotGeneratedRsaPrototypeWithBase64EncodedSpecPublicKeyOnly() throws Exception {
		testNotGeneratedAsymmetricToken("RSA", KeyEncodingStringFormat.base64, false);
	}

	@Test(expected = CryptorFactoryException.class)
	public void testNotGeneratedRsaPrototypeWithHexEncodedSpec() throws Exception {
		testNotGeneratedAsymmetricToken("RSA", KeyEncodingStringFormat.hex, true);
	}

	@Test(expected = CryptorFactoryException.class)
	public void testNotGeneratedRsaPrototypeWithHexEncodedSpecPublicKeyOnly() throws Exception {
		testNotGeneratedAsymmetricToken("RSA", KeyEncodingStringFormat.hex, false);
	}

	// ########################
	// ## .. AES tests ..... ##
	// ########################

	@Test
	public void testAesPrototypeWithBase64EncodedSpec() throws Exception {
		testSymmetricTokenGeneration("AES", KeyEncodingStringFormat.base64);
	}

	@Test
	public void testAesPrototypeWithHexEncodedSpec() throws Exception {
		testSymmetricTokenGeneration("AES", KeyEncodingStringFormat.hex);
	}

	@Test(expected = CryptorFactoryException.class)
	public void testNotGeneratedAesPrototypeWithBase64EncodedSpec() throws Exception {
		testNotGeneratedSymmetricToken("AES", KeyEncodingStringFormat.base64);
	}

	@Test(expected = CryptorFactoryException.class)
	public void testNotGeneratedAesPrototypeWithHexEncodedSpec() throws Exception {
		testNotGeneratedSymmetricToken("AES", KeyEncodingStringFormat.hex);
	}

	// ########################
	// ## .. DES tests ..... ##
	// ########################

	@Test
	public void tesDesPrototypeWithBase64EncodedSpec() throws Exception {
		testSymmetricTokenGeneration("DES", KeyEncodingStringFormat.base64);
	}

	@Test
	public void tesDesPrototypeWithHexEncodedSpec() throws Exception {
		testSymmetricTokenGeneration("DES", KeyEncodingStringFormat.hex);
	}

	@Test(expected = CryptorFactoryException.class)
	public void testNotGeneratedDesPrototypeWithBase64EncodedSpec() throws Exception {
		testNotGeneratedSymmetricToken("DES", KeyEncodingStringFormat.base64);
	}

	@Test(expected = CryptorFactoryException.class)
	public void testNotGeneratedDesPrototypeWithHexEncodedSpec() throws Exception {
		testNotGeneratedSymmetricToken("DES", KeyEncodingStringFormat.hex);
	}

	// ###########################
	// ## .. DESede tests ..... ##
	// ###########################

	@Test
	public void tesDesEdePrototypeWithBase64EncodedSpec() throws Exception {
		testSymmetricTokenGeneration("DESede", KeyEncodingStringFormat.base64);
	}

	@Test
	public void tesDesEdePrototypeWithHexEncodedSpec() throws Exception {
		testSymmetricTokenGeneration("DESede", KeyEncodingStringFormat.hex);
	}

	@Test(expected = CryptorFactoryException.class)
	public void testNotGeneratedDesEdePrototypeWithBase64EncodedSpec() throws Exception {
		testNotGeneratedSymmetricToken("DESede", KeyEncodingStringFormat.base64);
	}

	@Test(expected = CryptorFactoryException.class)
	public void testNotGeneratedDesEdePrototypeWithHexEncodedSpec() throws Exception {
		testNotGeneratedSymmetricToken("DESede", KeyEncodingStringFormat.hex);
	}
	
	// ######################
	// ## .. Commons ..... ##
	// ######################

	protected void testAsymmetricTokenGeneration(String algorithm, KeyEncodingStringFormat encoding, boolean loadPrivateKey) throws Exception {

		AsymmetricEncryptionConfiguration config = createAsymmetricEncryptionConfigurationPrototype(algorithm, encoding, true, loadPrivateKey);

		KeyPair keyPair = encodedKeyPairGenerator.generate((EncodedKeyPair)config.getAsymmetricEncryptionToken(), config.getProvider());
		
		assertEncodedKeyPairGeneration(keyPair, config, loadPrivateKey);

		Cryptor cryptor = cipherCryptorFactory.getCryptor(config);

		testCryptor(cryptor);

	}

	protected void testNotGeneratedAsymmetricToken(String algorithm, KeyEncodingStringFormat encoding, boolean loadPrivateKey) throws Exception {

		AsymmetricEncryptionConfiguration config = createAsymmetricEncryptionConfigurationPrototype(algorithm, encoding, true, loadPrivateKey);

		cipherCryptorFactory.getCryptor(config);

	}

	protected void testSymmetricTokenGeneration(String algorithm, KeyEncodingStringFormat encoding) throws Exception {

		SymmetricEncryptionConfiguration config = createSymmetricEncryptionConfigurationPrototype(algorithm, encoding);

		SecretKey secretkey = encodedSecretKeyGenerator.generate((EncodedSecretKey)config.getSymmetricEncryptionToken(), config.getProvider());

		assertEncodedSecretKeyGeneration(secretkey, config);

		Cryptor cryptor = cipherCryptorFactory.getCryptor(config);

		testCryptor(cryptor);

	}

	protected void testNotGeneratedSymmetricToken(String algorithm, KeyEncodingStringFormat encoding) throws Exception {

		SymmetricEncryptionConfiguration config = createSymmetricEncryptionConfigurationPrototype(algorithm, encoding);

		cipherCryptorFactory.getCryptor(config);

	}

	private static void assertEncodedKeyPairGeneration(KeyPair keyPair, AsymmetricEncryptionConfiguration config, boolean loadedPrivate) {

		Assert.assertNotNull("Key pair should have been returned", keyPair);
		Assert.assertNotNull("Key pair should have a public key", keyPair.getPublic());
		Assert.assertNotNull("Key pair should have a private key", keyPair.getPrivate());

		Assert.assertNotNull("Config should have a token", config.getAsymmetricEncryptionToken());

		Assert.assertTrue("Token should have been of encoded key pair", config.getAsymmetricEncryptionToken() instanceof EncodedKeyPair);

		EncodedKeyPair encodedKeyPair = (EncodedKeyPair)config.getAsymmetricEncryptionToken();
		EncodedPublicKey encodedPublicKey = encodedKeyPair.getPublicKey();
		EncodedPrivateKey encodedPrivateKey = encodedKeyPair.getPrivateKey();

		Assert.assertNotNull("Key pair spec should have contained a key algorithm", encodedKeyPair.getKeyAlgorithm());
		Assert.assertNotNull("Key pair spec should have contained a key size", encodedKeyPair.getKeySize());

		Assert.assertNotNull("Public key spec should have contained a encoded key", encodedPublicKey.getEncodedKey());
		Assert.assertNotNull("Public key spec should have contained a key algorithm", encodedPublicKey.getKeyAlgorithm());
		Assert.assertNotNull("Public key spec should have contained a key format", encodedPublicKey.getEncodingFormat());
		Assert.assertNotNull("Public key spec should have contained a key string format", encodedPublicKey.getEncodingStringFormat());

		if (loadedPrivate) {
			Assert.assertNotNull("Config should have a private key spec", encodedPrivateKey);

			Assert.assertNotNull("Private key spec should have contained a encoded key", encodedPrivateKey.getEncodedKey());
			Assert.assertNotNull("Private key spec should have contained a key algorithm", encodedPrivateKey.getKeyAlgorithm());
			Assert.assertNotNull("Private key spec should have contained a key format", encodedPrivateKey.getEncodingFormat());
			Assert.assertNotNull("Private key spec should have contained a key string format", encodedPrivateKey.getEncodingStringFormat());
		} else {
			Assert.assertNull("Config shouldn't have a private key spec", encodedPrivateKey);
		}

	}

	private static void assertEncodedSecretKeyGeneration(SecretKey secretKey, SymmetricEncryptionConfiguration config) {

		Assert.assertNotNull("Secret key should have been returned", secretKey);

		Assert.assertNotNull("Config should have a token", config.getSymmetricEncryptionToken());

		Assert.assertTrue("Token should have been of secret key type", config.getSymmetricEncryptionToken() instanceof EncodedSecretKey);

		EncodedSecretKey secKeySpec = (EncodedSecretKey) config.getSymmetricEncryptionToken();
		Assert.assertNotNull("Secret key spec should have contained a encoded key", secKeySpec.getEncodedKey());
		Assert.assertNotNull("Secret key spec should have contained a key algorithm", secKeySpec.getKeyAlgorithm());
		Assert.assertNotNull("Secret key spec should have contained a key size", secKeySpec.getKeySize());
		Assert.assertNotNull("Secret key spec should have contained a key format", secKeySpec.getEncodingFormat());
		Assert.assertNotNull("Secret key spec should have contained a key string format", secKeySpec.getEncodingStringFormat());

	}

}
