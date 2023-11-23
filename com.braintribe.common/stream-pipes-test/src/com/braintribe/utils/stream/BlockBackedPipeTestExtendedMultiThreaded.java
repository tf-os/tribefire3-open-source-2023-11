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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.braintribe.logging.Logger;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.stream.api.StreamPipe;
import com.braintribe.utils.stream.pools.CompoundBlockPool;
import com.braintribe.utils.stream.pools.CompoundBlockPoolBuilder;

public class BlockBackedPipeTestExtendedMultiThreaded {

	private static final int NUMBER_OF_THREADS = 30;
	private static final int NUMBER_OF_INMEMORY_BLOCKS_PER_POOL = 10;

	public static final Logger log = Logger.getLogger(BlockBackedPipeTestExtendedMultiThreaded.class);

	private static ExecutorService executor;

	private static byte[] FIRST_LINE = "first line".getBytes();
	private static byte[] SECOND_LINE = "second line".getBytes();

	private CompoundBlockPool blockPool;

	@ClassRule
	public static TemporaryFolder tempFolder = new TemporaryFolder();

	@BeforeClass
	public static void init() {
		executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
	}

	@AfterClass
	public static void destroy() {
		ExecutorService e = executor;
		if (e != null) {
			e.shutdownNow();
			executor = null;
		}
	}

	@Before
	public void initBlockPool() {
		blockPool = CompoundBlockPoolBuilder.start() //
				.appendInMemoryBlockPool(15, NUMBER_OF_INMEMORY_BLOCKS_PER_POOL) //
				.appendInMemoryBlockPool(15, NUMBER_OF_INMEMORY_BLOCKS_PER_POOL) //
				.build();
	}

