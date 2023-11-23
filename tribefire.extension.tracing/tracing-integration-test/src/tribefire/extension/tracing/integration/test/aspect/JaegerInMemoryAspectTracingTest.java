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

import static com.braintribe.utils.lcd.CollectionTools2.asSet;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import com.braintribe.model.notification.Level;
import com.braintribe.utils.CommonTools;

import tribefire.extension.tracing.model.deployment.connector.JaegerInMemoryTracingConnector;
import tribefire.extension.tracing.model.deployment.connector.TracingConnector;
import tribefire.extension.tracing.model.deployment.service.CustomValueAttribute;
import tribefire.extension.tracing.model.deployment.service.DefaultAttribute;
import tribefire.extension.tracing.model.service.demo.DemoTracing;
import tribefire.extension.tracing.model.service.status.TracingStatusResult;
import tribefire.extension.tracing.model.service.status.local.InMemoryTracingStatusResult;

/**
 * Tests with {@link JaegerInMemoryTracingConnector}
 * 
 *
 */
public class JaegerInMemoryAspectTracingTest extends AbstractAspectTracingTest {

	// -----------------------------------------------------------------------
	// ABSTRACT
	// -----------------------------------------------------------------------

	@Override
	protected TracingConnector connector() {
		if (connector == null) {
			connector = cortexSession.create(JaegerInMemoryTracingConnector.T);
			connector.setName(TEST_CONNECTOR_EXTERNAL_ID);
			connector.setExternalId(TEST_CONNECTOR_EXTERNAL_ID);

			connector.setAddAttributesFromNotificationsMessage(Level.INFO);
			connector.setAddAttributesFromNotificationsDetailsMessage(Level.INFO);

			connector.setDefaultAttributes(Stream.of(DefaultAttribute.values()).collect(Collectors.toSet()));
		}
		return connector;
	}

	// -----------------------------------------------------------------------
	// TESTS - InitializeTracing
	// -----------------------------------------------------------------------

	// TODO: test health
	// TODO: test configureTracing
	// TODO: add test for user/entityType inclusions/exclusions

	@Test
	public void testAspect_initializeTracing() {
		InMemoryTracingStatusResult tracingStatus;
		initializeTracing();
		tracingStatus = tracingStatus();
		assertThat(tracingStatus.getSpans()).isEmpty();

		demoTracing();
		tracingStatus = tracingStatus();
		assertThat(tracingStatus.getSpans()).hasSize(1);

		initializeTracing();
		tracingStatus = tracingStatus();
		assertThat(tracingStatus.getSpans()).isEmpty();
	}

	@Test
	public void testAspect_initializeTracingMulticast() {
		TracingStatusResult tracingStatus;
		initializeTracingMulticast();
		tracingStatus = tracingStatusMulticast();
		assertThat(((InMemoryTracingStatusResult) tracingStatus.getResults().entrySet().iterator().next().getValue()).getSpans()).isEmpty();

		demoTracing();
		tracingStatus = tracingStatusMulticast();
		assertThat(((InMemoryTracingStatusResult) tracingStatus.getResults().entrySet().iterator().next().getValue()).getSpans()).hasSize(1);

		initializeTracing();
		tracingStatus = tracingStatusMulticast();
		assertThat(((InMemoryTracingStatusResult) tracingStatus.getResults().entrySet().iterator().next().getValue()).getSpans()).isEmpty();
	}

	// -----------------------------------------------------------------------
	// TESTS - EnableTracing / DisableTracing
	// -----------------------------------------------------------------------

	@Test
	public void testAspect_enableDisable() {
		InMemoryTracingStatusResult tracingStatus;

		disableTracing();
		demoTracing();

		tracingStatus = tracingStatus();

		assertThat(tracingStatus).isNotNull();
		assertThat(tracingStatus.getTracingEnabled()).isFalse();
		assertThat(tracingStatus.getSpans()).hasSize(0);

		enableTracing();
		demoTracing();

		tracingStatus = tracingStatus();

		assertThat(tracingStatus).isNotNull();
		assertThat(tracingStatus.getTracingEnabled()).isTrue();
		assertThat(tracingStatus.getSpans()).hasSize(1);
	}

	@Test
	public void testAspect_enableAutoDisable() {
		InMemoryTracingStatusResult tracingStatus;

		enableTracing(timeSpan(2000));

		demoTracing();

		tracingStatus = tracingStatus();

		assertThat(tracingStatus).isNotNull();
		assertThat(tracingStatus.getTracingEnabled()).isTrue();
		assertThat(tracingStatus.getSpans()).hasSize(1);

		CommonTools.sleep(5000);

		tracingStatus = tracingStatus();

		assertThat(tracingStatus).isNotNull();
		assertThat(tracingStatus.getTracingEnabled()).isFalse();
		assertThat(tracingStatus.getSpans()).hasSize(1);
	}

