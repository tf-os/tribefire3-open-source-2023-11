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
package com.braintribe.utils.lcd;

import static com.braintribe.common.lcd.Numbers.MILLISECONDS_PER_DAY;
import static com.braintribe.common.lcd.Numbers.MILLISECONDS_PER_HOUR;
import static com.braintribe.common.lcd.Numbers.MILLISECONDS_PER_MINUTE;
import static com.braintribe.common.lcd.Numbers.MILLISECONDS_PER_SECOND;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.braintribe.common.lcd.Constants;
import com.braintribe.common.lcd.GenericRuntimeException;
import com.braintribe.common.lcd.Numbers;
import com.braintribe.common.lcd.Pair;
import com.braintribe.common.lcd.transformer.Transformer;
import com.braintribe.common.lcd.transformer.TransformerException;
import com.braintribe.utils.lcd.string.StringDistance;

/**
 * This class provides helper methods related to {@link String}s.
 *
 * @author michael.lafite
 */
public class StringTools {

	public static final String WHITE_SPACE_REGEX = "\\s+";

	protected StringTools() {
		// nothing to do
	}

	/** @see CommonTools#isEmpty(String) */
	public static boolean isEmpty(final String string) {
		return CommonTools.isEmpty(string);
	}

	/** @deprecated use {@link #isEmpty(String)} */
	@Deprecated
	public static boolean isEmptyOrNull(final String string) {
		return CommonTools.isEmpty(string) || string == null;
	}

	/**
	 * See {@link CommonTools#isBlank(String)}.
	 */
	public static boolean isBlank(final String string) {
		return CommonTools.isBlank(string);
	}

	/**
	 * See {@link CommonTools#isAllEmpty(String...)}.
	 */
	public static boolean isAllEmpty(final String... strings) {
		return CommonTools.isAllEmpty(strings);
	}

	/**
	 * See {@link CommonTools#isAnyEmpty(String...)}.
	 */
	public static boolean isAnyEmpty(final String... strings) {
		return CommonTools.isAnyEmpty(strings);
	}

	/**
	 * See {@link CommonTools#isAllBlank(String...)}.
	 */
	public static boolean isAllBlank(final String... strings) {
		return CommonTools.isAllBlank(strings);
	}

	/**
	 * See {@link CommonTools#isAnyBlank(String...)}.
	 */
	public static boolean isAnyBlank(final String... strings) {
		return CommonTools.isAnyBlank(strings);
	}

	public static String requireNonEmpty(String s, String message) {
		return CommonTools.requireNonEmpty(s, message);
	}

	public static String requireNonEmpty(String s, Supplier<String> messageSupplier) {
		return CommonTools.requireNonEmpty(s, messageSupplier);
	}

	/**
	 * Removes the first and last character from the passed string. Mainly used to remove brackets.
	 *
	 * @return the new string where the first and last character have been removed or an empty string if the length of the specified string was
	 *         <code>&lt;= 1</code> or <code>null</code> if the passed string was <code>null</code>.
	 */

	public static String removeFirstAndLastCharacter(final String string) {
		if (string == null) {
			return null;
		}
		if (string.length() <= Numbers.TWO) {
			return "";
		}

		return string.substring(1, string.length() - 1);
	}

	/**
	 * Returns the result of {@link #getSubstringBefore(String, String, boolean)} searching the first occurrence.
	 */

	public static String getSubstringBefore(final String string, final String searchedString) {
		return getSubstringBefore(string, searchedString, false);
	}

	/**
	 * Returns the result of {@link #getSubstringBefore(String, String, boolean)} searching the last occurrence.
	 */

	public static String getSubstringBeforeLast(final String string, final String searchedString) {
		return getSubstringBefore(string, searchedString, true);
	}

	/**
	 * Searches the <code>searchedString</code> in the passed <code>string</code> and returns the substring before the <code>searchedstring</code>. If
	 * <code>string</code> is <code>null</code>, <code>null</code> is returned. If <code>searchedString</code> is <code>null</code>, the empty string
	 * is returned. If <code>searchedString</code> cannot be found in <code>string</code>, the whole <code>string</code> is returned.
	 *
	 * @param lastOccurrenceInsteadOfFirst
	 *            specifies whether to search for the first or the last occurrence.
	 */

	public static String getSubstringBefore(final String string, final String searchedString, final boolean lastOccurrenceInsteadOfFirst) {
		if (string == null) {
			return null;
		}

		if (searchedString == null) {
			return "";
		}

		final int index = (lastOccurrenceInsteadOfFirst ? string.lastIndexOf(searchedString) : string.indexOf(searchedString));
		if (index < Numbers.ZERO) {
			return string;
		}

		return string.substring(0, index);
	}

	/**
	 * Returns the result of {@link #getSubstringAfter(String, String, boolean)} searching the first occurrence.
	 */
	public static String getSubstringAfter(final String string, final String searchedString) {
		return getSubstringAfter(string, searchedString, false);
	}

	/**
	 * Returns the result of {@link #getSubstringAfter(String, String, boolean)} searching the last occurrence.
	 */

	public static String getSubstringAfterLast(final String string, final String searchedString) {
		return getSubstringAfter(string, searchedString, true);
	}

	/**
	 * Searches the <code>searchedString</code> in the passed <code>string</code> and returns the substring after the <code>searchedstring</code>. If
	 * <code>string</code> is <code>null</code>, <code>null</code> is returned. If <code>searchedString</code> is <code>null</code>, the
	 * <code>string</code> is returned. If <code>searchedString</code> cannot be found in <code>string</code>, an empty string is returned.
	 *
	 * @param lastOccurrenceInsteadOfFirst
	 *            specifies whether to search for the first or the last occurrence.
	 */
	public static String getSubstringAfter(final String string, final String searchedString, final boolean lastOccurrenceInsteadOfFirst) {
		if ((string == null) || (searchedString == null)) {
			return string;
		}

		final int index = (lastOccurrenceInsteadOfFirst ? string.lastIndexOf(searchedString) : string.indexOf(searchedString));
		if (index < Numbers.ZERO) {
			return "";
		}

		return string.substring(index + searchedString.length());
	}

	/**
	 * Returns the substring of <code>string</code> between <code>leftSearchString</code> and <code>rightSearchString</code>. Arguments must not be
	 * <code>null</code>!
	 */
	public static String getSubstringBetween(final String string, final String leftSearchString, final String rightSearchString) {
		Arguments.notNullWithNames("string", string, "leftSearchString", leftSearchString, "rightSearchString", rightSearchString);
		String temp = getSubstringAfter(string, leftSearchString);
		String result = getSubstringBefore(temp, rightSearchString);
		return result;
	}

