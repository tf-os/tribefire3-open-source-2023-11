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
package com.braintribe.servlet.exception;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.braintribe.cfg.Configurable;
import com.braintribe.logging.Logger;

public class ExceptionFilter implements Filter {

	private static final Logger logger = Logger.getLogger(ExceptionFilter.class);
	
	private Set<ExceptionHandler> exceptionHandlers;
	
	@Override
	public void destroy() {
		/* Intentionally left empty */
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
		
		
		try {
			filterChain.doFilter(request, response);
		}
		catch(Throwable t) {

			String tracebackId = UUID.randomUUID().toString();

			ExceptionHandlingContext context = new ExceptionHandlingContext(tracebackId, request, response, t);
			if (response.isCommitted()) {
				logger.debug("Response has been committed already. Dumping exception in log.");
				context.setOutputCommitted(true);
			}
			
			boolean handled = false;
			for (ExceptionHandler handler : this.exceptionHandlers) {
				Boolean result = handler.apply(context);
				if (result != null && result.booleanValue()) {
					handled = true;
				}
			}
			
			if (!handled) {
				throw t;
			}
			
		}
	}
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		if (exceptionHandlers == null || exceptionHandlers.isEmpty()) {
			exceptionHandlers = new LinkedHashSet<>();
			exceptionHandlers.add(new StandardExceptionHandler());
		}
	}

	@Configurable
	public void setExceptionHandlers(Set<ExceptionHandler> exceptionHandlers) {
		this.exceptionHandlers = exceptionHandlers;
	}

}
