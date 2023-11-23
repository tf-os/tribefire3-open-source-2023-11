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
package tribefire.extension.tracing.model.service.status.local;

import java.util.Date;

import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

import tribefire.extension.tracing.model.deployment.connector.TracingConnector;
import tribefire.extension.tracing.model.service.TracingResult;

public interface TracingStatusLocalResult extends TracingResult {

	EntityType<TracingStatusLocalResult> T = EntityTypes.T(TracingStatusLocalResult.class);

	String tracingEnabled = "tracingEnabled";
	String name = "name";
	String disableTracingAt = "disableTracingAt";
	String connectorConfiguration = "connectorConfiguration";

	boolean getTracingEnabled();
	void setTracingEnabled(boolean tracingEnabled);

	@Mandatory
	String getName();
	void setName(String name);

	Date getDisableTracingAt();
	void setDisableTracingAt(Date disableTracingAt);

	@Mandatory
	@Name("Connector Configuration")
	@Description("Tracing Connector configuration")
	TracingConnector getConnectorConfiguration();
	void setConnectorConfiguration(TracingConnector connectorConfiguration);
}
