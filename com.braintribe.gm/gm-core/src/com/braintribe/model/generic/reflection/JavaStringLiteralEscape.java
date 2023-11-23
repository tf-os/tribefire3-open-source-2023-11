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
package com.braintribe.model.generic.reflection;

public class JavaStringLiteralEscape {
	/**
	 * Escapes string content to be a valid string literal.
	 * 
	 * @return an escaped version of <code>unescaped</code>, suitable for being enclosed in double quotes in Java source
	 */
	public static String escape(String unescaped) {
		int extra = 0;
		for (int in = 0, n = unescaped.length(); in < n; ++in) {
			switch (unescaped.charAt(in)) {
				case '\0':
				case '\n':
				case '\r':
				case '\"':
				case '\\':
					++extra;
					break;
			}
		}

		if (extra == 0) {
			return unescaped;
		}

		char[] oldChars = unescaped.toCharArray();
		char[] newChars = new char[oldChars.length + extra];
		for (int in = 0, out = 0, n = oldChars.length; in < n; ++in, ++out) {
			char c = oldChars[in];
			switch (c) {
				case '\0':
					newChars[out++] = '\\';
					c = '0';
					break;
				case '\n':
					newChars[out++] = '\\';
					c = 'n';
					break;
				case '\r':
					newChars[out++] = '\\';
					c = 'r';
					break;
				case '\"':
					newChars[out++] = '\\';
					c = '"';
					break;
				case '\\':
					newChars[out++] = '\\';
					c = '\\';
					break;
			}
			newChars[out] = c;
		}

		return String.valueOf(newChars);
	}
}
