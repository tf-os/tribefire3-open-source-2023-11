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
package com.braintribe.testing.junit.assertions.assertj.core.api;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThatExecuting;

import org.junit.Test;

import com.braintribe.testing.test.AbstractTest;

/**
 * Provides {@link SharedCharSequenceAssert} related tests.
 *
 * @author michael.lafite
 */
public class SharedCharSequenceAssertTest extends AbstractTest {

	@Test
	public void test() {

		// order irrelevant
		assertThat("abcde").containsAll("c", "de", "a");

		// order relevant
		assertThat("abcde").containsSubsequence("a", "cd", "e");
		assertThatExecuting(() -> assertThat("abcde").containsSubsequence("a", "e", "cd")).fails();

		// order relevant, no elements in between allowed
		assertThat("abcde").containsSequence("a", "b", "cd", "e");
		assertThatExecuting(() -> assertThat("abcde").containsSequence("a", "cd", "e")).fails();

		assertThat("abc").hasSameSizeAs("def");

		assertThat("aaba").containsNTimes("a", 3).containsNTimes("b", 0, 1).containsAtMostNTimes("c", 0);

		assertThatExecuting(() -> assertThat("test\r\n").isEqualToWithVerboseErrorMessage("test\n")).fails().throwingThrowableWhich()
				.isInstanceOf(AssertionError.class).hasMessageContaining("code 13");

		assertThatExecuting(() -> assertThat("test\r\n").isEqualToWithVerboseErrorMessageAndLogging("test\n")).fails().throwingThrowableWhich()
				.isInstanceOf(AssertionError.class).hasMessageContaining("code 13");
	}

}
