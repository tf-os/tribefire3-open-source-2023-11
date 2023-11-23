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
package com.braintribe.web.multipart.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.braintribe.web.multipart.api.MultipartFormat;

public class MultipartMimetypeParser {
	public static MultipartFormat parse(String mimeType) {
		String parts[] = mimeType.split("(,|;)");
		Map<String, String> params = null;
		
		String simpleMimeType = null;
		MultipartSubFormat subFormat = null;
		
		boolean first = true;
		for (String part : parts) {
			if (first) {
				first = false;
				simpleMimeType = part.trim();

				int index = simpleMimeType.indexOf('/');
				String subType = index != -1? simpleMimeType.substring(index + 1): "";
				
				switch (subType) {
					case "form-data":
						subFormat = MultipartSubFormat.formData;
						break;
					case "chunked":
						subFormat = MultipartSubFormat.chunked;
						break;
					default:
						subFormat = MultipartSubFormat.none;
						break;
				}
			}
			else {
				part = part.trim();
				int index = part.indexOf('=');
				if (index != -1) {
					String key = part.substring(0, index);
					String value = part.substring(index + 1);
					
					if (params == null)
						params = new HashMap<String, String>();
					params.put(key, value);
				}
			}
		}
		
		return new BasicMultipartFormat(simpleMimeType, subFormat, params != null? params: Collections.emptyMap());
	}
}
