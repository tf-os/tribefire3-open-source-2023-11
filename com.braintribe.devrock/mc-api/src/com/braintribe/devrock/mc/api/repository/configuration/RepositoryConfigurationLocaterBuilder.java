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
package com.braintribe.devrock.mc.api.repository.configuration;

import java.io.File;

import com.braintribe.utils.paths.UniversalPath;

public interface RepositoryConfigurationLocaterBuilder {
	RepositoryConfigurationLocaterBuilder addLocation(File file);
	RepositoryConfigurationLocator  addRequiredLocation(File file);
	RepositoryConfigurationLocaterBuilder addLocationEnvVariable(String envVar);
	RepositoryConfigurationLocaterBuilder addDevEnvLocation(UniversalPath path);
	RepositoryConfigurationLocaterBuilder addUserDirLocation(UniversalPath path);
	RepositoryConfigurationLocaterBuilder add(RepositoryConfigurationLocator locator);
	RepositoryConfigurationLocaterBuilder collectorReasonMessage(String text); 
	RepositoryConfigurationLocator done();
}
