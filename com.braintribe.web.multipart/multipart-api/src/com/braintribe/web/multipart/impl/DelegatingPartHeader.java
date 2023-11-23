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

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Stream;

import com.braintribe.web.multipart.api.PartHeader;

public class DelegatingPartHeader implements PartHeader {

	private PartHeader delegate;

	@Override
	public String getFileName() {
		return delegate.getFileName();
	}

	@Override
	public String getName() {
		return delegate.getName();
	}

	@Override
	public boolean isFile() {
		return delegate.isFile();
	}

	@Override
	public String getContentType() {
		return delegate.getContentType();
	}

	@Override
	public String getFormDataContentDisposition() {
		return delegate.getFormDataContentDisposition();
	}

	@Override
	public Set<String> getFormDataContentDispositionParameterNames() {
		return delegate.getFormDataContentDispositionParameterNames();
	}

	@Override
	public String getFormDataContentDispositionParameter(String name) {
		return delegate.getFormDataContentDispositionParameter(name);
	}

	@Override
	public Set<String> getHeaderNames() {
		return delegate.getHeaderNames();
	}

	@Override
	public String getHeader(String name) {
		return delegate.getHeader(name);
	}

	@Override
	public Collection<String> getHeaders(String name) {
		return delegate.getHeaders(name);
	}
	
	@Override
	public Stream<Entry<String, List<String>>> getHeaders() {
		return delegate.getHeaders();
	}

	public DelegatingPartHeader(PartHeader delegate) {
		this.delegate = delegate;
	}

	@Override
	public String getTransferEncoding() {
		return delegate.getTransferEncoding();
	}

}
