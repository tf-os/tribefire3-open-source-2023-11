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
package com.braintribe.devrock.artifactcontainer.plugin.malaclypse.scope.overrides;

import com.braintribe.build.artifact.representations.RepresentationException;
import com.braintribe.build.artifact.representations.artifact.maven.settings.LocalRepositoryLocationProvider;
import com.braintribe.build.artifact.virtualenvironment.VirtualPropertyResolver;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.model.malaclypse.cfg.preferences.mv.MavenPreferences;


/**
 * an implementation of the {@link LocalRepositoryLocationProvider} that heads the override from the preferences, 
 * and the the {@link VirtualPropertyResolver}
 * @author pit
 *
 */
public class OverrideLocalRepositoryRetrievalExpert implements LocalRepositoryLocationProvider {	
	private VirtualPropertyResolver propertyResolver;
	private ArtifactContainerPlugin plugin = ArtifactContainerPlugin.getInstance();
	
	@Configurable  @Required
	public void setPropertyResolver(VirtualPropertyResolver propertyResolver) {
		this.propertyResolver = propertyResolver;
	}
	
	@Override
	public String getLocalRepository(String expression) throws RepresentationException {
		if (expression == null) {
			expression = "${env.M2_REPO}";
		}
		MavenPreferences preferences = plugin.getArtifactContainerPreferences(false).getMavenPreferences();
		String override = preferences.getLocalRepositoryOverride();
		if (override != null) {
			return propertyResolver.resolve(override);
		}	
		return propertyResolver.resolve(expression);
	}

}
