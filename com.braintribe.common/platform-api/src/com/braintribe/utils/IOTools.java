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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Properties;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.braintribe.common.lcd.AssertionException;
import com.braintribe.common.lcd.Constants;
import com.braintribe.common.lcd.Numbers;
import com.braintribe.common.uncheckedcounterpartexceptions.UncheckedMalformedURLException;
import com.braintribe.exception.Exceptions;
import com.braintribe.logging.Logger;
import com.braintribe.utils.lcd.Not;
import com.braintribe.utils.lcd.NullSafe;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * This class may contain Java-only (i.e. GWT incompatible) code. For further information please see {@link com.braintribe.utils.lcd.IOTools}.
 *
 * @author michael.lafite
 */
public class IOTools extends com.braintribe.utils.lcd.IOTools {

	private static final Logger logger = Logger.getLogger(IOTools.class);
	private static byte[] bitchBuffer;

	protected IOTools() {
		// nothing to do
	}

	public static void download(final URL url, final File file) throws IOException {
		final InputStream inputStream = url.openStream();
		final OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file));
		try {
			final int bufferSize = 16384;
			final byte[] buffer = new byte[bufferSize];
			while (true) {
				final int len = inputStream.read(buffer);
				if (len <= Numbers.ZERO) {
					break;
				}
				outputStream.write(buffer, Numbers.NO_OFFSET, len);
			}
		} finally {
			outputStream.close();
			inputStream.close();
		}
	}

	public static String slurp(final URL url) throws IOException {
		final URLConnection conn = url.openConnection();
		final InputStream inputStream = Not.Null(conn.getInputStream());
		final String result = slurp(inputStream, conn.getContentEncoding());
		inputStream.close();
		return result;
	}

	public static String slurp(final URL url, final String encoding) throws IOException {
		final URLConnection conn = url.openConnection();
		final InputStream inputStream = Not.Null(conn.getInputStream());
		final String result = slurp(inputStream, encoding);
		inputStream.close();
		return result;
	}

	public static void pump(final Reader reader, final Writer writer, int bufferSize) throws IOException {
		char[] buffer = new char[bufferSize];

		int c = -1;

		while ((c = reader.read(buffer)) != -1) {
			writer.write(buffer, 0, c);
		}

		writer.flush();
	}

	public static void pump(final Reader reader, final Writer writer) throws IOException {
		char[] buffer = new char[SIZE_64K];

		int c = -1;

		while ((c = reader.read(buffer)) != -1) {
			writer.write(buffer, 0, c);
		}

		writer.flush();
	}

	public static long transferBytes(InputStream inputStream, OutputStream outputStream) {
		return transferBytes(inputStream, outputStream, BUFFER_SUPPLIER_64K);
	}

	private static byte[] getBitchBuffer() {
		if (bitchBuffer == null) {
			bitchBuffer = BUFFER_SUPPLIER_64K.get();
		}

		return bitchBuffer;
	}

	public static long consume(InputStream in) throws IOException {
		byte buffer[] = getBitchBuffer();

		long bytesReadTotally = 0;
		int bytesRead;

		while ((bytesRead = in.read(buffer)) != -1) {
			bytesReadTotally += bytesRead;
		}

		return bytesReadTotally;
	}

	public static long transferBytes(InputStream inputStream, OutputStream outputStream, Supplier<byte[]> bufferSupplier) {

		byte[] buffer = bufferSupplier.get();

		int count = 0;
		long totalCount = 0;

		try {
			while ((count = inputStream.read(buffer)) != -1) {

				outputStream.write(buffer, 0, count);

				totalCount += count;
			}
		} catch (IOException e) {
			throw new UncheckedIOException("Error while transfering data. Data transferred so far: " + totalCount + ". Current buffer size: " + count,
					e);
		}

		return totalCount;

	}

	// temporary method that adds some fixes for the pump() method, there might be a better solution though
	// private static int pump_fixed(final InputStream inputStream, final OutputStream outputStream) throws IOException
	// {
	// final int bufferSize = 4096;
	// final byte[] buffer = new byte[bufferSize];
	//
	// int bytesProcessed = 0;
	//
	// // if the input stream supports the available(), we use it.
	// boolean availableSupported = inputStream.available() > 0;
	// // for some reason we must NOT break on 0 available, if the input stream is as HTTP_InputStream
	// // TODO: it should not be necessary to handle that stream specifically!
	// boolean breakIfNoneAvailable = availableSupported &&
	// (!inputStream.getClass().getName().equals("biz.i2z.comm.service.http.HTTP_InputStream"));
	//
	// while (true) {
	//
	// int bytesRead;
	//
	// if (availableSupported) {
	// int bytesAvailable = inputStream.available();
	// if (breakIfNoneAvailable && bytesAvailable <= 0) {
	// break;
	// }
	//
	// int bytesToRead = Math.min(bufferSize, bytesAvailable);
	// bytesRead = inputStream.read(buffer, 0, bytesToRead);
	// } else {
	// /*
	// * The following line doesn't always work properly, e.g. there are issues with
	// * biz.i2z.comm.service.http.HTTP_InputStream (not whole content is read).
	// */
	// bytesRead = inputStream.read(buffer);
	// }
	//
	// if (bytesRead == Numbers.NEGATIVE_ONE) {
	// break;
	// }
	// outputStream.write(buffer, 0, bytesRead);
	// bytesProcessed += bytesRead;
	// }
	//
	// outputStream.flush();
	// return bytesProcessed;
	// }

	public static byte[] slurpBytes(final InputStream inputStream) throws IOException {
		return slurpBytes(inputStream, false);
	}

	public static byte[] slurpBytes(final InputStream inputStream, boolean closeStream) throws IOException {
		final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		pump(inputStream, outputStream);
		if (closeStream) {
			closeCloseable(inputStream, logger);
		}
		return Not.Null(outputStream.toByteArray());

	}

	public static String slurp(final File file, final String encoding) throws IOException {
		final FileInputStream inputStream = new FileInputStream(file);
		String result = null;
		try {
			result = slurp(inputStream, encoding);
		} finally {
			inputStream.close();
		}
		return result;
	}

	@SuppressFBWarnings("DM_DEFAULT_ENCODING")
	// default encoding is only used if the encoding is not provided
	public static String slurp(final InputStream inputStream, final String encoding) throws IOException {
		final InputStreamReader reader = ((encoding == null) ? new InputStreamReader(inputStream) : new InputStreamReader(inputStream, encoding));
		final String result = slurp(reader);
		reader.close();

		return result;
	}

	public static String slurp(final Reader reader) throws IOException {
		final StringBuilder stringBuilder = new StringBuilder();
		final int bufferSize = 16384;
		final char[] buff = new char[bufferSize];
		while (true) {
			final int len = reader.read(buff);
			if (len <= Numbers.ZERO) {
				break;
			}
			stringBuilder.append(buff, Numbers.NO_OFFSET, len);
		}

		return stringBuilder.toString();
	}

	public static void writeLines(Path path, Stream<? extends CharSequence> lines) {
		try {
			Files.write(path, ((Stream<CharSequence>) (Stream<?>) lines)::iterator);

		} catch (IOException e) {
			throw Exceptions.unchecked(e, "Error while writing model names to file: " + path, UncheckedIOException::new);
		}
	}

	public static void writeLines(Path path, Iterable<? extends CharSequence> lines) {
		try {
			Files.write(path, lines);

		} catch (IOException e) {
			throw Exceptions.unchecked(e, "Error while writing model names to file: " + path, UncheckedIOException::new);
		}
	}

	public static Stream<String> linesUnchecked(File file) {
		return linesUnchecked(file.toPath());
	}

	public static Stream<String> linesUnchecked(Path path) {
		try {
			return Files.lines(path);
		} catch (IOException e) {
			throw Exceptions.unchecked(e, "Error while reading lines from: " + path.toAbsolutePath(), java.io.UncheckedIOException::new);
		}
	}

	public static void spit(final File file, final String data, final String encoding, final boolean append) throws IOException {
		final OutputStream outputStream = new FileOutputStream(file, append);
		final Writer writer = new OutputStreamWriter(outputStream, encoding);
		try {
			writer.write(data);
		} finally {
			writer.close();
			outputStream.close();
		}
	}

	public static boolean hasValidExtension(final String name, final Iterable<String> validExtensions) {
		final String ext = getExtension(name);
		for (final String curExt : validExtensions) {
			if (curExt.equalsIgnoreCase(ext)) {
				return true;
			}
		}
		return false;
	}

	public static Properties readAsProperties(InputStream inputStream) throws IOException {
		Properties properties = new Properties();
		properties.load(inputStream);
		return properties;
	}

	public static boolean hasValidExtension(final File file, final Iterable<String> validExtensions) {
		return hasValidExtension(Not.Null(file.getName()), validExtensions);
	}

	public static String getExtension(final String file) {
		final int index = file.lastIndexOf('.');
		if (index >= Numbers.ZERO) {
			return Not.Null(file.substring(index + 1));
		} else {
			return null;
		}
	}

	public static String getExtension(final File file) {
		return getExtension(Not.Null(file.getName()));
	}

	public static boolean replaceInFile(final String pathname, final Map<String, String> keyValueMap, final boolean useRegExp) throws IOException {
		return replaceInFile(new File(pathname), keyValueMap, useRegExp, null);
	}

	public static boolean replaceInFile(final File file, final Map<String, String> keyValueMap, final boolean useRegExp) throws IOException {
		return replaceInFile(file, keyValueMap, useRegExp, null);
	}

	public static boolean replaceInFile(final String pathname, final Map<String, String> keyValueMap, final boolean useRegExp,
			final String charsetName) throws IOException {
		return replaceInFile(new File(pathname), keyValueMap, useRegExp, charsetName);
	}

	@SuppressFBWarnings("DM_DEFAULT_ENCODING")
	// default encoding is only used if the charset name is not provided
	public static boolean replaceInFile(final File file, final Map<String, String> keyValueMap, final boolean useRegExp, final String charsetName)
			throws IOException {
		FileInputStream fis = null;
		byte[] byteArray = null;
		try {
			fis = new FileInputStream(file);
			final int len = (int) file.length();
			byteArray = new byte[len];
			final int numberOfBytesRead = fis.read(byteArray, 0, len);
			if (numberOfBytesRead != len) {
				throw new IOException("Couldn't read file completely! "
						+ CommonTools.getParametersString("file", file, "file size in bytes", len, "number of bytes read", numberOfBytesRead));
			}
		} finally {
			if (fis != null) {
				fis.close();
			}
		}

		String str = null;
		if (charsetName == null) {
			str = new String(byteArray);
		} else {
			str = new String(byteArray, charsetName);
		}

		for (final Map.Entry<String, String> entry : keyValueMap.entrySet()) {
			final String key = entry.getKey();
			final String value = entry.getValue();

			if (useRegExp) {
				/* if the key starts with ^ then we have to switch to multiline mode and search (= find) for the key in the string
				 *
				 * If we have a hit then we use the grouping functionality to get the property name at in group(1) and the property value in group2)
				 * and then replace the property line completely */
				if (NullSafe.startsWith(key, '^')) {
					final Pattern pattern = Pattern.compile(key, Pattern.MULTILINE);
					final Matcher matcher = pattern.matcher(str);
					if (matcher.find()) {
						final String propertyName = matcher.group(1);
						final String propertyValue = matcher.group(2);

						logger.debug(String.format("replacing property value %s: %s -> %s", propertyName, propertyValue, value));

						str = String.format("%s%s=%s%s", str.substring(0, matcher.start()), propertyName, value, str.substring(matcher.end()));
					}
				} else {
					str = str.replaceAll(key, value);
				}
			} else {
				int pos;
				String before = "";
				String after = str;
				while (after.indexOf(key) != Numbers.NEGATIVE_ONE) {
					pos = str.indexOf(key);
					if (pos < Numbers.ZERO) {
						logger.warn("File: " + file.getPath());
						logger.warn("Failed to update string '" + key + "'");
						return false; // ignore
					}
					before = str.substring(0, pos);
					after = str.substring(pos + key.length());
					str = before + value + after;
				}
			}
		}

		byte[] bytesToWrite = null;
		if (charsetName == null) {
			bytesToWrite = str.getBytes();
		} else {
			bytesToWrite = str.getBytes(charsetName);
		}

		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			fos.write(bytesToWrite);
		} finally {
			if (fos != null) {
				fos.close();
			}
		}
		return true;
	}

	/**
	 * Replaces the passed <code>properties</code> in the specified <code>file</code> or, if the file it's a directory, in the files of that
	 * directory.
	 */
	public static void replaceProperties(final File file, final Map<String, String> properties, final String includedFilenamesRegex,
			final String excludedFilenamesRegex, final boolean subdirectoriesIncluded) throws IOException {

		if (file.isDirectory()) {
			for (final File currentFile : file.listFiles()) {
				if (currentFile.isDirectory() && !subdirectoriesIncluded) {
					continue;
				}

				replaceProperties(currentFile, properties, includedFilenamesRegex, excludedFilenamesRegex, subdirectoriesIncluded);
			}
		} else {
			if (file.getName().matches(includedFilenamesRegex) && !file.getName().matches(excludedFilenamesRegex)) {
				// Replace properties in file. This also changes the encoding to UTF-8.
				replaceInFile(Not.Null(file.getAbsolutePath()), properties, false, "UTF-8");
			}
		}
	}

	/**
	 * See {@link CommonTools#replaceFileSeparators(String)}.
	 */
	public static String replaceFileSeparators(final String path) {
		return FileTools.replaceFileSeparators(path);
	}

	/**
	 * See {@link CommonTools#replaceFileSeparators(String, String)}.
	 */
	public static String replaceFileSeparators(final String path, final String newFileSeparator) {
		return FileTools.replaceFileSeparators(path, newFileSeparator);
	}

	/**
	 * See {@link CommonTools#getCanonicalPath(String)}.
	 */
	public static String getCanonicalPath(final String path) {
		return FileTools.getCanonicalPath(path);
	}

	/**
	 * See {@link CommonTools#normalizePath(String)}.
	 */
	public static String normalizePath(final String path) {
		return FileTools.normalizePath(path);
	}

	/**
	 * Reads from the specified <code>URL</code> and returns the result as a <code>String</code>.
	 *
	 * @param nullableEncoding
	 *            the encoding. If not set, the method will try to get the encoding from the {@link URLConnection#getContentType() content-type}. If
	 *            that fails, <code>UTF-8</code> is used.
	 *
	 * @throws UncheckedIOException
	 *             if any error occurs while trying to access and read from the <code>url</code>.
	 */
	public static String urlToString(final URL url, final String nullableEncoding) throws UncheckedIOException {

		BufferedReader bufferedReader = null;

		IOException exceptionWhileReading = null;

		try {
			final URLConnection connection = url.openConnection();

			String encoding;
			if (nullableEncoding == null) {
				String contentType = connection.getContentType();
				encoding = contentType != null ? getEncodingFromContentType(contentType) : null;
				if (encoding == null) {
					encoding = "UTF-8";
				}
			} else {
				encoding = nullableEncoding;
			}

			final InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream(), encoding);
			bufferedReader = new BufferedReader(inputStreamReader);

			final StringBuilder stringBuilder = new StringBuilder();
			while (true) {
				final String line = bufferedReader.readLine();
				if (line == null) {
					break;
				}
				stringBuilder.append(line);
			}
			return Not.Null(stringBuilder.toString());
		} catch (final IOException e) {
			exceptionWhileReading = e;
			throw new UncheckedIOException("Error while reading from URL '" + url + "'!", e);
		} finally {
			try {
				if (bufferedReader != null) {
					bufferedReader.close();
				}
			} catch (final IOException exceptionWhileClosingStream) {
				if (exceptionWhileReading == null) {
					throw new UncheckedIOException("Error while closing stream after successfully reading from URL '" + url + "'!",
							exceptionWhileClosingStream);
				} else {
					// ignore
				}
			}
		}
	}

	/**
	 * Gets the charset from a Content-Type string (as returned by {@link URLConnection#getContentType()}), e.g. "text/html; charset=ISO-8859-1").
	 */
	public static String getEncodingFromContentType(final String contentTypeString) {
		final String parameterString = Not.Null(StringTools.getSubstringAfter(contentTypeString, ";"));
		return MapTools.parseKeyValueMap(parameterString, ";", "=", true).get("charset");
	}

	/**
	 * Reads from the passed <code>inputStream</code> and returns the result as a <code>byte</code> array.
	 */
	public static byte[] inputStreamToByteArray(final InputStream inputStream) throws IOException {
		return slurpBytes(inputStream);
	}

	/**
	 * Creates a new {@link URL}.
	 *
	 * @param string
	 *            the string passed to {@link URL#URL(String)}
	 * @return the created <code>URL</code> instance.
	 * @throws UncheckedMalformedURLException
	 *             if {@link URL#URL(String)} throws a {@link MalformedURLException}
	 */
	public static URL newUrl(final String string) throws UncheckedMalformedURLException {
		try {
			return new URL(string);
		} catch (final MalformedURLException e) {
			throw new UncheckedMalformedURLException("Error while creating URL from string '" + string + "'!", e);
		}
	}

	/**
	 * Creates a new <code>File</code> with the specified <code>filePath</code> and returns its <code>URL</code>.
	 */
	public static URL newFileUrl(final String filePath) {
		return FileTools.toURL(new File(filePath));
	}

	/**
	 * Gets the parent path of the specified path. Example: for <code>/path/to/somefile.txt</code> the method returns <code>/path/to</code>).
	 *
	 * @param path
	 *            the path to get the parent from. Should usually also work with URLs.
	 */
	public static String getParent(final String path) {
		String normalizedPath = replaceFileSeparators(path);
		normalizedPath = StringTools.removeTrailingCharacters(normalizedPath, Constants.fileSeparator().charAt(0));
		final int lastFileSeparatorIndex = normalizedPath.lastIndexOf(Constants.fileSeparator());
		return Not.Null(path.substring(0, lastFileSeparatorIndex));
	}

	/**
	 * Just converts the <code>url</code> to a string, invokes {@link #getParent(String)} and returns a new <code>URL</code>.
	 */
	public static URL getParent(final URL url) {
		return newUrl(getParent(url.toString()));
	}

	/**
	 * Quietly closes the passed <code>stream</code> (ignoring <code>null</code>s and exceptions).
	 */
	public static void closeQuietly(final InputStream stream) {
		try {
			if (stream != null) {
				stream.close();
			}
		} catch (final Exception e) {
			// ignore
		}
	}

	/**
	 * Quietly closes the passed <code>stream</code> (ignoring <code>null</code>s and exceptions).
	 */
	public static void closeQuietly(final OutputStream stream) {
		try {
			if (stream != null) {
				stream.close();
			}
		} catch (final IOException e) {
			// ignore
		}
	}

	/**
	 * Reads the content of the <code>inputStream</code> and writes it to the <code>outputStream</code>.
	 *
	 * @throws UncheckedIOException
	 *             if any {@link IOException} occurs.
	 */
	public static void inputToOutput(final InputStream inputStream, final OutputStream outputStream) throws UncheckedIOException {
		try {
			pump(inputStream, outputStream);
		} catch (final IOException e) {
			throw new UncheckedIOException("Error while streaming from input stream to output stream!", e);
		}
	}

	/** Same as {@code inputToFile(inputStream, outputFile, false)}. */
	public static void inputToFile(final InputStream inputStream, final File outputFile) throws IOException {
		inputToFile(inputStream, outputFile, false);
	}

	public static void fileToOutput(final File sourceFile, final OutputStream os) throws IOException {
		try (InputStream in = new BufferedInputStream(new FileInputStream(sourceFile))) {
			pump(in, os);
		}
	}

	/**
	 * Appends given {@link InputStream} to the given output file, possibly at the end of it if append parameter is <tt>true</tt>. If the file does
	 * not exist, but it's parent folder does, a new file will be created automatically.
	 *
	 * @throws FileNotFoundException
	 *             if the parent folder of given output file does not exist
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public static void inputToFile(InputStream in, File outputFile, boolean append) throws IOException {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(outputFile, append);
			inputToOutput(in, fos);
		} finally {
			closeQuietly(fos);
		}
	}

	public static void truncateFile(File outputFile, long newSize) throws IOException {
		try (FileOutputStream fos = new FileOutputStream(outputFile, true)) {
			fos.getChannel().truncate(newSize);
		}
	}

	public static byte[] getCheckSum(final InputStream inputStream, String algorithm) throws IOException {
		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance(algorithm);
		} catch (final NoSuchAlgorithmException e) {
			// should be unreachabele
			throw new AssertionException("Unexpected exception: algorithm '" + algorithm + "' not available?!", e);
		}
		int numRead;
		final int bufferSize = 1024;
		final byte[] buffer = new byte[bufferSize];
		try {
			while (true) {
				numRead = inputStream.read(buffer);

				if (numRead > Numbers.ZERO) {
					digest.update(buffer, 0, numRead);
				} else {
					break;
				}
			}
		} finally {
			if (inputStream != null) {
				inputStream.close();
			}
		}

		return Not.Null(digest.digest());
	}

	/**
	 * Reads from the <code>inputStream</code>stream and returns the MD5 sum using {@link MessageDigest}. Use {@link CommonTools#asString(byte[])} to
	 * convert to a string.
	 */
	public static byte[] getMD5CheckSum(final InputStream inputStream) throws IOException {
		return getCheckSum(inputStream, "MD5");
	}

	/**
	 * Reads from the <code>inputStream</code>stream and returns the SHA1 sum using {@link MessageDigest}. Use {@link CommonTools#asString(byte[])} to
	 * convert to a string.
	 */
	public static byte[] getSHA1CheckSum(final InputStream inputStream) throws IOException {
		return getCheckSum(inputStream, "SHA1");
	}

	/**
	 * Closes the closeable if it's not null. (null argument will be ignored silently). If the invocation of {@link Closeable#close()} throws an
	 * {@link IOException}, it will be wrapped by a {@link java.io.UncheckedIOException}.
	 *
	 * @param closeable
	 *            The closeable to be closed.
	 * @throws java.io.UncheckedIOException
	 *             If the close operations throws an {@link IOException}
	 */
	public static void closeCloseableUnchecked(Closeable closeable) {
		if (closeable == null) {
			return;
		}
		try {
			closeable.close();
		} catch (IOException e) {
			throw new java.io.UncheckedIOException("Error while trying to close " + closeable, e);
		}
	}
	/**
	 * Closes the closeable if it's not null. (null argument will be ignored silently). If the invocation of {@link Closeable#close()} throws an
	 * {@link Exception}, it will be wrapped by a {@link RuntimeException}.
	 *
	 * @param closeable
	 *            The closeable to be closed.
	 * @throws RuntimeException
	 *             If the close operations throws an {@link Exception}
	 */
	public static void closeCloseableUnchecked(AutoCloseable closeable) {
		if (closeable == null) {
			return;
		}
		try {
			closeable.close();
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Error while trying to close " + closeable);
		}
	}

	public static void closeCloseable(Closeable closeable, Logger callerLogger) {
		if (closeable == null) {
			return;
		}
		closeCloseable(closeable, null, callerLogger);
	}

	public static void closeCloseable(AutoCloseable closeable, Logger callerLogger) {
		if (closeable == null) {
			return;
		}
		closeCloseable(closeable, null, callerLogger);
	}

	public static void closeCloseable(Closeable closeable, String resourceId, Logger callerLogger) {
		if (closeable == null) {
			return;
		}
		try {
			closeable.close();
		} catch (Throwable t) {
			String message = "Could not close closeable: '" + (resourceId == null ? closeable : resourceId) + "'";
			if (callerLogger == null) {
				logger.debug(message, t);
			} else {
				callerLogger.debug(message, t);
			}
		}
	}

	public static void closeCloseable(AutoCloseable closeable, String resourceId, Logger callerLogger) {
		if (closeable == null) {
			return;
		}
		try {
			closeable.close();
		} catch (Throwable t) {
			String message = "Could not close closeable: '" + (resourceId == null ? closeable : resourceId) + "'";
			if (callerLogger == null) {
				logger.debug(message, t);
			} else {
				callerLogger.debug(message, t);
			}
		}
	}

	public static void flushOutputStream(OutputStream outputStream, Logger callerLogger) {
		flushOutputStream(outputStream, null, callerLogger);
	}

	public static void flushOutputStream(OutputStream outputStream, String resourceId, Logger callerLogger) {
		if (outputStream == null) {
			return;
		}
		try {
			outputStream.flush();
		} catch (Throwable t) {
			String message = "Could not flush output stream: '" + (resourceId == null ? outputStream : resourceId) + "'";
			if (callerLogger == null) {
				logger.debug(message, t);
			} else {
				callerLogger.debug(message, t);
			}
		}
	}

	/**
	 * Convenience method that internally calls {@link #readFully(InputStream, byte[], int, int)} with offset 0 and the length of the buffer.
	 *
	 * @param in
	 *            The InputStream that should be read from.
	 * @param b
	 *            The buffer that should be filled.
	 * @return The number of bytes read.
	 * @throws IOException
	 *             Thrown in case of an underlying IOException from the InputStream.
	 * @throws NullPointerException
	 *             Thrown if either the InputStream or the buffer is null.
	 */
	public static final int readFully(InputStream in, byte[] b) throws IOException, NullPointerException {
		if (in == null) {
			throw new NullPointerException("The InputStream is null.");
		}
		if (b == null) {
			throw new NullPointerException("The buffer is null.");
		}
		return readFully(in, b, 0, b.length);
	}

	/**
	 * Reads from the provided InputStream into the buffer until the buffer is completely full (or the end of the stream has been reached). The
	 * {@link java.io.InputStream#read(byte[])} method does not guarantee that the buffer is filled completely. This method will <b>try</b>
	 * iteratively to fill the buffer until it is full. This method cannot guarantee that the buffer will be filled completely as the stream might end
	 * before the buffer limit is reached. Use this method with care as it might block until the next bytes can be read from the InputStream. If you
	 * do not have control over the InputStream, you might want to employ some method that includes a timeout.
	 *
	 * @param in
	 *            The InputStream that should be read from.
	 * @param b
	 *            The buffer that should be filled.
	 * @param off
	 *            The offset where the buffer should be filled from.
	 * @param len
	 *            The maximum number of bytes that should be read.
	 * @return The number of bytes read.
	 * @throws IOException
	 *             Thrown in case of an underlying IOException from the InputStream.
	 * @throws NullPointerException
	 *             Thrown if either the InputStream or the buffer is null.
	 */
	public static final int readFully(InputStream in, byte[] b, int off, int len) throws IOException, NullPointerException {
		if (in == null) {
			throw new NullPointerException("The InputStream is null.");
		}
		if (b == null) {
			throw new NullPointerException("The buffer is null.");
		}
		if (len < 0) {
			throw new IndexOutOfBoundsException("The provided length is below 0: " + len);
		}
		if (off < 0) {
			throw new IndexOutOfBoundsException("The provided offset is below 0: " + len);
		}
		if ((off + len) > b.length) {
			throw new IndexOutOfBoundsException(
					"Offset (" + off + ") and len (" + len + ") is larger that the actual buffer size (" + b.length + ")");
		}

		int n = 0;

		while (n < len) {
			int count = in.read(b, off + n, len - n);

			if (count < 0) {
				if (n == 0) {
					return -1;
				} else {
					return n;
				}
			}

			n += count;
		}

		return n;
	}
}
