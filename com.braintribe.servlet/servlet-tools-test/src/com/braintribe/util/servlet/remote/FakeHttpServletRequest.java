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
package com.braintribe.util.servlet.remote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

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

public class FakeHttpServletRequest implements HttpServletRequest {

	protected Map<String,List<String>> headers = new HashMap<>();
	protected String remoteAddr;
	protected Map<String,String[]> parameters = new HashMap<>();
	
	public FakeHttpServletRequest(String remoteAddr) {
		this.remoteAddr = remoteAddr;
	}
	public void addHeader(String key, String value) {
		List<String> valueList = headers.get(key);
		if (valueList == null) {
			valueList = new ArrayList<>();
			headers.put(key, valueList);
		}
		valueList.add(value);
	}
	public void addParameter(String key, String value) {
		parameters.put(key, new String[] {value});
	}
	public void addParameters(String key, String[] value) {
		parameters.put(key, value);
	}
	
	
	@Override
	public String getHeader(String key) {
		List<String> valueList = headers.get(key);
		if (valueList == null || valueList.isEmpty()) {
			return null;
		}
		return valueList.get(0);
	}
	@Override
	public Enumeration<String> getHeaderNames() {
		return new Vector<String>(headers.keySet()).elements();
	}
	@Override
	public Enumeration<String> getHeaders(String key) {
		List<String> valueList = headers.get(key);
		if (valueList == null) {
			return new Vector<String>().elements();
		}
		return new Vector<String>(valueList).elements();
	}

	
	
	@Override
	public AsyncContext getAsyncContext() {
		return null;
	}

	@Override
	public Object getAttribute(String arg0) {
		return null;
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return null;
	}

	@Override
	public String getCharacterEncoding() {
		return null;
	}

	@Override
	public int getContentLength() {
		return 0;
	}

	@Override
	public String getContentType() {
		return null;
	}

	@Override
	public DispatcherType getDispatcherType() {
		return null;
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		return null;
	}

	@Override
	public String getLocalAddr() {
		return null;
	}

	@Override
	public String getLocalName() {
		return null;
	}

	@Override
	public int getLocalPort() {
		return 0;
	}

	@Override
	public Locale getLocale() {
		return null;
	}

	@Override
	public Enumeration<Locale> getLocales() {
		return null;
	}

	@Override
	public String getParameter(String arg0) {
		String[] val = parameters.get(arg0);
		if (val == null || val.length == 0) {
			return null;
		}
		return val[0];
	}

	@Override
	public Map<String, String[]> getParameterMap() {
		return parameters;
	}

	@Override
	public Enumeration<String> getParameterNames() {
		return new Vector<String>(parameters.keySet()).elements();
	}

	@Override
	public String[] getParameterValues(String arg0) {
		return parameters.get(arg0);
	}

	@Override
	public String getProtocol() {
		return null;
	}

	@Override
	public BufferedReader getReader() throws IOException {
		return null;
	}

	@Deprecated // this is still required by Java Servlet API (3.0.1)
	@Override
	public String getRealPath(String arg0) {
		return null;
	}

	@Override
	public String getRemoteAddr() {
		return remoteAddr;
	}

	@Override
	public String getRemoteHost() {
		return null;
	}

	@Override
	public int getRemotePort() {
		return 0;
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String arg0) {
		return null;
	}

	@Override
	public String getScheme() {
		return null;
	}

	@Override
	public String getServerName() {
		return null;
	}

	@Override
	public int getServerPort() {
		return 0;
	}

	@Override
	public ServletContext getServletContext() {
		return null;
	}

	@Override
	public boolean isAsyncStarted() {
		return false;
	}

	@Override
	public boolean isAsyncSupported() {
		return false;
	}

	@Override
	public boolean isSecure() {
		return false;
	}

	@Override
	public void removeAttribute(String arg0) {
		//Intentionally left empty
	}

	@Override
	public void setAttribute(String arg0, Object arg1) {
		//Intentionally left empty
	}

	@Override
	public void setCharacterEncoding(String arg0) throws UnsupportedEncodingException {
		//Intentionally left empty
	}

	@Override
	public AsyncContext startAsync() throws IllegalStateException {
		return null;
	}

	@Override
	public AsyncContext startAsync(ServletRequest arg0, ServletResponse arg1) throws IllegalStateException {
		return null;
	}
	@Override
	public boolean authenticate(HttpServletResponse arg0) throws IOException, ServletException {

		return false;
	}
	@Override
	public String getAuthType() {

		return null;
	}
	@Override
	public String getContextPath() {

		return null;
	}
	@Override
	public Cookie[] getCookies() {

		return null;
	}
	@Override
	public long getDateHeader(String arg0) {

		return 0;
	}
	@Override
	public int getIntHeader(String arg0) {

		return 0;
	}
	@Override
	public String getMethod() {

		return null;
	}
	@Override
	public Part getPart(String arg0) throws IOException, ServletException {

		return null;
	}
	@Override
	public Collection<Part> getParts() throws IOException, ServletException {

		return null;
	}
	@Override
	public String getPathInfo() {

		return null;
	}
	@Override
	public String getPathTranslated() {

		return null;
	}
	@Override
	public String getQueryString() {

		return null;
	}
	@Override
	public String getRemoteUser() {

		return null;
	}
	@Override
	public String getRequestURI() {

		return null;
	}
	@Override
	public StringBuffer getRequestURL() {

		return null;
	}
	@Override
	public String getRequestedSessionId() {

		return null;
	}
	@Override
	public String getServletPath() {

		return null;
	}
	@Override
	public HttpSession getSession() {

		return null;
	}
	@Override
	public HttpSession getSession(boolean arg0) {

		return null;
	}
	@Override
	public Principal getUserPrincipal() {

		return null;
	}
	@Override
	public boolean isRequestedSessionIdFromCookie() {

		return false;
	}
	@Override
	public boolean isRequestedSessionIdFromURL() {

		return false;
	}
	@Deprecated
	@Override
	public boolean isRequestedSessionIdFromUrl() {
		return false;
	}
	@Override
	public boolean isRequestedSessionIdValid() {

		return false;
	}
	@Override
	public boolean isUserInRole(String arg0) {

		return false;
	}
	@Override
	public void login(String arg0, String arg1) throws ServletException {
		//Intentionally left empty
	}
	@Override
	public void logout() throws ServletException {
		//Intentionally left empty
	}

}
