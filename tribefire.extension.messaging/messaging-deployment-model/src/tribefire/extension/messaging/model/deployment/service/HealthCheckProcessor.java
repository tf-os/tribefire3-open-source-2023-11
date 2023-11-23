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
package tribefire.extension.messaging.model.deployment.service;

import com.braintribe.model.extensiondeployment.check.CheckProcessor;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface HealthCheckProcessor extends CheckProcessor {

	EntityType<HealthCheckProcessor> T = EntityTypes.T(HealthCheckProcessor.class);

	String messagingConnector = "messagingConnector";

	/*@Mandatory TODO Would probably not be further used @dmiex check when testing HealthCheck task: https://document-one.atlassian.net/browse/D1-3311
	@Name("Messaging Connector")
	@Description("Messaging Connector as low level connection to the tracer")
    MessagingConnectorDeployable getMessagingConnector();
	void setMessagingConnector(MessagingConnectorDeployable messagingConnectorDeployable);*/

}
