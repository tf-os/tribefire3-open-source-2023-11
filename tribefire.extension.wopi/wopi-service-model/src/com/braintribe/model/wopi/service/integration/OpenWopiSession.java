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
package com.braintribe.model.wopi.service.integration;

import static tribefire.extension.wopi.model.WopiMetaDataConstants.ALLOWED_ROLES_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.ALLOWED_ROLES_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.BREADCRUMB_BRAND_NAME_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.BREADCRUMB_DOC_NAME_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.BREADCRUMB_FOLDER_NAME_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.CORRELATIONID_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.CORRELATIONID_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.CREATOR_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.CREATOR_ID_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.CREATOR_ID_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.CREATOR_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.DISABLE_PRINT_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.DISABLE_TRANSLATION_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.DOCUMENT_MODE_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.DOCUMENT_MODE_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.MAX_VERSIONS_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.MAX_VERSIONS_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.RESOURCE_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.RESOURCE_NAME;
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
import static tribefire.extension.wopi.model.WopiMetaDataConstants.WOPI_LOCK_EXPIRATION_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.WOPI_LOCK_EXPIRATION_NAME;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.WOPI_TAGS_DESCRIPTION;
import static tribefire.extension.wopi.model.WopiMetaDataConstants.WOPI_TAGS_NAME;

import java.util.Set;

import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.annotation.meta.MaxLength;
import com.braintribe.model.generic.annotation.meta.MinLength;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.wopi.DocumentMode;

/**
 * Opens a WOPI session
 * 
 *
 */
public interface OpenWopiSession extends WopiRequest {

	EntityType<OpenWopiSession> T = EntityTypes.T(OpenWopiSession.class);

	@Override
	EvalContext<? extends OpenWopiSessionResult> eval(Evaluator<ServiceRequest> evaluator);

	String maxVersions = "maxVersions";
	String tenant = "tenant";

	String correlationId = "correlationId";
	String sourceReference = "sourceReference";
	String documentMode = "documentMode";
	String allowedRoles = "allowedRoles";
	String resource = "resource";

	String wopiLockExpirationInMs = "wopiLockExpirationInMs";
	String tags = "tags";
	String updateNotificationMessage = "updateNotificationMessage";
	String updateNotificationAccessId = "updateNotificationAccessId";
	String creatorId = "creatorId";
	String creator = "creator";

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

	@Name(MAX_VERSIONS_NAME)
	@Description(MAX_VERSIONS_DESCRIPTION)
	Integer getMaxVersions();
	void setMaxVersions(Integer maxVersions);

	@Name(TENANT_NAME)
	@Description(TENANT_DESCRIPTION)
	String getTenant();
	void setTenant(String tenant);

	@Name(WOPI_LOCK_EXPIRATION_NAME)
	@Description(WOPI_LOCK_EXPIRATION_DESCRIPTION)
	Long getWopiLockExpirationInMs();
	void setWopiLockExpirationInMs(Long wopiLockExpirationInMs);

	@Name(CORRELATIONID_NAME)
	@Description(CORRELATIONID_DESCRIPTION)
	@Mandatory
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

	@Name(DOCUMENT_MODE_NAME)
	@Description(DOCUMENT_MODE_DESCRIPTION)
	@Mandatory
	@Initializer("enum(com.braintribe.model.wopi.DocumentMode,view)")
	DocumentMode getDocumentMode();
	void setDocumentMode(DocumentMode documentMode);

	@Name(ALLOWED_ROLES_NAME)
	@Description(ALLOWED_ROLES_DESCRIPTION)
	@Mandatory
	@MinLength(1)
	@Initializer("{'$all'}")
	Set<String> getAllowedRoles();
	void setAllowedRoles(Set<String> allowedRoles);

	@Name(RESOURCE_NAME)
	@Description(RESOURCE_DESCRIPTION)
	@Mandatory
	Resource getResource();
	void setResource(Resource resource);

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

	@Name(CREATOR_ID_NAME)
	@Description(CREATOR_ID_DESCRIPTION)
	@MinLength(1)
	@MaxLength(255)
	String getCreatorId();
	void setCreatorId(String creatorId);

	@Name(CREATOR_NAME)
	@Description(CREATOR_DESCRIPTION)
	@MinLength(1)
	@MaxLength(255)
	String getCreator();
	void setCreator(String creator);

	// -----------------------------------------------------------------------
	// UI CUSTOMIZATION
	// -----------------------------------------------------------------------

	@Name(SHOW_USER_FRIENDLY_NAME_NAME)
	Boolean getShowUserFriendlyName();
	void setShowUserFriendlyName(Boolean showUserFriendlyName);

	@Name(SHOW_BREADCRUMB_BRAND_NAME_NAME)
	Boolean getShowBreadcrumbBrandName();
	void setShowBreadcrumbBrandName(Boolean showBreadcrumbBrandName);

	@Name(BREADCRUMB_BRAND_NAME_NAME)
	String getBreadcrumbBrandName();
	void setBreadcrumbBrandName(String breadcrumbBrandName);

	@Name(URL_BREADCRUMB_BRAND_NAME)
	String getBreadcrumbBrandNameUrl();
	void setBreadcrumbBrandNameUrl(String breadcrumbBrandNameUrl);

	@Name(SHOW_BREADCRUMB_DOC_NAME_NAME)
	Boolean getShowBreadcrumbDocName();
	void setShowBreadcrumbDocName(Boolean showBreadcrumbDocName);

	@Name(BREADCRUMB_DOC_NAME_NAME)
	String getBreadcrumbDocName();
	void setBreadcrumbDocName(String breadcrumbDocName);

	@Name(SHOW_BREADCRUMB_FOLDER_NAME_NAME)
	Boolean getShowBreadcrumbFolderName();
	void setShowBreadcrumbFolderName(Boolean showBreadcrumbFolderName);

	@Name(BREADCRUMB_FOLDER_NAME_NAME)
	String getBreadcrumbFolderName();
	void setBreadcrumbFolderName(String breadcrumbFolderName);

	@Name(URL_BREADCRUMB_FOLDER_NAME)
	String getBreadcrumbFolderNameUrl();
	void setBreadcrumbFolderNameUrl(String breadcrumbFolderNameUrl);

	@Name(DISABLE_PRINT_NAME)
	Boolean getDisablePrint();
	void setDisablePrint(Boolean disablePrint);

	@Name(DISABLE_TRANSLATION_NAME)
	Boolean getDisableTranslation();
	void setDisableTranslation(Boolean disableTranslation);

}
