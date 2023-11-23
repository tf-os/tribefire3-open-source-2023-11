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
package com.braintribe.utils;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.function.Supplier;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.common.lcd.Constants;
import com.braintribe.testing.category.Online;
import com.braintribe.testing.tools.TestTools;
import com.braintribe.utils.lcd.Not;

/**
 * Provides tests for {@link IOTools}.
 *
 * @author michael.lafite
 */

public class IOToolsTest {

	@Category(Online.class)
	@Test
	public void testReadFromURL() throws MalformedURLException, IOException {
		assertTrue(IOTools.urlToString(new URL("http://www.google.com"), null).startsWith("<!doctype html><html"));

		final File file = TestTools.newTempFile(true);
		final String fileContentWritten = "abc���߀";
		final String encoding = Constants.encodingUTF8();
		final String wrongEncoding = "ISO8859_1";
		FileTools.writeStringToFile(file, fileContentWritten, encoding);

		final String fileContentRead = FileTools.readStringFromFile(file, encoding);
		assertEquals(fileContentWritten, fileContentRead);
		final String fileContentReadWithWrongEncoding = FileTools.readStringFromFile(file, wrongEncoding);
		assertNotEquals(fileContentReadWithWrongEncoding, fileContentWritten);

		final URL fileURL = Not.Null(file.toURI().toURL());
		assertEquals(fileContentWritten, IOTools.urlToString(fileURL, null));
		assertEquals(fileContentWritten, IOTools.urlToString(fileURL, encoding));
		assertNotEquals(fileContentWritten, IOTools.urlToString(fileURL, wrongEncoding));
	}

	@Test
	public void testGetParent() {
		boolean isWindows = false;
		if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
			isWindows = true;
		}

		final File file = isWindows ? new File("C:/Windows/System32/drivers/etc/hosts") : new File("/var/log");
		final URL url = FileTools.toURL(file);
		final String urlString = url.toString();