	/**
	 * Returns the substring of <code>string</code> from <code>leftSearchString</code> to <code>rightSearchString</code> . Arguments must not be
	 * <code>null</code>! This works like {@link #getSubstringBetween(String, String, String)} except that search strings are included in the returned
	 * string.
	 */
	public static String getSubstringFromTo(final String string, final String leftSearchString, final String rightSearchString) {
		return leftSearchString + getSubstringBetween(string, leftSearchString, rightSearchString) + rightSearchString;
	}

	/**
	 * Returns the substring starting with <code>searchedString</code> until the end of the <code>string</code>.
	 *
	 * @see #getSubstringFromTo(String, String, String)
	 * @see #getSubstringTo(String, String)
	 *
	 */
	public static String getSubstringFrom(final String string, final String searchedString) {
		return searchedString + getSubstringAfter(string, searchedString);
	}

	/**
	 * Returns the substring starting with <code>searchedString</code> until the end of the <code>string</code>.
	 *
	 * @see #getSubstringFromTo(String, String, String)
	 * @see #getSubstringFrom(String, String)
	 */
	public static String getSubstringTo(final String string, final String searchedString) {
		return getSubstringBefore(string, searchedString) + searchedString;
	}

	/**
	 * Shortens a String by cutting something out in the middle and replacing it with a character. If the resulting text has to be smaller than 6
	 * characters, only one filler char will be used. If the text has to be smaller than 8 characters, two filler chars will be used. In any other
	 * case, 3 filler chars will be used.
	 *
	 * @param text
	 *            The String that should be shortened
	 * @param maxTotalLength
	 *            The maximum total length of the result (including the filler characters)
	 * @param fillChar
	 *            The character to use to fill the gap
	 * @return A shortened String
	 */
	public static String abbreviateMiddle(String text, int maxTotalLength, char fillChar) {
		if (text == null) {
			return text;
		}
		int originalLength = text.length();
		if (originalLength <= maxTotalLength) {
			return text;
		}
		if (maxTotalLength <= 0) {
			throw new IllegalArgumentException("The maximum total length (" + maxTotalLength + ") must be a positive number.");
		}

		int fillCount = 3;
		if (maxTotalLength <= 5) {
			fillCount = 1;
		} else if (maxTotalLength <= 7) {
			fillCount = 2;
		}

		int remainLength = maxTotalLength - fillCount;

		if (remainLength < 2) {
			return text.substring(0, maxTotalLength);
		}
		int fromRight = remainLength / 2;
		int left = remainLength - fromRight;
		int right = originalLength - fromRight;

		int correctedLeft = (Character.isLowSurrogate(text.charAt(left)) && left > 0) ? left - 1 : left;
		int correctedRight = (Character.isLowSurrogate(text.charAt(right)) && right > 0) ? right - 1 : right;

		String leftString = text.substring(0, correctedLeft);
		String rightString = text.substring(correctedRight);

		StringBuilder sb = new StringBuilder(leftString);
		for (int i = 0; i < fillCount; ++i) {
			sb.append(fillChar);
		}
		sb.append(rightString);
		return sb.toString();
	}

	/**
	 * Returns the given {@code string}, shortened to 12 characters, with 3 '.' (dots) appended.
	 *
	 * @param string
	 *            - String to be abbreviated
	 */
	public static String abbreviate(final String string) {
		return abbreviate(string, 12, '.');
	}

	/**
	 * Returns the given {@code string}, shortened to the first {@code numberOfChars}, and with {@code appendedChar} appended 3 times.
	 *
	 * @param string
	 *            - String to be abbreviated
	 * @param numberOfCharacters
	 *            - Number of chars {@code string} is shortened to
	 * @param appendedCharacter
	 *            - Char to be appended 3 times. (Usually '.')
	 */
	public static String abbreviate(final String string, final int numberOfCharacters, final char appendedCharacter) {
		if (string.length() > numberOfCharacters) {
			return getFirstNCharacters(string, numberOfCharacters) + getFilledString(3, appendedCharacter);
		} else {
			return string;
		}
	}

	/**
	 * Gets the last characters of the passed <code>string</code>. If the passed <code>string</code> is <code>null</code>, <code>null</code> is
	 * returned. If <code>numberOfCharacters</code> is <code>&lt;=0</code>, an empty string is returned. If the <code>numberOfCharacters</code>
	 * exceeds the length of the <code>string</code>, the whole <code>string</code> is returned.
	 */

	public static String getLastNCharacters(final String string, final int numberOfCharacters) {
		if (string == null) {
			return null;
		}

		if (numberOfCharacters <= Numbers.ZERO) {
			return "";
		}

		final int length = string.length();

		if (length <= numberOfCharacters) {
			return string;
		}

		return string.substring(length - numberOfCharacters);
	}

	/**
	 * Removes the last characters from the passed <code>string</code> and returns the new string. If the passed <code>string</code> is
	 * <code>null</code>, <code>null</code> is returned. If <code>numberOfCharacters</code> is <code>&lt;=0</code>, the whole <code>string</code> is
	 * returned. If the <code>numberOfCharacters</code> exceeds the length of the <code>string</code>, an empty string is returned.
	 */

	public static String removeLastNCharacters(final String string, final int numberOfCharacters) {
		if (string == null) {
			return null;
		}

		if (numberOfCharacters <= Numbers.ZERO) {
			return string;
		}

		final int length = string.length();

		if (length <= numberOfCharacters) {
			return "";
		}

		return string.substring(0, length - numberOfCharacters);
	}

	/**
	 * Returns the prefix of given text up until the first occurrence of given boundary. If no boundary is found, empty string is returned.
	 */
	public static String findPrefix(String text, String boundary) {
		return getFirstNCharacters(text, text.indexOf(boundary));
	}

	/**
	 * Returns the prefix of given text up until the first occurrence of given boundary, including the boundary. If no boundary is found, empty string
	 * is returned.
	 */
	public static String findPrefixWithBoundary(String text, String boundary) {
		int i = text.indexOf(boundary);
		return i < 0 ? "" : getFirstNCharacters(text, i + boundary.length());
	}

	/** Gets the first characters of a string. Works like {@link #removeLastNCharacters(String, int)}. */
	public static String getFirstNCharacters(final String string, final int numberOfCharacters) {
		return string == null ? null : removeLastNCharacters(string, string.length() - numberOfCharacters);
	}

	/**
	 * Removes the first characters from a string and returns the result. Works like {@link #getLastNCharacters(String, int)}.
	 */
	public static String removeFirstNCharacters(final String string, final int numberOfCharacters) {
		return string == null ? null : getLastNCharacters(string, string.length() - numberOfCharacters);
	}

