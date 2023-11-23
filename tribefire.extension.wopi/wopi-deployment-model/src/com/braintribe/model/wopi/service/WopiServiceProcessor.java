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

import static tribefire.extension.wopi.model.WopiMetaDataConstants.WOPI_APP_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.WOPI_APP_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.WOPI_SERVICE_PROCESSOR_LOG_ERROR_THRESHOLD_IN_MS_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.WOPI_SERVICE_PROCESSOR_LOG_ERROR_THRESHOLD_IN_MS_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.WOPI_SERVICE_PROCESSOR_LOG_WARNING_THRESHOLD_IN_MS_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.WOPI_SERVICE_PROCESSOR_LOG_WARNING_THRESHOLD_IN_MS_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.WOPI_SERVICE_PROCESSOR_TEST_DOC_COMMAND_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.WOPI_SERVICE_PROCESSOR_TEST_DOC_COMMAND_NAME;

import com.braintribe.model.extensiondeployment.access.AccessRequestProcessor;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.annotation.meta.Max;
import com.braintribe.model.generic.annotation.meta.Min;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 *
 */
public interface WopiServiceProcessor extends AccessRequestProcessor {

	final EntityType<WopiServiceProcessor> T = EntityTypes.T(WopiServiceProcessor.class);

	String wopiApp = "wopiApp";
	String logWarningThresholdInMs = "logWarningThresholdInMs";
	String logErrorThresholdInMs = "logErrorThresholdInMs";
	String testDocCommand = "testDocCommand";

	@Name(WOPI_APP_NAME)
	@Description(WOPI_APP_DESCRIPTION)
	@Mandatory
	WopiApp getWopiApp();
	void setWopiApp(WopiApp wopiApp);

	@Name(WOPI_SERVICE_PROCESSOR_LOG_WARNING_THRESHOLD_IN_MS_NAME)
	@Description(WOPI_SERVICE_PROCESSOR_LOG_WARNING_THRESHOLD_IN_MS_DESCRIPTION)
	@Mandatory
	@Initializer("5000l") // 5s
	@Min("1l") // 1ms
	@Max("600000l") // 10min
	long getLogWarningThresholdInMs();
	void setLogWarningThresholdInMs(long logWarningThresholdInMs);

	@Name(WOPI_SERVICE_PROCESSOR_LOG_ERROR_THRESHOLD_IN_MS_NAME)
	@Description(WOPI_SERVICE_PROCESSOR_LOG_ERROR_THRESHOLD_IN_MS_DESCRIPTION)
	@Mandatory
	@Initializer("10000l") // 10s
	@Min("1l") // 1ms
	@Max("600000l") // 10min
	long getLogErrorThresholdInMs();
	void setLogErrorThresholdInMs(long logErrorThresholdInMs);

	@Name(WOPI_SERVICE_PROCESSOR_TEST_DOC_COMMAND_NAME)
	@Description(WOPI_SERVICE_PROCESSOR_TEST_DOC_COMMAND_DESCRIPTION)
	@Mandatory
	@Initializer("'/usr/local/bin/docker run --rm tylerbutler/wopi-validator -- -w %s -t %s -l %d %s'")
	String getTestDocCommand();
	void setTestDocCommand(String testDocCommand);

}
