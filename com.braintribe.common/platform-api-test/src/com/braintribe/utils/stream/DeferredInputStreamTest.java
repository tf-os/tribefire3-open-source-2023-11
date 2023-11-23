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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.CharConversionException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.braintribe.common.lcd.Pair;
import com.braintribe.logging.Logger;
import com.braintribe.provider.Holder;

/**
 * <p>
 * {@link DeferredInputStream} unit tests.
 *
 * <ul>
 * <li><em>Early bind tests:</em> The delegate is set to the {@code DeferredInputStream} before any attempt is made on reading from it.
 *
 * <li><em>Late bind tests:</em> The delegate is set to the {@code DeferredInputStream} after a thread already attempts to read from it.
 * </ul>
 *
 */
@SuppressWarnings("resource")
public class DeferredInputStreamTest {

	public static final Logger log = Logger.getLogger(DeferredInputStreamTest.class);

	public static final int bufferSize = 8192;
	public static final int dataSize = 6400000;
	public static final int minimalDataSize = 64000;

	private static ExecutorService executor;

	@ClassRule
	public static final TemporaryFolder tempFolder = new TemporaryFolder();

	@BeforeClass
	public static void init() {
		executor = Executors.newCachedThreadPool();
	}

	@AfterClass
	public static void destroy() {
		ExecutorService e = executor;
		if (e != null) {
			e.shutdownNow();
			executor = null;
		}
	}

	@Test
	public void testLateBindingOfRepeatableIncrementalDiskDelegate() throws Exception {
		testDeferredInputStream(true, true, true, false, false);
	}

	@Test
	public void testLateBindingOfRepeatableIncrementalDiskDelegateWithBufferedRead() throws Exception {
		testDeferredInputStream(true, true, true, true, false);
	}

	@Test
	public void testLateBindingOfRepeatableNonIncrementalDiskDelegate() throws Exception {
		testDeferredInputStream(true, true, false, false, false);
	}

	@Test
	public void testLateBindingOfRepeatableNonIncrementalDiskDelegateWithBufferedRead() throws Exception {
		testDeferredInputStream(true, true, false, true, false);
	}

	@Test
	public void testLateBindingOfNonRepeatableIncrementalDiskDelegate() throws Exception {
		testDeferredInputStream(true, false, true, false, false);
	}

	@Test
	public void testLateBindingOfNonRepeatableIncrementalDiskDelegateWithBufferedRead() throws Exception {
		testDeferredInputStream(true, false, true, true, false);
	}

	@Test
	public void testLateBindingOfNonRepeatableNonIncrementalDiskDelegate() throws Exception {
		testDeferredInputStream(true, false, false, false, false);
	}

	@Test
	public void testLateBindingOfNonRepeatableNonIncrementalDiskDelegateWithBufferedRead() throws Exception {
		testDeferredInputStream(true, false, false, true, false);
	}

	@Test
	public void testLateBindingOfRepeatableNonIncrementalInMemoryDelegate() throws Exception {
		testDeferredInputStream(true, true, false, false, true);
	}

	@Test
	public void testLateBindingOfRepeatableNonIncrementalInMemoryDelegateWithBufferedRead() throws Exception {
		testDeferredInputStream(true, true, false, true, true);
	}

	@Test
	public void testLateBindingOfNonRepeatableIncrementalInMemoryDelegate() throws Exception {
		testDeferredInputStream(true, false, true, false, true);
	}

	@Test
	public void testLateBindingOfNonRepeatableIncrementalInMemoryDelegateWithBufferedRead() throws Exception {
		testDeferredInputStream(true, false, true, true, true);
	}

	@Test
	public void testLateBindingOfNonRepeatableNonIncrementalInMemoryDelegate() throws Exception {
		testDeferredInputStream(true, false, false, false, true);
	}

	@Test
	public void testLateBindingOfNonRepeatableNonIncrementalInMemoryDelegateWithBufferedRead() throws Exception {
		testDeferredInputStream(true, false, false, true, true);
	}

	@Test
	public void testEarlyBindingOfRepeatableIncrementalDiskDelegate() throws Exception {
		testDeferredInputStream(false, true, true, false, false);
	}

	@Test
	public void testEarlyBindingOfRepeatableIncrementalDiskDelegateWithBufferedRead() throws Exception {
		testDeferredInputStream(false, true, true, true, false);
	}

