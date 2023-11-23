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

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;

import org.junit.Assert;
import org.junit.Test;

public class TeeWriterOutputStreamTest {

	@Test
	public void testWritingOutputStreamWithClose() throws Exception {

		String text = "Hello, world!";
		byte[] textBytes = text.getBytes("UTF-8");

		ByteArrayOutputStream delegate = new ByteArrayOutputStream();
		StringWriter writer = new StringWriter();
		int maxLogLength = 1000;

		TeeWriterOutputStream wos = new TeeWriterOutputStream(delegate, writer, maxLogLength);
		wos.write(textBytes);

		// Before close() has been invoked and before the limit has been reached, the writer should be empty
		Assert.assertEquals("", writer.toString());

		wos.flush();

		Assert.assertEquals("", writer.toString());
		Assert.assertArrayEquals(textBytes, delegate.toByteArray());

		wos.close();

		Assert.assertArrayEquals(textBytes, delegate.toByteArray());
		Assert.assertEquals(text, writer.toString());

	}

	@Test
	public void testWritingOutputStreamBeforeClose() throws Exception {

		String text = "Hello, world!";
		byte[] textBytes = text.getBytes("UTF-8");

		ByteArrayOutputStream delegate = new ByteArrayOutputStream();
		StringWriter writer = new StringWriter();
		int maxLogLength = 5;

		TeeWriterOutputStream wos = new TeeWriterOutputStream(delegate, writer, maxLogLength);
		wos.write(textBytes);

		// The limit has been reached, the first 5 bytes should be in the text
		Assert.assertEquals("Hello", writer.toString());

		wos.close();

		Assert.assertArrayEquals(textBytes, delegate.toByteArray());
		Assert.assertEquals("Hello", writer.toString());

	}
}
