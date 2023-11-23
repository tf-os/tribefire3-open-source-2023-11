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
package tribefire.extension.opentracing.model.deployment.service;

import com.braintribe.model.extensiondeployment.access.AccessRequestProcessor;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.annotation.meta.Max;
import com.braintribe.model.generic.annotation.meta.Min;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 *
 */
public interface OpentracingServiceProcessor extends AccessRequestProcessor {

	final EntityType<OpentracingServiceProcessor> T = EntityTypes.T(OpentracingServiceProcessor.class);

	String logWarningThresholdInMs = "logWarningThresholdInMs";
	String logErrorThresholdInMs = "logErrorThresholdInMs";

	@Mandatory
	@Initializer("5000l") // 5s
	@Min("1l") // 1ms
	@Max("600000l") // 10min
	long getLogWarningThresholdInMs();
	void setLogWarningThresholdInMs(long logWarningThresholdInMs);

	@Mandatory
	@Initializer("10000l") // 10s
	@Min("1l") // 1ms
	@Max("600000l") // 10min
	long getLogErrorThresholdInMs();
	void setLogErrorThresholdInMs(long logErrorThresholdInMs);

}
