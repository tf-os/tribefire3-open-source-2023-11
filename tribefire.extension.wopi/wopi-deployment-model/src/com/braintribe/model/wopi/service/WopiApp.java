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
import static tribefire.extension.wopi.model.WopiMetaDataConstants.APPLY_EARLY_FILE_SIZE_CHECKS_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.APPLY_EARLY_FILE_SIZE_CHECKS_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.CONTEXT_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.CONTEXT_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.DISABLE_PRINT_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.DISABLE_TRANSLATION_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.LOCK_EXPIRATION_IN_MS_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.LOCK_EXPIRATION_IN_MS_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.MAX_VERSIONS_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.MAX_VERSIONS_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.SHOW_BREADCRUMB_BRAND_NAME_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.SHOW_BREADCRUMB_DOC_NAME_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.SHOW_BREADCRUMB_FOLDER_NAME_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.SHOW_USER_FRIENDLY_NAME_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.TENANT_DEFAULT;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.TENANT_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.TENANT_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.WOPI_APP_ACCESS_TOKEN_TTL_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.WOPI_APP_ACCESS_TOKEN_TTL_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.WOPI_APP_LOG_ERROR_THRESHOLD_IN_MS_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.WOPI_APP_LOG_ERROR_THRESHOLD_IN_MS_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.WOPI_APP_LOG_WARNING_THRESHOLD_IN_MS_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.WOPI_APP_LOG_WARNING_THRESHOLD_IN_MS_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.WOPI_LOCK_EXPIRATION_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.WOPI_LOCK_EXPIRATION_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.WOPI_SESSION_EXPIRATION_IN_MS_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.WOPI_SESSION_EXPIRATION_IN_MS_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.WOPI_WAC_CONNECTOR_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.WOPI_WAC_CONNECTOR_NAME;

import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.extensiondeployment.WebTerminal;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.annotation.meta.Max;
import com.braintribe.model.generic.annotation.meta.MaxLength;
import com.braintribe.model.generic.annotation.meta.Min;
import com.braintribe.model.generic.annotation.meta.MinLength;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.annotation.meta.Unique;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.wopi.connector.WopiWacConnector;

/**
 * Servlet endpoint for WOPI REST calls
 * 
 *
 */
public interface WopiApp extends WebTerminal {

	final EntityType<WopiApp> T = EntityTypes.T(WopiApp.class);

	String context = "context";

	String wopiWacConnector = "wopiWacConnector";
	String access = "access";
	String lockExpirationInMs = "lockExpirationInMs";
	String wopiSessionExpirationInMs = "wopiSessionExpirationInMs";
	String logWarningThresholdInMs = "logWarningThresholdInMs";
	String logErrorThresholdInMs = "logErrorThresholdInMs";
	String accessTokenTtlInSec = "accessTokenTtlInSec";
	String applyEarlyFileSizeChecks = "applyEarlyFileSizeChecks";

	String maxVersions = "maxVersions";
	String tenant = "tenant";
	String wopiLockExpirationInMs = "wopiLockExpirationInMs";

	String showUserFriendlyName = "showUserFriendlyName";
	String showBreadcrumbBrandName = "showBreadcrumbBrandName";
	String showBreadcrumbDocName = "showBreadcrumbDocName";
	String showBreadcrumbFolderName = "showBreadcrumbFolderName";
	String disablePrint = "disablePrint";
	String disableTranslation = "disableTranslation";

	@Name(CONTEXT_NAME)
	@Description(CONTEXT_DESCRIPTION)
	@Mandatory
	@Unique
	@Initializer("'default'")
	@MinLength(3)
	@MaxLength(255)
	String getContext();
	void setContext(String context);

	@Name(WOPI_WAC_CONNECTOR_NAME)
	@Description(WOPI_WAC_CONNECTOR_DESCRIPTION)
	WopiWacConnector getWopiWacConnector();
	void setWopiWacConnector(WopiWacConnector wopiWacConnector);

	@Name(ACCESS_NAME)
	@Description(ACCESS_DESCRIPTION)
	@Mandatory
	IncrementalAccess getAccess();
	void setAccess(IncrementalAccess access);

	@Name(LOCK_EXPIRATION_IN_MS_NAME)
	@Description(LOCK_EXPIRATION_IN_MS_DESCRIPTION)
	@Mandatory
	@Initializer("3600000l") // 1 hour
	@Min("1l")
	@Max("86400000l") // 1 day
	long getLockExpirationInMs();
	void setLockExpirationInMs(long lockExpirationInMs);

