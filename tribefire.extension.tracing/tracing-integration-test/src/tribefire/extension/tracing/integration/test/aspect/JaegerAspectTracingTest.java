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
package tribefire.extension.tracing.integration.test.aspect;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.testing.category.VerySlow;

import tribefire.extension.tracing.model.deployment.connector.ConstantSampler;
import tribefire.extension.tracing.model.deployment.connector.HttpSenderConfiguration;
import tribefire.extension.tracing.model.deployment.connector.JaegerTracingConnector;
import tribefire.extension.tracing.model.deployment.connector.ReporterConfiguration;
import tribefire.extension.tracing.model.deployment.connector.TracingConnector;

/**
 * Tests with {@link JaegerTracingConnector}
 * 
 *
 */
@Category(VerySlow.class)
public class JaegerAspectTracingTest extends AbstractAspectTracingTest {

	// -----------------------------------------------------------------------
	// ABSTRACT
	// -----------------------------------------------------------------------

	@Override
	protected TracingConnector connector() {
		if (connector == null) {
			JaegerTracingConnector jaegerConnector = cortexSession.create(JaegerTracingConnector.T);

			ConstantSampler samplerConfiguration = cortexSession.create(ConstantSampler.T);

			HttpSenderConfiguration senderConfiguration = cortexSession.create(HttpSenderConfiguration.T);
			senderConfiguration.setEndpoint("http://localhost:14268/api/traces");

			ReporterConfiguration reporterConfiguration = cortexSession.create(ReporterConfiguration.T);
			reporterConfiguration.setFlushInterval(1);
			reporterConfiguration.setMaxQueueSize(1);
			reporterConfiguration.setSenderConfiguration(senderConfiguration);

			jaegerConnector.setSamplerConfiguration(samplerConfiguration);
			jaegerConnector.setReporterConfiguration(reporterConfiguration);

			connector = jaegerConnector;
			connector.setName(TEST_CONNECTOR_EXTERNAL_ID);
			connector.setExternalId(TEST_CONNECTOR_EXTERNAL_ID);
		}
		return connector;
	}

	// -----------------------------------------------------------------------
	// TESTS
	// -----------------------------------------------------------------------

	@Test
	public void testAspect_simple() {
		demoTracing();
	}

}
