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
package com.braintribe.model.jinni.api;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.annotation.meta.Description;

@Abstract
public interface MalaclypseOptions extends GenericEntity {
	@Description("Sets environment variable ARTIFACT_REPOSITORIES_USER_SETTINGS. "
			+ "See Malaclypse documentation for more information.")
	String getUserSettings();
	void setUserSettings(String userSettings);
	
	@Description("Sets environment variable ARTIFACT_REPOSITORIES_GLOBAL_SETTINGS. "
			+ "See Malaclypse documentation for more information.")
	String getGlobalSettings();
	void setGlobalSettings(String globalSettings);
	
	@Description("Sets environment variable ARTIFACT_REPOSITORIES_EXCLUSIVE_SETTINGS. "
			+ "See Malaclypse documentation for more information.")
	String getExclusiveSettings();
	void setExclusiveSettings(String exclusiveSettings);
	

	@Description("Sets environment variable MC_CONNECTIVITY_MODE to switch into offline mode. "
			+ "See Malaclypse documentation for more information.")
	Boolean getOffline();
	void setOffline(Boolean offline);

	@Description("Sets environment variable DEVROCK_REPOSITORY_CONFIGURATION to point to a repository configuration yaml.")
	String getRepositoryConfiguration();
	void setRepositoryConfiguration(String repositoryConfiguration);
}
