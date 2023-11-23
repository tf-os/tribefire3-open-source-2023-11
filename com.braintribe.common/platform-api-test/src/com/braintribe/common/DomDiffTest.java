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

import org.junit.Test;

import com.braintribe.common.StringDiff.DiffResult;

/**
 * Provides tests for {@link DomDiff}.
 *
 * @author michael.lafite
 */
public class DomDiffTest {

	@Test
	public void test() {
		DomDiff diff = new DomDiff();
		diff.setFormattingEnabled(false);
		diff.setCommentsIncluded(true);

		assertNoDifference(diff, "<root/>");
		assertNoDifference(diff, "<root><a/></root>");
		assertNoDifference(diff, "<!-- comment 1 --><root><!-- comment 2 --><a/></root><!-- comment 3 -->");
		assertNoDifference(diff, "<root/>", "<root></root>");
		// attributes will be ordered, even if formatting is disabled
		assertNoDifference(diff, "<root a=\"1\" b=\"2\" />", "<root b=\"2\" a=\"1\" />");
		// newline at the end doesn't matter
		assertNoDifference(diff, "<root/>", "", "\n");

		assertDifference(diff, "<!-- comment --><root/>", "<root/>");
		assertDifference(diff, "<!-- comment --><root/>", "<!-- other comment --><root/>");

		diff.setCommentsIncluded(false);
		assertNoDifference(diff, "<!-- comment --><root/>", "<root/>");
		assertNoDifference(diff, "<!-- c1 --><root><!-- c2 --><a/><!-- c3 --></root><!-- c4 -->", "<root><a/></root>");

		assertDifference(diff, "<root><!-- c --> <a/> <!-- c --></root>", "<root><a/></root>");
		diff.setFormattingEnabled(true);
		assertNoDifference(diff, "<root><!-- c --> <a/> <!-- c --></root>", "<root><a/></root>");

		assertNoDifference(diff, "  \t \n \n <!-- aksjdh --> <root>\n\n\t<!-- c --> \t\t\t  <a>some text</a> <!-- c -->\t\r\n</root>",
				"<root><a>some text</a></root>");
	}

	private static DiffResult assertDifference(DomDiff diff, String first, String second) {
		return assertDifference(diff, "", first, second);
	}

	private static DiffResult assertDifference(DomDiff diff, String commmonPrefix, String firstSuffix, String secondSuffix) {
		// new string to avoid == check
		String first = new String(commmonPrefix + firstSuffix);
		String second = new String(commmonPrefix + secondSuffix);

		DiffResult firstVsSecondDiffResult = diff.compare(first, second);
		DiffResult secondVsFirstDiffResult = diff.compare(second, first);

		StringDiffTest.assertValidResultPair(firstVsSecondDiffResult, secondVsFirstDiffResult);
		StringDiffTest.assertValidDifferenceResult(firstVsSecondDiffResult, null, null, null);

		return firstVsSecondDiffResult;
	}

	private static DiffResult assertNoDifference(DomDiff diff, String string) {
		return assertNoDifference(diff, "", string, string);
	}

	private static DiffResult assertNoDifference(DomDiff diff, String first, String second) {
		return assertNoDifference(diff, "", first, second);
	}

	private static DiffResult assertNoDifference(DomDiff diff, String commmonPrefix, String firstSuffix, String secondSuffix) {
		// new string to avoid == check
		String first = new String(commmonPrefix + firstSuffix);
		String second = new String(commmonPrefix + secondSuffix);

		DiffResult firstVsSecondDiffResult = diff.compare(first, second);
		DiffResult secondVsFirstDiffResult = diff.compare(second, first);

		StringDiffTest.assertValidResultPair(firstVsSecondDiffResult, secondVsFirstDiffResult);
		StringDiffTest.assertValidNoDifferenceResult(firstVsSecondDiffResult);

		return firstVsSecondDiffResult;
	}

}
