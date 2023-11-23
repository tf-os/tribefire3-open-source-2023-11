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

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.utils.date.NanoClock;

/*
 * There are additional tests available in LoggingJuliExtensionsTest
 */
public class CompiledFormatterTest {

	@Test
	public void testStringParameters() throws Exception {
		String format = "%4$-7s %7$-33s '%5$s' %6$s [%9$s]";

		String formatResult = String.format(format, "1", "2", "3", "4", "5", "6", "7", "8", "9");
		CompiledFormatter cf = new CompiledFormatter(format);
		String compiledFormatResult = cf.format("1", "2", "3", "4", "5", "6", "7", "8", "9");

		Assert.assertEquals(formatResult, compiledFormatResult);
	}

	@Test
	public void testDoubleParameters() throws Exception {
		String format = "%4$-7s %7$-33s '%5$s' %6$s [%9$e]";

		String formatResult = String.format(format, "1", "2", "3", "4", "5", "6", "7", "8", Double.valueOf(1.2f));
		CompiledFormatter cf = new CompiledFormatter(format);
		String compiledFormatResult = cf.format("1", "2", "3", "4", "5", "6", "7", "8", Double.valueOf(1.2f));

		Assert.assertEquals(formatResult, compiledFormatResult);
	}

	@Test
	public void testPerformanceWithStrings() throws Exception {
		String format = "%4$-7s %7$-33s '%5$s' %6$s [%9$s]";

		int count = 10000;
		String formatResult = null;
		String compiledFormatResult = null;

		long start1 = System.currentTimeMillis();
		for (int i = 0; i < count; ++i) {
			String result = String.format(format, "1", "2", "3", "4", "5", "6", "7", "8", "9");
			if (formatResult == null) {
				formatResult = result;
			}
			// System.out.println(result);
		}
		long stop1 = System.currentTimeMillis();
		long formatDuration = stop1 - start1;

		CompiledFormatter cf = new CompiledFormatter(format);
		long start2 = System.currentTimeMillis();
		for (int i = 0; i < count; ++i) {
			String result = cf.format("1", "2", "3", "4", "5", "6", "7", "8", "9");
			// System.out.println(result);
			if (compiledFormatResult == null) {
				compiledFormatResult = result;
			}
		}
		long stop2 = System.currentTimeMillis();
		long compiledFormatDuration = stop2 - start2;

		Assert.assertEquals(formatResult, compiledFormatResult);
		Assert.assertTrue("Formatter: " + formatDuration + ", CompiledFormatter: " + compiledFormatDuration, formatDuration > compiledFormatDuration);
	}

	@Test
	public void testPerformanceWithDoubles() throws Exception {
		String format = "%4$-7s %7$-33s '%5$s' %6$s [%9$e]";

		int count = 10000;
		String formatResult = null;
		String compiledFormatResult = null;

		long start1 = System.currentTimeMillis();
		for (int i = 0; i < count; ++i) {
			String result = String.format(format, "1", "2", "3", "4", "5", "6", "7", "8", Double.valueOf(1.2f));
			if (formatResult == null) {
				formatResult = result;
			}
			// System.out.println(result);
		}
		long stop1 = System.currentTimeMillis();
		long formatDuration = stop1 - start1;

		CompiledFormatter cf = new CompiledFormatter(format);
		long start2 = System.currentTimeMillis();
		for (int i = 0; i < count; ++i) {
			String result = cf.format("1", "2", "3", "4", "5", "6", "7", "8", Double.valueOf(1.2f));
			// System.out.println(result);
			if (compiledFormatResult == null) {
				compiledFormatResult = result;
			}
		}
		long stop2 = System.currentTimeMillis();
		long compiledFormatDuration = stop2 - start2;

		Assert.assertEquals(formatResult, compiledFormatResult);
		// Removing this test as there might be a technical problem or a short devience in the performance
		// Assert.assertTrue("Formatter: " + formatDuration + ", CompiledFormatter: " + compiledFormatDuration,
		// formatDuration > compiledFormatDuration);

		System.out.println("testPerformanceWithDoubles:");
		System.out.println("Formatter: " + formatDuration + "\nCompiledFormatter: " + compiledFormatDuration);
	}

	@Test
	public void testStringsWithoutIndices() throws Exception {
		String format = "%s %s '%s'";

		String formatResult = String.format(format, "1", "2", "3");

		CompiledFormatter cf = new CompiledFormatter(format);
		String compiledFormatResult = cf.format("1", "2", "3");

		Assert.assertEquals(formatResult, compiledFormatResult);
	}

	@Test
	public void testDoublesWithoutIndices() throws Exception {
		String format = "%e %e '%e'";

		String formatResult = String.format(format, Double.valueOf(1.2f), Double.valueOf(1.3f), Double.valueOf(1.4f));

		CompiledFormatter cf = new CompiledFormatter(format);
		String compiledFormatResult = cf.format(Double.valueOf(1.2f), Double.valueOf(1.3f), Double.valueOf(1.4f));

		Assert.assertEquals(formatResult, compiledFormatResult);
	}

	@Test
	public void testInvalidFormat() {
		try {
			CompiledFormatter cf = new CompiledFormatter("%$s");
			Assert.fail("CompiledFormatter accepted wrong format: " + cf.toString());
		} catch (Throwable t) {
			// This is where we want to be
		}
	}

	@Test
	public void testMultithreaded() {

		final int workers = 100;
		final int iterations = 1000;

		String format = "%4$-7s %7$-33s '%5$s' %6$s [%9$s]";
		String formatResult = String.format(format, "1", "2", "3", "4", "5", "6", "7", "8", "9");
		CompiledFormatter cf = new CompiledFormatter(format);

		ExecutorService service = Executors.newFixedThreadPool(workers);
		try {

			List<Future<?>> futures = new ArrayList<>();

			Instant start = NanoClock.INSTANCE.instant();
			for (int i = 0; i < workers; ++i) {

				futures.add(service.submit(new Runnable() {

					@Override
					public void run() {

						for (int j = 0; j < iterations; ++j) {
							try {
								String compiledFormatResult = cf.format("1", "2", "3", "4", "5", "6", "7", "8", "9");
								Assert.assertEquals(formatResult, compiledFormatResult);
							} catch (Exception e) {
								throw new RuntimeException(e);
							}
						}
					}

				}));

			}

			futures.forEach(e -> {
				try {
					e.get();
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			});
			Instant stop = NanoClock.INSTANCE.instant();
			Duration duration = Duration.between(start, stop);
			System.out.println("" + (workers * iterations) + " formatting operations took: " + StringTools.prettyPrintDuration(duration, true, null));
		} finally {
			service.shutdown();
		}
	}
}
