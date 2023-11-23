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
package tribefire.extension.metrics.initializer.wire.space;

import java.util.HashSet;
import java.util.Set;

import com.braintribe.model.extensiondeployment.check.CheckBundle;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.initializer.support.integrity.wire.contract.CoreInstancesContract;
import tribefire.cortex.initializer.support.wire.space.AbstractInitializerSpace;
import tribefire.cortex.model.check.CheckCoverage;
import tribefire.cortex.model.check.CheckWeight;
import tribefire.extension.metrics.initializer.wire.contract.DemoMetricsConnector;
import tribefire.extension.metrics.initializer.wire.contract.ExistingInstancesContract;
import tribefire.extension.metrics.initializer.wire.contract.MetricsInitializerContract;
import tribefire.extension.metrics.model.deployment.health.HealthCheckProcessor;
import tribefire.extension.metrics.templates.api.MetricsTemplateContext;
import tribefire.extension.metrics.templates.api.connector.MetricsTemplateConnectorContext;
import tribefire.extension.metrics.templates.api.connector.MetricsTemplateInMemoryConnectorContext;
import tribefire.extension.metrics.templates.api.connector.MetricsTemplateNewRelicConnectorContext;
import tribefire.extension.metrics.templates.api.connector.MetricsTemplatePrometheusConnectorContext;
import tribefire.extension.metrics.templates.wire.contract.MetricsTemplatesContract;

@Managed
public class MetricsInitializerSpace extends AbstractInitializerSpace implements MetricsInitializerContract {

	@Import
	private ExistingInstancesContract existingInstances;

	@Import
	private CoreInstancesContract coreInstances;

	@Import
	private MetricsTemplatesContract metricsTemplates;

	@Override
	public void setupDefaultConfiguration(DemoMetricsConnector demoMetricsConnect) {
		MetricsTemplateContext metricsTemplateContext = demoMetricsTemplateContext(demoMetricsConnect);
		metricsTemplates.setupMetrics(metricsTemplateContext);
	}

	@Managed
	public MetricsTemplateContext demoMetricsTemplateContext(DemoMetricsConnector demoMetricsConnector) {

		Set<MetricsTemplateConnectorContext> connectorContexts = new HashSet<>();

		MetricsTemplateConnectorContext connectorContext;
		if (demoMetricsConnector != null) {
			switch (demoMetricsConnector) {
				case PROMETHEUS:
					connectorContext = demoPrometheusMetricsTemplateConnectorContext();
					break;
				case IN_MEMORY:
					connectorContext = demoInMemoryMetricsTemplateConnectorContext();
					break;
				case NEW_RELIC:
					connectorContext = demoNewRelicMetricsTemplateConnectorContext();
					break;
				default:
					throw new IllegalArgumentException(DemoMetricsConnector.class.getSimpleName() + ": '" + demoMetricsConnector + "' not supported");
			}
		} else {
			connectorContext = defaultMetricsTemplateConnectorContext();
		}

		connectorContexts.add(connectorContext);

		boolean addDemo = true;

		//@formatter:off
		MetricsTemplateContext bean = MetricsTemplateContext.builder()
				.setAddDemo(addDemo)
				.setContext("default")
				.setEntityFactory(super::create)
				.setMetricsModule(existingInstances.module())
				.setConnectorContexts(connectorContexts)
				.setLookupFunction(super::lookup)
				.setLookupExternalIdFunction(super::lookupExternalId)
			.build();
		//@formatter:on

		return bean;
	}

	// -----------------------------------------------------------------------
	// DEMO
	// -----------------------------------------------------------------------

	private MetricsTemplateConnectorContext demoInMemoryMetricsTemplateConnectorContext() {
		//@formatter:off
		MetricsTemplateConnectorContext context = MetricsTemplateInMemoryConnectorContext.builder()
				.build();
		//@formatter:on
		return context;
	}

	private MetricsTemplateConnectorContext demoPrometheusMetricsTemplateConnectorContext() {
		//@formatter:off
		MetricsTemplateConnectorContext context = MetricsTemplatePrometheusConnectorContext.builder()
				.build();
		//@formatter:on
		return context;
	}

	private MetricsTemplateConnectorContext demoNewRelicMetricsTemplateConnectorContext() {
		//@formatter:off
		MetricsTemplateConnectorContext context = MetricsTemplateNewRelicConnectorContext.builder()
				.build();
		//@formatter:on
		return context;
	}

	private MetricsTemplateConnectorContext defaultMetricsTemplateConnectorContext() {
		return demoInMemoryMetricsTemplateConnectorContext();
	}

	// -----------------------------------------------------------------------
	// HEALTH
	// -----------------------------------------------------------------------

	@Override
	@Managed
	public CheckBundle functionalCheckBundle() {
		CheckBundle bean = create(CheckBundle.T);
		bean.setModule(existingInstances.module());
		bean.getChecks().add(healthCheckProcessor());
		bean.setName("METRICS Checks");
		bean.setWeight(CheckWeight.under1s);
		bean.setCoverage(CheckCoverage.connectivity);
		bean.setIsPlatformRelevant(false);

		return bean;
	}

	@Managed
	@Override
	public HealthCheckProcessor healthCheckProcessor() {
		HealthCheckProcessor bean = create(HealthCheckProcessor.T);
		bean.setModule(existingInstances.module());
		bean.setAutoDeploy(true);
		bean.setName("METRICS Health Check");
		bean.setExternalId("metrics.healthzProcessor");
		return bean;
	}

}
