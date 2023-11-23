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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.BeforeClass;
import org.junit.Test;

import com.braintribe.exception.Exceptions;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.lcd.StopWatch;
import com.braintribe.utils.stream.api.StreamPipe;
import com.braintribe.utils.stream.api.StreamPipes;
import com.braintribe.web.multipart.streams.BoundaryAwareInputStream;
import com.braintribe.web.multipart.streams.BufferlessChunkedInputStream;
import com.braintribe.web.multipart.streams.ChunkedInputStream;
import com.braintribe.web.multipart.streams.ChunkedOutputStream;
import com.braintribe.web.multipart.streams.ContentLengthAwareInputStream;

public class StreamsPerformanceTest {
	private static final int RANDOM_FILE_SIZE = 100_000_000;
	private static File dataDir;
	ExecutorService newCachedThreadPool = Executors.newFixedThreadPool(1);

	String boundaryPart = "boundary-" + UUID.randomUUID() + UUID.randomUUID() + UUID.randomUUID();

	@BeforeClass
	public static void init() throws IOException {
		dataDir = new File("data");

		if (dataDir.exists())
			FileTools.deleteDirectoryRecursively(dataDir);

		dataDir.mkdirs();

	}

	private void testInputStreamPerformance(File testData, Function<InputStream, InputStream> inputStreamProvider) throws IOException {
		byte[] b = IOTools.BUFFER_SUPPLIER_16K.get();

		StopWatch stopWatch = new StopWatch();
		String inputStreamClassSimpleName;
		try (InputStream fin = new BufferedInputStream(new FileInputStream(testData), IOTools.SIZE_64K);
				InputStream testIn = inputStreamProvider.apply(fin)) {

			while (testIn.read(b) != -1) {
				// just consume data
			}
			inputStreamClassSimpleName = testIn.getClass().getSimpleName();
		}
		System.out.println("Reading with " + inputStreamClassSimpleName + " took ......... " + stopWatch.getElapsedTime() + "ms.");

	}

	private void testOutputStreamPerformance(File testData, Function<OutputStream, OutputStream> outputStreamProvider)
			throws FileNotFoundException, IOException {

		if (testData.exists())
			testData.delete();

		try (OutputStream fileOut = new BufferedOutputStream(new FileOutputStream(testData), IOTools.SIZE_8K);
				OutputStream testOut = outputStreamProvider.apply(fileOut)) {
			StopWatch stopWatch = new StopWatch();
			// MultipartTestUtils.writeRandomText(RANDOM_FILE_SIZE, boundaryPart, testOut);
			MultipartTestUtils.writeRandomData(testOut, RANDOM_FILE_SIZE);

			System.out.println("Writing with " + testOut.getClass().getSimpleName() + " took ..... " + stopWatch.getElapsedTime() + "ms.");

		}
	}

	private void testChunkedStreamCombinationPerformance(int chunkSize) throws IOException {
		File chunkedRandomFile = new File(dataDir, "chunked-random.bin");

		System.out.println("Chunk size: " + chunkSize);

		testOutputStreamPerformance(chunkedRandomFile, out -> ChunkedOutputStream.instance(out, chunkSize, false));
		testInputStreamPerformance(chunkedRandomFile, in -> new ChunkedInputStream(in));

		System.out.println("----------\n");

	}

