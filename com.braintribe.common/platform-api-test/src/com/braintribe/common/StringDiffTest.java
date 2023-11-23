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
package com.braintribe.common;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.braintribe.common.StringDiff.DiffResult;

/**
 * Provides tests for {@link StringDiff}.
 *
 * @author michael.lafite
 */
public class StringDiffTest {

	@Test
	public void testNoDifference() {
		assertNoDifference("");
		assertNoDifference("a");
		assertNoDifference("abcdef0392483245\t+ä#.ö+ß´ß313.-.<->ÄÖ'*Ü?=!\"§$%&/()=?");
		assertNoDifference("\n");
		assertNoDifference("\n\n");
		assertNoDifference("\na\nb");
		assertNoDifference("\na\nb\n");
		assertNoDifference("\r\na\r\nb\r\nc\nd\rc");
	}

	@Test
	public void testDifference() {
		assertDifference("", "x", "y", 0, 1, 0);
		assertDifference("ab", "x", "y", 2, 1, 0);
		assertDifference("abc\n", "x", "y", 4, 2, 4);
		assertDifference("abc\nd", "x", "y", 5, 2, 4);

		assertDifference("\n", "x", "y", 1, 2, 1);
		assertDifference("\n\n", "x", "y", 2, 3, 2);

		assertDifference("abc", "\n", "y", 3, 1, 0);
		assertDifference("a\nb", "\n", "y", 3, 2, 2);

		assertDifference("test", "\r\n", "\n", 4, 1, 0);

		assertThat(assertDifference("", "", "x", 0, 1, 0).getFirstDifferenceDescription()).contains(StringDiff.END_OF_STRING);
		assertThat(assertDifference("abc", "", "x", 3, 1, 0).getFirstDifferenceDescription()).contains(StringDiff.END_OF_STRING);
		assertThat(assertDifference("a\nb", "", "x", 3, 2, 2).getFirstDifferenceDescription()).contains(StringDiff.END_OF_STRING);
		assertThat(assertDifference("a\nb", "", "\nxy", 3, 2, 2).getFirstDifferenceDescription()).contains(StringDiff.END_OF_STRING);
	}

	private static DiffResult assertDifference(String commmonPrefix, String firstSuffix, String secondSuffix, Integer firstDifferentCharacterIndex,
			Integer firstDifferentLineNumber, Integer firstDifferentLineStartIndex) {
		// new string to avoid == check
		String first = new String(commmonPrefix + firstSuffix);
		String second = new String(commmonPrefix + secondSuffix);

		StringDiff diff = new StringDiff();
		DiffResult firstVsSecondDiffResult = diff.compare(first, second);
		DiffResult secondVsFirstDiffResult = diff.compare(second, first);

		assertValidResultPair(firstVsSecondDiffResult, secondVsFirstDiffResult);
		assertValidDifferenceResult(firstVsSecondDiffResult, firstDifferentCharacterIndex, firstDifferentLineNumber, firstDifferentLineStartIndex);

		return firstVsSecondDiffResult;
	}

	private static DiffResult assertNoDifference(String string) {
		// new string to avoid == check
		String first = new String(string);
		String second = new String(string);

		StringDiff diff = new StringDiff();
		DiffResult firstVsSecondDiffResult = diff.compare(first, second);
		DiffResult secondVsFirstDiffResult = diff.compare(second, first);

		assertValidResultPair(firstVsSecondDiffResult, secondVsFirstDiffResult);
		assertValidNoDifferenceResult(firstVsSecondDiffResult);

		return firstVsSecondDiffResult;
	}

	static void assertValidResultPair(DiffResult firstVsSecondDiffResult, DiffResult secondVsFirstDiffResult) {
		assertThat(firstVsSecondDiffResult.getFirst()).isEqualTo(secondVsFirstDiffResult.getSecond());
		assertThat(firstVsSecondDiffResult.getSecond()).isEqualTo(secondVsFirstDiffResult.getFirst());

		assertThat(firstVsSecondDiffResult.getFirstDifferentCharacterIndex()).isSameAs(secondVsFirstDiffResult.getFirstDifferentCharacterIndex());
		assertThat(firstVsSecondDiffResult.getFirstDifferentLineNumber()).isSameAs(secondVsFirstDiffResult.getFirstDifferentLineNumber());
		assertThat(firstVsSecondDiffResult.getFirstDifferentLineStartIndex()).isSameAs(secondVsFirstDiffResult.getFirstDifferentLineStartIndex());
	}

	static void assertValidDifferenceResult(DiffResult result, Integer firstDifferentCharacterIndex, Integer firstDifferentLineNumber,
			Integer firstDifferentLineStartIndex) {

		assertThat(result.hasDifference()).isTrue();

		assertThat(result.getFirstDifferentCharacterIndex()).isNotNull();
		assertThat(result.getFirstDifferentLineNumber()).isNotNull();
		assertThat(result.getFirstDifferentLineStartIndex()).isNotNull();
		String differenceDescription = result.getFirstDifferenceDescription();
		assertThat(differenceDescription).isNotNull();

		if (firstDifferentCharacterIndex != null) {
			assertThat(result.getFirstDifferentCharacterIndex()).isEqualTo(firstDifferentCharacterIndex);
		}
		if (firstDifferentLineNumber != null) {
			assertThat(result.getFirstDifferentLineNumber()).isEqualTo(firstDifferentLineNumber);
		}
		if (firstDifferentLineStartIndex != null) {
			assertThat(result.getFirstDifferentLineStartIndex()).isEqualTo(firstDifferentLineStartIndex);
		}

		if (firstDifferentCharacterIndex != null) {
			assertThat(differenceDescription).startsWith("First difference found at index " + firstDifferentCharacterIndex);
		}
		if (firstDifferentLineNumber != null && firstDifferentLineNumber > 1) {
			assertThat(differenceDescription).contains("line " + firstDifferentLineNumber);
		}
	}

	static void assertValidNoDifferenceResult(DiffResult result) {
		assertThat(result.hasDifference()).isFalse();
		assertThat(result.getFirstDifferenceDescription()).isNull();
		assertThat(result.getFirstDifferentLineNumber()).isNull();
		assertThat(result.getFirstDifferentCharacterIndex()).isNull();
		assertThat(result.getFirstDifferentLineStartIndex()).isNull();
	}

}
