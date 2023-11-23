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

import org.junit.Test;

import com.braintribe.utils.IOTools;

public class CountingOutputStreamTest {

	@Test
	public void testCountingOuputStream() {

		CountingOutputStream cos = new CountingOutputStream(new ByteArrayOutputStream());
		assertThat(cos.getCount()).isEqualTo(0L);
	}

	@Test
	public void testCountingOuputStreamMultipleRandom() throws Exception {

		int runs = 100;
		Random rnd = new Random();

		for (int i = 0; i < runs; ++i) {

			int size = rnd.nextInt(100000);

			byte[] data = new byte[size];
			rnd.nextBytes(data);

			ByteArrayInputStream bis = null;
			ByteArrayOutputStream baos = null;

			CountingOutputStream cos = null;
			try {
				bis = new ByteArrayInputStream(data);
				baos = new ByteArrayOutputStream();
				cos = new CountingOutputStream(baos);

				IOTools.pump(bis, cos);

			} finally {
				IOTools.closeCloseable(bis, null);
				IOTools.closeCloseable(cos, null);
			}

			assertThat(cos.getCount()).isEqualTo(size);
			assertThat(baos.toByteArray()).isEqualTo(data);

		}

	}
}
