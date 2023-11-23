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
package com.braintribe.model.processing.wopi.model;

import com.braintribe.model.processing.wopi.misc.HttpResponseJSON;

/*
	{
	"FolderName":{"type":"string","optional":false},
	"BreadcrumbBrandIconUrl":{"type":"string","default":"","optional":true},
	"BreadcrumbBrandName":{"type":"string","default":"","optional":true},
	"BreadcrumbBrandUrl":{"type":"string","default":"","optional":true},
	"BreadcrumbDocName":{"type":"string","default":"","optional":true},
	"BreadcrumbDocUrl":{"type":"string","default":"","optional":true},
	"BreadcrumbFolderName":{"type":"string","default":"","optional":true},
	"BreadcrumbFolderUrl":{"type":"string","default":"","optional":true},
	"ClientUrl":{"type":"string","default":"","optional":true},
	"CloseButtonClosesWindow":{"type":"bool","default":false,"optional":true},
	"CloseUrl":{"type":"string","default":"","optional":true},
	"HostAuthenticationId"{"type":"string","default":"","optional":true},
	"HostEditUrl":{"type":"string","default":"","optional":true},
	"HostEmbeddedEditUrl":{"type":"string","default":"","optional":true},
	"HostEmbeddedViewUrl":{"type":"string","default":"","optional":true},
	"HostName":{"type":"string","default":"","optional":true},
	"HostViewUrl":{"type":"string","default":"","optional":true},
	"OwnerId":{"type":"string","optional":false},
	"PresenceProvider"{"type":"string","default":"","optional":true},
	"PresenceUserId"{"type":"string","default":"","optional":true},
	"PrivacyUrl":{"type":"string","default":"","optional":true},
	"SignoutUrl":{"type":"string","default":"","optional":true},
	"SupportsSecureStore":{"type":"bool","default":false,"optional":true},
	"TenantId"{"type":"string","default":"","optional":true},
	"TermsOfUseUrl":{"type":"string","default":"","optional":true},
	"UserCanWrite":{"type":"bool","default":false,"optional":true},
	"UserFriendlyName":{"type":"string","default":"","optional":true},
	"UserId":{"type":"string","default":"","optional":true},
	"WebEditingDisabled":{"type":"bool","default":false,"optional":true},
	}
 */
public class CheckFolderInfo extends HttpResponseJSON {
	// empty by intention
}
