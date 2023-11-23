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

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.UnaryOperator;
import java.util.regex.PatternSyntaxException;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.braintribe.common.lcd.Numbers;

/**
 * Provides tests for {@link StringTools}.
 *
 * @author michael.lafite
 */

public class StringToolsTest {

	@Test
	public void testSplitString() {
		final String[] expected = { "a", "b", "c" };
		final String[] actual = StringTools.splitString("a,b,c", ",");
		Assert.assertEquals(Arrays.asList(expected), Arrays.asList(actual));
	}

	@Test
	public void testSplitCamelCaseSmart() {
		final List<String> actual = StringTools.splitCamelCaseSmart("ThisIsSomeXYZString");
		final List<String> expected = Arrays.asList("This", "Is", "Some", "XYZ", "String");
		assertThat(actual).isEqualTo(expected);
	}

	@Test
	public void testSplitCamelCase() {
		final List<String> actual = StringTools.splitCamelCase("ThisIsSomeXYZString");
		final List<String> expected = Arrays.asList("This", "Is", "Some", "X", "Y", "Z", "String");
		assertThat(actual).isEqualTo(expected);
	}

	// @Ignore // ignoring because this test needs to be fixed, since it doesn't work on all platforms --> "." vs ","
	// //Hopefully fixed by adding a Locale in the implementation
	@Test
	public void testPrettyPrintBytesBinary() {

		assertThat(StringTools.prettyPrintBytesBinary(0)).isEqualTo("0 B");
		assertThat(StringTools.prettyPrintBytesBinary(1024)).isEqualTo("1.0 KiB");
		assertThat(StringTools.prettyPrintBytesBinary(1048576)).isEqualTo("1.0 MiB");
		assertThat(StringTools.prettyPrintBytesBinary(1048577)).isEqualTo("1.0 MiB");
		assertThat(StringTools.prettyPrintBytesBinary(512)).isEqualTo("512 B");
		assertThat(StringTools.prettyPrintBytesBinary(513)).isEqualTo("513 B");
		assertThat(StringTools.prettyPrintBytesBinary(1023)).isEqualTo("1023 B");
		assertThat(StringTools.prettyPrintBytesBinary(1200000)).isEqualTo("1.1 MiB");

	}

	// @Ignore // ignoring because this test needs to be fixed, since it doesn't work on all platforms --> "." vs ","
	// //Hopefully fixed by adding a Locale in the implementation
	@Test
	public void testPrettyPrintBytesDecimal() {

		assertThat(StringTools.prettyPrintBytesDecimal(0)).isEqualTo("0 B");
		assertThat(StringTools.prettyPrintBytesDecimal(1000)).isEqualTo("1.0 kB");
		assertThat(StringTools.prettyPrintBytesDecimal(1001)).isEqualTo("1.0 kB");
		assertThat(StringTools.prettyPrintBytesDecimal(1000000)).isEqualTo("1.0 MB");
		assertThat(StringTools.prettyPrintBytesDecimal(512)).isEqualTo("512 B");
		assertThat(StringTools.prettyPrintBytesDecimal(513)).isEqualTo("513 B");
		assertThat(StringTools.prettyPrintBytesDecimal(1023)).isEqualTo("1.0 kB");
		assertThat(StringTools.prettyPrintBytesDecimal(1200000)).isEqualTo("1.2 MB");

	}

	@Test
	public void testJoinFileArray() {

		File[] files = { new File("1"), new File("2") };
		String joined = StringTools.join(",", files);
		Assert.assertEquals("1,2", joined);
	}

	@Test
	public void testGetSubstringBetween() {
		assertThat(StringTools.getSubstringBetween("abcdef", "b", "e")).isEqualTo("cd");
	}

	@Test
	public void testGetSubstringFromTo() {
		assertThat(StringTools.getSubstringFromTo("abcdef", "b", "e")).isEqualTo("bcde");
	}

	@Test
	public void testGetSubstringFrom() {
		assertThat(StringTools.getSubstringFrom("abcdef", "b")).isEqualTo("bcdef");
	}

	@Test
	public void testGetSubstringTo() {
		assertThat(StringTools.getSubstringTo("abcdef", "e")).isEqualTo("abcde");
	}

	@Test
	public void testPrettifyCamelCase() {

		assertThat(StringTools.prettifyCamelCase("helloWorld", true)).isEqualTo("Hello World");
		assertThat(StringTools.prettifyCamelCase("hello", true)).isEqualTo("Hello");

		assertThat(StringTools.prettifyCamelCase("helloWorld", false)).isEqualTo("hello World");

		assertThat(StringTools.prettifyCamelCase("", true)).isEqualTo("");
		assertThat(StringTools.prettifyCamelCase(null, true)).isEqualTo(null);
		assertThat(StringTools.prettifyCamelCase(" ", true)).isEqualTo(" ");

	}

