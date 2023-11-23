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
package com.braintribe.filter.lcd.pattern;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.braintribe.filter.pattern.Range;

/**
 *
 */
public class CamelCasePatternMatcherTests {

	CamelCasePatternMatcher m = new CamelCasePatternMatcher();

	@Test
	public void matches() {
		final String text = "AaaaBbbbCcccDddd";

		final List<Range> ranges = this.m.matches("AaBCccD", text);
		final List<Range> expectedRanges = toRanges(0, 2, 4, 1, 8, 3, 12, 1);

		assertThat(ranges).isEqualTo(expectedRanges);
	}

	@Test
	public void matches2() {
		final String text = "AaaaBbbbCcccDddd";

		final List<Range> ranges = this.m.matches("AaBCD", text);
		final List<Range> expectedRanges = toRanges(0, 2, 4, 1, 8, 1, 12, 1);

		assertThat(ranges).isNotNull().isEqualTo(expectedRanges);
	}

	@Test
	public void matchesLowercase() {
		final String text = "aaaaBbbbCcccDddd";

		final List<Range> ranges = this.m.matches("aaBCD", text);
		final List<Range> expectedRanges = toRanges(0, 2, 4, 1, 8, 1, 12, 1);

		assertThat(ranges).isNotNull().isEqualTo(expectedRanges);
	}

	@Test
	public void matchesWithOtherCharacters() {
		final String text = "Aaaa_0$-BbbbCcccDddd";

		final List<Range> ranges = this.m.matches("AaBCcc", text);
		final List<Range> expectedRanges = toRanges(0, 2, 8, 1, 12, 3);

		assertThat(ranges).isEqualTo(expectedRanges);
	}

	/**
	 * If the pattern is AaaBCcc, then there can be no other capital letter between "A" and "B", and "B" and "C".
	 */
	@Test
	public void notMatchesIfOtherCapitalBetween() {
		final String text = "AaaaXBbbbCcccDddd";

		final List<Range> ranges = this.m.matches("AaaBCcc", text);

		assertThat(ranges).isNullOrEmpty();
	}

	@Test
	public void notMatchesIfStartsWithLowercase() {
		final String text = "aaaaBbbbCcccDddd";

		final List<Range> ranges = this.m.matches("AaB", text);

		assertThat(ranges).isNullOrEmpty();
	}

	private static List<Range> toRanges(final Integer... r) {
		final List<Range> result = new ArrayList<>();

		for (int i = 0; i < r.length;) {
			result.add(new Range(r[i++], r[i++]));
		}

		return result;
	}
}
