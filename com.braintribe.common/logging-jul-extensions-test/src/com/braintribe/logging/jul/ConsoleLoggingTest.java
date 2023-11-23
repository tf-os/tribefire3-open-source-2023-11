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
package com.braintribe.logging.jul;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.logging.LogManager;

import org.junit.Test;

import com.braintribe.logging.Logger;
import com.braintribe.utils.CommonTools;
import com.braintribe.utils.ReflectionTools;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.ThrowableTools;

/**
 * Tests logging to <code>System.out/err</code> in combination with logging to console via JUL. (Purpose of this test was to find a strange issue
 * where <code>System.err</code> logs were not printed at all when also using JUL and our own
 * {@link com.braintribe.logging.juli.handlers.ConsoleHandler}.)
 *
 * @author michael.lafite
 */
public class ConsoleLoggingTest {

	private static Logger logger = Logger.getLogger(ConsoleLoggingTest.class);

	// @formatter:off
	private static String JUL_CONFIG =
			".level = FINE\n" +
			"java.util.logging.SimpleFormatter.format=%4$-7s %5$s%n\n" +
			"handlers = com.braintribe.logging.juli.handlers.ConsoleHandler\n"+
			"com.braintribe.logging.juli.handlers.ConsoleHandler.level = INFO\n" +
			"com.braintribe.logging.juli.handlers.ConsoleHandler.formatter = java.util.logging.SimpleFormatter\n";
	// @formatter:on

	@Test
	public void testSystemErrorLogging() {

		// remember streams to make sure they are not changed (
		ExpectedStreams expectedStreams = new ExpectedStreams();

		// check streams
		checkStreams(expectedStreams);

		/* create a thread that (in addition) also checks streams in background. This can be useful in combination with debugging, where one steps
		 * through (JDK) code in one thread and streams are constantly checked in the background by a second thread. */
		StreamsChecker streamsChecker = new StreamsChecker(expectedStreams);
		new Thread(streamsChecker).start();

		log("before config");

		checkStreams(expectedStreams);

		try {
			LogManager.getLogManager().readConfiguration(StringTools.toInputStream(JUL_CONFIG));
		} catch (IOException e) {
			throw new RuntimeException("Error while reading logging configuration from input stream!", e);
		}

		checkStreams(expectedStreams);

		log("after config");

		checkStreams(expectedStreams);

		logger.info("JUL logging");
		logger.info("logging <null> message:");
		logger.info((String) null);

		checkStreams(expectedStreams);

		log("after JUL logging");

		checkStreams(expectedStreams);

		streamsChecker.stop = true;

		CommonTools.sleep(StreamsChecker.CHECK_INTERVAL * 3);
		assertThat(streamsChecker.stopped).isTrue();
		assertThat(streamsChecker.error).isNull();
	}

	private static class StreamsChecker implements Runnable {

		private static final int CHECK_INTERVAL = 10;

		private ExpectedStreams expectedStreams;
		private boolean stop;
		private boolean stopped;
		private String error;

		public StreamsChecker(ExpectedStreams expectedStreams) {
			this.expectedStreams = expectedStreams;
		}

		@Override
		public void run() {
			// if there is a problem with System.out, this logging might not work
			System.out.println("Checking streams...");
			while (!stop) {
				try {
					checkStreams(expectedStreams);
					CommonTools.sleep(CHECK_INTERVAL);
				} catch (AssertionError e) {
					error = "Streams are no longer valid!\n" + ThrowableTools.getStackTraceString(e);
					System.out.println(error);
					stop = true;
				} catch (Throwable t) {
					error = "Unexpected problem in " + StreamsChecker.class.getSimpleName() + "!!\n" + ThrowableTools.getStackTraceString(t);
					System.out.println(error);
					stop = true;
				}
			}
			System.out.println("Done checking streams.");
			stopped = true;
		}

	}

	private static class ExpectedStreams {
		PrintStream outPrintStream = System.out;
		PrintStream errPrintStream = System.err;
		OutputStream outOutputStream = getOutputStream(outPrintStream);
		OutputStream errOutputStream = getOutputStream(errPrintStream);
	}

	private static void log(String message) {
		CommonTools.sleep(1); // sleep to ensure log file order
		System.out.println("out: " + message);
		CommonTools.sleep(1);
		System.err.println("err: " + message);
		CommonTools.sleep(1);
	}

	private static void checkStreams(ExpectedStreams expectedStreams) {
		assertThat(System.out).isSameAs(expectedStreams.outPrintStream);
		assertThat(getOutputStream(System.out)).isNotNull().isSameAs(expectedStreams.outOutputStream);
		assertThat(System.out.checkError()).isFalse();

		assertThat(System.err).isSameAs(expectedStreams.errPrintStream);
		assertThat(getOutputStream(System.err)).isNotNull().isSameAs(expectedStreams.errOutputStream);
		assertThat(System.err.checkError()).isFalse();
	}

	private static OutputStream getOutputStream(PrintStream printStream) {
		return (OutputStream) ReflectionTools.getFieldValue("out", printStream);
	}
}
