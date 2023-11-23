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

import static java.util.Arrays.copyOfRange;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

public class WriteOnReadInputStreamTest {

	private int dataSize = 500 * 1024;

	@Test
	public void testFullRead() throws Exception {

		byte[] input = randomData(dataSize);

		ByteArrayInputStream in = new ByteArrayInputStream(input);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		WriteOnReadInputStream win = new WriteOnReadInputStream(in, out);

		read(win, dataSize);

		Assert.assertEquals(dataSize, win.getWriteCount());
		Assert.assertArrayEquals(input, out.toByteArray());

	}

	@Test
	public void testFullBufferedRead() throws Exception {

		byte[] input = randomData(dataSize);

		ByteArrayInputStream in = new ByteArrayInputStream(input);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		WriteOnReadInputStream win = new WriteOnReadInputStream(in, out);

		readBuffered(win, dataSize);

		Assert.assertEquals(dataSize, win.getWriteCount());
		Assert.assertArrayEquals(input, out.toByteArray());

	}

	@Test
	public void testPartialRead() throws Exception {

		byte[] input = randomData(dataSize);

		ByteArrayInputStream in = new ByteArrayInputStream(input);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		WriteOnReadInputStream win = new WriteOnReadInputStream(in, out);

		read(win, dataSize / 2);

		Assert.assertEquals(dataSize / 2, win.getWriteCount());
		Assert.assertArrayEquals(copyOfRange(input, 0, dataSize / 2), out.toByteArray());

	}

	@Test
	public void testPartialBufferedRead() throws Exception {

		byte[] input = randomData(dataSize);

		ByteArrayInputStream in = new ByteArrayInputStream(input);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		WriteOnReadInputStream win = new WriteOnReadInputStream(in, out);

		int read = readBuffered(win, dataSize / 2);

		Assert.assertEquals(read, win.getWriteCount());
		Assert.assertArrayEquals(copyOfRange(input, 0, read), out.toByteArray());

	}

	@Test
	public void testForcedConsumption() throws Exception {

		byte[] input = randomData(dataSize);

		ByteArrayInputStream in = new ByteArrayInputStream(input);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		WriteOnReadInputStream win = new WriteOnReadInputStream(in, out);

		int read = readBuffered(win, dataSize / 2);
		read += win.consume();

		Assert.assertEquals(dataSize, read);
		Assert.assertEquals(dataSize, win.getWriteCount());
		Assert.assertArrayEquals(input, out.toByteArray());

	}

	@Test
	public void testReadWithoutOutput() throws Exception {

		byte[] input = randomData(dataSize);

		ByteArrayInputStream in = new ByteArrayInputStream(input);
		WriteOnReadInputStream win = new WriteOnReadInputStream(in, null);

		int read = read(win, dataSize);

		Assert.assertEquals(0, win.getWriteCount());
		Assert.assertEquals(dataSize, read);

	}

	@Test
	public void testBufferedReadWithoutOutput() throws Exception {

		byte[] input = randomData(dataSize);

		ByteArrayInputStream in = new ByteArrayInputStream(input);
		WriteOnReadInputStream win = new WriteOnReadInputStream(in, null);

		int read = readBuffered(win, dataSize);

		Assert.assertEquals(0, win.getWriteCount());
		Assert.assertEquals(dataSize, read);

	}

	@Test
	public void testForcedConsumptionWithoutOutput() throws Exception {

		byte[] input = randomData(dataSize);

		ByteArrayInputStream in = new ByteArrayInputStream(input);
		WriteOnReadInputStream win = new WriteOnReadInputStream(in, null);

		int read = readBuffered(win, dataSize / 2);
		read += win.consume();

		Assert.assertEquals(0, win.getWriteCount());
		Assert.assertEquals(dataSize, read);

	}

	protected int readBuffered(InputStream is, int limit) throws Exception {
		int count;
		int totalCount = 0;
		while ((count = is.read(new byte[8192])) != -1) {
			totalCount += count;
			if (totalCount >= limit) {
				break;
			}
		}
		return totalCount;
	}

	protected int read(InputStream is, int limit) throws Exception {
		int totalCount = 0;
		while (is.read() != -1) {
			totalCount++;
			if (totalCount >= limit) {
				break;
			}
		}
		return totalCount;
	}

	protected byte[] randomData(int dataSize) {
		byte[] data = new byte[dataSize];
		new Random().nextBytes(data);
		return data;
	}

}