	@Test
	public void testAspect_enableDisableMulticast() {
		TracingStatusResult tracingStatus;

		disableTracingMulticast();
		demoTracing();

		tracingStatus = tracingStatusMulticast();

		assertThat(tracingStatus).isNotNull();
		assertThat(tracingStatus.getResults()).isNotNull();
		assertThat(tracingStatus.getResults().size()).isEqualTo(1);
		assertThat(tracingStatus.getResults().entrySet().iterator().next().getValue().getTracingEnabled()).isFalse();
		assertThat(((InMemoryTracingStatusResult) tracingStatus.getResults().entrySet().iterator().next().getValue()).getSpans()).hasSize(0);

		enableTracingMulticast();
		demoTracing();

		tracingStatus = tracingStatusMulticast();

		assertThat(tracingStatus).isNotNull();
		assertThat(tracingStatus.getResults()).isNotNull();
		assertThat(tracingStatus.getResults().size()).isEqualTo(1);
		assertThat(tracingStatus.getResults().entrySet().iterator().next().getValue().getTracingEnabled()).isTrue();
		assertThat(((InMemoryTracingStatusResult) tracingStatus.getResults().entrySet().iterator().next().getValue()).getSpans()).hasSize(1);
	}

	@Test
	public void testAspect_enableAutoDisableMulticast() {
		TracingStatusResult tracingStatus;

		enableTracingMulticast(timeSpan(2000));

		demoTracing();

		tracingStatus = tracingStatusMulticast();

		assertThat(tracingStatus).isNotNull();
		assertThat(tracingStatus.getResults().entrySet().iterator().next().getValue().getTracingEnabled()).isTrue();
		assertThat(((InMemoryTracingStatusResult) tracingStatus.getResults().entrySet().iterator().next().getValue()).getSpans()).hasSize(1);

		CommonTools.sleep(5000);

		tracingStatus = tracingStatusMulticast();

		assertThat(tracingStatus).isNotNull();
		assertThat(tracingStatus.getResults().entrySet().iterator().next().getValue().getTracingEnabled()).isFalse();
		assertThat(((InMemoryTracingStatusResult) tracingStatus.getResults().entrySet().iterator().next().getValue()).getSpans()).hasSize(1);
	}

	// -----------------------------------------------------------------------
	// TESTS - general tracing
	// -----------------------------------------------------------------------

	@Test
	public void testAspect_singleSpan() {
		demoTracing();

		InMemoryTracingStatusResult tracingStatus = tracingStatus();

		assertThat(tracingStatus).isNotNull();
		assertThat(tracingStatus.getConnectorConfiguration()).isNotNull();
		assertThat(tracingStatus.getTracingEnabled()).isTrue();
		assertThat(tracingStatus.getSpans()).hasSize(1);
		assertThat(tracingStatus.getSpans().get(0).getLogs()).isEmpty();
		assertThat(tracingStatus.getSpans().get(0).getDuration()).isGreaterThan(0);
		assertThat(tracingStatus.getSpans().get(0).getStart()).isGreaterThan(0);
		assertThat(tracingStatus.getSpans().get(0).getOperationName()).isEqualTo(DemoTracing.T.getTypeName());
		assertThat(tracingStatus.getSpans().get(0).getServiceName())
				.isEqualTo(TracingConnector.T.getProperty(TracingConnector.serviceName).getInitializer());

		Map<String, Object> attributes = tracingStatus.getSpans().get(0).getAttributes();

		attributes.forEach((k, v) -> {
			// ignore partition
			if (k.equals(DefaultAttribute.ATTRIBUTE_PARTITION.toString())) {
				return;
			}
			assertThat(v).describedAs("Key: '" + k + "' must have a non null value: '" + v + "'").isNotNull();
		});

		// ignore error cases, ignore messages on error
		//@formatter:off
		Stream.of(DefaultAttribute.values()).filter(t -> !(
				t == DefaultAttribute.ATTRIBUTE_STACK || 
				t == DefaultAttribute.ATTRIBUTE_ERROR || 
				t == DefaultAttribute.ATTRIBUTE_PARTITION || 
				t == DefaultAttribute.ATTRIBUTE_NOTIFICATION_MESSAGE || 
				t == DefaultAttribute.ATTRIBUTE_NOTIFICATION_DETAIL_MESSAGE)).forEach(t -> {
					assertThat(attributes).containsKey(t.toString());
					attributes.remove(t.toString());
				});
		//@formatter:on
		assertThat(attributes).hasSize(1);
		assertThat(attributes).containsKeys(CustomValueAttribute.ATTRIBUTE_COMPONENT_NAME.toString());
	}

