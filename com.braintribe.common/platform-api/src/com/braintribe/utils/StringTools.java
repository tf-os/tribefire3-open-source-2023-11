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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.xml.sax.InputSource;

import com.braintribe.common.Constants;
import com.braintribe.common.lcd.Numbers;
import com.braintribe.common.lcd.PairNonNull;
import com.braintribe.common.lcd.UnreachableCodeException;
import com.braintribe.logging.Logger;
import com.braintribe.utils.date.NanoClock;
import com.braintribe.utils.lcd.Arguments;
import com.braintribe.utils.lcd.Not;
import com.braintribe.utils.lcd.NullSafe;
import com.braintribe.utils.lcd.string.StringDistance;
import com.braintribe.utils.string.caseconvert.BasicCaseConverter;
import com.braintribe.utils.string.caseconvert.CaseConversionSplitter;

/**
 * This class may contain Java-only (i.e. GWT incompatible) code. For further information please see {@link com.braintribe.utils.lcd.StringTools}.
 *
 * @author michael.lafite
 */
public final class StringTools extends com.braintribe.utils.lcd.StringTools {

	private static Logger logger = Logger.getLogger(StringTools.class);

	private static final Pattern PATTERN_FORMAT_PLACEHOLDER_PATTERN = Pattern.compile("\\{.*?\\}");

	private StringTools() {
		// no instantiation required
	}

	/** Entrance to a fluent API for converting between String cases (e.g. camelCase, kebab-case, snake_case,...). */
	public static CaseConversionSplitter convertCase(String s) {
		return new BasicCaseConverter(s);
	}

	/**
	 * Returns the list of all patterns in a given string. Patterns are identified by a String enclosed in curly brackets. (e.g.,
	 * <code>Hello, my name is {name}</code> contains the pattern variable <code>name</code>)
	 *
	 * @param pattern
	 *            The pattern that may contain variables.
	 * @return A list of all variables. If no variables are provided, an empty list is returned.
	 * @throws NullPointerException
	 *             When the pattern is null.
	 */
	public static List<String> getPatternVariables(final String pattern) {
		final Matcher matcher = PATTERN_FORMAT_PLACEHOLDER_PATTERN.matcher(pattern);

		List<String> result = new ArrayList<>();

		while (matcher.find()) {

			String name = matcher.group();
			name = name.substring(1, name.length() - 1);

			result.add(name);

		}
		return result;
	}

	public static String patternFormat(final String pattern, final Map<String, Object> properties) {
		return patternFormat(pattern, properties, null);
	}

	public static String patternFormat(final String pattern, final Map<String, Object> properties, Object defaultValue) {
		final Matcher matcher = PATTERN_FORMAT_PLACEHOLDER_PATTERN.matcher(pattern);

		final StringBuilder result = new StringBuilder();
		int ofs = 0;
		while (matcher.find()) {
			final int idx = matcher.start();
			result.append(pattern.substring(ofs, idx));

			String name = matcher.group();
			name = name.substring(1, name.length() - 1);

			Object value = properties.get(name);
			if (value == null) {
				if (defaultValue == null) {
					throw new IllegalArgumentException("bad pattern: the property " + name + " was not found. Pattern: " + pattern);
				} else {
					value = defaultValue;
				}
			}

			result.append(value);

			ofs = matcher.end();
		}

		result.append(pattern.substring(ofs));

		return result.toString();
	}

	public static <T> String join(final String delimiter, final T[] array) {
		return join(delimiter, CommonTools.toList(array));
	}

	public static String join(final String delimiter, final byte[] array) {
		return joinArray(delimiter, array);
	}

	public static String join(final String delimiter, final short[] array) {
		return joinArray(delimiter, array);
	}

	public static String join(final String delimiter, final int[] array) {
		return joinArray(delimiter, array);
	}

	public static String join(final String delimiter, final long[] array) {
		return joinArray(delimiter, array);
	}

	public static String join(final String delimiter, final float[] array) {
		return joinArray(delimiter, array);
	}

	public static String join(final String delimiter, final double[] array) {
		return joinArray(delimiter, array);
	}

	public static String join(final String delimiter, final char[] array) {
		return joinArray(delimiter, array);
	}

	public static String join(final String delimiter, final boolean[] array) {
		return joinArray(delimiter, array);
	}

	public static String joinArray(final String delimiter, final Object array) throws IllegalArgumentException {
		final Class<?> clazz = array.getClass();
		if (!clazz.isArray()) {
			throw new IllegalArgumentException("object must be an array");
		}

		return join(delimiter, new ReflectionBasedArrayIterator(array));
	}

	public static String join(final String delimiter, final Iterable<? extends Object> iterable) {
		return join(delimiter, NullSafe.iterator(iterable));
	}

	public static String join(final String delimiter, final Iterator<? extends Object> iterator) {

		final StringBuffer buffer = new StringBuffer();

		while (iterator.hasNext()) {

			if (buffer.length() > Numbers.ZERO) {
				buffer.append(delimiter);
			}

			final Object object = iterator.next();
			if (object == null) {
				continue;
			}

			if (object.getClass().isArray()) {

				/* we cannot use java.util.Arrays.toString((Object[])object) here as the array could be of primitive type; instead we convert the
				 * elements to an array of Object and then get the output using the transformed value */
				final int length = Array.getLength(object);

				final Object[] arr = new Object[length]; // SuppressPMDWarnings (instantiation inside loop is fine here)
				for (int i = 0; i < length; i++) {
					arr[i] = Array.get(object, i);
				}

			} else {
				buffer.append(object.toString());
			}
		}

		return buffer.toString();
	}

	/**
	 * Finds a substring in the <code>string</code> specified by the <code>regex</code> and returns the first and last index of the substring (both
	 * inclusive, staring with <code>0</code>).
	 *
	 * @see StringTools#getSubstringByRegex(String, String)
	 */
	public static PairNonNull<Integer, Integer> findSubstringByRegex(final String string, final String regex) {
		final Pattern pattern = Pattern.compile(regex);
		final Matcher matcher = pattern.matcher(string);
		if (!matcher.find()) {
			return null;
		}
		final PairNonNull<Integer, Integer> result = new PairNonNull<>(matcher.start(), matcher.end() - 1);
		return result;
	}