	@Test
	public void testGetMatches() {
		assertThat(StringTools.getMatches(null, null)).isEmpty();
		assertThat(StringTools.getMatches(null, null, (String[]) null)).isEmpty();

		List<String> result = StringTools.getMatches(null, null, "hello");
		checkStringCollection(result, "hello");

		List<String> includes = new ArrayList<>();
		List<String> excludes = new ArrayList<>();

		includes.clear();
		excludes.clear();
		includes.add(".*llo.*");
		result = StringTools.getMatches(includes, excludes, "hello", "world");
		checkStringCollection(result, "hello");

		includes.clear();
		excludes.clear();
		includes.add(".*llo.*");
		result = StringTools.getMatches(includes, null, "hello", "world");
		checkStringCollection(result, "hello");

		includes.clear();
		excludes.clear();
		includes.add(".*llo.*");
		excludes.add("he.*");
		result = StringTools.getMatches(includes, excludes, "hello", "world");
		checkStringCollection(result);

		includes.clear();
		excludes.clear();
		includes.add(".*oe.*");
		result = StringTools.getMatches(includes, excludes, "what", "does", "the", "fox", "say");
		checkStringCollection(result, "does");

		try {
			includes.clear();
			excludes.clear();
			includes.add("{]<;-2");
			result = StringTools.getMatches(includes, excludes, "hello");
			throw new AssertionError("There should have been exception because of an invalid pattern here.");
		} catch (PatternSyntaxException expectedException) {
			// OK, expected
		}

		includes.clear();
		excludes.clear();
		includes.add(".*llo.*");
		assertThat(StringTools.matches(includes, excludes, "hello, world")).isTrue();

		includes.clear();
		excludes.clear();
		excludes.add(".*llo.*");
		assertThat(StringTools.matches(includes, excludes, "hello, world")).isFalse();

		includes.clear();
		excludes.clear();
		includes.add("hello");
		assertThat(StringTools.matches(includes, excludes, "hello")).isTrue();

		includes.clear();
		excludes.clear();
		excludes.add("hello");
		assertThat(StringTools.matches(includes, excludes, "hello")).isFalse();

		includes.clear();
		excludes.clear();
		excludes.add("hello");
		assertThat(StringTools.matches(includes, excludes, "hello, world")).isTrue();

		assertThat(StringTools.matches(includes, excludes, null)).isFalse();
	}

	@Ignore
	protected static void checkStringCollection(Collection<String> collection, String... expectedValues) {
		if (collection == null && expectedValues == null) {
			return;
		}
		if (collection != null && expectedValues == null) {
			throw new AssertionError("The collection " + collection + " is not null (not as expected).");
		}
		if (collection == null && expectedValues != null) {
			throw new AssertionError("The collection " + collection + " is null (not as expected).");
		}
		for (String exp : expectedValues) {
			if (!collection.contains(exp)) {
				throw new AssertionError("The collection " + collection + " does not contain the expected value: " + exp);
			}
		}
		for (String r : collection) {
			boolean found = false;
			for (String exp : expectedValues) {
				if (r.equals(exp)) {
					found = true;
					break;
				}
			}
			if (!found) {
				throw new AssertionError("The collection " + collection + " contains the value: " + r + " which was not expected.");
			}
		}
	}

	@Test
	public void testGetSubstringAfterLast() {

		assertThat(StringTools.getSubstringAfterLast("hello.this.is.world", ".")).isEqualTo("world");

		// Note from the author of this test: I would have this expected to return the string itself ("world")
		// But I don't want to change the implementation because the side-effects are not known to me.
		// At least, this test will fail when somebody changes the implementation unwittingly.
		assertThat(StringTools.getSubstringAfterLast("world", ".")).isEqualTo("");

	}

	@Test
	public void testGetPatternVariables() {

		List<String> result = null;
		List<String> expected = new ArrayList<>();

		result = StringTools.getPatternVariables("hello, world");
		assertThat(result).isNotNull();
		assertThat(result).isEmpty();

		result = StringTools.getPatternVariables("hello, {key} world");
		expected.clear();
		expected.add("key");
		assertThat(result).isNotNull();
		assertThat(result).isNotEmpty();
		assertThat(result).isEqualTo(expected);

		result = StringTools.getPatternVariables("hello, {key1} world {key2}");
		expected.clear();
		expected.add("key1");
		expected.add("key2");
		assertThat(result).isNotNull();
		assertThat(result).isNotEmpty();
		assertThat(result).isEqualTo(expected);

		try {
			StringTools.getPatternVariables(null);
			fail("This should have thrown a NPE.");
		} catch (NullPointerException npe) {
			// Nothing to do
		}
	}

