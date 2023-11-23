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
package com.braintribe.model.processing.ddra.endpoints;


import static com.braintribe.wire.api.util.Maps.entry;
import static com.braintribe.wire.api.util.Maps.linkedMap;
import static com.braintribe.wire.api.util.Sets.set;

import java.io.IOException;
import java.util.Map;

import javax.servlet.DispatcherType;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.braintribe.codec.marshaller.api.MarshallerRegistry;
import com.braintribe.exception.AuthorizationException;
import com.braintribe.model.processing.ddra.endpoints.ioc.TestMarshallerRegistry;
import com.braintribe.model.processing.securityservice.api.exceptions.SecurityServiceException;
import com.braintribe.model.processing.session.api.managed.NotFoundException;
import com.braintribe.processing.test.web.undertow.UndertowServer;
import com.braintribe.servlet.exception.ExceptionFilter;
import com.braintribe.servlet.exception.StandardExceptionHandler;
import com.braintribe.servlet.exception.StandardExceptionHandler.Exposure;

public abstract class AbstractDdraRestServletTest {

	protected static final String JSON = "application/json";
	protected static final String XML = "application/xml";
	protected static final String MULTIPART = "multipart/form-data";

	protected static UndertowServer server;
	protected static CloseableHttpClient httpClient;
	protected static TestHttpRequestFactory requests;
	
	protected static MarshallerRegistry marshallerRegistry = TestMarshallerRegistry.getMarshallerRegistry();

	
	protected static void setupStandardUndertowServer(String basePath, String servletName, HttpServlet servlet, String... mappings) {
		ExceptionFilter exceptionFilter = new ExceptionFilter();
		exceptionFilter.setExceptionHandlers(set(standardExceptionHandler()));

		//@formatter:off
		AbstractDdraRestServletTest.server =
				UndertowServer.create(basePath)
					.addFilter("exception-filter", exceptionFilter)
					.addFilterUrlMapping("exception-filter", "/*", DispatcherType.REQUEST)
					.addServlet(servletName, servlet, mappings)
					.start();
		//@formatter:on

		httpClient = HttpClients.createDefault();
		requests = new TestHttpRequestFactory(httpClient, server.getServerUrl());
	}
	
	
	protected static void destroy() throws IOException {
		server.stop();
		httpClient.close();
	}

	
	private static StandardExceptionHandler standardExceptionHandler() {
		StandardExceptionHandler bean = new StandardExceptionHandler();
		bean.setExceptionExposure(Exposure.auto);
		bean.setMarshallerRegistry(marshallerRegistry);
		bean.setStatusCodeMap(exceptionStatusCodeMap());
		return bean;
	}
	
	private static Map<Class<? extends Throwable>,Integer> exceptionStatusCodeMap() {
		//@formatter:off
		return linkedMap(
				entry(IllegalArgumentException.class, HttpServletResponse.SC_BAD_REQUEST),
				entry(UnsupportedOperationException.class, HttpServletResponse.SC_NOT_IMPLEMENTED),
				entry(NotFoundException.class, HttpServletResponse.SC_NOT_FOUND),
				entry(AuthorizationException.class, HttpServletResponse.SC_FORBIDDEN),
				entry(SecurityServiceException.class, HttpServletResponse.SC_FORBIDDEN)
		);
		//@formatter:on
	}
	

}
