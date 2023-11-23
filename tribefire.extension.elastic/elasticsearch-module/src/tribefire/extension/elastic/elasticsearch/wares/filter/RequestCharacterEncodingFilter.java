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
package tribefire.extension.elastic.elasticsearch.wares.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;

/**
 * Request character encoding filter.
 *
 * <p>
 * This class is used for setting character encoding for ServletRequest object and content-type for ServletResponse
 * object for each request coming from the client. The encoding to be set for the request is by default set to UTF-8.
 * </p>
 *
 */
public class RequestCharacterEncodingFilter implements Filter {

	private String encoding = null;

	public RequestCharacterEncodingFilter() {
		this.encoding = null;
	}

	@Override
	public void init(FilterConfig config) {
		if (this.encoding == null) {
			this.encoding = config.getInitParameter("encoding");
		}
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		request.setCharacterEncoding(encoding);
		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {
		// Intentionally left blank
	}

	@Configurable
	@Required
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

}
