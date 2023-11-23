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

import java.util.function.Function;

import com.braintribe.logging.Logger;
import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.deployment.Module;
import com.braintribe.model.descriptive.HasExternalId;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.resource.configuration.ExternalResourcesContext;
import com.braintribe.utils.StringTools;
import com.braintribe.wire.api.scope.InstanceConfiguration;
import com.braintribe.wire.api.scope.InstanceQualification;

import tribefire.extension.wopi.processing.StorageType;
import tribefire.extension.wopi.templates.util.WopiTemplateUtil;

public class WopiTemplateContextImpl implements WopiTemplateContext, WopiTemplateContextBuilder {

	private static final Logger logger = Logger.getLogger(WopiTemplateContextImpl.class);

	private Function<String, ? extends GenericEntity> lookupFunction;
	private Function<String, ? extends HasExternalId> lookupExternalIdFunction;
	private Function<EntityType<?>, GenericEntity> entityFactory = EntityType::create;

	// -----------------------------------------------------------------------
	// Constructor
	// -----------------------------------------------------------------------

	public WopiTemplateContextImpl(IncrementalAccess access) {
		this.access = access;
	}

	// -----------------------------------------------------------------------
	// Common
	// -----------------------------------------------------------------------

	private String context;

	private Module wopiModule;

	// -----------------------------------------------------------------------
	// WopiAccess
	// -----------------------------------------------------------------------

	private IncrementalAccess access;
	private WopiTemplateDatabaseContext wopiDatabaseContext;
	private ExternalResourcesContext externalResourcesContext;
	private String storageFolder;
	private StorageType storageType;

	// -----------------------------------------------------------------------
	// WopiWacConnector
	// -----------------------------------------------------------------------

	private String wacDiscoveryEndpoint;
	private String customPublicServicesUrl;
	private Integer connectionRequestTimeoutInMs;
	private Integer connectTimeoutInMs;
	private Integer socketTimeoutInMs;
	private Integer connectionRetries;
	private Integer delayOnRetryInMs;

	// -----------------------------------------------------------------------
	// WopiServiceProcessor
	// -----------------------------------------------------------------------

	private Long wopiServiceProcessorLogWarningThresholdInMs;

	private Long wopiServiceProcessorLogErrorThresholdInMs;

	// -----------------------------------------------------------------------
	// ExpireWopiSessionWorker
	// -----------------------------------------------------------------------

	private Long expireWopiSessionIntervalInMs;

	// -----------------------------------------------------------------------
	// CleanupWopiSessionWorker
	// -----------------------------------------------------------------------

	private Long cleanupWopiSessionIntervalInMs;

	// -----------------------------------------------------------------------
	// WopiApp
	// -----------------------------------------------------------------------

	private Long lockExpirationInMs;
	private Long wopiSessionExpirationInMs;
	private Long wopiAppLogWarningThresholdInMs;
	private Long wopiAppLogErrorThresholdInMs;

	// WOPI SESSION STANDARD VALUES
	private Integer maxVersions;
	private String tenant;
	private Long wopiLockExpirationInMs;

	// UI CUSTOMIZATION
	private Boolean showUserFriendlyName;
	private Boolean showBreadcrumbBrandName;
	private Boolean showBreadCrumbDocName;
	private Boolean showBreadcrumbFolderName;
	private Boolean disablePrint;
	private Boolean disableTranslation;

	// -----------------------------------------------------------------------
	// -----------------------------------------------------------------------
	// -----------------------------------------------------------------------

	// -----------------------------------------------------------------------
	// Common
	// -----------------------------------------------------------------------

	@Override
	public WopiTemplateContextBuilder setContext(String context) {
		this.context = context;
		return this;
	}

	@Override
	public String getContext() {
		return context;
	}

	@Override
	public WopiTemplateContextBuilder setDatabaseContext(WopiTemplateDatabaseContext wopiDatabaseContext) {
		this.wopiDatabaseContext = wopiDatabaseContext;
		return this;
	}

