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
package com.braintribe.model.processing.query.eval.context;

import org.fest.assertions.BooleanAssert;
import org.junit.Test;

import com.braintribe.utils.junit.assertions.BtAssertions;

public class ConditionEvaluationToolsTest {

	private String pattern;

	@Test
	public void constant() throws Exception {
		pattern = "text";

		yes("text");
		no("atext");
	}

	@Test
	public void wildcardEnding() throws Exception {
		pattern = "text*";

		yes("text");
		yes("text.");
		yes("texta");
		yes("text*");
		no("atext");
	}

	@Test
	public void wildcardBeginning() throws Exception {
		pattern = "*text";

		yes("text");
		yes(".text");
		yes("atext");
		yes("*text");
		no("texta");
	}

	@Test
	public void singleCharWildcard() throws Exception {
		pattern = "t?xt";

		yes("text");
		yes("toxt");
		no("txt");
	}

	@Test
	public void escapedWildcard() throws Exception {
		pattern = "text\\*";

		yes("text*");
		no("text");
	}

	@Test
	public void escapedSingleCharWildcard() throws Exception {
		pattern = "text\\?";

		yes("text?");
		no("text.");
	}

	@Test
	public void specialPatternChar() throws Exception {
		pattern = "text[a-z]";

		yes("text[a-z]");
		no("texta");
	}

	@Test
	public void specialSqlChar2() throws Exception {
		pattern = "text_";

		yes("text_");
		no("text");
		no("text.");
	}

	private void yes(String s) {
		assertLike(s).isTrue();
	}

	private void no(String s) {
		assertLike(s).isFalse();
	}

	private BooleanAssert assertLike(String s) {
		return BtAssertions.assertThat(s.matches(ConditionEvaluationTools.convertToRegexPattern(pattern))).as("Wrong value for: " + s);
	}

}
