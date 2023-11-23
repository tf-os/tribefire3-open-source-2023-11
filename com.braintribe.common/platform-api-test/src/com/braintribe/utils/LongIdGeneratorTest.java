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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class LongIdGeneratorTest {

	private static long initialRandom;
	private static AtomicLong atomicLongCounter;
	private static BackOffAtomicLong backOffAtomicLongCounter = new BackOffAtomicLong();
	private static long timestampOffset = 0L;
	// protected static List<Long> issuedIds;

	@BeforeClass
	public static void initialize() {
		initialRandom = new SecureRandom().nextLong();
		atomicLongCounter = new AtomicLong(initialRandom);
	}

	@AfterClass
	public static void close() {
		// issuedIds = null;
	}

	@Before
	public void initializeBeforeEachTest() {
		timestampOffset = 0L;
	}

	@Test
	public void testGeneratorExternal() {

		Set<Long> generatedIds = new HashSet<>();
		for (int i = 0; i < 100000; ++i) {
			long id = LongIdGenerator.provideLongId();
			// System.out.println("Generated "+id);
			if (generatedIds.contains(id)) {
				printDetailsOfId(id);
				printTimestampUsage(0, generatedIds);
				throw new AssertionError("The id " + id + " has been generated twice.");
			}
			generatedIds.add(id);
		}

	}

	@Test
	public void testGeneratorInternal() {

		Set<Long> generatedIds = new HashSet<>();
		for (int i = 0; i < 100_000; ++i) {
			long id = provideLongId();
			// System.out.println("Generated "+id);
			if (generatedIds.contains(id)) {
				printDetailsOfId(id);
				printTimestampUsage(0, generatedIds);
				throw new AssertionError("The id " + id + " has been generated twice. Initial random: " + initialRandom);
			}
			generatedIds.add(id);
		}

	}

	@Test
	public void multiThreadedGeneratorTestExternal() {
		multiThreadedGeneratorTestImpl(false);
	}
	@Test
	public void multiThreadedGeneratorTestInternal() {
		multiThreadedGeneratorTestImpl(true);
	}

	@Ignore
	private void multiThreadedGeneratorTestImpl(boolean internal) {

		int nrOfThreads = 5;
		final int iterations = 10000;
		final Set<Long> generatedIds = Collections.synchronizedSet(new HashSet<Long>());

		Date now = new Date();
		System.out.println("Current date: " + DateTools.encode(now, DateTools.ISO8601_DATE_WITH_MS_FORMAT) + " (" + now.getTime() + "); "
				+ (internal ? "Internal calls; initial random: " + initialRandom : "External calls used"));

		// issuedIds = Collections.synchronizedList(new ArrayList<Long>(10000000));

		ExecutorService executor = Executors.newFixedThreadPool(nrOfThreads);
		try {
			Set<Future<Long>> futures = new HashSet<>();

			for (int i = 0; i < nrOfThreads; ++i) {
				ThreadedTester tester = new ThreadedTester(i, iterations, generatedIds, internal);
				Future<Long> future = executor.submit(tester);
				futures.add(future);
			}

			for (Future<Long> future : futures) {
				try {
					Long id = future.get();
					if (id != null) {
						printDetailsOfId(id);
						throw new AssertionError("One of the threads generated a duplicate ID: " + id);
					}
				} catch (Exception e) {
					throw new AssertionError("Could not get result of thread: " + e.getMessage(), e);
				}
			}
		} finally {
			try {
				executor.shutdownNow();
			} catch (Throwable ignore) {
				//
			}
		}
	}

	@Test
	public void multiThreadedGeneratorTest2External() {
		multiThreadedGeneratorTest2Impl(false);
	}
	@Test
	public void multiThreadedGeneratorTest2Internal() {
		multiThreadedGeneratorTest2Impl(true);
	}

	@Ignore
	private void multiThreadedGeneratorTest2Impl(boolean internal) {

		int nrOfThreads = 5;
		final int iterations = 10000;
		final List<Set<Long>> generatedIdsList = new ArrayList<>(nrOfThreads);

		Date now = new Date();
		System.out.println("Current date: " + DateTools.encode(now, DateTools.ISO8601_DATE_WITH_MS_FORMAT) + " (" + now.getTime() + "); "
				+ (internal ? "Internal calls; initial random: " + initialRandom : "External calls used"));

		// issuedIds = Collections.synchronizedList(new ArrayList<Long>(10000000));

		ExecutorService executor = Executors.newFixedThreadPool(nrOfThreads);
		try {
			Map<Integer, Future<Long>> futures = new HashMap<>();

			long start = System.currentTimeMillis();

			for (int i = 0; i < nrOfThreads; ++i) {
				Set<Long> generatedIds = new LinkedHashSet<>();
				generatedIdsList.add(generatedIds);
				ThreadedTester tester = new ThreadedTester(i, iterations, generatedIds, internal);
				Future<Long> future = executor.submit(tester);
				futures.put(i, future);
			}

			for (Map.Entry<Integer, Future<Long>> entry : futures.entrySet()) {
				try {
					Integer workerId = entry.getKey();
					Future<Long> future = entry.getValue();
					Long id = future.get();
					if (id != null) {
						printDetailsOfId(id);
						System.out.println("One of the threads (" + workerId + ") generated a duplicate ID: " + id + " (duration so far: "
								+ (System.currentTimeMillis() - start) + " ms");
					}
				} catch (Exception e) {
					throw new AssertionError("Could not get result of thread: " + e.getMessage(), e);
				}
			}

			System.out.println("Generation of Ids ended after " + (System.currentTimeMillis() - start) + " ms");

			Set<Long> superSet = new HashSet<>();
			for (int i = 0; i < nrOfThreads; ++i) {
				Set<Long> generatedIds = generatedIdsList.get(i);
				Assert.assertEquals(iterations, generatedIds.size());
				for (Long gId : generatedIds) {
					if (superSet.contains(gId)) {
						List<Integer> workerIds = new ArrayList<>();
						for (int j = 0; j < i; ++j) {
							Set<Long> beforeIds = generatedIdsList.get(j);
							if (beforeIds.contains(gId)) {
								workerIds.add(j);
							}
						}
						System.out.println("The ID " + gId + ", which has been created by worker " + i
								+ " has been created before by another worker (" + workerIds + "). An error message will follow.");
						printDetailsOfId(gId);

						printTimestampUsage(i, generatedIdsList.get(i));
						printTimestampUsage(workerIds.get(0), generatedIdsList.get(workerIds.get(0)));
						// No further action needed. The following code will detect the problem
					}
				}
				superSet.addAll(generatedIds);
			}
			boolean success = (iterations * nrOfThreads) == superSet.size();
			if (!success) {
				printAllIds(generatedIdsList);
				printTimestampUsage(-1, superSet);
			}
			Assert.assertEquals(iterations * nrOfThreads, superSet.size());
		} finally {
			try {
				executor.shutdownNow();
			} catch (Throwable ignore) {
				//
			}
		}
	}

	protected static void printAllIds(List<Set<Long>> idsList) {
		String marker = UUID.randomUUID().toString();
		System.out.println("------------------");
		System.out.println("Start: " + marker + " ... base64-decode this ZIP file for analysis");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (ZipOutputStream out = new ZipOutputStream(baos)) {
			for (int i = 0; i < idsList.size(); ++i) {
				Integer workerId = i;
				Set<Long> set = idsList.get(i);
				out.putNextEntry(new ZipEntry("ids-of-worker-" + workerId + ".txt"));
				for (Long id : set) {
					out.write(("" + id + "\n").getBytes("UTF-8"));
				}
				out.closeEntry();
			}
			// if (internal) {
			// out.putNextEntry(new ZipEntry("issuedIds.txt"));
			// for (Long id : issuedIds) {
			// out.write((""+id+"\n").getBytes("UTF-8"));
			// }
			// out.closeEntry();
			// }
			out.finish();
		} catch (Exception e) {
			e.printStackTrace();
		}
		String text = Base64.encodeBytes(baos.toByteArray());
		System.out.println(text);
		System.out.println("End of " + marker);
	}

	class ThreadedTester implements Callable<Long> {
		protected int workerId;
		protected int iterations = 1;
		protected Set<Long> ids = null;
		private final boolean internal;

		public ThreadedTester(int workerId, int iterations, final Set<Long> ids, boolean internal) {
			this.workerId = workerId;
			this.iterations = iterations;
			this.ids = ids;
			this.internal = internal;
		}

		@Override
		public Long call() throws Exception {
			long lastTimestamp = -1;
			long lastId = -1;
			Long duplicate = null;
			for (int j = 0; j < iterations; ++j) {
				long id = internal ? provideLongId() : LongIdGenerator.provideLongId();
				// issuedIds.add(id);
				if (!this.ids.add(id)) {
					System.out.println("The id " + id + " has been generated twice.");
					printDetailsOfId(id);
					System.out.println("lastTimestamp=" + lastTimestamp);
					System.out.println("last id: ");
					printDetailsOfId(lastId);
					printTimestampUsage(this.workerId, ids);

					duplicate = id;
				} else {
					long timestamp = id >> 21;
					if (timestamp < lastTimestamp) {
						System.out.println("The id " + id + " has a timestamp that is LOWER than the one from the previous id.");
						printDetailsOfId(id);
						printTimestampUsage(this.workerId, ids);
						return id;
					}
					long now = System.currentTimeMillis() + timestampOffset;
					if (Math.abs(now - timestamp) > 10000L) {
						System.out.println(
								"The id " + id + " has a timestamp (" + timestamp + ") that is too far away from the current time (" + now + ").");
						printDetailsOfId(id);
						printTimestampUsage(this.workerId, ids);
						return id;
					}
					lastTimestamp = timestamp;
				}
				lastId = id;
			}
			return duplicate;
		}
	}

	class ThreadedTesterForAtomicNumber implements Callable<Long> {
		protected int workerId;
		protected int iterations = 1;
		protected Set<Long> ids = null;
		private final boolean internal;

		public ThreadedTesterForAtomicNumber(int workerId, int iterations, final Set<Long> ids, boolean internal) {
			this.workerId = workerId;
			this.iterations = iterations;
			this.ids = ids;
			this.internal = internal;
		}

		@Override
		public Long call() throws Exception {
			for (int j = 0; j < iterations; ++j) {
				long s1 = System.currentTimeMillis();
				long count = (internal) ? (backOffAtomicLongCounter.incrementAndGet()) : (LongIdGenerator.atomicLongCounter.incrementAndGet());
				long s2 = System.currentTimeMillis();
				long delay = s2 - s1;
				if (delay > 50) {
					System.out.println("Getting an " + (internal ? "backoff" : "normal") + " atomic increment took " + delay + " ms.");
				}
				if (!this.ids.add(count)) {
					System.out.println("The count " + count + " has been generated twice.");
					return count;
				}
			}
			return null;
		}
	}

	@Test
	public void multiThreadedAtomicLongTestInternal() {
		multiThreadedAtomicLongTestImpl(true);
	}
	@Test
	public void multiThreadedAtomicLongTestExternal() {
		multiThreadedAtomicLongTestImpl(false);
	}

	@Ignore
	public void multiThreadedAtomicLongTestImpl(boolean internal) {

		for (int k = 0; k < 15; ++k) {
			int nrOfThreads = 5;
			final int iterations = 10000;
			final List<Set<Long>> generatedIdsList = new ArrayList<>(nrOfThreads);

			ExecutorService executor = Executors.newFixedThreadPool(nrOfThreads);
			try {
				Set<Future<Long>> futures = new HashSet<>();

				long start = System.currentTimeMillis();

				for (int i = 0; i < nrOfThreads; ++i) {
					Set<Long> generatedIds = new HashSet<>();
					generatedIdsList.add(generatedIds);
					ThreadedTesterForAtomicNumber tester = new ThreadedTesterForAtomicNumber(i, iterations, generatedIds, internal);
					Future<Long> future = executor.submit(tester);
					futures.add(future);
				}

				for (Future<Long> future : futures) {
					try {
						Long id = future.get();
						if (id != null) {
							throw new AssertionError("One of the threads generated a duplicate ID: " + id + " (duration so far: "
									+ (System.currentTimeMillis() - start) + " ms");
						}
					} catch (Exception e) {
						throw new AssertionError("Could not get result of thread: " + e.getMessage());
					}
				}

				System.out.println("Generation of " + (iterations * nrOfThreads) + " " + (internal ? "internal" : "external")
						+ " atomic long values ended after " + (System.currentTimeMillis() - start) + " ms");

				Set<Long> superSet = new HashSet<>();
				for (int i = 0; i < nrOfThreads; ++i) {
					Set<Long> generatedIds = generatedIdsList.get(i);
					Assert.assertEquals(iterations, generatedIds.size());
					superSet.addAll(generatedIds);
				}
				Assert.assertEquals(iterations * nrOfThreads, superSet.size());
			} finally {
				try {
					executor.shutdownNow();
				} catch (Throwable ignore) {
					//
				}
			}
		}
	}

	@Ignore
	protected static void printTimestampUsage(int workerId, Set<Long> ids) {
		SortedMap<String, Integer> timestamps = new TreeMap<>();
		Date errD = new Date();
		for (Long existingId : ids) {
			existingId = existingId >> 21;

			errD.setTime(existingId);
			String errDString = DateTools.encode(errD, DateTools.ISO8601_DATE_WITH_MS_FORMAT);

			Integer errCount = timestamps.get(errDString);
			if (errCount == null) {
				timestamps.put(errDString, 1);
			} else {
				timestamps.put(errDString, errCount + 1);
			}
		}
		for (Map.Entry<String, Integer> entry : timestamps.entrySet()) {
			System.out.println("" + workerId + ": Timestamp: " + entry.getKey() + ": " + entry.getValue());
		}
	}

	@Ignore
	public static void printDetailsOfId(long id) {

		long timestamp = id >> 21;
		Date d = new Date();
		d.setTime(timestamp);
		long counter = id & 0x1fffff;

		StringBuilder sb = new StringBuilder();
		sb.append("Id: " + id + "\n");
		String binaryIdString = StringTools.extendStringInFront(Long.toBinaryString(id), '0', 64);
		sb.append(binaryIdString + "\n");

		String binaryTimestampString = StringTools.extendStringInFront(Long.toBinaryString(timestamp), '0', 43);
		sb.append(binaryTimestampString + " = " + DateTools.encode(d, DateTools.ISO8601_DATE_WITH_MS_FORMAT) + "\n");

		String counterLine = StringTools.extendStringInFront("" + counter + " = ", ' ', 43);

		String counterString = StringTools.extendStringInFront(Long.toBinaryString(counter), '0', 21);
		counterLine += counterString;

		sb.append(counterLine);
		System.out.println(StringTools.asciiBoxMessage(sb.toString(), 250));
	}

	@Test
	public void printSequentialIds() {
		long id = 3131619689881998739L;
		printDetailsOfId(id);

		for (int i = 0; i < 10; ++i) {
			id = LongIdGenerator.provideLongId();
			printDetailsOfId(id);
		}

	}

	@Test
	public void useLocalIdGenerator() {
		long id = provideLongId();
		printDetailsOfId(id);

	}

	/* This is a replica of the original method, with additional checks here */
	@Ignore
	private static long provideLongId() {

		long start = System.currentTimeMillis();
		long nowMs = start + timestampOffset;

		String nowMsString = Long.toBinaryString(nowMs);

		long s1 = System.currentTimeMillis();

		// Now, we make space for the sequence number. Thus, we shift the timestamp by 21 bits to the left
		// We have now space for approx 2 mio sequence numbers (per Millisecond) (2097151, to be exact)
		nowMs = nowMs << 21;

		long s2 = System.currentTimeMillis();

		String nowMsShifted = Long.toBinaryString(nowMs);
		String expectedNowMsShifted = StringTools.extendString(nowMsString, '0', nowMsString.length() + 21);
		if (!nowMsShifted.equals(expectedNowMsShifted)) {
			throw new AssertionError("The binary representation of nowMs (shifted) (" + nowMs + " = " + nowMsShifted
					+ ") is not equal to the expected String: " + expectedNowMsShifted);
		}

		// Take the next sequence number and use the 21 least significant bits
		long s4 = System.currentTimeMillis();
		long counter = atomicLongCounter.incrementAndGet();
		long s5 = System.currentTimeMillis();

		String counterString = Long.toBinaryString(counter);

		counter = counter & 0x1fffff;

		long s6 = System.currentTimeMillis();

		String cutCounterString = Long.toBinaryString(counter);
		cutCounterString = StringTools.extendStringInFront(cutCounterString, '0', 21);
		String expectedCounterString = counterString;
		if (expectedCounterString.length() > 21) {
			expectedCounterString = expectedCounterString.substring(expectedCounterString.length() - 21);
		}
		if (!cutCounterString.equals(expectedCounterString)) {
			throw new AssertionError(
					"The cut counter (" + counter + " = " + cutCounterString + ") is not equals to the expected String: " + expectedCounterString);
		}

		long s7 = System.currentTimeMillis();

		long id = nowMs | counter;

		long s8 = System.currentTimeMillis();

		String idBinary = Long.toBinaryString(id);
		cutCounterString = StringTools.extendStringInFront(cutCounterString, '0', 21);
		String expectedIdBinary = nowMsString + cutCounterString;
		if (!idBinary.equals(expectedIdBinary)) {
			throw new AssertionError("The resulting id (" + id + " = " + idBinary + ") is not equal to the expected result: " + expectedIdBinary);
		}

		long s9 = System.currentTimeMillis();

		long timeToComputeId = s9 - start;
		if (timeToComputeId > 50) {
			System.out.println("It took " + timeToComputeId + " ms to generate an Id (atomic long generation: " + (s5 - s4) + " ms; block1-2: "
					+ (s2 - s1) + " ms; block2-4: " + (s4 - s2) + " ms; block5-6: " + (s6 - s5) + " ms; block6-7: " + (s7 - s6) + " ms; block7-8: "
					+ (s8 - s7) + " ms; block8-9: " + (s9 - s8) + " ms).");
		}

		return id;
	}

	@Test
	public void testWithKnownInitialRandom() {
		long knownInitialRandom = 6174596977000205205L;
		long knownTestDateStart = 1493641686015L; // 20170501 14:28:06.015

		long now = (new Date()).getTime();
		timestampOffset = knownTestDateStart - now;

		try {
			atomicLongCounter = new AtomicLong(knownInitialRandom);

			multiThreadedGeneratorTest2Impl(true);

		} finally {
			// Reset to some random initial
			initialRandom = new SecureRandom().nextLong();
			atomicLongCounter = new AtomicLong(knownInitialRandom);
		}
	}

	public static void main(String[] args) throws Exception {

		File baseDir = new File("/Users/roman/Downloads/longid");
		File input = new File(baseDir, "base64.txt");
		String base64 = IOTools.slurp(input, "UTF-8");

		byte[] zippedBytes = Base64.decode(base64);
		try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zippedBytes))) {
			ZipEntry entry = null;

			while ((entry = zis.getNextEntry()) != null) {
				String name = entry.getName();
				System.out.println("Entry : " + name);

				byte[] b = new byte[2048];
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				int c = 0;

				while ((c = zis.read(b)) != -1) {
					baos.write(b, 0, c);
				}

				File output = new File(baseDir, name);
				try (BufferedReader in = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(baos.toByteArray()), "UTF-8"));
						PrintWriter pw = new PrintWriter(output, "UTF-8")) {
					String line = null;
					long lastId = -1L;
					long lastCounter = -1L;
					long lastTimestamp = -1L;
					while ((line = in.readLine()) != null) {
						if (line.trim().length() > 0) {
							Long id = Long.parseLong(line);

							long timestamp = id >> 21;
							Date d = new Date();
							d.setTime(timestamp);
							long counter = id & 0x1fffff;

							StringBuilder sb = new StringBuilder();
							sb.append(id);
							sb.append(" Date: ");
							sb.append(DateTools.encode(d, DateTools.LEGACY_DATETIME_FORMAT));
							sb.append(", Counter: ");
							sb.append(counter);

							if (counter < lastCounter) {
								sb.append(" CltpC");
							}
							if (timestamp < lastTimestamp) {
								sb.append(" TsltpTs");
							}
							if (id < lastId) {
								sb.append(" IdltpId");
							}

							pw.println(sb.toString());

							lastId = id;
							lastTimestamp = timestamp;
							lastCounter = counter;

						}
					}
				}

				zis.closeEntry();
			}

		}

	}

	// https://dzone.com/articles/wanna-get-faster-wait-bit
	// http://ashkrit.blogspot.it/2014/02/atomicinteger-java-7-vs-java-8.html
	// https://blogs.oracle.com/dave/atomic-fetch-and-add-vs-compare-and-swap
	// https://arxiv.org/abs/1305.5800
	public static class BackOffAtomicLong {
		private final AtomicLong value = new AtomicLong(0L);

		public long get() {
			return value.get();
		}

		public long incrementAndGet() {
			for (;;) {
				long current = get();
				long next = current + 1;
				if (compareAndSet(current, next)) {
					return next;
				}
			}
		}

		public boolean compareAndSet(final long current, final long next) {
			if (value.compareAndSet(current, next)) {
				return true;
			} else {
				LockSupport.parkNanos(1);
				return false;
			}
		}

	}
}
