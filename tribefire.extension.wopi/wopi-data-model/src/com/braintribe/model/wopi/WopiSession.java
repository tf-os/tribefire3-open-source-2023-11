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
package com.braintribe.model.wopi;

import static tribefire.extension.wopi.model.WopiMetaDataConstants.ACCESS_TOKENS_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.ACCESS_TOKENS_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.ALLOWED_ROLES_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.ALLOWED_ROLES_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.BREADCRUMB_BRAND_NAME_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.BREADCRUMB_DOC_NAME_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.BREADCRUMB_FOLDER_NAME_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.CONTEXT_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.CONTEXT_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.CORRELATIONID_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.CORRELATIONID_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.CREATION_DATE_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.CREATION_DATE_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.CREATOR_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.CREATOR_ID_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.CREATOR_ID_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.CREATOR_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.CURRENT_RESOURCE_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.CURRENT_RESOURCE_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.DISABLE_PRINT_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.DISABLE_TRANSLATION_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.DOCUMENT_MODE_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.DOCUMENT_MODE_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.LOCK_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.LOCK_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.MAX_VERSIONS_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.MAX_VERSIONS_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.POST_OPEN_RESOURCE_VERSIONS_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.POST_OPEN_RESOURCE_VERSIONS_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.RESOURCE_VERSIONS_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.RESOURCE_VERSIONS_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.SHOW_BREADCRUMB_BRAND_NAME_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.SHOW_BREADCRUMB_DOC_NAME_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.SHOW_BREADCRUMB_FOLDER_NAME_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.SHOW_USER_FRIENDLY_NAME_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.SOURCE_REFERENCE_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.SOURCE_REFERENCE_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.TENANT_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.TENANT_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.UPDATE_NOTIFICATION_ACCESS_ID_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.UPDATE_NOTIFICATION_ACCESS_ID_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.UPDATE_NOTIFICATION_MESSAGE_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.UPDATE_NOTIFICATION_MESSAGE_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.URL_BREADCRUMB_BRAND_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.URL_BREADCRUMB_FOLDER_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.VERSION_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.VERSION_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.WOPI_LOCK_EXPIRATION_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.WOPI_LOCK_EXPIRATION_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.WOPI_STATUS_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.WOPI_STATUS_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.WOPI_TAGS_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.WOPI_TAGS_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.WOPI_URL_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.WOPI_URL_NAME;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.braintribe.model.generic.StandardIdentifiable;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.annotation.meta.Max;
import com.braintribe.model.generic.annotation.meta.MaxLength;
import com.braintribe.model.generic.annotation.meta.Min;
import com.braintribe.model.generic.annotation.meta.MinLength;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.annotation.meta.Unique;
import com.braintribe.model.generic.annotation.meta.Unmodifiable;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.resource.Resource;

import tribefire.extension.wopi.model.WopiMetaDataConstants;

/**
 * A {@link WopiSession} represents a WOPI document which can be opened for {@link DocumentMode#view} or
 * {@link DocumentMode#edit}
 * 
 * @see <a href= "https://wopi.readthedocs.io/en/latest/">Using the WOPI protocol to integrate with Office for the
 *      web</a>
 * 
 * 
 *
 */
@SelectiveInformation("${correlationId}/${documentMode}/${status}")
public interface WopiSession extends StandardIdentifiable {

	EntityType<WopiSession> T = EntityTypes.T(WopiSession.class);

	String correlationId = "correlationId";
	String sourceReference = "sourceReference";

	String context = "context";

	String maxVersions = "maxVersions";
	String version = "version";
	String tenant = "tenant";
	String status = "status";
	String accessTokens = "accessTokens";
	String allowedRoles = "allowedRoles";
	String currentResource = "currentResource";
	String resourceVersions = "resourceVersions";
	String postOpenResourceVersions = "postOpenResourceVersions";
	String creatorId = "creatorId";
	String creator = "creator";
	String creationDate = "creationDate";
	String lastUpdated = "lastUpdated";
	String lastUpdatedUser = "lastUpdatedUser";
	String lastUpdatedUserId = "lastUpdatedUserId";
	String wopiUrl = "wopiUrl";
	String lock = "lock";
	String documentMode = "documentMode";
	String wopiLockExpirationInMs = "wopiLockExpirationInMs";
	String tags = "tags";
	String updateNotificationMessage = "updateNotificationMessage";
	String updateNotificationAccessId = "updateNotificationAccessId";

	String showUserFriendlyName = "showUserFriendlyName";
	String showBreadcrumbBrandName = "showBreadcrumbBrandName";
	String breadcrumbBrandName = "breadcrumbBrandName";
	String breadcrumbBrandNameUrl = "breadcrumbBrandNameUrl";
	String showBreadcrumbDocName = "showBreadcrumbDocName";
	String breadcrumbDocName = "breadcrumbDocName";
	String showBreadcrumbFolderName = "showBreadcrumbFolderName";
	String breadcrumbFolderName = "breadcrumbFolderName";
	String breadcrumbFolderNameUrl = "breadcrumbFolderNameUrl";
	String disablePrint = "disablePrint";
	String disableTranslation = "disableTranslation";

