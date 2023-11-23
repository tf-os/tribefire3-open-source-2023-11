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
package com.braintribe.servlet.exception;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.junit.BeforeClass;
import org.junit.Test;

import com.braintribe.codec.marshaller.api.ConfigurableMarshallerRegistry;
import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.codec.marshaller.api.MarshallerRegistry;
import com.braintribe.codec.marshaller.bin.Bin2Marshaller;
import com.braintribe.codec.marshaller.common.BasicConfigurableMarshallerRegistry;
import com.braintribe.codec.marshaller.json.JsonStreamMarshaller;
import com.braintribe.codec.marshaller.stax.StaxMarshaller;
import com.braintribe.exception.AuthorizationException;
import com.braintribe.exception.HttpException;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.manipulation.marshaller.ManMarshaller;
import com.braintribe.model.processing.securityservice.api.exceptions.InvalidSessionException;
import com.braintribe.model.processing.securityservice.api.exceptions.SecurityServiceException;
import com.braintribe.model.processing.securityservice.api.exceptions.UserNotFoundException;
import com.braintribe.model.processing.session.api.managed.NotFoundException;
import com.braintribe.servlet.exception.StandardExceptionHandler.Exposure;
import com.braintribe.servlet.test.mock.MockHttpServletRequest;
import com.braintribe.servlet.test.mock.MockHttpServletResponse;

public class StandardExceptionHandlerTest {

	private static MarshallerRegistry marshallerRegistry;

	@BeforeClass
	public static void beforeClass() throws Exception {
		marshallerRegistry = registry();
	}

