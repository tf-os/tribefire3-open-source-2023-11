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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.ErrorManager;
import java.util.logging.Filter;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.braintribe.logging.juli.ConfigurationException;
import com.braintribe.logging.juli.JulExtensionsHelpers;
import com.braintribe.logging.juli.handlers.util.CountingThreadFactory;
import com.braintribe.logging.juli.handlers.util.CronExpression;

/**
 * A {@link Handler} implementation that writes to a file. Compared to other file handler implementations (e.g. {@link java.util.logging.FileHandler}
 * or {@code org.apache.juli.FileHandler}) this class makes a few assumptions to keep the code simpler. First of all, it is assumed that the handler's
 * log file is not modified by other processes (i.e. there is no synchronization). Furthermore it currently does not support formatter
 * {@link Formatter#getHead(Handler) head} and {@link Formatter#getTail(Handler) tail}. On the other hand, the handler provides additional features
 * such as file rotation with timestamps for archived files or file compression.<br/>
 * Supported configuration properties are:
 * <ul>
 * <li>directory: the directory to log to</li>
 * <li>fileKey: the log file name (without extension)</li>
 * <li>fileExtension: the log file extension. Default: <code>log</code></li>
 * <li>maxFileSize: the maximum file size. If set (and greater than <code>-1</code>), a new log file is created whenever the current log file exceeds
 * the specified file size. The old file is then archived (see below). Note that the size check is performed after printing a log message. This means
 * that a single message will never be split.</li>
 * <li>maxArchivedFileCount: the maximum number of archived files. If exceeded, the oldest archived file will be deleted.</li>
 * <li>archivedFileKeyPrefix: the prefix for the file names of archived files. The full file name is [archivedFileKeyPrefix]_[timestamp]. Default:
 * (same as fileKey)</li>
 * <li>archivedFileCompressionEnabled: whether or not to compress archived files (using zip format). Default: <code>false</code></li>
 * <li>compressedFileExtension: the log file extension for compressed files. Note that changing the extension will not affect the compression format.
 * Usually one doesn't have to change the default, which is <code>zip</code>.</li>
 * <li>level: the log level (see {@link Handler#setLevel(Level)}). Default: <code>ALL</code></li>
 * <li>encoding: the encoding (see {@link Handler#setEncoding(String)}). Default: UTF-8</li>
 * <li>formatter: the formatter (see {@link Handler#setFormatter(Formatter)}).</li>
 * <li>filter: an optional filter (see {@link Handler#setFilter(Filter)}).</li>
 * <li>errorManager: an {@link ErrorManager} (see {@link Handler#setErrorManager(ErrorManager)}). Default:
 * <code>java.util.logging.ErrorManager.ErrorManager</code></li>
 * <li>cronRotate: A cron expression defining when logs should be rotated on a timely basis.</li>
 * </ul>
 *
 * @author michael.lafite
 *
 * @see FileHandler1
 */
public class FileHandler extends Handler {

	private String directory;
	private String fileKey;
	private String fileExtension;
	private String archivedFileKeyPrefix;
	private String cronRotate;
	private boolean archivedFileCompressionEnabled;
	private String compressedFileExtension;
	private Long maxFileSize;
	private Integer maxArchivedFileCount;
	private File logFile;
	private BytesCountingOutputStream bytesCountingOutputStream;
	private Writer logFileWriter;
	private ReentrantLock writerLock = new ReentrantLock();
	private CronExpression cronExpression;
	private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS").withLocale(Locale.US);

	// Making sure that only one scheduler is created; no need to have one thread for each log file
	private static ScheduledExecutorService scheduler;
	private static AtomicInteger schedulerUsageCounter = new AtomicInteger(0);
	private static ReentrantLock schedulerCreationLock = new ReentrantLock();

	/**
	 * Instantiates and {@link #configureHandler() configures} a new <code>FileHandler</code> instance and {@link #openLogFile() prepares} for
	 * logging.
	 */
	public FileHandler() {
		configureHandler();
		openLogFile();
	}

