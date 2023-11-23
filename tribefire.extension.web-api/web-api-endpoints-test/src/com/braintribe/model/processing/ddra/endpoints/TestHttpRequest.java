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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.junit.Assert;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.ddra.endpoints.ioc.TestMarshallerRegistry;

/**
 * A little helper to execute http requests against a client with a little gm marshalling support as well. <p>
 * NOTE that this class is mutable even after calling the {@link #execute()} method.
 */
public class TestHttpRequest {

	private final HttpRequestBase request;

	private final HttpClient client;

	private Boolean testUrlParams;

	public TestHttpRequest(HttpClient client, HttpRequestBase request) {
		this.client = client;
		this.request = request;
		
		RequestConfig requestConfig = RequestConfig.custom() //
				  .setSocketTimeout(500000) //
				  .setConnectTimeout(100) //
				  .setConnectionRequestTimeout(100) //
				  .build();
		
		request.setConfig(requestConfig);
	}

	public TestHttpRequest accept(String... mimeTypes) {
		return header("Accept", mimeTypes);
	}

	public TestHttpRequest contentType(String mimeType) {
		return header("Content-Type", mimeType);
	}

	public TestHttpRequest header(String name, String... values) {
		for (String value : values) {
			request.addHeader(name, value);
		}
		return this;
	}

	public TestHttpRequest urlParameter(String name, String... values) {
		URIBuilder builder = new URIBuilder(request.getURI());
		for (String value : values) {
			builder.addParameter(name, value);
		}
		setURI(builder);
		return this;
	}
	
	/**
	 * delegates to {@link #header(String, String...)} or {@link #urlParameter(String, String...)} depending on whether 
	 * you called {@link #testUrlParams} or {@link #testHeaderParams()} before
	 */
	public TestHttpRequest paramViaHeaderOrUrl(String alias, String... values) {
		if (testUrlParams == null) {
			throw new IllegalStateException("Please define first via the testUrlParams() or testHeaderParams() method which kind of params you want to test or call urlParameter() or header() directly");
		} else if (testUrlParams) {
			urlParameter(alias, values);
		} else {
			header("gm-" + alias, values);
		}
		
		return this;
	}
	
	public TestHttpRequest testUrlParams() {
		testUrlParams = true;
		return this;
	}
	
	public TestHttpRequest testHeaderParams() {
		testUrlParams = false;
		return this;
	}

	public TestHttpRequest path(String path) {
		URIBuilder builder = new URIBuilder(request.getURI());
		builder.setPath(path);
		setURI(builder);
		return this;
	}

	public TestHttpRequest body(String content) {
		((HttpEntityEnclosingRequestBase) request).setEntity(createHttpEntity(content));
		return this;
	}
	public TestHttpRequest body(byte[] content) {
		((HttpEntityEnclosingRequestBase) request).setEntity(createHttpEntity(content));
		return this;
	}

	public TestHttpRequest body(GenericEntity entity, String mimeType) {
		((HttpEntityEnclosingRequestBase) request).setEntity(createHttpEntity(entity, mimeType));
		return this;
	}

	private HttpEntity createHttpEntity(String content) {
		return createHttpEntity(content.getBytes());
	}
	
	private HttpEntity createHttpEntity(byte[] content) {
		return new ByteArrayEntity(content);
	}

	private HttpEntity createHttpEntity(GenericEntity entity, String mimeType) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		TestMarshallerRegistry.getMarshallerRegistry().getMarshaller(mimeType).marshall(out, entity);
		byte[] data = out.toByteArray();
		try {
			out.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return new ByteArrayEntity(data, getContentType(mimeType));
	}

	private ContentType getContentType(String mimeType) {
		if ("application/json".equals(mimeType)) {
			return ContentType.APPLICATION_JSON;
		}
		if ("application/xml".equals(mimeType)) {
			return ContentType.APPLICATION_XML;
		}
		throw new RuntimeException("Unsupported mimeType: " + mimeType);
	}

	public HttpRequestBase getRequest() {
		return request;
	}

	private void setURI(URIBuilder builder) {
		try {
			request.setURI(builder.build());
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	public TestHttpResponse execute() {
		try {
			return new TestHttpResponse(client.execute(request ));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public <T> T execute(int expectedResponseCode) {
		TestHttpResponse response = null;
		try {
			response = execute();
			Assert.assertEquals(expectedResponseCode, response.getStatusCode());
			return response.getContent();
		} finally {
			if (response != null)
				response.consumeEntity();
		}
	}
}
