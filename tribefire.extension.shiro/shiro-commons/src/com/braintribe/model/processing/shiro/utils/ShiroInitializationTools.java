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
package com.braintribe.model.processing.shiro.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.braintribe.utils.StringTools;

public class ShiroInitializationTools {

	public static Map<String, String> decodeMap(String listString) {
		if (!StringTools.isBlank(listString)) {
			String[] mapEntries = StringTools.splitSemicolonSeparatedString(listString, true);
			Map<String, String> result = new HashMap<>();
			for (String mapEntry : mapEntries) {
				int index = mapEntry.indexOf('=');
				if (index != -1) {
					String key = mapEntry.substring(0, index).trim();
					String value = mapEntry.substring(index + 1).trim();

					result.put(key, value);
				}
			}
			return result;
		} else {
			return null;
		}
	}

	public static Set<String> parseCollection(String listString) {
		if (!StringTools.isBlank(listString)) {
			String[] splitCommaSeparatedString = StringTools.splitCommaSeparatedString(listString, true);
			if (splitCommaSeparatedString != null && splitCommaSeparatedString.length > 0) {
				Set<String> set = new HashSet<>(Arrays.asList(splitCommaSeparatedString));
				return set;
			}
		}
		return null;
	}

}
