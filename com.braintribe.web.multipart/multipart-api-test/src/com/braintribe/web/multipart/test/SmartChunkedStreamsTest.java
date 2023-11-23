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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

import com.braintribe.testing.test.AbstractTest;
import com.braintribe.utils.IOTools;
import com.braintribe.web.multipart.impl.FormDataMultipartConstants;
import com.braintribe.web.multipart.streams.BufferlessChunkedInputStream;
import com.braintribe.web.multipart.streams.ChunkedOutputStream;

public class SmartChunkedStreamsTest extends AbstractTest implements FormDataMultipartConstants {

	@Test
	public void testSimpleInput() throws IOException {
		String testInput = "\na5\nThis \nb19\nis the input for the test\na0\n\naaaaaaaaaaaaaaaaaaaa\n27\nasd";
		String resultString = readChunked(testInput);

		assertThat(resultString).isEqualTo("This is the input for the test");

	}

	@Test
	public void testFaultyInput() {
		String testInputChunkDeclaredTooLong = "\na6\nThis \nb19\nis the input for the test\na0\n";
		String testInputChunkDeclaredTooLong2 = "\na7\nThis \nb19\nis the input for the test\na0\n";
		String testInputChunkDeclaredTooLong3 = "\nb17\nThis \nb19\nis the input for the test\na0\n";
		String testInputChunkDeclaredTooLong4 = "\na5\nThis \nb1A\nis the input for the test\na0\na";
		String testInputChunkDeclaredTooShort = "\na4\nThis \nb19\nis the input for the test\na0\n";
		String testInputChunkDeclaredTooShort2 = "\na1\nThis \nb19\nis the input for the test\na0\n";
		String testInputChunkDeclaredTooShort4 = "\na5\nThis \na1\nis the input for the test\na0\n";
		String testInputChunkWithoutCRLF = "\na5\nThis \nb19\nis the input for the test\r";
		String testInputChunkWithoutCRLF2 = "\na5\nThis \nb19\nis the input for the test";
		String testInputChunksWithoutEnd = "\na5\nThis \nb19\nis the input for the test\n";

		assertThatThrownBy(() -> readChunked(testInputChunkDeclaredTooLong)).isExactlyInstanceOf(IOException.class);
		assertThatThrownBy(() -> readChunked(testInputChunkDeclaredTooLong2)).isExactlyInstanceOf(IOException.class);
		assertThatThrownBy(() -> readChunked(testInputChunkDeclaredTooLong3)).isExactlyInstanceOf(IOException.class);
		assertThatThrownBy(() -> readChunked(testInputChunkDeclaredTooLong4)).isExactlyInstanceOf(IOException.class);
		assertThatThrownBy(() -> readChunked(testInputChunkDeclaredTooShort)).isExactlyInstanceOf(IOException.class);
		assertThatThrownBy(() -> readChunked(testInputChunkDeclaredTooShort2)).isExactlyInstanceOf(IOException.class);
		assertThatThrownBy(() -> readChunked(testInputChunkDeclaredTooShort4)).isExactlyInstanceOf(IOException.class);
		assertThatThrownBy(() -> readChunked(testInputChunkWithoutCRLF)).isExactlyInstanceOf(EOFException.class);
		assertThatThrownBy(() -> readChunked(testInputChunkWithoutCRLF2)).isExactlyInstanceOf(EOFException.class);

		assertThatThrownBy(() -> readChunked(testInputChunksWithoutEnd)).isExactlyInstanceOf(EOFException.class);
	}

	@Test
	public void testEdgeCases() throws IOException {
		String testInputChunkContainsCharsThatCanBeInterpretedAsChunkDeclarations = "\nb23\nThis \nb19\nis the input for the test\na0\n";

		String resultString = readChunked(testInputChunkContainsCharsThatCanBeInterpretedAsChunkDeclarations);
		assertThat(resultString).isEqualTo("This \nb19\nis the input for the test");

	}

	@Test
	public void testSimpleOutput() throws IOException {
		String testInput = "This is the input for the test";

		String result = writeChunked(testInput);

		assertThat(result).isEqualTo("\nb10\n" + "This is the inpu\n" + "b!E\n" + "t for the test");
	}

	@Test
	public void testCombination() throws IOException {
		String testInput = "This is the input for the test.";

		String result = readChunked(writeChunked(testInput));
		assertThat(result).isEqualTo(testInput);
	}
	@Test
	public void testNormalizedChunkSize() throws Exception {
		File file = new File("random-text.txt");
		MultipartTestUtils.writeRandomText(1_000_000, "--boundary^5", new FileOutputStream(file));
		
		String testInput = IOTools.slurp(file, "UTF-8");
		
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		ChunkedOutputStream chunkedOut = ChunkedOutputStream.instance(byteOut, IOTools.SIZE_8K, true);

		OutputStreamWriter writer = new OutputStreamWriter(chunkedOut, StandardCharsets.ISO_8859_1);

		writer.write(testInput);
		writer.close();

		String writeChunked = byteOut.toString();
		
		assertThat(writeChunked).startsWith("\n^0\n");
		
		String result = readChunked(writeChunked);
		assertThat(result).isEqualTo(testInput);
	}

	private String writeChunked(String input) throws IOException {
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		try (ChunkedOutputStream chunkedOut = ChunkedOutputStream.instance(byteOut, 16, true)){
			chunkedOut.write(input.getBytes());
		}
		
		String byteOutAsString = byteOut.toString();
		
		assertThat(byteOutAsString).isEqualTo(writeChunkedSingleBytes(input));
		
		return byteOutAsString;
	}
	
	private String writeChunkedSingleBytes(String input) throws IOException {
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		ChunkedOutputStream chunkedOut = ChunkedOutputStream.instance(byteOut, 16, true);
		
		 for (byte b: input.getBytes())
			 chunkedOut.write(b);
		 
		 chunkedOut.close();
		
		return byteOut.toString();
	}

	private String readChunked(String chunkedInput) throws IOException {
		InputStream in = new ByteArrayInputStream(chunkedInput.getBytes(StandardCharsets.ISO_8859_1));
		BufferlessChunkedInputStream chunkedIn = new BufferlessChunkedInputStream(in);

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
				BufferlessChunkedInputStream chunkedIn = new BufferlessChunkedInputStream(in);) {
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
