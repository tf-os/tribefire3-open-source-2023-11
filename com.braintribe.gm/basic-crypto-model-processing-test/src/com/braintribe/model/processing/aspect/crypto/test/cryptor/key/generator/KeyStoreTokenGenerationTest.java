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
import java.security.cert.Certificate;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.braintribe.crypto.Cryptor;
import com.braintribe.model.crypto.configuration.encryption.AsymmetricEncryptionConfiguration;
import com.braintribe.model.crypto.configuration.encryption.SymmetricEncryptionConfiguration;
import com.braintribe.model.crypto.key.keystore.HasKeyStoreEntry;
import com.braintribe.model.crypto.key.keystore.KeyStore;
import com.braintribe.model.crypto.key.keystore.KeyStoreCertificate;
import com.braintribe.model.crypto.key.keystore.KeyStoreKeyPair;
import com.braintribe.model.crypto.key.keystore.KeyStoreSecretKey;
import com.braintribe.model.crypto.token.AsymmetricEncryptionToken;
import com.braintribe.model.processing.aspect.crypto.test.TestBase;
import com.braintribe.model.processing.crypto.factory.CryptorFactoryException;
import com.braintribe.model.processing.crypto.token.generator.EncryptionTokenGeneratorException;

public class KeyStoreTokenGenerationTest extends TestBase {

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	// ########################
	// ## .. RSA tests ..... ##
	// ########################

	@Test
	public void testRsaPrototypeToJks() throws Exception {
		testAsymmetricTokenGeneration("jks", "RSA", true, true, true);
	}
	
	@Test(expected = EncryptionTokenGeneratorException.class)
	public void testRsaPrototypeToJksWithNoEntryPassword() throws Exception {
		testAsymmetricTokenGeneration("jks", "RSA", true, false, true);
	}
	
	@Test(expected = EncryptionTokenGeneratorException.class)
	public void testRsaPrototypeToJksWithNoKeyStorePassword() throws Exception {
		testAsymmetricTokenGeneration("jks", "RSA", false, true, true);
	}
	
	@Test(expected = EncryptionTokenGeneratorException.class)
	public void testRsaPrototypeToJksWithNoPasswords() throws Exception {
		testAsymmetricTokenGeneration("jks", "RSA", false, false, true);
	}
	
	@Test
	public void testRsaPrototypeToJksPublicKeyOnly() throws Exception {
		testAsymmetricTokenGeneration("jks", "RSA", true, true, false);
	}
	
	@Test(expected = EncryptionTokenGeneratorException.class)
	public void testRsaPrototypeToJksWithNoEntryPasswordPublicKeyOnly() throws Exception {
		testAsymmetricTokenGeneration("jks", "RSA", true, false, false);
	}
	
	@Test(expected = EncryptionTokenGeneratorException.class)
	public void testRsaPrototypeToJksWithNoKeyStorePasswordPublicKeyOnly() throws Exception {
		testAsymmetricTokenGeneration("jks", "RSA", false, true, false);
	}
	
	@Test(expected = EncryptionTokenGeneratorException.class)
	public void testRsaPrototypeToJksWithNoPasswordsPublicKeyOnly() throws Exception {
		testAsymmetricTokenGeneration("jks", "RSA", false, false, false);
	}

	// ########################
	// ## .. AES tests ..... ##
	// ########################

	@Test
	public void testAesPrototypeToJceks() throws Exception {
		testSymmetricTokenGeneration("jceks", "AES", true, true);
	}

	@Test
	public void testAesPrototypeToJceksWithNoEntryPassword() throws Exception {
		testSymmetricTokenGeneration("jceks", "AES", true, false);
	}

	@Test
	public void testAesPrototypeToJceksWithNoKeyStorePassword() throws Exception {
		testSymmetricTokenGeneration("jceks", "AES", false, true);
	}

	@Test
	public void testAesPrototypeToJceksWithNoPasswords() throws Exception {
		testSymmetricTokenGeneration("jceks", "AES", false, false);
	}

	@Test(expected = CryptorFactoryException.class)
	public void testNotGeneratedAesPrototypeToJceks() throws Exception {
		testNotGeneratedSymmetricToken("jceks", "AES", true, true);
	}

	@Test(expected = CryptorFactoryException.class)
	public void testNotGeneratedAesPrototypeToJceksWithNoEntryPassword() throws Exception {
		testNotGeneratedSymmetricToken("jceks", "AES", true, false);
	}

	@Test(expected = CryptorFactoryException.class)
	public void testNotGeneratedAesPrototypeToJceksWithNoKeyStorePassword() throws Exception {
		testNotGeneratedSymmetricToken("jceks", "AES", false, true);
	}

