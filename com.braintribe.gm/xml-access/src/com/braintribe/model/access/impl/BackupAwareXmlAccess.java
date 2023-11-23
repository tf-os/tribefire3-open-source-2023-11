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
package com.braintribe.model.access.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.access.ModelAccessException;
import com.braintribe.utils.DateTools;
import com.braintribe.utils.IOTools;

/**
 * 
 * 
 */
public class BackupAwareXmlAccess extends XmlAccess implements Comparator<File> {
	private static Logger logger = Logger.getLogger(BackupAwareXmlAccess.class);

	/*
	 * Variables (default values)
	 */

	protected final String backupPostfix = "backup", corruptPostfix = "corrupt";
	protected DateTimeFormatter dateFormatter = DateTools.TERSE_DATETIME_FORMAT;
	protected TreeSet<File> corruptFilePaths = new TreeSet<File>(this);
	protected TreeSet<File> backupFilePaths = new TreeSet<File>(this);
	protected FileParts fileParts = null;
	protected boolean autoRestore = true;
	protected int maxCorruptFiles = 10;
	protected int maxBackups = 10;

	/*
	 * Setter
	 */

	@Required
	@Override
	public void setFilePath(final File filePath) {
		super.setFilePath(filePath);
		this.fileParts = new FileParts(filePath);

		this.backupFilePaths.clear();
		this.backupFilePaths.addAll(getExistingFiles(this.backupPostfix));

		this.corruptFilePaths.clear();
		this.corruptFilePaths.addAll(getExistingFiles(this.corruptPostfix));
	}

	public void setDateFormat(final String dateFormat) {
		this.dateFormatter = DateTimeFormatter.ofPattern(dateFormat).withLocale(Locale.US);
	}

	public void setAutoRestore(final boolean autoRestore) {
		this.autoRestore = autoRestore;
	}

	public void setMaxCorruptFiles(final int maxCorruptFiles) {
		this.maxCorruptFiles = maxCorruptFiles;
	}

	public void setMaxBackups(final int maxBackups) {
		this.maxBackups = maxBackups;
	}

	/*
	 * Backup implementation
	 */

	@Override
	public synchronized Object loadModel() throws ModelAccessException {
		try {
			if (this.filePath.exists() == false && this.autoRestore == true) {
				throw new ModelAccessException("No file found at \"%s\".");
			}

			// Try to load model
			return super.loadModel();
		} catch (final ModelAccessException e) {
			// Check auto restore flag
			if (this.autoRestore == true) {
				// Define needed restore variables
				final List<File> corruptBackups = new ArrayList<File>();
				boolean successfullyRestored = false;
				Object restoredModel = null;

				// Iterate through all sorted backup files
				final Iterator<File> backupIterator = this.backupFilePaths.descendingIterator();
				while (backupIterator.hasNext() == true) {
					final File backupFilePath = backupIterator.next();

					// Check if current backup file exists
					if (backupFilePath.exists() == true) {
						// Log restore corrupt file with current backup file
						logger.warn(String.format("Restore file \"%s\" with backup file \"%s\"",
								this.filePath.toPath(), backupFilePath.toPath()));

						try {
							// Set replace CopyOption
							final CopyOption replaceExisting = java.nio.file.StandardCopyOption.REPLACE_EXISTING;
							if (this.filePath.exists() == true) {
								// Save corrupt file before restoring backup file
								final File corruptFilePath = createFilePath(this.corruptPostfix);
								Files.move(this.filePath.toPath(), corruptFilePath.toPath(), replaceExisting);

								// Add new backup to list & check backups
								this.corruptFilePaths.add(corruptFilePath);
								deleteOldFiles(this.corruptFilePaths, this.maxCorruptFiles);
							}

							// Restore corrupt file with current backup file
							Files.copy(backupFilePath.toPath(), this.filePath.toPath(), replaceExisting);
						} catch (final IOException ex) {
							// Error while restoring corrupt file
							throw new ModelAccessException("Could not replace filePath with backupFilePath", ex);
						}

						try {
							// Retry to load model
							restoredModel = super.loadModel();
							successfullyRestored = true;
							break;
						} catch (final ModelAccessException ex) {
							// Log backup file corrupt
							logger.warn(String.format(
									"Error while loading restored backup file \"%s\", continue with next backup.",
									backupFilePath.toPath()));

							// Add backup file to corrupt list
							corruptBackups.add(backupFilePath);
						}
					}
				}

				// Remove corrupt backup files from backup list
				for (int i = corruptBackups.size() - 1; i >= 0; i--) {
					final File corruptBackup = corruptBackups.get(i);

					// Remove backup file from hard disc
					if (corruptBackup.exists() == true) {
						corruptBackup.delete();
					}

					// Remove backup file from file set
					this.backupFilePaths.remove(corruptBackup);
				}

				// Check if restore was a success
				if (successfullyRestored == true) {
					// Return restored model
					return restoredModel;
				}

				// All backup files are corrupt, Re-Throw original exception
				throw new ModelAccessException("No working backup file found. Throw original exception.", e);
			}

			// Re-Throw
			throw e;
		}
	}

