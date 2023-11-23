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
package com.braintribe.web.cors.test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

import com.braintribe.web.cors.exception.OriginDeniedException;
import com.braintribe.web.cors.exception.UnsupportedMethodException;
import com.braintribe.web.cors.handler.CorsHandler;
import com.braintribe.web.cors.handler.CorsRequestType;

/**
 * <p>
 * Tests covering {@link com.braintribe.web.cors.handler.BasicCorsHandler} public methods:
 * 
 * <ul>
 * <li>{@link com.braintribe.web.cors.handler.BasicCorsHandler#handleActual(HttpServletRequest, HttpServletResponse)}
 * <li>{@link com.braintribe.web.cors.handler.BasicCorsHandler#handlePreflight(HttpServletRequest, HttpServletResponse)}
 * </ul>
 * 
 */
public class BasicCorsHandlerTest extends CorsTest {

	// =====================//
	// == ACTUAL REQUESTS ==//
	// =====================//

	@Test
	public void testSuccessfulCrossOriginRequestWithPermissiveConfiguration() throws Exception {

		HttpServletRequest request = createHttpServletRequest("POST", "http", "server-host", "http://any-origin");
		HttpServletResponse response = createHttpServletResponse();

		CorsHandler corsHandler = createBasicCorsHandler(permissiveConfiguration);

		corsHandler.handleActual(request, response);

		assertCorsResponseHeaders(CorsRequestType.actual, permissiveConfiguration, request, response);

	}

	@Test
	public void testSuccessfulCrossOriginRequestWithStrictConfiguration() throws Exception {

		HttpServletRequest request = createHttpServletRequest("POST", "http", "server-host", "http://valid-origin-1");
		HttpServletResponse response = createHttpServletResponse();

		CorsHandler corsHandler = createBasicCorsHandler(strictConfiguration);

		corsHandler.handleActual(request, response);

		assertCorsResponseHeaders(CorsRequestType.actual, strictConfiguration, request, response);

	}

	@Test
	public void testSuccessfulCrossOriginRequestWithStrictConfigurationAndNonStandardPort() throws Exception {

		HttpServletRequest request = createHttpServletRequest("POST", "http", "server-host", "http://valid-origin-1:8080");
		HttpServletResponse response = createHttpServletResponse();

		CorsHandler corsHandler = createBasicCorsHandler(strictConfiguration);

		corsHandler.handleActual(request, response);

		assertCorsResponseHeaders(CorsRequestType.actual, strictConfiguration, request, response);

	}

	@Test(expected = OriginDeniedException.class)
	public void testCrossOriginRequestFromUnallowedOrigin() throws Exception {

		HttpServletRequest request = createHttpServletRequest("POST", "http", "server-host", "http://any-origin");
		HttpServletResponse response = createHttpServletResponse();

		CorsHandler corsHandler = createBasicCorsHandler(strictConfiguration);

		corsHandler.handleActual(request, response);

	}

	@Test(expected = OriginDeniedException.class)
	public void testCrossOriginRequestFromUnallowedOriginPort() throws Exception {

		HttpServletRequest request = createHttpServletRequest("POST", "http", "server-host", "http://valid-origin-1:7777");
		HttpServletResponse response = createHttpServletResponse();

		CorsHandler corsHandler = createBasicCorsHandler(strictConfiguration);

		corsHandler.handleActual(request, response);

	}

	// =========================//
	// == PRE FLIGHT REQUESTS ==//
	// =========================//

	@Test(expected = UnsupportedMethodException.class)
	public void testCrossOriginRequestFromUnallowedMethod() throws Exception {

		HttpServletRequest request = createHttpServletRequest("DELETE", "http", "server-host", "http://valid-origin-1");
		HttpServletResponse response = createHttpServletResponse();

		CorsHandler corsHandler = createBasicCorsHandler(strictConfiguration);

		corsHandler.handleActual(request, response);

	}

	@Test
	public void testSuccessfulPreflightRequestWithPermissiveConfiguration() throws Exception {

		HttpServletRequest request = createHttpServletRequestForPreflight("POST", "http", "server-host", "http://any-origin");
		HttpServletResponse response = createHttpServletResponse();

		CorsHandler corsHandler = createBasicCorsHandler(permissiveConfiguration);

		corsHandler.handlePreflight(request, response);

		assertCorsResponseHeaders(CorsRequestType.preflight, permissiveConfiguration, request, response);

	}

	@Test
	public void testSuccessfulPreflightRequestWithStrictConfigurationAndNonStandardPort() throws Exception {

		HttpServletRequest request = createHttpServletRequestForPreflight("POST", "http", "server-host", "http://valid-origin-1:8080");
		HttpServletResponse response = createHttpServletResponse();

		CorsHandler corsHandler = createBasicCorsHandler(strictConfiguration);

		corsHandler.handlePreflight(request, response);

		assertCorsResponseHeaders(CorsRequestType.preflight, strictConfiguration, request, response);

	}

	@Test
	public void testSuccessfulPreflightRequestWithStrictConfiguration() throws Exception {

		HttpServletRequest request = createHttpServletRequestForPreflight("POST", "http", "server-host", "http://valid-origin-1");
		HttpServletResponse response = createHttpServletResponse();

		CorsHandler corsHandler = createBasicCorsHandler(strictConfiguration);

		corsHandler.handlePreflight(request, response);

		assertCorsResponseHeaders(CorsRequestType.preflight, strictConfiguration, request, response);

	}

	@Test(expected = OriginDeniedException.class)
	public void testPreflightRequestFromUnallowedOrigin() throws Exception {

		HttpServletRequest request = createHttpServletRequestForPreflight("POST", "http", "server-host", "http://any-origin");
		HttpServletResponse response = createHttpServletResponse();

		CorsHandler corsHandler = createBasicCorsHandler(strictConfiguration);

		corsHandler.handlePreflight(request, response);

	}

	@Test(expected = OriginDeniedException.class)
	public void testPreflightRequestFromUnallowedOriginPort() throws Exception {

		HttpServletRequest request = createHttpServletRequestForPreflight("POST", "http", "server-host", "http://valid-origin-1:7777");
		HttpServletResponse response = createHttpServletResponse();

		CorsHandler corsHandler = createBasicCorsHandler(strictConfiguration);

		corsHandler.handlePreflight(request, response);

	}

}