	@Test
	public void testGetFirstLineSeparator() {
		assertThat(StringTools.getFirstLineSeparator("")).isNull();
		assertThat(StringTools.getFirstLineSeparator("abc")).isNull();
		assertThat(StringTools.getFirstLineSeparator("\n")).isEqualTo("\n");
		assertThat(StringTools.getFirstLineSeparator("abc\n")).isEqualTo("\n");
		assertThat(StringTools.getFirstLineSeparator("\r\n\n\r")).isEqualTo("\r\n");
		assertThat(StringTools.getFirstLineSeparator("\n\r\n\n\r")).isEqualTo("\n");
	}

	@Test
	public void testNormalizeLineSeparators() {
		try {
			StringTools.normalizeLineSeparators("x", null);
			fail();
		} catch (IllegalArgumentException e) {
			// expected
		}
		try {
			StringTools.normalizeLineSeparators(null, "\n");
			fail();
		} catch (IllegalArgumentException e) {
			// expected
		}

		assertThat(StringTools.normalizeLineSeparators("", "x")).isEqualTo("");
		assertThat(StringTools.normalizeLineSeparators("X\nX\rX\r\nX", "\n")).isEqualTo("X\nX\nX\nX");
	}

	@Test
	public void testLimitConsecutiveOccurrences() {
		assertThat(StringTools.limitConsecutiveOccurrences("a", "b", 0)).isEqualTo("a");
		assertThat(StringTools.limitConsecutiveOccurrences("a", "a", 0)).isEqualTo("");
		assertThat(StringTools.limitConsecutiveOccurrences("a", "a", 1)).isEqualTo("a");
		assertThat(StringTools.limitConsecutiveOccurrences("aa", "a", 1)).isEqualTo("a");
		assertThat(StringTools.limitConsecutiveOccurrences("aa", "a", 2)).isEqualTo("aa");
		assertThat(StringTools.limitConsecutiveOccurrences("aaa", "a", 2)).isEqualTo("aa");
		assertThat(StringTools.limitConsecutiveOccurrences("aaabaababaaaa", "a", 2)).isEqualTo("aabaababaa");
		assertThat(StringTools.limitConsecutiveOccurrences("baaabaaaabaaaaaa", "a", 100)).isEqualTo("baaabaaaabaaaaaa");
	}

	@Test
	public void testPrettyPrintMilliseconds() {
		assertThat(StringTools.prettyPrintMilliseconds(0L)).isEqualTo("0");
		assertThat(StringTools.prettyPrintMilliseconds(1L)).isEqualTo(".001");
		assertThat(StringTools.prettyPrintMilliseconds(50L)).isEqualTo(".050");
		assertThat(StringTools.prettyPrintMilliseconds(500L)).isEqualTo(".500");
		assertThat(StringTools.prettyPrintMilliseconds(1000L)).isEqualTo("1.000");
		assertThat(StringTools.prettyPrintMilliseconds(1001L)).isEqualTo("1.001");
		assertThat(StringTools.prettyPrintMilliseconds(60000L)).isEqualTo("1:00.000");
		assertThat(StringTools.prettyPrintMilliseconds(120000L)).isEqualTo("2:00.000");
		assertThat(StringTools.prettyPrintMilliseconds(120050L)).isEqualTo("2:00.050");
		assertThat(StringTools.prettyPrintMilliseconds(3600000L)).isEqualTo("1:00:00.000");
		assertThat(StringTools.prettyPrintMilliseconds(3600001L)).isEqualTo("1:00:00.001");
		assertThat(StringTools.prettyPrintMilliseconds(86400000L)).isEqualTo("1:00:00:00.000");
		assertThat(StringTools.prettyPrintMilliseconds(86400010L)).isEqualTo("1:00:00:00.010");

		assertThat(StringTools.prettyPrintMilliseconds(0L, false)).isEqualTo("0");
		assertThat(StringTools.prettyPrintMilliseconds(50L, false)).isEqualTo(".050");
		assertThat(StringTools.prettyPrintMilliseconds(500L, false)).isEqualTo(".500");
		assertThat(StringTools.prettyPrintMilliseconds(1000L, false)).isEqualTo("1.000");
		assertThat(StringTools.prettyPrintMilliseconds(1001L, false)).isEqualTo("1.001");
		assertThat(StringTools.prettyPrintMilliseconds(60000L, false)).isEqualTo("1:00.000");
		assertThat(StringTools.prettyPrintMilliseconds(120000L, false)).isEqualTo("2:00.000");
		assertThat(StringTools.prettyPrintMilliseconds(120050L, false)).isEqualTo("2:00.050");
		assertThat(StringTools.prettyPrintMilliseconds(3600000L, false)).isEqualTo("1:00:00.000");
		assertThat(StringTools.prettyPrintMilliseconds(3600001L, false)).isEqualTo("1:00:00.001");
		assertThat(StringTools.prettyPrintMilliseconds(86400000L, false)).isEqualTo("1:00:00:00.000");
		assertThat(StringTools.prettyPrintMilliseconds(86400010L, false)).isEqualTo("1:00:00:00.010");
	}

