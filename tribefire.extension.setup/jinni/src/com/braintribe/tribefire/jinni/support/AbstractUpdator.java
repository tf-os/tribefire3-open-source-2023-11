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
package com.braintribe.tribefire.jinni.support;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import com.braintribe.build.cmd.assets.api.ArtifactResolutionContext;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.devrock.mc.api.repository.configuration.RepositoryConfigurationLocaterBuilder;
import com.braintribe.devrock.mc.api.repository.configuration.RepositoryConfigurationLocator;
import com.braintribe.devrock.mc.core.configuration.RepositoryConfigurationLocators;
import com.braintribe.setup.tools.TfSetupTools;
import com.braintribe.utils.paths.UniversalPath;
import com.braintribe.ve.api.VirtualEnvironment;
import com.braintribe.ve.impl.OverridingEnvironment;

public class AbstractUpdator {
	private static final String REPOSITORY_CONFIGURATION_DEVROCK_YAML = "repository-configuration-devrock.yaml";
	
	private List<Path> repositoryConfigurationLocations = Collections.emptyList();
	protected VirtualEnvironment virtualEnvironment;

	@Required
	public void setVirtualEnvironment(VirtualEnvironment virtualEnvironment) {
		OverridingEnvironment ve = new OverridingEnvironment(virtualEnvironment);
		ve.setEnv("PROFILE_USECASE", "devrock");
		this.virtualEnvironment = ve;
	}

	
	@Configurable
	public void setAdditionalRepositoryConfigurationLocations(List<Path> repositoryConfigurationLocations) {
		this.repositoryConfigurationLocations = repositoryConfigurationLocations;
	}
	
	protected void withNewArtifactResolutionContext(Consumer<ArtifactResolutionContext> closure) {
		RepositoryConfigurationLocaterBuilder repoConfigLocatorBuilder = RepositoryConfigurationLocators.build();
		
		UniversalPath devEnvLocation = UniversalPath.start(RepositoryConfigurationLocators.FOLDERNAME_ARTIFACTS).push(REPOSITORY_CONFIGURATION_DEVROCK_YAML);
		
		repoConfigLocatorBuilder //
			.addDevEnvLocation(devEnvLocation); //

		for (Path location: repositoryConfigurationLocations) {
			repoConfigLocatorBuilder.addLocation(location.toFile());
		}
		
		repoConfigLocatorBuilder //
			.addDevEnvLocation(UniversalPath.start(RepositoryConfigurationLocators.FOLDERNAME_ARTIFACTS).push(RepositoryConfigurationLocators.FILENAME_REPOSITORY_CONFIGURATION)) //
			.addLocationEnvVariable(RepositoryConfigurationLocators.ENV_DEVROCK_REPOSITORY_CONFIGURATION) //
			.addUserDirLocation(UniversalPath.start(RepositoryConfigurationLocators.FOLDERNAME_DEVROCK).push(RepositoryConfigurationLocators.FILENAME_REPOSITORY_CONFIGURATION));

		
		RepositoryConfigurationLocator repoConfigLocator = repoConfigLocatorBuilder.done();
		
		TfSetupTools.withNewArtifactResolutionContext(repoConfigLocator, virtualEnvironment, closure);
	}
}
