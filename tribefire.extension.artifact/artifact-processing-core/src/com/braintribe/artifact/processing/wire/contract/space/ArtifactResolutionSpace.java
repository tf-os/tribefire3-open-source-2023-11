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
package com.braintribe.artifact.processing.wire.contract.space;

import com.braintribe.artifact.processing.backend.ArtifactProcessingWalkConfigurationExpert;
import com.braintribe.artifact.processing.wire.contract.ArtifactIdentificationContract;
import com.braintribe.artifact.processing.wire.contract.ArtifactResolutionContract;
import com.braintribe.artifact.processing.wire.contract.RepositoryConfigurationContract;
import com.braintribe.artifact.processing.wire.contract.ResolutionConfigurationContract;
import com.braintribe.artifact.processing.wire.contract.exp.ArtifactResolver;
import com.braintribe.artifact.processing.wire.contract.exp.ArtifactResolverImpl;
import com.braintribe.build.artifact.walk.multi.Walker;
import com.braintribe.build.artifacts.mc.wire.classwalk.context.WalkerContext;
import com.braintribe.build.artifacts.mc.wire.classwalk.contract.ClasspathResolverContract;
import com.braintribe.model.artifact.processing.cfg.resolution.ResolutionConfiguration;
import com.braintribe.wire.api.annotation.Import;

public class ArtifactResolutionSpace implements ArtifactResolutionContract {
	
	@Import
	RepositoryConfigurationContract repositoryConfigurationContract;
	
	@Import
	ResolutionConfigurationContract resolutionConfigurationContract;
	
	@Import
	ArtifactIdentificationContract artifactIdentificationContract;
	
	@Import 
	ClasspathResolverContract classpathResolverContract;

	@Override
	public ArtifactResolver artifactResolver() {
		ArtifactResolverImpl bean = new ArtifactResolverImpl();
		bean.setArtifact( artifactIdentificationContract.artifactIdentification());
		
		bean.setDependencyResolver( classpathResolverContract.dependencyResolver());
		bean.setRepositoryReflection( classpathResolverContract.repositoryReflection());
		
		ResolutionConfiguration resolutionConfiguration = resolutionConfigurationContract.resolutionConfiguration();
		WalkerContext walkerContext = ArtifactProcessingWalkConfigurationExpert.acquireWalkerContext( resolutionConfiguration);		
		bean.setEnricher( classpathResolverContract.contextualizedEnricher(walkerContext));
		Walker walker = classpathResolverContract.walker( walkerContext);
		bean.setWalker(walker);
		if (resolutionConfiguration != null) {
			bean.setSortOrder( resolutionConfiguration.getSortOrder());
		}
		return bean;
	}
	
	

}
