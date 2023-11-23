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

import static com.braintribe.utils.lcd.Arguments.notNull;
import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.nullSafe;
import static java.util.Objects.requireNonNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.braintribe.common.lcd.Constants;
import com.braintribe.common.uncheckedcounterpartexceptions.FileAlreadyExistsException;
import com.braintribe.common.uncheckedcounterpartexceptions.UncheckedMalformedURLException;
import com.braintribe.exception.Exceptions;
import com.braintribe.logging.Logger;
import com.braintribe.utils.date.ExtSimpleDateFormat;
import com.braintribe.utils.file.compare.FileNameBasedFolderComparator;
import com.braintribe.utils.file.compare.FolderComparison;
import com.braintribe.utils.file.copy.FileCopyTargetSelector;
import com.braintribe.utils.file.copy.FileCopyingImpl;
import com.braintribe.utils.io.BasicFileReaderBuilder;
import com.braintribe.utils.io.BasicFileWriterBuilder;
import com.braintribe.utils.io.FileReaderBuilder;
import com.braintribe.utils.io.FileWriterBuilder;
import com.braintribe.utils.io.ReaderBuilder;
import com.braintribe.utils.io.WriterBuilder;
import com.braintribe.utils.io.ZipEntryWriter;
import com.braintribe.utils.lcd.Arguments;
import com.braintribe.utils.lcd.Not;
import com.braintribe.utils.paths.FilePath;
import com.braintribe.utils.stream.ReferencingFileInputStream;

/**
 * This class may contain Java-only (i.e. GWT incompatible) code. For further information please see {@link com.braintribe.utils.lcd.FileTools}.
 *
 * @author michael.lafite
 */
public class FileTools extends com.braintribe.utils.lcd.FileTools {

	private static Logger logger = Logger.getLogger(FileTools.class);

	public static final boolean isPosix = FileSystems.getDefault().supportedFileAttributeViews().contains("posix");

	protected FileTools() {
		// nothing to do
	}

	/** Entrance to a fluent API for reading from a file with various options. See examples in {@link ReaderBuilder}. */
	public static FileReaderBuilder read(File file) {
		return new BasicFileReaderBuilder(file);
	}

	/** Equivalent to {@link #read(File) FileTools}.read(path.toFile()). */
	public static FileReaderBuilder read(Path path) {
		return read(path.toFile());
	}

	/** Entrance to a fluent API for writing to a file with various options. See examples in {@link WriterBuilder} */
	public static FileWriterBuilder write(File file) {
		return new BasicFileWriterBuilder(file);
	}

	/** Equivalent to {@link #write(File) FileTools.write}(path.toFile()). */
	public static FileWriterBuilder write(Path path) {
		return write(path.toFile());
	}

	/** @see ZipTools#writeZip(File, Consumer) */
	public static void writeZip(File zipFile, Consumer<ZipEntryWriter> zipContentWriter) {
		ZipTools.writeZip(zipFile, zipContentWriter);
	}

	/** Entrance to a fluent API for copying a file. See {@link FileCopyTargetSelector}. */
	public static FileCopyTargetSelector copy(File file) {
		return new FileCopyingImpl(file);
	}

	/** Equivalent to {@link #copy(File) FileTools.copy}(path.toFile()). */
	public static FileCopyTargetSelector copy(Path path) {
		return copy(path.toFile());
	}

	public static FilePath filePath(File file) {
		return FilePath.of(file);
	}

	public static FilePath filePath(String pathname) {
		return FilePath.of(pathname);
	}

	public static File newCanonicalFile(String pathname) {
		return getCanonicalFileUnchecked(new File(pathname));
	}

	public static File getCanonicalFileUnchecked(File file) {
		try {
			return file.getCanonicalFile();
		} catch (IOException e) {
			throw new UncheckedIOException("Error whie resolving canonical file for: " + file.getPath(), e);
		}
	}

	public static String getNiceAbsPath(File file) {
		try {
			return file.getCanonicalPath();
		} catch (IOException e) {
			return file.getAbsolutePath();
		}
	}

	/**
	 * Gets the MD5 sum of the passed <code>file</code>.
	 *
	 * @see IOTools#getMD5CheckSum(InputStream)
	 */
	public static byte[] getMD5CheckSum(final File file) throws IOException {
		Arguments.notNull(file, "No file specified!");
		if (!file.exists()) {
			throw new IllegalArgumentException("The specified file doesn't exist! " + CommonTools.getParametersString(file.getAbsolutePath()));
		}

		final FileInputStream inputStream = new FileInputStream(file);
		return IOTools.getMD5CheckSum(inputStream);
	}

	/**
	 * Compares the contents of the specified files based on {@link #getMD5CheckSum(File)}.
	 *
	 * @param file1
	 *            the first file to compare.
	 * @param file2
	 *            the second file to compare.
	 * @return <code>true</code>, if the file contents are equal, otherwise <code>false</code>.
	 * @throws UncheckedIOException
	 *             if any error occurs, e.g. files don't exist, files are directories, files cannot be accessed, etc.
	 */
	public static boolean isFileContentEqual(final File file1, final File file2) throws UncheckedIOException {
		Arguments.notNullWithNames("file1", file1, "file2", file2);

		if (!file1.exists() || !file2.exists()) {
			throw new RuntimeException("Cannot compare files, because at least one file doesn't exist!");
		}

		if (!file1.isFile() || !file2.isFile()) {
			throw new RuntimeException("Cannot compare files, because at least one file isn't a file!");
		}

		if (file1.length() != file2.length()) {
			return false;
		}

		try {
			final byte[] checkSum1 = getMD5CheckSum(file1);
			final byte[] checkSum2 = getMD5CheckSum(file2);
			return Arrays.equals(checkSum1, checkSum2);
		} catch (final IOException e) {
			throw new UncheckedIOException("Error while comparing files '" + file1 + "' and '" + file2 + "'!", e);
		}
	}

	/**
	 * See {@link CommonTools#replaceFileSeparators(String)}.
	 */
	public static String replaceFileSeparators(final String path) {
		return CommonTools.replaceFileSeparators(path);
	}

	/**
	 * See {@link CommonTools#replaceFileSeparators(String, String)}.
	 */
	public static String replaceFileSeparators(final String path, final String newFileSeparator) {
		return CommonTools.replaceFileSeparators(path, newFileSeparator);
	}

	/**
	 * See {@link CommonTools#getCanonicalPath(String)}.
	 */
	public static String getCanonicalPath(final String path) {
		return CommonTools.getCanonicalPath(path);
	}

	/**
	 * See {@link CommonTools#getCanonicalPath(File)}.
	 */
	public static String getCanonicalPath(final File file) {
		return CommonTools.getCanonicalPath(file);
	}

	/**
	 * See {@link CommonTools#normalizePath(String)}.
	 */
	public static String normalizePath(final String path) {
		return CommonTools.normalizePath(path);
	}