	private void configureHandler() {

		this.directory = JulExtensionsHelpers.getProperty(getClass(), "directory", true, null, String.class);
		this.fileKey = JulExtensionsHelpers.getProperty(getClass(), "fileKey", true, null, String.class);
		this.fileExtension = JulExtensionsHelpers.getProperty(getClass(), "fileExtension", true, "log", String.class);
		this.archivedFileKeyPrefix = JulExtensionsHelpers.getProperty(getClass(), "archivedFileKeyPrefix", true, this.fileKey, String.class);
		this.archivedFileCompressionEnabled = JulExtensionsHelpers.getProperty(getClass(), "archivedFileCompressionEnabled", true, false,
				Boolean.class);
		this.compressedFileExtension = JulExtensionsHelpers.getProperty(getClass(), "compressedFileExtension", true, "zip", String.class);
		this.maxFileSize = JulExtensionsHelpers.getProperty(getClass(), "maxFileSize", false, null, Long.class);
		if (this.maxFileSize < 0) {
			this.maxFileSize = null;
		}
		this.maxArchivedFileCount = JulExtensionsHelpers.getProperty(getClass(), "maxArchivedFileCount", false, -1, Integer.class);
		if (this.maxArchivedFileCount < 0) {
			this.maxArchivedFileCount = null;
		}

		final String fileDateFormat = JulExtensionsHelpers.getProperty(getClass(), "fileDateFormat", false, null, String.class);
		if (fileDateFormat != null && fileDateFormat.trim().length() > 0) {
			dateFormatter = DateTimeFormatter.ofPattern(fileDateFormat).withLocale(Locale.US);
		}

		final String encoding = JulExtensionsHelpers.getProperty(getClass(), "encoding", true, "UTF-8", String.class);
		try {
			setEncoding(encoding);
		} catch (final UnsupportedEncodingException e) {
			throw new ConfigurationException("Error while setting encoding. The specified encoding '" + encoding + "' is unsupported!", e);
		}

		final String levelString = JulExtensionsHelpers.getProperty(getClass(), "level", true, Level.ALL.getName(), String.class);
		setLevel(Level.parse(levelString));

		final String formatterName = JulExtensionsHelpers.getProperty(getClass(), "formatter", true, null, String.class);
		setFormatter(JulExtensionsHelpers.createInstance(formatterName, Formatter.class, null,
				"Error while instantiating formatter " + formatterName + "!"));

		final String filterName = JulExtensionsHelpers.getProperty(getClass(), "filter", false, null, String.class);
		setFilter(JulExtensionsHelpers.createInstance(filterName, Filter.class, null, "Error while instantiating filter " + filterName + "!"));

		final String errorManagerName = JulExtensionsHelpers.getProperty(getClass(), "errorManager", false, null, String.class);
		setErrorManager(JulExtensionsHelpers.createInstance(errorManagerName, ErrorManager.class, new ErrorManager(),
				"Error while instantiating errorManager " + errorManagerName + "!"));

		this.cronRotate = JulExtensionsHelpers.getProperty(getClass(), "cronRotate", false, null, String.class);
		if (cronRotate != null && cronRotate.trim().length() > 0) {
			try {
				cronExpression = CronExpression.create(cronRotate);
				createScheduler();
				scheduleNextCronLogRotate();
			} catch (Exception e) {
				throw new ConfigurationException("Error while parsing the cron expression '" + cronRotate + "'", e);
			}
		}

	}

	private static void createScheduler() {
		schedulerUsageCounter.incrementAndGet();
		if (scheduler == null) {
			schedulerCreationLock.lock();
			try {
				if (scheduler == null) {
					scheduler = Executors.newScheduledThreadPool(1, new CountingThreadFactory("logrotation", true));
				}
			} finally {
				schedulerCreationLock.unlock();
			}
		}
	}
	private static void stopScheduler() {
		if (schedulerUsageCounter.decrementAndGet() <= 0) {
			schedulerCreationLock.lock();
			try {
				if (scheduler != null) {
					scheduler.shutdownNow();
				}
				scheduler = null;
			} finally {
				schedulerCreationLock.unlock();
			}
		}
	}

	// LogRotate based on cron expression
	protected void scheduleNextCronLogRotate() {
		if (cronExpression == null || scheduler == null) {
			return;
		}
		try {
			ZonedDateTime nextLogRotate = cronExpression.nextTimeAfter(ZonedDateTime.now());
			long nextLogRotateEpoch = nextLogRotate.toInstant().getEpochSecond();
			long nowEpoch = (new Date()).toInstant().getEpochSecond();
			long delay = nextLogRotateEpoch - nowEpoch;
			if (delay > 0) {
				scheduler.schedule(this::cronLogRotate, delay, TimeUnit.SECONDS);
			} else {
				System.err.println(
						"The cron expression " + cronRotate + " does not specify a point in time in the near future (< 4 years). Ignoring it.");
			}
		} catch (Exception e) {
			reportError("Error while trying to schedule the next cron-based log rotation.", e, ErrorManager.GENERIC_FAILURE);
		}
	}
	protected void cronLogRotate() {
		if (this.bytesCountingOutputStream.getBytesWritten() > 0) {
			doLogRotate();
		}
		scheduleNextCronLogRotate();
	}