	@Name(CORRELATIONID_NAME)
	@Description(CORRELATIONID_DESCRIPTION)
	@Mandatory
	@Unique
	@Unmodifiable
	@MinLength(1)
	@MaxLength(255)
	String getCorrelationId();
	void setCorrelationId(String correlationId);

	@Name(SOURCE_REFERENCE_NAME)
	@Description(SOURCE_REFERENCE_DESCRIPTION)
	@MinLength(1)
	@MaxLength(255)
	String getSourceReference();
	void setSourceReference(String sourceReference);

	@Name(CONTEXT_NAME)
	@Description(CONTEXT_DESCRIPTION)
	@Mandatory
	@Unmodifiable
	@MinLength(3)
	@MaxLength(255)
	String getContext();
	void setContext(String context);

	@Name(MAX_VERSIONS_NAME)
	@Description(MAX_VERSIONS_DESCRIPTION)
	@Mandatory
	@Unmodifiable
	@Min("1")
	@Max("1024")
	int getMaxVersions();
	void setMaxVersions(int maxVersions);

	@Name(VERSION_NAME)
	@Description(VERSION_DESCRIPTION)
	@Mandatory
	@Min("1")
	long getVersion();
	void setVersion(long version);

	@Name(TENANT_NAME)
	@Description(TENANT_DESCRIPTION)
	@Unmodifiable
	@Mandatory
	@MinLength(1)
	@MaxLength(255)
	String getTenant();
	void setTenant(String tenant);

	@Name(WOPI_STATUS_NAME)
	@Description(WOPI_STATUS_DESCRIPTION)
	@Mandatory
	@Unmodifiable
	WopiStatus getStatus();
	void setStatus(WopiStatus status);

	@Name(ACCESS_TOKENS_NAME)
	@Description(ACCESS_TOKENS_DESCRIPTION)
	@Unmodifiable
	List<WopiAccessToken> getAccessTokens();
	void setAccessTokens(List<WopiAccessToken> accessTokens);

	@Name(ALLOWED_ROLES_NAME)
	@Description(ALLOWED_ROLES_DESCRIPTION)
	@Mandatory
	@MinLength(1)
	@Unmodifiable
	Set<String> getAllowedRoles();
	void setAllowedRoles(Set<String> allowedRoles);

	@Name(CURRENT_RESOURCE_NAME)
	@Description(CURRENT_RESOURCE_DESCRIPTION)
	@Mandatory
	@Unmodifiable
	Resource getCurrentResource();
	void setCurrentResource(Resource currentResource);

	@Name(RESOURCE_VERSIONS_NAME)
	@Description(RESOURCE_VERSIONS_DESCRIPTION)
	@Unmodifiable
	List<Resource> getResourceVersions();
	void setResourceVersions(List<Resource> resourceVersions);

	@Name(POST_OPEN_RESOURCE_VERSIONS_NAME)
	@Description(POST_OPEN_RESOURCE_VERSIONS_DESCRIPTION)
	@Unmodifiable
	List<Resource> getPostOpenResourceVersions();
	void setPostOpenResourceVersions(List<Resource> postOpenResourceVersions);

	@Name(CREATOR_ID_NAME)
	@Description(CREATOR_ID_DESCRIPTION)
	@Mandatory
	@Unmodifiable
	@MinLength(1)
	@MaxLength(255)
	String getCreatorId();
	void setCreatorId(String creatorId);

	@Name(CREATOR_NAME)
	@Description(CREATOR_DESCRIPTION)
	@Mandatory
	@Unmodifiable
	@MinLength(1)
	@MaxLength(255)
	String getCreator();
	void setCreator(String creator);

	@Name(CREATION_DATE_NAME)
	@Description(CREATION_DATE_DESCRIPTION)
	@Mandatory
	@Unmodifiable
	Date getCreationDate();
	void setCreationDate(Date creationDate);

	@Name(WopiMetaDataConstants.LAST_UPDATED_NAME)
	@Description(WopiMetaDataConstants.LAST_UPDATED_DESCRIPTION)
	@Mandatory
	@Unmodifiable
	Date getLastUpdated();
	void setLastUpdated(Date lastUpdated);

	@Name(WopiMetaDataConstants.LAST_UPDATED_USER_NAME)
	@Description(WopiMetaDataConstants.LAST_UPDATED_USER_DESCRIPTION)
	@Mandatory
	@Unmodifiable
	@MinLength(1)
	@MaxLength(255)
	String getLastUpdatedUser();
	void setLastUpdatedUser(String lastUpdatedUser);

