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
package com.braintribe.model.wopi.connector;

import static tribefire.extension.wopi.model.WopiMetaDataConstants.CONNECTION_REQUEST_TIMEOUT_IN_MS_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.CONNECTION_REQUEST_TIMEOUT_IN_MS_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.CONNECTION_RETRIES_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.CONNECTION_RETRIES_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.CONNECT_TIMEOUT_IN_MS_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.CONNECT_TIMEOUT_IN_MS_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.CUSTOM_PUBLIC_SERVICES_URL_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.DELAY_ON_RETRY_IN_MS_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.DELAY_ON_RETRY_IN_MS_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.SOCKET_TIMEOUT_IN_MS_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.SOCKET_TIMEOUT_IN_MS_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.CUSTOM_PUBLIC_SERVICES_URL_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.WAC_DISCOVERY_ENDPOINT_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.WAC_DISCOVERY_ENDPOINT_NAME;

import com.braintribe.model.deployment.connector.Connector;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.DeployableComponent;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.annotation.meta.Min;
import com.braintribe.model.generic.annotation.meta.MinLength;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * Connector to the "Office Online Server"
 * 
 *
 */
@DeployableComponent
public interface WopiWacConnector extends Connector {

	// There was also the idea to add a BasicAuthentication configuration. This does not work because the WOPI Server
	// removes silently the BasicAuthentication information. It was tested around end 2019; maybe something changed
	// afterwards

	final EntityType<WopiWacConnector> T = EntityTypes.T(WopiWacConnector.class);

	String wacDiscoveryEndpoint = "wacDiscoveryEndpoint";
	String customPublicServicesUrl = "customPublicServicesUrl";
	String connectionRequestTimeoutInMs = "connectionRequestTimeoutInMs";
	String connectTimeoutInMs = "connectTimeoutInMs";
	String socketTimeoutInMs = "socketTimeoutInMs";
	String connectionRetries = "connectionRetries";
	String delayOnRetryInMs = "delayOnRetryInMs";

	@Name(WAC_DISCOVERY_ENDPOINT_NAME)
	@Description(WAC_DISCOVERY_ENDPOINT_DESCRIPTION)
	@Mandatory
	@MinLength(5)
	String getWacDiscoveryEndpoint();
	void setWacDiscoveryEndpoint(String wacDiscoveryEndpoint);

	@Name(CUSTOM_PUBLIC_SERVICES_URL_NAME)
	@Description(CUSTOM_PUBLIC_SERVICES_URL_DESCRIPTION)
	String getCustomPublicServicesUrl();
	void setCustomPublicServicesUrl(String customPublicServicesUrl);

	@Name(CONNECTION_REQUEST_TIMEOUT_IN_MS_NAME)
	@Description(CONNECTION_REQUEST_TIMEOUT_IN_MS_DESCRIPTION)
	@Mandatory
	@Initializer("2000") // 2s
	@Min("1")
	int getConnectionRequestTimeoutInMs();
	void setConnectionRequestTimeoutInMs(int connectionRequestTimeoutInMs);

	@Name(CONNECT_TIMEOUT_IN_MS_NAME)
	@Description(CONNECT_TIMEOUT_IN_MS_DESCRIPTION)
	@Mandatory
	@Initializer("2000") // 2s
	@Min("1")
	int getConnectTimeoutInMs();
	void setConnectTimeoutInMs(int connectTimeoutInMs);

	@Name(SOCKET_TIMEOUT_IN_MS_NAME)
	@Description(SOCKET_TIMEOUT_IN_MS_DESCRIPTION)
	@Mandatory
	@Initializer("2000") // 2s
	@Min("1")
	int getSocketTimeoutInMs();
	void setSocketTimeoutInMs(int socketTimeoutInMs);

	@Name(CONNECTION_RETRIES_NAME)
	@Description(CONNECTION_RETRIES_DESCRIPTION)
	@Mandatory
	@Initializer("3")
	@Min("1")
	int getConnectionRetries();
	void setConnectionRetries(int connectionRetries);

	@Name(DELAY_ON_RETRY_IN_MS_NAME)
	@Description(DELAY_ON_RETRY_IN_MS_DESCRIPTION)
	@Mandatory
	@Initializer("1000") // 1s
	@Min("1")
	int getDelayOnRetryInMs();
	void setDelayOnRetryInMs(int delayOnRetryInMs);

}