	@Test(expected = CryptorFactoryException.class)
	public void testNotGeneratedAesPrototypeToJceksWithNoPasswords() throws Exception {
		testNotGeneratedSymmetricToken("jceks", "AES", false, false);
	}

	// ########################
	// ## .. DES tests ..... ##
	// ########################

	@Test
	public void testDesPrototypeToJceks() throws Exception {
		testSymmetricTokenGeneration("jceks", "DES", true, true);
	}

	@Test
	public void testDesPrototypeToJceksWithNoEntryPassword() throws Exception {
		testSymmetricTokenGeneration("jceks", "DES", true, false);
	}

	@Test
	public void testDesPrototypeToJceksWithNoKeyStorePassword() throws Exception {
		testSymmetricTokenGeneration("jceks", "DES", false, true);
	}

	@Test
	public void testDesPrototypeToJceksWithNoPasswords() throws Exception {
		testSymmetricTokenGeneration("jceks", "DES", false, false);
	}

	@Test(expected = CryptorFactoryException.class)
	public void testNotGeneratedDesPrototypeToJceks() throws Exception {
		testNotGeneratedSymmetricToken("jceks", "DES", true, true);
	}

	@Test(expected = CryptorFactoryException.class)
	public void testNotGeneratedDesPrototypeToJceksWithNoEntryPassword() throws Exception {
		testNotGeneratedSymmetricToken("jceks", "DES", true, false);
	}

	@Test(expected = CryptorFactoryException.class)
	public void testNotGeneratedDesPrototypeToJceksWithNoKeyStorePassword() throws Exception {
		testNotGeneratedSymmetricToken("jceks", "DES", false, true);
	}

	@Test(expected = CryptorFactoryException.class)
	public void testNotGeneratedDesPrototypeToJceksWithNoPasswords() throws Exception {
		testNotGeneratedSymmetricToken("jceks", "DES", false, false);
	}

	// ###########################
	// ## .. DESede tests ..... ##
	// ###########################

	@Test
	public void testDesEdePrototypeToJceks() throws Exception {
		testSymmetricTokenGeneration("jceks", "DESede", true, true);
	}

	@Test
	public void testDesEdePrototypeToJceksWithNoEntryPassword() throws Exception {
		testSymmetricTokenGeneration("jceks", "DESede", true, false);
	}

	@Test
	public void testDesEdePrototypeToJceksWithNoKeyStorePassword() throws Exception {
		testSymmetricTokenGeneration("jceks", "DESede", false, true);
	}

	@Test
	public void testDesEdePrototypeToJceksWithNoPasswords() throws Exception {
		testSymmetricTokenGeneration("jceks", "DESede", false, false);
	}

	@Test(expected = CryptorFactoryException.class)
	public void testNotGeneratedDesEdePrototypeToJceks() throws Exception {
		testNotGeneratedSymmetricToken("jceks", "DESede", true, true);
	}

	@Test(expected = CryptorFactoryException.class)
	public void testNotGeneratedDesEdePrototypeToJceksWithNoEntryPassword() throws Exception {
		testNotGeneratedSymmetricToken("jceks", "DESede", true, false);
	}

	@Test(expected = CryptorFactoryException.class)
	public void testNotGeneratedDesEdePrototypeToJceksWithNoKeyStorePassword() throws Exception {
		testNotGeneratedSymmetricToken("jceks", "DESede", false, true);
	}

	@Test(expected = CryptorFactoryException.class)
	public void testNotGeneratedDesEdePrototypeToJceksWithNoPasswords() throws Exception {
		testNotGeneratedSymmetricToken("jceks", "DESede", false, false);
	}

	// ######################
	// ## .. Commons ..... ##
	// ######################

	protected void testAsymmetricTokenGeneration(String keyStoreType, String algorithm, boolean addPassword, boolean addEntryPassword, boolean addPrivateKey) throws Exception {

		AsymmetricEncryptionConfiguration config = createAsymmetricEncryptionConfigurationPrototype(keyStoreType, algorithm, addPassword, addEntryPassword, addPrivateKey);
		
		if (config.getAsymmetricEncryptionToken() instanceof KeyStoreKeyPair) {
			
			KeyStoreKeyPair keyStoreKeyPair = (KeyStoreKeyPair)config.getAsymmetricEncryptionToken();

			KeyPair keyPair = keyStoreKeyPairGenerator.generate((KeyStoreKeyPair)config.getAsymmetricEncryptionToken(), config.getProvider());

			assertKeyStoreKeyPairGeneration(keyPair, keyStoreKeyPair, addPassword, addEntryPassword);

		} else if (config.getAsymmetricEncryptionToken() instanceof KeyStoreCertificate) {
			
			KeyStoreCertificate keyStoreCertificate = (KeyStoreCertificate)config.getAsymmetricEncryptionToken();

			Certificate certificate = keyStoreCertificateGenerator.generate(keyStoreCertificate, config.getProvider());

			assertKeyStoreCertificateGeneration(certificate, keyStoreCertificate, addPassword, addEntryPassword);

		} else {
			throw new RuntimeException("Unexpected type of "+AsymmetricEncryptionToken.class+": "+config.getAsymmetricEncryptionToken());
		}
		
		Cryptor cryptor = cipherCryptorFactory.getCryptor(config);

		testCryptor(cryptor);

	}