	@Test
	public void testEarlyBindingOfRepeatableNonIncrementalDiskDelegate() throws Exception {
		testDeferredInputStream(false, true, false, false, false);
	}

	@Test
	public void testEarlyBindingOfRepeatableNonIncrementalDiskDelegateWithBufferedRead() throws Exception {
		testDeferredInputStream(false, true, false, true, false);
	}

	@Test
	public void testEarlyBindingOfNonRepeatableIncrementalDiskDelegate() throws Exception {
		testDeferredInputStream(false, false, true, false, false);
	}

	@Test
	public void testEarlyBindingOfNonRepeatableIncrementalDiskDelegateWithBufferedRead() throws Exception {
		testDeferredInputStream(false, false, true, true, false);
	}

	@Test
	public void testEarlyBindingOfNonRepeatableNonIncrementalDiskDelegate() throws Exception {
		testDeferredInputStream(false, false, false, false, false);
	}

	@Test
	public void testEarlyBindingOfNonRepeatableNonIncrementalDiskDelegateWithBufferedRead() throws Exception {
		testDeferredInputStream(false, false, false, true, false);
	}

	@Test
	public void testEarlyBindingOfRepeatableNonIncrementalInMemoryDelegate() throws Exception {
		testDeferredInputStream(false, true, false, false, true);
	}

	@Test
	public void testEarlyBindingOfRepeatableNonIncrementalInMemoryDelegateWithBufferedRead() throws Exception {
		testDeferredInputStream(false, true, false, true, true);
	}

	@Test
	public void testEarlyBindingOfNonRepeatableIncrementalInMemoryDelegate() throws Exception {
		testDeferredInputStream(false, false, true, false, true);
	}

	@Test
	public void testEarlyBindingOfNonRepeatableIncrementalInMemoryDelegateWithBufferedRead() throws Exception {
		testDeferredInputStream(false, false, true, true, true);
	}

	@Test
	public void testEarlyBindingOfNonRepeatableNonIncrementalInMemoryDelegate() throws Exception {
		testDeferredInputStream(false, false, false, false, true);
	}

	@Test
	public void testEarlyBindingOfNonRepeatableNonIncrementalInMemoryDelegateWithBufferedRead() throws Exception {
		testDeferredInputStream(false, false, false, true, true);
	}

	@Test
	public void testClosePostConsumption() throws Exception {

		byte[] testData = randomData(dataSize);

		DeferredInputStream deferred = new DeferredInputStream();

		CloseableByteArrayInputStream delegate = new CloseableByteArrayInputStream(testData);

		deferred.setDelegate(() -> delegate, true, false);

		byte[] readData;
		try {
			readData = loadBuffered(deferred);
		} finally {
			deferred.close();
		}

		Assert.assertArrayEquals(testData, readData);
		Assert.assertTrue(delegate.isClosed());

	}

	@Test
	public void testClosePreConsumption() throws Exception {

		byte[] testData = randomData(dataSize);

		DeferredInputStream deferred = new DeferredInputStream();

		CloseableByteArrayInputStream delegate = new CloseableByteArrayInputStream(testData);

		deferred.setDelegate(() -> delegate, true, false);

		deferred.close();

		Assert.assertTrue("Delegate should have been closed at this point", delegate.isClosed());

		try {
			loadBuffered(deferred);
			Assert.fail("Read on closed " + deferred.desc + " should have failed");
		} catch (IllegalStateException e) {
			// Expected at this point. Thrown by the test delegate directly
			Assert.assertEquals("Closed", e.getMessage());
		} finally {
			deferred.close();
		}

	}

	@Test
	public void testClosePreDelegateSetting() throws Exception {

		byte[] testData = randomData(dataSize);

		DeferredInputStream deferred = new DeferredInputStream();

		CloseableByteArrayInputStream delegate = new CloseableByteArrayInputStream(testData);

		deferred.close();

		deferred.setDelegate(() -> delegate, true, false);

		try {
			loadBuffered(deferred);
			Assert.fail("Read on closed " + deferred.desc + " should have failed");
		} catch (IOException e) {
			// Expected at this point. Thrown by DeferredInputStream
		} finally {
			deferred.close();
		}

	}

