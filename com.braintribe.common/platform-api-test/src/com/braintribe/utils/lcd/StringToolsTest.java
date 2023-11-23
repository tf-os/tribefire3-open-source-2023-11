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

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static com.braintribe.utils.lcd.StringTools.abbreviateMiddle;
import static com.braintribe.utils.lcd.StringTools.createStringFromArray;
import static com.braintribe.utils.lcd.StringTools.findPrefix;
import static com.braintribe.utils.lcd.StringTools.findPrefixWithBoundary;
import static com.braintribe.utils.lcd.StringTools.findSuffix;
import static com.braintribe.utils.lcd.StringTools.findSuffixWithBoundary;
import static com.braintribe.utils.lcd.StringTools.getLongestLineLength;
import static com.braintribe.utils.lcd.StringTools.isBlank;
import static com.braintribe.utils.lcd.StringTools.isEmpty;
import static com.braintribe.utils.lcd.StringTools.isNumeric;
import static com.braintribe.utils.lcd.StringTools.prettyPrintMilliseconds;
import static com.braintribe.utils.lcd.StringTools.removeFirstNCharacters;
import static com.braintribe.utils.lcd.StringTools.removeLastNCharacters;
import static com.braintribe.utils.lcd.StringTools.truncateIfRequired;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

/**
 * Provides tests for {@link StringTools}.
 *
 * @author michael.lafite
 */
public class StringToolsTest {

	@Test
	public void testPrettyPrintMilliseconds1() {
		assertThat(prettyPrintMilliseconds(1L)).isEqualTo(".001");
		assertThat(prettyPrintMilliseconds(1000L)).isEqualTo("1.000");
		assertThat(prettyPrintMilliseconds(1001L)).isEqualTo("1.001");
		assertThat(prettyPrintMilliseconds(36000000L)).isEqualTo("10:00:00.000");
		assertThat(prettyPrintMilliseconds(36000002L)).isEqualTo("10:00:00.002");
		assertThat(prettyPrintMilliseconds(90000000L)).isEqualTo("1:01:00:00.000");
		assertThat(prettyPrintMilliseconds(36061000L)).isEqualTo("10:01:01.000");
	}

	@Test
	public void testGetLongestLineLength() {
		assertThat(getLongestLineLength((String[]) null)).isEqualTo(0);
		assertThat(getLongestLineLength(new String[] { null })).isEqualTo(0);
		assertThat(getLongestLineLength("hello")).isEqualTo(5);
		assertThat(getLongestLineLength("hello", "world")).isEqualTo(5);
		assertThat(getLongestLineLength("hello", "pi")).isEqualTo(5);
		assertThat(getLongestLineLength("one", "three")).isEqualTo(5);
	}

	@Test
	public void testIsBlankOrEmpty() {
		assertThat(isBlank(null)).isTrue();
		assertThat(isBlank("")).isTrue();
		assertThat(isBlank("   ")).isTrue();
		assertThat(isBlank("hello, world")).isFalse();

		assertThat(isEmpty(null)).isTrue();
		assertThat(isEmpty("")).isTrue();
		assertThat(isEmpty("   ")).isFalse();
		assertThat(isEmpty("hello, world")).isFalse();

	}

	@Test
	public void testAbbreviateMiddle() {

		assertThat(abbreviateMiddle(null, 10, '.')).isNull();
		assertThat(abbreviateMiddle("", 10, '.')).isEqualTo("");
		assertThat(abbreviateMiddle("hello", 10, '.')).isEqualTo("hello");
		assertThat(abbreviateMiddle("hello", 5, '.')).isEqualTo("hello");
		assertThat(abbreviateMiddle("helllo", 5, '.')).isEqualTo("he.lo");
		assertThat(abbreviateMiddle("hello", 4, '.')).isEqualTo("he.o");

		assertThat(abbreviateMiddle("helllllllllllo", 1, '.')).isEqualTo("h");
		assertThat(abbreviateMiddle("helllllllllllo", 2, '.')).isEqualTo("he");
		assertThat(abbreviateMiddle("helllllllllllo", 3, '.')).isEqualTo("h.o");
		assertThat(abbreviateMiddle("helllllllllllo", 4, '.')).isEqualTo("he.o");
		assertThat(abbreviateMiddle("helllllllllllo", 5, '.')).isEqualTo("he.lo");
		assertThat(abbreviateMiddle("helllllllllllo", 6, '.')).isEqualTo("he..lo");
		assertThat(abbreviateMiddle("helllllllllllo", 7, '.')).isEqualTo("hel..lo");
		assertThat(abbreviateMiddle("helllllllllllo", 8, '.')).isEqualTo("hel...lo");
		assertThat(abbreviateMiddle("helllllllllllo", 9, '.')).isEqualTo("hel...llo");

		assertThat(abbreviateMiddle("abc\uD83D\uDE02def", 4, '.')).isEqualTo("ab.f");
		assertThat(abbreviateMiddle("abcd\uD83D\uDE02", 4, '.')).isEqualTo("ab.\uD83D\uDE02");
		assertThat(abbreviateMiddle("\uD83D\uDE02abcd", 4, '.')).isEqualTo("\uD83D\uDE02.d");
		assertThat(abbreviateMiddle("a\uD83D\uDE02bcd", 4, '.')).isEqualTo("a.d");

		assertIllegalArgumentException(() -> abbreviateMiddle("hello", -1, '.'));
		assertIllegalArgumentException(() -> abbreviateMiddle("hello", 0, '.'));

	}