	@Override
	public WopiTemplateDatabaseContext getDatabaseContext() {
		return wopiDatabaseContext;
	}

	@Override
	public WopiTemplateContextBuilder setEntityFactory(Function<EntityType<?>, GenericEntity> entityFactory) {
		this.entityFactory = entityFactory;
		return this;
	}

	@Override
	public Module getWopiModule() {
		return wopiModule;
	}

	@Override
	public WopiTemplateContextBuilder setWopiModule(Module module) {
		this.wopiModule = module;
		return this;
	}

	@Override
	public WopiTemplateContextBuilder setLookupFunction(Function<String, ? extends GenericEntity> lookupFunction) {
		this.lookupFunction = lookupFunction;
		return this;
	}

	@Override
	public WopiTemplateContextBuilder setLookupExternalIdFunction(Function<String, ? extends HasExternalId> lookupExternalIdFunction) {
		this.lookupExternalIdFunction = lookupExternalIdFunction;
		return this;
	}

	@Override
	public WopiTemplateContext build() {
		return this;
	}

	// GSC, 2019-12-08
	// Commenting specific hashCode() & equals() implementation, since i'm sure
	// this is just wrong. This prevents this template from returning new/updated
	// bean for new Context instances (potentially having different configurations set)
	// as long as the context has the same context string set.
	// From my PoV it's the callers responsibility to pass the same context instance
	// (e.g.: by managing in an outside Wire) in case he wants to have managed beans in multiple
	// runs.

	/* @Override public int hashCode() { return context.hashCode(); }
	 * 
	 * @Override public boolean equals(Object obj) { if (obj instanceof WopiTemplateContext) { return
	 * ((WopiTemplateContext) obj).getContext().equals(this.context); } return super.equals(obj); } */

	@Override
	public String toString() {
		return "WopiTemplateContextImpl [lookupFunction=" + lookupFunction + ", lookupExternalIdFunction=" + lookupExternalIdFunction
				+ ", entityFactory=" + entityFactory + ", context=" + context + ", wopiModule=" + wopiModule + ", access=" + access
				+ ", wopiDatabaseContext=" + wopiDatabaseContext + ", externalResourcesContext=" + externalResourcesContext + ", storageFolder="
				+ storageFolder + ", storageType=" + storageType + ", wacDiscoveryEndpoint=" + wacDiscoveryEndpoint + ", customPublicServicesUrl="
				+ customPublicServicesUrl + ", connectionRequestTimeoutInMs=" + connectionRequestTimeoutInMs + ", connectTimeoutInMs="
				+ connectTimeoutInMs + ", socketTimeoutInMs=" + socketTimeoutInMs + ", connectionRetries=" + connectionRetries + ", delayOnRetryInMs="
				+ delayOnRetryInMs + ", wopiServiceProcessorLogWarningThresholdInMs=" + wopiServiceProcessorLogWarningThresholdInMs
				+ ", wopiServiceProcessorLogErrorThresholdInMs=" + wopiServiceProcessorLogErrorThresholdInMs + ", expireWopiSessionIntervalInMs="
				+ expireWopiSessionIntervalInMs + ", cleanupWopiSessionIntervalInMs=" + cleanupWopiSessionIntervalInMs + ", lockExpirationInMs="
				+ lockExpirationInMs + ", wopiSessionExpirationInMs=" + wopiSessionExpirationInMs + ", wopiAppLogWarningThresholdInMs="
				+ wopiAppLogWarningThresholdInMs + ", wopiAppLogErrorThresholdInMs=" + wopiAppLogErrorThresholdInMs + ", maxVersions=" + maxVersions
				+ ", tenant=" + tenant + ", wopiLockExpirationInMs=" + wopiLockExpirationInMs + ", showUserFriendlyName=" + showUserFriendlyName
				+ ", showBreadcrumbBrandName=" + showBreadcrumbBrandName + ", showBreadCrumbDocName=" + showBreadCrumbDocName
				+ ", showBreadcrumbFolderName=" + showBreadcrumbFolderName + ", disablePrint=" + disablePrint + ", disableTranslation="
				+ disableTranslation + "]";
	}

