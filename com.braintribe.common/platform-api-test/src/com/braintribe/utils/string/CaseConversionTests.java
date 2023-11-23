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
package com.braintribe.utils.string;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.braintribe.utils.StringTools;
import com.braintribe.utils.string.caseconvert.CaseConversionSplitter;
import com.google.common.base.Function;

/**
 * @author peter.gazdik
 */
public class CaseConversionTests {

	@Test
	public void testItWorks() throws Exception {
		assertConversion("HelloWorld", "hello-world", s -> s.splitCamelCase().uncapitalizeAll().join("-"));
		assertConversion("HelloWorld", "hello-World", s -> s.splitCamelCase().uncapitalizeFirst().join("-"));
		assertConversion("hello-World", "helloWorld", s -> s.splitOnDelimiter("-").joinWithoutdelimiter());
		assertConversion("hello-world", "helloWorld", s -> s.splitOnDelimiter("-").capitalizeAllButFirst().joinWithoutdelimiter());

		assertConversion("HelloTHISWorld", "hello-this-world", s -> s.splitCamelCaseSmart().toLowerCase().join("-"));
		assertConversion("HelloTHISWorld", "HELLO_THIS_WORLD", s -> s.splitCamelCaseSmart().toUpperCase().join("_"));
	}

	private void assertConversion(String input, String expectedResult, Function<CaseConversionSplitter, String> conversionF) {
		CaseConversionSplitter convertCase = StringTools.convertCase(input);
		String result = conversionF.apply(convertCase);

		assertThat(result).as("Wrong conversion for input: " + input).isEqualTo(expectedResult);
	}

}
