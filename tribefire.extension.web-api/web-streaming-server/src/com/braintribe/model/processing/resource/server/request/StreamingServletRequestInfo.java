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
package com.braintribe.model.processing.resource.server.request;

import javax.servlet.http.HttpServletRequest;

public class StreamingServletRequestInfo {
	
	private ResourceStreamingRequest resourceStreamingRequest;
	private HttpServletRequest httpServletRequest;

	public StreamingServletRequestInfo(ResourceStreamingRequest resourceStreamingRequest, HttpServletRequest httpServletRequest) {
		super();
		this.resourceStreamingRequest = resourceStreamingRequest;
		this.httpServletRequest = httpServletRequest;
	}
	
	/**
	 * @return the resourceStreamingRequest
	 */
	public ResourceStreamingRequest getResourceStreamingRequest() {
		return resourceStreamingRequest;
	}
	/**
	 * @param resourceStreamingRequest the resourceStreamingRequest to set
	 */
	public void setResourceStreamingRequest(
			ResourceStreamingRequest resourceStreamingRequest) {
		this.resourceStreamingRequest = resourceStreamingRequest;
	}
	
	/**
	 * @return the httpServletRequest
	 */
	public HttpServletRequest getHttpServletRequest() {
		return httpServletRequest;
	}
	/**
	 * @param httpServletRequest the httpServletRequest to set
	 */
	public void setHttpServletRequest(HttpServletRequest httpServletRequest) {
		this.httpServletRequest = httpServletRequest;
	}

}