	protected void testNotGeneratedAsymmetricToken(String keyStoreType, String algorithm, boolean addPassword, boolean addEntryPassword, boolean addPrivateKey) throws Exception {

		AsymmetricEncryptionConfiguration config = createAsymmetricEncryptionConfigurationPrototype(keyStoreType, algorithm, addPassword, addEntryPassword, addPrivateKey);

		cipherCryptorFactory.getCryptor(config);

	}

	protected void testSymmetricTokenGeneration(String keyStoreType, String algorithm, boolean addPassword, boolean addEntryPassword) throws Exception {

		SymmetricEncryptionConfiguration config = createSymmetricEncryptionConfigurationPrototype(keyStoreType, algorithm, addPassword, addEntryPassword);
		
		KeyStoreSecretKey keyStoreSecretKey = (KeyStoreSecretKey)config.getSymmetricEncryptionToken();

		SecretKey secretkey = keyStoreSecretKeyGenerator.generate((KeyStoreSecretKey)config.getSymmetricEncryptionToken(), config.getProvider());

		assertKeyStoreSecretKeyGeneration(secretkey, keyStoreSecretKey, addPassword, addEntryPassword);

		Cryptor cryptor = cipherCryptorFactory.getCryptor(config);

		testCryptor(cryptor);

	}

	protected void testNotGeneratedSymmetricToken(String keyStoreType, String algorithm, boolean addPassword, boolean addEntryPassword) throws Exception {

		SymmetricEncryptionConfiguration config = createSymmetricEncryptionConfigurationPrototype(keyStoreType, algorithm, addPassword, addEntryPassword);

		cipherCryptorFactory.getCryptor(config);

	}

	private AsymmetricEncryptionConfiguration createAsymmetricEncryptionConfigurationPrototype(String keyStoreType, String algorithm, boolean addPassword, boolean addEntryPassword, boolean addPrivateKey) throws Exception {

		String id = "asymmetric-" + UUID.randomUUID().toString();

		KeyStore keyStoreDef = KeyStore.T.create();
		keyStoreDef.setFilePath(tempFolder.getRoot().getAbsolutePath() + "/" + id + "." + keyStoreType);
		keyStoreDef.setType(keyStoreType);

		if (addPassword) {
			keyStoreDef.setPassword(id + "-password");
		}
		
		if (addPrivateKey) {
			
			KeyStoreKeyPair keyPair = KeyStoreKeyPair.T.create();
			keyPair.setKeyAlgorithm(algorithm);
			keyPair.setKeyEntryAlias(id + "-pair-entry");
			if (addEntryPassword) {
				keyPair.setKeyEntryPassword(id + "-pair-entry-password");
			}
			keyPair.setKeyStore(keyStoreDef);

			return createAsymmetricEncryptionConfigurationPrototype(algorithm, keyPair);
			
		} else {
			
			KeyStoreCertificate certificate = KeyStoreCertificate.T.create();
			certificate.setKeyAlgorithm(algorithm);
			certificate.setKeyEntryAlias(id + "-cert-entry");
			if (addEntryPassword) {
				certificate.setKeyEntryPassword(id + "-cert-entry-password");
			}
			certificate.setKeyStore(keyStoreDef);
			
			return createAsymmetricEncryptionConfigurationPrototype(algorithm, certificate);
			
		}
		
	}

	private SymmetricEncryptionConfiguration createSymmetricEncryptionConfigurationPrototype(String keyStoreType, String algorithm, boolean addPassword, boolean addEntryPassword) throws Exception {

		String id = "symmetric-" + UUID.randomUUID().toString();

		KeyStore keyStoreDef = KeyStore.T.create();
		keyStoreDef.setFilePath(tempFolder.getRoot().getAbsolutePath() + "/" + id + "." + keyStoreType);
		keyStoreDef.setType(keyStoreType);

		if (addPassword) {
			keyStoreDef.setPassword(id + "-password");
		}

		KeyStoreSecretKey keyStorageKeySpec = KeyStoreSecretKey.T.create();

		keyStorageKeySpec.setKeyAlgorithm(algorithm);
		keyStorageKeySpec.setKeyEntryAlias(id + "-secret-key-entry");
		if (addEntryPassword) {
			keyStorageKeySpec.setKeyEntryPassword(id + "-secret-key-entry-password");
		}
		keyStorageKeySpec.setKeyStore(keyStoreDef);

		return createSymmetricEncryptionConfigurationPrototype(algorithm, keyStorageKeySpec);
	}