	@Test
	public void testCloseWhileWaitingForDelegate() throws Exception {

		DeferredInputStream deferred = new DeferredInputStream();

		Future<byte[]> result = executor.submit(() -> {
			try {
				return loadBuffered(deferred);
			} finally {
				deferred.close();
			}
		});

		Thread.sleep(500);

		deferred.close();

		try {
			result.get();
			Assert.fail("Closing " + deferred.desc + " in one thread should have thrown an exception while trying to read in another.");
		} catch (ExecutionException e) {
			Assert.assertTrue("Unexpected cause: " + e.getCause(), e.getCause() instanceof IOException);
		}

	}

	@Test
	public void testDelegateInvalidationBeforeBinding() throws Exception {

		DeferredInputStream deferred = new DeferredInputStream();

		Future<byte[]> result = executor.submit(() -> {
			try {
				return loadBuffered(deferred);
			} finally {
				deferred.close();
			}
		});

		Thread.sleep(500);

		CharConversionException failure = new CharConversionException("My invalidation reason");

		deferred.markDelegateAsInvalid(failure);

		try {
			result.get();
			Assert.fail("Invalidating " + deferred.desc + " in one thread should have thrown an exception while trying to read in another.");
		} catch (ExecutionException e) {
			Assert.assertTrue(e.getCause() instanceof IOException);
			Throwable readExceptionCause = e.getCause().getCause();
			Assert.assertNotNull(readExceptionCause);
			Assert.assertTrue(readExceptionCause == failure);
		}

	}

	@Test
	public void testDelegateInvalidationAfterBinding() throws Exception {

		DeferredInputStream deferred = new DeferredInputStream();

		Future<byte[]> result = executor.submit(() -> {
			try {

				return loadBuffered(deferred);
			} finally {
				deferred.close();
			}
		});

		byte[] testData = randomData(dataSize);

		deferred.setDelegate(() -> new CloseableByteArrayInputStream(testData), true, true);

		Thread.sleep(500);

		IOException failure = new IOException("My invalidation reason");

		deferred.markDelegateAsInvalid(failure);

		try {
			result.get();
			Assert.fail("Invalidating " + deferred.desc + " in one thread should have thrown an exception while trying to read in another.");
		} catch (ExecutionException e) {
			Assert.assertTrue(e.getCause() instanceof IOException);
			Throwable readExceptionCause = e.getCause().getCause();
			Assert.assertNotNull(readExceptionCause);
			Assert.assertTrue(readExceptionCause == failure);
		}

	}

	@Test
	public void testEarlyBindingOfRepeatableNonIncrementalDelegateWithBufferedRead() throws Exception {

		byte[] testData = randomData(dataSize);

		DeferredInputStream deferred = new DeferredInputStream();

		deferred.setDelegate(() -> new CloseableByteArrayInputStream(testData), true, false);

		byte[] readData;
		try {
			readData = loadBuffered(deferred);
		} finally {
			deferred.close();
		}

		Assert.assertArrayEquals(testData, readData);

		testRepeatedRead(true, testData, deferred);

	}

	@Test
	public void testEarlyBindingOfNonRepeatableNonIncrementalDelegateWithBufferedRead() throws Exception {

		byte[] testData = randomData(dataSize);

		DeferredInputStream deferred = new DeferredInputStream();

		InputStream delegate = new CloseableByteArrayInputStream(testData);

		deferred.setDelegate(() -> delegate, false, false);

		byte[] readData;
		try {
			readData = loadBuffered(deferred);
		} finally {
			deferred.close();
		}

		Assert.assertArrayEquals(testData, readData);

		testRepeatedRead(false, testData, deferred);

	}

	@Test
	public void testEarlyBindingOfRepeatableIncrementalDelegateWithBufferedRead() throws Exception {

		byte[] testData = randomData(dataSize);

		Path testFile = tempFolder.newFile().toPath();

		DeferredInputStream deferred = new DeferredInputStream();

		deferred.setDelegate(new FileInputStreamSupplier(testFile), true, true);

		Future<Boolean> result = executor.submit(() -> {
			ByteArrayInputStream in = new ByteArrayInputStream(testData);
			try (OutputStream out = Files.newOutputStream(testFile, StandardOpenOption.APPEND)) {
				long s = System.currentTimeMillis();
				long l = transferBuffered(in, out);
				out.flush();
				log.debug("Wrote in " + (System.currentTimeMillis() - s) + " ms");
				deferred.markDelegateAsComplete(l);
			} catch (IOException e) {
				deferred.markDelegateAsInvalid(e);
				return false;
			}
			return true;
		});

		byte[] readData;
		try {
			long s = System.currentTimeMillis();
			readData = loadBuffered(deferred);
			log.debug("Read in " + (System.currentTimeMillis() - s) + " ms");
		} finally {
			deferred.close();
		}

		Boolean delegateCompleted = result.get();

		Assert.assertTrue(delegateCompleted);
		Assert.assertArrayEquals(testData, readData);

		testRepeatedRead(true, testData, deferred);

	}

