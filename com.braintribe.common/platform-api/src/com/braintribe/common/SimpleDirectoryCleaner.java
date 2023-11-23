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

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.braintribe.common.lcd.GenericTask;
import com.braintribe.logging.Logger;

/**
 * The service provided by this class is executed periodically with the purpose of cleaning some specified folders. A configuration file allows the
 * specification of the following properties
 *
 * -The time intervals between executions -The names of the folders -The regular expressions that are used for matching the contained files -The time
 * specifications which are used for comparison with the last modification time of files
 *
 *
 */
public class SimpleDirectoryCleaner implements GenericTask {

	private static final Logger logger = Logger.getLogger(SimpleDirectoryCleaner.class);

	// This list holds objects that have the configuration options for the clean up task
	// Each list item refers to a different folder
	private List<CleanDirectorySpecs> cleanDirectorySpecList = new ArrayList<>();

	public void setCleanDirectorySpecList(final List<CleanDirectorySpecs> cleanDirectorySpecList) {
		this.cleanDirectorySpecList = cleanDirectorySpecList;
	}

	public List<CleanDirectorySpecs> getCleanDirectorySpecList() {
		return this.cleanDirectorySpecList;
	}

	@Override
	public void perform() throws GenericTaskException {
		logger.info("Starting scheduled task  - Cleaning up of specified temporary files");
		for (final CleanDirectorySpecs cleanDirectorySpecs : this.cleanDirectorySpecList) {
			initCleaningDirectory(cleanDirectorySpecs);
		}

		logger.info("Completed scheduled task - Cleaning up of specified temporary files");
	}

	/**
	 * This method initialize the recursive method that cleans up of the temporary files
	 * {@link SimpleDirectoryCleaner#cleanDirectory(File, boolean, long, RegexCheck)}
	 */
	private void initCleaningDirectory(final CleanDirectorySpecs cleanDirectorySpecs) {
		final File rootFolder = cleanDirectorySpecs.getBaseDir();
		final boolean processSubs = cleanDirectorySpecs.isProceedSubDirs();
		final long minMillisSinceLastModification = cleanDirectorySpecs.getMinMillisSinceFilesLastModification();
		final RegexCheck regexCheck = cleanDirectorySpecs.getRegexExpressionFilter();
		cleanDirectory(rootFolder, processSubs, minMillisSinceLastModification, regexCheck);
	}

	private void cleanDirectory(final File folder, final boolean proceedSubs, final long minMillisSinceLastModification,
			final RegexCheck regexCheck) {
		if (!folder.exists()) {
			logger.warn("There is not such folder : " + folder.getAbsolutePath() + "\n Check your configuration file!");
			return;
		}
		final File[] listOfFiles = folder.listFiles();
		for (final File file : listOfFiles) {
			if (file.isDirectory()) {
				if (proceedSubs) {
					logger.trace("For the directory: " + file.getName());
					cleanDirectory(file, proceedSubs, minMillisSinceLastModification, regexCheck);
					checkAndRemoveEmptySub(file);
				}
			} else {
				logger.trace("For the file: " + file.getName());
				if (checkForRegexMatch(file, regexCheck) && checkForFilesLastTimeModification(file, minMillisSinceLastModification)) {
					logger.trace("File matched all criteria: " + file.getName());
					file.delete();
					logger.debug("File deleted: " + file.getName());
				}
			}
		}
	}

	private static boolean checkAndRemoveEmptySub(final File file) {
		if (file.listFiles().length == 0) {
			logger.trace("-This directory is empty: " + file.getName());
			file.delete();
			logger.trace("-Directory deleted: " + file.getName());
			return false;
		}
		return true;
	}

	private static boolean checkForRegexMatch(final File file, final RegexCheck regexCheck) {
		logger.trace("-Checking for files with regular expression");
		logger.trace("--The file's name is: " + file.getName());
		if (regexCheck == null) {
			logger.trace("---If regex is null every file should be deleted: ");
			return true;
		} else {
			logger.trace("--Comparing to the included Regex: " + regexCheck.getIncludeRegex());
			logger.trace("--Comparing to the excluded Regex: " + regexCheck.getExcludeRegex());

			final boolean matched = regexCheck.check(file.getName());
			if (matched) {
				logger.trace("--The file matches the REGEX patern");
			} else {
				logger.trace("--The file doesn't match REGEX patern");
			}
			logger.trace("-Checked for files with regular expression");
			return matched;
		}
	}

	private static boolean checkForFilesLastTimeModification(final File file, final long minMillisSinceLastModification) {

		logger.trace("-Checking file's last time modification for the file: " + file.getName());
		logger.trace("--The file's last time modification is: " + new Date(file.lastModified()) + " - " + file.lastModified());
		final long timeFlag = System.currentTimeMillis() - minMillisSinceLastModification;
		logger.trace("--Comparing to the time flag: " + new Date(timeFlag) + " - " + timeFlag);
		if (file.lastModified() < timeFlag) {
			logger.trace("--The file " + file.getName() + " should be deleted");
			logger.trace("-Checked file's last time modification");
			return true;
		} else {
			logger.trace("--The file " + file.getName() + " should not be deleted");
			logger.trace("-Checked file's last time modification for the file: " + file.getName());
			return false;
		}
	}

	/**
	 * This class holds the specifications that are used in the clean up task.
	 */
	public static class CleanDirectorySpecs {

		private File baseDir = null;

		// Maximum time in milliseconds since the file was lastModified
		private long minMillisSinceFilesLastModification;

		// Whether or not to process sub directories
		private boolean proceedSubDirs = true;

		// Optional include/exclude regular expression patterns to specify the files to delete
		private RegexCheck regexExpressionFilter = null;

		public File getBaseDir() {
			return this.baseDir;
		}

		public void setBaseDir(final File baseDir) {
			this.baseDir = baseDir;
		}

		public long getMinMillisSinceFilesLastModification() {
			return this.minMillisSinceFilesLastModification;
		}

		public void setMinMillisSinceFilesLastModification(final long minMillisSinceFilesLastModification) {
			this.minMillisSinceFilesLastModification = minMillisSinceFilesLastModification;
		}

		public boolean isProceedSubDirs() {
			return this.proceedSubDirs;
		}

		public void setProceedSubDirs(final boolean proceedSubDirs) {
			this.proceedSubDirs = proceedSubDirs;
		}

		public RegexCheck getRegexExpressionFilter() {
			return this.regexExpressionFilter;
		}

		public void setRegexExpressionFilter(final RegexCheck regexExpressionFilter) {
			this.regexExpressionFilter = regexExpressionFilter;
		}
	}
}
