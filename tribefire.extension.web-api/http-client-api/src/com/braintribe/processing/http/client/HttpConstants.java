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
package com.braintribe.processing.http.client;

import java.util.Set;

import com.braintribe.utils.lcd.CollectionTools2;

public interface HttpConstants {
	
	final int HTTP_CODE_OK = 200;
	final int HTTP_CODE_CREATED = 201;
	final int HTTP_CODE_ACCEPTED = 202;

	final String HTTP_METHOD_GET = "GET"; 
	final String HTTP_METHOD_POST = "POST";
	final String HTTP_METHOD_PUT = "PUT";
	final String HTTP_METHOD_PATCH = "PATCH";
	final String HTTP_METHOD_DELETE = "DELETE";
	final String HTTP_METHOD_HEAD = "HEAD";
	final String HTTP_METHOD_OPTIONS = "OPTIONS"; 
	final String HTTP_METHOD_TRACE = "TRACE";

	final String HTTP_HEADER_ACCEPT = "ACCEPT";
	final String HTTP_HEADER_CONTENTTYPE = "CONTENT-TYPE";
	
	final Set<Integer> DEFAULT_SUCCESS_CODES = CollectionTools2.asSet(HTTP_CODE_OK, HTTP_CODE_CREATED, HTTP_CODE_ACCEPTED);
	final String DEFAULT_PATH = "/";
	final String DEFAULT_MIME_TYPE = "application/json";
	
}
