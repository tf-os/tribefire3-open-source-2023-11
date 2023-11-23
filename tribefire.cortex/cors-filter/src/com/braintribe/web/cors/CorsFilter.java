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
package com.braintribe.web.cors;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.web.cors.exception.CorsException;
import com.braintribe.web.cors.handler.CorsHandler;
import com.braintribe.web.cors.handler.CorsRequestType;

/**
 * {@link Filter} for handling W3C's CORS (Cross-Origin Resource Sharing) policies.
 * 
 */
public class CorsFilter implements Filter {
	
	private CorsHandler handler;

	private static final Logger log = Logger.getLogger(CorsFilter.class);
	
	@Required
	@Configurable
	public void setCorsHandler(CorsHandler handler) {
		this.handler = handler;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
			doFilter((HttpServletRequest)request, (HttpServletResponse)response, chain);
		} else {
			chain.doFilter(request, response);
		}
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		if (log.isTraceEnabled()) {
			log.trace("initializing "+this.getClass().getName());
		}
	}
	
	@Override
	public void destroy() {
		if (log.isTraceEnabled()) {
			log.trace("destroying "+this.getClass().getName());
		}
	}
	
	private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
		
		if (log.isTraceEnabled()) {
			logHeaders(request);
		}
		
		CorsRequestType requestType = CorsRequestType.get(request);
		
		if (requestType.equals(CorsRequestType.nonCors)) {
			
			if (log.isTraceEnabled()) {
				log.trace("Non-CORS request");
			}
			
			chain.doFilter(request, response);
			
		} else {
			
			if (log.isTraceEnabled()) {
				log.trace("CORS request: "+requestType);
			}
			
			try {
				if (requestType.equals(CorsRequestType.preflight)) {
					
					handler.handlePreflight(request, response);
					
				} else if (requestType.equals(CorsRequestType.actual)) {
					
					handler.handleActual(request, response);
					
					chain.doFilter(request, response);
					
				}
				
			} catch (CorsException cex) {
				
				response.setStatus(cex.getHttpResponseCode());
				response.resetBuffer();
				response.setContentType("text/plain");
				PrintWriter out = response.getWriter();
				out.println("Failed to apply Cross-Origin Resource Sharing (CORS) policy: " + cex.getMessage());
				
			}
		}
		
		if (log.isTraceEnabled()) {
			logHeaders(request, response);
		}
		
	}

	private static void logHeaders(HttpServletRequest request) {

		if (!log.isTraceEnabled()) {
			return;
		}

		Enumeration<String> headerNames = request.getHeaderNames();

		if (!headerNames.hasMoreElements()) {
			log.trace("No request headers ("+request.getRequestURI()+")");
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append("Request headers ("+request.getRequestURI()+"): ");
			while (headerNames.hasMoreElements()) {
				String header = headerNames.nextElement();
				sb.append("\n\t").append("[").append(header).append("]: [").append(request.getHeader(header)).append("]");
			}
			log.trace(sb.toString());
		}

	}

	private static void logHeaders(HttpServletRequest request, HttpServletResponse response) {

		if (!log.isTraceEnabled()) {
			return;
		}

		Collection<String> headerNames = response.getHeaderNames();

		if (headerNames.isEmpty()) {
			log.trace("No response headers ("+request.getRequestURI()+")");
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append("Response headers ("+request.getRequestURI()+"): ");
			for (String header : headerNames) {
				sb.append("\n\t").append("[").append(header).append("]: [").append(response.getHeader(header)).append("]");
			}
			log.trace(sb.toString());
		}

	}

}
