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
package com.braintribe.common;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.common.SimpleDirectoryCleaner.CleanDirectorySpecs;
import com.braintribe.logging.Logger;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.utils.DateTools;
import com.braintribe.utils.FileTools;

/**
 * Provides tests for {@link SimpleDirectoryCleaner}.
 *
 *
 */

public class SimpleDirectoryCleanerTest {

	private static Logger logger = Logger.getLogger(SimpleDirectoryCleanerTest.class);
	private String rootFolderPath = "./SimpleDirectoryCleanerTest";
	private File rootFolder = null;

	private SimpleDirectoryCleaner.CleanDirectorySpecs directorySpecs = new SimpleDirectoryCleaner.CleanDirectorySpecs();
	private final SimpleDirectoryCleaner simpleDirectoryCleaner = new SimpleDirectoryCleaner();

	// Empty directory test
	private static final String subFolderPathEmptyDirCheck = "/dir1/subdir1/";
	private static final int numberOfSubDirectoriesExpectedBeforeCleanUp = 1;
	private static final int numberOfSubDirectoriesExpectedAfterCleanUp = 0;

	// Regular expression test
	private static final String subFolderPathRegexCheck = "dir1/";
	private static final String filenameForRegexCheck1 = "AAAfilename";
	private static final String filenameForRegexCheck2 = "filBBBename";
	private static final String filenameForRegexCheck3 = "AAAfilBBBename";
	private static final String regexExpressionIncluded = "AAA.*";
	private static final String regexExpressionExcluded = ".*BBB.*";
	private static final int numberOfFilesExpectedBeforeCleanUp = 3;
	private static final int numberOfFilesExpectedAfterCleanUpWithOnlyIncludedRegex = 1;
	private static final int numberOfFilesExpectedAfterCleanUpWithOnlyExcludedRegex = 2;
	private static final int numberOfFilesExpectedAfterCleanUpWithIncludedAndExcludedRegex = 2;

	// Files' time modifications
	private static final String filenameCreatedBeforeLastTimeModification = "BeforeTimeModificationFile";
	private static final String filenameCreatedAfterLastTimeModification = "AfterTimeModificationFile";
	private static final long millisSinceLastFileTimeModification = 10000L; // modification time equals 10 seconds ago
	private static final int numberOfFilesExpectedAfterCleanUpForTimeModification = 1;

	@Before
	public void setUp() {
		logger.info("Creating a temp root folder for the testing perpose of the SimpleDirectoryCleaner task");
		this.rootFolderPath = this.rootFolderPath.concat(DateTools.encode(new Date(), DateTools.TERSE_DATETIME_FORMAT)).concat("/");
		this.rootFolder = FileTools.createNewTempDir(this.rootFolderPath);
		this.directorySpecs.setBaseDir(this.rootFolder);
	}

	@After
	public void tearDown() throws IOException {
		logger.info("Finalizing stage - Deleting the temp root folder for the testing purpose of the SimpleDirectoryCleaner task.");
		FileTools.deleteDirectoryRecursively(rootFolder);
	}

	@Test
	@Category(KnownIssue.class)
	public void testDeletionOfEmptyDirs() throws InterruptedException {
		logger.info("Starting empty directories Tests");
		logger.info("Creating empty directories");
		final String subFolderPath = this.rootFolderPath.concat(SimpleDirectoryCleanerTest.subFolderPathEmptyDirCheck);
		final File subRootFolder = FileTools.createNewTempDir(subFolderPath);
		assertThat(this.rootFolder.listFiles().length).isEqualTo(SimpleDirectoryCleanerTest.numberOfSubDirectoriesExpectedBeforeCleanUp);
		assertThat(subRootFolder).isDirectory();
		assertThat(subRootFolder).exists();
		logger.info("Created empty directories");

		logger.info("Setting up the SimpleDirectorySpecifications");
		setDirectorysSpecs(this.rootFolder, true, ".*", "", 0);

		logger.info("Perforimg clean up");
		printSimpleDirectoryCleanerDescription(simpleDirectoryCleaner);
		this.simpleDirectoryCleaner.perform();
		Thread.sleep(1000);

		assertThat(this.rootFolder.listFiles().length).isEqualTo(SimpleDirectoryCleanerTest.numberOfSubDirectoriesExpectedAfterCleanUp)
				.as("There should not be any files in the rootFolder!");
		assertThat(subRootFolder).doesNotExist().as("The sub directory " + subRootFolder.getName() + " should not exist after the clean up!");

		logger.info("Empty directories deleted");
		logger.info("Completed empty directories Tests");
	}

