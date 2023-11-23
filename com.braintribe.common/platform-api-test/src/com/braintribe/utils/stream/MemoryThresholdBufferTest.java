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

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import org.junit.Test;

import com.braintribe.utils.IOTools;

public class MemoryThresholdBufferTest {

	@Test
	public void testInMemory() throws Exception {

		byte[] bytes = "Hello, world".getBytes("UTF-8");

		MemoryThresholdBuffer buffer = new MemoryThresholdBuffer(100000);
		buffer.write(bytes);
		buffer.close();

		assertThat(buffer.getFile()).isNull();
		assertThat(buffer.openInputStream(true)).hasSameContentAs(new ByteArrayInputStream(bytes));
		assertThat(buffer.getLength()).isEqualTo(bytes.length);
	}

	@Test
	public void testWithFile() throws Exception {

		byte[] bytes = "Hello, world".getBytes("UTF-8");

		MemoryThresholdBuffer buffer = new MemoryThresholdBuffer(3);
		buffer.write(bytes);
		buffer.close();

		assertThat(buffer.getFile()).isNotNull();
		assertThat(buffer.openInputStream(true)).hasSameContentAs(new ByteArrayInputStream(bytes));
	}

	@Test
	public void testThreshold() throws Exception {

		byte[] bytes = "123".getBytes("UTF-8");

		MemoryThresholdBuffer buffer = new MemoryThresholdBuffer(3);
		buffer.write(bytes);
		buffer.close();

		assertThat(buffer.getFile()).isNull();
		assertThat(buffer.openInputStream(true)).hasSameContentAs(new ByteArrayInputStream(bytes));
	}

	@Test
	public void testThresholdExceed() throws Exception {

		byte[] bytes = "1234".getBytes("UTF-8");

		MemoryThresholdBuffer buffer = new MemoryThresholdBuffer(3);
		buffer.write(bytes);
		buffer.close();

		assertThat(buffer.getFile()).isNotNull();
		assertThat(buffer.openInputStream(true)).hasSameContentAs(new ByteArrayInputStream(bytes));
	}

	@Test
	public void testDelete() throws Exception {

		byte[] bytes = "1234".getBytes("UTF-8");

		MemoryThresholdBuffer buffer = new MemoryThresholdBuffer(3);
		buffer.write(bytes);
		buffer.close();

		assertThat(buffer.getFile()).isNotNull();
		assertThat(buffer.openInputStream(false)).hasSameContentAs(new ByteArrayInputStream(bytes));

		File bufferFile = buffer.getFile();
		assertThat(bufferFile).exists();
		buffer.delete();
		assertThat(bufferFile).doesNotExist();
	}

	@Test
	public void testAutomaticDelete() throws Exception {

		byte[] bytes = "1234".getBytes("UTF-8");

		MemoryThresholdBuffer buffer = new MemoryThresholdBuffer(3);
		buffer.write(bytes);
		buffer.close();

		assertThat(buffer.getFile()).isNotNull();
		assertThat(buffer.openInputStream(true)).hasSameContentAs(new ByteArrayInputStream(bytes));

		// Note: the consumption of the file should have deleted it (see constructor parameter)
		File bufferFile = buffer.getFile();
		assertThat(bufferFile).doesNotExist();

	}

	@Test
	public void testWrite() throws Exception {

		byte[] bytes = "123".getBytes("UTF-8");
		byte additionalByte = "4".getBytes("UTF-8")[0];
		byte[] expectedbytes = "1234".getBytes("UTF-8");

		MemoryThresholdBuffer buffer = new MemoryThresholdBuffer(3);
		buffer.write(bytes);
		buffer.write(additionalByte);
		buffer.close();

		assertThat(buffer.getFile()).isNotNull();
		assertThat(buffer.openInputStream(true)).hasSameContentAs(new ByteArrayInputStream(expectedbytes));

		// Note: the consumption of the file should have deleted it (see constructor parameter)
		File bufferFile = buffer.getFile();
		assertThat(bufferFile).doesNotExist();

	}

	@Test
	public void testMultipleOpensWithDeleteTrue() throws Exception {

		byte[] bytes = "1234".getBytes("UTF-8");

		MemoryThresholdBuffer buffer = new MemoryThresholdBuffer(3);
		buffer.write(bytes);
		buffer.close();

		try (InputStream in = buffer.openInputStream(true)) {

			// Note: asserThat(InputStream) closes the Stream
			assertThat(buffer.openInputStream(true)).hasSameContentAs(new ByteArrayInputStream(bytes));
			assertThat(buffer.openInputStream(true)).hasSameContentAs(new ByteArrayInputStream(bytes));
			assertThat(buffer.openInputStream(true)).hasSameContentAs(new ByteArrayInputStream(bytes));
		}

		File bufferFile = buffer.getFile();
		assertThat(bufferFile).doesNotExist();

	}

	@Test
	public void testMultipleOpensWithDeleteFalse() throws Exception {

		byte[] bytes = "1234".getBytes("UTF-8");

		MemoryThresholdBuffer buffer = new MemoryThresholdBuffer(3);
		buffer.write(bytes);
		buffer.close();

		try (InputStream in = buffer.openInputStream(false)) {

			// Note: asserThat(InputStream) closes the Stream
			assertThat(buffer.openInputStream(false)).hasSameContentAs(new ByteArrayInputStream(bytes));
			assertThat(buffer.openInputStream(false)).hasSameContentAs(new ByteArrayInputStream(bytes));
			assertThat(buffer.openInputStream(false)).hasSameContentAs(new ByteArrayInputStream(bytes));
		} finally {
			buffer.delete();
		}

	}

	@Test
	public void testIterativeWrite() throws Exception {

		byte[] bytes = "1234567890".getBytes("UTF-8");

		MemoryThresholdBuffer buffer = new MemoryThresholdBuffer(3);

		for (int i = 0; i < bytes.length; ++i) {
			buffer.write(bytes, i, 1);
			buffer.flush();

			byte[] actual;
			try (InputStream in = buffer.openInputStream(false)) {
				actual = IOTools.slurpBytes(in);
			}

			byte[] expected = new byte[i + 1];
			System.arraycopy(bytes, 0, expected, 0, i + 1);
			assertThat(actual).isEqualTo(expected);
		}

		buffer.close();
		buffer.delete();

	}
}
