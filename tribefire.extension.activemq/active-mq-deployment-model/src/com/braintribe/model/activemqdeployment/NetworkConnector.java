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
package com.braintribe.model.activemqdeployment;

import com.braintribe.model.generic.StandardIdentifiable;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface NetworkConnector extends StandardIdentifiable {

	final EntityType<NetworkConnector> T = EntityTypes.T(NetworkConnector.class);

	void setName(String name);
	String getName();
	
	void setHost(String host);
	String getHost();
	
	void setPort(Integer port);
	@Initializer("61616")
	Integer getPort();
	
	void setUseExponentialBackOff(Boolean useExponentialBackOff);
	@Initializer("false")
	Boolean getUseExponentialBackOff();
	
	void setInitialReconnectDelay(Long initialReconnectDelay);
	@Initializer("5000l")
	Long getInitialReconnectDelay();

	void setMaxReconnectDelay(Long maxReconnectDelay);
	@Initializer("5000l")
	Long getMaxReconnectDelay();

	void setDuplex(Boolean duplex);
	@Initializer("true")
	Boolean getDuplex();
	
	void setConduitSubscriptions(Boolean conduitSubscriptions);
	@Initializer("true")
	Boolean getConduitSubscriptions();
	
}
