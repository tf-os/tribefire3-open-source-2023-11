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
package tribefire.extension.modelling.processing.tools;

import com.braintribe.utils.StringTools;

public interface ExternalIdComputer {

	static String buildExternalId(String name) {
		String externalId = name;
		if (name != null) {
			externalId = StringTools.removeLeadingWhitespace(name);
			externalId = StringTools.removeTrailingWhitespace(name);
			externalId = StringTools.replaceWhiteSpace(externalId, "-");
			externalId = StringTools.removeDiacritics(externalId);
			externalId = StringTools.removeNonPrintableCharacters(externalId);
			externalId = replaceNonAlphaNumericOrDashCharacters(externalId);
			externalId = removeLeadingOrTrailingDashes(externalId);
			externalId = externalId.toLowerCase();
		}
		return externalId;
	}
	
	static String replaceNonAlphaNumericOrDashCharacters(String name) {
		StringBuilder b = new StringBuilder();
		name.chars()
			.forEach(c -> {
				if (isAlphaNumericCharacter(c) || isDashCharacter(c)) {
					b.append((char)c);
				} else {
					b.append('-');
				}
			});
		return b.toString();
	}
	
	static boolean containsAlphaNumericCharacters(String compare) {
		return compare.chars()
			.filter(c -> isAlphaNumericCharacter(c))
			.findAny()
			.isPresent();
	}

	static boolean isAlphaNumericCharacter(int c) {
		return Character.isLetter(c) || Character.isDigit(c);
	}
	
	static boolean isDashCharacter(int c) {
		return c == '-';
	}
	
	static String removeLeadingOrTrailingDashes(String name) {
		name = name.replaceAll("^\\-+", "");
		name = name.replaceAll("\\-$", "");
		return name;
	}
	
}
