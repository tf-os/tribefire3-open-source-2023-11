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
package com.braintribe.model.wopi.service;

import static tribefire.extension.wopi.model.WopiMetaDataConstants.ACCESS_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.ACCESS_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.WOPI_APP_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.WOPI_APP_NAME;

import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.extensiondeployment.WebTerminal;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.annotation.meta.Max;
import com.braintribe.model.generic.annotation.meta.Min;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * Integration example of WOPI
 * 
 *
 */
public interface WopiIntegrationExample extends WebTerminal {

	final EntityType<WopiIntegrationExample> T = EntityTypes.T(WopiIntegrationExample.class);

	String access = "access";
	String healthConnectTimeoutInMs = "healthConnectTimeoutInMs";
	String healthReadTimeoutInMs = "healthReadTimeoutInMs";
	String wopiApp = "wopiApp";

	@Name(ACCESS_NAME)
	@Description(ACCESS_DESCRIPTION)
	@Mandatory
	IncrementalAccess getAccess();
	void setAccess(IncrementalAccess access);

	@Mandatory
	@Initializer("5000")
	@Min("1l")
	@Max("60000l")
	int getHealthConnectTimeoutInMs();
	void setHealthConnectTimeoutInMs(int healthConnectTimeoutInMs);

	@Mandatory
	@Initializer("5000")
	@Min("1l")
	@Max("60000l")
	int getHealthReadTimeoutInMs();
	void setHealthReadTimeoutInMs(int healthReadTimeoutInMs);

	@Name(WOPI_APP_NAME)
	@Description(WOPI_APP_DESCRIPTION)
	@Mandatory
	WopiApp getWopiApp();
	void setWopiApp(WopiApp wopiApp);

}
