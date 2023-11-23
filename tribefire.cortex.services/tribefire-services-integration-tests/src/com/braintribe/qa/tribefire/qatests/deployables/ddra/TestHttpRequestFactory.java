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
package com.braintribe.qa.tribefire.qatests.deployables.ddra;

import java.net.URISyntaxException;
import java.net.URL;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;

public class TestHttpRequestFactory {

	private final HttpClient client;
	
	private final URL serverUrl;

	public TestHttpRequestFactory(HttpClient client, URL serverUrl) {
		super();
		this.client = client;
		this.serverUrl = serverUrl;
	}

	public TestHttpRequest get() {
		return request(RequestMethod.get);
	}
	
	public TestHttpRequest post() {
		return request(RequestMethod.post);
	}
	
	public TestHttpRequest put() {
		return request(RequestMethod.put);
	}

	public TestHttpRequest delete() {
		return request(RequestMethod.delete);
	}

	public TestHttpRequest request(RequestMethod method) {
		try {
			switch (method) {
				case get:
					return new TestHttpRequest(client, new HttpGet(serverUrl.toURI()));
				case post:
					return new TestHttpRequest(client, new HttpPost(serverUrl.toURI()));
				case put:
					return new TestHttpRequest(client, new HttpPut(serverUrl.toURI()));
				case delete:
					return new TestHttpRequest(client, new HttpDelete(serverUrl.toURI()));
				default:
					throw new UnsupportedOperationException("Method " + method + " not supported.");
			}
		} catch(URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
}
