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
package tribefire.extension.wopi.templates.api;

import com.braintribe.cfg.ScopeContext;
import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.deployment.Module;
import com.braintribe.model.descriptive.HasExternalId;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.resource.configuration.ExternalResourcesContext;
import com.braintribe.wire.api.scope.InstanceConfiguration;

import tribefire.extension.wopi.processing.StorageType;

/**
 * WOPI Template Context - contains information for configuring WOPI functionality
 * 
 *
 */
public interface WopiTemplateContext extends ScopeContext {

	// -----------------------------------------------------------------------
	// Common
	// -----------------------------------------------------------------------

	/**
	 * The WOPI {@link Module}
	 */
	Module getWopiModule();

	/**
	 * Unique context information to distinguish multiple WOPI deployments - based on this 'globalId, 'externalId',
	 * 'name' of Deployables gets calculated. In case {@link WopiTemplateContext#builder(IncrementalAccess)} has an
	 * {@link IncrementalAccess} attached 'globalId, 'externalId', 'name' of the access won't be reset of course.
	 * 
	 * This context should be written in 'camelCase'
	 */
	String getContext();

	/**
	 * Optional configuration for database usage - if not set smood access will be used; if
	 * {@link WopiTemplateContext#getStorageType()} is {@link StorageType#db} it must be set
	 */
	WopiTemplateDatabaseContext getDatabaseContext();

	/**
	 * Optional configuration for external resources usage - will be used if
	 * {@link WopiTemplateContext#getStorageType()} is {@link StorageType#external}
	 */
	ExternalResourcesContext getExternalResourcesContext();

	/**
	 * Storage type for Resources
	 */
	StorageType getStorageType();

	/**
	 * Builder
	 * 
	 * {@link IncrementalAccess} to be used for as WOPI access - all the configuration is used from there (data &
	 * Resource streaming)
	 */
	static WopiTemplateContextBuilder builder(IncrementalAccess access) {
		return new WopiTemplateContextImpl(access);
	}

	// -----------------------------------------------------------------------
	// WopiAccess
	// -----------------------------------------------------------------------

	/**
	 * An {@link IncrementalAccess} if set - otherwise null
	 */
	IncrementalAccess getAccess();

	/**
	 * Optional storage folder for storing in file system - if not set and {@link WopiTemplateContext#getStorageType()}
	 * is {@link StorageType#fs} default storage file system location will be used in case of file system usage
	 */
	String getStorageFolder();

	// -----------------------------------------------------------------------
	// WopiWacConnector
	// -----------------------------------------------------------------------

	String getWacDiscoveryEndpoint();

	String getCustomPublicServicesUrl();

	Integer getConnectionRequestTimeoutInMs();

	Integer getConnectTimeoutInMs();

	Integer getSocketTimeoutInMs();

	Integer getConnectionRetries();

	Integer getDelayOnRetryInMs();

	// -----------------------------------------------------------------------
	// WopiServiceProcessor
	// -----------------------------------------------------------------------

	Long getWopiServiceProcessorLogWarningThresholdInMs();

	Long getWopiServiceProcessorLogErrorThresholdInMs();

	// -----------------------------------------------------------------------
	// ExpireWopiSessionWorker
	// -----------------------------------------------------------------------

	Long getExpireWopiSessionIntervalInMs();

	// -----------------------------------------------------------------------
	// CleanupWopiSessionWorker
	// -----------------------------------------------------------------------

	Long getCleanupWopiSessionIntervalInMs();

	// -----------------------------------------------------------------------
	// WopiApp
	// -----------------------------------------------------------------------

	Long getLockExpirationInMs();

	Long getWopiSessionExpirationInMs();

	Long getWopiAppLogWarningThresholdInMs();

	Long getWopiAppLogErrorThresholdInMs();

	// WOPI SESSION STANDARD VALUES
	Integer getMaxVersions();

	String getTenant();

	Long getWopiLockExpirationInMs();

	// UI CUSTOMIZATION
	Boolean getShowUserFriendlyName();

	Boolean getShowBreadcrumbBrandName();

	Boolean getShowBreadCrumbDocName();

	Boolean getShowBreadcrumbFolderName();

	Boolean getDisablePrint();

	Boolean getDisableTranslation();

	// -----------------------------------------------------------------------
	// CONTEXT METHODS
	// -----------------------------------------------------------------------

	<T extends GenericEntity> T create(EntityType<T> entityType, InstanceConfiguration instanceConfiguration);

	<T extends GenericEntity> T lookup(String globalId);

	<T extends HasExternalId> T lookupExternalId(String externalId);

}