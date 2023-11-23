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
package com.braintribe.utils.lcd.string;

import static com.braintribe.utils.lcd.string.StringDistance.damerauLevenshteinDistance;
import static com.braintribe.utils.lcd.string.StringDistance.levenshteinDistance;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

/**
 * @author peter.gazdik
 */
public class StringDistanceTest {

	@Test
	public void damerauLevenshtein() {
		assertDamerauLevenshtein("", "", 0);
		assertDamerauLevenshtein("abc", "abc", 0);
		assertDamerauLevenshtein("a", "A", 1);
		assertDamerauLevenshtein("abc", "ab", 1);
		assertDamerauLevenshtein("ab", "ba", 1);
		assertDamerauLevenshtein("-ab-", "-ba-", 1);
		assertDamerauLevenshtein("-ab-cd-", "-ba-dc-", 2);
		assertDamerauLevenshtein("-ab-", "-bac-", 2);
		assertDamerauLevenshtein("ca", "abc", 3);
		assertDamerauLevenshtein("abc", "ca", 3);
	}

	private void assertDamerauLevenshtein(String s, String t, int expectedDiff) {
		int actualDiff = damerauLevenshteinDistance(s, t);
		assertThat(actualDiff).as("Wrong distance of [" + s + "] and [" + t + "]").isEqualTo(expectedDiff);
	}

	@Test
	public void levenshtein() {
		assertLevenshtein("", "", 0);
		assertLevenshtein("a", "", 1);
		assertLevenshtein("pit", "pot", 1);
		assertLevenshtein("brainon", "braintribe", 5);
		assertLevenshtein("braintribe", "brainon", 5);
	}

	private void assertLevenshtein(String s, String t, int expectedDiff) {
		int actualDiff = levenshteinDistance(s, t);
		assertThat(actualDiff).as("Wrong distance of [" + s + "] and [" + t + "]").isEqualTo(expectedDiff);
	}

}
