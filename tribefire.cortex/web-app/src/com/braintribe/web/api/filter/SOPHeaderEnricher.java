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
package com.braintribe.web.api.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SOPHeaderEnricher implements Filter {
	
	private String allowedOrigins = "*";
	
	public void setAllowedOrigins(String allowedOrigins) {
		this.allowedOrigins = allowedOrigins;
	}

	@Override
	public void destroy() {
		//Nothing to do
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		if(request instanceof HttpServletRequest)
			((HttpServletRequest)request).setAttribute("Access-Control-Allow-Origin", allowedOrigins);
		if(response instanceof HttpServletResponse){
			((HttpServletResponse)response).setHeader("Access-Control-Allow-Origin", allowedOrigins);
			((HttpServletResponse)response).setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
		}
		chain.doFilter(request, response);	
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		//Nothing to do
	}


}
