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
package com.braintribe.web.multipart.api;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public interface PartHeader {
	String getFileName();
	String getName();
	boolean isFile();

	String getContentType();

	String getFormDataContentDisposition();
	Set<String> getFormDataContentDispositionParameterNames();
	String getFormDataContentDispositionParameter(String name);

	Set<String> getHeaderNames();
	String getHeader(String name);
	Collection<String> getHeaders(String name);
	Stream<Map.Entry<String, List<String>>> getHeaders();

	String getTransferEncoding();
	
	default String getContentLength() {
		return getHeader("Content-Length");
	}
}