	@Test
	@Category(KnownIssue.class)
	public void testDeletionOfMatchedRegexFile() throws Exception {
		logger.info("Starting Regex Tests");
		File subfolder = createTemporaryFilesInsideSubDir(SimpleDirectoryCleanerTest.subFolderPathRegexCheck);
		logger.info("Starting regex test - only for included");
		logger.info("Setting up the SimpleDirectorySpecifications");
		setDirectorysSpecs(this.rootFolder, true, SimpleDirectoryCleanerTest.regexExpressionIncluded, "", 0);
		assertThat(subfolder.listFiles().length).isEqualTo(SimpleDirectoryCleanerTest.numberOfFilesExpectedBeforeCleanUp)
				.as("There should be" + SimpleDirectoryCleanerTest.numberOfFilesExpectedBeforeCleanUp + " in the " + subfolder);
		logger.info("Perforimg clean up");
		printSimpleDirectoryCleanerDescription(simpleDirectoryCleaner);
		this.simpleDirectoryCleaner.perform();
		assertThat(subfolder.listFiles().length).isEqualTo(SimpleDirectoryCleanerTest.numberOfFilesExpectedAfterCleanUpWithOnlyIncludedRegex)
				.as("There should be" + SimpleDirectoryCleanerTest.numberOfFilesExpectedBeforeCleanUp + " in the " + subfolder);
		logger.info("Completed regex test - only for included");
		for (final File file : subfolder.listFiles()) {
			file.delete();
		}
		subfolder.delete();

		subfolder = createTemporaryFilesInsideSubDir(SimpleDirectoryCleanerTest.subFolderPathRegexCheck);
		logger.info("Starting regex test - only for excluded");
		logger.info("Setting up the SimpleDirectorySpecifications");
		setDirectorysSpecs(this.rootFolder, true, ".*", SimpleDirectoryCleanerTest.regexExpressionExcluded, 0);
		assertThat(subfolder.listFiles().length).isEqualTo(SimpleDirectoryCleanerTest.numberOfFilesExpectedBeforeCleanUp)
				.as("There should be" + SimpleDirectoryCleanerTest.numberOfFilesExpectedBeforeCleanUp + " in the " + subfolder);
		logger.info("Perforimg clean up");
		printSimpleDirectoryCleanerDescription(simpleDirectoryCleaner);
		this.simpleDirectoryCleaner.perform();
		assertThat(subfolder.listFiles().length).isEqualTo(SimpleDirectoryCleanerTest.numberOfFilesExpectedAfterCleanUpWithOnlyExcludedRegex)
				.as("There should be" + SimpleDirectoryCleanerTest.numberOfFilesExpectedBeforeCleanUp + " in the " + subfolder);

		logger.info("Completed regex test - only for excluded");
		for (final File file : subfolder.listFiles()) {
			file.delete();
		}
		subfolder.delete();

		subfolder = createTemporaryFilesInsideSubDir(SimpleDirectoryCleanerTest.subFolderPathRegexCheck);
		logger.info("Starting regex test - for included and for excluded");
		logger.info("Setting up the SimpleDirectorySpecifications");
		setDirectorysSpecs(this.rootFolder, true, SimpleDirectoryCleanerTest.regexExpressionIncluded,
				SimpleDirectoryCleanerTest.regexExpressionExcluded, 0);
		assertThat(subfolder.listFiles().length).isEqualTo(SimpleDirectoryCleanerTest.numberOfFilesExpectedBeforeCleanUp)
				.as("There should be" + SimpleDirectoryCleanerTest.numberOfFilesExpectedBeforeCleanUp + " in the " + subfolder);
		logger.info("Perforimg clean up");
		printSimpleDirectoryCleanerDescription(simpleDirectoryCleaner);
		this.simpleDirectoryCleaner.perform();
		Thread.sleep(1000);
		assertThat(subfolder.listFiles().length).isEqualTo(SimpleDirectoryCleanerTest.numberOfFilesExpectedAfterCleanUpWithIncludedAndExcludedRegex)
				.as("There should be" + SimpleDirectoryCleanerTest.numberOfFilesExpectedBeforeCleanUp + " in the " + subfolder);

		logger.info("Completed regex test - only for excluded");
		logger.info("Completed Regex Tests");
	}

