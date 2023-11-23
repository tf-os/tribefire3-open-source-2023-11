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
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.utils.IOTools;
import com.braintribe.utils.StringTools;

public class RepeatableInputStreamTest {

	private static final int pivotDataSize = RepeatableInputStream.DEFAULT_MEMORY_BUFFER_SIZE;

	@Test
	public void testBackupInMemory() throws Exception {
		testRepeatableInputStream(true, false, 1, false, false);
	}

	@Test
	public void testBackupInMemoryBuffered() throws Exception {
		testRepeatableInputStream(true, true, 1, false, false);
	}

	@Test
	public void testBackupAsFile() throws Exception {
		testRepeatableInputStream(false, false, 1, false, false);
	}

	@Test
	public void testBackupAsFileBuffered() throws Exception {
		testRepeatableInputStream(false, true, 1, false, false);
	}

	@Test
	public void testBackupInMemoryWithMultipleReopenings() throws Exception {
		testRepeatableInputStream(true, false, 3, false, false);
	}

	@Test
	public void testBackupInMemoryBufferedWithMultipleReopenings() throws Exception {
		testRepeatableInputStream(true, true, 3, false, false);
	}

	@Test
	public void testBackupAsFileWithMultipleReopenings() throws Exception {
		testRepeatableInputStream(false, false, 3, false, false);
	}

	@Test
	public void testBackupAsFileBufferedWithMultipleReopenings() throws Exception {
		testRepeatableInputStream(false, true, 3, false, false);
	}

	@Test
	public void testBackupInMemoryWithoutClosing() throws Exception {
		testRepeatableInputStream(true, false, 1, true, false);
	}

	@Test
	public void testBackupInMemoryBufferedWithoutClosing() throws Exception {
		testRepeatableInputStream(true, true, 1, true, false);
	}

	@Test
	public void testBackupAsFileWithoutClosing() throws Exception {
		testRepeatableInputStream(false, false, 1, true, false);
	}

	@Test
	public void testBackupAsFileBufferedWithoutClosing() throws Exception {
		testRepeatableInputStream(false, true, 1, true, false);
	}

	@Test
	public void testBackupInMemoryWithMultipleReopeningsWithoutClosing() throws Exception {
		testRepeatableInputStream(true, false, 3, true, false);
	}

	@Test
	public void testBackupInMemoryBufferedWithMultipleReopeningsWithoutClosing() throws Exception {
		testRepeatableInputStream(true, true, 3, true, false);
	}

	@Test
	public void testBackupAsFileWithMultipleReopeningsWithoutClosing() throws Exception {
		testRepeatableInputStream(false, false, 3, true, false);
	}

	@Test
	public void testBackupAsFileBufferedWithMultipleReopeningsWithoutClosing() throws Exception {
		testRepeatableInputStream(false, true, 3, true, false);
	}

	@Test
	public void testBackupInMemoryWithTimeout() throws Exception {
		testRepeatableInputStream(true, false, 1, false, true);
	}

	@Test
	public void testBackupInMemoryBufferedWithTimeout() throws Exception {
		testRepeatableInputStream(true, true, 1, false, true);
	}

	@Test
	public void testBackupAsFileWithTimeout() throws Exception {
		testRepeatableInputStream(false, false, 1, false, true);
	}

	@Test
	public void testBackupAsFileBufferedWithTimeout() throws Exception {
		testRepeatableInputStream(false, true, 1, false, true);
	}

	@Test
	public void testBackupInMemoryWithMultipleReopeningsWithTimeout() throws Exception {
		testRepeatableInputStream(true, false, 3, false, true);
	}

	@Test
	public void testBackupInMemoryBufferedWithMultipleReopeningsWithTimeout() throws Exception {
		testRepeatableInputStream(true, true, 3, false, true);
	}

	@Test
	public void testBackupAsFileWithMultipleReopeningsWithTimeout() throws Exception {
		testRepeatableInputStream(false, false, 3, false, true);
	}

	@Test
	public void testBackupAsFileBufferedWithMultipleReopeningsWithTimeout() throws Exception {
		testRepeatableInputStream(false, true, 3, false, true);
	}

