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
package com.braintribe.model.processing.aspect.crypto.test.interceptor.manipulation;

import org.junit.Test;

import com.braintribe.model.processing.aspect.crypto.test.commons.model.Encrypted;
import com.braintribe.model.processing.aspect.crypto.test.commons.model.EncryptedMulti;
import com.braintribe.model.processing.aspect.crypto.test.commons.model.Hashed;
import com.braintribe.model.processing.aspect.crypto.test.commons.model.HashedMulti;
import com.braintribe.model.processing.aspect.crypto.test.commons.model.Mixed;
import com.braintribe.model.processing.aspect.crypto.test.commons.model.MixedMulti;
import com.braintribe.model.processing.aspect.crypto.test.interceptor.CryptoInterceptorTestBase;

public class CryptoManipulationInterceptorTest extends CryptoInterceptorTestBase {

	@Test
	public void testEncryptedCreation() throws Exception {
		testCreation(Encrypted.T);
	}
	
	@Test
	public void testHashedCreation() throws Exception {
		testCreation(Hashed.T);
	}

	@Test
	public void testMixedCreation() throws Exception {
		testCreation(Mixed.T);
	}
	
	@Test
	public void testEncryptedMultiCreation() throws Exception {
		testCreation(EncryptedMulti.T);
	}
	
	@Test
	public void testHashedMultiCreation() throws Exception {
		testCreation(HashedMulti.T);
	}

	@Test
	public void testMixedMultiCreation() throws Exception {
		testCreation(MixedMulti.T);
	}

	@Test
	public void testEncryptedUpdate() throws Exception {
		testUpdate(Encrypted.T);
	}

	@Test
	public void testHashedUpdate() throws Exception {
		testUpdate(Hashed.T);
	}

	@Test
	public void testMixedUpdate() throws Exception {
		testUpdate(Mixed.T);
	}
	
	@Test
	public void testEncryptedMultiUpdate() throws Exception {
		testUpdate(EncryptedMulti.T);
	}
	
	@Test
	public void testHashedMultiUpdate() throws Exception {
		testUpdate(HashedMulti.T);
	}

	@Test
	public void testMixedMultiUpdate() throws Exception {
		testUpdate(MixedMulti.T);
	}

}
