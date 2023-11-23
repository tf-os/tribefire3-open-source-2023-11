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
package com.braintribe.utils.junit.assertions;

import org.fest.assertions.Assertions;
import org.fest.assertions.StringAssert;

import com.braintribe.common.StringDiff;
import com.braintribe.logging.Logger;
import com.braintribe.utils.CommonTools;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.lcd.NullSafe;

/**
 * An extension of {@link StringAssert} to provide further assertions not (yet) provided by FEST.
 *
 * @author michael.lafite
 *
 */
public class ExtendedStringAssert extends StringAssert {

	private static Logger logger = Logger.getLogger(ExtendedStringAssert.class);

	public ExtendedStringAssert(final String actual) {
		super(actual);
	}

	/**
	 * Asserts that the string contains all <code>searchedStrings</code>.
	 */
	public StringAssert containsAll(final String... searchedStrings) {
		for (String searchedString : NullSafe.array(searchedStrings)) {
			Assertions.assertThat(super.actual).contains(searchedString);
		}
		return this;
	}

	/**
	 * Asserts that the number of occurrences of the <code>searchedString</code> matches exactly the specified
	 * <code>expectedNumberOfOccurrences</code>.
	 */
	public StringAssert containsNTimes(final String searchedString, int expectedNumberOfOccurrences) {
		int actualNumberOfOccurrences = StringTools.countOccurrences(super.actual, searchedString);
		Assertions.assertThat(actualNumberOfOccurrences).isEqualTo(expectedNumberOfOccurrences);
		return this;
	}

	/**
	 * Similar to {@link #containsNTimes(String, int)}.
	 */
	public StringAssert containsAtLeastNTimes(final String searchedString, int minimumNumberOfOccurrences) {
		int actualNumberOfOccurrences = StringTools.countOccurrences(super.actual, searchedString);
		Assertions.assertThat(actualNumberOfOccurrences).isGreaterThanOrEqualTo(minimumNumberOfOccurrences);
		return this;
	}

	/**
	 * Similar to {@link #containsNTimes(String, int)}.
	 */
	public StringAssert containsAtMostNTimes(final String searchedString, int minimumNumberOfOccurrences) {
		int actualNumberOfOccurrences = StringTools.countOccurrences(super.actual, searchedString);
		Assertions.assertThat(actualNumberOfOccurrences).isLessThanOrEqualTo(minimumNumberOfOccurrences);
		return this;
	}

	/**
	 * Asserts that the two strings are equal. The method prints verbose error info containing both strings (also using line separators), if the
	 * strings are not equal.
	 */
	public StringAssert isEqualToWithVerboseErrorInfo(String expected) {
		if (!CommonTools.equalsOrBothNull(super.actual, expected)) {
			logger.error("Assertion failed: strings are not equal!\nActual:\n" + super.actual + "\nExpected:\n" + expected + "\n"
					+ new StringDiff().compare(super.actual, expected).getFirstDifferenceDescription());
		}
		return super.isEqualTo(expected);
	}

	/**
	 * Asserts that the {@link String#length() length} matches the <code>expected</code> length.
	 */
	public StringAssert hasLength(int expected) {
		Assertions.assertThat(super.actual.length()).isEqualTo(expected);
		return this;
	}
}