	// -----------------------------------------------------------------------
	// WopiAccess
	// -----------------------------------------------------------------------

	@Override
	public WopiTemplateContextBuilder setStorageFolder(String storageFolder) {
		this.storageFolder = storageFolder;
		return this;
	}

	@Override
	public String getStorageFolder() {
		return storageFolder;
	}

	@Override
	public WopiTemplateContextBuilder setAccess(IncrementalAccess access) {
		this.access = access;
		return this;
	}

	@Override
	public IncrementalAccess getAccess() {
		return access;
	}

	@Override
	public StorageType getStorageType() {
		return storageType;
	}

	@Override
	public WopiTemplateContextBuilder setStorageType(StorageType storageType) {
		this.storageType = storageType;
		return this;
	}

	@Override
	public WopiTemplateContextBuilder setExternalResourcesContext(ExternalResourcesContext externalResourcesContext) {
		this.externalResourcesContext = externalResourcesContext;
		return this;
	}

	@Override
	public ExternalResourcesContext getExternalResourcesContext() {
		return externalResourcesContext;
	}

	// -----------------------------------------------------------------------
	// WopiWacConnector
	// -----------------------------------------------------------------------

	@Override
	public String getWacDiscoveryEndpoint() {
		return wacDiscoveryEndpoint;
	}

	@Override
	public WopiTemplateContextBuilder setWacDiscoveryEndpoint(String wacDiscoveryEndpoint) {
		this.wacDiscoveryEndpoint = wacDiscoveryEndpoint;
		return this;
	}

	@Override
	public String getCustomPublicServicesUrl() {
		return customPublicServicesUrl;
	}

	@Override
	public WopiTemplateContextBuilder setCustomPublicServicesUrl(String customPublicServicesUrl) {
		this.customPublicServicesUrl = customPublicServicesUrl;
		return this;
	}

	@Override
	public Integer getConnectionRequestTimeoutInMs() {
		return connectionRequestTimeoutInMs;
	}
	@Override
	public WopiTemplateContextImpl setConnectionRequestTimeoutInMs(Integer connectionRequestTimeoutInMs) {
		this.connectionRequestTimeoutInMs = connectionRequestTimeoutInMs;
		return this;
	}
	@Override
	public Integer getConnectTimeoutInMs() {
		return connectTimeoutInMs;
	}
	@Override
	public WopiTemplateContextImpl setConnectTimeoutInMs(Integer connectTimeoutInMs) {
		this.connectTimeoutInMs = connectTimeoutInMs;
		return this;
	}
	@Override
	public Integer getSocketTimeoutInMs() {
		return socketTimeoutInMs;
	}
	@Override
	public WopiTemplateContextImpl setSocketTimeoutInMs(Integer socketTimeoutInMs) {
		this.socketTimeoutInMs = socketTimeoutInMs;
		return this;
	}
	@Override
	public Integer getConnectionRetries() {
		return connectionRetries;
	}
	@Override
	public WopiTemplateContextImpl setConnectionRetries(Integer connectionRetries) {
		this.connectionRetries = connectionRetries;
		return this;
	}
	@Override
	public Integer getDelayOnRetryInMs() {
		return delayOnRetryInMs;
	}
	@Override
	public WopiTemplateContextImpl setDelayOnRetryInMs(Integer delayOnRetryInMs) {
		this.delayOnRetryInMs = delayOnRetryInMs;
		return this;
	}

	// -----------------------------------------------------------------------
	// WopiServiceProcessor
	// -----------------------------------------------------------------------

