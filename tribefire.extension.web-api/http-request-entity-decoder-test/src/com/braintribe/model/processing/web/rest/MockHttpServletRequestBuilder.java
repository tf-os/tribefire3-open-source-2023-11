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
package com.braintribe.model.processing.web.rest;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

public class MockHttpServletRequestBuilder {

	private final MockHttpServletRequest request = new MockHttpServletRequest();

	public MockHttpServletRequestBuilder header(String name, String ...values) {
		List<String> headers = request.getHeaderMap().get(name);
		if(headers == null) {
			headers = new ArrayList<>();
			request.getHeaderMap().put(name, headers);
		}
		for(String value : values) {
			headers.add(value);
		}
		
		return this;
	}
	
	public MockHttpServletRequestBuilder parameter(String name, String ...values) {
		String[] existing = request.getParameterMap().put(name, values);
		if(existing != null) {
			throw new IllegalStateException("The servlet request already contains the parameter with name \"" + name + "\", please add all values at once.");
		}
		return this;
	}
	
	public HttpServletRequest build() {
		return request;
	}
}