	@Test
	public void testEarlyBindingOfNonRepeatableIncrementalDelegateWithBufferedRead() throws Exception {

		byte[] initialData = randomData(1024);
		byte[] incrementalData = randomData(dataSize);
		byte[] testData = concat(initialData, incrementalData);

		Path testFile = tempFolder.newFile().toPath();

		DeferredInputStream deferred = new DeferredInputStream();

		InputStream delegate = Files.newInputStream(testFile);

		deferred.setDelegate(() -> delegate, false, true);

		try (OutputStream sourceOut = Files.newOutputStream(testFile)) {
			sourceOut.write(initialData);
		}

		Future<Boolean> result = executor.submit(() -> {
			ByteArrayInputStream in = new ByteArrayInputStream(incrementalData);
			try (OutputStream out = Files.newOutputStream(testFile, StandardOpenOption.APPEND)) {
				long l = transferBuffered(in, out);
				deferred.markDelegateAsComplete(l);
			} catch (IOException e) {
				deferred.markDelegateAsInvalid(e);
				return false;
			}
			return true;
		});

		byte[] readData;
		try {
			readData = loadBuffered(deferred);
		} finally {
			deferred.close();
		}

		Boolean delegateCompleted = result.get();

		Assert.assertTrue(delegateCompleted);
		Assert.assertArrayEquals(testData, readData);

		testRepeatedRead(false, testData, deferred);

	}

	@Test
	public void testEarlyBindingOfRepeatableIncrementalDelegateWithBufferedConcurrentRead() throws Exception {

		int totalFiles = 4;

		long start = System.currentTimeMillis();

		List<Pair<Path, byte[]>> initialFiles = new ArrayList<>(totalFiles);
		List<DeferredInputStream> deferredInputs = new ArrayList<>(totalFiles);
		final Map<Integer, String> hashes = new ConcurrentHashMap<>(totalFiles);
		List<Future<Integer>> readResults = Collections.synchronizedList(new ArrayList<>(totalFiles));

		for (int i = 0; i < totalFiles; i++) {
			Pair<Path, byte[]> pair = new Pair<>(tempFolder.newFile().toPath(), randomData(dataSize));
			initialFiles.add(pair);
			DeferredInputStream deferredInputStream = new DeferredInputStream();
			deferredInputs.add(deferredInputStream);
		}

		Future<List<Boolean>> writeResult = executor.submit(() -> {

			List<Boolean> writingResults = new ArrayList<>();

			for (int i = 0; i < initialFiles.size(); i++) {

				try {
					Pair<Path, byte[]> pair = initialFiles.get(i);
					Path path = pair.getFirst();
					DeferredInputStream deferred = deferredInputs.get(i);
					try (OutputStream out = Files.newOutputStream(pair.getFirst());
							ByteArrayInputStream in = new ByteArrayInputStream(pair.getSecond())) {
						deferred.setDelegate(new FileInputStreamSupplier(path), true, true);
						final int f = i;
						readResults.add(executor.submit(() -> {
							try (InputStream deferredIn = deferred) {
								String hash = hashBuffered(deferredIn, bufferSize);
								hashes.put(f, hash);
								return f;
							}
						}));
						long l = transferBuffered(in, out);
						deferred.markDelegateAsComplete(l);
						log.info("Marked file #" + i + " as complete after transfering [" + l + "] bytes");
						writingResults.add(Boolean.TRUE);
						continue;
					} catch (IOException e) {
						log.error("Marked file #" + i + " as invalid due to " + e, e);
						deferred.markDelegateAsInvalid(e);
					}
				} catch (Exception e) {
					log.error("File #" + i + " couldn not be marked as completed due to " + e, e);
				}

				writingResults.add(Boolean.FALSE);

			}

			return writingResults;

		});

		writeResult.get();

		log.debug("Write completed in " + (System.currentTimeMillis() - start) + " ms");

		for (Future<Integer> future : readResults) {
			future.get();
		}

		log.debug("Read completed in " + (System.currentTimeMillis() - start) + " ms");

		for (int i = 0; i < totalFiles; i++) {
			String initialHash = hash(initialFiles.get(i).getSecond());
			log.info("Read original file #" + i + ": " + initialHash);
			String openHash = hashes.get(i);
			Assert.assertEquals("The hash from the original deferred read for the file #" + i + " (" + openHash
					+ ") doesn't match the hash generated based on the original test data: " + initialHash, initialHash, openHash);
			String reopenHash = null;
			try (InputStream reopened = deferredInputs.get(i).reopen()) {
				log.info("Reopened input stream for file #" + i + ": " + reopened);
				reopenHash = hashBuffered(reopened, bufferSize);
			}
			log.info("Re-consumed file #" + i + ": " + reopenHash);
			Assert.assertEquals("The hash generated from the reopened inputstream for the file #" + i + " (" + reopenHash
					+ ") doesn't match the hash generated based on the original test data: " + initialHash, initialHash, reopenHash);
		}

	}

