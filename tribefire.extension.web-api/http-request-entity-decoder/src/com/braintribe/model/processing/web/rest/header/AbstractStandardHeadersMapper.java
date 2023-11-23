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
package com.braintribe.model.processing.web.rest.header;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.web.rest.StandardHeadersMapper;

/**
 * Base class for mappers' implementations.
 * 
 */
public abstract class AbstractStandardHeadersMapper<T extends GenericEntity> implements StandardHeadersMapper<T> {

	/**
	 * This method transforms a header name with format "-My-Header-Name" or "My-Header-Name" to "myHeaderName".
	 * 
	 * @param headerName the header name, must not be {@code null}
	 * 
	 * @return the corresponding property name, never {@code null}
	 */
	public String getPropertyNameFromStandardHeaderName(String headerName) {
		StringBuilder sb = new StringBuilder(headerName.length());
		boolean upperCase = false;
		boolean first = true;
		for (int i = 0; i < headerName.length(); i++) {
			final char character = headerName.charAt(i);
			if(character == '-') {
				// do not append, next character upper case
				upperCase = true;
			} else if (first) {
				// first character always lower case, even if it's after some dashes
				first = false;
				upperCase = false;
				sb.append(Character.toLowerCase(character));
			} else if (upperCase) {
				// after a dash, upper case
				upperCase = false;
				sb.append(Character.toUpperCase(character));
			} else {
				// lower case/sanitize the rest
				sb.append(Character.toLowerCase(character));
			}
		}
		
		return sb.toString();
	}
}
