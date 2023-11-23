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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Test;

public class EncryptTest {

	@Test
	public void testEncryption() throws Exception {

		String result = null;
		PrintStream old = System.out;
		try {
			// Create a stream to hold the output
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PrintStream ps = new PrintStream(baos);
			System.setOut(ps);

			Encrypt.main(new String[] { "cortex" });

			// Put things back
			System.out.flush();

			result = baos.toString().trim();

		} finally {
			System.setOut(old);
		}

		assertThat(result).isNotNull();
		assertThat(result).startsWith("${" + EncryptedPropertySource.PREFIX + ":");
		assertThat(result).endsWith("}");
		assertThat(result.length()).isGreaterThan(EncryptedPropertySource.PREFIX.length() + 4);

	}

}