	@Test
	public void compareStreams() throws Exception {
		File randomDataFile = new File(dataDir, "simple-random.bin");
		File randomDataFileChunked = new File(dataDir, "chunked-random.bin");
		File boundaryRandomFile = new File(dataDir, "boundary-random.bin");

		String finalBoundaryWithCRLF = "\r\n--" + boundaryPart + "--\r\n";

		System.out.println("=== Chunked ===");
		testChunkedStreamCombinationPerformance(IOTools.SIZE_16K);
		testChunkedStreamCombinationPerformance(IOTools.SIZE_64K);
		testChunkedStreamCombinationPerformance(IOTools.SIZE_8K);
		testChunkedStreamCombinationPerformance(IOTools.SIZE_64K - 1);
		testChunkedStreamCombinationPerformance(Integer.MAX_VALUE);

		System.out.println();
		System.out.println("=== Other ===");
		testOutputStreamPerformance(randomDataFile, out -> out);
		testOutputStreamPerformance(randomDataFileChunked, out -> ChunkedOutputStream.instance(out, IOTools.SIZE_16K, true));
		FileTools.copyFile(randomDataFile, boundaryRandomFile);
		Files.write(boundaryRandomFile.toPath(), finalBoundaryWithCRLF.getBytes(), StandardOpenOption.APPEND);

		testInputStreamPerformance(randomDataFile, in -> in);
		testInputStreamPerformance(randomDataFile, in -> new ContentLengthAwareInputStream(in, randomDataFile.length()));
		testInputStreamPerformance(boundaryRandomFile, in -> new BoundaryAwareInputStream(in, boundaryPart.getBytes()));
		testInputStreamPerformance(randomDataFileChunked, in -> new BufferlessChunkedInputStream(in));
		testInputStreamPerformance(randomDataFileChunked, in -> new BufferlessChunkedInputStream(in));
		testInputStreamPerformance(randomDataFileChunked, in -> fileBackedInputStream(new BufferlessChunkedInputStream(in)));
		testInputStreamPerformance(randomDataFile, in -> fileBackedInputStream(new ContentLengthAwareInputStream(in, randomDataFile.length())));
		testInputStreamPerformance(boundaryRandomFile, in -> fileBackedInputStream(new BoundaryAwareInputStream(in, boundaryPart.getBytes())));
	}

	public void feedFrom(InputStream in, StreamPipe fileBackedPipe) {
		newCachedThreadPool.execute(() -> {
			x(in, fileBackedPipe);
		});
	}

	private void x(InputStream in, StreamPipe fileBackedPipe) {
		try (OutputStream out = fileBackedPipe.openOutputStream()) {
			transferBytes(in, out, () -> new byte[0x10000]);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static long transferBytes(InputStream inputStream, OutputStream outputStream, Supplier<byte[]> bufferSupplier) {

		byte[] buffer = bufferSupplier.get();

		int count = 0;
		long totalCount = 0;

		try {
			while ((count = inputStream.read(buffer)) != -1) {

				outputStream.write(buffer, 0, count);

				totalCount += count;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return totalCount;

	}
	public InputStream fileBackedInputStream(InputStream in) {

		StreamPipe fileBackedPipe = StreamPipes.simpleFactory().newPipe("in");

		feedFrom(in, fileBackedPipe);

		try {
			return fileBackedPipe.openInputStream();
		} catch (IOException e) {
			throw Exceptions.unchecked(e, "Could not openn pipe input stream");
		}
	}

	@Test
	public void testFilePerformance() throws Exception {
		int meg10 = 100_000_000;
		int buferSize = 0x2000;

		File file = new File("file.txt");

		StopWatch stopWatch = new StopWatch();
		RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
		randomAccessFile.setLength(meg10);
		System.out.println("Allocation took " + stopWatch.getElapsedTime() + "ms");

		// OutputStream outputStream = new FileOutputStream(new File("file.txt"));
		// OutputStream outputStream = new RandomAccessFileOutputStream(randomAccessFile);
		OutputStream outputStream = new FileOutputStream(randomAccessFile.getFD());
		byte[] buffer = new byte[buferSize];

		int x = meg10 / buferSize;
		int r = meg10 % buferSize;

		stopWatch = new StopWatch();
		for (int i = 0; i < x; i++) {
			outputStream.write(buffer);
		}

		System.out.println("Opening InputStream");
		FileInputStream fis = new FileInputStream(file);
		fis.read();

		outputStream.write(buffer, 0, r);
		System.out.println("Writing took " + stopWatch.getElapsedTime() + "ms");

	}

}
