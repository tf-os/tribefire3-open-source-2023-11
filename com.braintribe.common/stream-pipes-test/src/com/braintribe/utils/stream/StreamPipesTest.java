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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.testing.category.Slow;
import com.braintribe.testing.category.VerySlow;
import com.braintribe.testing.test.AbstractTest;
import com.braintribe.testing.tools.TestTools;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.stream.api.StreamPipe;
import com.braintribe.utils.stream.api.StreamPipeFactory;
import com.braintribe.utils.stream.api.StreamPipes;

public class StreamPipesTest extends AbstractTest {

	@Test
	public void testHelloWorld() throws Exception {
		StreamPipeFactory streamPipeFactory = StreamPipes.simpleFactory();

		StreamPipe pipe = streamPipeFactory.newPipe("test");

		try (OutputStream os = pipe.openOutputStream()) {
			os.write("Hello, world".getBytes(StandardCharsets.UTF_8));
		}

		byte[] buffer = new byte[1024];
		try (InputStream in = pipe.openInputStream()) {
			int read = IOTools.readFully(in, buffer);
			String result = new String(buffer, 0, read, StandardCharsets.UTF_8);

			assertThat(result).isEqualTo("Hello, world");
		}

	}

	@Test
	public void testMediumSize() throws Exception {
		test(1 << 20);

	}

	@Test
	@Category(Slow.class)
	public void testHugeSize() throws Exception {
		test(1 << 30);
	}

	/**
	 * This test takes several minutes the first time it's executed - however it's an important one because it't the
	 * only one that handles data with a length larger than Integer.MAX_VALUE and also longer than a typical JVM's
	 * memory amount
	 */
	@Test
	@Category(VerySlow.class)
	@Ignore
	public void testJumboSize() throws Exception {
		test(1L << 33);
	}

	private void test(long minfileSize) throws IOException, UnsupportedEncodingException, FileNotFoundException {
		File file = new File("data/test-data_" + minfileSize + ".txt");
		if (!file.exists()) {
			System.out.println("Writing to file ~bytes " + minfileSize);
			MultipartTestUtils.writeRandomTextFile(file, minfileSize);
		}

		StreamPipeFactory streamPipeFactory = StreamPipes.simpleFactory();

		StreamPipe pipe = streamPipeFactory.newPipe("test", IOTools.SIZE_32K);

		System.out.println("Writing to pipe ~bytes " + minfileSize);

		long amountWritten;
		try (OutputStream os = pipe.openOutputStream(); InputStream is = new BufferedInputStream(new FileInputStream(file), IOTools.SIZE_32K)) {
			amountWritten = IOTools.transferBytes(is, os, () -> new byte[16]);
		}

		System.out.println("Reading from pipe bytes " + amountWritten);
		try (InputStream in = pipe.openInputStream()) {
			long read = IOTools.consume(in);

			assertThat(read).as("Did not read same amount of bytes as were written.").isEqualTo(amountWritten);
		}

		if (TestTools.isCiEnvironment()) {
			file.delete();
		}
	}
}
