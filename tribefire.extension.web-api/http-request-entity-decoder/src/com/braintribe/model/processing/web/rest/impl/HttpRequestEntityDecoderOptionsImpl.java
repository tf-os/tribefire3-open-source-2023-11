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
package com.braintribe.model.processing.web.rest.impl;

import java.util.ArrayList;
import java.util.List;

import com.braintribe.model.processing.web.rest.HttpRequestEntityDecoderOptions;
import com.braintribe.model.processing.web.rest.PropertyTypeResolver;

public class HttpRequestEntityDecoderOptionsImpl implements HttpRequestEntityDecoderOptions {
	
	private boolean ignoringHeaders;
	private boolean ignoringUnmappedUrlParameters;
	private boolean ignoringUnmappedHeaders;
	private boolean nullAware;
	private PropertyTypeResolver propertyTypeResolver;
	private List<String> ignoredParameters = new ArrayList<>();

	@Override
	public boolean isIgnoringHeaders() {
		return ignoringHeaders;
	}

	@Override
	public HttpRequestEntityDecoderOptions setIgnoringHeaders(boolean ignoringHeaders) {
		this.ignoringHeaders = ignoringHeaders;
		return this;
	}

	@Override
	public boolean isIgnoringUnmappedUrlParameters() {
		return ignoringUnmappedUrlParameters;
	}

	@Override
	public HttpRequestEntityDecoderOptions setIgnoringUnmappedUrlParameters(boolean ignoringUnmappedUrlParameters) {
		this.ignoringUnmappedUrlParameters = ignoringUnmappedUrlParameters;
		return this;
	}

	@Override
	public boolean isIgnoringUnmappedHeaders() {
		return ignoringUnmappedHeaders;
	}

	@Override
	public HttpRequestEntityDecoderOptions setIgnoringUnmappedHeaders(boolean ignoringUnmappedHeaders) {
		this.ignoringUnmappedHeaders = ignoringUnmappedHeaders;
		return this;
	}

	@Override
	public PropertyTypeResolver getPropertyTypeResolver() {
		return propertyTypeResolver;
	}

	@Override
	public HttpRequestEntityDecoderOptions setPropertyTypeResolver(PropertyTypeResolver propertyTypeResolver) {
		this.propertyTypeResolver = propertyTypeResolver;
		return this;
	}

	@Override
	public List<String> getIgnoredParameters() {
		return ignoredParameters;
	}

	@Override
	public HttpRequestEntityDecoderOptions setIgnoredParameters(List<String> ignoredParameters) {
		this.ignoredParameters = ignoredParameters;
		return this;
	}

	@Override
	public HttpRequestEntityDecoderOptions addIgnoredParameter(String ignoredParameter) {
		if (ignoredParameters == null) {
			ignoredParameters = new ArrayList<>();
		}
		
		ignoredParameters.add(ignoredParameter);
		return this;
	}

	@Override
	public HttpRequestEntityDecoderOptions setNullAware(boolean nullAware) {
		this.nullAware = nullAware;
		return this;
	}

	@Override
	public boolean isNullAware() {
		return nullAware;
	}

}
