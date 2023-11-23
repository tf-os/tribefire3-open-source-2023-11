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
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.braintribe.cfg.Configurable;
import com.braintribe.logging.Logger;

public class SetCharacterEncodingFilter implements Filter {

	private static final Logger logger = Logger.getLogger(SetCharacterEncodingFilter.class);

	protected String encoding = null;
	protected boolean ignore = false;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		Enumeration<String> paramNames = filterConfig.getInitParameterNames();
		while (paramNames.hasMoreElements()) {
			String paramName = paramNames.nextElement();
			String paramValue = filterConfig.getInitParameter(paramName);
			if (paramName.equalsIgnoreCase("encoding")) {
				this.setEncoding(paramValue);
			} else if (paramName.equalsIgnoreCase("ignore")) {
				this.setIgnore(Boolean.parseBoolean(paramValue));
			} else {
				logger.warn("Unknown parameter name: "+paramName+" with value "+paramValue);
			}
		}
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		if ((ignore) || (request.getCharacterEncoding() == null)) {
			if (this.encoding != null) {
				if (logger.isTraceEnabled()) {
					logger.trace("Setting character encoding "+this.encoding+" in request.");
				}
				request.setCharacterEncoding(this.encoding);
			}
		}
		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {
		//Nothing to do
	}

	@Configurable
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}
	@Configurable
	public void setIgnore(boolean ignore) {
		this.ignore = ignore;
	}

}
