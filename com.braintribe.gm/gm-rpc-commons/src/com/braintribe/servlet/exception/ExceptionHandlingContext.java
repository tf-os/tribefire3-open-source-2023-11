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

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.braintribe.exception.HasLogPreferences;
import com.braintribe.exception.LogPreferences;

/**
 * Context object that gets created in the {@link ExceptionFilter} for a exception to be processed by
 * {@link ExceptionHandler} implementations.
 * 
 * @author roman.kurmanowytsch
 *
 */
public class ExceptionHandlingContext {

	private String tracebackId;
	private ServletRequest request;
	private ServletResponse response;
	private Throwable throwable;
	private boolean outputCommitted = false;
	private LogPreferences logPreferences = null;

	public ExceptionHandlingContext(String tracebackId, ServletRequest request, ServletResponse response, Throwable throwable) {
		super();
		this.tracebackId = tracebackId;
		this.request = request;
		this.response = response;
		this.throwable = throwable;
		if (throwable instanceof HasLogPreferences) {
			HasLogPreferences hll = (HasLogPreferences) throwable;
			this.logPreferences = hll.getLogPreferences();
		}
	}

	public boolean isOutputCommitted() {
		return outputCommitted;
	}
	public void setOutputCommitted(boolean outputCommitted) {
		this.outputCommitted = outputCommitted;
	}
	public String getTracebackId() {
		return tracebackId;
	}
	public ServletRequest getRequest() {
		return request;
	}
	public ServletResponse getResponse() {
		return response;
	}
	public Throwable getThrowable() {
		return throwable;
	}
	public LogPreferences getLogPreferences() {
		return logPreferences;
	}

}
