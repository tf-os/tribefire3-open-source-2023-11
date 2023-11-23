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
package com.braintribe.artifact.processing.core.test.walk;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.Assert;

import com.braintribe.artifact.processing.ArtifactProcessingCoreExpert;
import com.braintribe.artifact.processing.core.test.AbstractArtifactProcessingLab;
import com.braintribe.artifact.processing.core.test.Commons;
import com.braintribe.model.artifact.processing.ArtifactResolution;
import com.braintribe.model.artifact.processing.AssetFilterContext;
import com.braintribe.model.artifact.processing.HasArtifactIdentification;
import com.braintribe.model.artifact.processing.PlatformAssetResolution;
import com.braintribe.model.artifact.processing.ResolvedArtifact;
import com.braintribe.model.artifact.processing.cfg.repository.RepositoryConfiguration;
import com.braintribe.model.artifact.processing.cfg.resolution.FilterScope;
import com.braintribe.model.artifact.processing.cfg.resolution.ResolutionConfiguration;
import com.braintribe.model.artifact.processing.cfg.resolution.ResolutionScope;
import com.braintribe.model.artifact.processing.service.data.ResolvedPlatformAssets;
import com.braintribe.model.artifact.processing.service.request.ResolvePlatformAssets;

public abstract class AbstractDependenciesLab extends AbstractArtifactProcessingLab {
	
	
	protected ArtifactResolution resolvedDependencies(File localRepository, HasArtifactIdentification hasArtifactIdentification, RepositoryConfiguration scopeConfiguration, ResolutionConfiguration walkConfiguration) {		
		return ArtifactProcessingCoreExpert.getArtifactResolution( localRepository, hasArtifactIdentification, scopeConfiguration, walkConfiguration);
	}
	
	protected PlatformAssetResolution resolvedAssets(File localRepository, HasArtifactIdentification hasArtifactIdentification, RepositoryConfiguration scopeConfiguration, AssetFilterContext context) {		
		return ArtifactProcessingCoreExpert.getAssetResolution(localRepository, hasArtifactIdentification, scopeConfiguration, context);
	}
	
	protected ResolvedPlatformAssets resolvedAssets(File localRepository, ResolvePlatformAssets pas, RepositoryConfiguration scopeConfiguration, AssetFilterContext context) {		
		return ArtifactProcessingCoreExpert.resolvePlatformAssets(localRepository, pas, scopeConfiguration, context);
	}
	
	
	protected void validateResult( List<ResolvedArtifact> found, List<ResolvedArtifact> expected) {
		List<ResolvedArtifact> unexpected = new ArrayList<>();
		List<ResolvedArtifact> matched = new ArrayList<>();
		for (ResolvedArtifact ra : found) {			
			boolean foundMatch = false;
			for (ResolvedArtifact rb : expected) {
				if (Commons.compare(ra, rb)) {
					foundMatch = true;
					matched.add(rb);					
				}	
			}		
			if (!foundMatch) {
				unexpected.add(ra);
			}
		}
		
		if (unexpected.size() > 0) {
			// dump			
			Assert.fail( "unexpected : [" + Commons.rsToString( unexpected) + "]");
		}
		if (matched.size() != expected.size()) {
			List<ResolvedArtifact> temp = new ArrayList<>( expected);
			temp.removeAll( matched);
			Assert.fail( "not found [" + Commons.rsToString( temp));
		}		
	}
	
	/**
	 * @param resolutionScope
	 * @param skipOptionals
	 * @param filterScopes
	 * @return
	 */
	protected ResolutionConfiguration buildWalkConfiguration( ResolutionScope resolutionScope, boolean skipOptionals, FilterScope ...filterScopes) {
		ResolutionConfiguration walkConfiguration = ResolutionConfiguration.T.create();		
		walkConfiguration.setResolutionScope(resolutionScope);
		if (filterScopes != null) {
			walkConfiguration.setFilterScopes( new HashSet<>(Arrays.asList(filterScopes)));
		}
		walkConfiguration.setIncludeOptionals( !skipOptionals);		
		return walkConfiguration;
	}
}
