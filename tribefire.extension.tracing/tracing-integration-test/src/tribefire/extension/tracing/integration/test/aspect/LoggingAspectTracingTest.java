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

import com.braintribe.model.logging.LogLevel;
import com.braintribe.testing.category.VerySlow;

import tribefire.extension.tracing.model.deployment.connector.LoggingTracingConnector;
import tribefire.extension.tracing.model.deployment.connector.TracingConnector;

/**
 * Tests with {@link LoggingTracingConnector}
 * 
 *
 */
@Category(VerySlow.class)
public class LoggingAspectTracingTest extends AbstractAspectTracingTest {

	// -----------------------------------------------------------------------
	// ABSTRACT
	// -----------------------------------------------------------------------

	@Override
	protected TracingConnector connector() {
		if (connector == null) {
			LoggingTracingConnector loggingConnector = cortexSession.create(LoggingTracingConnector.T);
			loggingConnector.setLogLevel(LogLevel.INFO);
			loggingConnector.setLogAttributes(true);

			connector = loggingConnector;
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
