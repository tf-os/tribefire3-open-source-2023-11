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


public class CookieValueProvider implements Function<HttpServletRequest, String>{
	
	private Function<HttpServletRequest, Cookie> cookieProvider;

	public CookieValueProvider() {
		
	}
	
	public CookieValueProvider(Function<HttpServletRequest, Cookie> cookieProvider) {
		this.cookieProvider = cookieProvider;
	}

	@Configurable
	public void setCookieProvider(
			Function<HttpServletRequest, Cookie> cookieProvider) {
		this.cookieProvider = cookieProvider;
	}
	

	@Override
	public String apply(HttpServletRequest request) throws RuntimeException {
		Cookie cookie = cookieProvider.apply(request);
		if (cookie != null)
			return cookie.getValue();
		return null;
	}

}