	@Test
	public void testLateBindingOfRepeatableNonIncrementalDelegateWithBufferedRead() throws Exception {

		byte[] testData = randomData(dataSize);

		DeferredInputStream deferred = new DeferredInputStream();

		Future<byte[]> result = executor.submit(() -> {
			try {
				return loadBuffered(deferred);
			} finally {
				deferred.close();
			}
		});

		Thread.sleep(500);

		deferred.setDelegate(() -> new CloseableByteArrayInputStream(testData), true, false);

		byte[] readData = result.get();

		Assert.assertArrayEquals(testData, readData);

		testRepeatedRead(true, testData, deferred);

	}

	@Test
	public void testLateBindingOfNonRepeatableNonIncrementalDelegateWithBufferedRead() throws Exception {

		byte[] testData = randomData(dataSize);

		DeferredInputStream deferred = new DeferredInputStream();

		Future<byte[]> result = executor.submit(() -> {
			try {
				return loadBuffered(deferred);
			} finally {
				deferred.close();
			}
		});

		Thread.sleep(500);

		deferred.setDelegate(() -> new CloseableByteArrayInputStream(testData), false, false);

		byte[] readData = result.get();

		Assert.assertArrayEquals(testData, readData);

		testRepeatedRead(false, testData, deferred);

	}

	@Test
	public void testLateBindingOfRepeatableIncrementalDelegateWithBufferedRead() throws Exception {

		byte[] testData = randomData(dataSize);

		Path testFile = tempFolder.newFile().toPath();

		DeferredInputStream deferred = new DeferredInputStream();

		Future<byte[]> result = executor.submit(() -> {
			try {
				return loadBuffered(deferred);
			} finally {
				deferred.close();
			}
		});

		deferred.setDelegate(new FileInputStreamSupplier(testFile), true, true);

		try (OutputStream out = Files.newOutputStream(testFile); ByteArrayInputStream in = new ByteArrayInputStream(testData)) {
			long l = transferBuffered(in, out);
			deferred.markDelegateAsComplete(l);
		} catch (IOException e) {
			deferred.markDelegateAsInvalid(e);
		}

		byte[] readData = result.get();

		Assert.assertArrayEquals(testData, readData);

		testRepeatedRead(true, testData, deferred);

	}

	@Test
	public void testLateBindingOfNonRepeatableIncrementalDelegateWithBufferedRead() throws Exception {

		byte[] testData = randomData(dataSize);

		Path testFile = tempFolder.newFile().toPath();

		DeferredInputStream deferred = new DeferredInputStream();

		Future<byte[]> result = executor.submit(() -> {
			try {
				return loadBuffered(deferred);
			} finally {
				deferred.close();
			}
		});

		Supplier<InputStream> delegateSupplier = new TestSupplier<>(new FileInputStreamSupplier(testFile), true);

		deferred.setDelegate(delegateSupplier, false, true);

		try (OutputStream out = Files.newOutputStream(testFile); ByteArrayInputStream in = new ByteArrayInputStream(testData)) {
			long l = transferBuffered(in, out);
			deferred.markDelegateAsComplete(l);
		} catch (IOException e) {
			deferred.markDelegateAsInvalid(e);
		}

		byte[] readData = result.get();

		Assert.assertArrayEquals(testData, readData);

		testRepeatedRead(false, testData, deferred);

	}

