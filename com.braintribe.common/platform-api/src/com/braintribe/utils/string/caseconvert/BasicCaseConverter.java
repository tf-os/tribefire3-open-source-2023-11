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
package com.braintribe.utils.string.caseconvert;

import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.braintribe.provider.Box;
import com.braintribe.utils.lcd.StringTools;

/**
 * @author peter.gazdik
 */
public class BasicCaseConverter implements CaseConversionSplitter, CaseConversionMapper {

	private final String s;
	private Stream<String> splitsStream;

	public BasicCaseConverter(String s) {
		this.s = s == null ? "" : s;
	}

	// ##########################################################
	// ## . . . . . . . CaseConversionSplitter . . . . . . . . ##
	// ##########################################################

	@Override
	public CaseConversionMapper splitCamelCase() {
		splitsStream = StringTools.splitCamelCase(s).stream();
		return this;
	}

	@Override
	public CaseConversionMapper splitCamelCaseSmart() {
		splitsStream = StringTools.splitCamelCaseSmart(s).stream();
		return this;
	}

	@Override
	public CaseConversionMapper splitOnDelimiter(String delimiter) {
		return splitOnRegex(Pattern.quote(delimiter));
	}

	@Override
	public CaseConversionMapper splitOnRegex(String regex) {
		splitsStream = Stream.of(s.split(regex));
		return this;
	}

	// ##########################################################
	// ## . . . . . . . . CaseConversionMapper . . . . . . . . ##
	// ##########################################################

	@Override
	public CaseConversionMapper when(boolean condition) {
		return condition ? this : new IgnoringNextMappingMapper();
	}

	private class IgnoringNextMappingMapper implements CaseConversionMapper {
		// @formatter:off
		@Override public CaseConversionJoiner uncapitalizeAll() { return BasicCaseConverter.this; }
		@Override public CaseConversionJoiner capitalizeAll() { return BasicCaseConverter.this; }
		@Override public CaseConversionJoiner uncapitalizeFirst() { return BasicCaseConverter.this; }
		@Override public CaseConversionJoiner capitalizeAllButFirst() { return BasicCaseConverter.this; }
		@Override public CaseConversionJoiner toLowerCase() { return BasicCaseConverter.this; }
		@Override public CaseConversionJoiner toUpperCase() { return BasicCaseConverter.this; }
		@Override public CaseConversionJoiner map(Function<? super String, String> mapping) { return null; }

		@Override public CaseConversionMapper when(boolean condition) { return throwError("when"); }
		@Override public Stream<String> asStream() { return throwError("asStream"); }
		@Override public String join(String delimiter) { return throwError("join"); }

		private <T> T throwError(String method) { throw new IllegalStateException("Cannot call " + method + " after 'when' method was used."); }
		// @formatter:on
	}

	@Override
	public CaseConversionJoiner uncapitalizeAll() {
		return map(StringTools::uncapitalize);
	}

	@Override
	public CaseConversionJoiner capitalizeAll() {
		return map(StringTools::capitalize);
	}

	@Override
	public CaseConversionJoiner uncapitalizeFirst() {
		Box<Boolean> flag = Box.of(Boolean.TRUE);
		return map(s -> {
			if (flag.value) {
				flag.value = Boolean.FALSE;
				s = StringTools.uncapitalize(s);
			}
			return s;
		});
	}

	@Override
	public CaseConversionJoiner capitalizeAllButFirst() {
		Box<Boolean> flag = Box.of(Boolean.TRUE);
		return map(s -> {
			if (flag.value)
				flag.value = Boolean.FALSE;
			else
				s = StringTools.capitalize(s);
			return s;
		});
	}

	@Override
	public CaseConversionJoiner toLowerCase() {
		return map(String::toLowerCase);
	}

	@Override
	public CaseConversionJoiner toUpperCase() {
		return map(String::toUpperCase);
	}

	@Override
	public CaseConversionJoiner map(Function<? super String, String> mapping) {
		splitsStream = splitsStream.map(mapping);
		return this;
	}

	@Override
	public Stream<String> asStream() {
		return splitsStream;
	}

	// ##########################################################
	// ## . . . . . . . . CaseConversionJoiner . . . . . . . . ##
	// ##########################################################

	@Override
	public String join(String delimiter) {
		return splitsStream.collect(Collectors.joining(delimiter));
	}

}