	@Test
	public void testPrettyPrintMillisecondsWithUnits() {
		assertThat(StringTools.prettyPrintMilliseconds(0L, true)).isEqualTo("0 ms");
		assertThat(StringTools.prettyPrintMilliseconds(50L, true)).isEqualTo("50 ms");
		assertThat(StringTools.prettyPrintMilliseconds(500L, true)).isEqualTo("500 ms");
		assertThat(StringTools.prettyPrintMilliseconds(1000L, true)).isEqualTo("1 s");
		assertThat(StringTools.prettyPrintMilliseconds(1001L, true)).isEqualTo("1 s 1 ms");
		assertThat(StringTools.prettyPrintMilliseconds(60000L, true)).isEqualTo("1 min");
		assertThat(StringTools.prettyPrintMilliseconds(120000L, true)).isEqualTo("2 min");
		assertThat(StringTools.prettyPrintMilliseconds(120050L, true)).isEqualTo("2 min 50 ms");
		assertThat(StringTools.prettyPrintMilliseconds(3600000L, true)).isEqualTo("1 h");
		assertThat(StringTools.prettyPrintMilliseconds(3600001L, true)).isEqualTo("1 h 1 ms");
		assertThat(StringTools.prettyPrintMilliseconds(86400000L, true)).isEqualTo("1 d");
		assertThat(StringTools.prettyPrintMilliseconds(86400010L, true)).isEqualTo("1 d 10 ms");
	}

	@Test
	public void testRemoveTrailingWhitespace() {
		assertThat(StringTools.removeTrailingWhitespace(null)).isNull();
		assertThat(StringTools.removeTrailingWhitespace("a")).isSameAs("a");
		assertThat(StringTools.removeTrailingWhitespace(" a")).isSameAs(" a");
		assertThat(StringTools.removeTrailingWhitespace(" \t\n\na  aljsdh  alksdhj  aksdj \t\n\n")).isEqualTo(" \t\n\na  aljsdh  alksdhj  aksdj");
	}

	@Test
	public void testRemoveLeadingWhitespace() {
		assertThat(StringTools.removeLeadingWhitespace(null)).isNull();
		assertThat(StringTools.removeLeadingWhitespace("a")).isSameAs("a");
		assertThat(StringTools.removeLeadingWhitespace("a ")).isSameAs("a ");
		assertThat(StringTools.removeLeadingWhitespace(" \t\n\na  aljsdh  alksdhj  aksdj \t\n\n")).isEqualTo("a  aljsdh  alksdhj  aksdj \t\n\n");
	}

	@Test
	public void testSplitCommaSeparatedString() {
		assertThat(StringTools.splitCommaSeparatedString(null, true)).isNull();
		assertThat(StringTools.splitCommaSeparatedString(null, false)).isNull();

		assertThat(StringTools.splitCommaSeparatedString("Hello", false)).isEqualTo(new String[] { "Hello" });
		assertThat(StringTools.splitCommaSeparatedString("Hello", true)).isEqualTo(new String[] { "Hello" });
		assertThat(StringTools.splitCommaSeparatedString("Hello ", false)).isEqualTo(new String[] { "Hello " });
		assertThat(StringTools.splitCommaSeparatedString("Hello", true)).isEqualTo(new String[] { "Hello" });

		assertThat(StringTools.splitCommaSeparatedString("Hello World", false)).isEqualTo(new String[] { "Hello World" });

		assertThat(StringTools.splitCommaSeparatedString("Hello, World", false)).isEqualTo(new String[] { "Hello", " World" });
		assertThat(StringTools.splitCommaSeparatedString("Hello, World", true)).isEqualTo(new String[] { "Hello", "World" });

		assertThat(StringTools.splitCommaSeparatedString("Hello, \"cruel,World\"", true)).isEqualTo(new String[] { "Hello", "\"cruel,World\"" });
	}

	@Test
	public void testSplitSemicolonSeparatedString() {
		assertThat(StringTools.splitSemicolonSeparatedString(null, true)).isNull();
		assertThat(StringTools.splitSemicolonSeparatedString(null, false)).isNull();

		assertThat(StringTools.splitSemicolonSeparatedString("Hello", false)).isEqualTo(new String[] { "Hello" });
		assertThat(StringTools.splitSemicolonSeparatedString("Hello", true)).isEqualTo(new String[] { "Hello" });
		assertThat(StringTools.splitSemicolonSeparatedString("Hello ", false)).isEqualTo(new String[] { "Hello " });
		assertThat(StringTools.splitSemicolonSeparatedString("Hello", true)).isEqualTo(new String[] { "Hello" });

		assertThat(StringTools.splitSemicolonSeparatedString("Hello World", false)).isEqualTo(new String[] { "Hello World" });

		assertThat(StringTools.splitSemicolonSeparatedString("Hello; World", false)).isEqualTo(new String[] { "Hello", " World" });
		assertThat(StringTools.splitSemicolonSeparatedString("Hello; World", true)).isEqualTo(new String[] { "Hello", "World" });

		assertThat(StringTools.splitSemicolonSeparatedString("Hello; \"cruel;World\"", true)).isEqualTo(new String[] { "Hello", "\"cruel;World\"" });
	}