	/**
	 * Finds a substring in the <code>string</code> specified by the <code>regex</code> and returns the substring. Returns <code>null</code>, if there
	 * is no match.
	 *
	 * @see StringTools#findSubstringByRegex(String, String)
	 */
	public static String getSubstringByRegex(final String string, final String regex) {
		final PairNonNull<Integer, Integer> firstAndLastIndex = findSubstringByRegex(string, regex);
		if (firstAndLastIndex != null) {
			return Not.Null(string.substring(Not.Null(firstAndLastIndex.first()), Not.Null(firstAndLastIndex.second()) + 1));
		}
		return null;
	}

	/**
	 * Returns an <code>InputSource</code> for the passed <code>string</code>.
	 */
	public static InputSource toInputSource(final String string) {
		return new InputSource(new StringReader(string));
	}

	/**
	 * Converts the passed <code>string</code> to a byte array (using UTF-8 encoding) and returns an <code>InputStream</code> for the byte array.
	 */
	public static InputStream toInputStream(final String string) {
		return new ByteArrayInputStream(toByteArray(string));
	}

	/**
	 * Converts the passed <code>string</code> to a byte array (using the specified <code>encoding</code>) and returns an <code>InputStream</code> for
	 * the byte array.
	 */
	public static InputStream toInputStream(final String string, final String encoding) {
		return new ByteArrayInputStream(toByteArray(string, encoding));
	}

	/**
	 * Invokes {@link #fromInputStream(InputStream, String)} with {@link Constants#ENCODING_UTF8_NIO UTF-8} encoding.
	 */
	public static String fromInputStream(final InputStream inputStream) throws UncheckedIOException {
		return fromInputStream(inputStream, Constants.ENCODING_UTF8_NIO);
	}

	/**
	 * Reads all bytes from the passed <code>inputStream</code> and creates a string.
	 */
	public static String fromInputStream(final InputStream inputStream, final String charsetName) throws UncheckedIOException {
		byte[] bytes;
		try {
			bytes = IOTools.inputStreamToByteArray(inputStream);
		} catch (final IOException e) {
			throw new UncheckedIOException("Error while reading from input stream!", e);
		}
		try {
			return new String(bytes, charsetName);
		} catch (final UnsupportedEncodingException e) {
			throw new UncheckedIOException("Error while creating string from bytes! Unsupported encoding: '" + charsetName + "'!", e);
		}
	}

	/**
	 * Converts the passed <code>string</code> into a <code>byte</code> array using UTF-8 encoding.
	 *
	 * @see #toByteArray(String, String)
	 */
	public static byte[] toByteArray(final String string) {
		return toByteArray(string, Constants.ENCODING_UTF8_NIO);
	}

	/**
	 * Converts the passed <code>string</code> into a <code>byte</code> array using the specified encoding.
	 *
	 * @see #toByteArray(String)
	 */
	public static byte[] toByteArray(final String string, final String encoding) {
		try {
			return string.getBytes(encoding);
		} catch (final UnsupportedEncodingException e) {
			throw new IllegalArgumentException("Charset '" + encoding + "' not supported?!", e);
		}
	}

	/**
	 * URL-encodes the passed <code>string</code>.
	 */
	public static String urlEncode(final String string) {
		try {
			return URLEncoder.encode(string, Constants.ENCODING_UTF8_NIO);
		} catch (final UnsupportedEncodingException e) {
			throw new UnreachableCodeException("Encoding " + Constants.ENCODING_UTF8_NIO + " not suppported?!", e);
		}
	}

	/**
	 * URI-encodes the passed <code>string</code>. (Copied from FPSE artifacts.)
	 */
	public static String uriEncode(final String string) {
		try {
			return new URI(null, string, null).toString();
		} catch (final URISyntaxException e) {
			throw new IllegalArgumentException("Cannout URI-encode string '" + string + "'!", e);
		}
	}

	/**
	 * Gets the MD5 sum of the passed <code>string</code>.
	 *
	 * @see IOTools#getMD5CheckSum(InputStream)
	 */
	public static byte[] getMD5CheckSum(final String string, final String encoding) throws IOException {
		Arguments.notNull(string, "No string specified!");
		final InputStream inputStream = toInputStream(string, encoding);
		return IOTools.getMD5CheckSum(inputStream);
	}

