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
package com.braintribe.model.processing.web.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import com.braintribe.common.lcd.NotImplementedException;
import com.braintribe.utils.collection.impl.HashMultiMap;

import io.undertow.util.DateUtils;

public class MockHttpServletRequest implements HttpServletRequest {

	private final Map<String, String[]> parameterMap = new HashMap<>();
	
	private final Map<String, List<String>> headerMap = new HashMultiMap<>();
	
	@Override
	public Map<String, String[]> getParameterMap() {
		return parameterMap;
	}
	
	@Override
	public Enumeration<String> getHeaderNames() {
		return Collections.enumeration(headerMap.keySet());
	}

	@Override
	public Enumeration<String> getHeaders(String name) {
		List<String> values = headerMap.get(name);
		return Collections.enumeration(values == null ? Collections.EMPTY_LIST : values);
	}

	public Map<String, List<String>> getHeaderMap() {
		return headerMap;
	}

	@Override
	public long getDateHeader(String name) {
		try {
			List<String> dates = headerMap.get(name);
			Date date = DateUtils.parseDate(dates.get(0));
			return date.getTime();
		} catch(Exception e) {
			throw new IllegalArgumentException("Cannot convert parameter " + name + " to date.");
		}
	}

	@Override
	public String getHeader(String name) {
		List<String> values = headerMap.get(name);
		if(values != null && !values.isEmpty()) {
			return values.get(0);
		}
		throw new IllegalArgumentException("No value associated with parameter " + name);
	}

	/**
	 * Unused methods, all of these throw new NotImplementedException().
	 */
	
	@Override
	public Object getAttribute(String name) {
		throw new NotImplementedException();
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		throw new NotImplementedException();
	}

	@Override
	public String getCharacterEncoding() {
		throw new NotImplementedException();
	}

	@Override
	public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
		throw new NotImplementedException();
	}

	@Override
	public int getContentLength() {
		throw new NotImplementedException();
	}

	@Override
	public String getContentType() {
		throw new NotImplementedException();
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		throw new NotImplementedException();
	}

	@Override
	public String getParameter(String name) {
		throw new NotImplementedException();
	}

	@Override
	public Enumeration<String> getParameterNames() {
		throw new NotImplementedException();
	}

	@Override
	public String[] getParameterValues(String name) {
		throw new NotImplementedException();
	}

	@Override
	public String getProtocol() {
		throw new NotImplementedException();
	}

	@Override
	public String getScheme() {
		throw new NotImplementedException();
	}

	@Override
	public String getServerName() {
		throw new NotImplementedException();
	}

	@Override
	public int getServerPort() {
		throw new NotImplementedException();
	}

	@Override
	public BufferedReader getReader() throws IOException {
		throw new NotImplementedException();
	}

	@Override
	public String getRemoteAddr() {
		throw new NotImplementedException();
	}

	@Override
	public String getRemoteHost() {
		throw new NotImplementedException();
	}

	@Override
	public void setAttribute(String name, Object o) {
		throw new NotImplementedException();
	}

	@Override
	public void removeAttribute(String name) {
		throw new NotImplementedException();
	}

	@Override
	public Locale getLocale() {
		throw new NotImplementedException();
	}

	@Override
	public Enumeration<Locale> getLocales() {
		throw new NotImplementedException();
	}

	@Override
	public boolean isSecure() {
		throw new NotImplementedException();
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String path) {
		throw new NotImplementedException();
	}

	@Override
	@Deprecated
	public String getRealPath(String path) {
		throw new NotImplementedException();
	}

	@Override
	public int getRemotePort() {
		throw new NotImplementedException();
	}

	@Override
	public String getLocalName() {
		throw new NotImplementedException();
	}

	@Override
	public String getLocalAddr() {
		throw new NotImplementedException();
	}

	@Override
	public int getLocalPort() {
		throw new NotImplementedException();
	}

	@Override
	public ServletContext getServletContext() {
		throw new NotImplementedException();
	}

	@Override
	public AsyncContext startAsync() throws IllegalStateException {
		throw new NotImplementedException();
	}

	@Override
	public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
		throw new NotImplementedException();
	}

	@Override
	public boolean isAsyncStarted() {
		throw new NotImplementedException();
	}

	@Override
	public boolean isAsyncSupported() {
		throw new NotImplementedException();
	}

	@Override
	public AsyncContext getAsyncContext() {
		throw new NotImplementedException();
	}

	@Override
	public DispatcherType getDispatcherType() {
		throw new NotImplementedException();
	}

	@Override
	public String getAuthType() {
		throw new NotImplementedException();
	}

	@Override
	public Cookie[] getCookies() {
		throw new NotImplementedException();
	}

	@Override
	public int getIntHeader(String name) {
		throw new NotImplementedException();
	}

	@Override
	public String getMethod() {
		throw new NotImplementedException();
	}

	@Override
	public String getPathInfo() {
		throw new NotImplementedException();
	}

	@Override
	public String getPathTranslated() {
		throw new NotImplementedException();
	}

	@Override
	public String getContextPath() {
		throw new NotImplementedException();
	}

	@Override
	public String getQueryString() {
		throw new NotImplementedException();
	}

	@Override
	public String getRemoteUser() {
		throw new NotImplementedException();
	}

	@Override
	public boolean isUserInRole(String role) {
		throw new NotImplementedException();
	}

	@Override
	public Principal getUserPrincipal() {
		throw new NotImplementedException();
	}

	@Override
	public String getRequestedSessionId() {
		throw new NotImplementedException();
	}

	@Override
	public String getRequestURI() {
		throw new NotImplementedException();
	}

	@Override
	public StringBuffer getRequestURL() {
		throw new NotImplementedException();
	}

	@Override
	public String getServletPath() {
		throw new NotImplementedException();
	}

	@Override
	public HttpSession getSession(boolean create) {
		throw new NotImplementedException();
	}

	@Override
	public HttpSession getSession() {
		throw new NotImplementedException();
	}

	@Override
	public boolean isRequestedSessionIdValid() {
		throw new NotImplementedException();
	}

	@Override
	public boolean isRequestedSessionIdFromCookie() {
		throw new NotImplementedException();
	}

	@Override
	public boolean isRequestedSessionIdFromURL() {
		throw new NotImplementedException();
	}

	@Override
	@Deprecated
	public boolean isRequestedSessionIdFromUrl() {
		throw new NotImplementedException();
	}

	@Override
	public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
		throw new NotImplementedException();
	}

	@Override
	public void login(String username, String password) throws ServletException {
		throw new NotImplementedException();
	}

	@Override
	public void logout() throws ServletException {
		throw new NotImplementedException();
	}

	@Override
	public Collection<Part> getParts() throws IOException, ServletException {
		throw new NotImplementedException();
	}

	@Override
	public Part getPart(String name) throws IOException, ServletException {
		throw new NotImplementedException();
	}

}
