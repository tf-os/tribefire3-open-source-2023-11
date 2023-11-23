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
package tribefire.extension.dmb.messaging.model.deployment;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.messagingdeployment.Messaging;

public interface DmbMessaging extends Messaging {

	final EntityType<DmbMessaging> T = EntityTypes.T(DmbMessaging.class);

	String getBrokerHost();
	void setBrokerHost(String brokerHost);

	int getConnectorPort();
	void setConnectorPort(int connectorPort);

	String getJmxServiceUrl();
	void setJmxServiceUrl(String jmxServiceUrl);

	String getUsername();
	void setUsername(String username);

	String getPassword();
	void setPassword(String password);

}
