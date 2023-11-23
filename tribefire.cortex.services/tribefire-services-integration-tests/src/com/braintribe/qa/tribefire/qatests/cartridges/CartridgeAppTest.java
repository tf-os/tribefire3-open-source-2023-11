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
package com.braintribe.qa.tribefire.qatests.cartridges;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.braintribe.model.deployment.DeploymentStatus;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.product.rat.imp.ImpApi;
import com.braintribe.qa.cartridge.main.model.deployment.terminal.TestBasicTemplateBasedApp;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.testing.internal.tribefire.tests.AbstractTribefireQaTest;
import com.braintribe.utils.DOMTools;

/**
 * This class creates a web terminal based on a <code>Velocity</code> context (i.e.
 * <code>BasicTemplateBasedServlet</code>) and tests the server-client HTTP communication.
 *
 */
@Category(KnownIssue.class) // TODO: Resolve issue
public class CartridgeAppTest extends AbstractTribefireQaTest {

	@Before
	@After
	public void cleanup() {
		eraseTestEntities();
	}

	@Test
	public void testCartridgeApp() throws GmSessionException, IOException {
		logger.info("Starting DevQA-test: creating a web terminal (app) in a cartridge...");

		ImpApi imp = apiFactory().build();

		String timestamp = "" + System.currentTimeMillis();
		String currentAppName = name(timestamp + "App");

		// @formatter:off
		TestBasicTemplateBasedApp templateBasedApp = imp.deployable()
				.webTerminal()
				.create(TestBasicTemplateBasedApp.T, currentAppName, currentAppName, currentAppName)
				.get();
		// @formatter:on

		templateBasedApp.setTimestamp(timestamp);

		imp.commit();
		imp.service().deployRequest(templateBasedApp).call();

		assertThat(templateBasedApp.getDeploymentStatus()).isEqualTo(DeploymentStatus.deployed);
		logger.info("Creating an HTTP request to the app...");
		String appURL = imp.getUrl() + "/component/" + currentAppName;

		HttpClient client = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(appURL);

		HttpResponse response = client.execute(request);

		assertThat(response).isNotNull();
		assertThat(response.getStatusLine().getStatusCode()).as("Wrong HTTP response code for URL: %s", appURL).isEqualTo(200);

		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}

		logger.info("Checking HTTP response content...");
		Document document = DOMTools.stringToDocument(result.toString());
		Element timestampElement = DOMTools.getElementByXPath(document.getDocumentElement(), "//div[@id='timestamp']");
		assertThat(timestampElement.getTextContent().trim()).isEqualTo(timestamp);

		logger.info("All assertions have completed succefully!");
		logger.info("Completed DevQA-test: creating a web terminal (app) in a cartridge.");
	}

}
