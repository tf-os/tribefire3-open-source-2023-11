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
/**
 *
 */
package com.braintribe.utils;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

/**
 *
 */
public class FileToolsShortenPathTest {

	@Test
	public void testPathShorterThanTargetLength() {
		String testString = "/this/is/a/test/path";

		assertThat(FileTools.shortenPath(testString, 40)).isEqualTo(testString);
	}

	@Test
	public void testPathEqualToTargetLength() {
		String testString = "/five/five/five/five/five/five/five/five";

		assertThat(FileTools.shortenPath(testString, 40)).isEqualTo(testString);
	}

	@Test
	public void testPathZeroLength() {
		String testString = "";

		assertThat(FileTools.shortenPath(testString, 0)).isEqualTo("");
	}

	@Test
	public void testPathDotLength() {
		String testString = "/five/five/five/five/five/five/five/five";
		assertThat(FileTools.shortenPath(testString, 0)).isEqualTo("");

		testString = "/five/five/five/five/five/five/five/five";
		assertThat(FileTools.shortenPath(testString, 1)).isEqualTo(".");

		testString = "/five/five/five/five/five/five/five/five";
		assertThat(FileTools.shortenPath(testString, 2)).isEqualTo("..");

		testString = "/five/five/five/five/five/five/five/five";
		assertThat(FileTools.shortenPath(testString, 3)).isEqualTo("...");
	}

	@Test
	public void testPathBoundaryLength() {
		String testString = "/afbd/afbd/afbd/afbd/afbd/afbd/afbda/fbaafs/bd/afbd/afbd/afbd/afbd/afbd/afbd/afd";

		assertThat(FileTools.shortenPath(testString, 74)).isEqualTo("/afbd/afbd/afbd/afbd/afbd/afbd/afb.../bd/afbd/afbd/afbd/afbd/afbd/afbd/afd");
	}

	@Test
	public void testPathLength() {
		String testString = "/afbd/afbd/afbd/afbd/afbd/afbd/afbda/fbaafs/bd/afbd/afbd/afbd/afbd/afbd/afbd/afd";

		assertThat(FileTools.shortenPath(testString, 78)).isEqualTo("/afbd/afbd/afbd/afbd/afbd/afbd/afbda/..../bd/afbd/afbd/afbd/afbd/afbd/afbd/afd");
	}

	@Test
	public void testPathLengthFillerLength() {
		String testString = "/afbd/afbd/afbd/afbd/afbd/afbd/afbda/fbaafs/bd/afbd/afbd/afbd/afbd/afbd/afbd/afd";

		assertThat(FileTools.shortenPath(testString, 61)).isEqualTo("/afbd/afbd/afbd/afbd/afbd/af...d/afbd/afbd/afbd/afbd/afbd/afd");
	}

	@Test
	public void testPathWithoutFileSeparators() {
		String testString = "blaskdfjlskadnflskdnflasknlfdksnlkfnsalblaskdfjlskadnflskdnflasknlfdksnlkfnsalblaskdfjlskadnflskdnflasknlfdksnlkfnsalblaskdfjlskadnflskdnflasknlfdksnlkfnsal";

		assertThat(FileTools.shortenPath(testString, 41)).isEqualTo("blaskdfjlskadnflskd...flasknlfdksnlkfnsal");
	}

	@Test
	public void testPathWithBigFileNameCloseBoundary() {
		String testString = "../sadfjs/ölkajsdölfkjaösldkjföalssdjkföalskjfdölasadfjö/lka/jsdölfkjaösldkjföalsdjkföalskjfdölsasadfjölkajsdö/l/f&/k/j/aösldkjföalsdjkföalskjfdölsasadfjölkajs/dölfkjaösldkjföalsdjkföalskjfdölsasadfjöl/kajsdölfkjaösldkjföalsdjkföals/kjfdölsa";

		assertThat(FileTools.shortenPath(testString, 80))
				.isEqualTo("../sadfjs/ölkajsdölfkjaö...fdölsasadfjöl/kajsdölfkjaösldkjföalsdjkföals/kjfdölsa");
	}

	@Test
	public void testPathWithBigFileNameCutBoundary() {
		String testString = "../sadfjs/ölkajsdölfkjaösldkjföalssdjkföalskjfdölasadfjö/lka/jsdölfkjaösldkjföalsdjkföalskjfdölsasadfjölkajsdö/l/f&/k/j/aösldkjföalsdjkföalskjfdölsasadfjölkajs/dölfkjaösldkjföalsdjkföalskjfdölsasadfjöl/kajsdölfkjaösldkjföalsdjkföals/kjfdölsa";

		assertThat(FileTools.shortenPath(testString, 60)).isEqualTo("../sadfjs/ölkajsdölfkjaösldkj...ösldkjföalsdjkföals/kjfdölsa");
	}

}
