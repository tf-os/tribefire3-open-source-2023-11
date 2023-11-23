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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.web.cors.handler.CorsHeaders;
import com.braintribe.web.cors.handler.CorsRequestType;

/**
 * <p>
 * Tests on {@link CorsRequestType} detection.
 * 
 */
public class CorsRequestTypeDetectionTest extends CorsTest {

	@Test
	public void testNonCorsPostRequestDetectionByLackOfOrigin() {
		HttpServletRequest request = createHttpServletRequest("POST");
		assertCorsRequestTypeDetection(CorsRequestType.nonCors, request);
	}

	@Test
	public void testNonCorsGetRequestGetDetectionByLackOfOrigin() {
		HttpServletRequest request = createHttpServletRequest("GET");
		assertCorsRequestTypeDetection(CorsRequestType.nonCors, request);
	}

	@Test
	public void testNonCorsPostRequestDetectionBySameOrigin() {

		Map<String, String> headers = new ConcurrentHashMap<>();
		headers.put(CorsHeaders.origin.getHeaderName(), "http://localhost");
		headers.put(CorsHeaders.host.getHeaderName(), "localhost");

		HttpServletRequest request = createHttpServletRequest("POST", "http", 80, headers);
		assertCorsRequestTypeDetection(CorsRequestType.nonCors, request);

	}

	@Test
	public void testNonCorsGetRequestDetectionBySameOrigin() {

		Map<String, String> headers = new ConcurrentHashMap<>();
		headers.put(CorsHeaders.origin.getHeaderName(), "http://localhost");
		headers.put(CorsHeaders.host.getHeaderName(), "localhost");

		HttpServletRequest request = createHttpServletRequest("GET", "http", 80, headers);
		assertCorsRequestTypeDetection(CorsRequestType.nonCors, request);

	}

	@Test
	public void testNonCorsPostRequestDetectionBySameOriginWithExplicitPort() {

		Map<String, String> headers = new ConcurrentHashMap<>();
		headers.put(CorsHeaders.origin.getHeaderName(), "http://localhost:8080");
		headers.put(CorsHeaders.host.getHeaderName(), "localhost");

		HttpServletRequest request = createHttpServletRequest("POST", "http", 8080, headers);
		assertCorsRequestTypeDetection(CorsRequestType.nonCors, request);

	}

	@Test
	public void testNonCorsGetRequestDetectionBySameOriginWithExplicitPort() {

		Map<String, String> headers = new ConcurrentHashMap<>();
		headers.put(CorsHeaders.origin.getHeaderName(), "http://localhost:8080");
		headers.put(CorsHeaders.host.getHeaderName(), "localhost");

		HttpServletRequest request = createHttpServletRequest("GET", "http", 8080, headers);
		assertCorsRequestTypeDetection(CorsRequestType.nonCors, request);

	}

	@Test
	public void testNonCorsPostRequestDetectionBySameOriginIncludingHostPort() {

		Map<String, String> headers = new ConcurrentHashMap<>();
		headers.put(CorsHeaders.origin.getHeaderName(), "http://localhost:8080");
		headers.put(CorsHeaders.host.getHeaderName(), "localhost:8080");

		HttpServletRequest request = createHttpServletRequest("POST", "http", 8080, headers);
		assertCorsRequestTypeDetection(CorsRequestType.nonCors, request);

	}

	@Test
	public void testNonCorsGetRequestDetectionBySameOriginIncludingHostPort() {

		Map<String, String> headers = new ConcurrentHashMap<>();
		headers.put(CorsHeaders.origin.getHeaderName(), "http://localhost:8080");
		headers.put(CorsHeaders.host.getHeaderName(), "localhost:8080");

		HttpServletRequest request = createHttpServletRequest("GET", "http", 8080, headers);
		assertCorsRequestTypeDetection(CorsRequestType.nonCors, request);

	}