	@Test
	public void multiThreadedTest() throws InterruptedException, ExecutionException, IOException {
		File dataDir = new File("data");

		if (dataDir.exists()) {
			FileTools.deleteDirectoryRecursively(dataDir);
		}

		blockPool = CompoundBlockPoolBuilder.start() //
				.appendInMemoryBlockPool(IOTools.SIZE_64K, NUMBER_OF_THREADS) //
				.appendSoftReferencedInMemoryBlockPool(1_000_000, NUMBER_OF_THREADS) //
				.appendDynamicFileBlockPool(dataDir, NUMBER_OF_THREADS) //
				.build();

		StreamPipe pipe = blockPool.newPipe("test");

		File dataFile = new File(dataDir, "data");
		dataFile.getParentFile().mkdirs();

		try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(dataFile.toPath()), IOTools.SIZE_32K)) {
			MultipartTestUtils.writeRandomText(10_000_000, out);
		}

		List<Future<File>> futures = new ArrayList<>();

		// execute before reading threads
		for (int i = 0; i < NUMBER_OF_THREADS/2; i++) {
			futures.add(executor.submit(() -> read(pipe)));
		}

		// execute writing thread
		executor.execute(() -> {
			try {
				try (InputStream in = Files.newInputStream(dataFile.toPath()); OutputStream out = pipe.openOutputStream()) {
					IOTools.transferBytes(in, out);
				}
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});

		// execute after reading threads
		for (int i = 0; i < NUMBER_OF_THREADS/2; i++) {
			futures.add(executor.submit(() -> read(pipe)));
		}

		for (Future<File> future : futures) {
			File file = future.get();

			assertThat(file).as("Unexpected file content").hasSameTextualContentAs(dataFile);
		}
	}

	private File read(StreamPipe pipe) {
		try {
			return readChecked(pipe);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	private File readAndClose(StreamPipe p) {
		try(StreamPipe pipe = p) {
			return readChecked(pipe);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private File readChecked(StreamPipe pipe) throws IOException {
		String fileName = UUID.randomUUID().toString();
		File targetFile = new File("data/" + fileName);

		try (InputStream in = pipe.openInputStream(); OutputStream out = Files.newOutputStream(targetFile.toPath())) {
			IOTools.transferBytes(in, out);
		}

		return targetFile;
	}

	@Test
	public void testPipeClosing() throws Exception {
		
		// 2 pools with only one 15-byte block each
		blockPool = CompoundBlockPoolBuilder.start() //
				.appendInMemoryBlockPool(15, 1) //
				.appendInMemoryBlockPool(15, 1) //
				.build();
		
		StreamPipe pipe = blockPool.newPipe("test");
		
		OutputStream out = pipe.openOutputStream();
		out.write(FIRST_LINE);
		out.write(SECOND_LINE);
		out.close();
		
		InputStream in = pipe.openInputStream();
		in.close();
		
		assertThatThrownBy(() -> {
			StreamPipe newPipe = blockPool.newPipe("test");
			OutputStream o = newPipe.openOutputStream();
			o.write(0); // Write anything to trigger Block-acquiration (which should fail because there aren't any left)
		}).isExactlyInstanceOf(IllegalStateException.class);
		
		// Closing the pipe instantly frees the Blocks so now we can write again to any new pipe
		pipe.close();
		
		StreamPipe pipe2 = blockPool.newPipe("test");
		
		out = pipe2.openOutputStream();
		out.write(FIRST_LINE);
		out.write(SECOND_LINE);
		
		// Closing the pipe should fail now because there is still an open output stream
		assertThatThrownBy(pipe2::close).isExactlyInstanceOf(IllegalStateException.class);
		
		out.close();
		in = pipe2.openInputStream();
		
		// Closing the pipe should fail now because there is still an open input stream
		assertThatThrownBy(pipe2::close).isExactlyInstanceOf(IllegalStateException.class);
		
		in.close();
		
		// Closing the pipe should now succeed
		pipe2.close();
		
		// Opening InputStreams should not be possible any more
		assertThatThrownBy(pipe2::openInputStream).isExactlyInstanceOf(IllegalStateException.class);

		// The OutputStream of the pipe should be closed for good.
		assertThatThrownBy(() -> pipe2.acquireOutputStream()).isExactlyInstanceOf(IllegalStateException.class);
	}
	
	@Test
	public void testMultiThreadedPipeClosing() throws InterruptedException, ExecutionException, IOException {
		File dataDir = new File("data");

		if (dataDir.exists()) {
			FileTools.deleteDirectoryRecursively(dataDir);
		}

		// Using very small sizes on purpose so that GC won't be triggered
		blockPool = CompoundBlockPoolBuilder.start() //
				.appendInMemoryBlockPool(10, NUMBER_OF_THREADS) //
				.appendSoftReferencedInMemoryBlockPool(10, NUMBER_OF_THREADS) //
				.appendDynamicFileBlockPool(dataDir, NUMBER_OF_THREADS) //
				.build();


		File dataFile = new File(dataDir, "data");
		dataFile.getParentFile().mkdirs();

		try (OutputStream out = Files.newOutputStream(dataFile.toPath())) {
			MultipartTestUtils.writeRandomText(30, out);
		}

		List<Future<File>> futures = new ArrayList<>();

		for (int i = 0; i < NUMBER_OF_THREADS * 10; i++) {
			StreamPipe pipe = blockPool.newPipe("test");
			futures.add(executor.submit(() -> readAndClose(pipe)));
			
			executor.execute(() -> {
				try {
					try (InputStream in = Files.newInputStream(dataFile.toPath()); OutputStream out = pipe.openOutputStream()) {
						IOTools.transferBytes(in, out);
					}
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			});
		}

		for (Future<File> future : futures) {
			File file = future.get();

			assertThat(file).as("Unexpected file content").hasSameTextualContentAs(dataFile);
		}
	}
}

class MultipartTestUtils {
	private static String[] words = { "one", "two", "three", "four", "five", "six", "seven", "eight", "nine" };

	private MultipartTestUtils() {
		// Prevent from instantiating the class
	}

	enum PartStreamingMethod {
		chunked,
		contentLengthAware,
		raw
	}

	static void writeRandomData(OutputStream out, long byteCount) throws IOException {
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

	static long writeRandomText(long minfileSize, OutputStream out) throws IOException, UnsupportedEncodingException, FileNotFoundException {
		Random random = new Random(System.currentTimeMillis());
		long amountWritten = 0;
		int lineWordsWritten = 0;
		try (Writer writer = new OutputStreamWriter(out, "UTF-8")) {
			while (amountWritten < minfileSize) {

				if (lineWordsWritten > 20) {
					writer.write('\n');
					lineWordsWritten = 0;
					amountWritten++;
				} else if (lineWordsWritten > 0) {
					writer.write(' ');
					amountWritten++;
				}

				String word;

				int index = random.nextInt(words.length);
				word = words[index];

				writer.write(word);
				amountWritten += word.length();
				lineWordsWritten++;
			}
		}

		return amountWritten;
	}

	static void writeRandomDataFile(File generatedFile, long byteCount) {
		try (OutputStream out = new BufferedOutputStream(new FileOutputStream(generatedFile), IOTools.SIZE_32K)) {
			writeRandomData(out, byteCount);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	static void writeRandomTextFile(File generatedFile, long byteCount) {
		try (OutputStream out = new BufferedOutputStream(new FileOutputStream(generatedFile), IOTools.SIZE_32K)) {
			writeRandomText(byteCount, out);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

}
