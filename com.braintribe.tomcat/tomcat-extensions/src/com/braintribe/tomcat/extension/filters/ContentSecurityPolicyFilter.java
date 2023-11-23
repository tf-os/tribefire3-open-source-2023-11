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
package com.braintribe.tomcat.extension.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

/**
 * A simple filter which can be used to set Content Security Policy directives via HTTP header {@value #CONTENT_SECURITY_POLICY_HEADER_NAME}. The
 * directives to be set can be configured via filter init parameter {@value #DIRECTIVES_INIT_PARAMATER_NAME}.
 * <p>
 * For further information about cContent Security Policy see https://content-security-policy.com/.
 */
public class ContentSecurityPolicyFilter implements Filter {

	private static final String CONTENT_SECURITY_POLICY_HEADER_NAME = "Content-Security-Policy";
	private static final String DIRECTIVES_INIT_PARAMATER_NAME = "directives";

	private String directivesString;

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		HttpServletResponse response = (HttpServletResponse) res;

		response.setHeader(CONTENT_SECURITY_POLICY_HEADER_NAME, directivesString);

		chain.doFilter(req, res);
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		directivesString = config.getInitParameter(DIRECTIVES_INIT_PARAMATER_NAME);
		// we could do some validation here
	}

	@Override
	public void destroy() {
		// nothing to do
	}
}