	@Test
	public void testSplitSeparatedStringCustomQuoteChar() {
		assertThat(StringTools.splitCharSeparatedString("Hello, 'cruel,World'", ',', '\'', true))
				.isEqualTo(new String[] { "Hello", "'cruel,World'" });
		assertThat(StringTools.splitCharSeparatedString("'Hello', 'cruel,World'", ',', '\'', true))
				.isEqualTo(new String[] { "'Hello'", "'cruel,World'" });
		assertThat(StringTools.splitCharSeparatedString("'Hello', 'cruel''World'", ',', '\'', true))
				.isEqualTo(new String[] { "'Hello'", "'cruel''World'" });

		String[] actual = StringTools.splitCharSeparatedString("\"(?i).*derby.*\", \"(?i).*mysql.*\", \"(?i).*maria.*\", \"(?i).*microsoft.*jdbc.*\"",
				',', '\"', true);
		String[] expected = { "\"(?i).*derby.*\"", "\"(?i).*mysql.*\"", "\"(?i).*maria.*\"", "\"(?i).*microsoft.*jdbc.*\"" };
		assertThat(actual).isEqualTo(expected);

	}

	@Test
	public void testParseBytesString() {
		try {
			StringTools.parseBytesString(null);
			throw new AssertionError("This should have thrown a NullPointerException");
		} catch (NullPointerException npe) {
			// expected
		}

		try {
			StringTools.parseBytesString("");
			throw new AssertionError("This should have thrown an IllegalArgumentException");
		} catch (IllegalArgumentException iae) {
			// expected
		}

		try {
			StringTools.parseBytesString("   ");
			throw new AssertionError("This should have thrown an IllegalArgumentException");
		} catch (IllegalArgumentException iae) {
			// expected
		}

		try {
			StringTools.parseBytesString("-1");
			throw new AssertionError("This should have thrown an IllegalArgumentException");
		} catch (IllegalArgumentException iae) {
			// expected
		}

		try {
			StringTools.parseBytesString("kb1");
			throw new AssertionError("This should have thrown an IllegalArgumentException");
		} catch (IllegalArgumentException iae) {
			// expected
		}

		try {
			StringTools.parseBytesString("Hello, world!");
			throw new AssertionError("This should have thrown an IllegalArgumentException");
		} catch (IllegalArgumentException iae) {
			// expected
		}

		assertThat(StringTools.parseBytesString("1kb")).isEqualTo(1024L);
		assertThat(StringTools.parseBytesString("2 kb")).isEqualTo(2048L);
		assertThat(StringTools.parseBytesString("2    kb")).isEqualTo(2048L);
		assertThat(StringTools.parseBytesString("1023")).isEqualTo(1023L);
		assertThat(StringTools.parseBytesString("0")).isEqualTo(0L);
		assertThat(StringTools.parseBytesString("0GB")).isEqualTo(0L);
		assertThat(StringTools.parseBytesString("1MB")).isEqualTo((long) Math.pow(1024L, 2));
		assertThat(StringTools.parseBytesString("1GB")).isEqualTo((long) Math.pow(1024L, 3));
		assertThat(StringTools.parseBytesString("1TB")).isEqualTo((long) Math.pow(1024L, 4));
		assertThat(StringTools.parseBytesString("1PB")).isEqualTo((long) Math.pow(1024L, 5));
		assertThat(StringTools.parseBytesString("1EB")).isEqualTo((long) Math.pow(1024L, 6));
		assertThat(StringTools.parseBytesString("0.1kb")).isEqualTo(102L);
		assertThat(StringTools.parseBytesString("0.2 kb")).isEqualTo(204L);

	}

	protected static Map<String, String> codec(Map<String, String> map) {
		String encoded = StringTools.encodeStringMapToString(map);
		System.out.println("Map: " + map + ": encoded: " + encoded);
		return StringTools.decodeStringMapFromString(encoded);
	}

