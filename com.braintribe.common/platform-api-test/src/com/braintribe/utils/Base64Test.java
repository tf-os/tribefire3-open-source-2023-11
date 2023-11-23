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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Random;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.io.BaseEncoding;

public class Base64Test {

	protected int runs = 100;
	protected int maxDataSize = 100_000;
	protected Random rnd = new Random();

	@Test
	public void testSimpleEncodeDecode() {
		String string = "abcdefghjklabcdefghjklabcdefghjklabcdefghjklabcdefghjklabcdefghjklabcdefghjklabcdefghjklabcdefghjklabcdefghjklabcdefghjklabcdefghjklabcdefghjklabcdefghjklabcdefghjklabcdefghjklabcdefghjklabcdefghjklabcdefghjklabcdefghjklabcdefghjklabcdefghjklabcdefghjklabcdefghjkl";
		String encoded = Base64.encodeString(string);
		String decoded = Base64.decodeToString(encoded);
		assertThat(decoded).isEqualTo(string);
	}

	@Test
	public void testEncodingOutputStreamWriter() throws Exception {

		for (int i = 0; i < this.runs; ++i) {

			byte[] data = this.generateRandomData();

			StringWriter btWriter = new StringWriter();
			OutputStream btOut = new Base64.OutputStream(btWriter, Base64.ENCODE | Base64.DONT_BREAK_LINES);
			btOut.write(data);
			btOut.flush();
			btOut.close();
			String btText = btWriter.toString().trim();

			BaseEncoding base64 = BaseEncoding.base64();
			StringWriter writer = new StringWriter();
			OutputStream out = base64.encodingStream(writer);
			out.write(data);
			out.flush();
			out.close();
			String guavaText = writer.toString().trim();

			Assert.assertEquals(guavaText, btText);
		}

	}

	@Test
	public void testEncodingOutputStreamWriterWithLineBreaks() throws Exception {

		for (int i = 0; i < this.runs; ++i) {

			byte[] data = this.generateRandomData();

			StringWriter btWriter = new StringWriter();
			OutputStream btOut = new Base64.OutputStream(btWriter, Base64.ENCODE);
			btOut.write(data);
			btOut.flush();
			btOut.close();
			String btText = btWriter.toString().trim();

			BaseEncoding base64 = BaseEncoding.base64().withSeparator("\n", 76);
			StringWriter writer = new StringWriter();
			OutputStream out = base64.encodingStream(writer);
			out.write(data);
			out.flush();
			out.close();
			String guavaText = writer.toString().trim();

			Assert.assertEquals(guavaText, btText);
		}

	}

	@Test
	public void testEncodingOutputStream() throws Exception {

		for (int i = 0; i < this.runs; ++i) {

			byte[] data = this.generateRandomData();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			OutputStream btOut = new Base64.OutputStream(baos, Base64.ENCODE | Base64.DONT_BREAK_LINES);
			btOut.write(data);
			btOut.flush();
			btOut.close();
			String btText = baos.toString("ASCII");

			BaseEncoding base64 = BaseEncoding.base64();
			StringWriter writer = new StringWriter();
			OutputStream out = base64.encodingStream(writer);
			out.write(data);
			out.flush();
			out.close();
			String guavaText = writer.toString().trim();

			Assert.assertEquals(guavaText, btText);
		}

	}

	@Test
	public void testDecodingInputStreamReader() throws Exception {

		for (int i = 0; i < this.runs; ++i) {

			byte[] data = this.generateRandomData();

			BaseEncoding base64 = BaseEncoding.base64();
			StringWriter writer = new StringWriter();
			OutputStream out = base64.encodingStream(writer);
			out.write(data);
			out.flush();
			out.close();
			String guavaText = writer.toString().trim();

			StringReader reader = new StringReader(guavaText);
			InputStream btIn = new Base64.InputStream(reader, Base64.DECODE);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			IOTools.pump(btIn, baos);
			btIn.close();
			baos.close();
			byte[] result = baos.toByteArray();

			assertThat(data).isEqualTo(result);
		}

	}

	@Test
	public void testDecodingInputStream() throws Exception {

		for (int i = 0; i < this.runs; ++i) {

			byte[] data = this.generateRandomData();

			BaseEncoding base64 = BaseEncoding.base64();
			StringWriter writer = new StringWriter();
			OutputStream out = base64.encodingStream(writer);
			out.write(data);
			out.flush();
			out.close();
			String guavaText = writer.toString().trim();

			InputStream encodedIn = new ByteArrayInputStream(guavaText.getBytes("ASCII"));
			InputStream btIn = new Base64.InputStream(encodedIn, Base64.DECODE);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			IOTools.pump(btIn, baos);
			btIn.close();
			baos.close();
			byte[] result = baos.toByteArray();

			assertThat(data).isEqualTo(result);
		}

	}

