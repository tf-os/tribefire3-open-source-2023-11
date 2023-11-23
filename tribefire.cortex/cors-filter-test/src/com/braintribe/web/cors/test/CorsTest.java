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

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.braintribe.model.cortex.deployment.cors.CorsConfiguration;
import com.braintribe.provider.Holder;
import com.braintribe.web.cors.CorsFilter;
import com.braintribe.web.cors.handler.BasicCorsHandler;
import com.braintribe.web.cors.handler.CorsHandler;
import com.braintribe.web.cors.handler.CorsHeaders;
import com.braintribe.web.cors.handler.CorsRequestType;

/**
 * <p>
 * Base class for CORS handling related tests.
 * 
 */
public abstract class CorsTest {

	protected static CorsConfiguration permissiveConfiguration;
	protected static CorsConfiguration strictConfiguration;

	@BeforeClass
	public static void createConfigurations() {

		// Permissive: Allows any origin, exposes custom headers, pre-flight is cached for 2 minutes, exposes cookies
		permissiveConfiguration = CorsConfiguration.T.create();
		permissiveConfiguration.setAllowAnyOrigin(true);
		permissiveConfiguration.setExposedHeaders(asSet("custom-header-1", "custom-header-2"));
		permissiveConfiguration.setMaxAge(120);
		permissiveConfiguration.setSupportedMethods(asSet("POST", "GET", "PUT", "DELETE"));
		permissiveConfiguration.setSupportsCredentials(true);

		// Strict: Allows only given origins, pre-flight results are no cached, no cookies are exposed
		strictConfiguration = CorsConfiguration.T.create();
		strictConfiguration.setAllowAnyOrigin(false);
		strictConfiguration.setAllowedOrigins(asSet("http://valid-origin-1", "http://valid-origin-2", "http://valid-origin-1:8080", "http://valid-origin-2:8080"));
		strictConfiguration.setMaxAge(0);
		strictConfiguration.setSupportedMethods(asSet("POST", "GET"));
		strictConfiguration.setSupportsCredentials(false);

	}

	protected static CorsHandler createBasicCorsHandler(CorsConfiguration corsConfiguration) {
		BasicCorsHandler handler = new BasicCorsHandler();
		handler.setConfiguration(corsConfiguration);
		return handler;
	}

	protected static CorsFilter createCorsFilter(CorsHandler handler) {
		CorsFilter filter = new CorsFilter();
		filter.setCorsHandler(handler);
		return filter;
	}

	protected static void assertCorsResponseHeaders(CorsRequestType requestType, CorsConfiguration config, HttpServletRequest request, HttpServletResponse response) {

		if (requestType == CorsRequestType.actual) {
			assertActualCorsRequestResponseHeaders(config, response);
		}

		if (requestType == CorsRequestType.preflight) {
			assertPreflightCorsRequestResponseHeaders(config, response);
		}

		// Common assertions to both actual and preflight:

		// -- Origin and Access-Control-Allow-Origin headers

		String requestOrigin = request.getHeader(CorsHeaders.origin.getHeaderName());
		String responseAccessControlAllowOrigin = response.getHeader(CorsHeaders.accessControlAllowOrigin.getHeaderName());

		Assert.assertEquals(requestOrigin, responseAccessControlAllowOrigin);

		// -- Access-Control-Allow-Credentials and Vary headers

		String responseAccessControlAllowCredentials = response.getHeader(CorsHeaders.accessControlAllowCredentials.getHeaderName());
		String responseVary = response.getHeader(CorsHeaders.vary.getHeaderName());

		if (config.getSupportsCredentials()) {
			Assert.assertNotNull("Missing response header " + CorsHeaders.accessControlAllowCredentials.getHeaderName(), responseAccessControlAllowCredentials);
			Assert.assertEquals("Unexpected response header " + CorsHeaders.accessControlAllowCredentials.getHeaderName() + " value: " + responseAccessControlAllowCredentials, "true", responseAccessControlAllowCredentials);
			Assert.assertTrue("When credentials are allowed, Vary header containing Origin is expected", responseVary != null && responseVary.contains("Origin"));
		}

	}

	/**
	 * <p>
	 * Assertions relevant only for actual requests.
	 */
	protected static void assertActualCorsRequestResponseHeaders(CorsConfiguration config, HttpServletResponse response) {

		// -- Access-Control-Expose-Headers

		if (config.getExposedHeaders() != null) {
			String responseExposedHeaders = response.getHeader(CorsHeaders.accessControlExposeHeaders.getHeaderName());
			Assert.assertNotNull("Missing response header " + CorsHeaders.accessControlExposeHeaders.getHeaderName(), responseExposedHeaders);
			for (String exposedHeader : config.getExposedHeaders()) {
				Assert.assertTrue("Response " + CorsHeaders.accessControlExposeHeaders.getHeaderName() + " header lacks of an expected value: " + exposedHeader, responseExposedHeaders.contains(exposedHeader));
			}
		}

	}

