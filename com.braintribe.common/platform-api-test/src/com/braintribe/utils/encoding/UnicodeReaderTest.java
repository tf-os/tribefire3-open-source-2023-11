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
package com.braintribe.utils.encoding;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.Ignore;
import org.junit.Test;

import com.braintribe.utils.IOTools;

public class UnicodeReaderTest {

	@Test
	public void testUnicodeInputStreamWithBOM() throws Exception {

		this.doTest("UTF-8", (byte) 0xEF, (byte) 0xBB, (byte) 0xBF);
		this.doTest("UTF-32BE", (byte) 0x00, (byte) 0x00, (byte) 0xFE, (byte) 0xFF);
		this.doTest("UTF-32LE", (byte) 0xFF, (byte) 0xFE, (byte) 0x00, (byte) 0x00);
		this.doTest("UTF-16BE", (byte) 0xFE, (byte) 0xFF);
		this.doTest("UTF-16LE", (byte) 0xFF, (byte) 0xFE);

	}

	@Test
	public void testUnicodeInputStreamWithoutBOM() throws Exception {

		this.doTest("UTF-8");

	}

	@Ignore
	protected void doTest(String encoding, byte... prefix) throws Exception {
		String expected = "Hello, world";
		UnicodeReader in = this.createContentReaderWithPrefix(expected, encoding, prefix);
		String actual = this.getContentBackFromReader(in);
		String actualEncoding = in.getEncoding();
		assertThat(actualEncoding).isEqualTo(encoding);
		assertThat(actual).isEqualTo(expected);
	}

	@Ignore
	protected String getContentBackFromReader(UnicodeReader in) throws Exception {
		String encoding = in.getEncoding();
		if (encoding == null) {
			encoding = "UTF-8";
		}
		String content = IOTools.slurp(in);
		return content;
	}

	@Ignore
	protected UnicodeReader createContentReaderWithPrefix(String content, String encoding, byte... prefix) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		baos.write(prefix);
		baos.write(content.getBytes(encoding));
		byte[] byteArray = baos.toByteArray();
		ByteArrayInputStream inStream = new ByteArrayInputStream(byteArray);
		UnicodeReader uReader = new UnicodeReader(inStream, "UTF-8");
		return uReader;
	}
}
