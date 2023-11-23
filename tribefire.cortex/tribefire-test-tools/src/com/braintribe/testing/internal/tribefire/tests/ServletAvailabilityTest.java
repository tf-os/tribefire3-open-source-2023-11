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
package com.braintribe.testing.internal.tribefire.tests;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.BeforeClass;

import com.braintribe.product.rat.imp.ImpApiFactory;
import com.braintribe.utils.IOTools;

public abstract class ServletAvailabilityTest extends AbstractTribefireQaTest {

	protected static String sessionId;
	private static String publicServicesUrl;

	@BeforeClass
	public static void beforeClass() throws Exception {
		publicServicesUrl = apiFactory().getURL();
		sessionId = authenticate();
	}

	/**
	 * @return a valid sessionId
	 */
	protected static String authenticate() {
		return apiFactory().build().session().getSessionAuthorization().getSessionId();
	}
	
	/**
	 * Asserts that given URL returns a 200 response code.
	 * 
	 * @param relativeUrl URL relative to the URL of the tested tribefire services ( {@link ImpApiFactory#getURL()} )
	 */
	protected void assertServletAvailability(String relativeUrl) throws IOException, ClientProtocolException {
		CloseableHttpResponse response = httpGet(relativeUrl);
		
		assertThat(response).isNotNull();
		int statusCode = response.getStatusLine().getStatusCode();
		
		logger.info("Endpoint returns HTTP status code " + statusCode + " for url " + relativeUrl);
		
		assertThat(statusCode).isEqualTo(200);
	}

	/**
	 * @param relativeUrl URL relative to the URL of the tested tribefire services ( {@link ImpApiFactory#getURL()} )
	 */
	protected static CloseableHttpResponse httpGet(String relativeUrl) throws IOException, ClientProtocolException {
		CloseableHttpResponse response;
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
			response = httpClient.execute(new HttpGet(publicServicesUrl + relativeUrl));
		}
		return response;
	}

	/**
	 * Executes a GET request and returns the content of the response body.
	 * 
	 * @param relativeUrl URL relative to the URL of the tested tribefire services ( {@link ImpApiFactory#getURL()} )
	 * @return HTTP response body as UTF-8 string
	 */
	protected static String httpGetContent(String relativeUrl) throws Exception {
		try (InputStream in = httpGet(relativeUrl).getEntity().getContent()) {
			return IOTools.slurp(in, "UTF-8");
		}
	}
}