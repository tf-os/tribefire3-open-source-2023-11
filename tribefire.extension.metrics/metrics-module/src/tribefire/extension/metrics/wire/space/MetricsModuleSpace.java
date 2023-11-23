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
package tribefire.extension.metrics.wire.space;

import com.braintribe.model.processing.deployment.api.binding.DenotationBindingBuilder;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.extension.metrics.model.deployment.connector.InMemoryMetricsConnector;
import tribefire.extension.metrics.model.deployment.connector.MetricsConnector;
import tribefire.extension.metrics.model.deployment.connector.NewRelicMetricsConnector;
import tribefire.extension.metrics.model.deployment.connector.PrometheusMetricsConnector;
import tribefire.extension.metrics.model.deployment.health.HealthCheckProcessor;
import tribefire.extension.metrics.model.deployment.service.MetricsDemoProcessor;
import tribefire.extension.metrics.model.deployment.service.MetricsProcessor;
import tribefire.extension.metrics.model.deployment.service.aspect.MetricsCounterAspect;
import tribefire.extension.metrics.model.deployment.service.aspect.MetricsInProgressAspect;
import tribefire.extension.metrics.model.deployment.service.aspect.MetricsSummaryAspect;
import tribefire.extension.metrics.model.deployment.service.aspect.MetricsTimerAspect;
import tribefire.module.wire.contract.TribefireModuleContract;
import tribefire.module.wire.contract.TribefireWebPlatformContract;

@Managed
public class MetricsModuleSpace implements TribefireModuleContract {

	@Import
	private TribefireWebPlatformContract tfPlatform;

	@Import
	private DeployablesSpace deployables;

	@Override
	public void bindDeployables(DenotationBindingBuilder bindings) {
		//@formatter:off
		//----------------------------
		// CONNECTOR
		//----------------------------
//		bindings.bind(PrometheusMetricsConnector.T)
//			.component(MetricsConnector.T, tribefire.extension.metrics.connector.api.MetricsConnector.class)
//			.expertFactory(deployables::prometheusMetricsConnector);
//		bindings.bind(PrometheusMetricsScrapingEndpoint.T)
//			.component(tfPlatform.binders().webTerminal())
//			.expertFactory(deployables::prometheusMetricsScrapingEndpoint);
		
		bindings.bind(PrometheusMetricsConnector.T)
			.component(tfPlatform.binders().webTerminal())
			.expertFactory(deployables::prometheusMetricsScrapingEndpoint)
			.component(MetricsConnector.T, tribefire.extension.metrics.connector.api.MetricsConnector.class)
			.expertFactory(deployables::prometheusMetricsConnector);
		
		
		bindings.bind(InMemoryMetricsConnector.T)
			.component(MetricsConnector.T, tribefire.extension.metrics.connector.api.MetricsConnector.class)
			.expertFactory(deployables::inMemoryMetricsConnector);		

		bindings.bind(NewRelicMetricsConnector.T)
			.component(MetricsConnector.T, tribefire.extension.metrics.connector.api.MetricsConnector.class)
			.expertFactory(deployables::newRelicMetricsConnector);		
		
		
		
		//----------------------------
		// PROCESSOR
		//----------------------------
		bindings.bind(MetricsProcessor.T)
			.component(tfPlatform.binders().serviceProcessor())
			.expertFactory(deployables::metricsProcessor);
		
		bindings.bind(MetricsDemoProcessor.T)
			.component(tfPlatform.binders().serviceProcessor())
			.expertFactory(deployables::metricsDemoProcessor);		
		//----------------------------
		// ASPECT
		//----------------------------
		bindings.bind(MetricsCounterAspect.T)
			.component(tfPlatform.binders().serviceAroundProcessor())
			.expertFactory(deployables::metricsCounterAspect);
		
		bindings.bind(MetricsTimerAspect.T)
			.component(tfPlatform.binders().serviceAroundProcessor())
			.expertFactory(deployables::metricsTimerAspect);

		bindings.bind(MetricsSummaryAspect.T)
			.component(tfPlatform.binders().serviceAroundProcessor())
			.expertFactory(deployables::metricsSummaryAspect);
		
		bindings.bind(MetricsInProgressAspect.T)
			.component(tfPlatform.binders().serviceAroundProcessor())
			.expertFactory(deployables::metricsInProgressAspect);
		
		
		//----------------------------
		// HEALTH
		//----------------------------
		bindings.bind(HealthCheckProcessor.T)
			.component(tfPlatform.binders().checkProcessor())
			.expertFactory(deployables::healthCheckProcessor);	
		
		//@formatter:on
	}
}
