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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

import com.braintribe.web.multipart.api.MutablePartHeader;
import com.braintribe.web.multipart.api.PartHeader;

import static com.braintribe.web.multipart.api.PartHeaders.CONTENT_DISPOSITION;

public class MutablePartHeaderImpl implements MutablePartHeader {
	private Map<String, List<String>> headers = new LinkedHashMap<String, List<String>>();
	private Map<String, String> formDataContentDispositionParameters;
	
	public MutablePartHeaderImpl() {
		// Allow empty constructor
	}
	
	public MutablePartHeaderImpl(PartHeader template) {
		template.getHeaders().forEach(entry -> {
			entry.getValue().forEach(value -> addHeader(entry.getKey(), value));
		});
	}

	@Override
	public String getName() {
		return getFormDataContentDispositionParameter("name");
	}

	@Override
	public String getFileName() {
		return getFormDataContentDispositionParameter("filename");
	}

	@Override
	public MutablePartHeader setFileName(String filename) {
		setContentDispositionParameter("filename", filename);
		return this;
	}

	@Override
	public MutablePartHeader setName(String name) {
		setContentDispositionParameter("name", name);
		return this;
	}

	@Override
	public boolean isFile() {
		return getContentType() != null && getFileName() != null;
	}

	@Override
	public String getContentType() {
		return getHeader("Content-Type");
	}

	@Override
	public String getFormDataContentDisposition() {
		Collection<String> contentDispositions = getHeaders(CONTENT_DISPOSITION);

		for (String contentDisposition : contentDispositions) {
			if (contentDisposition.startsWith("form-data")) {
				return contentDisposition;
			}
		}
		return null;
	}

	@Override
	public Set<String> getFormDataContentDispositionParameterNames() {
		return getFormDataContentDispositionParameters().keySet();
	}

	private Map<String, String> getFormDataContentDispositionParameters() {
		if (formDataContentDispositionParameters == null) {
			String contentDisposition = getFormDataContentDisposition();

			if (contentDisposition == null) {
				formDataContentDispositionParameters = new LinkedHashMap<String, String>();
			} else {
				formDataContentDispositionParameters = parseContentDisposition(contentDisposition);
			}
		}

		return formDataContentDispositionParameters;
	}

	private Map<String, String> parseContentDisposition(String contentDisposition) {
		String parts[] = contentDisposition.split(";");
		Map<String, String> parameters = new LinkedHashMap<String, String>();
		for (int i = 0; i < parts.length; i++) {
			String part = parts[i].trim();
			
			if (i == 0) {
				if ("form-data".equals(part)) {
					continue;
				}
				else {
					throw new IllegalStateException("Content-Disposition is missing form-data key");
				}
			}
			
			int index = part.indexOf("=");

			if (index != -1) {
				String name = part.substring(0, index);
				String value = part.substring(index + 1);

				if (value.startsWith("\"") && value.endsWith("\"")) {
					value = value.substring(1, value.length() - 1);
				}
				parameters.put(name, value);
			} else {
				parameters.put(part, null);
			}

		}
		return parameters;
	}

	@Override
	public String getFormDataContentDispositionParameter(String name) {
		return getFormDataContentDispositionParameters().get(name);
	}

	@Override
	public Set<String> getHeaderNames() {
		return headers.keySet();
	}

	@Override
	public String getHeader(String name) {
		Collection<String> values = getHeaders(name);
		return values.size() > 0 ? values.iterator().next() : null;
	}

	@Override
	public Collection<String> getHeaders(String name) {
		List<String> values = headers.get(name);
		return values != null ? values : Collections.<String> emptyList();
	}

	@Override
	public Stream<Entry<String, List<String>>> getHeaders() {
		return headers.entrySet().stream();
	}

	@Override
	public MutablePartHeader setContentType(String contentType) {
		setHeader("Content-Type", contentType);
		return this;
	}

	@Override
	public String getTransferEncoding() {
		return getHeader("Transfer-Encoding");

	}

	@Override
	public MutablePartHeader setTransferEncoding(String transferEncoding) {
		setHeader("Transfer-Encoding", transferEncoding);
		return this;
	}

	@Override
	public MutablePartHeader addHeader(String name, String value) {
		List<String> values = headers.put(name, Collections.<String> singletonList(value));
		if (values != null) {
			if (values.size() == 1) {
				List<String> extendedValues = new ArrayList<String>(2);
				extendedValues.add(values.get(0));
				extendedValues.add(value);

				headers.put(name, extendedValues);
			} else {
				values.add(value);
			}
		}

		return this;
	}

	@Override
	public MutablePartHeader setHeader(String name, String value) {
		if (value == null) {
			headers.remove(name);
		}else {
			headers.put(name, Collections.<String> singletonList(value));
		}
		
		if (name.equals(CONTENT_DISPOSITION)) {
			formDataContentDispositionParameters = null;
		}
		return this;
	}

	@Override
	public MutablePartHeader setContentDispositionParameter(String name, String value) {
		Objects.requireNonNull(value, "Null values not allowed for Content-Disposition parameter values. Key would have been '" + name + "'.");
		Objects.requireNonNull(name, "Null values not allowed for Content-Disposition parameter keys. Value would have been '" + value + "'.");
		getFormDataContentDispositionParameters().put(name, value);
		transferContentDispositionParametersToHeader();
		return this;
	}

	private void transferContentDispositionParametersToHeader() {
		StringBuilder cdHeader = new StringBuilder();
		cdHeader.append("form-data");

		for (Map.Entry<String, String> entry : getFormDataContentDispositionParameters().entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();

			cdHeader.append("; ");

			if (value == null) {
				cdHeader.append(key);
			} else {
				cdHeader.append(key);
				cdHeader.append('=');
				cdHeader.append('"');
				cdHeader.append(value);
				cdHeader.append('"');
			}
		}
		
		headers.put(CONTENT_DISPOSITION, Collections.<String> singletonList(cdHeader.toString()));
	}
	
	void removeContentDispositionParameter(String key) {
		getFormDataContentDispositionParameters().remove(key);
		transferContentDispositionParametersToHeader();
	}

}
