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
package tribefire.extension.metrics.model.deployment.service;

import java.util.Set;

import com.braintribe.model.extensiondeployment.ServiceProcessor;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

import tribefire.extension.metrics.model.deployment.service.aspect.MetricsAspect;

/**
 * Metrics Processor
 * 
 *
 */
// TODO: maybe add description for meters?
public interface MetricsProcessor extends ServiceProcessor {

	final EntityType<MetricsProcessor> T = EntityTypes.T(MetricsProcessor.class);

	String metricsBinderConfig = "metricsBinderConfig";
	String metricsAspects = "metricsAspects";

	MetricsBinderConfig getMetricsBinderConfig();
	void setMetricsBinderConfig(MetricsBinderConfig metricsBinderConfig);

	Set<MetricsAspect> getMetricsAspects();
	void setMetricsAspects(Set<MetricsAspect> metricsAspects);

}