	/**
	 * <p>
	 * Assertions relevant only for pre-flight requests.
	 */
	protected static void assertPreflightCorsRequestResponseHeaders(CorsConfiguration config, HttpServletResponse response) {

		// -- Access-Control-Allow-Methods header

		String responseAccessControlAllowMethods = response.getHeader(CorsHeaders.accessControlAllowMethods.getHeaderName());

		Assert.assertNotNull("Missing response header " + CorsHeaders.accessControlAllowMethods.getHeaderName() + " header", responseAccessControlAllowMethods);
		if (config.getSupportedMethods() != null) {
			for (String supportedMethod : config.getSupportedMethods()) {
				Assert.assertTrue("Response " + CorsHeaders.accessControlAllowMethods.getHeaderName() + " header lacks of an expected value: " + supportedMethod, responseAccessControlAllowMethods.contains(supportedMethod));
			}
		}

		// -- Access-Control-Max-Age header

		if (config.getMaxAge() > 0) {
			String responseAccessControlMaxAge = response.getHeader(CorsHeaders.accessControlMaxAge.getHeaderName());
			Assert.assertNotNull("Missing response header " + CorsHeaders.accessControlMaxAge.getHeaderName(), responseAccessControlMaxAge);
			Assert.assertEquals("Unexpected response header " + CorsHeaders.accessControlMaxAge.getHeaderName() + " value:" + responseAccessControlMaxAge, Integer.toString(config.getMaxAge()), responseAccessControlMaxAge);
		}

	}

	protected HttpServletRequest createHttpServletRequestForPreflight(final String method, String scheme, final String host, final String origin) {

		Map<String, String> headers = new ConcurrentHashMap<>();

		headers.put(CorsHeaders.origin.getHeaderName(), origin);
		headers.put(CorsHeaders.host.getHeaderName(), host);
		headers.put(CorsHeaders.accessControlRequestMethod.getHeaderName(), method);

		return createHttpServletRequest("OPTIONS", scheme, headers);

	}

	protected HttpServletRequest createHttpServletRequest(final String method, String scheme, final String host, final String origin) {
		Map<String, String> headers = new ConcurrentHashMap<>();

		headers.put(CorsHeaders.origin.getHeaderName(), origin);
		headers.put(CorsHeaders.host.getHeaderName(), host);

		return createHttpServletRequest(method, scheme, headers);
	}

	protected HttpServletRequest createHttpServletRequest(final String method) {
		final Map<String, String> headers = new ConcurrentHashMap<>();
		return createHttpServletRequest(method, headers);
	}

	protected HttpServletRequest createHttpServletRequest(final String method, final Map<String, String> headers) {
		return createHttpServletRequest(method, null, headers);
	}

	protected HttpServletRequest createHttpServletRequest(final String method, String scheme, final Map<String, String> headers) {

		if (scheme == null || scheme.isEmpty()) {
			scheme = "http";
		}

		int port = 0;
		if ("http".equals(scheme)) {
			port = 80;
		} else if ("https".equals(scheme)) {
			port = 443;
		}

		return createHttpServletRequest(method, scheme, port, headers);

	}

	protected HttpServletRequest createHttpServletRequest(final String method, final String scheme, int port, final Map<String, String> headers) {

		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

		Mockito.doAnswer(new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				String key = invocation.getArgumentAt(0, String.class);
				Object value = headers.get(key);
				return value;
			}
		}).when(request).getHeader(Mockito.anyString());

		Mockito.when(request.getMethod()).thenReturn(method);
		Mockito.when(request.getScheme()).thenReturn(scheme);
		Mockito.when(request.getServerPort()).thenReturn(port);

		return request;
	}

	protected HttpServletResponse createHttpServletResponse() throws Exception {
		return createHttpServletResponse(new ConcurrentHashMap<String, String>());
	}

	protected HttpServletResponse createHttpServletResponse(final Map<String, String> headers) throws Exception {

		HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
		final Holder<Integer> status = new Holder<>();

		Mockito.doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				String key = invocation.getArgumentAt(0, String.class);
				String value = invocation.getArgumentAt(1, String.class);
				String currentValue = headers.get(key);
				if (currentValue != null) {
					headers.put(key, currentValue+", "+value);
				} else {
					headers.put(key, value);
				}
				return null;
			}
		}).when(response).addHeader(Mockito.anyString(), Mockito.anyString());

		Mockito.doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				String key = invocation.getArgumentAt(0, String.class);
				String value = invocation.getArgumentAt(1, String.class);
				headers.put(key, value);
				return null;
			}
		}).when(response).setHeader(Mockito.anyString(), Mockito.anyString());

		Mockito.doAnswer(new Answer<String>() {
			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				String key = invocation.getArgumentAt(0, String.class);
				String value = headers.get(key);
				return value;
			}
		}).when(response).getHeader(Mockito.anyString());

		Mockito.doAnswer(new Answer<PrintWriter>() {
			@Override
			public PrintWriter answer(InvocationOnMock invocation) throws Throwable {
				PrintWriter writer = new PrintWriter(System.out);
				return writer;
			}
		}).when(response).getWriter();

		Mockito.doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				Integer s = invocation.getArgumentAt(0, Integer.class);
				status.accept(s);
				return null;
			}
		}).when(response).setStatus(Mockito.anyInt());

		Mockito.doAnswer(new Answer<Integer>() {
			@Override
			public Integer answer(InvocationOnMock invocation) throws Throwable {
				return status.get();
			}
		}).when(response).getStatus();

		return response;

	}

	protected FilterChain createFilterChain() {
		FilterChain chain = Mockito.mock(FilterChain.class);
		return chain;
	}

	private static Set<String> asSet(String... strings) {
		return new HashSet<>(Arrays.asList(strings));
	}

}
