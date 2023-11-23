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

import java.io.ByteArrayOutputStream;
import java.util.Random;

import org.junit.Test;

public class RangeOutputStreamTest {

	@Test
	public void testPlain() throws Exception {

		Random rnd = new Random();

		for (int i = 0; i < 10; ++i) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			RangeOutputStream ros = new RangeOutputStream(baos, 0, null);

			int size = rnd.nextInt(1000) + 100;
			byte[] input = new byte[size];
			rnd.nextBytes(input);

			ros.write(input);
			ros.close();

			assertThat(baos.toByteArray()).isEqualTo(input);
		}
	}

	@Test
	public void testStartToLimit() throws Exception {

		Random rnd = new Random();

		for (int i = 0; i < 10; ++i) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			RangeOutputStream ros = new RangeOutputStream(baos, 0, 49L);

			int size = rnd.nextInt(1000) + 100;
			byte[] input = new byte[size];
			rnd.nextBytes(input);

			ros.write(input);
			ros.close();

			byte[] actual = baos.toByteArray();
			byte[] expected = new byte[50];
			System.arraycopy(input, 0, expected, 0, 50);

			assertThat(actual).hasSize(50);
			assertThat(actual).isEqualTo(expected);
		}
	}

	@Test
	public void testLimitToLimit() throws Exception {

		Random rnd = new Random();

		for (int i = 0; i < 10; ++i) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			RangeOutputStream ros = new RangeOutputStream(baos, 10, 49L);

			int size = rnd.nextInt(1000) + 100;
			byte[] input = new byte[size];
			rnd.nextBytes(input);

			ros.write(input);
			ros.close();

			byte[] actual = baos.toByteArray();
			byte[] expected = new byte[40];
			System.arraycopy(input, 10, expected, 0, 40);

			assertThat(actual).hasSize(40);
			assertThat(actual).isEqualTo(expected);
		}
	}

	@Test
	public void testLimitToEnd() throws Exception {

		Random rnd = new Random();

		for (int i = 0; i < 10; ++i) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			RangeOutputStream ros = new RangeOutputStream(baos, 10, null);

			int size = rnd.nextInt(1000) + 100;
			byte[] input = new byte[size];
			rnd.nextBytes(input);

			ros.write(input);
			ros.close();

			byte[] actual = baos.toByteArray();
			byte[] expected = new byte[size - 10];
			System.arraycopy(input, 10, expected, 0, expected.length);

			assertThat(actual).hasSize(expected.length);
			assertThat(actual).isEqualTo(expected);
		}
	}

	@Test
	public void testLimitToMaxValue() throws Exception {

		Random rnd = new Random();

		for (int i = 0; i < 10; ++i) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			RangeOutputStream ros = new RangeOutputStream(baos, 10, Long.MAX_VALUE);

			int size = rnd.nextInt(1000) + 100;
			byte[] input = new byte[size];
			rnd.nextBytes(input);

			ros.write(input);
			ros.close();

			byte[] actual = baos.toByteArray();
			byte[] expected = new byte[size - 10];
			System.arraycopy(input, 10, expected, 0, expected.length);

			assertThat(actual).hasSize(expected.length);
			assertThat(actual).isEqualTo(expected);
		}
	}
}