	@Override
	public Long getWopiServiceProcessorLogWarningThresholdInMs() {
		return wopiServiceProcessorLogWarningThresholdInMs;
	}
	@Override
	public WopiTemplateContextImpl setWopiServiceProcessorLogWarningThresholdInMs(Long wopiServiceProcessorLogWarningThresholdInMs) {
		this.wopiServiceProcessorLogWarningThresholdInMs = wopiServiceProcessorLogWarningThresholdInMs;
		return this;
	}
	@Override
	public Long getWopiServiceProcessorLogErrorThresholdInMs() {
		return wopiServiceProcessorLogErrorThresholdInMs;
	}
	@Override
	public WopiTemplateContextImpl setWopiServiceProcessorLogErrorThresholdInMs(Long wopiServiceProcessorLogErrorThresholdInMs) {
		this.wopiServiceProcessorLogErrorThresholdInMs = wopiServiceProcessorLogErrorThresholdInMs;
		return this;
	}

	// -----------------------------------------------------------------------
	// ExpireWopiSessionWorker
	// -----------------------------------------------------------------------

	@Override
	public WopiTemplateContextBuilder setExpireWopiSessionIntervalInMs(Long expireWopiSessionIntervalInMs) {
		this.expireWopiSessionIntervalInMs = expireWopiSessionIntervalInMs;
		return this;
	}

	@Override
	public Long getExpireWopiSessionIntervalInMs() {
		return expireWopiSessionIntervalInMs;
	}

	// -----------------------------------------------------------------------
	// CleanupWopiSessionWorker
	// -----------------------------------------------------------------------

	@Override
	public WopiTemplateContextBuilder setCleanupWopiSessionIntervalInMs(Long cleanupWopiSessionIntervalInMs) {
		this.cleanupWopiSessionIntervalInMs = cleanupWopiSessionIntervalInMs;
		return this;
	}

	@Override
	public Long getCleanupWopiSessionIntervalInMs() {
		return cleanupWopiSessionIntervalInMs;
	}

	// -----------------------------------------------------------------------
	// WopiApp
	// -----------------------------------------------------------------------

	@Override
	public WopiTemplateContextBuilder setLockExpirationInMs(Long lockExpirationInMs) {
		this.lockExpirationInMs = lockExpirationInMs;
		return this;
	}

	@Override
	public WopiTemplateContextBuilder setWopiSessionExpirationInMs(Long wopiSessionExpirationInMs) {
		this.wopiSessionExpirationInMs = wopiSessionExpirationInMs;
		return this;
	}

	@Override
	public WopiTemplateContextBuilder setWopiAppLogWarningThresholdInMs(Long wopiAppLogWarningThresholdInMs) {
		this.wopiAppLogWarningThresholdInMs = wopiAppLogWarningThresholdInMs;
		return this;
	}

	@Override
	public WopiTemplateContextBuilder setWopiAppLogErrorThresholdInMs(Long wopiAppLogErrorThresholdInMs) {
		this.wopiAppLogErrorThresholdInMs = wopiAppLogErrorThresholdInMs;
		return this;
	}

	@Override
	public WopiTemplateContextBuilder setMaxVersions(Integer maxVersions) {
		this.maxVersions = maxVersions;
		return this;
	}

	@Override
	public WopiTemplateContextBuilder setTenant(String tenant) {
		this.tenant = tenant;
		return this;
	}

	@Override
	public WopiTemplateContextBuilder setWopiLockExpirationInMs(Long wopiLockExpirationInMs) {
		this.wopiLockExpirationInMs = wopiLockExpirationInMs;
		return this;
	}

	@Override
	public WopiTemplateContextBuilder setShowUserFriendlyName(Boolean showUserFriendlyName) {
		this.showUserFriendlyName = showUserFriendlyName;
		return this;
	}

