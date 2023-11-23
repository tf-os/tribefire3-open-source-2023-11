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
import org.junit.Test;

import com.braintribe.crypto.CryptorException;
import com.braintribe.crypto.hash.Hasher;
import com.braintribe.model.crypto.configuration.hashing.HashingConfiguration;
import com.braintribe.model.processing.aspect.crypto.test.commons.TestDataProvider;
import com.braintribe.model.processing.cryptor.basic.hash.BasicHasherFactory;

public class HasherFactoryTest extends CryptorFactoryTestBase {

	@Test
	public void testForAllSupportedConfigurations() throws Exception {
		testForAllSupportedConfigurations(hasherFactory);
	}

	@Test(expected = CryptorException.class)
	public void testUnsupportedConfiguration() throws Exception {

		HashingConfiguration config = createHashingConfiguration("MD4", false);

		Hasher hasher = hasherFactory.newCryptor(config);

		hasher.encrypt(TestDataProvider.inputAString).result();

	}

	protected static void testForAllSupportedConfigurations(BasicHasherFactory factory) throws Exception {
		for (HashingConfiguration configuration : hashingConfigurations.values()) {
			Hasher hasher = factory.newCryptor(configuration);
			Assert.assertNotNull(hasher);
			assertCryptorType(hasher, configuration);
			testCryptor(hasher);
		}
	}

}
