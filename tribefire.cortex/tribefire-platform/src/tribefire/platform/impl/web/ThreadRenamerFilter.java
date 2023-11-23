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
package tribefire.platform.impl.web;

import java.io.IOException;
import java.util.Objects;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import com.braintribe.cfg.Configurable;
import com.braintribe.logging.Logger;
import com.braintribe.logging.ThreadRenamer;

public class ThreadRenamerFilter implements Filter {

	private static final Logger logger = Logger.getLogger(ThreadRenamerFilter.class);

	private ThreadRenamer threadRenamer = ThreadRenamer.NO_OP;
	private boolean addFullUrl;

	@Configurable
	public void setThreadRenamer(ThreadRenamer threadRenamer) {
		Objects.requireNonNull(threadRenamer, "threadRenamer cannot be set to null");
		this.threadRenamer = threadRenamer;
	}

	@Configurable
	public void setAddFullUrl(boolean addFullUrl) {
		this.addFullUrl = addFullUrl;
	}

	@Override
	public void destroy() {
		/* Intentionally left empty */
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
		threadRenamer.push(() -> name(request));
		try {
			filterChain.doFilter(request, response);
		} finally {
			threadRenamer.pop();
		}
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		/* Intentionally left empty */
	}

	private String name(ServletRequest request) {
		return "from(" + url(request) + ")";
	}

	private String url(ServletRequest request) {
		try {
			if (addFullUrl) {
				return ((HttpServletRequest) request).getRequestURL().toString();
			}
			return ((HttpServletRequest) request).getRequestURI();
		} catch (Throwable t) {
			logger.error("Failed to obtain an URL from " + request, t);
			return "<unknown>";
		}
	}

}
