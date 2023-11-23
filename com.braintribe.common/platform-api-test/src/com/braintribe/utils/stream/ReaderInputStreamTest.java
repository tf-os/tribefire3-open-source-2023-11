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
import java.io.StringReader;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.utils.IOTools;

public class ReaderInputStreamTest {

	@Test
	public void testReaderInputStreamSimple() throws Exception {

		String text = "Hello, world!";
		StringReader reader = new StringReader(text);
		ReaderInputStream in = new ReaderInputStream(reader);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		IOTools.pump(in, baos);

		String target = baos.toString("UTF-8");
		Assert.assertEquals(text, target);
	}

	@Test
	public void testReaderInputStreamLarge() throws Exception {

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 10000; ++i) {
			sb.append("Hello, world!");
		}
		String text = sb.toString();

		StringReader reader = new StringReader(text);
		ReaderInputStream in = new ReaderInputStream(reader);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		IOTools.pump(in, baos);

		String target = baos.toString("UTF-8");
		Assert.assertEquals(text, target);
	}

	@Test
	public void testReaderInputStreamEmpty() throws Exception {

		String text = "";

		StringReader reader = new StringReader(text);
		ReaderInputStream in = new ReaderInputStream(reader);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		IOTools.pump(in, baos);

		String target = baos.toString("UTF-8");
		Assert.assertEquals(text, target);
	}
}
