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
package com.braintribe.web.cors.handler;

import javax.servlet.http.HttpServletRequest;

public enum CorsRequestType {

	actual, preflight, nonCors;

	public static CorsRequestType get(HttpServletRequest request) {

		String origin = request.getHeader(CorsHeaders.origin.getHeaderName());

		if (origin == null) {
			return nonCors;
		}

		if (isSameOrigin(request, origin)) {
			return nonCors;
		}

		if (isOptions(request) && request.getHeader(CorsHeaders.accessControlRequestMethod.getHeaderName()) != null) {
			return preflight;
		}

		return actual;

	}

	public static boolean isOptions(HttpServletRequest request) {
		return request.getMethod() != null && request.getMethod().equalsIgnoreCase("OPTIONS");
	}

	/**
	 * <p>
	 * Determines whether the {@code Origin} header matches the {@code request} target.
	 * 
	 * <p>
	 * Comparison is based on origin serialization rules defined at http://tools.ietf.org/html/rfc6454#section-6.1
	 * 
	 * @param request
	 *            The base for determining the CORS origin matching target (scheme://host:port)
	 * @param origin
	 *            The {@code Origin} header value
	 * @return Whether the {@code Origin} header matches the target represented by the given {@code request}
	 */
	private static boolean isSameOrigin(HttpServletRequest request, String origin) {

		String scheme = request.getScheme();
		if (scheme == null) {
			return false;
		}

		String host = request.getHeader(CorsHeaders.host.getHeaderName());
		if (host == null) {
			return false;
		}

		StringBuilder target = new StringBuilder(scheme.length() + host.length() + 10);
		target.append(scheme).append("://").append(host);

		int port = request.getServerPort();
		if (("http".equals(scheme) && port != 80 || "https".equals(scheme) && port != 443) && !host.contains(":")) {
			target.append(':').append(port);
		}

		return origin.equalsIgnoreCase(target.toString());

	}

}
