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
/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package tribefire.extension.elastic.elasticsearch.wares;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.rest.RestRequest;

import com.braintribe.utils.IOTools;

/**
 *
 */
public class ServletRestRequest extends RestRequest {

	private final HttpServletRequest servletRequest;

	private final Method method;

	private final byte[] content;

	protected String pathIdentifier = null;

	// TODO: check migration from 2.2.1
	// public ServletRestRequest(String terminalId, HttpServletRequest servletRequest) throws IOException {
	// this.servletRequest = servletRequest;
	// this.terminalId = terminalId != null ? ("/" + terminalId) : null;
	// this.method = Method.valueOf(servletRequest.getMethod());
	// this.params = new HashMap<String, String>();
	//
	// if (servletRequest.getQueryString() != null) {
	// org.elasticsearch.rest.RestUtils.decodeQueryString(servletRequest.getQueryString(), 0, params);
	// }
	//
	// content = IOTools.slurpBytes(servletRequest.getInputStream());
	// }
	public ServletRestRequest(String pathIdentifier, HttpServletRequest servletRequest, NamedXContentRegistry xContentRegistry, String uri,
			Map<String, List<String>> headers) throws IOException {
		super(xContentRegistry, uri, headers);

		this.servletRequest = servletRequest;
		this.pathIdentifier = pathIdentifier != null ? ("/" + pathIdentifier) : null;
		this.method = Method.valueOf(servletRequest.getMethod());

		content = IOTools.slurpBytes(servletRequest.getInputStream());
	}

	@Override
	public Method method() {
		return this.method;
	}

	@Override
	public String uri() {
		String queryString = servletRequest.getQueryString();
		if (queryString != null && !queryString.trim().isEmpty()) {
			return servletRequest.getRequestURI().substring(servletRequest.getContextPath().length() + servletRequest.getServletPath().length()) + "?"
					+ queryString;
		}
		return servletRequest.getRequestURI().substring(servletRequest.getContextPath().length() + servletRequest.getServletPath().length());
	}

	@Override
	public String rawPath() {
		String requestUri = servletRequest.getRequestURI();
		String contextPath = servletRequest.getContextPath();
		int contextPathLength = contextPath.length();
		String servletPath = servletRequest.getServletPath();
		int servletPathLength = servletPath.length();
		int totalLength = contextPathLength + servletPathLength;

		String rawPath = requestUri.substring(totalLength);
		if (this.pathIdentifier != null) {
			if (rawPath.startsWith(this.pathIdentifier)) {
				rawPath = rawPath.substring(this.pathIdentifier.length());
			}
		}
		return rawPath;
	}

	@Override
	public boolean hasContent() {
		return content.length > 0;
	}

	public boolean contentUnsafe() {
		return false;
	}

	@Override
	public BytesReference content() {
		return new BytesArray(content);
	}

	// TODO: check migration from 2.2.1
	// @Override
	// public String header(String name) {
	// return servletRequest.getHeader(name);
	// }

	// TODO: check migration from 2.2.1
	public Iterable<Map.Entry<String, String>> headers() {
		if (servletRequest.getHeaderNames() == null) {
			return null;
		}

		Map<String, String> headersMap = new HashMap<String, String>();
		for (Enumeration<String> e = servletRequest.getHeaderNames(); e.hasMoreElements();) {
			String headerName = e.nextElement();
			headersMap.put(headerName, servletRequest.getHeader(headerName));
		}

		Set<Entry<String, String>> entrySet = headersMap.entrySet();
		return entrySet;
	}

	// TODO: check migration from 2.2.1
	// @Override
	// public boolean hasParam(String key) {
	// return params.containsKey(key);
	// }

	// TODO: check migration from 2.2.1
	// @Override
	// public String param(String key) {
	// return params.get(key);
	// }

	// TODO: check migration from 2.2.1
	// @Override
	// public String param(String key, String defaultValue) {
	// String value = params.get(key);
	// if (value == null) {
	// return defaultValue;
	// }
	// return value;
	// }
}
