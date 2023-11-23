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
package com.braintribe.exception;

public class HttpException extends RuntimeException implements HasLogPreferences {

	private static final long serialVersionUID = 1L;

	private final int statusCode;
	private Object payload;
	private LogPreferences logPreferences;

	public HttpException(int status) {
		super();
		this.statusCode = status;
	}

	public HttpException(int status, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		this.statusCode = status;
	}

	public HttpException(int status, String message, Throwable cause) {
		super(message, cause);
		this.statusCode = status;
	}

	public HttpException(int status, String message) {
		super(message);
		this.statusCode = status;
	}

	public HttpException(int status, Throwable cause) {
		super(cause);
		this.statusCode = status;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public HttpException withPayload(Object payload) {
		this.payload = payload;
		return this;
	}

	public Object getPayload() {
		return payload;
	}

	public void setLogPreferences(LogPreferences logPreferences) {
		this.logPreferences = logPreferences;
	}

	@Override
	public LogPreferences getLogPreferences() {
		return logPreferences;
	}
}
