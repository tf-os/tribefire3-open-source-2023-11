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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Random;

import com.braintribe.utils.IOTools;

class MultipartTestUtils {
	private static String[] words = { "one", "two", "three", "four", "five", "six", "seven", "eight", "nine" };

	private MultipartTestUtils() {
		// Prevent from instantiating the class
	}

	static enum PartStreamingMethod {
		chunked, contentLengthAware, raw
	}

	static void writeRandomData(OutputStream out, int byteCount) throws IOException {
		byte buffer[] = IOTools.BUFFER_SUPPLIER_8K.get();

		Random r = new Random(0);

		int bufferBytesUsed = 0;
		for (int i = 0; i < byteCount; i++) {
			buffer[bufferBytesUsed++] = (byte) r.nextInt(255);

			if (bufferBytesUsed == IOTools.SIZE_8K) {
				out.write(buffer);
				bufferBytesUsed = 0;
			}
		}

		if (bufferBytesUsed > 0) {
			out.write(buffer, 0, bufferBytesUsed);
		}

	}

	static void writeRandomText(int minfileSize, String boundary, OutputStream out)
			throws IOException, UnsupportedEncodingException, FileNotFoundException {
		Random random = new Random(System.currentTimeMillis());
		int amountWritten = 0;
		int lineWordsWritten = 0;
		int boundariesWritten = 0;
		try (Writer writer = new OutputStreamWriter(out, "UTF-8")) {
			while (amountWritten < minfileSize) {

				if (lineWordsWritten > 20) {
					writer.write('\n');
					lineWordsWritten = 0;
				} else if (lineWordsWritten > 0) {
					writer.write(' ');
				}

				String word;

				if (random.nextInt(100) == 1) {
					int boundaryChars = boundariesWritten++ % (boundary.length() - 2);
					word = "\r\n" + boundary.substring(0, boundaryChars);
				} else {
					int index = random.nextInt(words.length);
					word = words[index];
				}

				writer.write(word);
				amountWritten += word.length();
				lineWordsWritten++;
			}
		}
	}

	static void writeRandomDataFile(File generatedFile, int byteCount) {
		try (OutputStream out = new FileOutputStream(generatedFile)) {
			writeRandomData(out, byteCount);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
