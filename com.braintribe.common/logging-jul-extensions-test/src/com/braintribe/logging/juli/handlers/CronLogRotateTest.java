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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.util.UUID;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.testing.category.Slow;
import com.braintribe.utils.FileTools;

@Category(Slow.class)
public class CronLogRotateTest {

	protected static File logFileDir;

	@BeforeClass
	public static void beforeClass() throws Exception {
		final String testId = CronLogRotateTest.class.getSimpleName();

		File configFile = new File("res/" + testId + "/logging.properties");
		System.out.println(configFile.getAbsolutePath());

		try (FileInputStream fis = new FileInputStream(configFile)) {
			LogManager.getLogManager().readConfiguration(fis);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		final String relativeLogFileDirPath = "logs/" + testId;
		logFileDir = new File(relativeLogFileDirPath);
	}

	@AfterClass
	public static void afterClass() throws Exception {
		if (logFileDir.exists()) {
			FileTools.deleteForcedly(logFileDir);
		}
	}

	@Before
	public void before() throws Exception {
		if (logFileDir.exists()) {
			FileTools.deleteForcedly(logFileDir);
		}
		if (!logFileDir.mkdirs()) {
			throw new RuntimeException("Couldn't create dir " + logFileDir + "!");
		}
	}

	@Test
	public void testCronLogRotate() throws Exception {

		// Log config is configured to rotate every 10 seconds
		// We will write logs for a minute and check whether we 5-6 non-empty files

		LoggingClass1 lc = new LoggingClass1();
		lc.run();

		File[] listFiles = logFileDir.listFiles((FilenameFilter) (dir, name) -> name.startsWith("cronLogRotateTest"));

		assertThat(listFiles != null);
		assertThat(listFiles.length >= 5);
		assertThat(listFiles.length <= 7);
		for (File f : listFiles) {
			assertThat(f.length() > 0);
		}
	}

	private static class LoggingClass1 {

		private static Logger logger = Logger.getLogger(LoggingClass1.class.getName());

		protected void run() throws Exception {
			long start = System.currentTimeMillis();
			do {
				logger.info("This is a log message for your convenience. Here's an ID: " + UUID.randomUUID().toString());
				Thread.sleep(500L);
			} while ((System.currentTimeMillis() - start) < 60_000L);
		}
	}
}
