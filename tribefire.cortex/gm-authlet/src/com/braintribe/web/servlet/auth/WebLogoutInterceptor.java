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
package com.braintribe.web.servlet.auth;

import com.braintribe.cfg.Required;
import com.braintribe.model.processing.service.api.ServicePostProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.util.servlet.HttpServletArguments;
import com.braintribe.util.servlet.HttpServletArgumentsAttribute;

public class WebLogoutInterceptor implements ServicePostProcessor<Object> {
	private CookieHandler cookieHandler;

	@Required
	public void setCookieHandler(CookieHandler cookieHandler) {
		this.cookieHandler = cookieHandler;
	}

	@Override
	public Object process(ServiceRequestContext requestContext, Object response) {
		String sessionId = requestContext.getRequestorSessionId();

		if (sessionId == null)
			return response;

		requestContext.findAttribute(HttpServletArgumentsAttribute.class).ifPresent(a -> clearCookie(sessionId, a));
		return response;
	}

	private void clearCookie(String sessionId, HttpServletArguments servletArguments) {
		cookieHandler.invalidateCookie(servletArguments.getRequest(), servletArguments.getResponse(), sessionId);
	}
}
