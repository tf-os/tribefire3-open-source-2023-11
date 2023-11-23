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
package tribefire.extension.demo.test.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.transport.http.DefaultHttpClientProvider;
import com.braintribe.transport.http.HttpClientProvider;

import tribefire.extension.demo.model.data.Company;
import tribefire.extension.demo.model.data.Department;
import tribefire.extension.demo.model.data.Person;
import tribefire.extension.demo.test.integration.utils.AbstractDemoTest;

/**
 * Tests basic functionality of the DemoApp
 *
 * @author Neidhart
 *
 */
public class AppTest extends AbstractDemoTest {

	private String appURL;

	private static HttpClientProvider clientProvider = new DefaultHttpClientProvider();
	private static CloseableHttpClient httpClient = null;

	@BeforeClass
	public static void beforeClass() {
		try {
			httpClient = clientProvider.provideHttpClient();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@AfterClass
	public static void afterClass() {
		if (httpClient != null) {
			try {
				httpClient.close();
			} catch (IOException e) {
				e.printStackTrace(System.err);
			}
		}
	}

	@Before
	public void initLocal() {
		appURL = apiFactory().getURL() + "/component/app.demo";
	}

	/**
	 * returns the full content of the HTTP response after calling respective URL
	 */
	private String getHTMLFromURL(String URL) throws ClientProtocolException, IOException {
		HttpGet request = new HttpGet(URL);

		HttpResponse response = httpClient.execute(request);

		assertThat(response).as("No HTTP Response recieved").isNotNull();

		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}

		assertThat(response.getStatusLine().getStatusCode())
				.as("Wrong HTTP response code for URL: %s\nHTTP response body:\n%s", URL, result.toString()).isEqualTo(200);

		return result.toString();
	}

	/**
	 * just tests if the searched text appears again somewhere in the HTTP response (HTML) as xpath is not working as
	 * HTML is currently not in valid XML
	 */
	private void queryEntityInDemoApp(EntityType<?> type, String text, boolean shouldFindSomething) throws ClientProtocolException, IOException {

		String typeFullName = type.getTypeName();
		String requestURL = appURL + "?type=" + typeFullName + "&text=" + text + "&parameter=findByText";

		logger.info("send request to app looking for '" + typeFullName + "' with '" + text + "'.");

		String document = getHTMLFromURL(requestURL);

		logger.info("Checking HTTP response content...");

		if (shouldFindSomething) {
			assertThat(document).as("Entity not found via the demo app").contains(text);
			logger.info("found at least one matching entity, as expected");
		} else {
			assertThat(document).as("Entity not found via the demo app").doesNotContain(text);
			logger.info("found NO matching entity, as expected");
		}

	}

	/**
	 * looks up a few entities via the DemoApp and makes sure that some standard entities are found and - at the same
	 * time - no results are found for very unusual queries
	 */
	@Test
	public void testApp() throws ClientProtocolException, IOException {
		logger.info("Testing DemoApp...");

		queryEntityInDemoApp(Company.T, "Braintribe", true);
		queryEntityInDemoApp(Person.T, "Doe", true);
		queryEntityInDemoApp(Department.T, "Marketing", true);

		queryEntityInDemoApp(Person.T, "Marketing", false);
		queryEntityInDemoApp(Person.T, "ThisStringShouldNeverMatch", false);

	}

}