	/**
	 * Removes given prefix from given string. If this string does not start with such prefix, the string is returned unmodified.
	 */
	public static String removePrefixIfEligible(String text, String maybePrefix) {
		return text.startsWith(maybePrefix) ? removePrefix(text, maybePrefix) : text;
	}

	/**
	 * Removes the specified <code>prefix</code> from the passed <code>string</code> and returns the resulting string.
	 *
	 * @throws IllegalArgumentException
	 *             if <code>string</code> doesn't start with <code>prefix</code>.
	 */
	public static String removePrefix(String string, String prefix) throws IllegalArgumentException {
		if (!string.startsWith(prefix)) {
			throw new IllegalArgumentException("The passed string doesn't start with the specified prefix! "
					+ CommonTools.getParametersString("string", string, "prefix", prefix));
		}
		String result = StringTools.removeFirstNCharacters(string, prefix.length());
		return result;
	}

	/**
	 * Returns the suffix of given text after the last occurrence of given boundary. If not boundary is found, the original string is returned.
	 */
	public static String findSuffix(String text, String boundary) {
		int i = text.lastIndexOf(boundary);
		return i < 0 ? text : getLastNCharacters(text, text.length() - i - boundary.length());
	}

	/**
	 * Returns the suffix of given text after the last occurrence of given boundary, including the boundary. If not boundary is found, empty String is
	 * returned.
	 */
	public static String findSuffixWithBoundary(String text, String boundary) {
		int i = text.lastIndexOf(boundary);
		return i < 0 ? "" : getLastNCharacters(text, text.length() - i);
	}

	/**
	 * Removes given suffix from the given string. If this string does not end with such suffix, the string is returned unmodified.
	 */
	public static String removeSuffixIfEligible(String text, String maybeSuffix) {
		return text.endsWith(maybeSuffix) ? removeSuffix(text, maybeSuffix) : text;
	}

	/**
	 * Removes the specified <code>suffix</code> from the passed <code>string</code> and returns the resulting string.
	 *
	 * @throws IllegalArgumentException
	 *             if <code>string</code> doesn't end with <code>suffix</code>.
	 */
	public static String removeSuffix(String string, String suffix) throws IllegalArgumentException {
		if (string.endsWith(suffix)) {
			return StringTools.removeLastNCharacters(string, suffix.length());
		} else {
			throw new IllegalArgumentException("The passed string doesn't end with the specified suffix! "
					+ CommonTools.getParametersString("string", string, "suffix", suffix));
		}

	}

	/**
	 * Gets the the specified number of characters of a string starting from <code>startIndex</code>. If the passed <code>string</code> is
	 * <code>null</code>, <code>null</code> is returned. If the <code>startIndex</code> exceeds the length of the string, an empty string is returned.
	 * If the <code>string</code> is not long enough to return as many characters as specified, less characters are returned.
	 */

	public static String getNCharacters(final String string, final int numberOfCharacters, final int startIndex) {
		if (string == null) {
			return null;
		}
		if (startIndex > string.length()) {
			return "";
		}

		final String tmp = string.substring(startIndex);
		return getFirstNCharacters(tmp, numberOfCharacters);
	}

	/**
	 * Truncates the passed <code>string</code> if it's longer then the specified maximum length. If the passed string is <code>null</code>, null is
	 * returned.
	 */

	public static String truncateIfRequired(final String string, final int maxLength) {
		return truncateIfRequired(string, maxLength, false);
	}
	public static String truncateIfRequired(final String string, final int maxLength, boolean writeRemainingLength) {
		if (maxLength < Numbers.ZERO) {
			throw new IllegalArgumentException("Max length " + maxLength + " is invalid!");
		}
		if (string == null) {
			return string;
		}

		if (maxLength <= 20) {
			// Would not make sense as we would just see the message
			writeRemainingLength = false;
		}

		int toCut = string.length() - maxLength;
		if (toCut > 0) {
			if (writeRemainingLength) {
				String expl = "(" + toCut + " chars remaining)";
				int toCut2 = toCut + expl.length();
				expl = "(" + toCut2 + " chars remaining)";
				int toCut3 = toCut + expl.length();
				String tmp = removeLastNCharacters(string, toCut3);
				return tmp.concat(expl);
			} else {
				return removeLastNCharacters(string, string.length() - maxLength);
			}
		}
		return string;
	}
	// ........(1234 chars remaining)

	/**
	 * @see CommonTools#toStringOrNull(Object)
	 */

	public static String toStringOrNull(final Object object) {
		return CommonTools.toStringOrNull(object);
	}

	/**
	 * @see CommonTools#toStringOrEmptyString(Object)
	 */
	public static String toStringOrEmptyString(final Object object) {
		return CommonTools.toStringOrEmptyString(object);
	}

	/**
	 * @see CommonTools#toStringOrDefault(Object, String)
	 */
	public static String toStringOrDefault(final Object object, final String defaultString) {
		return CommonTools.toStringOrDefault(object, defaultString);
	}

	/**
	 * Gets a string with the specified <code>length</code> filled with the specified <code>character</code>.
	 */
	public static String getFilledString(final int length, final char character) {
		final char[] charArray = new char[length];
		Arrays.fill(charArray, character);
		return new String(charArray);
	}

	/**
	 * Returns a new string where the substring from index <code>startIndex</code> (inclusive) to index <code>endIndex</code> (exclusive) is replaced
	 * by the <code>replacement</code> string which may have any length.
	 */
	public static String replaceRegion(final String string, final String replacement, final int startIndex, final int endIndex) {
		if (CommonTools.isAnyNull(string, replacement)) {
			throw new IllegalArgumentException(
					"Passed arguments must not be null! " + CommonTools.getParametersString("string", string, "replacement", replacement));

		}

		if (startIndex > endIndex) {
			throw new IllegalArgumentException(
					"Illegal index numbers! " + CommonTools.getParametersString("string", string, "startIndex", startIndex, "endIndex", endIndex));
		}

		return string.substring(0, (startIndex == 0 ? 0 : startIndex)) + replacement + string.substring(endIndex);
	}

	/** Invokes {@link #replaceWhiteSpace(String, String)} with an empty replacement string. */
	public static String removeWhiteSpace(final String string) {
		return replaceWhiteSpace(string, "");
	}

	/**
	 * Replaces all white space in the passes <code>string</code> with the specified <code>replacementString</code> and returns the result.
	 */
	public static String replaceWhiteSpace(final String string, final String replacementString) {
		if (isEmpty(string)) {
			return string;
		}

		return Not.Null(string.replaceAll(WHITE_SPACE_REGEX, replacementString));
	}