	@Test
	public void testMapEncoding() {

		Map<String, String> map = new LinkedHashMap<>();
		map.put("hello", "world");

		assertThat(codec(map)).isEqualTo(map);

		map.clear();
		map.put("1", "2");
		map.put("3", "4");

		assertThat(codec(map)).isEqualTo(map);

		map.clear();

		assertThat(codec(map)).isEqualTo(map);

		map.clear();
		map.put("&=", "\\&amp;");

		assertThat(codec(map)).isEqualTo(map);

		map.clear();
		map.put("=", "=");

		assertThat(codec(map)).isEqualTo(map);

		map.clear();
		map.put("&=", "\\&amp;");

		assertThat(codec(map)).isEqualTo(map);

		map.clear();
		map.put("&=", "\\&amp;");
		map.put("=", "&");
		map.put("&", "%32");

		assertThat(codec(map)).isEqualTo(map);

		map.clear();
		map.put("", "");

		assertThat(codec(map)).isEqualTo(map);

		map.clear();
		map.put("", "hello");

		assertThat(codec(map)).isEqualTo(map);

		map.clear();
		map.put("hello", "");

		assertThat(codec(map)).isEqualTo(map);

		map.put("", "");
		map.put("hello", "");

		assertThat(codec(map)).isEqualTo(map);

		try {
			StringTools.encodeStringMapToString(null);
			throw new AssertionError("This should have thrown an exception.");
		} catch (IllegalArgumentException expected) {
			// do nothing
		}
	}

	@Test
	public void testPrettyPrintDurationWithUnits() {
		Duration d;

		d = Duration.ZERO;
		assertThat(StringTools.prettyPrintDuration(d, true, null)).isEqualTo("0 ns");

		d = Duration.ZERO;
		assertThat(StringTools.prettyPrintDuration(d, true, ChronoUnit.HOURS)).isEqualTo("0 h");

		d = Duration.ZERO.plusNanos(1L).minusNanos(1L);
		assertThat(StringTools.prettyPrintDuration(d, true, null)).isEqualTo("0 ns");

		d = Duration.ZERO.plusHours(1);
		assertThat(StringTools.prettyPrintDuration(d, true, null)).isEqualTo("1 h");

		d = Duration.ZERO.plusHours(1).plusMinutes(1);
		assertThat(StringTools.prettyPrintDuration(d, true, null)).isEqualTo("1 h 1 min");

		d = Duration.ZERO.plusHours(1).plusMinutes(1).plusNanos(1000);
		assertThat(StringTools.prettyPrintDuration(d, true, null)).isEqualTo("1 h 1 min 1000 ns");

		d = Duration.ZERO.plusHours(1).plusNanos(1000);
		assertThat(StringTools.prettyPrintDuration(d, true, null)).isEqualTo("1 h 1000 ns");

		d = Duration.ZERO.plusHours(1).plusMinutes(1).plusNanos(1000);
		assertThat(StringTools.prettyPrintDuration(d, true, ChronoUnit.MINUTES)).isEqualTo("1 h 1 min");

		d = Duration.ZERO.plusMillis(1);
		assertThat(StringTools.prettyPrintDuration(d, true, null)).isEqualTo("1 ms");

		d = Duration.ZERO.plusMillis(1);
		assertThat(StringTools.prettyPrintDuration(d, true, ChronoUnit.MINUTES)).isEqualTo("0 min");
	}

	@Test
	public void testPrettyPrintDurationWithoutUnits() {
		Duration d;

		d = Duration.ZERO.plusHours(1);
		assertThat(StringTools.prettyPrintDuration(d, false, null)).isEqualTo("1:00:00:000:000000");

		d = Duration.ZERO.plusHours(1).plusNanos(12345);
		assertThat(StringTools.prettyPrintDuration(d, false, null)).isEqualTo("1:00:00:000:012345");

		d = Duration.ZERO.plusNanos(12345);
		assertThat(StringTools.prettyPrintDuration(d, false, null)).isEqualTo("12345");

		d = Duration.ZERO.plusMinutes(1).plusNanos(12345);
		assertThat(StringTools.prettyPrintDuration(d, false, null)).isEqualTo("1:00:000:012345");

		d = Duration.ZERO.plusMinutes(1).plusNanos(12345);
		assertThat(StringTools.prettyPrintDuration(d, false, ChronoUnit.SECONDS)).isEqualTo("1:00");

		d = Duration.ZERO.plusMillis(1);
		assertThat(StringTools.prettyPrintDuration(d, false, null)).isEqualTo("1:000000");
	}

	@Test
	public void testAsciiBoxMessage() {

		assertThat(StringTools.asciiBoxMessage("Hello, world!", -1))
				.isEqualTo("/---------------\\\n" + "| Hello, world! |\n" + "\\---------------/\n");
		assertThat(StringTools.asciiBoxMessage("Hello, world!", 5))
				.isEqualTo("/-------\\\n" + "| Hello |\n" + "| , wor |\n" + "| ld!   |\n" + "\\-------/\n");

		assertThat(StringTools.asciiBoxMessage("Hello\nworld!", 0)).isEqualTo("/--------\\\n" + "| Hello  |\n" + "| world! |\n" + "\\--------/\n");

		assertThat(StringTools.asciiBoxMessage("Hello\nworld!", 100)).isEqualTo("/--------\\\n" + "| Hello  |\n" + "| world! |\n" + "\\--------/\n");
	}

