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

import com.braintribe.crypto.Cryptor;
import com.braintribe.model.crypto.configuration.encryption.AsymmetricEncryptionConfiguration;
import com.braintribe.model.crypto.configuration.encryption.SymmetricEncryptionConfiguration;
import com.braintribe.model.processing.aspect.crypto.test.TestBase;

public class StandardTokenGenerationTest extends TestBase {

	// ########################
	// ## .. RSA tests ..... ##
	// ########################

	@Test
	public void testRsa() throws Exception {
		testAsymmetricTokenGeneration("RSA");
	}

	// ########################
	// ## .. AES tests ..... ##
	// ########################

	@Test
	public void testAes() throws Exception {
		testSymmetricTokenGeneration("AES");
	}

	// ########################
	// ## .. DES tests ..... ##
	// ########################

	@Test
	public void testDes() throws Exception {
		testSymmetricTokenGeneration("DES");
	}

	// ###########################
	// ## .. DESede tests ..... ##
	// ###########################

	@Test
	public void testDesEde() throws Exception {
		testSymmetricTokenGeneration("DESede");
	}

	// ######################
	// ## .. Commons ..... ##
	// ######################

	protected void testAsymmetricTokenGeneration(String algorithm) throws Exception {

		AsymmetricEncryptionConfiguration config = getEncryptionConfiguration(algorithm);

		KeyPair keyPair = standardKeyPairGenerator.generate((com.braintribe.model.crypto.key.KeyPair) config.getAsymmetricEncryptionToken(), config.getProvider());

		Cryptor cryptor1 = cipherCryptorFactory.builder().keyPair(keyPair).mode(config.getMode()).padding(config.getPadding()).provider(config.getProvider()).build();
		Cryptor cryptor2 = cipherCryptorFactory.builder().keyPair(keyPair).mode(config.getMode()).padding(config.getPadding()).provider(config.getProvider()).build();

		Assert.assertNotEquals("Cryptor instances created based on " + KeyPair.class.getName() + "(s) must not be cached", cryptor1, cryptor2);

		testCryptors(cryptor1, cryptor2);

	}

	protected void testSymmetricTokenGeneration(String algorithm) throws Exception {

		SymmetricEncryptionConfiguration config = getEncryptionConfiguration(algorithm);

		SecretKey secretkey = standardSecretKeyGenerator.generate((com.braintribe.model.crypto.key.SecretKey) config.getSymmetricEncryptionToken(), config.getProvider());

		Cryptor cryptor1 = cipherCryptorFactory.builder().key(secretkey).mode(config.getMode()).padding(config.getPadding()).provider(config.getProvider()).build();
		Cryptor cryptor2 = cipherCryptorFactory.builder().key(secretkey).mode(config.getMode()).padding(config.getPadding()).provider(config.getProvider()).build();

		Assert.assertNotEquals("Cryptor instances created based on " + SecretKey.class.getName() + "(s) must not be cached", cryptor1, cryptor2);

		testCryptors(cryptor1, cryptor2);

	}

}
