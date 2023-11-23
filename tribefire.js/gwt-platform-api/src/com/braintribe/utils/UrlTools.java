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
package com.braintribe.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

public class UrlTools {

	public static String encodeQuery(final Map<String, String> parameterMap) throws UnsupportedEncodingException {
		final StringBuilder sb = new StringBuilder();
		for (final Map.Entry<String, String> entry : parameterMap.entrySet()) {
			final String encodedKey = URLEncoder.encode(entry.getKey(), "UTF-8");
			final String encodedVal = URLEncoder.encode(entry.getValue(), "UTF-8");
			if (sb.length() == 0) {
				sb.append("?");
			} else {
				sb.append("&");
			}
			sb.append(encodedKey);
			sb.append("=");
			sb.append(encodedVal);
		}
		return sb.toString();
	}
}