	/**
	 * Shortens and normalizes a {@code path} to given {@code length} by omitting part of the string and filling it with dots.
	 *
	 * @param path
	 *            The path to be shortened.
	 * @param length
	 *            The target path length to be shortened to. Starting 0.
	 *
	 * @return The shortened and normalized path OR the normalized path if {@code path.length()} <= {@code length}
	 *
	 */
	public static String shortenPath(final String path, int length) {
		String levelDenominator = "/";
		String string = replaceFileSeparators(path, levelDenominator);

		if (length <= 3) {
			return StringTools.getFilledString(length, '.');
		}

		int lenghtDiff = string.length() - length;

		// Return if string length is less or equal to targeted length.
		if (lenghtDiff <= 0) {
			return path;
		}

		int ceiledHalfOfStringLength = (int) Math.ceil((double) string.length() / 2);
		int targetLengthFirstCut = ceiledHalfOfStringLength - (int) Math.ceil((double) lenghtDiff / 2);
		int targetLengthSecondCut = string.length() - (string.length() - ceiledHalfOfStringLength)
				- ((lenghtDiff) - (int) Math.ceil((double) lenghtDiff / 2));

		String cutOne = string.substring(0, ceiledHalfOfStringLength).substring(0, targetLengthFirstCut);
		String cutTwo = StringTools.getLastNCharacters(string.substring(ceiledHalfOfStringLength), targetLengthSecondCut);

		String firstPart;
		String secondPart;
		if (cutOne.contains(levelDenominator)) {
			firstPart = cutOne.substring(0, cutOne.lastIndexOf(levelDenominator) + 1);
		} else {
			firstPart = cutOne.substring(0, cutOne.length() - 1);
		}
		if (cutTwo.contains(levelDenominator)) {
			secondPart = cutTwo.substring(cutTwo.indexOf(levelDenominator));
		} else {
			secondPart = cutTwo.substring(2);
		}

		int fillerLength = length - (firstPart.length() + secondPart.length());

		if (fillerLength > 5) {
			int fillFirstPart = (int) Math.ceil(((double) fillerLength - 3) / 2);
			firstPart = string.substring(0, firstPart.length() + fillFirstPart);
			int fillSecondPart = (fillerLength - 3) - (int) Math.ceil(((double) fillerLength - 3) / 2);
			secondPart = string.substring(string.length() - (secondPart.length() + fillSecondPart));
			fillerLength = 3;
		} else if (fillerLength < 3) {
			firstPart = firstPart.substring(0, firstPart.length() - (3 - fillerLength));
			fillerLength = 3;
		}

		String shortenedPath = path.substring(0, firstPart.length()) + StringTools.getFilledString(fillerLength, '.')
				+ path.substring(string.length() - secondPart.length());

		return shortenedPath;
	}

	/**
	 * Invokes {@link FileTools#writeStringToFile(File, String, String)} with <code>UTF-8</code> encoding.
	 */

	public static File writeStringToFile(final File file, final String string) throws UncheckedIOException {
		return writeStringToFile(file, string, Constants.ENCODING_UTF8);
	}

