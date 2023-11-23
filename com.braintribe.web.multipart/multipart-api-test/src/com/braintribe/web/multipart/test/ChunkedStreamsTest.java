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
package com.braintribe.web.multipart.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

import com.braintribe.testing.test.AbstractTest;
import com.braintribe.web.multipart.impl.FormDataMultipartConstants;
import com.braintribe.web.multipart.streams.ChunkedInputStream;
import com.braintribe.web.multipart.streams.ChunkedOutputStream;

public class ChunkedStreamsTest extends AbstractTest implements FormDataMultipartConstants {

	@Test
	public void testSimpleInput() throws IOException {
		String testInput = "5\r\nThis \r\n19\r\nis the input for the test\r\n0\r\n\r\naaaaaaaaaaaaaaaaaaaa\r\n27\r\nasd";
		String resultString = readChunked(testInput);

		assertThat(resultString).isEqualTo("This is the input for the test");

	}

	@Test
	public void testFaultyInput() {
		String testInputChunkDeclaredTooLong = "6\r\nThis \r\n19\r\nis the input for the test\r\n0\r\n";
		String testInputChunkDeclaredTooLong2 = "7\r\nThis \r\n19\r\nis the input for the test\r\n0\r\n";
		String testInputChunkDeclaredTooLong3 = "17\r\nThis \r\n19\r\nis the input for the test\r\n0\r\n";
		String testInputChunkDeclaredTooLong4 = "5\r\nThis \r\n1A\r\nis the input for the test\r\n0\r\n";
		String testInputChunkDeclaredTooShort = "4\r\nThis \r\n19\r\nis the input for the test\r\n0\r\n";
		String testInputChunkDeclaredTooShort2 = "1\r\nThis \r\n19\r\nis the input for the test\r\n0\r\n";
		String testInputChunkDeclaredTooShort4 = "5\r\nThis \r\n1\r\nis the input for the test\r\n0\r\n";
		String testInputChunkWithoutCRLF = "5\r\nThis \r\n19\r\nis the input for the test\r";
		String testInputChunkWithoutCRLF2 = "5\r\nThis \r\n19\r\nis the input for the test";
		String testInputChunksWithoutEnd = "5\r\nThis \r\n19\r\nis the input for the test\r\n";

		assertThatThrownBy(() -> readChunked(testInputChunkDeclaredTooLong)).isExactlyInstanceOf(IllegalStateException.class);
		assertThatThrownBy(() -> readChunked(testInputChunkDeclaredTooLong2)).isExactlyInstanceOf(IllegalStateException.class);
		assertThatThrownBy(() -> readChunked(testInputChunkDeclaredTooLong3)).isExactlyInstanceOf(IllegalStateException.class);
		assertThatThrownBy(() -> readChunked(testInputChunkDeclaredTooLong4)).isExactlyInstanceOf(IllegalStateException.class);
		assertThatThrownBy(() -> readChunked(testInputChunkDeclaredTooShort)).isExactlyInstanceOf(IllegalStateException.class);
		assertThatThrownBy(() -> readChunked(testInputChunkDeclaredTooShort2)).isExactlyInstanceOf(IllegalStateException.class);
		assertThatThrownBy(() -> readChunked(testInputChunkDeclaredTooShort4)).isExactlyInstanceOf(IllegalStateException.class);
		assertThatThrownBy(() -> readChunked(testInputChunkWithoutCRLF)).isExactlyInstanceOf(EOFException.class);
		assertThatThrownBy(() -> readChunked(testInputChunkWithoutCRLF2)).isExactlyInstanceOf(EOFException.class);

		assertThatThrownBy(() -> readChunked(testInputChunksWithoutEnd)).isExactlyInstanceOf(EOFException.class);
	}

	@Test
	public void testEdgeCases() throws IOException {
		String testInputChunkContainsCharsThatCanBeInterpretedAsChunkDeclarations = "24\r\nThis \r\n19\r\nis the input for the test\r\n0\r\n";

		String resultString = readChunked(testInputChunkContainsCharsThatCanBeInterpretedAsChunkDeclarations);
		assertThat(resultString).isEqualTo("This \r\n19\r\nis the input for the test");

	}

	@Test
	public void testSimpleOutput() throws IOException {
		String testInput = "This is the input for the test";

		String result = writeChunked(testInput);

		assertThat(result).isEqualTo("10\r\n" + "This is the inpu\r\n" + "E\r\n" + "t for the test\r\n" + "0\r\n");
	}

	@Test
	public void testCombination() throws IOException {
		String testInput = "This is the input for the test.";

		String result = readChunked(writeChunked(testInput));
		assertThat(result).isEqualTo(testInput);

	}

	private String writeChunked(String input) throws IOException {
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		ChunkedOutputStream chunkedOut = ChunkedOutputStream.instance(byteOut, 16, false);

		OutputStreamWriter writer = new OutputStreamWriter(chunkedOut, StandardCharsets.ISO_8859_1);

		writer.write(input);
		writer.close();

		String byteOutAsString = byteOut.toString();
		
		assertThat(byteOutAsString).isEqualTo(writeChunkedSingleBytes(input));
		
		return byteOutAsString;
	}
	
	private String writeChunkedSingleBytes(String input) throws IOException {
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		ChunkedOutputStream chunkedOut = ChunkedOutputStream.instance(byteOut, 16, false);
		
		 for (byte b: input.getBytes())
			 chunkedOut.write(b);
		 
		 chunkedOut.close();
		
		return byteOut.toString();
	}

	private String readChunked(String chunkedInput) throws IOException {
		InputStream in = new ByteArrayInputStream(chunkedInput.getBytes(StandardCharsets.ISO_8859_1));
		ChunkedInputStream chunkedIn = new ChunkedInputStream(in);

		try (InputStreamReader reader = new InputStreamReader(chunkedIn)) {
			char[] resultBuffer = new char[chunkedInput.length()];

			int num = 0;
			do {
				num += reader.read(resultBuffer, num, chunkedInput.length() - num);
			} while(num != -1);

			String result = new String(resultBuffer).trim();
			
			assertThat(result).isEqualTo(readChunkedSingleBytes(chunkedInput));
			
			return result;
		}
	}
	
	private String readChunkedSingleBytes(String chunkedInput) throws IOException {
		try (InputStream in = new ByteArrayInputStream(chunkedInput.getBytes(StandardCharsets.ISO_8859_1));
				ChunkedInputStream chunkedIn = new ChunkedInputStream(in);) {
			byte[] resultBuffer = new byte[chunkedInput.length()];

			for (int i=0; i<resultBuffer.length; i++) {
				int readByte = chunkedIn.read();
				
				if (readByte == -1)
					break;
				
				resultBuffer[i] = (byte) readByte;
			}

			return new String(resultBuffer).trim();
		}
	}

}