	@Override
	public WopiTemplateContextBuilder setShowBreadcrumbBrandName(Boolean showBreadcrumbBrandName) {
		this.showBreadcrumbBrandName = showBreadcrumbBrandName;
		return this;
	}

	@Override
	public WopiTemplateContextBuilder setShowBreadCrumbDocName(Boolean showBreadcrumbDocName) {
		this.showBreadCrumbDocName = showBreadcrumbDocName;
		return this;
	}

	@Override
	public WopiTemplateContextBuilder setShowBreadcrumbFolderName(Boolean showBreadcrumbFolderName) {
		this.showBreadcrumbFolderName = showBreadcrumbFolderName;
		return this;
	}

	@Override
	public WopiTemplateContextBuilder setDisablePrint(Boolean disablePrint) {
		this.disablePrint = disablePrint;
		return this;
	}

	@Override
	public WopiTemplateContextBuilder setDisableTranslation(Boolean disableTranslation) {
		this.disableTranslation = disableTranslation;
		return this;
	}

	@Override
	public Long getLockExpirationInMs() {
		return lockExpirationInMs;
	}

	@Override
	public Long getWopiSessionExpirationInMs() {
		return wopiSessionExpirationInMs;
	}

	@Override
	public Long getWopiAppLogWarningThresholdInMs() {
		return wopiAppLogWarningThresholdInMs;
	}

	@Override
	public Long getWopiAppLogErrorThresholdInMs() {
		return wopiAppLogErrorThresholdInMs;
	}

	@Override
	public Integer getMaxVersions() {
		return maxVersions;
	}

	@Override
	public String getTenant() {
		return tenant;
	}

	@Override
	public Long getWopiLockExpirationInMs() {
		return wopiLockExpirationInMs;
	}

	@Override
	public Boolean getShowUserFriendlyName() {
		return showUserFriendlyName;
	}

	@Override
	public Boolean getShowBreadcrumbBrandName() {
		return showBreadcrumbBrandName;
	}

	@Override
	public Boolean getShowBreadCrumbDocName() {
		return showBreadCrumbDocName;
	}

	@Override
	public Boolean getShowBreadcrumbFolderName() {
		return showBreadcrumbFolderName;
	}

	@Override
	public Boolean getDisablePrint() {
		return disablePrint;
	}

	@Override
	public Boolean getDisableTranslation() {
		return disableTranslation;
	}

	// -----------------------------------------------------------------------
	// CONTEXT METHODS
	// -----------------------------------------------------------------------

	@Override
	public <T extends GenericEntity> T create(EntityType<T> entityType, InstanceConfiguration instanceConfiguration) {

		T entity = (T) entityFactory.apply(entityType);

		String globalId = WopiTemplateUtil.resolveContextBasedGlobalId(this, instanceConfiguration);

		InstanceQualification qualification = instanceConfiguration.qualification();

		entity.setGlobalId(globalId);

		if (entity instanceof HasExternalId) {
			HasExternalId eid = (HasExternalId) entity;

			String part1 = StringTools.camelCaseToDashSeparated(entityType.getShortName());
			String part2 = StringTools.camelCaseToDashSeparated(context);
			String part3 = StringTools.camelCaseToDashSeparated(qualification.space().getClass().getSimpleName());
			String part4 = StringTools.camelCaseToDashSeparated(qualification.name());
			String externalId = part1 + "." + part2 + "." + part3 + "." + part4;
			externalId = externalId.replace("/", "-");

			if (logger.isDebugEnabled()) {
				logger.debug("Prepared externalId: '" + externalId + "' for globalId: '" + globalId + "'");
			}

			eid.setExternalId(externalId);
		}

		return entity;
	}

	@Override
	public <T extends GenericEntity> T lookup(String globalId) {
		return (T) lookupFunction.apply(globalId);
	}

	@Override
	public <T extends HasExternalId> T lookupExternalId(String externalId) {
		return (T) lookupExternalIdFunction.apply(externalId);
	}

}
