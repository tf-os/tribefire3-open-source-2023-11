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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.braintribe.filter.pattern.PatternMatcher;
import com.braintribe.filter.pattern.Range;
import com.braintribe.utils.lcd.StringTools;

/**
 *
 */
public class CamelCasePatternMatcher implements PatternMatcher {

	/**
	 * Matches texts supporting camel-case like search/code completion in eclipse does.
	 */
	@Override
	public List<Range> matches(final String pattern, final String text) {
		if (pattern == null) {
			final Range range = new Range(0, 0);
			return Collections.singletonList(range);
		}

		final List<String> partialPatterns = StringTools.splitCamelCase(pattern);

		String remainingText = text;
		int offset = 0;
		final List<Range> result = new ArrayList<>();

		for (final String partialPattern : partialPatterns) {
			if (!remainingText.startsWith(partialPattern)) {
				return null;
			}

			result.add(new Range(offset, partialPattern.length()));

			offset += partialPattern.length();
			remainingText = remainingText.substring(partialPattern.length());

			final int firstCapitalPosition = StringTools.findFirstCapitalPosition(remainingText);

			if (firstCapitalPosition < 0) {
				remainingText = "";
			} else {
				offset += firstCapitalPosition;
				remainingText = remainingText.substring(firstCapitalPosition);
			}
		}

		return result;
	}

}
