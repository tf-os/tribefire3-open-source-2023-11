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
package com.braintribe.mimetype;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Parses a qualified mime type into a {@link ParsedMimeType}.
 *
 * @see ParsedMimeType
 *
 */
public class MimeTypeParser {

	public static ParsedMimeType getParsedMimeType(String qualifiedMimeType) {
		StringTokenizer tokens = new StringTokenizer(qualifiedMimeType, ";,");
		Map<String, String> params = new HashMap<>();

		String mimeType = null;
		String mediaType = null;
		String subType = null;

		boolean first = true;

		while (tokens.hasMoreElements()) {
			String token = tokens.nextToken();
			if (first) {
				first = false;
				mimeType = token.trim();

				int index = token.indexOf('/');
				mediaType = index != -1 ? token.substring(0, index) : token;
				subType = index != -1 ? token.substring(index + 1) : "";

			} else {
				token = token.trim();
				int index = token.indexOf('=');
				if (index != -1) {
					String key = token.substring(0, index);
					String value = token.substring(index + 1);

					params.put(key, value);
				}
			}
		}

		return new ParsedMimeType(mimeType, mediaType, subType, params);
	}

}