	@Test
	public void testLateBindingOfRepeatableIncrementalDelegateWithBufferedConcurrentRead() throws Exception {

		int totalFiles = 4;

		long start = System.currentTimeMillis();

		List<Pair<Path, byte[]>> initialFiles = new ArrayList<>(totalFiles);
		List<DeferredInputStream> deferredInputs = new ArrayList<>(totalFiles);
		final Map<Integer, String> hashes = new ConcurrentHashMap<>(totalFiles);
		List<Future<Integer>> readResults = new ArrayList<>(totalFiles);

		for (int i = 0; i < totalFiles; i++) {
			Pair<Path, byte[]> pair = new Pair<>(tempFolder.newFile().toPath(), randomData(dataSize));
			initialFiles.add(pair);
			DeferredInputStream deferredInputStream = new DeferredInputStream();
			deferredInputs.add(deferredInputStream);
			final int f = i;
			readResults.add(executor.submit(() -> {
				try (InputStream in = deferredInputStream) {
					String hash = hashBuffered(in, bufferSize);
					hashes.put(f, hash);
					log.info("Read deferred input for file #" + f + ": " + hash);
					return f;
				}
			}));
		}

		Future<List<Boolean>> writeResult = executor.submit(() -> {

			List<Boolean> writingResults = new ArrayList<>();

			for (int i = 0; i < initialFiles.size(); i++) {

				try {
					Pair<Path, byte[]> pair = initialFiles.get(i);
					Path path = pair.getFirst();
					DeferredInputStream deferred = deferredInputs.get(i);
					try (OutputStream out = Files.newOutputStream(pair.getFirst());
							ByteArrayInputStream in = new ByteArrayInputStream(pair.getSecond())) {
						deferred.setDelegate(new FileInputStreamSupplier(path), true, true);
						long l = transferBuffered(in, out);
						deferred.markDelegateAsComplete(l);
						log.info("Marked file #" + i + " as complete after transfering [" + l + "] bytes");
						writingResults.add(Boolean.TRUE);
						continue;
					} catch (IOException e) {
						log.error("Marked file #" + i + " as invalid due to " + e, e);
						deferred.markDelegateAsInvalid(e);
					}
				} catch (Exception e) {
					log.error("File #" + i + " couldn not be marked as completed due to " + e, e);
				}

				writingResults.add(Boolean.FALSE);

			}

			return writingResults;

		});

		writeResult.get();

		log.debug("Write completed in " + (System.currentTimeMillis() - start) + " ms");

		for (Future<Integer> future : readResults) {
			future.get();
		}

		log.debug("Read completed in " + (System.currentTimeMillis() - start) + " ms");

		for (int i = 0; i < totalFiles; i++) {
			String initialHash = hash(initialFiles.get(i).getSecond());
			log.info("Read original file #" + i + ": " + initialHash);
			String openHash = hashes.get(i);
			Assert.assertEquals("The hash from the original deferred read for the file #" + i + " (" + openHash
					+ ") doesn't match the hash generated based on the original test data: " + initialHash, initialHash, openHash);
			String reopenHash = null;
			try (InputStream reopened = deferredInputs.get(i).reopen()) {
				log.info("Reopened input stream for file #" + i + ": " + reopened);
				reopenHash = hashBuffered(reopened, bufferSize);
			}
			log.info("Re-consumed file #" + i + ": " + reopenHash);
			Assert.assertEquals("The hash generated from the reopened inputstream for the file #" + i + " (" + reopenHash
					+ ") doesn't match the hash generated based on the original test data: " + initialHash, initialHash, reopenHash);
		}

	}

