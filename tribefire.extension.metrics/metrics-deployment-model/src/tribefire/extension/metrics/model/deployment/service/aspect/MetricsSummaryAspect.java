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
package tribefire.extension.metrics.model.deployment.service.aspect;

import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * Distribution Summary Aspect
 * 
 *
 */
public interface MetricsSummaryAspect extends MetricsAspect {

	final EntityType<MetricsSummaryAspect> T = EntityTypes.T(MetricsSummaryAspect.class);

	String baseUnit = "baseUnit";
	String scale = "scale";
	String summaryStatistics = "summaryStatistics";

	String getBaseUnit();
	void setBaseUnit(String baseUnit);

	Double getScale();
	void setScale(Double scale);

	@Mandatory
	@Initializer("enum(tribefire.extension.metrics.model.deployment.service.aspect.SummaryStatistics,REQUEST_SIZE)")
	SummaryStatistics getSummaryStatistics();
	void setSummaryStatistics(SummaryStatistics summaryStatistics);

}