		final URL expected = isWindows ? IOTools.newUrl("file:/C:/Windows/System32/drivers/etc") : IOTools.newUrl("file:/var");
		assertThat(IOTools.getParent(url)).isEqualTo(expected);
		assertThat(IOTools.getParent(urlString)).isEqualTo(expected.toString());
	}

	@Test
	public void testReadFullySimple() throws IOException {
		String testString = "Hello, World";
		byte[] testBytes = testString.getBytes(StandardCharsets.UTF_8);
		ByteArrayInputStream bais = new ByteArrayInputStream(testBytes);
		byte[] readBuffer = new byte[1024];
		int readBytes = IOTools.readFully(bais, readBuffer);

		assertThat(readBytes).isEqualTo(testBytes.length);
		assertThat(new String(readBuffer, 0, readBytes, StandardCharsets.UTF_8)).isEqualTo(testString);
	}

	@Test
	public void testReadFullyPartialBuffer() throws IOException {
		String testString = "Hello, World";
		byte[] testBytes = testString.getBytes(StandardCharsets.UTF_8);
		ByteArrayInputStream bais = new ByteArrayInputStream(testBytes);
		byte[] readBuffer = new byte[1024];
		int readBytes = IOTools.readFully(bais, readBuffer, 1, 3);

		assertThat(readBytes).isEqualTo(3);
		assertThat(new String(readBuffer, 1, 3, StandardCharsets.UTF_8)).isEqualTo("Hel");
	}

	@Test
	public void testReadFullyCheckLimitsNegativeOffset() throws IOException {
		String testString = "Hello, World";
		byte[] testBytes = testString.getBytes(StandardCharsets.UTF_8);
		ByteArrayInputStream bais = new ByteArrayInputStream(testBytes);
		byte[] readBuffer = new byte[1024];

		try {
			IOTools.readFully(bais, readBuffer, -1, 3);
			Assert.fail("There should be an exception!");
		} catch (IndexOutOfBoundsException ioobe) {
			// this is what we want
		}
	}

	@Test
	public void testReadFullyCheckLimitsNegativeLen() throws IOException {
		String testString = "Hello, World";
		byte[] testBytes = testString.getBytes(StandardCharsets.UTF_8);
		ByteArrayInputStream bais = new ByteArrayInputStream(testBytes);
		byte[] readBuffer = new byte[1024];

		try {
			IOTools.readFully(bais, readBuffer, 0, -1);
			Assert.fail("There should be an exception!");
		} catch (IndexOutOfBoundsException ioobe) {
			// this is what we want
		}
	}

	@Test
	public void testReadFullyCheckLimitsTooLargeLen() throws IOException {
		String testString = "Hello, World";
		byte[] testBytes = testString.getBytes(StandardCharsets.UTF_8);
		ByteArrayInputStream bais = new ByteArrayInputStream(testBytes);
		byte[] readBuffer = new byte[10];

		try {
			IOTools.readFully(bais, readBuffer, 0, 11);
			Assert.fail("There should be an exception!");
		} catch (IndexOutOfBoundsException ioobe) {
			// this is what we want
		}
	}

	@Test
	public void testReadFullyCheckLimitsLenExceedsBuffer() throws IOException {
		String testString = "Hello, World";
		byte[] testBytes = testString.getBytes(StandardCharsets.UTF_8);
		ByteArrayInputStream bais = new ByteArrayInputStream(testBytes);
		byte[] readBuffer = new byte[10];

		try {
			IOTools.readFully(bais, readBuffer, 1, 10);
			Assert.fail("There should be an exception!");
		} catch (IndexOutOfBoundsException ioobe) {
			// this is what we want
		}
	}

	@Test
	public void testReadFullyEmptyStream() throws IOException {
		String testString = "";
		byte[] testBytes = testString.getBytes(StandardCharsets.UTF_8);
		ByteArrayInputStream bais = new ByteArrayInputStream(testBytes);
		byte[] readBuffer = new byte[1024];
		int readBytes = IOTools.readFully(bais, readBuffer);

		assertThat(readBytes).isEqualTo(-1);
	}

	@Test
	public void testReadFullyIOException() {
		InputStream bais = new InputStream() {
			@Override
			public int read(byte b[], int off, int len) throws IOException {
				throw new IOException("Gotcha!");
			}
			@Override
			public int read() throws IOException {
				throw new IOException("Gotcha!");
			}
		};
		byte[] readBuffer = new byte[1024];
		try {
			IOTools.readFully(bais, readBuffer);
			Assert.fail("There should be an exception!");
		} catch (IOException e) {
			// this is what we want
		}
	}

	@Test
	public void testReadFullyFullBuffer() throws IOException {
		String testString = "Hello, World";
		byte[] testBytes = testString.getBytes(StandardCharsets.UTF_8);
		ByteArrayInputStream bais = new ByteArrayInputStream(testBytes);
		byte[] readBuffer = new byte[testBytes.length];
		int readBytes = IOTools.readFully(bais, readBuffer);

		assertThat(readBytes).isEqualTo(testBytes.length);
		assertThat(new String(readBuffer, 0, readBytes, StandardCharsets.UTF_8)).isEqualTo(testString);
	}

	@Test
	public void testReadFullyTooSmallBuffer() throws IOException {
		String testString = "Hello, World";
		byte[] testBytes = testString.getBytes(StandardCharsets.UTF_8);
		ByteArrayInputStream bais = new ByteArrayInputStream(testBytes);
		byte[] readBuffer = new byte[testBytes.length - 1];
		int readBytes = IOTools.readFully(bais, readBuffer);

		assertThat(readBytes).isEqualTo(testBytes.length - 1);
		assertThat(new String(readBuffer, 0, readBytes, StandardCharsets.UTF_8)).isEqualTo("Hello, Worl");
	}

	@Test
	public void testReadFullyTricklingInputStream() throws IOException {
		String testString = "Hello, World";
		byte[] testBytes = testString.getBytes(StandardCharsets.UTF_8);
		ByteArrayInputStream bais = new ByteArrayInputStream(testBytes) {
			@Override
			public synchronized int read(byte b[], int off, int len) {
				return super.read(b, off, 1);
			}
		};
		byte[] readBuffer = new byte[1024];
		int readBytes = IOTools.readFully(bais, readBuffer);

		assertThat(readBytes).isEqualTo(testBytes.length);
		assertThat(new String(readBuffer, 0, readBytes, StandardCharsets.UTF_8)).isEqualTo(testString);
	}

	@Test
	public void testReadFullyZeroLen() throws IOException {
		String testString = "Hello, World";
		byte[] testBytes = testString.getBytes(StandardCharsets.UTF_8);
		ByteArrayInputStream bais = new ByteArrayInputStream(testBytes);
		byte[] readBuffer = new byte[1024];
		int readBytes = IOTools.readFully(bais, readBuffer, 0, 0);

		assertThat(readBytes).isEqualTo(0);
	}

	@Test
	public void testReadFullyNullPointerExceptions() throws IOException {
		String testString = "Hello, World";
		byte[] testBytes = testString.getBytes(StandardCharsets.UTF_8);
		ByteArrayInputStream bais = new ByteArrayInputStream(testBytes);
		byte[] readBuffer = new byte[10];

		try {
			IOTools.readFully(null, readBuffer, 1, 10);
			Assert.fail("There should be an exception!");
		} catch (NullPointerException ioobe) {
			// this is what we want
		}
		try {
			IOTools.readFully(bais, null, 1, 10);
			Assert.fail("There should be an exception!");
		} catch (NullPointerException ioobe) {
			// this is what we want
		}

		try {
			IOTools.readFully(null, readBuffer);
			Assert.fail("There should be an exception!");
		} catch (NullPointerException ioobe) {
			// this is what we want
		}
		try {
			IOTools.readFully(bais, null);
			Assert.fail("There should be an exception!");
		} catch (NullPointerException ioobe) {
			// this is what we want
		}
	}

	@Test
	public void testReadFullyRuntimeException() throws IOException {
		InputStream bais = new InputStream() {
			@Override
			public int read(byte b[], int off, int len) throws IOException {
				throw new RuntimeException("Gotcha!");
			}
			@Override
			public int read() throws IOException {
				throw new RuntimeException("Gotcha!");
			}
		};
		byte[] readBuffer = new byte[1024];
		try {
			IOTools.readFully(bais, readBuffer);
			Assert.fail("There should be an exception!");
		} catch (RuntimeException e) {
			// this is what we want
		}
	}

	@Test
	public void testTransferBytes() throws Exception {
		ByteArrayInputStream source = new ByteArrayInputStream("Hello, world".getBytes(StandardCharsets.UTF_8));
		ByteArrayOutputStream target = new ByteArrayOutputStream();
		IOTools.transferBytes(source, target);
		assertThat(target.toString("UTF-8")).isEqualTo("Hello, world");
	}

	@Test
	public void testTransferBytesBufferConstantsSuppliers() {

		float below = 0.7f;
		float above = 1.3f;

		testTransferBytesBufferConstantsSupplier(IOTools.BUFFER_SUPPLIER_4K, below);
		testTransferBytesBufferConstantsSupplier(IOTools.BUFFER_SUPPLIER_4K, 1);
		testTransferBytesBufferConstantsSupplier(IOTools.BUFFER_SUPPLIER_4K, above);

		testTransferBytesBufferConstantsSupplier(IOTools.BUFFER_SUPPLIER_8K, below);
		testTransferBytesBufferConstantsSupplier(IOTools.BUFFER_SUPPLIER_8K, 1);
		testTransferBytesBufferConstantsSupplier(IOTools.BUFFER_SUPPLIER_8K, above);

		testTransferBytesBufferConstantsSupplier(IOTools.BUFFER_SUPPLIER_16K, below);
		testTransferBytesBufferConstantsSupplier(IOTools.BUFFER_SUPPLIER_16K, 1);
		testTransferBytesBufferConstantsSupplier(IOTools.BUFFER_SUPPLIER_16K, above);

		testTransferBytesBufferConstantsSupplier(IOTools.BUFFER_SUPPLIER_32K, below);
		testTransferBytesBufferConstantsSupplier(IOTools.BUFFER_SUPPLIER_32K, 1);
		testTransferBytesBufferConstantsSupplier(IOTools.BUFFER_SUPPLIER_32K, above);

		testTransferBytesBufferConstantsSupplier(IOTools.BUFFER_SUPPLIER_64K, below);
		testTransferBytesBufferConstantsSupplier(IOTools.BUFFER_SUPPLIER_64K, 1);
		testTransferBytesBufferConstantsSupplier(IOTools.BUFFER_SUPPLIER_64K, above);

	}

	private void testTransferBytesBufferConstantsSupplier(Supplier<byte[]> bufferSupplier, float factor) {

		int bufferSize = bufferSupplier.get().length;
		byte[] data = randomData(Math.round(bufferSize * factor));

		ByteArrayInputStream source = new ByteArrayInputStream(data);
		ByteArrayOutputStream target = new ByteArrayOutputStream();

		long transferred = IOTools.transferBytes(source, target);

		assertThat(transferred).isEqualTo(data.length);
		assertThat(target.toByteArray()).isEqualTo(data);

	}

	private static byte[] randomData(int dataSize) {
		byte[] data = new byte[dataSize];
		new Random().nextBytes(data);
		return data;
	}

	@Test
	public void testCloseUnchecked() {

		Closeable ioExceptionCloseable = new Closeable() {
			@Override
			public void close() throws IOException {
				throw new IOException("forced exception");
			}
		};

		try {
			IOTools.closeCloseableUnchecked(ioExceptionCloseable);
			fail("There should have been an exception.");
		} catch (UncheckedIOException expected) {
			// Do nothing, this is expected.
		}

		AutoCloseable exceptionAutoCloseable = new AutoCloseable() {
			@Override
			public void close() throws Exception {
				throw new Exception("forced exception");
			}
		};

		try {
			IOTools.closeCloseableUnchecked(exceptionAutoCloseable);
			fail("There should have been an exception.");
		} catch (RuntimeException expected) {
			// Do nothing, this is expected.
		}
	}

}
