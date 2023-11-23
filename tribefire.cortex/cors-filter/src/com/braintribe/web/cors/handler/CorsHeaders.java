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
package com.braintribe.web.cors.handler;

public enum CorsHeaders {
	
	accessControlAllowCredentials("Access-Control-Allow-Credentials"),
	accessControlAllowHeaders	 ("Access-Control-Allow-Headers"),
	accessControlAllowMethods	 ("Access-Control-Allow-Methods"),
	accessControlAllowOrigin	 ("Access-Control-Allow-Origin"),
	accessControlExposeHeaders	 ("Access-Control-Expose-Headers"),
	accessControlMaxAge			 ("Access-Control-Max-Age"),
	accessControlRequestHeaders	 ("Access-Control-Request-Headers"),
	accessControlRequestMethod	 ("Access-Control-Request-Method"),
	host						 ("Host"),
	origin						 ("Origin"),
	vary						 ("Vary");
	
	private String headerName;
	
	private CorsHeaders(String headerName) {
		this.headerName = headerName;
	}
	
	public String getHeaderName() {
		return headerName;
	}
	
}