	@Test
	public void testPreflightOfPostRequestDetection() {

		Map<String, String> headers = new ConcurrentHashMap<>();
		headers.put(CorsHeaders.origin.getHeaderName(), "http://localhost:8080");
		headers.put(CorsHeaders.accessControlRequestMethod.getHeaderName(), "POST");

		HttpServletRequest request = createHttpServletRequest("OPTIONS", "http", headers);
		assertCorsRequestTypeDetection(CorsRequestType.preflight, request);

	}

	@Test
	public void testPreflightOfGetRequestDetection() {

		Map<String, String> headers = new ConcurrentHashMap<>();
		headers.put(CorsHeaders.origin.getHeaderName(), "http://localhost:8080");
		headers.put(CorsHeaders.accessControlRequestMethod.getHeaderName(), "GET");

		HttpServletRequest request = createHttpServletRequest("OPTIONS", "http", headers);
		assertCorsRequestTypeDetection(CorsRequestType.preflight, request);

	}

	@Test
	public void testCrossOriginGetRequestDetectionByOriginHostDifference() {

		Map<String, String> headers = new ConcurrentHashMap<>();
		headers.put(CorsHeaders.origin.getHeaderName(), "http://localhost:8080");
		headers.put(CorsHeaders.host.getHeaderName(), "otherhost:8080");

		HttpServletRequest request = createHttpServletRequest("GET", "http", headers);
		assertCorsRequestTypeDetection(CorsRequestType.actual, request);

	}

	@Test
	public void testCrossOriginPostRequestDetectionByOriginHostDifference() {

		Map<String, String> headers = new ConcurrentHashMap<>();
		headers.put(CorsHeaders.origin.getHeaderName(), "http://localhost:8080");
		headers.put(CorsHeaders.host.getHeaderName(), "otherhost:8080");

		HttpServletRequest request = createHttpServletRequest("POST", "http", headers);
		assertCorsRequestTypeDetection(CorsRequestType.actual, request);

	}

	@Test
	public void testCrossOriginGetRequestDetectionByOriginSchemeDifference() {

		Map<String, String> headers = new ConcurrentHashMap<>();
		headers.put(CorsHeaders.origin.getHeaderName(), "http://localhost:8080");
		headers.put(CorsHeaders.host.getHeaderName(), "localhost:8080");

		HttpServletRequest request = createHttpServletRequest("GET", "https", headers);
		assertCorsRequestTypeDetection(CorsRequestType.actual, request);

	}

	@Test
	public void testCrossOriginPostRequestDetectionByOriginSchemeDifference() {

		Map<String, String> headers = new ConcurrentHashMap<>();
		headers.put(CorsHeaders.origin.getHeaderName(), "http://localhost:8080");
		headers.put(CorsHeaders.host.getHeaderName(), "localhost");

		HttpServletRequest request = createHttpServletRequest("POST", "https", headers);
		assertCorsRequestTypeDetection(CorsRequestType.actual, request);

	}

	@Test
	public void testCrossOriginGetRequestDetectionByOriginPortDifference() {

		Map<String, String> headers = new ConcurrentHashMap<>();
		headers.put(CorsHeaders.origin.getHeaderName(), "https://localhost:8080");
		headers.put(CorsHeaders.host.getHeaderName(), "localhost");

		HttpServletRequest request = createHttpServletRequest("GET", "https", headers);
		assertCorsRequestTypeDetection(CorsRequestType.actual, request);

	}

	@Test
	public void testCrossOriginPostRequestDetectionByOriginPortDifference() {

		Map<String, String> headers = new ConcurrentHashMap<>();
		headers.put(CorsHeaders.origin.getHeaderName(), "http://localhost:8080");
		headers.put(CorsHeaders.host.getHeaderName(), "localhost:8080");

		HttpServletRequest request = createHttpServletRequest("POST", "https", headers);
		assertCorsRequestTypeDetection(CorsRequestType.actual, request);

	}

	protected void assertCorsRequestTypeDetection(CorsRequestType expectedType, HttpServletRequest request) {
		CorsRequestType actualType = CorsRequestType.get(request);
		Assert.assertEquals("Unexpected CORS request type: [" + actualType + "] expected was [" + expectedType + "]", expectedType, actualType);
	}

}
