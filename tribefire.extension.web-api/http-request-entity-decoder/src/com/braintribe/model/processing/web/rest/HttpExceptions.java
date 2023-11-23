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

import javax.servlet.http.HttpServletResponse;

import com.braintribe.exception.HttpException;

public class HttpExceptions {
	
	public static void methodNotAllowed(String message, Object ...params) {
		httpException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, message, params);
	}

	public static void notAcceptable(String message, Object ...params) {
		httpException(HttpServletResponse.SC_NOT_ACCEPTABLE, message, params);
	}

	public static void preConditionFaild(String message, Object ...params) {
		httpException(HttpServletResponse.SC_PRECONDITION_FAILED, message, params);
	}

	public static void expectationFailed(String message, Object ...params) {
		httpException(HttpServletResponse.SC_EXPECTATION_FAILED, message, params);
	}

	public static void unauthotized(String message, Object ...params) {
		httpException(HttpServletResponse.SC_UNAUTHORIZED, message, params);
	}

	public static void notFound(String message, Object ...params) {
		httpException(HttpServletResponse.SC_NOT_FOUND, message, params);
	}
	
	public static void badRequest(String message, Object ...params) {
		httpException(HttpServletResponse.SC_BAD_REQUEST, message, params);
	}

	public static void notImplemented(String message, Object ...params) {
		httpException(HttpServletResponse.SC_NOT_IMPLEMENTED, message, params);
	}

	public static void internalServerError(String message, Object ...params) {
		httpException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message, params);
	}
	public static void httpException(int code, String message, Object ...params) {
		throw new HttpException(code, String.format(message, params));
	}
	
}
