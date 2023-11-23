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
package com.google.gwt.dev.js;

/**
 * @author peter.gazdik
 */
public class NamerTools {

	public static String ensureFirstCharIsNotValidForPropertyNames(String s) {
		char firstChar = s.charAt(0);

		if (Character.isLowerCase(firstChar)) {
			return Character.toUpperCase(firstChar) + s.substring(1);
		} else {
			if (firstChar == '$' || firstChar == '_') {
				s = "A" + s;
			}
			
			return s;
		}
	}

	public static boolean ensureFirstCharNotLower() {
		return true;
	}
	
	public static String fixGenericIssue(String prefix) {
		if (prefix.endsWith(">")) {
			int index = prefix.lastIndexOf("<");
			prefix = prefix.substring(0, index);
		}

		return prefix;
	}

}
