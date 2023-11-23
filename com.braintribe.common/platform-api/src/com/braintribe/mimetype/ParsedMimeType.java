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

import java.util.Map;

import com.braintribe.utils.lcd.CommonTools;

/**
 * <p>
 * This class represents a parsed qualified mime type. A qualified mime type is a mime type that may be further specialized, for example:
 * <ul>
 * <li>text/html;spec=module-checks-response</li>
 * <li>text/html;charset=UTF-8</li>
 * <li>text/yaml;abc=def</li>
 * </ul>
 *
 * <p>
 * This further specialization is optional, but the purpose of this class and its related {@link MimeTypeParser} implementation is to handle exactly
 * such specifications.
 *
 * @see MimeTypeParser
 *
 */
public class ParsedMimeType {
	private String mimeType;
	private String mediaType;
	private String subType;
	private Map<String, String> params;

	public ParsedMimeType(String mimeType, String mediaType, String subType, Map<String, String> params) {
		this.mimeType = mimeType;
		this.mediaType = mediaType;
		this.subType = subType;
		this.params = params;
	}

	public String getMimeType() {
		return mimeType;
	}

	public String getMediaType() {
		return mediaType;
	}

	public String getSubType() {
		return subType;
	}

	public Map<String, String> getParams() {
		return params;
	}

	public boolean equalsPlain(ParsedMimeType other) {
		if (other == null) {
			return false;
		}
		return CommonTools.equalsOrBothNull(this.mediaType, other.mediaType) && CommonTools.equalsOrBothNull(this.subType, other.subType);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(mimeType);

		if (!params.isEmpty()) {
			for (Map.Entry<String, String> p : params.entrySet()) {
				builder.append(';');
				builder.append(p.getKey());
				builder.append('=');
				builder.append(p.getValue());
			}
		}

		return builder.toString();
	}

}