	@Name(WOPI_SESSION_EXPIRATION_IN_MS_NAME)
	@Description(WOPI_SESSION_EXPIRATION_IN_MS_DESCRIPTION)
	@Mandatory
	@Initializer("86400000l") // 1 day
	@Min("3600000l") // 1 hour
	@Max("172800000l") // 2 days
	long getWopiSessionExpirationInMs();
	void setWopiSessionExpirationInMs(long wopiSessionExpirationInMs);

	@Name(WOPI_APP_LOG_WARNING_THRESHOLD_IN_MS_NAME)
	@Description(WOPI_APP_LOG_WARNING_THRESHOLD_IN_MS_DESCRIPTION)
	@Mandatory
	@Initializer("5000l") // 5s
	@Min("1l") // 1ms
	@Max("600000l") // 10min
	long getLogWarningThresholdInMs();
	void setLogWarningThresholdInMs(long logWarningThresholdInMs);

	@Name(WOPI_APP_LOG_ERROR_THRESHOLD_IN_MS_NAME)
	@Description(WOPI_APP_LOG_ERROR_THRESHOLD_IN_MS_DESCRIPTION)
	@Mandatory
	@Initializer("10000l") // 10s
	@Min("1l") // 1ms
	@Max("600000l") // 10min
	long getLogErrorThresholdInMs();
	void setLogErrorThresholdInMs(long logErrorThresholdInMs);

	@Name(WOPI_APP_ACCESS_TOKEN_TTL_NAME)
	@Description(WOPI_APP_ACCESS_TOKEN_TTL_DESCRIPTION)
	@Mandatory
	@Initializer("21600l") // 6h
	@Min("1l")
	@Max("172800l") // 48h
	long getAccessTokenTtlInSec();
	void setAccessTokenTtlInSec(long accessTokenTtlInSec);

	@Name(APPLY_EARLY_FILE_SIZE_CHECKS_NAME)
	@Description(APPLY_EARLY_FILE_SIZE_CHECKS_DESCRIPTION)
	@Mandatory
	@Initializer("true")
	boolean getApplyEarlyFileSizeChecks();
	void setApplyEarlyFileSizeChecks(boolean applyEarlyFileSizeChecks);

	// -----------------------------------------------------------------------
	// WOPI SESSION STANDARD VALUES
	// -----------------------------------------------------------------------

	@Name(MAX_VERSIONS_NAME)
	@Description(MAX_VERSIONS_DESCRIPTION)
	@Mandatory
	@Initializer("5")
	@Min("1")
	@Max("1024")
	int getMaxVersions();
	void setMaxVersions(int maxVersions);

	@Name(TENANT_NAME)
	@Description(TENANT_DESCRIPTION)
	@Mandatory
	@Initializer(TENANT_DEFAULT)
	@MinLength(1)
	@MaxLength(255)
	String getTenant();
	void setTenant(String tenant);

	@Name(WOPI_LOCK_EXPIRATION_NAME)
	@Description(WOPI_LOCK_EXPIRATION_DESCRIPTION)
	@Mandatory
	@Initializer("18000000l") // 5 hours
	@Min("1l")
	@Max("86400000l") // 1 day
	long getWopiLockExpirationInMs();
	void setWopiLockExpirationInMs(long wopiLockExpirationInMs);

	// -----------------------------------------------------------------------
	// UI CUSTOMIZATION
	// -----------------------------------------------------------------------

	@Name(SHOW_USER_FRIENDLY_NAME_NAME)
	@Mandatory
	@Initializer("true")
	boolean getShowUserFriendlyName();
	void setShowUserFriendlyName(boolean showUserFriendlyName);

	@Name(SHOW_BREADCRUMB_BRAND_NAME_NAME)
	@Mandatory
	@Initializer("false")
	boolean getShowBreadcrumbBrandName();
	void setShowBreadcrumbBrandName(boolean showBreadcrumbBrandName);

	@Name(SHOW_BREADCRUMB_DOC_NAME_NAME)
	@Mandatory
	@Initializer("true")
	boolean getShowBreadCrumbDocName();
	void setShowBreadCrumbDocName(boolean showBreadcrumbDocName);

	@Name(SHOW_BREADCRUMB_FOLDER_NAME_NAME)
	@Mandatory
	@Initializer("false")
	boolean getShowBreadcrumbFolderName();
	void setShowBreadcrumbFolderName(boolean showBreadcrumbFolderName);

	@Name(DISABLE_PRINT_NAME)
	@Mandatory
	@Initializer("false")
	boolean getDisablePrint();
	void setDisablePrint(boolean disablePrint);

	@Name(DISABLE_TRANSLATION_NAME)
	@Mandatory
	@Initializer("false")
	boolean getDisableTranslation();
	void setDisableTranslation(boolean disableTranslation);

}