	@Override
	public synchronized void storeModel(final Object model) throws ModelAccessException {
		if (this.filePath.exists() == true) {
			try {
				// Create path for new backup file
				final File backupFilePath = createFilePath(this.backupPostfix);

				// Create backup file from current file
				final CopyOption replaceExisting = java.nio.file.StandardCopyOption.REPLACE_EXISTING;
				Files.copy(this.filePath.toPath(), backupFilePath.toPath(), replaceExisting);

				// Add new backup to list & check backups
				this.backupFilePaths.add(backupFilePath);
				deleteOldFiles(this.backupFilePaths, this.maxBackups);
			} catch (final IOException ex) {
				// Error while creating backup file
				throw new ModelAccessException("Could not create backup of filePath", ex);
			}
		}

		// Save model to filePath
		super.storeModel(model);
	}

	@Override
	public int compare(final File file1, final File file2) {
		// Extract backup dates from backup file path
		Date checkDate1 = getDateFromFile(file1, this.backupPostfix);
		Date checkDate2 = getDateFromFile(file2, this.backupPostfix);

		// Check if extracted dates are backup dates
		if (checkDate1 == null && checkDate2 == null) {
			// Extract move dates from corrupt file path
			checkDate1 = getDateFromFile(file1, this.corruptPostfix);
			checkDate2 = getDateFromFile(file2, this.corruptPostfix);
		}

		// Check extracted dates
		if (checkDate1 != null && checkDate2 != null) {
			// Compare extracted dates
			return checkDate1.compareTo(checkDate2);
		} else {
			// Check which date is null
			return (checkDate1 == null && checkDate2 == null ? 0 : (checkDate1 == null ? -1 : 1));
		}
	}

	/*
	 * File management
	 */

	protected File createFilePath(final String filePostfix) {
		// Check parameter
		if (filePostfix != null) {
			// Get file parts of base file path
			final File fileFolder = this.fileParts.getFolder();
			final String fileExtension = this.fileParts.getExtension();
			final String cleanFileName = this.fileParts.getCleanName();

			// Set backup date and set backup file path from given file path.
			final String currentDate = DateTools.encode(new Date(), this.dateFormatter);
			return new File(String.format("%s\\%s_%s_%s%s", fileFolder.getPath(), cleanFileName, filePostfix,
					currentDate, fileExtension));
		}

		return null;
	}

	protected TreeSet<File> getExistingFiles(final String filePostfix) {
		final TreeSet<File> existingFiles = new TreeSet<File>();

		// Check parameter
		if (filePostfix != null) {
			// Work though all sub files of folder
			for (final File subFile : this.fileParts.getFolder().listFiles()) {
				// Check name of file to find backup file
				if (subFile.isFile() == true && getDateFromFile(subFile, filePostfix) != null) {
					// Found a backup file
					existingFiles.add(subFile);
				}
			}
		}

		return existingFiles;
	}

	protected void deleteOldFiles(final TreeSet<File> fileSet, final int maxFiles) {
		// Max backups defined?
		if (fileSet != null && maxFiles > -1) {
			// Remove files if max is reached
			while (fileSet.size() > maxFiles) {
				final File oldestFile = fileSet.first();

				// Remove file from hard disc
				if (oldestFile.exists() == true) {
					oldestFile.delete();
				}

				// Remove file from file set
				fileSet.remove(oldestFile);
			}
		}
	}

	protected Date getDateFromFile(final File backupFile, final String filePostfix) {
		// Check parameters
		if (backupFile != null && filePostfix != null) {
			// Get name and extension from base file path
			final String cleanName = this.fileParts.getCleanName() + '_' + filePostfix + '_';
			final String extension = this.fileParts.getExtension();
			final String backupFileName = backupFile.getName();

			// Check name of backup file
			if (backupFileName.startsWith(cleanName) == true && backupFileName.endsWith(extension) == true) {
				// Split date from backup file
				final int dateEndIndex = backupFileName.length() - extension.length();
				final String dateString = backupFileName.substring(cleanName.length(), dateEndIndex);

				try {
					// Try to parse date of backup file
					return DateTools.decodeDateTime(dateString, this.dateFormatter);
				} catch (final Exception ex) {
					// Ignore
				}
			}
		}

		return null;
	}

	/*
	 * File management -> file parts
	 */

	protected class FileParts {
		private File folder;

		public File getFolder() {
			return this.folder;
		}

		private String name;

		public String getName() {
			return this.name;
		}

		private String extension;

		public String getExtension() {
			return this.extension;
		}

		private String cleanName;

		public String getCleanName() {
			return this.cleanName;
		}

		public FileParts(final File file) {
			if (file != null) {
				// Get and check file folder
				final File fileFolder = file.getParentFile();
				this.folder = (fileFolder != null ? fileFolder : new File("."));

				// Get and check file name
				final String fileName = file.getName();
				this.name = (fileName != null ? fileName : "");

				// Get and check file extension
				final String fileExtension = IOTools.getExtension(file);
				this.extension = (fileExtension != null ? String.format(".%s", fileExtension) : "");

				// Get file name without extension from given file path.
				final int cleanNameSize = this.name.length() - this.extension.length();
				this.cleanName = this.name.substring(0, cleanNameSize);
			}
		}
	}
}
