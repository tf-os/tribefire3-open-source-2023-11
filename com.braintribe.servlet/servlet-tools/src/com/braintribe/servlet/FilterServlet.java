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
package com.braintribe.servlet;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;


public class FilterServlet extends HttpServlet {
	
	private static final long serialVersionUID = -5843774757323766685L;
	
	private Filter filter;
	private HttpServlet delegate;

	public Filter getFilter() {
		return filter;
	}

	@Required
	@Configurable
	public void setFilter(Filter authFilter) {
		this.filter = authFilter;
	}
	
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		filter.doFilter(req, resp, delegate::service);
	}

	@Required
	@Configurable
	public void setDelegate(HttpServlet delegate) {
		this.delegate = delegate;
	}
	
	@Override
	public int hashCode() {
		return delegate.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return delegate.equals(obj);
	}

	@Override
	public void destroy() {
		delegate.destroy();
	}

	@Override
	public String getInitParameter(String name) {
		return delegate.getInitParameter(name);
	}

	@Override
	public Enumeration<String> getInitParameterNames() {
		return delegate.getInitParameterNames();
	}

	@Override
	public ServletConfig getServletConfig() {
		return delegate.getServletConfig();
	}

	@Override
	public ServletContext getServletContext() {
		return delegate.getServletContext();
	}

	@Override
	public String getServletInfo() {
		return delegate.getServletInfo();
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		delegate.init(config);
	}

	@Override
	public void init() throws ServletException {
		delegate.init();
	}

	@Override
	public void log(String msg) {
		delegate.log(msg);
	}

	@Override
	public void log(String message, Throwable t) {
		delegate.log(message, t);
	}

	@Override
	public String getServletName() {
		return delegate.getServletName();
	}	
}