	@Test
	public void testAspect_singleSpanError() {
		demoTracingError();

		InMemoryTracingStatusResult tractingStatus = tracingStatus();

		assertThat(tractingStatus).isNotNull();
		assertThat(tractingStatus.getConnectorConfiguration()).isNotNull();
		assertThat(tractingStatus.getTracingEnabled()).isTrue();
		assertThat(tractingStatus.getSpans()).hasSize(1);
		assertThat(tractingStatus.getSpans().get(0).getLogs()).isEmpty();
		assertThat(tractingStatus.getSpans().get(0).getDuration()).isGreaterThan(0);
		assertThat(tractingStatus.getSpans().get(0).getStart()).isGreaterThan(0);
		assertThat(tractingStatus.getSpans().get(0).getOperationName()).isEqualTo(DemoTracing.T.getTypeName());
		assertThat(tractingStatus.getSpans().get(0).getServiceName())
				.isEqualTo(TracingConnector.T.getProperty(TracingConnector.serviceName).getInitializer());

		Map<String, Object> attributes = tractingStatus.getSpans().get(0).getAttributes();

		attributes.forEach((k, v) -> {
			// ignore partition
			if (k.equals(DefaultAttribute.ATTRIBUTE_PARTITION.toString())) {
				return;
			}
			assertThat(v).describedAs("Key: '" + k + "' must have a non null value: '" + v + "'").isNotNull();
		});

		// ignore error cases
		Stream.of(DefaultAttribute.values()).forEach(t -> {
			attributes.remove(t.value());
		});
		assertThat(attributes).hasSize(1);
		assertThat(attributes).containsKeys(CustomValueAttribute.ATTRIBUTE_COMPONENT_NAME.toString());
	}

	@Test
	public void testAspect_multipleSpan() {
		demoTracing();
		demoTracing();

		InMemoryTracingStatusResult tracingStatus = tracingStatus();

		assertThat(tracingStatus).isNotNull();
		assertThat(tracingStatus.getTracingEnabled()).isTrue();
		assertThat(tracingStatus.getSpans()).hasSize(2);
	}

	@Test
	public void testAspect_customValueAttributes() {
		String tenantName = UUID.randomUUID().toString();
		String componentName = UUID.randomUUID().toString();
		JaegerInMemoryTracingConnector tracingConnector = JaegerInMemoryTracingConnector.T.create();
		tracingConnector.setTenant(tenantName);
		tracingConnector.setComponentName(componentName);

		adaptDeployable(tracingConnector, TEST_CONNECTOR_EXTERNAL_ID, asSet(TracingConnector.tenant, TracingConnector.componentName));

		demoTracing();

		InMemoryTracingStatusResult tracingStatus = tracingStatus();

		assertThat(tracingStatus).isNotNull();
		assertThat(tracingStatus.getSpans().get(0).getAttributes()).containsKey(CustomValueAttribute.ATTRIBUTE_TENANT.toString());
		assertThat(tracingStatus.getSpans().get(0).getAttributes()).containsValue(tenantName);
		assertThat(tracingStatus.getSpans().get(0).getAttributes()).containsKey(CustomValueAttribute.ATTRIBUTE_COMPONENT_NAME.toString());
		assertThat(tracingStatus.getSpans().get(0).getAttributes()).containsValue(componentName);
	}

	@Test
	public void testAspect_serviceName() {
		String serviceName = UUID.randomUUID().toString();

		JaegerInMemoryTracingConnector tracingConnector = JaegerInMemoryTracingConnector.T.create();
		tracingConnector.setServiceName(serviceName);

		adaptDeployable(tracingConnector, TEST_CONNECTOR_EXTERNAL_ID, asSet(TracingConnector.serviceName));

		demoTracing();

		InMemoryTracingStatusResult tracingStatus = tracingStatus();

		assertThat(tracingStatus).isNotNull();
		assertThat(tracingStatus.getSpans().get(0).getServiceName()).isEqualTo(serviceName);
	}

	@Test
	public void testAspect_customValueAttributes_default() {
		demoTracing();

		InMemoryTracingStatusResult tracingStatus = tracingStatus();

		assertThat(tracingStatus).isNotNull();
		assertThat(tracingStatus.getSpans().get(0).getAttributes()).doesNotContainKey(CustomValueAttribute.ATTRIBUTE_TENANT.toString());
		assertThat(tracingStatus.getSpans().get(0).getAttributes()).containsKey(CustomValueAttribute.ATTRIBUTE_COMPONENT_NAME.toString());
	}

}