	@Test
	public void testToAndFromHex() throws Exception {

		assertThat(StringTools.toHex("a".getBytes("UTF-8"))).isEqualTo("61");
		assertThat(StringTools.toHex("Hello, world!".getBytes("UTF-8"))).isEqualTo("48656c6c6f2c20776f726c6421");

		String hexRepresentation = StringTools.toHex("Hello, world".getBytes(StandardCharsets.UTF_8));
		byte[] bytes = StringTools.fromHex(hexRepresentation);
		String actual = new String(bytes, StandardCharsets.UTF_8);
		assertThat(actual).isEqualTo("Hello, world");

		Random r = new Random();
		for (int i = 0; i < Numbers.THOUSAND; ++i) {
			int size = r.nextInt(Numbers.THOUSAND) + 1;
			byte[] array = new byte[size];
			r.nextBytes(array);

			hexRepresentation = StringTools.toHex(array);
			byte[] actualArray = StringTools.fromHex(hexRepresentation);
			assertThat(actualArray).isEqualTo(array);
		}
	}

	@Test
	public void testSplitStringBySize() {
		assertThat(StringTools.splitStringBySize("1234567890", 5)).hasSameElementsAs(asList("12345", "67890"));
		assertThat(StringTools.splitStringBySize("1234567890", 10)).hasSameElementsAs(asList("1234567890"));
		assertThat(StringTools.splitStringBySize("1234567890", 1)).hasSameElementsAs(asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"));
		assertThat(StringTools.splitStringBySize("1234567890", 100)).hasSameElementsAs(asList("1234567890"));
		assertThat(StringTools.splitStringBySize("", 100)).isEmpty();
		try {
			StringTools.splitStringBySize("fail", -1);
			fail("This should have thrown an exception.");
		} catch (IllegalArgumentException expected) {
			// do nothing
		}
		assertThat(StringTools.splitStringBySize(null, 100)).isEmpty();
		assertThat(StringTools.splitStringBySize(null, -1)).isEmpty();
	}

	@Test
	public void testRemoveDiacritics() {

		String input = "H\u00e9llo World"; // LATIN SMALL LETTER E WITH ACUTE
		assertThat(StringTools.removeDiacritics(input)).isEqualTo("Hello World");

		input = "\u00DDMCA"; // LATIN CAPITAL LETTER Y WITH ACUTE
		assertThat(StringTools.removeDiacritics(input)).isEqualTo("YMCA");

		input = "Ind\u00EFa"; // LATIN SMALL LETTER I WITH DIAERESIS
		assertThat(StringTools.removeDiacritics(input)).isEqualTo("India");

		// Note: this is not supposed to work. Leaving this test here anyway for clarification
		input = "I\u0397TA"; // GREEK CAPITAL LETTER ETA
		assertThat(StringTools.removeDiacritics(input)).isNotEqualTo("IHTA");
	}

	@Test
	public void testRemoveNonPrintableCharacters() {

		String input = "H\u00e9llo World"; // LATIN SMALL LETTER E WITH ACUTE
		assertThat(StringTools.removeNonPrintableCharacters(input)).isEqualTo("Hllo World");

		input = "\u00DDMCA"; // LATIN CAPITAL LETTER Y WITH ACUTE
		assertThat(StringTools.removeNonPrintableCharacters(input)).isEqualTo("MCA");

		input = "Ind\u00EFa"; // LATIN SMALL LETTER I WITH DIAERESIS
		assertThat(StringTools.removeNonPrintableCharacters(input)).isEqualTo("Inda");

		input = "I\u0397TA"; // GREEK CAPITAL LETTER ETA
		assertThat(StringTools.removeNonPrintableCharacters(input)).isEqualTo("ITA");
	}

	@Test
	public void testCamelCaseToDashSeparated() {

		assertThat(StringTools.camelCaseToDashSeparated("HelloWorld")).isEqualTo("hello-world");
		assertThat(StringTools.camelCaseToDashSeparated("helloWorld")).isEqualTo("hello-world");
		assertThat(StringTools.camelCaseToDashSeparated("helloworld")).isEqualTo("helloworld");
		assertThat(StringTools.camelCaseToDashSeparated("Elastic.Default")).isEqualTo("elastic.default");

		assertThat(StringTools.camelCaseToDashSeparated("")).isEqualTo("");
		assertThat(StringTools.camelCaseToDashSeparated(null)).isNull();

	}

	@Test
	public void testPrettyPrintDoubleMillisecondsWithUnits() {
		assertThat(StringTools.prettyPrintMilliseconds(0d, true, null)).isEqualTo("0 ms");
		assertThat(StringTools.prettyPrintMilliseconds(50d, true, null)).isEqualTo("50 ms");
		assertThat(StringTools.prettyPrintMilliseconds(500d, true, null)).isEqualTo("500 ms");
		assertThat(StringTools.prettyPrintMilliseconds(1000d, true, null)).isEqualTo("1 s");
		assertThat(StringTools.prettyPrintMilliseconds(1001d, true, null)).isEqualTo("1 s 1 ms");
		assertThat(StringTools.prettyPrintMilliseconds(60000d, true, null)).isEqualTo("1 min");
		assertThat(StringTools.prettyPrintMilliseconds(120000d, true, null)).isEqualTo("2 min");
		assertThat(StringTools.prettyPrintMilliseconds(120050d, true, null)).isEqualTo("2 min 50 ms");
		assertThat(StringTools.prettyPrintMilliseconds(3600000d, true, null)).isEqualTo("1 h");
		assertThat(StringTools.prettyPrintMilliseconds(3600001d, true, null)).isEqualTo("1 h 1 ms");
		assertThat(StringTools.prettyPrintMilliseconds(86400000d, true, null)).isEqualTo("1 d");
		assertThat(StringTools.prettyPrintMilliseconds(86400010d, true, null)).isEqualTo("1 d 10 ms");

		assertThat(StringTools.prettyPrintMilliseconds(0d, true, ChronoUnit.MILLIS)).isEqualTo("0 ms");
		assertThat(StringTools.prettyPrintMilliseconds(50d, true, ChronoUnit.MILLIS)).isEqualTo("50 ms");
		assertThat(StringTools.prettyPrintMilliseconds(500d, true, ChronoUnit.MILLIS)).isEqualTo("500 ms");
		assertThat(StringTools.prettyPrintMilliseconds(1000d, true, ChronoUnit.MILLIS)).isEqualTo("1 s");
		assertThat(StringTools.prettyPrintMilliseconds(1001d, true, ChronoUnit.MILLIS)).isEqualTo("1 s 1 ms");
		assertThat(StringTools.prettyPrintMilliseconds(60000d, true, ChronoUnit.MILLIS)).isEqualTo("1 min");
		assertThat(StringTools.prettyPrintMilliseconds(120000d, true, ChronoUnit.MILLIS)).isEqualTo("2 min");
		assertThat(StringTools.prettyPrintMilliseconds(120050d, true, ChronoUnit.MILLIS)).isEqualTo("2 min 50 ms");
		assertThat(StringTools.prettyPrintMilliseconds(3600000d, true, ChronoUnit.MILLIS)).isEqualTo("1 h");
		assertThat(StringTools.prettyPrintMilliseconds(3600001d, true, ChronoUnit.MILLIS)).isEqualTo("1 h 1 ms");
		assertThat(StringTools.prettyPrintMilliseconds(86400000d, true, ChronoUnit.MILLIS)).isEqualTo("1 d");
		assertThat(StringTools.prettyPrintMilliseconds(86400010d, true, ChronoUnit.MILLIS)).isEqualTo("1 d 10 ms");

		assertThat(StringTools.prettyPrintMilliseconds(0.1d, true, ChronoUnit.NANOS)).isEqualTo("0.1 ms");
		assertThat(StringTools.prettyPrintMilliseconds(0.01d, true, ChronoUnit.NANOS)).isEqualTo("0.01 ms");
		assertThat(StringTools.prettyPrintMilliseconds(0.001d, true, ChronoUnit.NANOS)).isEqualTo("1000 ns");
		assertThat(StringTools.prettyPrintMilliseconds(0.0001d, true, ChronoUnit.NANOS)).isEqualTo("100 ns");
		assertThat(StringTools.prettyPrintMilliseconds(0.00001d, true, ChronoUnit.NANOS)).isEqualTo("10 ns");

	}

	@Test
	public void testSubstringByUtf8BytesLength() {
		assertThat(StringTools.substringByUtf8BytesLength(null, 0)).isNull();
		assertThat(StringTools.substringByUtf8BytesLength("h", 0)).isEqualTo("");
		assertThat(StringTools.substringByUtf8BytesLength("hello world", 5)).isEqualTo("hello");
		assertThat(StringTools.substringByUtf8BytesLength("\u0ca1", 1)).isEqualTo("");
		assertThat(StringTools.substringByUtf8BytesLength("\u0ca1\u0ca4", 3)).isEqualTo("\u0ca1");
		assertThat(StringTools.substringByUtf8BytesLength("\u0ca1\u0ca4", 4)).isEqualTo("\u0ca1");
		assertThat(StringTools.substringByUtf8BytesLength("\u0ca1\u0ca4", 6)).isEqualTo("\u0ca1\u0ca4");
		assertThat(StringTools.substringByUtf8BytesLength("\u0ca1\u0ca4", 7)).isEqualTo("\u0ca1\u0ca4");
	}
}