	protected void testDeferredInputStream(boolean lateBinding, boolean repeatable, boolean incremental, boolean bufferedRead, boolean inMemory)
			throws Exception {

		if (inMemory && repeatable && incremental) {
			// MAYBE ONLY incremental == true.
			throw new IllegalArgumentException("inMemory cannot be tested as repeatable and incremental");
		}

		Object bindMonitor = new Object();
		final Holder<Boolean> earlyBound = new Holder<>(false);

		byte[] testData = randomData(bufferedRead ? dataSize : minimalDataSize);

		Path testFile = inMemory ? null : tempFolder.newFile().toPath();

		DeferredInputStream deferred = new DeferredInputStream();

		Future<byte[]> result = executor.submit(() -> {
			try {
				if (!lateBinding) {
					while (!earlyBound.get()) {
						synchronized (bindMonitor) {
							bindMonitor.wait(100);
						}
					}
				}
				return (bufferedRead) ? loadBuffered(deferred) : load(deferred);
			} finally {
				deferred.close();
			}
		});

		Supplier<InputStream> inSupplier = null;
		Supplier<OutputStream> outSupplier = null;

		if (inMemory) {
			if (incremental) {
				PipedOutputStream pout = new PipedOutputStream();
				PipedInputStream pin = new PipedInputStream(dataSize);
				pout.connect(pin);
				inSupplier = () -> pin;
				outSupplier = () -> pout;
			} else {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				inSupplier = () -> new ByteArrayInputStream(out.toByteArray());
				outSupplier = () -> out;
			}
		} else {
			inSupplier = new FileInputStreamSupplier(testFile);
			outSupplier = new FileOutputStreamSupplier(testFile);
		}

		Supplier<InputStream> delegateSupplier = new TestSupplier<>(inSupplier, !repeatable);

		if (incremental) {

			deferred.setDelegate(delegateSupplier, repeatable, incremental);

			if (!lateBinding) {
				synchronized (bindMonitor) {
					bindMonitor.notifyAll();
				}
				earlyBound.accept(true);
			}

			try (OutputStream out = outSupplier.get(); ByteArrayInputStream in = new ByteArrayInputStream(testData)) {
				long l = transferBuffered(in, out);
				deferred.markDelegateAsComplete(l);
			} catch (IOException e) {
				deferred.markDelegateAsInvalid(e);
			}

		} else {

			try (OutputStream out = outSupplier.get(); ByteArrayInputStream in = new ByteArrayInputStream(testData)) {
				transferBuffered(in, out);
			}

			deferred.setDelegate(delegateSupplier, repeatable, incremental);

			if (!lateBinding) {
				synchronized (bindMonitor) {
					bindMonitor.notifyAll();
				}
				earlyBound.accept(true);
			}

		}

		byte[] readData = result.get();

		Assert.assertArrayEquals(testData, readData);

		testRepeatedRead(repeatable, testData, deferred);

	}

	protected void testRepeatedRead(boolean repeatable, byte[] testData, DeferredInputStream deferred) throws IOException {

		InputStream reopened = null;
		try {
			reopened = deferred.reopen();
			if (!repeatable) {
				Assert.fail("Non-repeatable source should have failed to be reopened");
			}
		} catch (Exception e) {
			if (repeatable) {
				Assert.fail("Repeatable source should have been reopened but an exception was thrown: " + e);
			}
		}
		if (repeatable) {
			byte[] readData = loadBuffered(reopened);
			reopened.close();
			Assert.assertArrayEquals(testData, readData);
		}

	}

	protected byte[] randomData(int dataSize) {
		byte[] data = new byte[dataSize];
		new Random().nextBytes(data);
		return data;
	}

	protected String hashBuffered(final InputStream inputStream, int bufferSize) throws IOException {
		MessageDigest md = createMessageDigest();
		try (DigestInputStream in = new DigestInputStream(inputStream, md)) {
			consumeBuffered(in, bufferSize);
			return digest(md);
		}
	}

	protected long consumeBuffered(final InputStream inputStream, int bufferSize) throws IOException {
		final byte[] buffer = new byte[bufferSize];
		int count;
		long totalCount = 0;
		while ((count = inputStream.read(buffer)) != -1) {
			totalCount += count;
		}
		return totalCount;
	}

	protected long transferBuffered(final InputStream inputStream, final OutputStream outputStream) throws IOException {
		return transferBuffered(inputStream, outputStream, bufferSize);
	}

	protected long transferBuffered(final InputStream inputStream, final OutputStream outputStream, int bufferSize) throws IOException {
		final byte[] buffer = new byte[bufferSize];
		int count;
		long totalCount = 0;
		while ((count = inputStream.read(buffer)) != -1) {
			outputStream.write(buffer, 0, count);
			totalCount += count;
		}
		return totalCount;
	}