	@Test
	@Category(KnownIssue.class)
	public void testDeletionSinceFilesLastModification() throws Exception {
		logger.info("Starting files time modifications Tests");
		logger.info("Creating files with different modification times");
		assertThat(this.rootFolder.listFiles().length).isEqualTo(0);

		// Note: I had to change the test to work with 50 seconds instead of 50 milliseconds as
		// the precision of the last modified date is not consistent over different operating
		// systems and Java versions.
		// see: http://hg.openjdk.java.net/jdk8/jdk8/jdk/rev/06da87777d0e

		long before = SimpleDirectoryCleanerTest.millisSinceLastFileTimeModification + 50000;
		long after = SimpleDirectoryCleanerTest.millisSinceLastFileTimeModification - 50000;
		long tPoint = System.currentTimeMillis();
		long tBefore = tPoint - before;
		long tAfter = tPoint - after;
		logger.info("Time point before scheduled clean: " + tBefore);
		logger.info("Time point after scheduled clean: " + tAfter);
		assertThat(tBefore).isLessThan(tAfter);
		createTempFile(this.rootFolder, SimpleDirectoryCleanerTest.filenameCreatedBeforeLastTimeModification, before);
		createTempFile(this.rootFolder, SimpleDirectoryCleanerTest.filenameCreatedAfterLastTimeModification, after);
		assertThat(this.rootFolder.listFiles().length).isEqualTo(2);
		logger.info("Created files with different modification times");
		logger.info("Setting up the SimpleDirectorySpecifications");
		setDirectorysSpecs(this.rootFolder, false, ".*", "", SimpleDirectoryCleanerTest.millisSinceLastFileTimeModification);
		long tCrucial = System.currentTimeMillis() - SimpleDirectoryCleanerTest.millisSinceLastFileTimeModification;
		assertThat(tCrucial - tBefore).isGreaterThan(0);
		assertThat(tCrucial - tAfter).isLessThan(0);

		logger.info("Perforimg clean up");
		logger.info("Cleaning documents that were modified before: " + tCrucial);
		printSimpleDirectoryCleanerDescription(simpleDirectoryCleaner);
		this.simpleDirectoryCleaner.perform();

		assertThat(this.rootFolder.listFiles().length).isEqualTo(SimpleDirectoryCleanerTest.numberOfFilesExpectedAfterCleanUpForTimeModification).as(
				"There should be " + SimpleDirectoryCleanerTest.numberOfFilesExpectedAfterCleanUpForTimeModification + " file left after clean up");

		assertThat(this.rootFolder.listFiles()[0].getName())
				.isEqualTo(SimpleDirectoryCleanerTest.filenameCreatedAfterLastTimeModification.concat(".txt"))
				.as("The file expected after clean up is : " + SimpleDirectoryCleanerTest.filenameCreatedAfterLastTimeModification.concat(".txt"));

		logger.info("Completed files time modifications Tests");
	}