	/**
	 * Writes the passed <code>string</code> to the <code>file</code> using the specified <code>encoding</code>. The <code>file</code> is returned for
	 * convenience.
	 *
	 * @throws UncheckedIOException
	 *             if any error occurs
	 */
	public static File writeStringToFile(final File file, final String string, final String encoding) throws UncheckedIOException {
		Writer writer = null;
		IOException exceptionWhileWriting = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), encoding));
			writer.write(string);
		} catch (final IOException e) {
			exceptionWhileWriting = e;
			throw new UncheckedIOException("Error while writing to file " + file + "!", e);
		} finally {
			try {
				if (writer != null) {
					writer.close();
				}
			} catch (final IOException exceptionWileClosingStream) {
				if (exceptionWhileWriting == null) {
					throw new RuntimeException("Error while closing stream after successfully writing to " + file + "!", exceptionWileClosingStream);
				} else {
					// ignore this exception
				}
			}
		}
		return file;
	}

	/**
	 * Invokes {@link FileTools#readStringFromFile(File, String)} with <code>UTF-8</code> encoding.
	 */
	public static String readStringFromFile(final File file) throws UncheckedIOException {
		return readStringFromFile(file, Constants.ENCODING_UTF8);
	}

	/**
	 * Reads the specified <code>file</code> and returns its content as a <code>String</code>.
	 *
	 * @throws UncheckedIOException
	 *             if any error occurs
	 */
	public static String readStringFromFile(final File file, final String encoding) throws UncheckedIOException {

		BufferedReader reader = null;
		IOException exceptionWhileReading = null;

		try {
			final InputStream inputStream = new FileInputStream(file);
			final InputStreamReader inputStreamReader = new InputStreamReader(inputStream, encoding);
			reader = new BufferedReader(inputStreamReader);
			final int bufferSize = IOTools.SIZE_8K;
			final StringBuilder stringBuilder = new StringBuilder(bufferSize);
			final char[] buffer = new char[bufferSize];

			while (true) {
				final int charactersRead = reader.read(buffer);
				if (charactersRead < 0) {
					break;
				}
				stringBuilder.append(String.valueOf(buffer, 0, charactersRead));
			}
			return stringBuilder.toString();
		} catch (final IOException e) {
			exceptionWhileReading = e;
			throw new UncheckedIOException("Error while reading from file " + file + "!", e);
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (final IOException exceptionWileClosingStream) {
				if (exceptionWhileReading == null) {
					throw new UncheckedIOException("Error while closing stream after successfully reading from " + file + "!",
							exceptionWileClosingStream);
				} else {
					exceptionWhileReading.addSuppressed(exceptionWileClosingStream);
				}
			}
		}

	}

	/**
	 * Invokes {@link FileTools#readFirstLineFromFile(File, String)} with <code>UTF-8</code> encoding.
	 */
	public static String readFirstLineFromFile(final File file) throws UncheckedIOException {
		return readFirstLineFromFile(file, Constants.ENCODING_UTF8);
	}

	/**
	 * Reads the first line from a (text) file and returns it as a <code>String</code>.
	 *
	 * @throws UncheckedIOException
	 *             if any error occurs
	 */
	public static String readFirstLineFromFile(final File file, final String encoding) throws UncheckedIOException {
		BufferedReader reader = null;
		IOException exceptionWhileReading = null;

		try {
			final InputStream inputStream = new FileInputStream(file);
			final InputStreamReader inputStreamReader = new InputStreamReader(inputStream, encoding);
			reader = new BufferedReader(inputStreamReader);

			String firstLine = reader.readLine();
			return firstLine;
		} catch (final IOException e) {
			exceptionWhileReading = e;
			throw new UncheckedIOException("Error while reading from file " + file + "!", e);
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (final IOException exceptionWileClosingStream) {
				if (exceptionWhileReading == null) {
					throw new UncheckedIOException("Error while closing stream after successfully reading from " + file + "!",
							exceptionWileClosingStream);
				} else {
					// ignore this exception
				}
			}
		}
	}

	public static List<String> readLines(final File file, final String encoding) throws UncheckedIOException {
		List<String> result = newList();

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding))) {

			String line;
			while ((line = reader.readLine()) != null) {
				result.add(line);
			}

		} catch (final IOException e) {
			throw new UncheckedIOException("Error while reading from file " + file + "!", e);
		}

		return result;
	}

	/** Equivalent to {@code writeBytesToFile(file, bytes, false)} (i.e. not appending, but overwriting the file) */
	public static File writeBytesToFile(final File file, final byte[] bytes) throws UncheckedIOException {
		return writeBytesToFile(file, bytes, false);
	}

	/**
	 * Writes given {@code byte[]} to given file. If the file exists but is a directory rather than a regular file, does not exist but cannot be
	 * created, or cannot be opened for any other reason then a <code>FileNotFoundException</code> is thrown.
	 */
	public static File writeBytesToFile(final File file, final byte[] bytes, boolean append) throws UncheckedIOException {
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(file, append);

		} catch (final FileNotFoundException e) {
			throw new UncheckedIOException("Unable to open file.", e);
		}

		final DataOutputStream dos = new DataOutputStream(fos);
		boolean writingOK = false;

		try {
			dos.write(bytes);
			writingOK = true;

		} catch (final IOException e) {
			throw new RuntimeException("", e);

		} finally {
			try {
				dos.close();

			} catch (final IOException e) {
				if (writingOK) {
					throw new RuntimeException("Error while closing stream after successfully writing to " + file + "!", e);
				}
			}
		}

		return file;
	}

	public static byte[] readBytesFromFile(final File file) {
		FileInputStream fis = null;
		try {
			try {
				fis = new FileInputStream(file);

			} catch (final FileNotFoundException e) {
				throw new UncheckedIOException("Unable to open file.", e);
			}

			// prepare array for result
			final long fileSize = file.length();
			if (fileSize > Integer.MAX_VALUE) {
				throw new IllegalArgumentException("File too big.");
			}
			final byte[] result = new byte[(int) fileSize];

			final DataInputStream dis = new DataInputStream(fis);
			boolean readingOK = false;

			try {
				dis.readFully(result);
				readingOK = true;

			} catch (final IOException e) {
				throw new RuntimeException("", e);

			} finally {
				try {
					dis.close();

				} catch (final IOException e) {
					if (readingOK) {
						throw new RuntimeException("Error while closing stream after successfully reading from " + file + "!", e);
					}
				}
			}

			return result;
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (Exception e) {
					logger.debug("Could not close input stream from " + file, e);
				}
			}
		}
	}

	public static FileInputStream newFileInputStreamSafe(File file) {
		try {
			return new ReferencingFileInputStream(file);
		} catch (FileNotFoundException e) {
			throw Exceptions.unchecked(e);
		}
	}

	public static FileOutputStream newFileOutputStreamSafe(File file) {
		try {
			return new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			throw Exceptions.unchecked(e);
		}
	}

	/**
	 * Creates a new directory specified by the passed <code>path</code>.
	 *
	 * @throws IllegalStateException
	 *             if the directory does not exist AND cannot be created.
	 */
	public static File createDirectory(final String path) {
		final File dir = new File(path);
		if (!dir.exists()) {
			final boolean success = dir.mkdirs();
			if (!dir.exists()) {
				throw new IllegalStateException(
						"Couldn't create dir \"" + dir + "\"! (Reason unknown; success:" + success + ", exists: " + dir.exists() + ")");
			}
		}
		return dir;
	}

	/** Calls {@link #ensureFolderExists(File)} on this file's parent iff the parent is not null. Otherwise NOOP. */
	public static void ensureParentFolder(File file) {
		File parent = file.getParentFile();
		if (parent != null) {
			ensureFolderExists(parent);
		}
	}

	/** Same as {@link #ensureFolderExists} but throws a checked exception - for those who prefer. */
	public static File ensureDirectoryExists(final File folder) throws UncheckedIOException, IOException {
		return ensureFolder(folder, IOException::new);
	}

	public static File ensureFolderExists(final File folder) throws UncheckedIOException {
		return ensureFolder(folder, RuntimeException::new);
	}

	private static <E extends Exception> File ensureFolder(final File folder, Function<String, E> exceptionFactory) throws E {
		if (folder.exists()) {
			if (!folder.isDirectory()) {
				throw exceptionFactory.apply(folder.getAbsolutePath() + " already exists and is not a directory!");
			} else {
				return folder;
			}
		}

		if (!folder.mkdirs()) {
			throw exceptionFactory.apply("Failed to create directory " + folder.getAbsolutePath());
		}

		return folder;
	}

	/** Gets the {@link URL} for the passed <code>file</code>. */
	public static URL toURL(File file) {
		try {
			return file.toURI().toURL();

		} catch (final MalformedURLException e) {
			throw new UncheckedMalformedURLException("Error while getting URL for file: " + file.getAbsolutePath(), e);
		}
	}

	public static File createNewTempDir(String first, String... path) {
		File file = new File(first);
		if (path != null)
			for (String pathItem : path)
				file = new File(file, pathItem);

		return createNewTempDir(file.getPath());
	}

	/**
	 * Creates a directory inside the temporary files directory (as specified by system property 'java.io.tmpdir').
	 *
	 * @throws FileAlreadyExistsException
	 *             if the directory already exists.
	 */
	public static File createNewTempDir(final String name) {
		final File newDir = newTempFile(name);
		if (newDir.exists())
			throw new FileAlreadyExistsException(newDir);

		return createDirectory(newDir.getAbsolutePath());
	}

	/**
	 * Creates a {@link File} instance with the specified <code>fileName</code> in the {@link #getTempDir() temp directory} and returns it. The file
	 * will be {@link File#deleteOnExit() deleted on exit}.
	 *
	 * @throws UncheckedIOException
	 *             if the file cannot be created.
	 * @throws FileAlreadyExistsException
	 *             if the file already exists.
	 */
	public static File createNewTempFile(String fileName) throws UncheckedIOException, FileAlreadyExistsException {
		File file = newTempFile(fileName);
		if (file.exists())
			throw new FileAlreadyExistsException(file);

		file.deleteOnExit();
		return file;
	}

	/**
	 * {@link #createNewTempFile(String) Creates a new temp file} using a timestamp for the file name. The method retries with a new file name, if the
	 * file already exists.
	 *
	 * @param fileNamePrefix
	 *            the file name prefix. The full file name will also include a timestamp.
	 * @param fileExtension
	 *            the (optional) file extension, e.g. "pdf".
	 */
	public static File createNewTempFile(final String fileNamePrefix, final String fileExtension) {
		while (true) {
			final String timestamp = new ExtSimpleDateFormat("yyyyMMdd-HHmmss-SSS").format(new Date());
			try {
				String fileName = fileNamePrefix + timestamp;
				if (!CommonTools.isEmpty(fileExtension)) {
					fileName += "." + fileExtension;
				}
				return createNewTempFile(fileName);
			} catch (final FileAlreadyExistsException e) {
				// ignore;
			}
		}
	}

	/**
	 * Create a temporary file and tries to set the access flags so that only the owner can read/write it. The executable flag will also be cleared.
	 * If this is not possible, a normal temp file will be returned instead. Note that the returned file will be created and might need to be deleted
	 * before usage.
	 *
	 * @param filenamePrefix
	 *            The prefix of the temp file.
	 * @param fileExtension
	 *            The extension of the temp file, including the preceding "." if necessary
	 * @return A temporary file according to the name specifications.
	 * @throws RuntimeException
	 *             Thrown when no temp file could be generated.
	 */
	public static File createTempFileSecure(final String filenamePrefix, final String fileExtension) {

		File tempFile = null;
		if (isPosix) {
			try {
				FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------"));
				tempFile = Files.createTempFile(filenamePrefix, fileExtension, attr).toFile();
				return tempFile;
			} catch (IOException e) {
				logger.debug(() -> "Could not create a temporary file with posix permissions.", e);
			}
		}

		try {
			tempFile = Files.createTempFile(filenamePrefix, fileExtension).toFile();
			tempFile.setReadable(true, true);
			tempFile.setWritable(true, true);
			tempFile.setExecutable(false, false);
			return tempFile;
		} catch (IOException e) {
			logger.debug(() -> "Could not create a temporary file with restricted permissions", e);
		}
		try {
			tempFile = File.createTempFile(filenamePrefix, fileExtension);
			return tempFile;
		} catch (IOException e) {
			throw Exceptions.unchecked(e, "Could not create a temporary file with name " + filenamePrefix + " and extension " + fileExtension);
		}
	}

	public static File ensureTempDir(String relativePath) {
		File dir = new File(getTempDir(), relativePath);
		ensureFolderExists(dir);
		return dir;
	}

	/**
	 * Returns a {@link File} instance denoting a file inside the system temp folder based on given relative path.
	 * <p>
	 * No physical file or directory is created in the file system.
	 */
	public static File newTempFile(String relativePath) {
		return new File(getTempDir(), relativePath);
	}

	/** Returns the temporary files directory (as specified by system property 'java.io.tmpdir'). */
	public static File getTempDir() {
		return createDirectory(Not.Null(new File(System.getProperty("java.io.tmpdir")).getAbsolutePath()));
	}

	/**
	 * checks if the directory (so far it is one) is empty which mean it has no file or directory contents
	 */
	public static boolean isEmpty(final File directory) {
		if (directory == null) {
			return true;
		}

		if (!directory.exists()) {
			return true;
		}

		if (!directory.isDirectory()) {
			return false;
		}

		final File[] subFiles = directory.listFiles();
		if ((subFiles != null) && (subFiles.length > 0)) {
			return false;
		}

		return true;
	}

	/**
	 * Returns a new {@link InputStream} for the passed <code>file</code>.
	 *
	 * @throws UncheckedIOException
	 *             if the file is not found.
	 */
	public static InputStream newInputStream(final File file) throws UncheckedIOException {
		try {
			return new FileInputStream(notNull(file, "No file passed!"));

		} catch (final FileNotFoundException e) {
			throw new UncheckedIOException("Error while getting input stream for file " + file + "!", e);
		}
	}

	/**
	 * Returns a new {@link InputStream} for the passed <code>file</code>, starting at given position.
	 *
	 * @throws UncheckedIOException
	 *             if the file is not found, the startPosition is not valid, or an I/O error occurs while seeking the position in the file.
	 */
	public static FileInputStream newInputStream(File file, long startPosition) throws UncheckedIOException {
		try {
			FileInputStream result = new FileInputStream(file);
			result.getChannel().position(startPosition);

			return result;

		} catch (final IOException e) {
			throw new UncheckedIOException("Error while getting input stream for file:" + file, e);
		}
	}

	/**
	 * Returns a new {@link OutputStream} for the passed <code>file</code>.
	 *
	 * @throws UncheckedIOException
	 *             if the file is not found or any other IO error occurs.
	 */
	public static OutputStream newOutputStream(final File file) throws UncheckedIOException {
		return newOutputStream(file, true);
	}

	/**
	 * Returns a new {@link OutputStream} for the passed <code>file</code>.
	 *
	 * @throws UncheckedIOException
	 *             if any IO error occurs.
	 */
	public static OutputStream newOutputStream(final File file, final boolean fileMustExist) throws UncheckedIOException {
		notNull(file, "No file passed!");

		if (!fileMustExist) {
			// create file unless it already exists
			createFile(file);
		}

		try {
			return new FileOutputStream(file);
		} catch (final FileNotFoundException e) {
			throw new UncheckedIOException("Error while getting input stream for file " + file + "!", e);
		}
	}

	/**
	 * Replaces characters that are illegal in file names (in Windows and/or Unix based operating systems).
	 *
	 * @param fileNameWithoutExtension
	 *            the file name without the extension (see {@link #getNameWithoutExtension(String)}.
	 * @param replacementString
	 *            string illegal characters will be replaced with.
	 * @return the (valid) file name
	 */
	public static String replaceIllegalCharactersInFileName(final String fileNameWithoutExtension, final String replacementString) {
		// Based on "How to Strip Invalid Characters from Filenames" OReilly Answers
		// [\\/:"*?<>|]+ Is the pattern for illegal characters
		// In Java pattern we need four slashes for the character \
		final String newFileNameWithoutExtension = fileNameWithoutExtension.replaceAll("[\\\\/:\"*?<>|]+", replacementString);
		return newFileNameWithoutExtension;
	}

	/**
	 * Deletes the specified <code>file</code> (if it exists at all) and makes sure it doesn't exist any more after deletion.
	 */
	public static void deleteFile(final File file) {
		if (file.exists()) {
			final boolean deleted = file.delete();
			if (file.exists()) {
				if (deleted) {
					throw new IllegalStateException("Tried to delete file " + file + ". It still exists although delete method was successful!");
				}
				throw new RuntimeException("Couldn't delete file " + file + "!");
			}

			if (!deleted) {
				// we just ignore this
			}
		}
	}

	public static void deleteIfExists(File file) throws UncheckedIOException {
		if (file.exists()) {
			deleteDirectoryRecursivelyUnchecked(file);
		}
	}

	public static void deleteDirectoryRecursivelyUnchecked(File file) {
		try {
			deleteDirectoryRecursively(file);
		} catch (IOException e) {
			throw new UncheckedIOException("Error while deleting: " + file.getAbsolutePath(), e);
		}
	}

	public static void deleteDirectoryRecursively(File file) throws IOException {
		if (file == null || !file.exists()) {
			return;
		}

		if (file.isDirectory()) {
			for (File c : file.listFiles()) {
				deleteDirectoryRecursively(c);
			}
		}

		/* PGA: Originally, there was only Files.delete. But turns out it sometimes fails, as the file keeps existing but is not accessible - I saw
		 * this in combination with Files.newDirectoryStream() being used on a super-folder, where the stream was not closed. The file.delete worked
		 * in that way. So this seems more stable than the first impl, and because I don't understand the differences between these two deletes, I
		 * keep using both. */
		if (!file.delete() && file.exists()) {
			Files.delete(file.toPath());
		}
	}

	public static void deleteDirectoryRecursivelyOnExit(File file) throws IOException {
		if (file.isDirectory()) {
			for (File c : file.listFiles()) {
				deleteDirectoryRecursivelyOnExit(c);
			}
		}

		file.deleteOnExit();
	}

	/**
	 * {@link #deleteFile(File) Deletes} the specified <code>files</code>.
	 */
	public static void deleteFiles(final Collection<File> files) {
		for (File file : nullSafe(files)) {
			deleteFile(file);
		}
	}

	/** Lists the files in the specified <code>directory</code> using the passed <code>filter</code>. */
	public static List<File> listFiles(final File directory, final Predicate<File> filter) {
		return asList(directory.listFiles(filter::test));
	}

	public static List<File> listFilesRecursively(final File directory) {
		return listFilesRecursively(directory, null);
	}

	public static List<File> listFilesRecursively(final File directory, Predicate<File> filter) {
		if (directory == null) {
			return null;
		}
		final List<File> list = new ArrayList<>();

		if (!directory.isDirectory()) {
			if (filter != null) {
				if (filter.test(directory)) {
					list.add(directory);
				}
			} else {
				list.add(directory);
			}
			return list;
		}

		File[] files = directory.listFiles();
		if (files != null) {
			for (File f : files) {
				if (f.isDirectory()) {
					List<File> subList = listFilesRecursively(f, filter);
					if (subList != null) {
						list.addAll(subList);
					}
				} else {
					if (filter != null) {
						if (filter.test(f)) {
							list.add(f);
						}
					} else {
						list.add(f);
					}
				}
			}
		}

		return list;
	}

	/**
	 * Similar to the {@link #listFilesRecursively(File, Predicate)} method with the difference that the predicate is also applied on folders and not
	 * just on files. When a directory matches the predicate, it and all its sub-folders will not be included in the search.
	 *
	 * @param directory
	 *            The starting point.
	 * @param filter
	 *            An optional filter that decides which files/folders to include.
	 * @return A list of all files found. null is returned if the directory is also null.
	 */
	public static List<File> findRecursively(final File directory, Predicate<File> filter) {
		if (directory == null) {
			return null;
		}
		if (filter == null) {
			filter = f -> true;
		}

		final List<File> list = new ArrayList<>();

		List<File> candidates = new LinkedList<>();
		candidates.add(directory);

		while (!candidates.isEmpty()) {
			File candidate = candidates.remove(0);

			if (candidate != null) {
				if (filter.test(candidate)) {
					if (candidate.isDirectory()) {
						File[] files = candidate.listFiles();
						if (files != null) {
							for (File f : files) {
								candidates.add(f);
							}
						}
					} else {
						list.add(candidate);
					}
				}
			}
		}

		return list;
	}

	public static void copyFileToDirectory(File sourceFile, File targetFolder) throws UncheckedIOException {
		ensureFolderExists(targetFolder);

		copyFileToExistingDirectory(sourceFile, targetFolder);
	}

	public static void copyFileToExistingDirectory(File sourceFile, File targetFolder) throws UncheckedIOException {
		File targetFile = new File(targetFolder, sourceFile.getName());
		copyFile(sourceFile, targetFile);
	}

	public static void copyToDir(File fileOrDir, File targetDir) {
		ensureFolderExists(targetDir);

		copyToExistingDir(fileOrDir, targetDir);
	}

	private static void copyToExistingDir(File fileOrDir, File targetDir) {
		File targetFile = new File(targetDir, fileOrDir.getName());
		copyFileOrDirectory(fileOrDir, targetFile);
	}

	public static void copyFileOrDirectory(File source, File target) throws UncheckedIOException {
		if (source.isDirectory()) {
			copyDirectoryUnchecked(source, target);
		} else {
			copyFile(source, target);
		}
	}

	/**
	 * Copies the content of <code>source</code> to <code>target</code> (which will be overwritten, if it already exists).
	 */
	public static void copyFile(File source, File target) throws UncheckedIOException {
		try (InputStream inputStream = new FileInputStream(source); OutputStream outputStream = newOutputStream(target, false)) {
			IOTools.inputToOutput(inputStream, outputStream);

		} catch (final IOException e) {
			throw new UncheckedIOException("Error while copying file " + source.getAbsolutePath() + " to " + target.getAbsolutePath() + ".", e);
		}
	}

	/**
	 * Creates the specified file (unless it already exists).
	 *
	 * @throws UncheckedIOException
	 *             if any IO error occurs.
	 */
	public static File createFile(final File file) {
		if (!file.exists()) {
			if (file.getParent() != null) { // a relative path as in 'new File("test)' has no parent
				createDirectory(file.getParent());
			}

			try {
				file.createNewFile();
			} catch (final IOException e) {
				throw new UncheckedIOException("Error while creating new file " + file + "!", e);
			}
		}
		return file;
	}

	/**
	 * {@link FileTools#createFile(File) Creates} the specified file.
	 *
	 * @throws UncheckedIOException
	 *             if the file already exists or any IO error occurs.
	 */
	public static File createNewFile(final File file) {
		if (file.exists()) {
			throw new IllegalArgumentException("The file " + file + " already exists!");
		}
		return createFile(file);
	}

	/**
	 * {@link FileTools#deleteFile(File) Delete} the specified file. Even it has read write permission. It deletes recursively if path is a non empty
	 * folder and {@link FileTools#notSymbolicLink(File) not a symbolic link}.
	 */
	public static void deleteForcedly(final File path) throws java.lang.SecurityException, UncheckedIOException {
		if (!path.exists()) {
			return;
		}
		if (!path.canRead()) {
			path.setReadable(true, false);
		}
		if (!path.canWrite()) {
			path.setWritable(true, false);
		}
		if (path.isDirectory() && notSymbolicLink(path)) {
			final File[] childs = path.listFiles();
			if (null != childs) {
				for (File child : childs) {
					deleteForcedly(child);
				}
			}
		}

		deleteFile(path);
	}

	public static boolean notSymbolicLink(final File file) throws java.lang.SecurityException, UncheckedIOException {
		if (File.separatorChar == '\\' || !file.exists()) {
			return true;// Windows OS case
		}
		final File parent = file.getParentFile();
		try {
			final File canonicalFile = null == parent ? file.getCanonicalFile() : new File(parent.getCanonicalFile(), file.getName());
			return canonicalFile.equals(canonicalFile.getAbsoluteFile());
		} catch (IOException e) {
			throw new UncheckedIOException("File does exist. May be a symbolic link.", e);
		}
	}

	/**
	 * Creates a new {@link File} instance using the specified <code>filepath</code>. The file must be a file (and not a directory) and must already
	 * exist.
	 *
	 * @see #getFile(String, boolean, boolean, boolean, boolean)
	 */
	public static File getExistingFile(String filepath) throws IllegalArgumentException, IOException {
		return getFile(filepath, true, false, true, false);
	}

	/**
	 * Invokes {@link #getFile(String, boolean, boolean, boolean, boolean, boolean)} without skipping file/directory creation.
	 */
	public static File getFile(String filepath, boolean mustExist, boolean mustNotExist, boolean isFile, boolean isDirectory)
			throws IllegalArgumentException, IOException {
		return getFile(filepath, mustExist, mustNotExist, isFile, isDirectory, false);
	}

	/**
	 * Creates a new {@link File} instance using the specified <code>filepath</code>. If the file (or directory) doesn't exist yet, it is created,
	 * unless either <code>mustExist</code> is <code>true</code> (in which case an exception is thrown) or
	 * <code>skipFileCreationForNotExistingFiles</code> is <code>true</code> (in which case neither the file/directory nor any missing parent
	 * directories are created).
	 *
	 * @param filepath
	 *            the filename and path of the file.
	 * @param mustExist
	 *            whether the file must exist.
	 * @param mustNotExist
	 *            whether the file must not exist.
	 * @param isFile
	 *            whether the file must be a file.
	 * @param isDirectory
	 *            whether the file must be a directory
	 * @param skipFileCreationForNotExistingFiles
	 *            if <code>true</code>, only the <code>File</code> instance is returned, the file/directory and any missing parent directoies are not
	 *            created.
	 * @return the {@code File}.
	 * @throws IllegalArgumentException
	 *             if the passed arguments are not valid, e.g. if both, <code>mustExist</code> and <code>mustNotExist</code> are <code>true</code> or
	 *             if the specified <code>filepath</code> is empty.
	 * @throws IOException
	 *             if the file doens't exist (although it should), if it can't be read, etc.
	 */
	public static File getFile(String filepath, boolean mustExist, boolean mustNotExist, boolean isFile, boolean isDirectory,
			boolean skipFileCreationForNotExistingFiles) throws IllegalArgumentException, IOException {

		if (StringTools.isEmpty(filepath)) {
			throw new IllegalArgumentException("Filepath must not be empty! filepath==" + filepath);
		}

		if (mustExist && mustNotExist) {
			throw new IllegalArgumentException("File cannot exist and not exist at the same time!");
		}

		if (isFile && isDirectory) {
			throw new IllegalArgumentException("File cannot be a diretory AND a file!");
		}

		if (!(isFile || isDirectory)) {
			throw new IllegalArgumentException("File must be either a file or a directory, but both arguments are false!");
		}

		File file = new File(filepath);

		if (file.exists() && mustNotExist) {
			throw new IOException("File '" + file + "' exists although it shouldn't!");
		}

		if (!file.exists() && mustExist) {
			throw new IOException("File '" + file + "' doesn't exist although it should!");
		}

		boolean fileReadCheckEnabled = true;

		if (!file.exists()) {
			if (!skipFileCreationForNotExistingFiles) {
				if (!file.getParentFile().exists()) {
					// parent dir doesn't exist --> create it
					file.getParentFile().mkdirs();
				}

				if (isDirectory) {
					// create directory
					file.mkdir();
				} else {
					// create new file
					file.createNewFile();
				}
			} else {
				// since we skip the file creation, we can disable the read check.
				fileReadCheckEnabled = false;
			}
		} else {
			// make sure file is a directory/file (as expected)
			if (file.isDirectory() != isDirectory) {
				throw new IOException(
						"File '" + filepath + "' is a directory: " + file.isDirectory() + ", file should be a directory: " + isDirectory);
			}
		}

		if (fileReadCheckEnabled && !file.canRead()) {
			throw new IOException("Cannot read file " + file + ".");
		}

		return file;
	}

	/**
	 * Returns the absolute, canonical path of the working directory.
	 */
	public static String getWorkingDirectoryPath() {
		try {
			return new File(".").getCanonicalFile().getAbsolutePath();
		} catch (IOException e) {
			throw new UncheckedIOException("Error while getting canonical path of working directory!", e);
		}
	}

	/**
	 * Takes a filename and removes all characters that might cause trouble in filesystems. Basically, this method replaces all characters that are
	 * NOT alphanumerical, '-', '_', '.' by the character provided in the 2. argument. The length of the filename does not get changed.
	 *
	 * @param filename
	 *            The filename that should be sanitized.
	 * @param filler
	 *            The character that should be used instead of problematic characters.
	 * @return The sanitizied file name, or null if the original filename was also null.
	 */
	public static String normalizeFilename(String filename, char filler) {
		if (filename == null) {
			return null;
		}

		char[] chars = filename.toCharArray();
		char[] cleanChars = new char[chars.length];

		for (int i = 0; i < chars.length; ++i) {
			char c = chars[i];
			if (!Character.isLetterOrDigit(c)) {
				if ((c != '.') && (c != '_') && (c != '-')) {
					c = filler;
				}
			}
			cleanChars[i] = c;
		}

		filename = new String(cleanChars);

		return filename;
	}

	/**
	 * Returns the <code>fileObjects</code> that represent a {@link File#isFile() file} (and not a directory).
	 *
	 * @see #getDirectories(Collection)
	 */
	public static List<File> getFiles(Collection<File> fileObjects) {
		return nullSafe(fileObjects).stream() //
				.filter(fo -> fo != null && fo.isFile()) //
				.collect(Collectors.toList());
	}

	/**
	 * Returns the <code>fileObjects</code> that represent a {@link File#isDirectory() directory} (and not a file).
	 *
	 * @see #getFiles(Collection)
	 */
	public static List<File> getDirectories(Collection<File> fileObjects) {
		return nullSafe(fileObjects).stream() //
				.filter(fo -> fo != null && fo.isDirectory()) //
				.collect(Collectors.toList());
	}

	/**
	 * Removes all empty directories in the provided baseFolder. The baseFolder itself is not removed, even if it is empty after the removal of
	 * substructures.
	 *
	 * @param baseFolder
	 *            The folder that should be cleared from empty directories.
	 */
	public static void cleanEmptyFolders(File baseFolder) {
		cleanEmptyFolders(baseFolder, true);
	}

	/**
	 * Cleans a hierarchical file structure from empty directories. When a folder does contain one or more files (or other non-empty directories), it
	 * will not be deleted.
	 *
	 * @param baseFolder
	 *            The base folder where the cleaning process should start.
	 * @param keepThisFolder
	 *            Indicates whether the provided folder itself should be deleted or not at the end (if it is empty).
	 */
	public static void cleanEmptyFolders(File baseFolder, boolean keepThisFolder) {
		if (baseFolder == null) {
			return;
		}
		if (!baseFolder.isDirectory()) {
			return;
		}
		boolean foundFile = false;
		File[] entries = baseFolder.listFiles();
		if (entries != null && entries.length > 0) {
			for (File entry : entries) {
				if (entry.isDirectory()) {
					cleanEmptyFolders(entry, false);
				} else {
					foundFile = true;
					break;
				}
			}
		} else {
			if (!keepThisFolder) {
				baseFolder.delete();
			}
			return;
		}
		if (foundFile) {
			return;
		}

		// At this point, we have hopefully deleted all sub-folders; trying again
		entries = baseFolder.listFiles();
		if (entries == null || entries.length == 0) {
			if (!keepThisFolder) {
				baseFolder.delete();
			}
		}
	}

	public static String getRelativePath(File fileOrDir, File baseDir) {
		if (fileOrDir == null || baseDir == null) {
			throw new NullPointerException("Either fileOrDir (" + fileOrDir + ") or baseDir (" + baseDir + ") is null.");
		}
		String baseDirPath = baseDir.getAbsolutePath();
		String filePath = fileOrDir.getAbsolutePath();
		if (!filePath.startsWith(baseDirPath)) {
			return filePath;
		}
		if (baseDirPath.equals(filePath)) {
			return "";
		}

		String relPath = filePath.substring(baseDirPath.length() + 1);
		return relPath;
	}

	public static void patternFormatTextFile(File textInputFile, File outputFile, String encoding, Map<String, Object> properties,
			Object defaultValue) throws NullPointerException, IOException {

		if (textInputFile == null) {
			throw new NullPointerException("The input file is null.");
		}
		if (!textInputFile.exists()) {
			throw new FileNotFoundException("Could not find the input file " + textInputFile.getAbsolutePath());
		}
		if (outputFile == null) {
			throw new NullPointerException("The output file is null.");
		}
		File outputDir = outputFile.getParentFile();
		if (!outputDir.exists()) {
			outputDir.mkdirs();
		}
		if (properties == null || properties.isEmpty()) {
			copyFile(textInputFile, outputFile);
			return;
		}
		if (encoding == null || encoding.trim().length() == 0) {
			encoding = "UTF-8";
		}

		String line = null;
		BufferedReader bufferedReader = null;
		BufferedWriter bufferedWriter = null;
		FileInputStream fis = null;
		FileOutputStream fos = null;
		try {
			fis = new FileInputStream(textInputFile);
			bufferedReader = new BufferedReader(new InputStreamReader(fis, encoding));

			fos = new FileOutputStream(outputFile);
			bufferedWriter = new BufferedWriter(new OutputStreamWriter(fos, encoding));

			while (true) {
				line = bufferedReader.readLine();
				if (line == null) {
					break;
				}

				try {
					String formattedLine = StringTools.patternFormat(line, properties, defaultValue);
					if (formattedLine != null) {
						bufferedWriter.write(formattedLine);
					}
				} catch (IllegalArgumentException iae) {
					bufferedWriter.write(line);
				}
				bufferedWriter.newLine();
			}

		} finally {
			IOTools.closeCloseable(bufferedWriter, logger);
			IOTools.closeCloseable(fos, logger);
			IOTools.closeCloseable(bufferedReader, logger);
			IOTools.closeCloseable(fis, logger);
		}

	}

	public static String getNextFreeFilename(Collection<String> filenames, String filename) {

		String lowerCaseFilename = filename.toLowerCase();

		if (!filenames.contains(lowerCaseFilename)) {
			filenames.add(lowerCaseFilename);
			return filename;
		}

		String lowerCasePrefix = lowerCaseFilename;
		String lowerCaseSuffix = "";
		String prefix = filename;
		String suffix = "";

		int idx = lowerCaseFilename.lastIndexOf(".");
		if (idx != -1) {
			lowerCasePrefix = lowerCaseFilename.substring(0, idx);
			lowerCaseSuffix = lowerCaseFilename.substring(idx);
			prefix = filename.substring(0, idx);
			suffix = filename.substring(idx);
		}

		int count = 2;
		while (filenames.contains(lowerCaseFilename)) {
			lowerCaseFilename = lowerCasePrefix + count + lowerCaseSuffix;
			count++;
		}
		filenames.add(lowerCaseFilename);

		return prefix + (count - 1) + suffix;
	}

	/**
	 * Delegates to {@link #findFreeFilename(File, String, int, boolean)} with <code>skipBaseName</code> disabled.
	 */
	public static File findFreeFilename(File directory, String fullFilename, int maxtries) throws Exception {
		return findFreeFilename(directory, fullFilename, maxtries, false);
	}

	/**
	 * Searches for a free (unused) filename in the given directory. The provided filename is split into name and extension and a counter is used to
	 * search for free filenames (e.g., filename1.ext, filename2.ext).
	 *
	 * @param directory
	 *            - The directory where the filename should be searched for
	 * @param fullFilename
	 *            - The desired filename
	 * @param maxtries
	 *            - The maximum number of tries this method should take to find a free filename.
	 * @param skipBaseName
	 *            - Do skip the base name and start the check with <code>&lt;basename&gt;_&lt;count&gt;.&lt;extension&gt;</code>
	 *
	 * @return The given filename, if it's unused, or a new one with a counter
	 *
	 * @throws Exception
	 *             if any error occurs during processing
	 */
	public static File findFreeFilename(File directory, String fullFilename, int maxtries, boolean skipBaseName) throws Exception {

		String filename = fullFilename;
		String extension = "";

		int idx = fullFilename.lastIndexOf(".");
		if (idx != -1) {
			filename = fullFilename.substring(0, idx);
			extension = fullFilename.substring(idx);
		}

		File f = new File(directory, fullFilename);
		int count = 0;

		if (skipBaseName) {
			count++;
			f = new File(directory, filename + "_" + count + extension);
		}

		while (f.exists()) {
			if (count++ > maxtries) {
				throw new Exception(String.format("could not find a free filename for %s; max retries of %d reached", fullFilename, maxtries));
			}

			f = new File(directory, filename + "_" + count + extension);
		}

		return f;
	}

	/**
	 * Invokes {@link #copyDirectory(File, File, boolean)} without traversing symbolic links and un-checks any possible {@link IOException}.
	 */
	public static void copyDirectoryUnchecked(File source, File target) {
		try {
			copyDirectory(source, target, false);
		} catch (IOException e) {
			throw Exceptions.unchecked(e, "Error while copying directory '" + source.getAbsolutePath() + "' to '" + target.getAbsolutePath() + "'");
		}
	}

	public static void deleteRecursivelySymbolLinkAware(File file) {
		Path path = file.toPath();

		if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
			return;
		}

		try {
			if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
				for (File child : file.listFiles()) {
					deleteRecursivelySymbolLinkAware(child);
				}
			}

			Files.delete(path);

		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	/**
	 * Copies a sourceFile to a targetFile keeping symbol links. If the sourceFile is a directory the process will work recursively.
	 */
	public static void copyRecursivelyAndKeepSymbolLinks(File sourceFile, File targetFile) {
		FileTools.copy(sourceFile).as(targetFile).please();
	}

	/**
	 * Copies a sourceFile to a targetFile keeping symbol links. If the sourceFile is a directory the process will work recursively.
	 *
	 * @param sourceFile
	 *            the source file or directory to be copied
	 * @param targetFile
	 *            the target file or directory
	 * @param skipFilter
	 *            the filter that will skip files or folders during copying
	 */
	public static void copyRecursivelyAndKeepSymbolLinks(File sourceFile, File targetFile, BiPredicate<File, File> skipFilter) {
		FileTools.copy(sourceFile).as(targetFile).filter(skipFilter).please();
	}

	/** Invokes {@link #copyDirectory(File, File, boolean)} without traversing symbolic links. */
	public static void copyDirectory(File source, File target) throws IOException {
		copyDirectory(source, target, false);
	}

	/**
	 * Copies the content of the directory <code>source</code> to the directory <code>target</code>. The method traverse the file tree and
	 * {@link #copyFile(File, File) copies} each file encountered. Based on the argument <code>followLinks</code> one can traverse a symbolic link
	 * that points to some directory. Of course that only make sense if there are no cycle paths during traversing.
	 *
	 * @param source
	 *            the directory which content we want to copy.
	 * @param target
	 *            the directory where we want the content to be copied to. If this directory doesn't exist, it is created automatically.
	 * @param followLinks
	 *            if <code>true</code>, follow the symbolic links that points to a directory
	 * @throws IOException
	 *             if any error occurs during directory copying
	 */
	public static void copyDirectory(File source, File target, boolean followLinks) throws IOException {
		if (!source.exists()) {
			return;
		}

		ensureFolderExists(target);

		final Path fromPath = source.toPath();
		final Path toPath = target.toPath();

		SimpleFileVisitor<Path> fileCopyVisitor = new SimpleFileVisitor<Path>() {

			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				Path targetPath = toPath.resolve(fromPath.relativize(dir));
				if (!Files.exists(targetPath)) {
					Files.createDirectory(targetPath);
				}
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				File sourceFile = file.toFile();
				File targetFile = toPath.resolve(fromPath.relativize(file)).toFile();

				FileTools.copyFile(sourceFile, targetFile);
				return FileVisitResult.CONTINUE;
			}
		};

		Set<FileVisitOption> fileVisitOptions = CommonTools.getSet();
		if (followLinks) {
			fileVisitOptions.add(FileVisitOption.FOLLOW_LINKS);
		}
		Files.walkFileTree(source.toPath(), fileVisitOptions, Integer.MAX_VALUE, fileCopyVisitor);
	}

	/**
	 * Returns <code>true</code>, if the specified path is an {@link File#isAbsolute() absolute} file path, otherwise <code>false</code>. Note that
	 * this method is platform-dependent. A path like "/a/b/c" will be absolute in a Unix-environment, but will be treated as a relative path on
	 * Windows as it would be relative to the current drive.
	 */
	public static boolean isAbsolutePath(String filePath) {
		return new File(filePath).isAbsolute();
	}

	/**
	 * Indicates whether the provided File is meant to be a /dev/null location. Depending on the operating system, a simple file path like /dev/null
	 * might be changed to a arbitrary absolute path.
	 *
	 * @param file
	 *            The File object that should be examined.
	 * @return True, if /dev/null was meant by the parameter, false otherwise.
	 */
	public static boolean isDevNull(File file) {
		if (file == null) {
			return true;
		}
		String absPath = file.getAbsolutePath().toLowerCase();
		if (absPath.startsWith("/dev/null") || absPath.endsWith("/dev/null")) {
			return true;
		}
		if (absPath.startsWith("\\dev\\null") || absPath.endsWith("\\dev\\null")) {
			return true;
		}
		return false;
	}

	public static void deleteFileSilently(File file) {
		if (file == null) {
			return;
		}
		deleteFileSilently(file.getAbsolutePath());
	}

	public static void deleteFileSilently(String filePath) {
		if (filePath == null) {
			return;
		}
		try {
			File blobFile = new File(filePath);

			if (blobFile.exists()) {
				blobFile.delete();
				logger.debug(() -> "Deleted file: " + filePath);
			}
		} catch (Exception e) {
			logger.error("Could not delete file " + filePath, e);
		}
	}

	/** Please read the {@link FileAutoDeletion#deleteFileWhenOrphaned(File) actual implementation} carefully. */
	public static void deleteFileWhenOrphaned(File tempFile) {
		FileAutoDeletion.deleteFileWhenOrphaned(tempFile);
	}

	/**
	 * Returns the filename part of a full path
	 *
	 * @param fullpath
	 *            The full path that should be parsed
	 * @return The filename component, which is basically everything after the last slash or backslash
	 */
	public static String getFilenameFromFullPath(String fullpath) {
		if (fullpath == null) {
			return null;
		}
		int idx1 = fullpath.lastIndexOf('/');
		int idx2 = fullpath.lastIndexOf('\\');
		int idx = Math.max(idx1, idx2);
		if (idx == -1) {
			return fullpath;
		}

		return fullpath.substring(idx + 1);
	}

	public static void unzip(File zipFile, File targetDir) {
		ZipTools.unzip(zipFile, targetDir, null);
	}

	public static void unzip(File zipFile, File targetDir, Function<String, File> mapper) {
		ZipTools.unzip(zipFile, targetDir, mapper);
	}

	/** @see FileNameBasedFolderComparator#compare(File, File) */
	public static FolderComparison compareFoldersBasedOnFileNames(File leftDir, File rightDir) {
		return FileNameBasedFolderComparator.compare(leftDir, rightDir);
	}

	/**
	 * Checks whether the provided <code>fileToCheck</code> (which basically could be a directory as well) is contained in <code>directory</code> or
	 * one of its subdirectories.
	 *
	 * @param directory
	 *            The directory where the <code>fileToCheck</code> may reside (or not)
	 * @param fileToCheck
	 *            The file/directory to check.
	 * @return True, if <code>fileToCheck</code> is located within <code>directory</code>, false otherwise.
	 * @throws IllegalArgumentException
	 *             Thrown when <code>directory</code> is not a directory.
	 * @throws NullPointerException
	 *             Thrown when one of the two parameters is null.
	 * @throws UncheckedIOException
	 *             Thrown when the canonical paths of either directory or fileToCheck throw an exception.
	 */
	public static boolean isInSubDirectory(File directory, File fileToCheck) {
		requireNonNull(directory, "directory must not be null.");
		requireNonNull(fileToCheck, "fileToCheck must not be null.");

		if (directory.exists() && !directory.isDirectory()) {
			throw new NullPointerException("The directory \"" + directory + "\" must be an actual folder.");
		}

		try {
			String folderPath = directory.getCanonicalPath();
			String filePath = fileToCheck.getCanonicalPath();

			return filePath.startsWith(folderPath);

		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	/**
	 * Collects a list of File objects, containing all folders (starting from the startDir up to the (optional) stopDir). When the stopDir is null,
	 * the file system's root folder is the stopDir. The stopDir will not be included in the list, the startDir is included.
	 *
	 * @param startDir
	 *            The starting folder.
	 * @param stopDir
	 *            The folder where the process should stop (will not be included in the resulting list)
	 * @return A list of folders, up to the stopDir that are empty. When the startDir does not exist, an empty list is returned.
	 * @throws IOException
	 *             When the file system cannot be accessed.
	 * @throws IllegalArgumentException
	 *             When the startDir folder is either null or not a folder.
	 */
	public static List<File> collectEmptySuperFolders(File startDir, File stopDir) throws IOException {
		requireNonNull(startDir, "startDir must not be null.");

		if (!startDir.exists()) {
			return Collections.emptyList();
		}

		if (!startDir.isDirectory()) {
			throw new IllegalArgumentException("The start folder must be an actual directory.");
		}

		File currentDir = startDir;

		if (stopDir != null) {
			String currentDirString = currentDir.getAbsolutePath();
			String stopDirString = stopDir.getAbsolutePath();
			if (!currentDirString.startsWith(stopDirString)) {
				throw new IllegalArgumentException("The start folder " + currentDirString + " does not seem to be part of " + stopDirString);
			}
		}
		List<File> result = new ArrayList<>();
		String previousFolderName = null;

		while (currentDir != null && (stopDir == null || !currentDir.equals(stopDir))) {

			File[] subFiles = currentDir.listFiles();

			if (subFiles.length == 0) {
				result.add(currentDir);
			} else {
				String filename = subFiles[0].getName();
				if (previousFolderName == null || !previousFolderName.equals(filename)) {
					break;
				}
				if (subFiles.length > 1) {
					// Uh-oh, there is at least another entry; abort
					break;
				}
				result.add(currentDir);
			}

			previousFolderName = currentDir.getName();
			currentDir = currentDir.getParentFile();
		}

		return result;
	}

	/** Returns the relative portion of given file path's, consisting of up-to {@code relativeDepth} parts. */
	public static Path relativePart(File file, int relativeDepth) {
		Path originalPath = file.toPath();
		Path parent = originalPath;

		while (relativeDepth-- > 0) {
			parent = parent.getParent();
			if (parent == null) {
				return originalPath;
			}
		}

		return parent.relativize(originalPath);
	}

	/**
	 * Truncates a filename while trying to keep the extension. The length of the resulting String does not exceed the number of UTF-8 bytes of the
	 * input.
	 *
	 * @param filename
	 *            The filename that should be truncated.
	 * @param maxBytesLength
	 *            The maximum number of UTF-8 bytes of the result.
	 * @return The truncated filename.
	 */
	public static String truncateFilenameByUtf8BytesLength(String filename, int maxBytesLength) {
		if (filename == null) {
			return filename;
		}
		if (maxBytesLength < 0) {
			throw new IllegalArgumentException("The maxBytesLength must not be less than 0: " + maxBytesLength);
		}
		if (maxBytesLength == 0) {
			return "";
		}
		byte[] sba = filename.getBytes(StandardCharsets.UTF_8);
		if (sba.length <= maxBytesLength) {
			return filename;
		}

		String rawName = getNameWithoutExtension(filename);
		String extension = getExtension(filename);
		if (StringTools.isBlank(extension)) {
			return StringTools.substringByUtf8BytesLength(filename, maxBytesLength);
		}

		int extensionLength = extension.getBytes(StandardCharsets.UTF_8).length;

		if (extensionLength <= 4) {
			// Seems like a "normal" extension; let's try to keep it.
			int maxRawNameLength = maxBytesLength - extensionLength - 1;
			if (maxRawNameLength < 1) {
				// No chance to keep the extension
				return StringTools.substringByUtf8BytesLength(filename, maxBytesLength);
			} else {
				String truncatedRawName = StringTools.substringByUtf8BytesLength(rawName, maxRawNameLength);
				if (truncatedRawName.length() == 0) {
					return StringTools.substringByUtf8BytesLength(filename, maxBytesLength);
				} else {
					return truncatedRawName + "." + extension;
				}
			}
		} else {
			// Maybe the extension is not an extension but rather some random part after a '.'
			return StringTools.substringByUtf8BytesLength(filename, maxBytesLength);
		}
	}

	/**
	 * Attempts to calculate the size of a file or directory.
	 * <p>
	 * Since the operation is non-atomic, the returned value may be inaccurate. However, this method is quick and does its best.
	 */
	public static FolderSize getFolderSize(Path path) {
		return FolderSize.of(path);
	}

	/**
	 * Returns the total size in bytes of all transitively contained files and directories.
	 * <p>
	 * Since the operation is non-atomic, the returned value may be inaccurate. However, this method is quick and does its best.
	 *
	 * @see #getFolderSize(Path)
	 */
	public static long getFolderSizeInBytes(Path path) {
		return getFolderSize(path).getSize();
	}

}