	@Test
	public void testBackupInMemoryWithoutClosingWithTimeout() throws Exception {
		testRepeatableInputStream(true, false, 1, true, true);
	}

	@Test
	public void testBackupInMemoryBufferedWithoutClosingWithTimeout() throws Exception {
		testRepeatableInputStream(true, true, 1, true, true);
	}

	@Test
	public void testBackupAsFileWithoutClosingWithTimeout() throws Exception {
		testRepeatableInputStream(false, false, 1, true, true);
	}

	@Test
	public void testBackupAsFileBufferedWithoutClosingWithTimeout() throws Exception {
		testRepeatableInputStream(false, true, 1, true, true);
	}

	@Test
	public void testBackupInMemoryWithMultipleReopeningsWithoutClosingWithTimeout() throws Exception {
		testRepeatableInputStream(true, false, 3, true, true);
	}

	@Test
	public void testBackupInMemoryBufferedWithMultipleReopeningsWithoutClosingWithTimeout() throws Exception {
		testRepeatableInputStream(true, true, 3, true, true);
	}

	@Test
	public void testBackupAsFileWithMultipleReopeningsWithoutClosingWithTimeout() throws Exception {
		testRepeatableInputStream(false, false, 3, true, true);
	}

	@Test
	public void testBackupAsFileBufferedWithMultipleReopeningsWithoutClosingWithTimeout() throws Exception {
		testRepeatableInputStream(false, true, 3, true, true);
	}

	@Test
	public void testReopenUnclosedAndPartiallyConsumed() throws Exception {

		int dataSize = pivotDataSize;

		int consumedDataSize = dataSize / 2;

		byte[] originalData = randomData(dataSize);

		TestInputStream originalInput = new TestInputStream(originalData);

		RepeatableInputStream repeatableInput = new RepeatableInputStream(originalInput);

		try {

			readBuffered(repeatableInput, consumedDataSize);

			// reopen without previous close() or fully RepeatableInputStream consumption
			try {
				repeatableInput.reopen();
				Assert.fail("reopen() should have failed without previous close() or fully RepeatableInputStream consumption");
			} catch (IllegalStateException e) {
				// Expected (only at this point) and ignored
			}

		} finally {
			repeatableInput.destroy();
		}

	}

	@Test
	public void testReopenClosedAndPartiallyConsumed() throws Exception {

		int dataSize = pivotDataSize;

		int consumedDataSize = dataSize / 2;

		byte[] originalData = randomData(dataSize);

		TestInputStream originalInput = new TestInputStream(originalData);

		RepeatableInputStream repeatableInput = new RepeatableInputStream(originalInput);

		try {
			read(repeatableInput, consumedDataSize);
			repeatableInput.close();
			byte[] backupData = null;
			try (InputStream backupInput = repeatableInput.reopen()) {
				backupData = IOTools.slurpBytes(backupInput);
			}
			Assert.assertArrayEquals(copyOfRange(originalData, 0, consumedDataSize), backupData);
		} finally {
			repeatableInput.destroy();
		}

	}

	@Test
	public void testReopenClosedAndDestroyed() throws Exception {

		int dataSize = pivotDataSize;

		byte[] originalData = randomData(dataSize);

		TestInputStream originalInput = new TestInputStream(originalData);

		RepeatableInputStream repeatableInput = new RepeatableInputStream(originalInput);

		try {
			readBuffered(repeatableInput);
			repeatableInput.close();
		} finally {
			repeatableInput.destroy();
		}

		try {
			repeatableInput.reopen();
			Assert.fail("reopen() should have failed if called after destroy()");
		} catch (IllegalStateException e) {
			// Expected (only at this point) and ignored
		}

	}

	@Test
	public void testReopenDestroyed() throws Exception {

		int dataSize = pivotDataSize;

		byte[] originalData = randomData(dataSize);

		TestInputStream originalInput = new TestInputStream(originalData);

		RepeatableInputStream repeatableInput = new RepeatableInputStream(originalInput);

		try {
			readBuffered(repeatableInput);
		} finally {
			repeatableInput.destroy();
		}

		try {
			repeatableInput.reopen();
			Assert.fail("reopen() should have failed if called after destroy()");
		} catch (IllegalStateException e) {
			// Expected (only at this point) and ignored
		}

	}