	protected void doLogRotate() {
		File archivedLogFile = null;
		writerLock.lock();
		try {
			archivedLogFile = archiveLogFile();
			openLogFile();
		} finally {
			writerLock.unlock();
		}

		// Start the clean up in a separate thread so it won't stop processing here
		FileRenameAndZipAndDeleteOld renamed = new FileRenameAndZipAndDeleteOld(archivedLogFile);
		Thread t = new Thread(renamed);
		t.setDaemon(true);
		t.setName("Logfile-Removal-Daemon");
		t.start();
	}

	private void openLogFile() {

		if (this.logFileWriter != null) {
			closeFile();
		}

		this.logFile = new File(getLogFilePath(this.directory, this.fileKey, this.fileExtension));

		if (this.logFile.exists()) {
			if (this.logFile.isDirectory()) {
				reportError("Log file " + this.logFile.getAbsolutePath() + " is a directory!", null, ErrorManager.OPEN_FAILURE);
				return;
			}
		} else {
			final File logDir = new File(this.directory);
			if (!logDir.exists()) {
				if (!logDir.mkdirs()) {
					reportError("Couldn't create directory " + logDir.getAbsolutePath() + "!", null, ErrorManager.OPEN_FAILURE);
					return;
				}
			} else {
				if (!logDir.isDirectory()) {
					reportError("Directory path " + logDir.getAbsolutePath() + " is not a directory!", null, ErrorManager.OPEN_FAILURE);
					return;
				}
			}
		}

		FileOutputStream fileOutputStream;
		try {
			fileOutputStream = new FileOutputStream(this.logFile, true);
		} catch (final FileNotFoundException e) {
			reportError("Log file " + this.logFile + " doesn't exist and/or cannot be created!", e, ErrorManager.OPEN_FAILURE);
			return;
		}
		this.bytesCountingOutputStream = new BytesCountingOutputStream(fileOutputStream, this.logFile.length());

		OutputStreamWriter outputStreamWriter;
		try {
			outputStreamWriter = new OutputStreamWriter(this.bytesCountingOutputStream, getEncoding());
		} catch (final UnsupportedEncodingException e) {
			reportError("Encoding '" + getEncoding() + "' not supported!", e, ErrorManager.OPEN_FAILURE);
			this.logFileWriter = null;
			return;
		}

		this.logFileWriter = new BufferedWriter(outputStreamWriter);
	}

	protected File archiveLogFile() {
		final File logFileToArchive = this.logFile;

		closeFile();

		File archivedLogFile = null;
		boolean fileExists = false;
		int filesNo = 0;
		while (true) {

			LocalDateTime now = LocalDateTime.now();
			String nowString = dateFormatter.format(now);

			String archivedFileKeyPrefixWithTimestamp = null;
			if (fileExists) {
				archivedFileKeyPrefixWithTimestamp = this.archivedFileKeyPrefix.concat("_").concat(nowString).concat(".") + filesNo;
			} else {
				archivedFileKeyPrefixWithTimestamp = this.archivedFileKeyPrefix.concat("_").concat(nowString);
			}

			archivedLogFile = new File(getLogFilePath(this.directory, archivedFileKeyPrefixWithTimestamp, this.fileExtension));

			if (archivedLogFile.exists()) {
				fileExists = true;
				// if it really ever happens that the file already exists, we wait a bit and try again
				try {
					Thread.sleep(2);
				} catch (final InterruptedException e) {
					// ignore
				}
				filesNo++;
			} else {
				break;
			}
		}

		logFileToArchive.renameTo(archivedLogFile);
		return archivedLogFile;
	}

	protected void deleteOldArchivedFilesIfRequired() {
		if (this.maxArchivedFileCount == null) {
			return;
		}
		final File logFileDir = new File(this.directory);
		final List<File> archivedLogFiles = Arrays.asList(logFileDir.listFiles(new FilenameFilter() {
			String prefix = FileHandler.this.archivedFileKeyPrefix + "_";
			String suffix = "."
					+ (FileHandler.this.archivedFileCompressionEnabled ? FileHandler.this.compressedFileExtension : FileHandler.this.fileExtension);

			@Override
			public boolean accept(final File dir, final String name) {
				return name.startsWith(this.prefix) && name.endsWith(this.suffix);
			}
		}));

		final int numberOfFilesToDelete = archivedLogFiles.size() - this.maxArchivedFileCount;

		if (numberOfFilesToDelete > 0) {

			Collections.sort(archivedLogFiles, (File f1, File f2) -> f1.getName().compareTo(f2.getName()));

			for (int i = 0; i < numberOfFilesToDelete; i++) {
				final File archivedLogFileToDelete = archivedLogFiles.get(i);
				if (!archivedLogFileToDelete.delete()) {
					reportError("Coudln't delete archived log file " + archivedLogFileToDelete.getAbsolutePath() + "!", null,
							ErrorManager.GENERIC_FAILURE);

				}
			}
		}
	}