	protected byte[] loadBuffered(final InputStream inputStream) throws IOException {
		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			transferBuffered(inputStream, outputStream, bufferSize);
			return outputStream.toByteArray();
		}
	}

	protected int readBuffered(InputStream is) throws Exception {
		return readBuffered(is, 0);
	}

	protected int readBuffered(InputStream is, int limit) throws Exception {
		int count;
		int totalCount = 0;
		while ((count = is.read(new byte[bufferSize])) != -1) {
			totalCount += count;
			if (limit > 0 && totalCount >= limit) {
				break;
			}
		}
		return totalCount;
	}

	protected long transfer(InputStream inputStream, OutputStream outputStream) throws IOException {
		int b;
		long totalCount = 0;
		while ((b = inputStream.read()) != -1) {
			outputStream.write(b);
			totalCount++;
		}
		return totalCount;
	}

	protected byte[] load(InputStream inputStream) throws IOException {
		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			transfer(inputStream, outputStream);
			return outputStream.toByteArray();
		}
	}

	protected int read(InputStream is) throws Exception {
		return read(is, 0);
	}

	protected int read(InputStream is, int limit) throws Exception {
		int totalCount = 0;
		while (is.read() != -1) {
			totalCount++;
			if (limit > 0 && totalCount >= limit) {
				break;
			}
		}
		return totalCount;
	}

	protected static MessageDigest createMessageDigest() {
		try {
			return MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("MD5 digest is not available", e);
		}
	}

	protected static String digest(MessageDigest md) {
		return convertToHex(md.digest());
	}

	protected static String hash(byte[] data) {
		MessageDigest md = createMessageDigest();
		md.update(data, 0, data.length);
		byte[] md5hash = md.digest();
		return convertToHex(md5hash);
	}

	protected static String convertToHex(byte[] data) {
		StringBuffer buf = new StringBuffer();
		for (byte element : data) {
			int halfbyte = (element >>> 4) & 0x0F;
			int two_halfs = 0;
			do {
				if ((0 <= halfbyte) && (halfbyte <= 9)) {
					buf.append((char) ('0' + halfbyte));
				} else {
					buf.append((char) ('a' + (halfbyte - 10)));
				}
				halfbyte = element & 0x0F;
			} while (two_halfs++ < 1);
		}
		return buf.toString();
	}

	protected static byte[] concat(byte[] first, byte[] second) {
		byte[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}

	public static class CloseableByteArrayInputStream extends ByteArrayInputStream {

		private boolean closed;

		public CloseableByteArrayInputStream(byte[] buf, int offset, int length) {
			super(buf, offset, length);
		}

		public CloseableByteArrayInputStream(byte[] buf) {
			super(buf);
		}
		@Override
		public synchronized int read() {
			if (closed) {
				throw new IllegalStateException("Closed");
			}
			return super.read();
		}

		@Override
		public synchronized int read(byte[] b, int off, int len) {
			if (closed) {
				throw new IllegalStateException("Closed");
			}
			return super.read(b, off, len);
		}

		@Override
		public void close() throws IOException {
			closed = true;
		}

		public boolean isClosed() {
			return closed;
		}

	}

	public static class TestSupplier<T> implements Supplier<T> {

		private final Supplier<T> delegate;
		private final boolean cache;

		private T cachedValue;
		private boolean supplied;

		public TestSupplier(Supplier<T> delegate, boolean cache) {
			this.delegate = delegate;
			this.cache = cache;
		}

		@Override
		public T get() {
			supplied = true;
			if (cachedValue != null) {
				return cachedValue;
			}
			T value = delegate.get();
			if (cache) {
				cachedValue = value;
			}
			return value;
		}

		public boolean wasSupplied() {
			return supplied;
		}

	}

	static class FileInputStreamSupplier implements Supplier<InputStream> {

		private final Path path;

		public FileInputStreamSupplier(Path path) {
			super();
			this.path = path;
		}

		@Override
		public InputStream get() {
			try {
				return Files.newInputStream(path);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}

	}

	static class FileOutputStreamSupplier implements Supplier<OutputStream> {

		private final Path path;

		public FileOutputStreamSupplier(Path path) {
			super();
			this.path = path;
		}

		@Override
		public OutputStream get() {
			try {
				return Files.newOutputStream(path);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}

	}

}
