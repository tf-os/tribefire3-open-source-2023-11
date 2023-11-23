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
package com.braintribe.logging;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThatExecuting;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.StreamHandler;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.common.lcd.Numbers;
import com.braintribe.logging.Logger.LogLevel;
import com.braintribe.logging.juli.formatters.simple.SimpleFormatter;
import com.braintribe.testing.category.Slow;
import com.braintribe.testing.test.AbstractTest;
import com.braintribe.utils.CommonTools;
import com.braintribe.utils.lcd.StopWatch;

/**
 * Provides tests for {@link Logger}.
 */
public class LoggerTest extends AbstractTest {

	protected ByteArrayOutputStream logBuffer = null;
	protected StreamHandler streamHandler = null;

	@Before
	public void initialize() throws Exception {

		if (this.streamHandler == null) {
			this.logBuffer = new ByteArrayOutputStream();
			this.streamHandler = new StreamHandler(this.logBuffer,
					new SimpleFormatter("%4$-7s %7$-33s '%5$s' %6$s [%9$s - %10$s]%n"));

			java.util.logging.Logger logger = java.util.logging.Logger.getLogger(LoggerTest.class.getName());
			logger.addHandler(this.streamHandler);
		}
	}

	@Ignore
	protected String getLastLogLine() throws Exception {
		this.streamHandler.flush();
		BufferedReader br = new BufferedReader(
				new InputStreamReader(new ByteArrayInputStream(this.logBuffer.toByteArray()), "UTF-8"));
		String line = null;
		String lastLine = null;
		while ((line = br.readLine()) != null) {
			lastLine = line;
		}
		br.close();
		this.logBuffer.reset();
		if (lastLine != null) {
			lastLine = lastLine.trim();
		}
		return lastLine;
	}

	@Test
	public void testWithNoParameters() throws Exception {

		Logger logger = Logger.getLogger(LoggerTest.class);

		logger.info(null, "Test Message");

		String lastLine = this.getLastLogLine();

		assertThat(lastLine).contains("Test Message");
	}

	@Test
	public void testWithSimpleParameters() throws Exception {

		Logger logger = Logger.getLogger(LoggerTest.class);

		logger.info(null, "Test Message: %1$s", "Hello, World");

		String lastLine = this.getLastLogLine();

		assertThat(lastLine).contains("Test Message: Hello, World");
	}

	@Test
	public void testWithLambdaParameters() throws Exception {

		Logger logger = Logger.getLogger(LoggerTest.class);

		List<String> collection = new ArrayList<>();
		collection.add("Hello, World");

		logger.info(null, "Test Message: %1$d", (Supplier<Object>) collection::size);

		String lastLine = this.getLastLogLine();

		assertThat(lastLine).contains("Test Message: 1");
	}

	@Test
	public void testWithMixedParameters() throws Exception {

		Logger logger = Logger.getLogger(LoggerTest.class);

		List<String> collection = new ArrayList<>();
		collection.add("Hello, World");

		logger.info(null, "%1$s %2$d %3$s", "Collection Size:", (Supplier<Object>) collection::size, "elements");

		String lastLine = this.getLastLogLine();

		assertThat(lastLine).contains("Collection Size: 1 elements");
	}

	@Test
	public void testLambdaOnlyParameter() throws Exception {
		assertThat(logger.isTraceEnabled()).isFalse();

		// throwDummyException() must not be called, since trace level is disabled
		logger.trace(() -> throwDummyException());
		logger.trace(() -> "x" + throwDummyException() + "x");

		assertThat(logger.isInfoEnabled()).isTrue();
		assertThatExecuting(() -> logger.info(() -> throwDummyException())).fails()
				.with(IllegalArgumentException.class);

		logger.trace(() -> "trace - " + computeString());
		logger.debug(() -> "debug - " + computeString());
		logger.info(() -> "info - " + computeString());
		logger.warn(() -> "warn - " + computeString());
		logger.error(() -> "error - " + computeString());
		logger.log(LogLevel.INFO, () -> "log - " + computeString());

		try {
			throwDummyException();
		} catch (Exception e) {
			logger.trace(() -> "trace - " + computeString(), e);
			logger.debug(() -> "debug - " + computeString(), e);
			logger.info(() -> "info - " + computeString(), e);
			logger.warn(() -> "warn - " + computeString(), e);
			logger.error(() -> "error - " + computeString(), e);
			logger.log(LogLevel.INFO, () -> "log - " + computeString(), e);
		}

		// again make sure that expensive string computation is skipped
		assertThatExecuting(() -> logger.trace(() -> "trace - " + computeString_expensive())).succeeds()
				.afterLessThan(5, ChronoUnit.MILLIS);
		assertThatExecuting(() -> logger.trace(() -> "trace - " + computeString_expensive(), new Exception()))
				.succeeds().afterLessThan(5, ChronoUnit.MILLIS);
	}

	private static String throwDummyException() {
		throw new IllegalArgumentException("dummy exception");
	}

	private static String computeString() {
		return "(result of computeString)";
	}

	private static String computeString_expensive() {
		// fake expensive string computation
		CommonTools.sleep(1000);
		return "(result of computeString_expensive)";
	}

	@Category(Slow.class)
	@Test
	public void testLambdaPerformance() throws Exception {
		assertThat(logger.isTraceEnabled()).isFalse();

		long iterations = Numbers.BILLION;

		long ifCheckTime = 0;
		long lambdaTime = 0;
		long anonymousClassTime = 0;

		final File exampleObjectPassedToLogMethod = new File("a/b/c");

		final int testRuns = 5;
		for (int run = 0; run < testRuns; run++) {

			// performance test
			{
				StopWatch stopWatch = new StopWatch();
				for (int i = 0; i < iterations; i++) {
					if (logger.isTraceEnabled()) {
						logger.trace("some message that won't be logged anyway " + exampleObjectPassedToLogMethod);
					}
				}
				ifCheckTime = stopWatch.getElapsedTime();
			}
			{
				StopWatch stopWatch = new StopWatch();
				for (int i = 0; i < iterations; i++) {
					logger.trace(() -> "some message that won't be logged anyway " + exampleObjectPassedToLogMethod);
				}
				lambdaTime = stopWatch.getElapsedTime();
			}
			{
				StopWatch stopWatch = new StopWatch();
				for (int i = 0; i < iterations; i++) {
					// this not really an alternative, but it's interesting to see how it performs compared to lambda
					logger.trace(new Supplier<String>() {
						@Override
						public String get() {
							return "some message that won't be logged anyway " + exampleObjectPassedToLogMethod;
						}
					});
				}
				anonymousClassTime = stopWatch.getElapsedTime();
			}

			int lambdaPercentage = (int) (100d / ifCheckTime * lambdaTime);
			int anonymousClassPercentage = (int) (100d / ifCheckTime * anonymousClassTime);

			String msg = "Performance comparison for guarding log messages: if-check=" + ifCheckTime
					+ "(100%) vs lambda=" + lambdaTime + "(" + lambdaPercentage + "%) vs anonymous class="
					+ anonymousClassTime + "(" + anonymousClassPercentage + "%)";
			logger.info(msg);
			System.out.println(msg);

			// assert that lambda is less than 5 times slower
			// (sounds much, but in absolute numbers both solutions have almost no performance impact)
			assertThat(lambdaPercentage).isLessThan(500);
		}
	}

}
