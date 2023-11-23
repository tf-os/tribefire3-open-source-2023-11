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
package tribefire.extension.spreadsheet.processing.importing.common;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexMapper implements Function<String, String> {
	private Pattern pattern;
	private String template;
	
	public RegexMapper(String patternString, String template) {
		super();
		this.pattern = Pattern.compile(patternString);
		this.template = template;
	}
	
	@Override
	public String apply(String s) {
		Matcher matcher = pattern.matcher(s);
		
		if (matcher.find()) {
			return matcher.replaceFirst(template);
		}
		else {
			return s;
		}
	}
}
