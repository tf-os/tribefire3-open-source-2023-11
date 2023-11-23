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
package tribefire.cortex.testing.junit.classpathfinder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implements the Ant/Maven style classname matching algorithm.
 */
final public class JavaStyleClassnameMatcher {
	private static final Pattern WILDCARDS = Pattern.compile("\\*{1,2}");

	private final Pattern pattern;

	public JavaStyleClassnameMatcher(String pattern) {
		StringBuilder rx = new StringBuilder();
		rx.append("^");
		for (String part : splitIncludingSeparator(pattern)) {
			if (part.equals("**")) {
				rx.append(".*");
			} else if (part.equals("*")) {
				rx.append("[^\\.]*");
			} else {
				rx.append(Pattern.quote(part));
			}
		}
		rx.append("$");

		this.pattern = Pattern.compile(rx.toString());
	}

	private static Collection<String> splitIncludingSeparator(String input) {
		Collection<String> result = new ArrayList<String>();
		Matcher matcher = WILDCARDS.matcher(input);
		int start = 0;
		while (matcher.find()) {
			if (matcher.start() > start) {
				result.add(input.substring(start, matcher.start()));
			}
			result.add(matcher.group());
			start = matcher.end();
		}
		if (start < input.length()) {
			result.add(input.substring(start));
		}
		return result;
	}

	/**
	 * @param classname
	 *            the fully qualified classname to check for match.
	 * @return true if the classname matches the filter, false otherwise.
	 */
	boolean matches(String classname) {
		return pattern.matcher(classname).matches();
	}
}