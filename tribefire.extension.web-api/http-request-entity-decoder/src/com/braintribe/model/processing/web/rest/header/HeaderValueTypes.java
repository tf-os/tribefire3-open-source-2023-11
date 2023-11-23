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

import static com.braintribe.model.processing.web.rest.header.HeaderValueType.DATE;
import static com.braintribe.model.processing.web.rest.header.HeaderValueType.INT;
import static com.braintribe.model.processing.web.rest.header.HeaderValueType.STRING;
import static com.braintribe.model.processing.web.rest.header.HeaderValueType.STRING_LIST;

import java.util.Map;

import com.braintribe.utils.MapTools;

/**
 * This class contains the map of well known headers' name with their types: {@link #WELL_KNOWN_HEADERS}.
 * 
 */
public class HeaderValueTypes {
	
	/**
	 * Contains all well known headers' types, by name.
	 */
	//@formatter:off
	public static final Map<String, HeaderValueType> WELL_KNOWN_HEADERS = MapTools.getParameterizedMap(
			String.class, 			HeaderValueType.class,
			"accept", 				STRING_LIST,
			"acceptCharset", 		STRING_LIST,
			"acceptEncoding", 		STRING_LIST,
			"acceptLanguage", 		STRING_LIST,
			"acceptRange", 			STRING_LIST,
			"age", 					INT,
			"allow", 				STRING_LIST,
			"authorization", 		STRING,
			"cacheControl", 			STRING_LIST,
			"connection", 			STRING_LIST,
			"contentEncoding", 		STRING_LIST,
			"contentLanguage", 		STRING_LIST,
			"contentLength", 		INT,
			"contentLocation", 		STRING,
			"contentMd5", 			STRING,
			"contentRange", 			STRING,
			"contentType", 			STRING,
			"date", 					DATE,
			"eTag", 					STRING,
			"expect", 				STRING_LIST,
			"expires", 				DATE, 
			"from", 					STRING,
			"host", 					STRING,
			"ifMatch", 				STRING_LIST, // TODO list or "*"
			"ifModifiedSince",	 	DATE,
			"ifNoneMatch", 			STRING_LIST, // TODO list or "*"
			"ifRange", 				STRING, // TODO date or string (entity-tag)
			"ifUnmodifiedSince",		DATE,
			"lastModified", 			DATE,
			"location", 				STRING,
			"maxForwards", 			INT,
			"pragma", 				STRING_LIST,
			"proxyAuthenticate", 	STRING_LIST,
			"proxyAuthorization",	STRING,
			"range", 				STRING,
			"referer", 				STRING,
			"retryAfter", 			STRING, // TODO date or int
			"server", 				STRING,
			"te", 					STRING_LIST,
			"trailer", 				STRING_LIST,
			"transferEncoding", 		STRING_LIST,
			"upgrade", 				STRING_LIST,
			"userAgent", 			STRING_LIST,
			"vary", 					STRING_LIST, // TODO list or "*"
			"via", 					STRING_LIST,
			"warning", 				STRING_LIST,
			"wwwAuthenticate", 		STRING_LIST
			);
	//@formatter:on
}
