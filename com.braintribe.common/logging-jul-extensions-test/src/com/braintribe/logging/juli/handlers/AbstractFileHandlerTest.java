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
package com.braintribe.logging.juli.handlers;

import java.io.File;
import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.braintribe.utils.FileTools;
import com.braintribe.utils.StringTools;

/**
 * Abstract super class for the test classes which test the different file handlers. Provides a test that logs using multiple threads (some through
 * the same, some through different classes, i.e. loggers).
 *
 * @author michael.lafite
 */
public abstract class AbstractFileHandlerTest {

	protected void runMultiThreadLoggingTest() {

		final String testId = getClass().getSimpleName();

		File configFile = new File("res/" + testId + "/logging.properties");
		System.out.println(configFile.getAbsolutePath());

		try (FileInputStream fis = new FileInputStream(configFile)) {
			LogManager.getLogManager().readConfiguration(fis);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		final String relativeLogFileDirPath = "logs/" + testId;
		final File logFileDir = new File(relativeLogFileDirPath);
		if (logFileDir.exists()) {
			FileTools.deleteForcedly(logFileDir);
		}
		if (!logFileDir.mkdirs()) {
			throw new RuntimeException("Couldn't create dir " + logFileDir + "!");
		}

		final int numberOfLogMessagesPerLoggingClass = 1000;
		final int numberOfLoggingClasses = 100;
		final ThreadPoolExecutor executor = new ThreadPoolExecutor(numberOfLoggingClasses, numberOfLoggingClasses, 10, TimeUnit.SECONDS,
				new ArrayBlockingQueue<Runnable>(numberOfLoggingClasses));
		for (int loggerNumber = 1; loggerNumber <= numberOfLoggingClasses; loggerNumber++) {
			final AbstractLoggingClass loggingClass = (loggerNumber % 2 == 0) ? new LoggingClass1() : new LoggingClass2();
			loggingClass.init(numberOfLogMessagesPerLoggingClass, numberOfLoggingClasses * numberOfLogMessagesPerLoggingClass,
					"logger " + toStringWithLeadingZeros(loggerNumber, numberOfLoggingClasses));
			executor.submit(loggingClass);
		}

		executor.shutdown();
		try {
			if (!executor.awaitTermination(100, TimeUnit.SECONDS)) {
				throw new RuntimeException("Termination failed! (timeout reached)");
			}
		} catch (final InterruptedException e) {
			throw new RuntimeException("Termination failed! (interrupted)");
		}
	}

	private static String toStringWithLeadingZeros(final long number, final long maxNumber) {
		return new DecimalFormat(StringTools.getFilledString(String.valueOf(maxNumber).length(), '0')).format(number);
	}

	/**
	 * This abstract class and the two implementations are used to test logging from multiple threads using the same logger (e.g. two instances of
	 * {@link LoggingClass1}) or different loggers (one instance of {@link LoggingClass1} and one of {@link LoggingClass2});
	 *
	 * @author michael.lafite
	 */
	private static abstract class AbstractLoggingClass implements Runnable {

		private static AtomicLong messageCount = new AtomicLong(0);

		private String loggingClassId;
		private long maxMessageCount;
		private int numberOfMessagesToLog;

		@SuppressWarnings("hiding")
		private void init(final int numberOfMessagesToLog, final long maxMessageCount, final String loggingClassId) {
			this.numberOfMessagesToLog = numberOfMessagesToLog;
			this.maxMessageCount = maxMessageCount;
			this.loggingClassId = loggingClassId;
		}

		@Override
		public void run() {
			for (int i = 0; i < this.numberOfMessagesToLog; i++) {
				final String message = "This is log message number " + toStringWithLeadingZeros(messageCount.addAndGet(1), this.maxMessageCount)
						+ " logged by " + this.loggingClassId + ".";
				log(message);
			}
		}

		protected abstract void log(String message);

	}

	private static class LoggingClass1 extends AbstractLoggingClass {

		private static Logger logger = Logger.getLogger(LoggingClass1.class.getName());

		@Override
		protected void log(final String message) {
			logger.info(message);
		}
	}

	private static class LoggingClass2 extends AbstractLoggingClass {
		private static Logger logger = Logger.getLogger(LoggingClass2.class.getName());

		@Override
		protected void log(final String message) {
			logger.info(message);
		}
	}

}
