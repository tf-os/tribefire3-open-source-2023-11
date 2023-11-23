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
package com.braintribe.artifact.processing.wire.contract.exp;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

import com.braintribe.artifact.processing.ArtifactProcessingCoreCommons;
import com.braintribe.artifact.processing.backend.transpose.ArtifactProcessingModelTransposer;
import com.braintribe.build.artifact.retrieval.multi.enriching.MultiRepositorySolutionEnricher;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.RepositoryReflection;
import com.braintribe.build.artifact.retrieval.multi.resolving.DependencyResolver;
import com.braintribe.build.artifact.walk.multi.Walker;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.ArtifactIdentification;
import com.braintribe.model.artifact.processing.ArtifactResolution;
import com.braintribe.model.artifact.processing.cfg.resolution.ResolutionSortOrder;

public class ArtifactResolverImpl implements ArtifactResolver {
	//private RepositoryConfiguration scopeConfiguration;
	private ArtifactIdentification artifact;
	//private ResolutionConfiguration walkConfiguration;
	private DependencyResolver dependencyResolver;
	private Walker walker;
	private MultiRepositorySolutionEnricher enricher;
	private RepositoryReflection repositoryReflection;
	private ResolutionSortOrder sortOrder;
	
	

	public ArtifactIdentification getArtifact() {
		return artifact;
	}
	public void setArtifact(ArtifactIdentification artifact) {
		this.artifact = artifact;
	}

	public DependencyResolver getDependencyResolver() {
		return dependencyResolver;
	}
	public void setDependencyResolver(DependencyResolver dependencyResolver) {
		this.dependencyResolver = dependencyResolver;
	}


	public Walker getWalker() {
		return walker;
	}
	public void setWalker(Walker walker) {
		this.walker = walker;
	}

	public MultiRepositorySolutionEnricher getEnricher() {
		return enricher;
	}
	public void setEnricher(MultiRepositorySolutionEnricher enricher) {
		this.enricher = enricher;
	}


	public RepositoryReflection getRepositoryReflection() {
		return repositoryReflection;
	}
	public void setRepositoryReflection(RepositoryReflection repositoryReflection) {
		this.repositoryReflection = repositoryReflection;
	}

	public ResolutionSortOrder getSortOrder() {
		return sortOrder;
	}
	public void setSortOrder(ResolutionSortOrder sortOrder) {
		this.sortOrder = sortOrder;
	}
	
	
	@Override
	public ArtifactResolution resolve() {
		//WireContext<ClasspathResolverContract> context = classpathResolverContract( scopeConfiguration);

		Solution solution = ArtifactProcessingCoreCommons.determineSolution( artifact, dependencyResolver);
		/*
		WalkerContext walkerContext = ArtifactProcessingWalkConfigurationExpert.acquireWalkerContext(walkConfiguration);		
		Walker walker = context.contract().walker( walkerContext);
		*/
				
		String walkScopeId = UUID.randomUUID().toString();
		Collection<Solution> collectedSolutions = walker.walk( walkScopeId, solution);
		enricher.enrich( walkScopeId, Collections.singleton( solution));
		/*
		ResolutionSortOrder sortOrder = null;
		if (walkConfiguration != null) {
			sortOrder = walkConfiguration.getSortOrder();
		}
		*/
		
		return ArtifactProcessingModelTransposer.transpose( repositoryReflection, solution, collectedSolutions, sortOrder);	
	}

}
