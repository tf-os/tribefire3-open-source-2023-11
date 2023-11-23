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
package com.braintribe.model.library.deployment.service;

import com.braintribe.model.extensiondeployment.access.AccessRequestProcessor;
import com.braintribe.model.generic.annotation.meta.Confidential;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface LibraryService extends AccessRequestProcessor {

	final EntityType<LibraryService> T = EntityTypes.T(LibraryService.class);

	void setProfile(Profile profile);
	Profile getProfile();

	void setWkHtmlToPdf(WkHtmlToPdf wkHtmlToPdf);
	WkHtmlToPdf getWkHtmlToPdf();

	void setRepositoryBasePath(String repositoryBasePath);
	String getRepositoryBasePath();

	void setRepositoryUsername(String repositoryUsername);
	String getRepositoryUsername();

	void setRepositoryPassword(String repositoryPassword);
	@Confidential
	String getRepositoryPassword();

	void setRepositoryUrl(String url);
	String getRepositoryUrl();

	void setRavenhurstUrl(String library_RAVENHURST_URL);
	String getRavenhurstUrl();

	void setNvdMirrorBasePath(String nvdMirrorBasePath);
	String getNvdMirrorBasePath();

}
