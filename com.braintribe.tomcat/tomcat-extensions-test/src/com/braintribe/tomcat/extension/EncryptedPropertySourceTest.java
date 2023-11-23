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

import org.junit.Test;

public class EncryptedPropertySourceTest {

	@Test
	public void testSystemProps() throws Exception {

		EncryptedPropertySource instance = new EncryptedPropertySource();

		System.setProperty("hello", "world");

		String result = instance.getProperty("hello");

		assertThat(result).isNotNull();
		assertThat(result).isEqualTo("world");

	}

	@Test
	public void testDecryption() throws Exception {

		EncryptedPropertySource instance = new EncryptedPropertySource();

		String encrypted = "AES/CBC/PKCS5Padding:A2NLSWtB+se5MeD2m9qJ3c6UYbe8Dc9RRee02X2VUto69Ql2DmpWJXrBZZxvVf+x7HUjbw==";

		String result = instance.getProperty(encrypted);

		assertThat(result).isNotNull();
		assertThat(result).isEqualTo("cortex");

	}

	@Test
	public void testDecryptionWithKeyLength() throws Exception {

		EncryptedPropertySource instance = new EncryptedPropertySource();

		String encrypted = "AES/CBC/PKCS5Padding:128:5y+M4kFHswOJRQmzryLT3rYBLxGfRWATEPBLfNCSHFttW1PxB+GOZIdFBN9EK6Y5ReBnXQ==";

		String result = instance.getProperty(encrypted);

		assertThat(result).isNotNull();
		assertThat(result).isEqualTo("cortex");

	}

	@Test
	public void testDecryptionWithWrongKeyLength() throws Exception {

		EncryptedPropertySource instance = new EncryptedPropertySource();

		String encrypted = "AES/CBC/PKCS5Padding:128:A2NLSWtB+se5MeD2m9qJ3c6UYbe8Dc9RRee02X2VUto69Ql2DmpWJXrBZZxvVf+x7HUjbw=="; // Correct encoding,
																																// wrong keylength

		String result = instance.getProperty(encrypted);

		assertThat(result).isNull();

	}
}
