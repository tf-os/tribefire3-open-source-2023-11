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
package tribefire.extension.metrics.templates.wire.contract;

import java.util.Set;

import com.braintribe.wire.api.space.WireSpace;

import tribefire.extension.metrics.model.deployment.connector.MetricsConnector;
import tribefire.extension.metrics.model.deployment.service.MetricsDemoProcessor;
import tribefire.extension.metrics.model.deployment.service.MetricsProcessor;
import tribefire.extension.metrics.model.deployment.service.aspect.MetricsCounterAspect;
import tribefire.extension.metrics.model.deployment.service.aspect.MetricsInProgressAspect;
import tribefire.extension.metrics.model.deployment.service.aspect.MetricsSummaryAspect;
import tribefire.extension.metrics.model.deployment.service.aspect.MetricsTimerAspect;
import tribefire.extension.metrics.templates.api.MetricsTemplateContext;

public interface MetricsTemplatesContract extends WireSpace {

	/**
	 * Setup METRICS with a specified {@link MetricsTemplateContext}
	 */
	void setupMetrics(MetricsTemplateContext context);

	MetricsProcessor metricsServiceProcessor(MetricsTemplateContext context);

	MetricsDemoProcessor metricsDemoProcessor(MetricsTemplateContext context);

	Set<MetricsConnector> metricsConnectors(MetricsTemplateContext context);

	MetricsCounterAspect metricsCounterAspect(MetricsTemplateContext context);

	MetricsTimerAspect metricsTimerAspect(MetricsTemplateContext context);

	MetricsSummaryAspect metricsSummaryAspect(MetricsTemplateContext context);

	MetricsInProgressAspect metricsInProgressAspect(MetricsTemplateContext context);
}
