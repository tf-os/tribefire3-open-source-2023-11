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
package com.braintribe.logging.juli.formatters.commons;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.braintribe.logging.Logger;
import com.braintribe.logging.juli.formatters.simple.SimpleFormatter;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.lcd.CommonTools;

/*
 * There are additional tests available in PlatformApiTest
 */
public class CompiledFormatterTest {

	protected ByteArrayOutputStream logBuffer = null;
	protected StreamHandler streamHandler = null;

	@Before
	public void initialize() throws Exception {

		if (this.streamHandler == null) {
			this.logBuffer = new ByteArrayOutputStream();
			this.streamHandler = new StreamHandler(this.logBuffer, new SimpleFormatter("%4$-7s %7$-33s '%5$s' %6$s [%9$s - %10$s]%n"));

			java.util.logging.Logger logger = java.util.logging.Logger.getLogger(CompiledFormatterTest.class.getName());
			logger.addHandler(this.streamHandler);
		}
		logBuffer.reset();
	}

	@Ignore
	protected List<String> getLogBuffer() throws Exception {
		this.streamHandler.flush();
		BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(this.logBuffer.toByteArray()), "UTF-8"));
		List<String> bufferList = new ArrayList<>();
		String line = null;
		while ((line = br.readLine()) != null) {
			bufferList.add(line);
		}
		br.close();
		return bufferList;
	}

	@Test
	public void testSingleOutput() throws Exception {
		Logger logger = Logger.getLogger(CompiledFormatterTest.class);
		logger.info("Hello, world");
		List<String> logBuffer = this.getLogBuffer();
		Assert.assertEquals(1, logBuffer.size());
		String line = logBuffer.get(0);
		Assert.assertTrue(line.contains("Hello, world"));
	}

	@Test
	public void testMultipleOutput() throws Exception {
		Logger logger = Logger.getLogger(CompiledFormatterTest.class);
		int count = 10;
		for (int i = 0; i < count; ++i) {
			logger.info("Hello, world: " + i);
		}
		List<String> logBuffer = this.getLogBuffer();
		Assert.assertEquals(count, logBuffer.size());
		boolean[] found = new boolean[count];
		for (int i = 0; i < count; ++i) {
			found[i] = false;
		}
		for (int i = 0; i < count; ++i) {
			String line = logBuffer.get(i);
			int idx1 = line.indexOf("Hello, world: ") + 13;
			int idx2 = line.indexOf("'", idx1 + 1);
			String indexString = line.substring(idx1, idx2).trim();
			int index = Integer.parseInt(indexString);
			found[index] = true;
		}
		for (int i = 0; i < count; ++i) {
			Assert.assertTrue(found[i]);
		}
	}

	@Test
	public void testParallelOutput() throws Exception {

		int workerCount = 100;
		int iterations = 100;
		ExecutorService executor = Executors.newFixedThreadPool(workerCount);
		List<Future<Void>> futures = new ArrayList<>();
		boolean[][] found = new boolean[workerCount][];

		for (int i = 0; i < workerCount; ++i) {
			found[i] = new boolean[iterations];
			for (int j = 0; j < iterations; ++j) {
				found[i][j] = false;
			}
			futures.add(executor.submit(new Worker(i, iterations)));
		}
		for (Future<Void> f : futures) {
			f.get();
		}
		List<String> logBuffer = this.getLogBuffer();
		for (String line : logBuffer) {
			int idx1 = line.indexOf("ID:") + 3;
			int idx2 = line.indexOf('#', idx1);
			int idx3 = line.indexOf('A', idx2);
			if (idx1 < 3 || idx2 < 0 || idx3 < 0) {
				throw new RuntimeException("Unexpected negative index " + CommonTools.getParametersString("idx1", idx1, "idx2", idx2, "idx3", idx3)
						+ " in line: " + line);
			}
			String idString = line.substring(idx1, idx2);
			String itString = line.substring(idx2 + 1, idx3);
			int id = Integer.parseInt(idString);
			int it = Integer.parseInt(itString);
			found[id][it] = true;
		}
		for (int i = 0; i < workerCount; ++i) {
			for (int j = 0; j < iterations; ++j) {
				Assert.assertTrue(found[i][j]);
			}
		}
		executor.shutdown();
	}

	@Ignore
	static class Worker implements Callable<Void> {
		Logger logger = Logger.getLogger(CompiledFormatterTest.class);
		private int id;
		private int iterations;

		public Worker(int id, int iterations) {
			this.id = id;
			this.iterations = iterations;
		}

		@Override
		public Void call() throws Exception {
			for (int i = 0; i < iterations; ++i) {
				logger.info("ID:" + id + "#" + i + "A");
			}
			return null;
		}

	}

	@Test
	public void comparisonTest() throws Exception {
		SimpleFormatter sf = new SimpleFormatter("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS.%1$tL %4$-7s %3$-75s '%5$s'%13$s [%9$s]%n");
		LogRecord lr = new LogRecord(Level.INFO, "Hello, world!");
		lr.setLoggerName(CompiledFormatterTest.class.getName());
		Throwable throwable = new Throwable("level 3", new Throwable("level 2", new Throwable("level 1")));
		throwable.addSuppressed(new Exception("Suppressed"));
		lr.setThrown(throwable);
		Logger logger = Logger.getLogger(CompiledFormatterTest.class);
		logger.pushContext("context1");

		int iterations = 1;

		System.out.println("NEW......");
		long start = System.currentTimeMillis();
		for (int i = 0; i < iterations; ++i) {
			sf.format(lr);
			System.out.println(StringTools.asciiBoxMessage(sf.format(lr), -1));
		}
		long end = System.currentTimeMillis();
		System.out.println("New: " + iterations + " took " + (end - start) + " ms");

		System.out.println("OLD......");
		sf = new SimpleFormatter("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS.%1$tL %4$-7s %3$-75s '%5$s'%6$s [%9$s]%n");
		start = System.currentTimeMillis();
		for (int i = 0; i < iterations; ++i) {
			sf.format(lr);
			System.out.println(StringTools.asciiBoxMessage(sf.format(lr), -1));
		}
		end = System.currentTimeMillis();
		System.out.println("Old: " + iterations + " took " + (end - start) + " ms");

		logger.popContext();
		// 100000 took 2557 ms
		// 100000 took 2685 ms
		// 100000 took 2649 ms
	}

	@Test
	public void performanceComparisonTest() throws Exception {
		SimpleFormatter sf = new SimpleFormatter("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS.%1$tL %4$-7s %3$-75s '%5$s'%13$s [%9$s]%n");
		LogRecord lr = new LogRecord(Level.INFO, "Hello, world!");
		lr.setThrown(new Throwable());
		Logger logger = Logger.getLogger(CompiledFormatterTest.class);
		logger.pushContext("context1");

		int iterations = 100000;

		long start = System.currentTimeMillis();
		for (int i = 0; i < iterations; ++i) {
			sf.format(lr);
		}
		long end = System.currentTimeMillis();
		System.out.println("New: " + iterations + " took " + (end - start) + " ms");

		sf = new SimpleFormatter("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS.%1$tL %4$-7s %3$-75s '%5$s'%6$s [%9$s]%n");
		start = System.currentTimeMillis();
		for (int i = 0; i < iterations; ++i) {
			sf.format(lr);
		}
		end = System.currentTimeMillis();
		System.out.println("Old: " + iterations + " took " + (end - start) + " ms");

		sf = new SimpleFormatter("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS.%1$tL %4$-7s %3$-75s '%5$s'%13$s [%9$s]%n");
		start = System.currentTimeMillis();
		for (int i = 0; i < iterations; ++i) {
			sf.format(lr);
		}
		end = System.currentTimeMillis();
		System.out.println("New: " + iterations + " took " + (end - start) + " ms");

		sf = new SimpleFormatter("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS.%1$tL %4$-7s %3$-75s '%5$s'%6$s [%9$s]%n");
		start = System.currentTimeMillis();
		for (int i = 0; i < iterations; ++i) {
			sf.format(lr);
		}
		end = System.currentTimeMillis();
		System.out.println("Old: " + iterations + " took " + (end - start) + " ms");

		sf = new SimpleFormatter("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS.%1$tL %4$-7s %3$-75s '%5$s'%13$s [%9$s]%n");
		start = System.currentTimeMillis();
		for (int i = 0; i < iterations; ++i) {
			sf.format(lr);
		}
		end = System.currentTimeMillis();
		System.out.println("New: " + iterations + " took " + (end - start) + " ms");

		sf = new SimpleFormatter("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS.%1$tL %4$-7s %3$-75s '%5$s'%6$s [%9$s]%n");
		start = System.currentTimeMillis();
		for (int i = 0; i < iterations; ++i) {
			sf.format(lr);
		}
		end = System.currentTimeMillis();
		System.out.println("Old: " + iterations + " took " + (end - start) + " ms");

		logger.popContext();
		// 100000 took 2557 ms
		// 100000 took 2685 ms
		// 100000 took 2649 ms
	}

	@Test
	public void performanceTest() throws Exception {
		SimpleFormatter sf = new SimpleFormatter("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS.%1$tL %4$-7s %3$-75s '%5$s'%13$s [%9$s]%n");
		LogRecord lr = new LogRecord(Level.INFO, "Hello, world!");
		lr.setThrown(new Throwable());
		Logger logger = Logger.getLogger(CompiledFormatterTest.class);
		logger.pushContext("context1");

		int iterations = 100000;

		long start = System.currentTimeMillis();
		for (int i = 0; i < iterations; ++i) {
			sf.format(lr);
		}
		long end = System.currentTimeMillis();
		System.out.println("New: " + iterations + " took " + (end - start) + " ms");

		start = System.currentTimeMillis();
		for (int i = 0; i < iterations; ++i) {
			sf.format(lr);
		}
		end = System.currentTimeMillis();
		System.out.println("New: " + iterations + " took " + (end - start) + " ms");

		start = System.currentTimeMillis();
		for (int i = 0; i < iterations; ++i) {
			sf.format(lr);
		}
		end = System.currentTimeMillis();
		System.out.println("New: " + iterations + " took " + (end - start) + " ms");

		logger.popContext();
		// 100000 took 2557 ms
		// 100000 took 2685 ms
		// 100000 took 2649 ms
	}

	@Test
	public void performanceTestOld() throws Exception {
		SimpleFormatter sf = new SimpleFormatter("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS.%1$tL %4$-7s %3$-75s '%5$s'%6$s [%9$s]%n");
		LogRecord lr = new LogRecord(Level.INFO, "Hello, world!");
		lr.setThrown(new Throwable());
		Logger logger = Logger.getLogger(CompiledFormatterTest.class);
		logger.pushContext("context1");

		int iterations = 100000;
		int runs = 5;

		for (int j = 0; j < runs; ++j) {
			long start = System.currentTimeMillis();
			for (int i = 0; i < iterations; ++i) {
				sf.format(lr);
			}
			long end = System.currentTimeMillis();
			System.out.println("Old: " + iterations + " took " + (end - start) + " ms");
		}

		logger.popContext();
		// 100000 took 2557 ms
		// 100000 took 2685 ms
		// 100000 took 2649 ms
	}

	@Test
	public void performanceTestNew() throws Exception {
		SimpleFormatter sf = new SimpleFormatter("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS.%1$tL %4$-7s %3$-75s '%5$s'%13$s [%9$s]%n");
		LogRecord lr = new LogRecord(Level.INFO, "Hello, world!");
		lr.setThrown(new Throwable());
		Logger logger = Logger.getLogger(CompiledFormatterTest.class);
		logger.pushContext("context1");

		int iterations = 100000;
		int runs = 5;

		for (int j = 0; j < runs; ++j) {
			long start = System.currentTimeMillis();
			for (int i = 0; i < iterations; ++i) {
				sf.format(lr);
			}
			long end = System.currentTimeMillis();
			System.out.println("Old: " + iterations + " took " + (end - start) + " ms");
		}

		logger.popContext();
		// 100000 took 2557 ms
		// 100000 took 2685 ms
		// 100000 took 2649 ms
	}

	@Test
	public void iso8601UTCTest() throws Exception {
		SimpleFormatter sf1 = new SimpleFormatter("%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS.%1$tL%1$tz %4$-7s %3$-75s '%5$s'%13$s%n");
		SimpleFormatter sf2 = new SimpleFormatter("%14$s %4$-7s %3$-75s '%5$s'%13$s%n");
		LogRecord lr = new LogRecord(Level.INFO, "Hello, world!");
		lr.setLoggerName(CompiledFormatterTest.class.getName());

		String classicResult = null;
		String iso8601Result = null;
		int runs = 3;
		int iterations = 100000;

		for (int j = 0; j < runs; ++j) {
			System.out.println("Standard......");
			long start = System.currentTimeMillis();
			for (int i = 0; i < iterations; ++i) {
				sf1.format(lr);
				if (i == 0 && j == 0) {
					classicResult = sf1.format(lr);
					System.out.println(StringTools.asciiBoxMessage(classicResult, -1));
				}
			}
			long end = System.currentTimeMillis();
			System.out.println("Standard: " + iterations + " took " + (end - start) + " ms");

			System.out.println("ISO8601......");
			start = System.currentTimeMillis();
			for (int i = 0; i < iterations; ++i) {
				sf2.format(lr);
				if (i == 0 && j == 0) {
					iso8601Result = sf2.format(lr);
					System.out.println(StringTools.asciiBoxMessage(iso8601Result, -1));
				}
			}
			end = System.currentTimeMillis();
			System.out.println("ISO8601: " + iterations + " took " + (end - start) + " ms");
		}

		Assert.assertEquals(classicResult, iso8601Result);
	}

	@Test
	public void threadTest() throws Exception {
		Thread currentThread = Thread.currentThread();
		String oldName = currentThread.getName();
		currentThread.setName("test");
		try {
			SimpleFormatter sf = new SimpleFormatter("%5$s [%15$s]%n");
			LogRecord lr = new LogRecord(Level.INFO, "Hello, world!");
			lr.setLoggerName(CompiledFormatterTest.class.getName());

			String format = sf.format(lr);
			Assert.assertEquals(format.trim(), "Hello, world! [test]");
		} finally {
			currentThread.setName(oldName);
		}
	}
}