	/**
	 * Compares two char arrays.
	 *
	 * @param charArray1
	 *            the first array to compare.
	 * @param offset1
	 *            the offset for the first array.
	 * @param charArray2
	 *            the second array to compare.
	 * @param offset2
	 *            the offset for the second array.
	 * @param length
	 *            how many characters to compare.
	 * @return <code>true</code> if the specified regions match or if both arrays are <code>null</code>.
	 */
	public static boolean regionMatches(final char[] charArray1, final int offset1, final char[] charArray2, final int offset2, final int length) {
		if (CommonTools.isAllNull(charArray1, charArray2)) {
			return true;
		}
		if (CommonTools.isAnyNull(charArray1, charArray2)) {
			return false;
		}

		if (charArray1.length < offset1 + length || charArray2.length < offset2 + length) {
			return false;
		}

		for (int i = 0; i < length; i++) {
			if (charArray1[offset1 + i] != charArray2[offset2 + i]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Searches a substring in a collection of strings and returns the matching results.
	 *
	 * @param strings
	 *            the strings to check.
	 * @param searchedString
	 *            the searched string (which must not be null).
	 * @return the strings in the passed string collection that contain the specified searched string.
	 */
	public static List<String> getStringsContainingSearchedString(final Collection<String> strings, final String searchedString) {
		Arguments.notNullWithNames("searchedString", searchedString);
		final List<String> result = new ArrayList<>();
		if (!CommonTools.isEmpty(strings)) {
			for (final String string : strings) {
				if (string != null && string.contains(searchedString)) {
					result.add(string);
				}
			}
		}
		return result;
	}

	/**
	 * Searches a substring in a collection of strings and returns the first match.
	 *
	 * @param strings
	 *            the strings to check.
	 * @param searchedString
	 *            the searched string (which must not be null).
	 * @return the first string in the passed string collection that contains the specified searched string (or <code>null</code> if the searched
	 *         string is not found).
	 */

	public static String getFirstStringContainingSearchedString(final Collection<String> strings, final String searchedString) {
		Arguments.notNullWithNames("searchedString", searchedString);
		if (!CommonTools.isEmpty(strings)) {
			for (final String string : NullSafe.iterable(strings)) {
				if (string != null && string.contains(searchedString)) {
					return string;
				}
			}
		}
		return null;
	}

	/**
	 * Checks if at least one of the passed strings contains the specified searched string.
	 *
	 * @param strings
	 *            the strings to check.
	 * @param searchedString
	 *            the searched string (which must not be null).
	 * @return <code>true</code> if the searched string is found, otherwise <code>false</code>.
	 */
	public static boolean containsStringContainingSearchedString(final Collection<String> strings, final String searchedString) {
		Arguments.notNullWithNames("searchedString", searchedString);
		return getFirstStringContainingSearchedString(strings, searchedString) != null;
	}

	/**
	 * Splits the passed <code>string</code> once, i.e. at the first occurrence of the <code>delimiter</code>, which is NOT handled as a regular
	 * expression.
	 */
	public static Pair<String, String> splitStringOnce(String string, String delimiter) throws IllegalArgumentException {
		if (isAnyEmpty(string, delimiter)) {
			throw new IllegalArgumentException("Neither the string nor the delimiter may be null or empty! "
					+ CommonTools.getParametersString("string", string, "delimiter", delimiter));
		}

		int index = string.indexOf(delimiter);
		if (index < 0) {
			throw new IllegalArgumentException("Cannot split string because it doesn't contain the delimiter! "
					+ CommonTools.getParametersString("string", string, "delimiter", delimiter));
		}

		String first = string.substring(0, index);
		String second = string.substring(index + delimiter.length());
		return new Pair<>(first, second);
	}

	/**
	 * Splits the passed <code>string</code> once, i.e. at the first occurrence of the <code>delimiter</code>, which is NOT handled as a regular
	 * expression. If the delimiter is not found a {@link Pair} with a nulled {@link Pair#second()} is returned.
	 */
	public static Pair<String, String> splitDelimitedPair(final String string, final String delimiter) throws IllegalArgumentException {
		if (string == null) {
			throw new IllegalArgumentException("Argument string must not be null");
		}

		if (delimiter == null || delimiter.isEmpty()) {
			throw new IllegalArgumentException("Argument delimiter must not be null or empty");
		}

		final int index = string.indexOf(delimiter);
		if (index < 0) {
			return Pair.of(string, null);
		} else {
			final String first = string.substring(0, index);
			final String second = string.substring(index + delimiter.length());
			return Pair.of(first, second);
		}
	}

	/**
	 * Splits the passed <code>string</code> once, i.e. at the first occurrence of the <code>delimiter</code>, which is NOT handled as a regular
	 * expression. If the delimiter is not found a {@link Pair} with a nulled {@link Pair#second()} is returned.
	 */
	public static Pair<String, String> splitDelimitedPair(final String string, final char delimiter) throws IllegalArgumentException {
		if (string == null) {
			throw new IllegalArgumentException("Argument string must not be null");
		}

		final int index = string.indexOf(delimiter);
		if (index < 0) {
			return Pair.of(string, null);
		} else {
			final String first = string.substring(0, index);
			final String second = string.substring(index + 1);
			return Pair.of(first, second);
		}
	}

	/**
	 * Behaves like {@link String#split(String, int)} (with second argument set to <code>-1</code>) except that the <code>delimiter</code> is not
	 * handled as a regular expression.
	 */
	public static String[] splitString(final String stringToSplit, final String delimiter) {
		final List<String> stringList = new ArrayList<>();
		if (!isEmpty(stringToSplit)) {
			final int stringLength = stringToSplit.length();
			final int delimiterLength = delimiter.length();
			int currentIndex = 0;

			while (currentIndex <= stringLength) {

				int nextDelimiterIndex = stringToSplit.indexOf(delimiter, currentIndex);
				if (nextDelimiterIndex == -1) {
					nextDelimiterIndex = stringLength;
				}

				final String element = stringToSplit.substring(currentIndex, nextDelimiterIndex);
				stringList.add(element);

				currentIndex = nextDelimiterIndex + delimiterLength;
			}
		}
		final String[] result = new String[stringList.size()];
		ArrayTools.toArray(stringList, result);
		return result;
	}

	/** ThisIsXYZText -> this-is--xyz-text */
	public static String camelCaseToSocialDistancingCase(String s) {
		return splitCamelCaseSmart(s).stream() //
				.map(String::toLowerCase) //
				.collect(Collectors.joining("-"));
	}

	/** ThisIsXYZText -> THIS_IS_XYZ_TEXT */
	public static String camelCaseToScreamingSnakeCase(String s) {
		return camelCaseToUnderscoreSeparated(s).toUpperCase();
	}

	/** ThisIsXYZText -> this_is_xyz_text */
	public static String camelCaseToSnakeCase(String s) {
		return camelCaseToUnderscoreSeparated(s).toLowerCase();
	}

	private static String camelCaseToUnderscoreSeparated(String s) {
		return splitCamelCaseSmart(s).stream() //
				.collect(Collectors.joining("_"));
	}

	/** E.g. for "This1IsXYZStuff" returns ["This1", "Is", "XYZ", "StufF"]. */
	public static List<String> splitCamelCaseSmart(String value) {
		// split on every position with sequence (nonCapital, Capital) OR (Capital, Capital+NonCapital)
		return Arrays.asList(value.split("(?<=[^A-Z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][^A-Z])"));
	}

	/** E.g. for "ThisIsXYZStuff" returns ["This", "Is", "X", "Y", "Z", "StufF"]. */
	public static List<String> splitCamelCase(String value) {
		final List<String> result = new ArrayList<>();

		while (value.length() > 0) {
			int pos = findFirstCapitalPosition(value.substring(1));

			if (pos < 0) {
				result.add(value);
				value = "";
			} else {
				pos++;
				result.add(value.substring(0, pos));
				value = value.substring(pos);
			}
		}

		return result;
	}

	public static int findFirstCapitalPosition(final String value) {
		for (int i = 0; i < value.length(); i++) {
			if (Character.isUpperCase(value.charAt(i))) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Removes trailing characters from the passed string and returns a new one.
	 */
	public static String removeTrailingCharacters(final String text, final char charToRemove) {
		int index = text.length() - 1;
		while (index >= 0 && text.charAt(index) == charToRemove) {
			index--;
		}
		return Not.Null(text.substring(0, index + 1));
	}

	/**
	 * @see CommonTools#getStringRepresentation(Object)
	 */
	public static String getStringRepresentation(final Object object) {
		return CommonTools.getStringRepresentation(object);
	}

	/**
	 * Invokes {@link #replaceAllButLastOccurrence(String, String, String)} with an empty replacement string.
	 */
	public static String removeAllButLastOccurrence(final String string, final String searchedString) {
		return replaceAllButLastOccurrence(string, searchedString, "");
	}

	/**
	 * Replaces all but the last occurrence of <code>searchedString</code> in the passed <code>string</code> with the specified
	 * <code>replacementString</code> and returns the result. Arguments must not be <code>null</code>!
	 */
	public static String replaceAllButLastOccurrence(final String string, final String searchedString, final String replacementString) {

		if (CommonTools.isAnyNull(string, searchedString, replacementString)) {
			throw new IllegalArgumentException("At least one of the arguments is null! "
					+ CommonTools.getParametersString("string", string, "searchedString", searchedString, "replacementString", replacementString));

		}
		final int lastOccurrenceIndex = string.lastIndexOf(searchedString);
		if (lastOccurrenceIndex < Numbers.ZERO) {
			return string;
		}

		final String stringBeforeLastOccurrence = string.substring(0, lastOccurrenceIndex);
		final String stringBeforeLastOccurrenceWithAllOtherOccurrencesReplaced = stringBeforeLastOccurrence.replace(searchedString,
				replacementString);

		final String stringBeginningWithLastOccurence = string.substring(lastOccurrenceIndex);

		return stringBeforeLastOccurrenceWithAllOtherOccurrencesReplaced + stringBeginningWithLastOccurence;
	}

	/**
	 * Returns the result of {@link #countOccurrences(String, String, boolean)} with case sensitivity enabled.
	 */
	public static int countOccurrences(final String text, final String token) throws NullPointerException {
		return countOccurrences(text, token, true);
	}

	/**
	 * Counts the number of occurrences of the <code>token</code> in the <code>text</code>.
	 *
	 * @param text
	 *            The text that should be examined.
	 * @param token
	 *            The token that should be counted.
	 * @param caseSensitivityEnabled
	 *            Indicates whether the check should be case sensitive.
	 * @return the number of occurrences of token in text.
	 * @throws NullPointerException
	 *             if <code>text</code> and/or <code>token</code> are <code>null</code>.
	 */
	public static int countOccurrences(final String text, final String token, final boolean caseSensitivityEnabled) throws NullPointerException {

		if (CommonTools.isAnyNull(text, token)) {
			throw new IllegalArgumentException(
					"At least one of passed arguments is null! " + CommonTools.getParametersString("text", text, "token", token));
		}

		String normalizedText = text;
		String normalizedToken = token;

		if (!caseSensitivityEnabled) {
			normalizedText = normalizedText.toLowerCase();
			normalizedToken = normalizedToken.toLowerCase();
		}

		int result = 0;
		int idx = normalizedText.indexOf(normalizedToken);
		while (idx != -1) {
			result++;
			idx = normalizedText.indexOf(normalizedToken, idx + 1);
		}

		return result;
	}

	/**
	 * Invokes {@link #createStringFromCollection(Collection, String, Transformer)} with a transformer that uses the automatic Java string conversion.
	 */
	public static <E> String createStringFromCollection(final Collection<E> collection, final String separator) {
		final Transformer<E, String, Object> stringRepresentationTransformer = new Transformer<E, String, Object>() {

			@Override
			public String transform(final E input, final Object transformationContext) {
				return String.valueOf(input);
			}
		};

		return createStringFromCollection(collection, separator, stringRepresentationTransformer);
	}

	/**
	 * Returns the result of {@link #createStringFromCollection(Collection, String, Transformer, Object)} with no transformation context.
	 */
	public static <E, C> String createStringFromCollection(final Collection<E> collection, final String separator,
			final Transformer<E, String, C> stringRepresentationTransformer) {
		return createStringFromCollection(collection, separator, stringRepresentationTransformer, null);
	}

	/**
	 * Creates a string containing the string representations of the elements in the passed <code>collection</code> separated by the specified
	 * <code>separator</code>.
	 *
	 * @param stringRepresentationTransformer
	 *            the transformer used to get the string representation of the elements.
	 * @param transformationContext
	 *            a transformation context. May or may not be required (depending on the transformer implementation).
	 * @param <E>
	 *            type of the elements in the collection
	 * @param <C>
	 *            type of the transformation context
	 */
	public static <E, C> String createStringFromCollection(final Collection<E> collection, final String separator,
			final Transformer<E, String, C> stringRepresentationTransformer, final C transformationContext) {
		if (collection == null || collection.isEmpty()) {
			return "";
		}

		final StringBuilder stringBuilder = new StringBuilder();
		for (final E element : collection) {
			String stringRepresentationOfElement;
			try {
				stringRepresentationOfElement = stringRepresentationTransformer.transform(element, transformationContext);
			} catch (final TransformerException e) {
				throw new GenericRuntimeException(
						"Error while getting string representation of element! " + CommonTools.getParametersString("element", element,
								"stringRepresentationTransformer", stringRepresentationTransformer, "transformationContext", transformationContext),
						e);
			}
			stringBuilder.append(stringRepresentationOfElement);
			stringBuilder.append(separator);
		}
		final String resultWithSepartorSuffix = stringBuilder.toString();
		final String result = removeLastNCharacters(resultWithSepartorSuffix, separator.length());
		return Not.Null(result);
	}

	/**
	 * See {@link CommonTools#uncapitalize(String)}.
	 */
	public static String uncapitalize(final String string) {
		return CommonTools.uncapitalize(string);
	}

	/**
	 * See {@link CommonTools#capitalize(String)}.
	 */
	public static String capitalize(final String string) {
		return CommonTools.capitalize(string);
	}

	/**
	 * Concatenates the passed <code>strings</code>.
	 */
	public static String concat(final String... strings) {
		return newStringBuilder(strings).toString();
	}

	/**
	 * Works like {@link #concat(String...)} except that it gets the string representation of the <code>objects</code>.
	 */
	public static String concat(final Object... objects) {
		return newStringBuilder(objects).toString();
	}

	/**
	 * Creates a new <code>StringBuilder</code> and appends the passed <code>strings</code>.
	 */
	public static StringBuilder newStringBuilder(final String... strings) {
		final StringBuilder stringBuilder = new StringBuilder();
		append(stringBuilder, strings);
		return stringBuilder;
	}

	/**
	 * Works like {@link #newStringBuilder(String...)} except that it gets the string representation of the <code>objects</code>.
	 */
	public static StringBuilder newStringBuilder(final Object... objects) {
		final StringBuilder stringBuilder = new StringBuilder();
		append(stringBuilder, objects);
		return stringBuilder;
	}

	/**
	 * Appends the <code>stringsToAppend</code> to the <code>stringBuilder</code>.
	 */
	// catch nullpointer instead of checking for null (performance)
	public static void append(final StringBuilder stringBuilder, final String... strings) {
		try {
			for (final String stringToAppend : strings) {
				stringBuilder.append(stringToAppend);
			}
		} catch (final NullPointerException e) {
			throw new IllegalArgumentException(
					"Nullpointer exception while appending strings to stringBuilder! The passed arguments must not be null! "
							+ CommonTools.getParametersString("stringBuilder", stringBuilder, "strings", strings),
					e);
		}
	}

	/**
	 * Works like {@link #append(StringBuilder, String...)} except that it gets the string representation of the <code>objects</code>.
	 */
	// catch nullpointer instead of checking for null (performance)
	public static void append(final StringBuilder stringBuilder, final Object... objects) {
		try {
			for (final Object objectToAppend : objects) {
				stringBuilder.append(objectToAppend);
			}
		} catch (final NullPointerException e) {
			throw new IllegalArgumentException(
					"Nullpointer exception while appending strings to stringBuilder! The passed arguments must not be null! "
							+ CommonTools.getParametersString("stringBuilder", stringBuilder, "objects", objects),
					e);
		}
	}

	public static String join(final CharSequence separator, final Iterable<?> iterable) {
		return join(separator, NullSafe.iterator(iterable));
	}

	/* Slightly modified implementation from {@link org.apache.commons.lang.StringUtils#join(Iterator, char)} () */
	public static String join(final CharSequence separator, final Iterator<?> iterator) {
		final StringBuilder buf = new StringBuilder(256);

		while (iterator.hasNext()) {
			final Object o = iterator.next();
			if (o != null) {
				buf.append(o);
			}
			if (iterator.hasNext()) {
				buf.append(separator);
			}
		}

		return buf.toString();
	}

	public static String replaceAllOccurences(final String text, final String match, final String replacement) {
		return replaceAllOccurences(text, match, replacement, true);
	}

	public static String replaceAllOccurences(String text, final String match, final String replacement, final boolean caseSensitive) {
		if (text == null) {
			return null;
		}

		int ix = -1;

		String matchTL = null;
		if (caseSensitive) {
			ix = text.indexOf(match);
		} else {
			final String textTL = text.toLowerCase();
			matchTL = match.toLowerCase();
			ix = textTL.indexOf(matchTL);
		}

		while (ix != -1) {
			text = text.substring(0, ix) + replacement + text.substring(ix + match.length());

			if (caseSensitive) {
				ix = text.indexOf(match, ix + (replacement.length()));
			} else {
				final String textTL = text.toLowerCase();
				ix = textTL.indexOf(matchTL);
			}
		}
		return text;
	}

	/**
	 * Invokes {@link #simpleObfuscatePassword(String, int)} with a maximum of 2 displayed characters.
	 */
	public static String simpleObfuscatePassword(final String password) {
		return simpleObfuscatePassword(password, 2);
	}

	/**
	 * Invokes {@link #simpleObfuscatePassword(String, int, int)} with a minimum of 2 obfuscated characters.
	 */
	public static String simpleObfuscatePassword(final String password, final int maximumDisplayedCharacters) {
		return simpleObfuscatePassword(password, 2, maximumDisplayedCharacters);
	}

	/**
	 * Obfuscates the passord by replacing the characters with '*'. The beginning of the password is optionally shown (depending on the parameters).
	 *
	 * @param password
	 *            the password to obfuscate
	 * @param minimumObfuscatedCharacters
	 *            the minimum number of obfuscated passwords. If set to <code>&lt;0</code>, all characters will be obfuscated.
	 * @param maximumDisplayedCharacters
	 *            the maximum number of displayed characters. If set to <code>&lt;0</code>, all characters may be displayed (parameter
	 *            <code>minimumObfuscatedCharacters</code> is stronger).
	 * @return the obfuscated password.
	 */
	public static String simpleObfuscatePassword(final String password, int minimumObfuscatedCharacters, int maximumDisplayedCharacters) {

		if (password == null) {
			return null;
		}

		if (minimumObfuscatedCharacters < 0) {
			minimumObfuscatedCharacters = Integer.MAX_VALUE;
		}
		if (maximumDisplayedCharacters < 0) {
			maximumDisplayedCharacters = Integer.MAX_VALUE;
		}

		final int passwordLength = password.length();

		int displayedCount = 0;
		int obfuscatedCount = 0;

		obfuscatedCount = Math.min(minimumObfuscatedCharacters, passwordLength);

		displayedCount = passwordLength - obfuscatedCount;

		if (displayedCount > maximumDisplayedCharacters) {
			final int temp = displayedCount - maximumDisplayedCharacters;

			displayedCount -= temp;
			obfuscatedCount += temp;
		}

		final StringBuilder buf = new StringBuilder();

		if (displayedCount > 0) {
			buf.append(password.substring(0, displayedCount));
		}

		for (int i = 0; i < obfuscatedCount; i++) {
			buf.append("*");
		}

		return buf.toString();
	}

	/**
	 * Convenience method to create a human-readable String representation of a time span. If the includeUnits is false, the method
	 * {@link #prettyPrintMilliseconds(long)} will be internally invoked. If includeUnits is set to true, this method will return the time span with
	 * time units included (using SI - International System of Units - standards). Note that only those parts are included in the resulting String
	 * that are greater than 0 (if, for example, the time span is 1 hour and 10 milliseconds, the result will not include 0 minutes).<br>
	 * Examples: 0 will result in "0 ms", 60000 in "1 min", 86400010 in "1 d 10 ms"
	 *
	 * @param ms
	 *            The time span in milliseconds.
	 * @param includeUnits
	 *            Indicates whether the time units should be included.
	 * @return A String representation of the time span.
	 */
	public static String prettyPrintMilliseconds(long ms, boolean includeUnits) {
		if (ms == 0) {
			return includeUnits ? "0 ms" : "0";
		}

		if (!includeUnits) {
			return prettyPrintMilliseconds(ms);
		}

		long msperday = 86400000;
		long msperhour = 3600000;
		long msperminute = 60000;
		long mspersecond = 1000;

		long days = 0;
		long hours = 0;
		long minutes = 0;
		long seconds = 0;

		days = (ms / msperday);
		ms = ms % msperday;
		hours = (ms / msperhour);
		ms = ms % msperhour;
		minutes = (ms / msperminute);
		ms = ms % msperminute;
		seconds = (ms / mspersecond);
		ms = ms % mspersecond;

		StringBuilder result = new StringBuilder();
		if (days > 0) {
			result.append(days);
			result.append(" d");
		}
		if (hours > 0) {
			if (result.length() > 0) {
				result.append(' ');
			}
			result.append(hours);
			result.append(" h");
		}
		if (minutes > 0) {
			if (result.length() > 0) {
				result.append(' ');
			}
			result.append(minutes);
			result.append(" min");
		}
		if (seconds > 0) {
			if (result.length() > 0) {
				result.append(' ');
			}
			result.append(seconds);
			result.append(" s");
		}
		if (ms > 0) {
			if (result.length() > 0) {
				result.append(' ');
			}
			result.append(ms);
			result.append(" ms");
		}
		return result.toString();
	}

	private static final long mspersecond = MILLISECONDS_PER_SECOND;
	private static final long msperminute = MILLISECONDS_PER_MINUTE;
	private static final long msperhour = MILLISECONDS_PER_HOUR;
	private static final long msperday = MILLISECONDS_PER_DAY;

	/**
	 * Returns the provided milliseconds in a more human-readable form. If 0 (zero) is provided, the return String will be "0". 1 will result in
	 * ".001", 1000 in "1.000", 60000 (1 minute) in "1:00.000", 86400000 (1 day) in "1:00:00:00.000". The maximum unit is 'days'.
	 *
	 * @param ms
	 *            The time span in milliseconds that should be converted to a String.
	 * @return The String representing the time span.
	 */
	public static String prettyPrintMilliseconds(long ms) {
		if (ms == 0) {
			return "0";
		}

		long days = ms / msperday;
		ms = ms % msperday;
		long hours = ms / msperhour;
		ms = ms % msperhour;
		long minutes = ms / msperminute;
		ms = ms % msperminute;
		long seconds = ms / mspersecond;
		ms = ms % mspersecond;

		StringBuilder result = new StringBuilder();

		if (days > 0) {
			result.append(days);
		}
		appendIfGreaterThanZero(result, hours);
		appendIfGreaterThanZero(result, minutes);
		appendIfGreaterThanZero(result, seconds);

		result.append(".");
		if (ms < 100) {
			result.append("0");
		}
		if (ms < 10) {
			result.append("0");
		}
		result.append(ms);

		return result.toString();
	}

	protected static void appendIfGreaterThanZero(StringBuilder sb, long number) {
		if (number > 0) {
			if (sb.length() > 0) {
				sb.append(":");
				if (number < 10) {
					sb.append("0");
				}
			}
			sb.append(number);
		} else if (sb.length() > 0) {
			sb.append(":00");
		}
	}

	/**
	 * Returns <code>true</code>, if the passed <code>string</code> {@link String#startsWith(String) starts with} any of the specified
	 * <code>prefixes</code>, otherwise <code>false</code>.
	 */
	public static boolean startsWithAny(String string, Collection<String> prefixes) {
		for (String prefix : NullSafe.iterable(prefixes)) {
			if (string.startsWith(prefix)) {
				return true;
			}
		}
		return false;
	}

	public static String replaceOnce(final String text, final String searchString, final String replacement) {
		return replace(text, searchString, replacement, 1);
	}

	public static String replace(final String text, final String searchString, final String replacement, int max) {
		if (isEmpty(text) || isEmpty(searchString) || replacement == null || max == 0) {
			return text;
		}
		int start = 0;
		int end = text.indexOf(searchString, start);
		if (end == -1) {
			return text;
		}
		final int replLength = searchString.length();
		int increase = replacement.length() - replLength;
		increase = increase < 0 ? 0 : increase;
		increase *= max < 0 ? 16 : max > 64 ? 64 : max;
		final StringBuilder buf = new StringBuilder(text.length() + increase);
		while (end != -1) {
			buf.append(text.substring(start, end)).append(replacement);
			start = end + replLength;
			if (--max == 0) {
				break;
			}
			end = text.indexOf(searchString, start);
		}
		buf.append(text.substring(start));
		return buf.toString();
	}

	/**
	 * Checks if the passed string is a multi line string.
	 *
	 * @see Constants#SYSTEM_INDEPENDENT_LINE_SEPARATOR_REGEX
	 */
	public static boolean isMultiLineString(String string) {
		return NullSafe.matches(string, ".*" + Constants.SYSTEM_INDEPENDENT_LINE_SEPARATOR_REGEX + ".*");
	}

	/**
	 * Splits the <code>string</code> using {@link Constants#SYSTEM_INDEPENDENT_LINE_SEPARATOR_REGEX line separator regex} and returns the individual
	 * lines.
	 */
	public static List<String> getLines(String string) {
		Arguments.notNull(string, "The passed string must not be null!");
		List<String> result = Arrays.asList(string.split(Constants.SYSTEM_INDEPENDENT_LINE_SEPARATOR_REGEX));
		return result;
	}

	/**
	 * Checks if the passed <code>string</code> contains all of the <code>searchedStrings</code>.
	 */
	public static boolean containsAll(String string, Iterable<String> searchedStrings) {
		Arguments.notNullWithNames(string, "string");
		boolean result = true;
		for (String searchedString : NullSafe.iterable(searchedStrings)) {
			if (!string.contains(searchedString)) {
				result = false;
				break;
			}
		}
		return result;
	}

	/**
	 * Checks if the passed <code>string</code> contains any of the <code>searchedStrings</code>.
	 */
	public static boolean containsAny(String string, Iterable<String> searchedStrings) {
		Arguments.notNullWithNames(string, "string");
		boolean result = false;
		for (String searchedString : NullSafe.iterable(searchedStrings)) {
			if (string.contains(searchedString)) {
				result = true;
				break;
			}
		}
		return result;
	}

	/**
	 * Returns <code>true</code>, if the two strings are are equal or both <code>null</code>, otherwise <code>false</code>.
	 *
	 * @deprecated use {@link CommonTools#equals(Object, Object)} instead.
	 */
	@Deprecated
	public static boolean equals(String a, String b) {
		if (a == null && b == null) {
			return true;
		}
		if (a == null || b == null) {
			return false;
		}
		return a.equals(b);
	}

	public static boolean equalsIgnoreCaseOrBothNull(String a, String b) {
		if (a == null) {
			return b == null;
		} else {
			return a.equalsIgnoreCase(b);
		}
	}

	/**
	 * Replaces {@link Constants#SYSTEM_INDEPENDENT_LINE_SEPARATOR_REGEX line separators} in the passed <code>string</code> with the specified
	 * <code>lineSeparator</code>.
	 *
	 * @throws IllegalArgumentException
	 *             if any argument is <code>null</code>.
	 */
	public static String normalizeLineSeparators(String string, String lineSeparator) throws IllegalArgumentException {
		Arguments.notNullWithNames("string", string, "lineSeparator", lineSeparator);
		String result = string.replaceAll(Constants.SYSTEM_INDEPENDENT_LINE_SEPARATOR_REGEX, lineSeparator);
		return result;
	}

	/**
	 * Gets an array of Strings and returns the length of the longest String. If the array is null or empty, 0 will be returned.
	 *
	 * @param strings
	 *            An array of Strings.
	 * @return The length of the longest String of the array.
	 */
	public static int getLongestLineLength(String... strings) {
		if (strings == null || strings.length == 0) {
			return 0;
		}
		int maxLength = 0;
		for (String s : strings) {
			if (s != null) {
				maxLength = Math.max(maxLength, s.length());
			}
		}
		return maxLength;
	}

	/**
	 * Tests whether the provided String is numeric or not. Note that any other character (e.g., a newline, a tab, etc) will cause this method to
	 * return false.
	 *
	 * @param string
	 *            The String that should be examined.
	 * @return True, when the String only contains numbers, false othewise.
	 */
	public static boolean isNumeric(String string) {
		if (string == null) {
			return false;
		}
		int len = string.length();
		for (int i = 0; i < len; i++) {
			char charAt = string.charAt(i);
			if (charAt == '-' && i == 0) {
				continue;
			}
			if (!Character.isDigit(charAt)) {
				return false;
			}
		}
		return true;
	}

	public static String repeat(String s, int times) {
		return new String(new char[times]).replace("\0", s);
	}

	/** Invokes {@link #createStringFromArray(Object[], String)} with "," separator. */
	public static String createStringFromArray(final Object[] array) {
		return createStringFromArray(array, ",");
	}

	/**
	 * Creates a string from an error.
	 *
	 * @param array
	 *            Object array
	 * @param separator
	 *            used to separate the string representations of the elements in the <code>array</code>.
	 * @return <code>separator</code> delimited string representation of the provided array or <code>null</code>, if the array is <code>null</code>.
	 *         If there is a <code>null</code> value in the array, it is represented by a <i>null</i> string.
	 */
	public static String createStringFromArray(final Object[] array, final String separator) {
		if (array == null) {
			return null;
		}

		StringJoiner sj = new StringJoiner(separator);
		for (Object element : array) {
			sj.add(element == null ? "null" : element.toString());
		}

		return sj.toString();
	}

	/**
	 * Returns a string representation of the passed array. The whole array and each row are wrapped by '[' and ']'. Commas and line breaks are added
	 * after each row. The columns are comma separated as well.
	 *
	 * @param array
	 *            Two dimensional Object array.
	 * @return String representation of the passed array or <code>null</code> if the passed array is null.
	 */

	public static String createStringFromTwoDimensionalArray(final Object[][] array) {
		if (array == null) {
			return null;
		}
		StringBuilder stringBuilder = new StringBuilder("[");
		if (array.length > Numbers.ZERO) {
			for (final Object[] row : array) {
				final String rowString = StringTools.createStringFromArray(row, ", ");
				if (rowString == null) {
					stringBuilder.append("null");
				} else {
					stringBuilder.append('[');
					stringBuilder.append(rowString);
					stringBuilder.append(']');
				}
				stringBuilder.append(",\n");
			}
			stringBuilder = new StringBuilder(stringBuilder.substring(0, stringBuilder.length() - 2));
		}

		stringBuilder.append(']');
		return stringBuilder.toString();
	}

	/** @see StringDistance#damerauLevenshteinDistance(CharSequence, CharSequence) */
	public static int distanceDamerauLevenshtein(String a, String b) {
		return StringDistance.damerauLevenshteinDistance(a, b);
	}

	/** @see StringDistance#damerauLevenshteinDistance(CharSequence, CharSequence, int) */
	public static int distanceDamerauLevenshtein(String a, String b, int max) {
		return StringDistance.damerauLevenshteinDistance(a, b, max);
	}

	/** Compares two strings primarily by their length (shorter comes first), secondarily by their natural order. */
	public static int compareStringsSizeFirst(String s1, String s2) {
		int result = s1.length() - s2.length();
		return result != 0 ? result : s1.compareTo(s2);
	}

	/**
	 * Returns the input text, or, when this is blank, the defaultText
	 *
	 * @param text
	 *            The initial text that should be checked for blankness
	 * @param defaultText
	 *            The backup text that will be returned if text is blank
	 * @return Either text or defaultText, depending on whether text is blank or not.
	 */
	public static String ensureNonBlankString(String text, String defaultText) {
		if (!StringTools.isBlank(text)) {
			return text;
		}
		return defaultText;
	}
}