	@Test
	public void testSequenceWithoutReopening() throws Exception {

		int runs = 10;
		long t = 0;
		long readData = 0;

		byte[] originalData = randomData(100_000);

		for (int i = runs; i-- > 0;) {

			TestInputStream originalInput = new TestInputStream(originalData);

			RepeatableInputStream repeatableInput = new RepeatableInputStream(originalInput);

			try {
				long c = System.currentTimeMillis();
				readData = readBuffered(repeatableInput);
				repeatableInput.close();
				t += System.currentTimeMillis() - c;
			} finally {
				repeatableInput.destroy();
			}

		}

		System.out.println("Consumption of " + StringTools.prettyPrintBytesDecimal(readData) + " bytes took in average " + t / runs + "ms");

	}

	protected void testRepeatableInputStream(boolean inMemory, boolean bufferRead, int reopens, boolean bypassClose, boolean useTimeout)
			throws Exception {

		int dataSize = inMemory ? (pivotDataSize / 4) : (3 * pivotDataSize + (pivotDataSize / 2));

		byte[] originalData = randomData(dataSize);

		TestInputStream originalInput = new TestInputStream(originalData);

		RepeatableInputStream repeatableInput = new RepeatableInputStream(originalInput);

		try {

			int total = bufferRead ? readBuffered(repeatableInput) : read(repeatableInput);

			if (!bypassClose) {
				repeatableInput.close();
			}

			Assert.assertEquals(dataSize, total);

			for (int i = 0; i < reopens; i++) {

				byte[] backupData = null;

				try (InputStream backupInput = useTimeout ? repeatableInput.reopen(10000) : repeatableInput.reopen()) {
					backupData = IOTools.slurpBytes(backupInput);
				}

				Assert.assertArrayEquals(originalData, backupData);

			}

		} finally {

			repeatableInput.destroy();

		}

	}

	protected static int readBuffered(InputStream is) throws Exception {
		return readBuffered(is, 0);
	}

	protected static int readBuffered(InputStream is, int limit) throws Exception {
		int count;
		int totalCount = 0;
		while ((count = is.read(new byte[8192])) != -1) {
			totalCount += count;
			if (limit > 0 && totalCount >= limit) {
				break;
			}
		}
		return totalCount;
	}

	protected int read(InputStream is) throws Exception {
		return read(is, 0);
	}

	protected int read(InputStream is, int limit) throws Exception {
		int totalCount = 0;
		while (is.read() != -1) {
			totalCount++;
			if (limit > 0 && totalCount >= limit) {
				break;
			}
		}
		return totalCount;
	}

	public static byte[] randomData(int dataSize) {
		byte[] data = new byte[dataSize];
		new Random().nextBytes(data);
		return data;
	}

	protected static class TestInputStream extends ByteArrayInputStream {

		private boolean closed = false;
		private final double maxThroughputInMbitPerSecond;
		private boolean enforcedMaxThroughput = false;

		public TestInputStream(byte[] buf) {
			this(buf, 0);
		}

		public TestInputStream(byte[] buf, double maxThroughputInMbitPerSecond) {
			super(buf);
			this.maxThroughputInMbitPerSecond = maxThroughputInMbitPerSecond;
		}

		@Override
		public synchronized int read() {
			if (closed) {
				throw new IllegalStateException(this.getClass().getSimpleName() + " is closed");
			}
			int b = super.read();
			enforceMaxThroughputIfNeeded();
			return b;
		}

		@Override
		public synchronized int read(byte b[], int off, int len) {
			if (closed) {
				throw new IllegalStateException(this.getClass().getSimpleName() + " is closed");
			}
			int t = super.read(b, off, len);
			enforceMaxThroughputIfNeeded();
			return t;
		}

		@Override
		public void close() throws IOException {
			this.closed = true;
		}

		protected void enforceMaxThroughputIfNeeded() {

			if (maxThroughputInMbitPerSecond > 0 && !enforcedMaxThroughput) {

				Double minWait = Math.floor(buf.length / 125000.0 / maxThroughputInMbitPerSecond * 1000);

				try {
					Thread.sleep(minWait.longValue());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				enforcedMaxThroughput = true;

			}

		}

	}

}
