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
package com.braintribe.utils.stream;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Random;

import org.junit.Ignore;
import org.junit.Test;

import com.braintribe.utils.IOTools;

public class CountingInputStreamTest {

	@Test
	public void testInputStreamUnbuffered() throws Exception {
		this.testInputStream(false);
	}

	@Test
	public void testInputStreamBuffered() throws Exception {
		this.testInputStream(true);
	}

	@Ignore
	public void testInputStream(boolean buffered) throws Exception {

		int runs = 20;
		Random rnd = new Random();

		for (int i = 0; i < runs; ++i) {

			int size = rnd.nextInt(50000);

			byte[] input = new byte[size];
			rnd.nextBytes(input);

			ByteArrayInputStream bis = null;
			ByteArrayOutputStream baos = null;
			CountingInputStream cis = null;
			try {
				bis = new ByteArrayInputStream(input);
				cis = new CountingInputStream(bis, buffered);

				baos = new ByteArrayOutputStream();
				IOTools.pump(cis, baos);

			} finally {
				IOTools.closeCloseable(bis, null);
				IOTools.closeCloseable(cis, null);
			}

			assertThat(cis.getCount()).isEqualTo(size);
			assertThat(baos.toByteArray()).isEqualTo(input);

		}

	}
}