	// From: https://programming.guide/worlds-most-copied-so-snippet.html
	public static strictfp String humanReadableByteCount(long bytes, boolean si) {
		int unit = si ? 1000 : 1024;
		long absBytes = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
		if (absBytes < unit) {
			return bytes + " B";
		}
		int exp = (int) (Math.log(absBytes) / Math.log(unit));
		long th = (long) Math.ceil(Math.pow(unit, exp) * (unit - 0.05));
		if (exp < 6 && absBytes >= th - ((th & 0xFFF) == 0xD00 ? 51 : 0)) {
			exp++;
		}
		String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
		if (exp > 4) {
			bytes /= unit;
			exp -= 1;
		}
		return String.format(Locale.US, "%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}

	public static String prettyPrintBytesBinary(long size) {
		return humanReadableByteCount(size, false);
	}

	public static String prettyPrintBytesDecimal(long size) {
		return humanReadableByteCount(size, true);
	}

	public static String prettyPrintBytes(long size) {
		return humanReadableByteCount(size, true) + " / " + humanReadableByteCount(size, false);
	}

	public static String extendString(String text, char fillCharacter, int desiredLength) {
		if (text == null) {
			return null;
		}
		int currentLength = text.length();
		if (currentLength >= desiredLength) {
			return text;
		}
		int count = desiredLength - currentLength;
		StringBuilder sb = new StringBuilder(text);
		for (int i = 0; i < count; i++) {
			sb.append(fillCharacter);
		}
		return sb.toString();
	}

	public static String extendStringInFront(String text, char fillCharacter, int desiredLength) {
		if (text == null) {
			return null;
		}
		int currentLength = text.length();
		if (currentLength >= desiredLength) {
			return text;
		}
		int count = desiredLength - currentLength;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < count; i++) {
			sb.append(fillCharacter);
		}
		sb.append(text);
		return sb.toString();
	}

	/**
	 * Returns a new string based on given camel case like this: <br />
	 * input: "myCamelCaseString" <br />
	 * output: "My Camel Case String" <br />
	 */
	public static String prettifyCamelCase(String name) {
		return prettifyCamelCase(name, true);
	}

	public static String prettifyCamelCase(String name, boolean ensureFirstCharacterCapital) {
		if (name == null) {
			return null;
		}
		if (name.length() == 0) {
			return "";
		}
		String pretty = join(" ", splitCamelCase(name));
		if (ensureFirstCharacterCapital) {
			return capitalize(pretty.substring(0, 1)) + pretty.substring(1);
		} else {
			return pretty;
		}
	}

	public static List<String> readLinesFromInputStream(InputStream inputStream, String charsetName, boolean closeStream) throws IOException {

		List<String> lines = new ArrayList<>();
		BufferedReader bufferedReader = null;

		try {
			if (charsetName == null) {
				bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
			} else {
				bufferedReader = new BufferedReader(new InputStreamReader(inputStream, charsetName));
			}

			while (true) {
				String line = bufferedReader.readLine();
				if (line == null) {
					break;
				}
				lines.add(line);
			}

		} finally {
			if (closeStream) {
				IOTools.closeCloseable(bufferedReader, logger);
			}
		}

		return lines;
	}

	/**
	 * Writes a list of Strings to an output stream using an (optional) encoding. If requested, this method can also close the OutputStream at the
	 * end.
	 *
	 * @param outputStream
	 *            The OutputStream that the Strings should be written to.
	 * @param output
	 *            The list of Strings that should be written to the OutputStream.
	 * @param charsetName
	 *            The encoding that should be used. If this is null, the system's default encoding will be used (discouraged).
	 * @param closeStream
	 *            Indicates whether the OutputStream should be closed at the end.
	 * @throws IOException
	 *             Thrown when an IOException occurs while writing to the OutputStream.
	 * @throws IllegalArgumentException
	 *             Thrown when the outputStream is null.
	 * @throws UnsupportedEncodingException
	 *             Thrown when the provided charset name is not supported.
	 */
	public static void writeLinesToOutputStream(OutputStream outputStream, List<String> output, String charsetName, boolean closeStream)
			throws IOException, IllegalArgumentException, UnsupportedEncodingException {

		BufferedWriter bufferedWriter = null;

		if (outputStream == null) {
			throw new IllegalArgumentException("The output stream must not be null.");
		}

		try {
			if (output == null || output.isEmpty()) {
				return;
			}

			if (charsetName == null) {
				bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
			} else {
				bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, charsetName));
			}

			for (String line : output) {
				bufferedWriter.write(line);
				bufferedWriter.newLine();
			}

			bufferedWriter.flush();

		} finally {
			if (closeStream) {
				if (bufferedWriter != null) {
					IOTools.closeCloseable(bufferedWriter, logger);
				} else {
					IOTools.closeCloseable(outputStream, logger);
				}
			}
		}

	}

	/** Just like {@link #asciiBoxMessage(String, int)}, but with no maxLength limitation. */
	public static String asciiBoxMessage(Object text) {
		return asciiBoxMessage("" + text, 0);
	}

	/**
	 * Takes the provided text (which may have multiple lines) and returns this text enclosed in an ASCII box. Example: <br>
	 * Input: Hello, world! <br>
	 * Output: <br>
	 * /---------------\ | Hello, world! | \---------------/
	 *
	 * This allows to print/log information in a way that makes it easier to identify this information among other output.
	 *
	 * If the text is longer than the provided maxLength, it will be split into several lines.
	 *
	 * @param text
	 *            The text that should be boxed. When this text is null or empty, an empty String will be returned.
	 * @param maxLength
	 *            The maximum length of the box. This value will be ignored if zero or negative.
	 * @return The text in a box.
	 */
	public static String asciiBoxMessage(String text, int maxLength) {
		if (text == null || text.trim().length() == 0) {
			return "";
		}
		String[] lines = text.split("\\r?\\n");
		for (int i = 0; i < lines.length; ++i) {
			lines[i] = lines[i].replaceAll("\\s+$", "").replaceAll("\t", "    ");
		}

		// Search for the maximum line length
		int length = 0;
		for (String line : lines) {
			int lineLength = line.length();
			length = Math.max(length, lineLength);
		}
		if (length > maxLength && maxLength > 0) {
			length = maxLength;
		}

		StringBuilder sb = new StringBuilder();
		sb.append('/');
		for (int i = 0; i <= length + 1; ++i) {
			sb.append('-');
		}
		sb.append("\\\n");

		for (String line : lines) {

			while (line.length() > 0) {

				sb.append("| ");

				int chunklen = Math.min(line.length(), length);
				String chunk = line.substring(0, chunklen);
				line = line.substring(chunklen);

				sb.append(chunk);

				for (int i = 0; i < (length - chunklen); ++i) {
					sb.append(' ');
				}

				sb.append(" |\n");

			}
		}

		sb.append('\\');
		for (int i = 0; i <= length + 1; ++i) {
			sb.append('-');
		}
		sb.append("/\n");
		return sb.toString();
	}

	/**
	 * Returns a String containing the hex representation of the provided byte array. Example: <br>
	 * Input: a <br>
	 * Output: 61 <br>
	 * <br>
	 * Input: Hello, world! <br>
	 * Output: 48656c6c6f2c20776f726c6421 <br>
	 * As an alternative, a Base64 encoding/decoding could be used.
	 *
	 * @param data
	 *            The byte array that should be encoded.
	 * @return A String containing the hex values of all bytes.
	 * @throws IllegalArgumentException
	 *             Thrown when the provided data array is null.
	 */
	public static String toHex(byte[] data) {
		if (data == null) {
			throw new IllegalArgumentException("The data array must not be null");
		}
		StringBuilder buf = new StringBuilder();
		for (byte element : data) {
			int halfbyte = (element >>> 4) & 0x0F;
			int two_halfs = 0;
			do {
				if ((0 <= halfbyte) && (halfbyte <= 9)) {
					buf.append((char) ('0' + halfbyte));
				} else {
					buf.append((char) ('a' + (halfbyte - 10)));
				}
				halfbyte = element & 0x0F;
			} while (two_halfs++ < 1);
		}
		return buf.toString();
	}

	/**
	 * Decodes a String returned by {@link #toHex(byte[])}.
	 *
	 * @param s
	 *            The String containing a hex representation of a byte array.
	 * @return The decoded byte array.
	 * @throws IllegalArgumentException
	 *             Thrown when the provided String is null.
	 */
	public static byte[] fromHex(String s) {
		if (s == null) {
			throw new IllegalArgumentException("The String for hex decoding must not be null");
		}
		int l = s.length();
		byte[] data = new byte[l / 2];
		for (int i = 0; i < l; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

	/**
	 * Checks whether the provided value matches the include/exclude patterns. If no patterns are provided, this method will return true. When the
	 * value itself is null, this method returns false.
	 *
	 * @param includes
	 *            A collection of regular expressions
	 * @param excludes
	 *            A collection of regular expressions
	 * @param value
	 *            True, if the value is included (or includes is empty) and not excluded from the provided patterns.
	 */
	public static boolean matches(Collection<String> includes, Collection<String> excludes, String value) {
		if (value == null) {
			return false;
		}
		List<String> result = getMatches(includes, excludes, value);
		if (result == null || result.size() != 1) {
			return false;
		}
		return true;
	}

	/**
	 * Returns a list of those values that match any of the regular expressions in the includes parameter and none of the regular expressions in the
	 * excludes parameter. If any of the two collection parameters is either null or empty, it will not be takes into consideration. So, when both are
	 * null, all values will be returned. If the includes pattern is null, all values will be regarded as included. This method will always return a
	 * non-null List.
	 *
	 * @param includes
	 *            A collection of regular expressions
	 * @param excludes
	 *            A collection of regular expressions
	 * @param values
	 *            A list of values that should be matched against the regular expressions
	 * @return A list of values that fit the include/exclude patterns
	 */
	public static List<String> getMatches(Collection<String> includes, Collection<String> excludes, String... values) {

		List<String> result = new ArrayList<>();
		if (values == null || values.length == 0) {
			return result;
		}

		List<Pattern> includePatterns = createPatterns(includes);
		List<Pattern> excludePatterns = createPatterns(excludes);

		for (String value : values) {

			boolean included = true;
			if (includePatterns != null && includePatterns.size() > 0) {
				included = false;
				for (Pattern include : includePatterns) {
					Matcher matcher = include.matcher(value);
					if (matcher.matches()) {
						included = true;
						break;
					}
				}
			}
			if (included) {
				boolean excluded = false;
				if (excludePatterns != null && excludePatterns.size() > 0) {
					for (Pattern exclude : excludePatterns) {
						Matcher matcher = exclude.matcher(value);
						if (matcher.matches()) {
							excluded = true;
							break;
						}
					}
				}

				if (!excluded) {
					result.add(value);
				}
			}
		}

		return result;
	}

	/**
	 * Creates a list of Pattern object out of the regular expresssion Strings provided in the parameter.
	 *
	 * @param patternStrings
	 *            A collection of regular expressions in String objects.
	 * @return A list of pattern objects.
	 * @throws PatternSyntaxException
	 *             Thrown when a regular expression is not valid.
	 */
	public static List<Pattern> createPatterns(Collection<String> patternStrings) throws PatternSyntaxException {
		if (patternStrings == null) {
			return null;
		}
		List<Pattern> patterns = new ArrayList<>();
		for (String patternString : patternStrings) {
			patterns.add(Pattern.compile(patternString));
		}
		return patterns;
	}

	/**
	 * Removes all invalid characters from a String. It removes, for example, all hex codes below 20, but keeps tabs and new line characters.
	 *
	 * @param input
	 *            The String that should be sanitized
	 * @return The sanitized String
	 */
	public static String removeUndisplayableCharacters(String input) {
		if (input == null) {
			return null;
		}
		return input.replaceAll("[^\\P{Cc}\\t\\r\\n]", "");
	}

	/**
	 * Returns the (first) {@link Constants#LINE_SEPARATORS line separator} in the passed <code>string</code> or <code>null</code>, if it's a single
	 * line string.
	 */
	public static String getFirstLineSeparator(String string) {
		Arguments.notNullWithNames("string", string);

		for (int i = 0; i < string.length(); i++) {
			for (String lineSeparator : Constants.LINE_SEPARATORS) {
				if (string.startsWith(lineSeparator, i)) {
					return lineSeparator;
				}
			}
		}
		return null;
	}

	/**
	 * Normalizes the passed (text file) string with respect to line separators, i.e. converts line separators, if needed, so that all are the same
	 * (e.g. UNIX vs Windows) and adds a line separator at the end, if it's missing.
	 *
	 * @param string
	 *            the string to normalize.
	 * @param lineSeparator
	 *            the line separator to use in the string. This setting is optional. If not set, the method determines the line separator to use by
	 *            searching the string for the first line separator. If it's a single line string, the UNIX line separator will be used to ensure
	 *            reproducible results in all environments.
	 */
	public static String normalizeLineSeparatorsInTextFileString(String string, String lineSeparator) {
		if (lineSeparator == null) {
			// first line separator wins
			lineSeparator = StringTools.getFirstLineSeparator(string);
			if (lineSeparator == null) {
				// single line string -> assume linux line separator
				lineSeparator = "\n";
			}
		}

		// ensure line separators not mixed
		string = StringTools.normalizeLineSeparators(string, lineSeparator);
		// ensure line separator at the end
		if (!string.endsWith(lineSeparator)) {
			string += lineSeparator;
		}

		return string;
	}

	/**
	 * Limits the maximum consecutive occurrences of <code>searchString</code> in <code>string</code>.
	 *
	 * @throws IllegalArgumentException
	 *             if <code>string</code> or <code>searchString</code> are <code>null</code> or if <code>maxConsecutiveOccurrence</code> is negative.
	 */
	public static String limitConsecutiveOccurrences(String string, String searchedString, int maxConsecutiveOccurrence)
			throws IllegalArgumentException {
		Arguments.notNullWithNames("string", string, "searchedString", searchedString);
		if (maxConsecutiveOccurrence < 0) {
			throw new IllegalArgumentException("Argument maxConsecutiveOccurrence (" + maxConsecutiveOccurrence + ") must not be negative!");
		}

		String result;
		if (!string.contains(searchedString)) {
			// since the searched string is not contained, we can just return the original string
			result = string;
		}
		if (maxConsecutiveOccurrence == 0) {
			// since 0 occurrences are allowed, we can just remove all occurrences
			result = string.replace(searchedString, "");
		} else {
			// walk through the string, count consecutive occurrences and remove them (if too many)
			int lastCheckedIndex = 0;

			StringBuilder builder = new StringBuilder();
			while (true) {
				int index = string.indexOf(searchedString, lastCheckedIndex);

				if (index == -1) {
					// no more occurrences --> copy rest of string and break
					builder.append(string.substring(lastCheckedIndex));
					break;
				}

				builder.append(string.substring(lastCheckedIndex, index));

				int consecutiveOccurrencesCount = 0;
				while (string.startsWith(searchedString, index)) {
					index = index + searchedString.length();
					if (consecutiveOccurrencesCount < maxConsecutiveOccurrence) {
						consecutiveOccurrencesCount++;
						builder.append(searchedString);
					}
				}
				lastCheckedIndex = index;
			}

			result = builder.toString();
		}
		return result;
	}

	/**
	 * Returns a string where leading whitespace (if any) has been removed from the passed <code>string</code> (or <code>null</code>, if the passed
	 * <code>string</code> is <code>null</code>).
	 */
	public static String removeLeadingWhitespace(String string) {
		String leadingWhitespaceRemoved = null;
		if (string != null) {
			leadingWhitespaceRemoved = string.replaceAll("^\\s+", "");
		}
		return leadingWhitespaceRemoved;
	}

	/**
	 * Returns a string where trailing whitespace (if any) has been removed from the passed <code>string</code> (or <code>null</code>, if the passed
	 * <code>string</code> is <code>null</code>).
	 */
	public static String removeTrailingWhitespace(String string) {
		String trailingWhitespaceRemoved = null;
		if (string != null) {
			trailingWhitespaceRemoved = string.replaceAll("\\s+$", "");
		}
		return trailingWhitespaceRemoved;
	}

	/**
	 * Splits a comma-separated text into separate token. Quoted texts may contains commas.
	 *
	 * @param text
	 *            The text that should be broken into parts.
	 * @param trim
	 *            If true, the resulting parts will also be trimmed.
	 * @return An array of the indiviual parts of the text. If the text is null, null will be returned. If the text is empty, an empty array will be
	 *         returned. Quoted Strings will keep the quotes.
	 */
	public static String[] splitCommaSeparatedString(String text, boolean trim) {
		return splitCharSeparatedString(text, ',', '\"', trim);
	}
	/**
	 * Splits a semicolon-separated text into separate token. Quoted texts may contains semicolons.
	 *
	 * @param text
	 *            The text that should be broken into parts.
	 * @param trim
	 *            If true, the resulting parts will also be trimmed.
	 * @return An array of the indiviual parts of the text. If the text is null, null will be returned. If the text is empty, an empty array will be
	 *         returned. Quoted Strings will keep the quotes.
	 */
	public static String[] splitSemicolonSeparatedString(String text, boolean trim) {
		return splitCharSeparatedString(text, ';', '\"', trim);
	}

	/**
	 * Splits a comma-separated text into separate token. Quoted texts may contains commas.
	 *
	 * @param text
	 *            The text that should be broken into parts.
	 * @param splitChar
	 *            The character that should be between the parts (usually a comma or semicolon)
	 * @param quoteChar
	 *            The character that is used to quote parts (e.g., \" or ').
	 * @param trim
	 *            If true, the resulting parts will also be trimmed.
	 * @return An array of the indiviual parts of the text. If the text is null, null will be returned. If the text is empty, an empty array will be
	 *         returned. Quoted Strings will keep the quotes.
	 */
	public static String[] splitCharSeparatedString(String text, char splitChar, char quoteChar, boolean trim) {
		if (text == null) {
			return null;
		}
		if (text.length() == 0) {
			return new String[0];
		}
		// see
		// http://stackoverflow.com/questions/1757065/java-splitting-a-comma-separated-string-but-ignoring-commas-in-quotes

		// A bit friendlier for the eyes:
		// String otherThanQuote = " [^"+quoteChar+"] ";
		// String quotedString = String.format(" \" %s* \" ", otherThanQuote);
		// String regex = String.format("(?x) "+ // enable comments, ignore white spaces
		// splitChar+" "+ // match a comma
		// "(?= "+ // start positive look ahead
		// " (?: "+ // start non-capturing group 1
		// " %s* "+ // match 'otherThanQuote' zero or more times
		// " %s "+ // match 'quotedString'
		// " )* "+ // end group 1 and repeat it zero or more times
		// " %s* "+ // match 'otherThanQuote'
		// " $ "+ // match the end of the string
		// ") ", // stop positive look ahead
		// otherThanQuote, quotedString, otherThanQuote);
		//
		// String[] tokens = text.split(regex, -1);

		String[] parts = text
				.split(splitChar + "(?=(?:[^" + quoteChar + "]*" + quoteChar + "[^" + quoteChar + "]*" + quoteChar + ")*[^" + quoteChar + "]*$)", -1); // Quoted
		// text
		// may
		// contain
		// semicolons
		// as
		// well
		// String[] parts = text.split(splitChar+"(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1); //Quoted text may contain
		// semicolons as well
		if (parts != null && trim) {
			String[] trimmedParts = new String[parts.length];
			for (int i = 0; i < parts.length; ++i) {
				trimmedParts[i] = (parts[i] == null ? null : parts[i].trim());
			}
			parts = trimmedParts;
		}
		return parts;
	}

	/**
	 * Returns the memory size specified by the provided String. Hence, 1 kb will be transformed to 1024L.
	 *
	 * @param sizeString
	 *            The String specifying the size
	 * @throws IllegalArgumentException
	 *             Thrown if the provided String is not parseable or negative.
	 * @throws NullPointerException
	 *             Thrown if the provided String is null.
	 * @return The long value specified by the String.
	 */
	public static long parseBytesString(String sizeString) {
		if (sizeString == null) {
			throw new NullPointerException("The sizeString must not be null.");
		}
		sizeString = sizeString.trim();
		if (sizeString.length() == 0) {
			throw new IllegalArgumentException("The sizeString must not be empty.");
		}
		if (sizeString.startsWith("-")) {
			throw new IllegalArgumentException("The sizeString must not be negative.");
		}
		sizeString = sizeString.replaceAll(" ", "");
		long returnValue = -1;
		Pattern patt = Pattern.compile("([\\d.]+)([EPBTGMK]B)", Pattern.CASE_INSENSITIVE);
		Matcher matcher = patt.matcher(sizeString);
		Map<String, Integer> powerMap = new HashMap<>();
		powerMap.put("EB", 6);
		powerMap.put("PB", 5);
		powerMap.put("TB", 4);
		powerMap.put("GB", 3);
		powerMap.put("MB", 2);
		powerMap.put("KB", 1);
		if (matcher.find()) {
			String number = matcher.group(1);
			int pow = powerMap.get(matcher.group(2).toUpperCase());
			BigDecimal bytes = new BigDecimal(number);
			bytes = bytes.multiply(BigDecimal.valueOf(1024).pow(pow));
			returnValue = bytes.longValue();
		} else {
			try {
				returnValue = Long.parseLong(sizeString);
			} catch (NumberFormatException nfe) {
				throw new IllegalArgumentException("Could not parse String " + sizeString, nfe);
			}
		}
		return returnValue;
	}

	/**
	 * Encodes a Map<String,String> into a single String. The result can be decoded with {@link #decodeStringMapFromString(String)} Entries that have
	 * either a key or a value with null will be excluded.
	 *
	 * @param map
	 *            The map that should be encoded to a String.
	 * @return The encoded String. If the map is empty, the resulting String is also empty.
	 * @throws IllegalArgumentException
	 *             Thrown when the provided map is empty.
	 * @throws UnsupportedOperationException
	 *             Thrown in the unlikely event that the key or the value could not be URL encoded.
	 */
	public static String encodeStringMapToString(Map<String, String> map) throws IllegalArgumentException, UnsupportedOperationException {
		if (map == null) {
			throw new IllegalArgumentException("The map must not be null.");
		}
		StringBuilder sb = new StringBuilder();
		try {
			final Iterator<Map.Entry<String, String>> it = map.entrySet().iterator();
			while (it.hasNext()) {
				final Map.Entry<String, String> entry = it.next();
				String key = entry.getKey();
				String value = entry.getValue();
				if (key != null && value != null) {
					sb.append(URLEncoder.encode(key, "UTF-8"));
					sb.append("=");
					sb.append(URLEncoder.encode(value, "UTF-8"));
					if (it.hasNext()) {
						sb.append("&");
					}
				}
			}
			return sb.toString();
		} catch (Exception e) {
			throw new UnsupportedOperationException(e);
		}
	}

	/**
	 * Decodes a String to a Map<String,String> object. The String provided should have been encoded by {@link #encodeStringMapToString(Map)}.
	 *
	 * @param encodedStringMap
	 *            The encoded String.
	 * @return The Map that was encoded in the String.
	 * @throws IllegalArgumentException
	 *             Thrown if the String is either null or not parseable.
	 */
	public static Map<String, String> decodeStringMapFromString(String encodedStringMap) throws IllegalArgumentException {
		if (encodedStringMap == null) {
			throw new IllegalArgumentException("The encoded map String must not be null.");
		}
		Map<String, String> result = new LinkedHashMap<>();
		encodedStringMap = encodedStringMap.trim();
		if (encodedStringMap.length() > 0) {
			try {
				String[] parts = encodedStringMap.split("\\&");
				for (String part : parts) {
					try {
						String[] keyValue = part.split("=");
						if (keyValue.length == 0) {
							result.put("", "");
						} else {
							String encodedKey = null;
							String encodedValue = null;
							if (keyValue.length == 1) {
								if (part.endsWith("=")) {
									encodedKey = keyValue[0];
									encodedValue = "";
								} else {
									encodedKey = "";
									encodedValue = keyValue[0];
								}
							} else {
								encodedKey = keyValue[0];
								encodedValue = keyValue[1];
							}
							String key = URLDecoder.decode(encodedKey, "UTF-8");
							String value = URLDecoder.decode(encodedValue, "UTF-8");
							result.put(key, value);
						}
					} catch (Exception e) {
						throw new IllegalArgumentException("Could not parse part " + part, e);
					}
				}
			} catch (Exception e) {
				throw new IllegalArgumentException("Could not parse " + encodedStringMap, e);
			}
		}
		return result;
	}

	/**
	 * Convenience method that directly calls {@link #prettyPrintDuration(Duration, boolean, ChronoUnit)} with the duration specified in milliseconds.
	 *
	 * @param durationInMs
	 *            The duration in milliseconds.
	 * @param includeUnits
	 *            Indicates whether units should be included in the result.
	 * @param stopAt
	 *            The lowest time unit that should be included. If it is set to nanoseconds, it will be changed to milliseconds (as the duration is in
	 *            ms).
	 * @return A human readable String representing the Duration.
	 * @throws IllegalArgumentException
	 *             When start is null.
	 */
	public static String prettyPrintDuration(long durationInMs, boolean includeUnits, ChronoUnit stopAt) throws IllegalArgumentException {
		if (stopAt == null || stopAt == ChronoUnit.NANOS) {
			stopAt = ChronoUnit.MILLIS;
		}
		return prettyPrintDuration(Duration.ofMillis(durationInMs), includeUnits, stopAt);
	}
	/**
	 * Convenience method that directly calls {@link #prettyPrintDuration(Duration, boolean, ChronoUnit)} with the duration between the provided start
	 * instant and now.
	 *
	 * @param start
	 *            The start instant.
	 * @param includeUnits
	 *            Indicates whether units should be included in the result.
	 * @param stopAt
	 *            The lowest time unit that should be included.
	 * @return A human readable String representing the Duration.
	 * @throws IllegalArgumentException
	 *             When start is null.
	 */
	public static String prettyPrintDuration(Instant start, boolean includeUnits, ChronoUnit stopAt) throws IllegalArgumentException {
		if (start == null) {
			throw new IllegalArgumentException("The start instant must not be null.");
		}
		Instant end = NanoClock.INSTANCE.instant();
		return prettyPrintDuration(Duration.between(start, end), includeUnits, stopAt);
	}
	/**
	 * Convenience method that directly calls {@link #prettyPrintDuration(Duration, boolean, ChronoUnit)} with the duration between the start/end
	 * instants.
	 *
	 * @param start
	 *            The start instant.
	 * @param end
	 *            The end instant.
	 * @param includeUnits
	 *            Indicates whether units should be included in the result.
	 * @param stopAt
	 *            The lowest time unit that should be included.
	 * @return A human readable String representing the Duration.
	 * @throws IllegalArgumentException
	 *             When start and/or end are null.
	 */
	public static String prettyPrintDuration(Instant start, Instant end, boolean includeUnits, ChronoUnit stopAt) throws IllegalArgumentException {
		if (start == null || end == null) {
			throw new IllegalArgumentException("Both start (" + start + ") and end (" + end + ") instants must not be null.");
		}
		return prettyPrintDuration(Duration.between(start, end), includeUnits, stopAt);
	}

	/**
	 * Returns the provided duration in a more human-readable form. See {@link #prettyPrintDuration(Duration, boolean, ChronoUnit)} for more
	 * information. The additional value of this method is that it takes a double value, which means, that it can also handle sub-1 ms time spans
	 * (i.e., nano-seconds). Values between 0.01 and 1 will still be shown as ms, everything below that will be displayed in nanoseconds.
	 *
	 * If the value is higher than the equivalent of 1 seconds, it will not use nano precision anymore but only takes the full millisecond number into
	 * account.
	 *
	 * @param milliseconds
	 *            The duration (in milliseconds) that should be printed
	 * @param includeUnits
	 *            Whether time units should be included or not.
	 * @param stopAt
	 *            The lowest time unit that should be included.
	 * @return A human readable String representing the Duration.
	 */
	public static String prettyPrintMilliseconds(double milliseconds, boolean includeUnits, ChronoUnit stopAt) {
		if (milliseconds <= 0) {
			if (stopAt == null) {
				stopAt = ChronoUnit.MILLIS;
			}
			return getZeroDurationAsString(includeUnits, stopAt);
		}
		if (stopAt == null) {
			stopAt = ChronoUnit.NANOS;
		}
		Duration duration = null;
		if (milliseconds >= Numbers.MILLISECONDS_PER_SECOND) {
			// No need to go to nano-precision
			double roundedMilliseconds = MathTools.roundToDecimals(milliseconds, 0);
			duration = Duration.ofMillis((long) roundedMilliseconds);
		} else {
			if (milliseconds < 0.01d) {
				double nanoSeconds = milliseconds * Numbers.NANOSECONDS_PER_MILLISECOND;
				duration = Duration.ofNanos((long) nanoSeconds);
			} else {
				double roundedMilliseconds = MathTools.roundDownToDecimals(milliseconds, 2);
				if (Math.floor(roundedMilliseconds) == roundedMilliseconds) {
					return "" + ((long) roundedMilliseconds) + " ms";
				} else {
					return "" + roundedMilliseconds + " ms";
				}
			}
		}
		return prettyPrintDuration(duration, includeUnits, stopAt);
	}
	/**
	 * Returns the provided duration in a more human-readable form. If a duration of 0 (zero) is provided, the return String will be "0". Depending on
	 * the parameter includeUnits, the result will either be in the form <code>dd:hh:mm:ss:SSS.ns</code> (false) or
	 * <code>x d, x h, x min, x s, x ms, x ns</code>. In either case, leading 0 values will not be returned. Hence, when the duration is 1 ms, the
	 * result will either be <code>1:000000</code> (includeUnits is false) or <code>1 ms</code> (includeUnits is true). When units should be included,
	 * 0 values will be omitted at all.
	 *
	 * The optional stopAt parameter can be used to specify the lowest time unit that should be included.
	 *
	 * As an alternative, you can use the {@link Duration#toString()} method to get a ISO-8601 representation of the duration (which is a bit harder
	 * to read).
	 *
	 * @param duration
	 *            The duration that should be printed.
	 * @param includeUnits
	 *            Whether time units should be included or not.
	 * @param stopAt
	 *            The lowest time unit that should be included.
	 * @return A human readable String representing the Duration.
	 */
	public static String prettyPrintDuration(Duration duration, boolean includeUnits, ChronoUnit stopAt) {
		if (stopAt == null) {
			stopAt = ChronoUnit.NANOS;
		}
		try {
			// The last part of this if might be a bit odd, but it prevents toNanos from producing an
			// ArithmeticException
			if (duration == null || duration == Duration.ZERO || (duration.toDays() <= 0 && duration.toNanos() == 0L)) {
				return getZeroDurationAsString(includeUnits, stopAt);
			}
		} catch (ArithmeticException ae) {
			// Thrown by toNanos when the duration cannot be expressed as nanoseconds in a long
			// We can ignore it as it means that the duration is definitely not 0.
		}

		long days = duration.toDays();
		if (days > 0) {
			duration = duration.minusDays(days);
		}

		StringBuilder sb = new StringBuilder();
		appendIfGreaterThanZero(sb, days, includeUnits ? "d" : null, 2);
		if (stopAt != ChronoUnit.DAYS) {
			long hours = duration.toHours();
			if (hours > 0) {
				duration = duration.minusHours(hours);
			}
			appendIfGreaterThanZero(sb, hours, includeUnits ? "h" : null, 2);
			if (stopAt != ChronoUnit.HOURS) {
				long minutes = duration.toMinutes();
				if (minutes > 0) {
					duration = duration.minusMinutes(minutes);
				}
				appendIfGreaterThanZero(sb, minutes, includeUnits ? "min" : null, 2);
				if (stopAt != ChronoUnit.MINUTES) {
					long millis = duration.toMillis();
					long seconds = 0;
					if (millis > 0) {
						seconds = millis / Numbers.MILLISECONDS_PER_SECOND;
						if (seconds > 0) {
							duration = duration.minusSeconds(seconds);
							millis = millis - (seconds * Numbers.MILLISECONDS_PER_SECOND);
						}
					}
					appendIfGreaterThanZero(sb, seconds, includeUnits ? "s" : null, 2);
					if (stopAt != ChronoUnit.SECONDS) {
						if (millis > 0) {
							duration = duration.minusMillis(millis);
						}
						appendIfGreaterThanZero(sb, millis, includeUnits ? "ms" : null, 3);
						if (stopAt != ChronoUnit.MILLIS) {
							long nanos = duration.toNanos();
							appendIfGreaterThanZero(sb, nanos, includeUnits ? "ns" : null, 6);
						}
					}
				}
			}
		}
		if (sb.length() == 0) {
			return getZeroDurationAsString(includeUnits, stopAt);
		} else {
			return sb.toString();
		}
	}

	private static String getZeroDurationAsString(boolean includeUnits, ChronoUnit stopAt) {
		if (includeUnits) {
			switch (stopAt) {
				case DAYS:
					return "0 d";
				case HOURS:
					return "0 h";
				case MINUTES:
					return "0 min";
				case SECONDS:
					return "0 s";
				case MILLIS:
					return "0 ms";
				case NANOS:
					return "0 ns";
				default:
					return "0 " + stopAt.name();
			}
		} else {
			return "0";
		}
	}

	private static void appendIfGreaterThanZero(StringBuilder sb, long number, String unit, int expectedLength) {
		if (number > 0) {
			String numberString = "" + number;

			if (sb.length() > 0) {
				if (unit != null) {
					sb.append(' ');
				} else {
					while (numberString.length() < expectedLength) {
						numberString = "0".concat(numberString);
					}
					sb.append(':');
				}
			}
			sb.append(numberString);
			if (unit != null) {
				sb.append(' ');
				sb.append(unit);
			}
		} else {
			if (unit == null) {
				if (sb.length() > 0) {
					sb.append(':');
					for (int i = 0; i < expectedLength; ++i) {
						sb.append('0');
					}
				}
			}
		}
	}

	/**
	 * Splits a String into separate chunks, each having a maximum size as defined in the <code>size</code> parameter. The result is a collection of
	 * the individual chunks.
	 *
	 * @param str
	 *            The String that should be split.
	 * @param size
	 *            The maximum length of the chunks.
	 * @return A {@link Collection} that contains the chunks. If the String was null or empty, an empty Collection is returned.
	 * @throws IllegalArgumentException
	 *             Thrown when the size is negative or zero (which is not thrown when str is null or empty).
	 */
	public static Collection<String> splitStringBySize(String str, int size) throws IllegalArgumentException {
		ArrayList<String> split = new ArrayList<>();
		if (!isBlank(str)) {
			if (size <= 0) {
				throw new IllegalArgumentException("The size argument must not be negative or zero. (" + size + ")");
			}
			int totalLength = str.length();
			for (int i = 0; i <= totalLength / size; i++) {
				int startIndex = i * size;
				int stopIndex = Math.min((i + 1) * size, totalLength);
				if (startIndex < totalLength) {
					split.add(str.substring(startIndex, stopIndex));
				}
			}
		}
		return split;
	}

	/**
	 * This method removes diacritical marks (e.g., accents) from a String. It cannot be guaranteed that for all characters a latin equivalent can be
	 * found (e.g., Chinese). This method provides a best-effort that can be used, for example, to clean filenames of special characters before
	 * removing the remaining invalid characters.
	 *
	 * @param str
	 *            The input String that might contain umlauts, accents, etc.
	 * @return The input String with all special characters replaced by their latin equivalent.
	 */
	public static String removeDiacritics(String str) {
		if (str == null || str.trim().length() == 0) {
			return str;
		}
		String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD); // \\p{IsM}
		Pattern pattern = null;
		try {
			pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
		} catch (PatternSyntaxException pse) {
			pattern = Pattern.compile("\\p{IsM}+");
		}
		return pattern.matcher(nfdNormalizedString).replaceAll("");
	}

	/**
	 * Removes non-printable characters of a String. This could be useful for logging Strings that might have special characters or have been created
	 * from a byte[] of unknown source.
	 *
	 * @param text
	 *            The text that should be cleaned.
	 * @return The cleaned text, or null if the input was null.
	 */
	public static String removeNonPrintableCharacters(String text) {
		if (text == null) {
			return null;
		}
		if (text.trim().length() == 0) {
			return text;
		}

		// strips off all non-ASCII characters
		text = text.replaceAll("[^\\x00-\\x7F]", "");

		// erases all the ASCII control characters
		text = text.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");

		// removes non-printable characters from Unicode
		text = text.replaceAll("\\p{C}", "");

		return text.trim();
	}

	/** @see StringDistance#levenshteinDistance(String, String) */
	public static int levenshteinDistance(String s, String t) {
		return StringDistance.levenshteinDistance(s, t);
	}

	/**
	 * Converts a CamelCase String into dash-separated-string (converting all to lower case). There is a special case when the Input String contains
	 * dots ('.'). In that case, the dot remains and no additional dash will be inserted between.
	 *
	 * @param camelCaseString
	 *            The input string.
	 * @return The dashed String, or the original input String if it is empty or null.
	 */
	public static String camelCaseToDashSeparated(String camelCaseString) {
		if (isBlank(camelCaseString)) {
			return camelCaseString;
		}
		StringBuilder sb = new StringBuilder();

		String[] split = camelCaseString.split("\\.");
		for (int i = 0; i < split.length; ++i) {
			if (i > 0) {
				sb.append('.');
			}
			List<String> list = splitCamelCase(split[i]);
			if (list == null || list.isEmpty()) {
				return camelCaseString.toLowerCase();
			}
			String joined = join("-", list);
			sb.append(joined.toLowerCase());
		}
		return sb.toString();
	}

	/**
	 * Special substring version that does not take the number of characters but rather the number of UTF-8 bytes into account. Special care is taken
	 * that a multi-byte UTF-8 character is not split in half at the end.
	 *
	 * @param input
	 *            The Input String that should be split (if it's byte length exceeds maxBytesLength).
	 * @param maxBytesLength
	 *            The maximum byte-length of the resulting String.
	 * @return The input string, truncated to the max length provided by the maxBytesLength parameter.
	 */
	public static String substringByUtf8BytesLength(String input, int maxBytesLength) {
		if (input == null) {
			return null;
		}
		if (maxBytesLength < 0) {
			throw new IllegalArgumentException("The maxBytesLength must not be less than 0: " + maxBytesLength);
		}
		if (maxBytesLength == 0) {
			return "";
		}
		Charset utf8Charset = Charset.forName("UTF-8");
		CharsetDecoder cd = utf8Charset.newDecoder();
		byte[] sba = input.getBytes(StandardCharsets.UTF_8);
		if (sba.length <= maxBytesLength) {
			return input;
		}
		// Ensure truncating by having byte buffer = DB_FIELD_LENGTH
		ByteBuffer bb = ByteBuffer.wrap(sba, 0, maxBytesLength); // len in [B]
		CharBuffer cb = CharBuffer.allocate(maxBytesLength); // len in [char] <= # [B]
		// Ignore an incomplete character
		cd.onMalformedInput(CodingErrorAction.IGNORE);
		cd.decode(bb, cb, true);
		cd.flush(cb);
		String result = new String(cb.array(), 0, cb.position());
		return result;
	}
}