	@Ignore
	protected byte[] generateRandomData() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int size = this.rnd.nextInt(this.maxDataSize);
		for (int j = 0; j < size; ++j) {
			baos.write(this.rnd.nextInt());
		}
		byte[] data = baos.toByteArray();
		return data;
	}

	@Test
	public void testZeroTrailingData() throws Exception {
		int trailingZeroes = 6;
		byte[] data = this.generateRandomData();
		byte[] moredata = new byte[data.length + trailingZeroes];
		System.arraycopy(data, 0, moredata, 0, data.length);
		for (int i = 0; i < trailingZeroes; ++i) {
			moredata[data.length + i] = 0;
		}

		StringWriter writer = new StringWriter();
		int base64Options = Base64.DONT_BREAK_LINES | Base64.ENCODE;
		OutputStream out = new Base64.OutputStream(writer, base64Options);

		out.write(moredata);
		out.flush();
		out.close();

		String encoded = writer.toString();

		InputStream in = new Base64.InputStream(new StringReader(encoded), Base64.DECODE);

		byte[] decodedData = IOTools.slurpBytes(in);

		assertThat(decodedData).isEqualTo(moredata);
	}

	@Test
	public void testBin2EncodedValue() throws Exception {
		byte[] data = { 2, 0, 0, 0, 2, 0, 0, 0, 66, 99, 111, 109, 46, 98, 114, 97, 105, 110, 116, 114, 105, 98, 101, 46, 109, 111, 100, 101, 108, 46,
				115, 101, 99, 117, 114, 105, 116, 121, 115, 101, 114, 118, 105, 99, 101, 46, 99, 114, 101, 100, 101, 110, 116, 105, 97, 108, 115, 46,
				67, 108, 105, 101, 110, 116, 67, 114, 101, 100, 101, 110, 116, 105, 97, 108, 115, 0, 0, 0, 1, 0, 2, 0, 0, 0, 20, 99, 108, 105, 101,
				110, 116, 73, 100, 101, 110, 116, 105, 102, 105, 99, 97, 116, 105, 111, 110, 0, 1, 0, 9, 105, 115, 79, 110, 101, 80, 97, 115, 115, 0,
				1, 0, 84, 99, 111, 109, 46, 98, 114, 97, 105, 110, 116, 114, 105, 98, 101, 46, 109, 111, 100, 101, 108, 46, 115, 101, 99, 117, 114,
				105, 116, 121, 115, 101, 114, 118, 105, 99, 101, 46, 99, 114, 101, 100, 101, 110, 116, 105, 97, 108, 115, 46, 105, 100, 101, 110, 116,
				105, 102, 105, 99, 97, 116, 105, 111, 110, 46, 67, 108, 105, 101, 110, 116, 73, 100, 101, 110, 116, 105, 102, 105, 99, 97, 116, 105,
				111, 110, 0, 0, 0, 1, 0, 1, 0, 0, 0, 10, 101, 120, 116, 101, 114, 110, 97, 108, 73, 100, 0, 0, 0, 0, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1,
				0, 0, 0, 0, 0, 0, 26, 0, 1, 0, 0, 0, 0, 33, -1, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 24, 0, 0, 0, 23, 65, 99, 116, 105, 118, 101, 77, 113,
				83, 101, 114, 118, 101, 114, 67, 97, 114, 116, 114, 105, 100, 103, 101, -1, 0, 0, 0, 0, 0 };

		StringWriter writer = new StringWriter();
		int base64Options = Base64.DONT_BREAK_LINES | Base64.ENCODE;
		OutputStream out = new Base64.OutputStream(writer, base64Options);

		out.write(data);
		out.flush();
		out.close();

		String encoded = writer.toString();

		InputStream in = new Base64.InputStream(new StringReader(encoded), Base64.DECODE);

		byte[] decodedData = IOTools.slurpBytes(in);

		assertThat(decodedData).isEqualTo(data);

	}

	@Test
	public void testDecode() {
		String decoded = "Hello, world!";
		String encoded = "SGVsbG8sIHdvcmxkIQ==";

		assertThat(Base64.decodeToString(encoded)).isEqualTo(decoded);
	}
}
