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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.codec.marshaller.json.JsonStreamMarshaller;
import com.braintribe.model.check.service.CheckRequest;
import com.braintribe.model.check.service.CheckResult;
import com.braintribe.model.check.service.CheckStatus;
import com.braintribe.model.extensiondeployment.check.CheckBundle;
import com.braintribe.model.generic.collection.PlainMap;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.transport.http.DefaultHttpClientProvider;
import com.braintribe.transport.http.HttpClientProvider;

import tribefire.extension.demo.model.deployment.DemoHealthCheckProcessor;
import tribefire.extension.demo.test.integration.utils.AbstractDemoTest;
import tribefire.extension.demo.test.integration.utils.DemoConstants;

@Category(KnownIssue.class)
public class HealthCheckProcessorTest extends AbstractDemoTest implements DemoConstants {

	private static HttpClientProvider clientProvider = new DefaultHttpClientProvider();
	private static CloseableHttpClient httpClient = null;

	private JsonStreamMarshaller jsonMarshaller;
	private String targetUrl;
	private CheckBundle healthCheck;
	private PersistenceGmSession cortexSession;

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
		jsonMarshaller = new JsonStreamMarshaller();
		targetUrl = apiFactory().getBaseURL() + HEALTH_CHECK_PATH;

		cortexSession = globalCortexSessionFactory.newSession("cortex");

		DemoHealthCheckProcessor healthCheckProcessor = cortexSession.findEntityByGlobalId(DEMO_HEALTH_CHECK_PROCESSOR_GLOBAL_ID);

		healthCheck = cortexSession.create(CheckBundle.T);
		healthCheck.setChecks(Collections.singletonList(healthCheckProcessor));
		healthCheck.setName("demo-health-check");

		cortexSession.commit();
	}

	@After
	public void cleanup() {
		cortexSession.deleteEntity(healthCheck);

		cortexSession.commit();
	}

	@Test
	public void testHealthCheck() throws Exception {
		HttpGet request = new HttpGet(targetUrl);
		HttpResponse response = httpClient.execute(request);

		// TODO CWI This needs to be updated as soon as custom health checks for modules are available
		assertThat(response).as("No HTTP response recieved").isNotNull();
		assertThat(response.getStatusLine().getStatusCode()).as("Wrong HTTP response code").isEqualTo(200);

		PlainMap<CheckRequest, CheckResult> result = getHealthCheckResult(response);

		assertThat(result.values()).isNotNull();
		assertThat(result).hasSize(1);
		Collection<CheckResult> values2 = result.values();
		ArrayList<CheckResult> values = new ArrayList<>(result.values());
//		assertThat(values.get(0).getEntries().size()).isEqualTo(2);

//		for (CheckResult checkResult : values) {
//			assertThat(checkResult.getEntries()).isNotNull();
//			assertThat(checkResult.getEntries()).hasSize(1);
//			assertThat(checkResult.getEntries().get(0).getCheckStatus()).isEqualTo(CheckStatus.ok);
//		}
	}

	@SuppressWarnings("unchecked")
	private PlainMap<CheckRequest, CheckResult> getHealthCheckResult(HttpResponse response) throws Exception {
		return (PlainMap<CheckRequest, CheckResult>) jsonMarshaller.unmarshall(response.getEntity().getContent());
	}
}
