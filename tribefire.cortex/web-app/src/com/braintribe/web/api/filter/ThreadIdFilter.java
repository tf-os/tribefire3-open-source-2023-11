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
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import com.braintribe.logging.Logger;

public class ThreadIdFilter implements Filter {

	private static final Logger logger = Logger.getLogger(ThreadIdFilter.class);
	
	private static AtomicLong threadIdCounter = new AtomicLong(0);

	@Override
	public void destroy() {
		//nothing to do
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
		String servletPath = null;
		if (request instanceof HttpServletRequest) {
			servletPath = ((HttpServletRequest) request).getServletPath();
		} else {
			servletPath = "n/a";
		}
		
		String originalThreadName = null;
		try {
			originalThreadName = Thread.currentThread().getName();
		} catch(Exception e) {
			logger.debug("Could not get the thread's name", e);
		}
		
		try {
			Long threadId = threadIdCounter.incrementAndGet();
			String threadIdString = Long.toString(threadId.longValue(), 36);
			String context = servletPath+"#"+threadIdString;
			logger.pushContext(context);
			
			try {
				Thread.currentThread().setName(context);
			} catch(Exception e) {
				logger.debug("Could not set thread name "+context, e);
				originalThreadName = null;
			}
			
			filterChain.doFilter(request, response);

		} finally {
			
			if (originalThreadName != null) {
				try {
					Thread.currentThread().setName(originalThreadName);
				} catch(Exception e) {
					logger.debug("Could not reset the thread's name to "+originalThreadName);
				}
			}

			logger.removeContext();
			logger.clearMdc();
		}
		
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		//nothing to do
	}

}
