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
package com.braintribe.web.servlet.auth.cookie;

import java.util.function.Function;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.braintribe.cfg.Configurable;
import com.braintribe.logging.Logger;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.securityservice.OpenUserSessionWithUserAndPassword;
import com.braintribe.web.servlet.auth.Constants;
import com.braintribe.web.servlet.auth.CookieHandler;
import com.braintribe.web.servlet.auth.providers.CookieProvider;

public class DefaultCookieHandler implements CookieHandler {

	private static final Logger log = Logger.getLogger(DefaultCookieHandler.class);

	private int cookieExpiry = 24 * 60 * 60; // 24h
	private String cookiePath = null;
	private String cookieDomain = null;
	private Boolean cookieHttpOnly = Boolean.FALSE; // DEVCX-208: The Control Center cannot access the cookie anymore if this is true
	private Function<HttpServletRequest, Cookie> sessionCookieProvider = new CookieProvider(Constants.COOKIE_SESSIONID);
	private boolean addSessionCookie = true;

	@Override
	public Cookie ensureCookie(HttpServletRequest req, HttpServletResponse resp, String sessionId) {
		return this.ensureCookie(req, resp, sessionId, null);
	}

	@Override
	public Cookie ensureCookie(HttpServletRequest req, HttpServletResponse resp, String sessionId,
			OpenUserSessionWithUserAndPassword openUserSessionRequest) {

		if (!addSessionCookie) {
			log.trace(() -> "Session cookies are disabled.");
			return null;
		}

		Cookie sessionCookie = acquireCookie(req, sessionId);
		sessionCookie.setMaxAge(getMaxAge(req, openUserSessionRequest));

		String cookiePath = this.cookiePath;
		if (cookiePath == null) {
			cookiePath = "/"; // if not explicitly configured we enable this cookie for all paths on the domain.
		}
		sessionCookie.setPath(cookiePath);

		if (this.cookieDomain != null) {
			sessionCookie.setDomain(this.cookieDomain);
		}

		String scheme = req != null ? req.getScheme() : "https";
		if (scheme != null && scheme.equalsIgnoreCase("https")) {
			sessionCookie.setSecure(true);
		}

		if (cookieHttpOnly != null && cookieHttpOnly.booleanValue()) {
			sessionCookie.setHttpOnly(true);
		}

		resp.addCookie(sessionCookie);

		return sessionCookie;
	}

	@Override
	public void invalidateCookie(HttpServletRequest req, HttpServletResponse resp, String sessionId) {

		Cookie sessionCookie = sessionCookieProvider.apply(req);
		if (sessionCookie != null) {
			sessionCookie.setMaxAge(0);
			sessionCookie.setValue("");

			String cookiePath = this.cookiePath;
			if (cookiePath == null) {
				cookiePath = "/";
			}

			sessionCookie.setPath(cookiePath);

			if (this.cookieDomain != null) {
				sessionCookie.setDomain(this.cookieDomain);
			}

			String scheme = req.getScheme();
			if (scheme != null && scheme.equalsIgnoreCase("https")) {
				sessionCookie.setSecure(true);
			}

			if (cookieHttpOnly != null && cookieHttpOnly.booleanValue()) {
				sessionCookie.setHttpOnly(true);
			}

			resp.addCookie(sessionCookie);
		}
	}

	private Cookie acquireCookie(HttpServletRequest req, String sessionId) throws RuntimeException {
		Cookie sessionCookie = req != null ? sessionCookieProvider.apply(req) : null;
		if (sessionCookie == null) {
			sessionCookie = new Cookie(Constants.COOKIE_SESSIONID, sessionId);
		} else {
			sessionCookie.setValue(sessionId);
		}
		return sessionCookie;
	}

	private int getMaxAge(HttpServletRequest req, OpenUserSessionWithUserAndPassword openUserSessionRequest) {

		if (!offerStaySigned()) {
			if (log.isTraceEnabled())
				log.trace(Constants.TRIBEFIRE_RUNTIME_OFFER_STAYSIGNED
						+ " is disabled. Any user-session cookie will be invalid after a browser restart.");
			return -1;
		}

		if (req == null && openUserSessionRequest == null) {
			return -1;
		}

		boolean staySignedInRequested = false;
		if (openUserSessionRequest != null) {
			staySignedInRequested = openUserSessionRequest.getStaySignedIn();
		}
		String staySigned = req.getParameter(Constants.REQUEST_PARAM_STAYSIGNED);
		if (staySigned != null && staySigned.equals(Boolean.TRUE.toString())) {
			staySignedInRequested = true;
		}

		if (staySignedInRequested) {
			return cookieExpiry;
		} else {
			return -1;
		}
	}

	protected static boolean offerStaySigned() {
		String offerStayLoggedIn = TribefireRuntime.getProperty(Constants.TRIBEFIRE_RUNTIME_OFFER_STAYSIGNED);
		if (offerStayLoggedIn != null && offerStayLoggedIn.equalsIgnoreCase("false")) {
			return false;
		} else {
			return true;
		}
	}

	@Configurable
	public void setCookieExpiry(int cookieExpiry) {
		this.cookieExpiry = cookieExpiry;
	}
	@Configurable
	public void setCookiePath(String cookiePath) {
		this.cookiePath = cookiePath;
	}
	@Configurable
	public void setCookieDomain(String cookieDomain) {
		this.cookieDomain = cookieDomain;
	}
	@Configurable
	public void setCookieHttpOnly(Boolean cookieHttpOnly) {
		this.cookieHttpOnly = cookieHttpOnly;
	}
	@Configurable
	public void setSessionCookieProvider(Function<HttpServletRequest, Cookie> sessionCookieProvider) {
		this.sessionCookieProvider = sessionCookieProvider;
	}
	@Configurable
	public void setAddCookie(boolean addCookie) {
		this.addSessionCookie = addCookie;
	}
}
