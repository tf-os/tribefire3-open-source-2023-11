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
import java.io.InputStream;

import org.junit.Ignore;
import org.junit.Test;

import com.braintribe.utils.IOTools;

public class UnicodeInputStreamTest {

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
		InputStream in = this.createContentStreamWithPrefix(expected, encoding, prefix);
		UnicodeInputStream uis = new UnicodeInputStream(in, "UTF-8");
		String actual = this.getContentBackFromStream(uis);
		String actualEncoding = uis.getEncoding();
		assertThat(actualEncoding).isEqualTo(encoding);
		assertThat(actual).isEqualTo(expected);
	}

	@Ignore
	protected String getContentBackFromStream(UnicodeInputStream uis) throws Exception {
		String encoding = uis.getEncoding();
		if (encoding == null) {
			encoding = "UTF-8";
		}
		String content = IOTools.slurp(uis, encoding);
		return content;
	}

	@Ignore
	protected InputStream createContentStreamWithPrefix(String content, String encoding, byte... prefix) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		baos.write(prefix);
		baos.write(content.getBytes(encoding));
		byte[] byteArray = baos.toByteArray();
		return new ByteArrayInputStream(byteArray);
	}
}