	@Name(WopiMetaDataConstants.LAST_UPDATED_USER_ID_NAME)
	@Description(WopiMetaDataConstants.LAST_UPDATED_USER_ID_DESCRIPTION)
	@Mandatory
	@Unmodifiable
	@MinLength(1)
	@MaxLength(255)
	String getLastUpdatedUserId();
	void setLastUpdatedUserId(String lastUpdatedUserId);

	@Name(WOPI_URL_NAME)
	@Description(WOPI_URL_DESCRIPTION)
	@Mandatory
	@Unmodifiable
	String getWopiUrl();
	void setWopiUrl(String wopiUrl);

	@Name(LOCK_NAME)
	@Description(LOCK_DESCRIPTION)
	@Unmodifiable
	WopiLock getLock();
	void setLock(WopiLock lock);

	@Name(DOCUMENT_MODE_NAME)
	@Description(DOCUMENT_MODE_DESCRIPTION)
	@Unmodifiable
	@Mandatory
	DocumentMode getDocumentMode();
	void setDocumentMode(DocumentMode documentMode);

	@Name(WOPI_LOCK_EXPIRATION_NAME)
	@Description(WOPI_LOCK_EXPIRATION_DESCRIPTION)
	@Unmodifiable
	@Mandatory
	@Min("1")
	Long getWopiLockExpirationInMs();
	void setWopiLockExpirationInMs(Long wopiLockExpirationInMs);

	@Name(WOPI_TAGS_NAME)
	@Description(WOPI_TAGS_DESCRIPTION)
	Set<String> getTags();
	void setTags(Set<String> tags);

	@Name(UPDATE_NOTIFICATION_MESSAGE_NAME)
	@Description(UPDATE_NOTIFICATION_MESSAGE_DESCRIPTION)
	String getUpdateNotificationMessage();
	void setUpdateNotificationMessage(String updateNotificationMessage);

	@Name(UPDATE_NOTIFICATION_ACCESS_ID_NAME)
	@Description(UPDATE_NOTIFICATION_ACCESS_ID_DESCRIPTION)
	String getUpdateNotificationAccessId();
	void setUpdateNotificationAccessId(String updateNotificationAccessId);

	// -----------------------------------------------------------------------
	// UI CUSTOMIZATION
	// -----------------------------------------------------------------------

	@Name(SHOW_USER_FRIENDLY_NAME_NAME)
	@Unmodifiable
	@Mandatory
	boolean getShowUserFriendlyName();
	void setShowUserFriendlyName(boolean showUserFriendlyName);

	@Name(SHOW_BREADCRUMB_BRAND_NAME_NAME)
	@Unmodifiable
	@Mandatory
	boolean getShowBreadcrumbBrandName();
	void setShowBreadcrumbBrandName(boolean showBreadcrumbBrandName);

	@Name(BREADCRUMB_BRAND_NAME_NAME)
	@Unmodifiable
	@Mandatory
	String getBreadcrumbBrandName();
	void setBreadcrumbBrandName(String breadcrumbBrandName);

	@Name(URL_BREADCRUMB_BRAND_NAME)
	@Unmodifiable
	@Mandatory
	String getBreadcrumbBrandNameUrl();
	void setBreadcrumbBrandNameUrl(String breadcrumbBrandNameUrl);

	@Name(SHOW_BREADCRUMB_DOC_NAME_NAME)
	@Unmodifiable
	@Mandatory
	boolean getShowBreadcrumbDocName();
	void setShowBreadcrumbDocName(boolean showBreadcrumbDocName);

	@Name(BREADCRUMB_DOC_NAME_NAME)
	@Unmodifiable
	@Mandatory
	String getBreadcrumbDocName();
	void setBreadcrumbDocName(String breadcrumbDocName);

	@Name(SHOW_BREADCRUMB_FOLDER_NAME_NAME)
	@Unmodifiable
	@Mandatory
	boolean getShowBreadcrumbFolderName();
	void setShowBreadcrumbFolderName(boolean showBreadcrumbFolderName);

	@Name(BREADCRUMB_FOLDER_NAME_NAME)
	@Unmodifiable
	@Mandatory
	String getBreadcrumbFolderName();
	void setBreadcrumbFolderName(String breadcrumbFolderName);

	@Name(URL_BREADCRUMB_FOLDER_NAME)
	@Unmodifiable
	@Mandatory
	String getBreadcrumbFolderNameUrl();
	void setBreadcrumbFolderNameUrl(String breadcrumbFolderNameUrl);

	@Name(DISABLE_PRINT_NAME)
	@Unmodifiable
	@Mandatory
	boolean getDisablePrint();
	void setDisablePrint(boolean disablePrint);

	@Name(DISABLE_TRANSLATION_NAME)
	@Unmodifiable
	@Mandatory
	boolean getDisableTranslation();
	void setDisableTranslation(boolean disableTranslation);

}