	private File createTemporaryFilesInsideSubDir(final String subDir) throws Exception {
		logger.info("Creating temporary files");
		final String subFolderPath = this.rootFolderPath.concat(subDir);
		final File subRootFolder = FileTools.createNewTempDir(subFolderPath);
		assertThat(subRootFolder.listFiles().length).isEqualTo(0);
		createTempFile(subRootFolder, SimpleDirectoryCleanerTest.filenameForRegexCheck1, 0);
		createTempFile(subRootFolder, SimpleDirectoryCleanerTest.filenameForRegexCheck2, 0);
		createTempFile(subRootFolder, SimpleDirectoryCleanerTest.filenameForRegexCheck3, 0);
		assertThat(subRootFolder.listFiles().length).isEqualTo(3);
		logger.info("Created temporary files");
		return subRootFolder;
	}

	/**
	 * Simple helper method that sets up the directory's specifications for the clean up task
	 */
	private void setDirectorysSpecs(final File baseDir, final boolean proceedSubs, final String regexInclude, final String regexExcluded,
			final long millisSinceLastFileTimeModificationArg) {
		logger.info("Changing clean directory's specifications");
		this.directorySpecs = new SimpleDirectoryCleaner.CleanDirectorySpecs();
		this.directorySpecs.setBaseDir(baseDir);
		this.directorySpecs.setProceedSubDirs(proceedSubs);
		this.directorySpecs.setMinMillisSinceFilesLastModification(millisSinceLastFileTimeModificationArg);
		final RegexCheck regexCheck = new RegexCheck();
		regexCheck.setIncludeRegex(regexInclude);
		regexCheck.setExcludeRegex(regexExcluded);
		this.directorySpecs.setRegexExpressionFilter(regexCheck);
		this.simpleDirectoryCleaner.getCleanDirectorySpecList().clear();
		this.simpleDirectoryCleaner.getCleanDirectorySpecList().add(this.directorySpecs);
		logger.info("Changed clean directory's specifications");
	}

	/**
	 * Simple helper method that creates a temporary file in a specified folder and file's last modification time
	 *
	 * @param subRootFolder
	 *            The folder where the file will be created
	 */
	private static void createTempFile(final File subRootFolder, final String filename, final long millisSinceLastFileTimeModificationArg)
			throws Exception {
		try {
			final File tempFile = new File(subRootFolder, filename + ".txt");
			tempFile.createNewFile();
			if (millisSinceLastFileTimeModificationArg != 0) {
				final long lastFileTimeModification = System.currentTimeMillis() - millisSinceLastFileTimeModificationArg;
				tempFile.setLastModified(lastFileTimeModification);
			}
		} catch (final Exception e) {
			throw new Exception("Couldn't create the file " + filename + " in the directory : " + subRootFolder.getName(), e);
		}
	}

	private static void printSimpleDirectoryCleanerDescription(SimpleDirectoryCleaner simpleDirectoryCleaner) {
		logger.info("---------------------------------------------------------------");
		logger.info("*** SimpleDirectoryCleaner specification description ***");
		List<CleanDirectorySpecs> specs = simpleDirectoryCleaner.getCleanDirectorySpecList();
		for (CleanDirectorySpecs cleanDirectorySpecs : specs) {
			logger.info(
					"Minimum time in milliseconds since the file was lastModified: " + cleanDirectorySpecs.getMinMillisSinceFilesLastModification());
			logger.info("Include regex: " + cleanDirectorySpecs.getRegexExpressionFilter().getIncludeRegex());
			logger.info("Exclude regex: " + cleanDirectorySpecs.getRegexExpressionFilter().getExcludeRegex());
			logger.info("Base directory: " + cleanDirectorySpecs.getBaseDir().getName());
			logger.info("Proceed sub dirs: " + cleanDirectorySpecs.isProceedSubDirs());
		}
		logger.info("---------------------------------------------------------------");
	}
}
