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
package com.braintribe.web.servlet.auth.providers;

import java.util.function.Function;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;

public class CookieProvider implements Function<HttpServletRequest, Cookie>{
	
	private String cookieName;
	
	public CookieProvider() {
		
	}
	public CookieProvider(String cookieName) {
		this.cookieName = cookieName;
	}
	
	@Configurable @Required
	public void setCookieName(String cookieName) {
		this.cookieName = cookieName;
	}
	
	
	@Override
	public Cookie apply(HttpServletRequest request) throws RuntimeException {
		if (request.getCookies() != null) {
			for (Cookie cookie : request.getCookies()) {
				if (cookie.getName().equals(cookieName)) {
					return cookie;
				}
			}
		}
		return null;
	}

}