	protected static String getLogFilePath(final String directory, final String key, final String extension) {
		return new File(directory, key + "." + extension).getAbsolutePath();
	}

	@Override
	public void publish(final LogRecord logRecord) {
		if (!isLoggable(logRecord)) {
			return;
		}

		String formattedMessage = null;
		try {
			formattedMessage = getFormatter().format(logRecord);
		} catch (final Exception e) {
			reportError("Error while formatting message '" + logRecord.getMessage() + "'!", e, ErrorManager.FORMAT_FAILURE);
			return;
		}

		writerLock.lock();
		try {
			if (this.logFileWriter == null) {
				reportError("Log writer is not initialized yet or already closed! Can't log message '" + formattedMessage + "'.", null,
						ErrorManager.WRITE_FAILURE);
			} else {

				try {
					this.logFileWriter.write(formattedMessage);
					this.logFileWriter.flush();
				} catch (final IOException e) {
					reportError("Error while writing log message '" + formattedMessage + "'!", e, ErrorManager.WRITE_FAILURE);
				}

				if (this.maxFileSize != null && this.bytesCountingOutputStream.getBytesWritten() >= this.maxFileSize) {
					doLogRotate();
				}

			}
		} finally {
			writerLock.unlock();
		}
	}

	@Override
	public void flush() {
		// since everything is written immediately, there is no need to flush
	}

	protected void closeFile() {
		if (this.logFileWriter != null) {
			writerLock.lock();
			try {
				if (this.logFileWriter != null) {
					this.logFileWriter.close();
				}
			} catch (final IOException e) {
				reportError("Error while closing writer!", e, ErrorManager.CLOSE_FAILURE);
			} finally {
				this.logFileWriter = null;
				this.logFile = null;
				this.bytesCountingOutputStream = null;
				writerLock.unlock();
			}
		}
	}

	@Override
	public void close() {
		closeFile();
		stopScheduler();
	}

	private class FileRenameAndZipAndDeleteOld implements Runnable {
		private File archivedLogFile;

		public FileRenameAndZipAndDeleteOld(File archivedLogFile) {
			this.archivedLogFile = archivedLogFile;
		}

		@Override
		public void run() {
			deleteOldArchivedFilesIfRequired();
			if (archivedFileCompressionEnabled && archivedLogFile != null && archivedLogFile.exists()) {

				boolean success = false;
				File compressedFile = new File(archivedLogFile.getParentFile(), archivedLogFile.getName() + "." + compressedFileExtension);
				try (ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(compressedFile)));
						InputStream in = new BufferedInputStream(new FileInputStream(archivedLogFile))) {
					ZipEntry e = new ZipEntry(archivedLogFile.getName());
					out.putNextEntry(e);
					byte[] buffer = new byte[8192];
					int count = 0;
					while ((count = in.read(buffer)) != -1) {
						out.write(buffer, 0, count);
					}
					out.closeEntry();
					success = true;
				} catch (Exception e) {
					reportError("Could not compress file " + archivedLogFile.getAbsolutePath(), e, ErrorManager.WRITE_FAILURE);
					success = false;
				}

				if (success) {
					if (!archivedLogFile.delete()) {
						reportError("Coudln't delete archived log file " + archivedLogFile.getAbsolutePath() + "!", null,
								ErrorManager.GENERIC_FAILURE);
					}
				}
			}
		}

	}

	/**
	 * An {@link OutputStream} that passes all requests to a delegate stream and counts the bytes written.
	 *
	 * @author michael.lafite
	 */
	private class BytesCountingOutputStream extends OutputStream {
		private final OutputStream delegate;
		private long bytesWritten;

		private BytesCountingOutputStream(final OutputStream delegate, final long initialBytesWritten) {
			this.delegate = delegate;
			this.bytesWritten = initialBytesWritten;
		}

		public long getBytesWritten() {
			return this.bytesWritten;
		}

		@Override
		public void flush() throws IOException {
			this.delegate.flush();
		}

		@Override
		public void close() throws IOException {
			this.delegate.close();
		}

		@Override
		public void write(final int byteToWrite) throws IOException {
			this.delegate.write(byteToWrite);
			this.bytesWritten++;
		}

		@Override
		public void write(final byte[] bufferToWrite) throws IOException {
			this.delegate.write(bufferToWrite);
			this.bytesWritten += bufferToWrite.length;
		}

		@Override
		public void write(final byte[] bufferToWrite, final int offset, final int length) throws IOException {
			this.delegate.write(bufferToWrite, offset, length);
			this.bytesWritten += length;
		}

	}

	/**
	 * Returns the filekey of this FileHandler. This is needed by the JdkLogger to find the right file handler.
	 *
	 * @return The file key of this handler.
	 */
	public String getFileKey() {
		return fileKey;
	}

}
