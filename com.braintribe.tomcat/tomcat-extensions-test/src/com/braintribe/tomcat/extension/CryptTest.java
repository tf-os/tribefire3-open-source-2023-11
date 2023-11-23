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
package com.braintribe.tomcat.extension;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;

public class CryptTest {

	@Test
	public void testEncryptionAndDecryption() throws Exception {

		String pw = "hello, world";

		String encrypted = Crypt.encrypt(pw, 128);

		assertThat(encrypted).isNotNull();
		assertThat(encrypted.length()).isGreaterThan(0);
		assertThat(encrypted).isNotEqualTo(pw);
		assertThat(encrypted).doesNotStartWith("${" + EncryptedPropertySource.PREFIX + ":");

		String decrypted = Crypt.decrypt(encrypted, 128);

		assertThat(decrypted).isNotNull();
		assertThat(decrypted.length()).isGreaterThan(0);
		assertThat(decrypted).isEqualTo(pw);

	}

	@Test
	public void testSalting() throws Exception {

		String pw = "hello, world";

		String encrypted1 = Crypt.encrypt(pw, 128);

		assertThat(encrypted1).isNotNull();
		assertThat(encrypted1.length()).isGreaterThan(0);

		String encrypted2 = Crypt.encrypt(pw, 128);

		assertThat(encrypted2).isNotNull();
		assertThat(encrypted2.length()).isGreaterThan(0);

		assertThat(encrypted1).isNotEqualTo(encrypted2);

	}

	@Test
	public void testDecryptionWithWrongKeyLength() throws Exception {
		// Correct encoding, wrong keylength
		String encrypted = "A2NLSWtB+se5MeD2m9qJ3c6UYbe8Dc9RRee02X2VUto69Ql2DmpWJXrBZZxvVf+x7HUjbw==";
		try {
			Crypt.decrypt(encrypted, 128);
			fail("This should have thrown an error.");
		} catch (Exception expected) {
			// Do nothing
		}
	}
}
