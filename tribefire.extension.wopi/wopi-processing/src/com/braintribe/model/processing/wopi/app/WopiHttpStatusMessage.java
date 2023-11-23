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
package com.braintribe.model.processing.wopi.app;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import com.braintribe.logging.Logger;
import com.braintribe.model.processing.wopi.misc.HttpResponseMessage;

public class WopiHttpStatusMessage {

	private static final Logger logger = Logger.getLogger(WopiHttpStatusMessage.class);

	// -----------------------------------------------------------------------
	// STATUS MESSAGES
	// -----------------------------------------------------------------------

	/** 200 Success */
	public static HttpResponseMessage returnSuccess() {
		return returnSuccess(null);
	}

	/** 200 Success */
	public static HttpResponseMessage returnSuccess(Map<String, String> header) {
		HttpResponseMessage response = new HttpResponseMessage(HttpServletResponse.SC_OK);
		if (header != null) {
			header.forEach((k, v) -> response.addHeader(k, v));
		}
		return response;
	}

	/** 401 Invalid Token */
	public static HttpResponseMessage returnInvalidToken(String msg) {
		logger.debug(() -> msg);
		return new HttpResponseMessage(HttpServletResponse.SC_UNAUTHORIZED);
	}

	/** 404 File Unknown/User Unauthorized */
	public static HttpResponseMessage returnFileUnknown(String msg) {
		logger.debug(() -> "Not Found: " + msg);
		return new HttpResponseMessage(HttpServletResponse.SC_NOT_FOUND);
	}

	/** 409 Lock mismatch/Locked by another interface */
	public static HttpResponseMessage returnLockMismatch(String existingLock, String reason) {
		logger.debug(() -> "Conflict: " + (reason == null ? "?" : reason));
		HttpResponseMessage response = new HttpResponseMessage(HttpServletResponse.SC_CONFLICT);
		response.addHeader(WopiHeader.Lock.key(), existingLock != null ? existingLock : "");
		if (StringUtils.isNotEmpty(reason)) {
			response.addHeader(WopiHeader.LockFailureReason.key(), reason);
		}
		return response;
	}

	/** 500 Server Error" */
	public static HttpResponseMessage returnServerError(Throwable ex) {
		logger.error(() -> "Internal Server Error", ex);
		return new HttpResponseMessage(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	}

	/** 501 Unsupported */
	public static HttpResponseMessage returnUnsupported(String msg) {
		logger.warn(() -> "Not Implemented: " + msg);
		return new HttpResponseMessage(HttpServletResponse.SC_NOT_IMPLEMENTED);
	}
}