	@Test
	public void testStatusCodes() throws Exception {

		Map<Throwable, Integer> expectedMap = new HashMap<>();

		IllegalArgumentException iae = new IllegalArgumentException("test");
		Exception wrapper = new Exception("wrapper", iae);
		expectedMap.put(wrapper, HttpServletResponse.SC_BAD_REQUEST);

		Exception exc = new Exception("Test");
		expectedMap.put(exc, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

		NotFoundException nfe = new NotFoundException("test");
		Exception wrapper2 = new Exception("wrapper", nfe);
		expectedMap.put(wrapper2, HttpServletResponse.SC_NOT_FOUND);

		AuthorizationException ae = new AuthorizationException("test");
		Exception wrapper3 = new Exception("wrapper", ae);
		expectedMap.put(wrapper3, HttpServletResponse.SC_FORBIDDEN);

		StandardExceptionHandler handler = new StandardExceptionHandler();
		handler.postConstruct();

		for (Map.Entry<Throwable, Integer> entry : expectedMap.entrySet()) {

			ExceptionHandlingContext context = createContext("tbid1", entry.getKey(), "text/plain");
			handler.apply(context);

			int statusCode = getStatusCode(context.getResponse());
			assertThat(statusCode).isEqualTo(entry.getValue());

		}

	}

	@Test
	public void testStatusCodesSubClasses() throws Exception {

		Map<Throwable, Integer> expectedMap = new HashMap<>();

		SecurityServiceException sse = new SecurityServiceException();
		expectedMap.put(sse, HttpServletResponse.SC_FORBIDDEN);

		UserNotFoundException unfe = new UserNotFoundException("test");
		expectedMap.put(unfe, HttpServletResponse.SC_FORBIDDEN);

		InvalidSessionException ise = new InvalidSessionException();
		expectedMap.put(ise, HttpServletResponse.SC_FORBIDDEN);

		StandardExceptionHandler handler = new StandardExceptionHandler();
		handler.postConstruct();

		for (Map.Entry<Throwable, Integer> entry : expectedMap.entrySet()) {

			ExceptionHandlingContext context = createContext("tbid1", entry.getKey(), "text/plain");
			handler.apply(context);

			int statusCode = getStatusCode(context.getResponse());
			assertThat(statusCode).isEqualTo(entry.getValue());

		}

	}

	@Test
	public void testTextOutput() throws Exception {

		StandardExceptionHandler handler = new StandardExceptionHandler();
		handler.setExceptionExposure(Exposure.full);

		ExceptionHandlingContext context = createContext("tbid1", "message1", "text/plain");
		handler.apply(context);
		String body = getBody(context.getResponse());
		assertThat(body).isNotBlank();
		assertThat(body).contains("tbid1");
		assertThat(body).contains("message1");
		assertThat(body).contains("at ");

		handler.setExceptionExposure(Exposure.messageOnly);
		context = createContext("tbid2", "message2", "text/plain");
		handler.apply(context);
		body = getBody(context.getResponse());
		assertThat(body).isNotBlank();
		assertThat(body).contains("tbid2");
		assertThat(body).contains("message2");
		assertThat(body).doesNotContain("at ");

		handler.setExceptionExposure(Exposure.none);
		context = createContext("tbid3", "message3", "text/plain");
		handler.apply(context);
		body = getBody(context.getResponse());
		assertThat(body).isNotBlank();
		assertThat(body).contains("tbid3");
		assertThat(body).doesNotContain("message3");
		assertThat(body).doesNotContain("at ");

		handler.setExceptionExposure(Exposure.auto);
		TribefireRuntime.setProperty(TribefireRuntime.ENVIRONMENT_EXCEPTION_MESSAGE_EXPOSITION, "true");
		TribefireRuntime.setProperty(TribefireRuntime.ENVIRONMENT_EXCEPTION_EXPOSITION, "true");
		context = createContext("tbid4", "message4", "text/plain");
		handler.apply(context);
		body = getBody(context.getResponse());
		assertThat(body).isNotBlank();
		assertThat(body).contains("tbid4");
		assertThat(body).contains("message4");
		assertThat(body).contains("at ");

		handler.setExceptionExposure(Exposure.auto);
		TribefireRuntime.setProperty(TribefireRuntime.ENVIRONMENT_EXCEPTION_MESSAGE_EXPOSITION, "true");
		TribefireRuntime.setProperty(TribefireRuntime.ENVIRONMENT_EXCEPTION_EXPOSITION, "false");
		context = createContext("tbid5", "message5", "text/plain");
		handler.apply(context);
		body = getBody(context.getResponse());
		assertThat(body).isNotBlank();
		assertThat(body).contains("tbid5");
		assertThat(body).contains("message5");
		assertThat(body).doesNotContain("at ");

		handler.setExceptionExposure(Exposure.auto);
		TribefireRuntime.setProperty(TribefireRuntime.ENVIRONMENT_EXCEPTION_MESSAGE_EXPOSITION, "false");
		TribefireRuntime.setProperty(TribefireRuntime.ENVIRONMENT_EXCEPTION_EXPOSITION, "false");
		context = createContext("tbid6", "message6", "text/plain");
		handler.apply(context);
		body = getBody(context.getResponse());
		assertThat(body).isNotBlank();
		assertThat(body).contains("tbid6");
		assertThat(body).doesNotContain("message6");
		assertThat(body).doesNotContain("at ");
	}

	@Test
	public void testJson() throws Exception {

		StandardExceptionHandler handler = new StandardExceptionHandler();
		handler.setMarshallerRegistry(marshallerRegistry);
		handler.setExceptionExposure(Exposure.full);

		ExceptionHandlingContext context = createContext("tbid1", "message1", "application/json");
		handler.apply(context);
		String body = getBody(context.getResponse());
		assertThat(body).isNotBlank();
		assertThat(body).contains("tbid1");
		assertThat(body).contains("message1");
		assertThat(body).contains(this.getClass().getName());

		handler.setExceptionExposure(Exposure.messageOnly);
		context = createContext("tbid2", "message2", "application/json");
		handler.apply(context);
		body = getBody(context.getResponse());
		assertThat(body).isNotBlank();
		assertThat(body).contains("tbid2");
		assertThat(body).contains("message2");
		assertThat(body).doesNotContain(this.getClass().getName());

		handler.setExceptionExposure(Exposure.none);
		context = createContext("tbid3", "message3", "application/json");
		handler.apply(context);
		body = getBody(context.getResponse());
		assertThat(body).isNotBlank();
		assertThat(body).contains("tbid3");
		assertThat(body).doesNotContain("message3");
		assertThat(body).doesNotContain(this.getClass().getName());

	}

	@Test
	public void testMultiAccept() throws Exception {

		StandardExceptionHandler handler = new StandardExceptionHandler();
		handler.setMarshallerRegistry(marshallerRegistry);
		handler.setExceptionExposure(Exposure.full);

		ExceptionHandlingContext context = createContext("tbid1", "message1", "application/json;q=0.9, application/xml");
		handler.apply(context);
		String body = getBody(context.getResponse());
		assertThat(body).isNotBlank();
		assertThat(body).contains("tbid1");
		assertThat(body).contains("message1");
		assertThat(body).contains(this.getClass().getName());
		assertThat(body.trim()).startsWith("{");

		context = createContext("tbid2", "message2", "type1/subtype1, type2/subtype2");
		((MockHttpServletRequest) context.getRequest()).addHeader("Accept", "application/json");
		handler.apply(context);
		body = getBody(context.getResponse());
		assertThat(body).isNotBlank();
		assertThat(body).contains("tbid2");
		assertThat(body).contains("message2");
		assertThat(body).contains(this.getClass().getName());
		assertThat(body.trim()).startsWith("{");

	}

	@Test
	public void testXml() throws Exception {

		StandardExceptionHandler handler = new StandardExceptionHandler();
		handler.setMarshallerRegistry(marshallerRegistry);
		handler.setExceptionExposure(Exposure.full);

		ExceptionHandlingContext context = createContext("tbid1", "message1", "gm/xml");
		handler.apply(context);
		String body = getBody(context.getResponse());
		assertThat(body).isNotBlank();
		assertThat(body).contains("tbid1");
		assertThat(body).contains("message1");
		assertThat(body).contains(this.getClass().getName());
		assertThat(body).contains("<?gm-xml");

	}

	@Test
	public void testUnexposed400Exception() throws Exception {

		StandardExceptionHandler handler = new StandardExceptionHandler();
		handler.setMarshallerRegistry(marshallerRegistry);
		handler.setExceptionExposure(Exposure.messageOnly);

		ExceptionHandlingContext context = createContext("tbid1", "message1", 400, "application/json");
		handler.apply(context);
		String body = getBody(context.getResponse());
		assertThat(body).isNotBlank();
		assertThat(body).contains("tbid1");
		assertThat(body).contains("message1");
		assertThat(body).doesNotContain(this.getClass().getName());

		context = createContext("tbid2", "message2", 400, "text/plain");
		handler.apply(context);
		body = getBody(context.getResponse());
		assertThat(body).isNotBlank();
		assertThat(body).contains("tbid2");
		assertThat(body).contains("message2");
		assertThat(body).doesNotContain(this.getClass().getName());

	}

	private static String getBody(ServletResponse response) {
		if (response instanceof MockHttpServletResponse) {
			MockHttpServletResponse r = (MockHttpServletResponse) response;
			byte[] bytes = r.getBytes();
			try {
				return new String(bytes, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
		}
		return null;
	}

	private static int getStatusCode(ServletResponse response) {
		if (response instanceof MockHttpServletResponse) {
			MockHttpServletResponse r = (MockHttpServletResponse) response;
			return r.getStatus();
		}
		return -2;
	}

	private static ExceptionHandlingContext createContext(String tbid, Throwable t, String... mimeTypes) {

		MockHttpServletRequest request = new MockHttpServletRequest();
		if (mimeTypes != null) {
			for (String m : mimeTypes) {
				request.addHeader("Accept", m);
			}
		}
		MockHttpServletResponse response = new MockHttpServletResponse();
		ExceptionHandlingContext context = new ExceptionHandlingContext(tbid, request, response, t);

		return context;
	}

	private static ExceptionHandlingContext createContext(String tbid, String throwableMessage, String... mimeTypes) {

		Throwable t = new Throwable(throwableMessage);
		MockHttpServletRequest request = new MockHttpServletRequest();
		if (mimeTypes != null) {
			for (String m : mimeTypes) {
				request.addHeader("Accept", m);
			}
		}
		MockHttpServletResponse response = new MockHttpServletResponse();
		ExceptionHandlingContext context = new ExceptionHandlingContext(tbid, request, response, t);

		return context;
	}

	private static ExceptionHandlingContext createContext(String tbid, String throwableMessage, int statusCode, String... mimeTypes) {

		HttpException t = new HttpException(statusCode, throwableMessage);

		MockHttpServletRequest request = new MockHttpServletRequest();
		if (mimeTypes != null) {
			for (String m : mimeTypes) {
				request.addHeader("Accept", m);
			}
		}
		MockHttpServletResponse response = new MockHttpServletResponse();
		ExceptionHandlingContext context = new ExceptionHandlingContext(tbid, request, response, t);

		return context;
	}

	private static ConfigurableMarshallerRegistry registry() {
		BasicConfigurableMarshallerRegistry bean = new BasicConfigurableMarshallerRegistry();

		bean.registerMarshaller("text/xml", xmlMarshaller());
		bean.registerMarshaller("gm/xml", xmlMarshaller());
		bean.registerMarshaller("gm/bin", binMarshaller());
		bean.registerMarshaller("application/json", jsonMarshaller());
		bean.registerMarshaller("gm/man", manMarshaller());

		return bean;

	}
	private static Marshaller xmlMarshaller() {
		StaxMarshaller bean = new StaxMarshaller();
		return bean;
	}
	private static Marshaller jsonMarshaller() {
		JsonStreamMarshaller bean = new JsonStreamMarshaller();
		return bean;
	}
	private static Marshaller binMarshaller() {
		Bin2Marshaller bean = new Bin2Marshaller();
		return bean;
	}
	private static Marshaller manMarshaller() {
		ManMarshaller bean = new ManMarshaller();
		return bean;
	}

}
