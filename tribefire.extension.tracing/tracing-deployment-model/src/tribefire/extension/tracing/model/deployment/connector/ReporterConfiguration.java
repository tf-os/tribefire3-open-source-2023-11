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
package tribefire.extension.tracing.model.deployment.connector;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.annotation.meta.Min;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface ReporterConfiguration extends GenericEntity {

	final EntityType<ReporterConfiguration> T = EntityTypes.T(ReporterConfiguration.class);

	String senderConfiguration = "senderConfiguration";
	String flushInterval = "flushInterval";
	String maxQueueSize = "maxQueueSize";
	String logSpans = "logSpans";

	@Mandatory
	SenderConfiguration getSenderConfiguration();
	void setSenderConfiguration(SenderConfiguration senderConfiguration);

	@Mandatory
	@Min("1")
	@Initializer("1000")
	int getFlushInterval();
	void setFlushInterval(int flushInterval);

	@Mandatory
	@Min("1")
	@Initializer("10000")
	int getMaxQueueSize();
	void setMaxQueueSize(int maxQueueSize);
}