	private static void assertIllegalArgumentException(Runnable r) {
		try {
			r.run();
			throw new RuntimeException("This should have caused an IllegalArgumentException");
		} catch (IllegalArgumentException iae) { // NOSONAR
			// expected, all good
		}
	}

	@Test
	public void testAbbreviateMiddle2() {
		assertThat(abbreviateMiddle("abcd\uD83D\uDE02", 4, '.')).isEqualTo("ab.\uD83D\uDE02");
	}

	@Test
	public void testTruncate() {
		assertThat(truncateIfRequired("hello world", 200, false)).isEqualTo("hello world");
		assertThat(truncateIfRequired("hello world", 2, false)).isEqualTo("he");
		assertThat(truncateIfRequired("hello world", 2, true)).isEqualTo("he");

		assertThat(truncateIfRequired("12345678901234567890", 20, true)).isEqualTo("12345678901234567890");
		assertThat(truncateIfRequired("1234567890123456789012345678901234567890", 20, false)).isEqualTo("12345678901234567890");
		assertThat(truncateIfRequired("1234567890123456789012345678901234567890", 20, true)).isEqualTo("12345678901234567890");
		assertThat(truncateIfRequired("1234567890123456789012345678901234567890", 21, true)).isEqualTo("1(39 chars remaining)");
		assertThat(truncateIfRequired("1234567890123456789012345678901234567890", 22, true)).isEqualTo("12(38 chars remaining)");
		assertThat(truncateIfRequired("1234567890123456789012345678901234567890", 30, true)).isEqualTo("1234567890(30 chars remaining)");

	}

	@Test
	public void testIsNumeric() {
		assertThat(isNumeric(null)).isFalse();
		assertThat(isNumeric("hello")).isFalse();
		assertThat(isNumeric("1-1")).isFalse();
		assertThat(isNumeric("-1")).isTrue();
		assertThat(isNumeric("576348756348796578932465789324659873465837428765879563487563478")).isTrue();
	}

	@Test
	public void removeFirstN() {
		assertThat(removeFirstNCharacters("abc", -1)).isEqualTo("abc");
		assertThat(removeFirstNCharacters("abc", 0)).isEqualTo("abc");
		assertThat(removeFirstNCharacters("abc", 1)).isEqualTo("bc");
		assertThat(removeFirstNCharacters("abc", 4)).isEqualTo("");
	}

	@Test
	public void removesLastN() {
		assertThat(removeLastNCharacters("abc", -1)).isEqualTo("abc");
		assertThat(removeLastNCharacters("abc", 0)).isEqualTo("abc");
		assertThat(removeLastNCharacters("abc", 1)).isEqualTo("ab");
		assertThat(removeLastNCharacters("abc", 4)).isEqualTo("");
	}

	@Test
	public void findsPrefix() {
		assertThat(findPrefix("abc", "c")).isEqualTo("ab");
		assertThat(findPrefix("abc", "abc")).isEqualTo("");
		assertThat(findPrefix("abc", "dd")).isEqualTo("");
		assertThat(findPrefix("abc", "d")).isEqualTo("");
	}

	@Test
	public void findsPrefixWithBoundary() {
		assertThat(findPrefixWithBoundary("abc", "c")).isEqualTo("abc");
		assertThat(findPrefixWithBoundary("abc", "abc")).isEqualTo("abc");
		assertThat(findPrefixWithBoundary("abc", "ddd")).isEqualTo("");
		assertThat(findPrefixWithBoundary("abcabc", "ca")).isEqualTo("abca");
	}

	@Test
	public void findsSuffix() {
		assertThat(findSuffix("abc", "a")).isEqualTo("bc");
		assertThat(findSuffix("abc", "abc")).isEqualTo("");
		assertThat(findSuffix("abc", "d")).isEqualTo("abc");
		assertThat(findSuffix("abc", "b")).isEqualTo("c");
	}

	@Test
	public void findsSuffixWithBoundary() {
		assertThat(findSuffixWithBoundary("abc", "a")).isEqualTo("abc");
		assertThat(findSuffixWithBoundary("abc", "abc")).isEqualTo("abc");
		assertThat(findSuffixWithBoundary("abc", "d")).isEqualTo("");
		assertThat(findSuffixWithBoundary("abc", "ddd")).isEqualTo("");
		assertThat(findSuffixWithBoundary("abcabc", "ca")).isEqualTo("cabc");
	}

	@Test
	public void createsStringFromArray() {
		assertThat(createStringFromArray(array("a"), "-")).isEqualTo("a");
		assertThat(createStringFromArray(array("a", "b"), "-")).isEqualTo("a-b");
		assertThat(createStringFromArray(array("a", null), "-")).isEqualTo("a-null");
	}

	private Object[] array(Object... objects) {
		return objects;
	}
}
