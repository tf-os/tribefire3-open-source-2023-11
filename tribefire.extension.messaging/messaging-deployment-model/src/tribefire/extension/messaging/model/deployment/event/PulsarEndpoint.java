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
package tribefire.extension.messaging.model.deployment.event;

import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.annotation.meta.Priority;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

import tribefire.extension.messaging.model.MessagingConnectorType;

@SelectiveInformation("Pulsar Endpoint: ${name} - ${connectionUrl}")
public interface PulsarEndpoint extends EventEndpoint {

	EntityType<PulsarEndpoint> T = EntityTypes.T(PulsarEndpoint.class);

	// ***************************************************************************************************
	// STATIC CONFIGURATION
	// ***************************************************************************************************
	String adminUrl = "adminUrl";

	@Override
	@Mandatory
	@Initializer("'pulsar://localhost:6650'")
	@Priority(3.0d)
	String getConnectionUrl();

	@Mandatory
	@Name("Admin URL")
	@Description("URL for Admin connection for HealthCheck support")
	@Initializer("'http://localhost:8081'")
	@Priority(4.0d)
	String getAdminUrl();
	void setAdminUrl(String adminUrl);

	// -----------------------
	// Advanced Settings
	// -----------------------

	@Override
	default MessagingConnectorType getConnectorType() {
		return MessagingConnectorType.PULSAR;
	}
}