	private static void assertKeyStoreKeyPairGeneration(KeyPair keyPair, KeyStoreKeyPair keyStoreKeyPair, boolean addPassword, boolean addEntryPassword) {
		
		Assert.assertNotNull(KeyPair.class.getName()+" should have been returned", keyPair);
		Assert.assertNotNull(KeyPair.class.getName()+" should have been returned with a public key", keyPair.getPublic());
		Assert.assertNotNull(KeyPair.class.getName()+" should have been returned with a private key", keyPair.getPrivate());

		Assert.assertNotNull("Key store entry shouldn't be null", keyStoreKeyPair);
		Assert.assertNotNull(keyStoreKeyPair.getClass().getSimpleName()+" should have contained a key algorithm", keyStoreKeyPair.getKeyAlgorithm());
		Assert.assertNotNull(keyStoreKeyPair.getClass().getSimpleName()+" should have contained a key size", keyStoreKeyPair.getKeySize());
		
		assertKeyStoreEntryGeneration(keyStoreKeyPair, addPassword, addEntryPassword);

	}

	private static void assertKeyStoreCertificateGeneration(Certificate certificate, KeyStoreCertificate keyStoreCertificate, boolean addPassword, boolean addEntryPassword) {

		Assert.assertNotNull(Certificate.class.getName()+" should have been returned", certificate);
		Assert.assertNotNull(Certificate.class.getName()+" should have been returned with a public key", certificate.getPublicKey());

		Assert.assertNotNull("Key store entry shouldn't be null", keyStoreCertificate);
		Assert.assertNotNull(keyStoreCertificate.getClass().getSimpleName()+" should have contained a key algorithm", keyStoreCertificate.getKeyAlgorithm());
		Assert.assertNotNull(keyStoreCertificate.getClass().getSimpleName()+" should have contained a key size", keyStoreCertificate.getKeySize());

		assertKeyStoreEntryGeneration(keyStoreCertificate, addPassword, addEntryPassword);

	}

	private static void assertKeyStoreSecretKeyGeneration(SecretKey secretKey, KeyStoreSecretKey keyStoreSecretKey, boolean addPassword, boolean addEntryPassword) {
		
		Assert.assertNotNull(SecretKey.class.getName()+" should have been returned", secretKey);

		Assert.assertNotNull("Key store entry shouldn't be null", keyStoreSecretKey);
		Assert.assertNotNull(keyStoreSecretKey.getClass().getSimpleName()+" should have contained a key algorithm", keyStoreSecretKey.getKeyAlgorithm());
		Assert.assertNotNull(keyStoreSecretKey.getClass().getSimpleName()+" should have contained a key size", keyStoreSecretKey.getKeySize());

		assertKeyStoreEntryGeneration(keyStoreSecretKey, addPassword, addEntryPassword);
		
	}
	
	private static void assertKeyStoreEntryGeneration(HasKeyStoreEntry entry, boolean addPassword, boolean addEntryPassword) {

		Assert.assertNotNull("Key store entry shouldn't be null", entry);
		
		String keyStoreKeyPairType = entry.getClass().getSimpleName();
		
		Assert.assertNotNull(keyStoreKeyPairType+" should have contained a key entry alias", entry.getKeyEntryAlias());

		if (addEntryPassword) {
			Assert.assertNotNull(keyStoreKeyPairType+" should have contained a key entry password", entry.getKeyEntryPassword());
		} else {
			Assert.assertNull(keyStoreKeyPairType+" shouldn't have contained a key entry password", entry.getKeyEntryPassword());
		}

		Assert.assertNotNull(keyStoreKeyPairType+" should have contained a key store definition", entry.getKeyStore());

		KeyStore keyStore = entry.getKeyStore();

		Assert.assertNotNull(keyStoreKeyPairType+"'s key store should have contained a type", keyStore.getType());
		Assert.assertNotNull(keyStoreKeyPairType+"'s key store should have contained a file path", keyStore.getFilePath());

		if (addPassword) {
			Assert.assertNotNull(keyStoreKeyPairType+"'s key store should have contained a password", keyStore.getPassword());
		} else {
			Assert.assertNull(keyStoreKeyPairType+"'s key store shouldn't have contained a password", keyStore.getPassword());
		}
		
	}

}
