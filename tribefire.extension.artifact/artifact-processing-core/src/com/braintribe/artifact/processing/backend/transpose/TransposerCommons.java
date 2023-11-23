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
package com.braintribe.artifact.processing.backend.transpose;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.List;

import com.braintribe.build.artifact.retrieval.multi.repository.reflection.ArtifactReflectionExpert;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.RepositoryReflection;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.PartTuple;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.info.RepositoryOrigin;
import com.braintribe.model.artifact.info.VersionInfo;
import com.braintribe.model.artifact.processing.ResolvedArtifactPart;

public class TransposerCommons {
	private static Logger log = Logger.getLogger(TransposerCommons.class);
	protected RepositoryReflection repositoryReflection;
	
	@Configurable @Required
	public void setRepositoryReflection(RepositoryReflection repositoryReflection) {
		this.repositoryReflection = repositoryReflection;
	}
	
	/**
	 * acquire the information about the repositories that contain the solution. Local repo has its URL cleaned
	 * @param solution - the {@link Solution} to retrieve the information
	 * @return - a {@link List} of {@link RepositoryOrigin}
	 */
	protected List<RepositoryOrigin> getRepositoryOrigins( Solution solution) {
		ArtifactReflectionExpert artifactReflectionExpert = repositoryReflection.acquireArtifactReflectionExpert(solution);		
		VersionInfo versionOrigin = artifactReflectionExpert.getVersionOrigin(solution.getVersion(), null);
		if (versionOrigin ==  null)
			return Collections.emptyList();
		// clean URL of local repository  
		List<RepositoryOrigin> repositoryOrigins = versionOrigin.getRepositoryOrigins();
		for (RepositoryOrigin origin : repositoryOrigins) {
			// 
			if (origin.getName()!= null && origin.getName().equalsIgnoreCase("local")) {
				origin.setUrl( null);
				break;
			}
		}
		
		return repositoryOrigins;
		
	}
	
	/**
	 * transpose a {@link Part} into a {@link ResolvedArtifactPart}
	 * @param part - the {@link Part} to transpose
	 * @return - the {@link ResolvedArtifactPart} as a result
	 */
	protected ResolvedArtifactPart transpose(Part part) {
		
		ResolvedArtifactPart resolvedPart = ResolvedArtifactPart.T.create();
		
		String location = part.getLocation();
		PartTuple tuple = part.getType();
		
		resolvedPart.setClassifier( tuple.getClassifier());
		resolvedPart.setType( tuple.getType());
		
		try {
			String url = new File( location).toURI().toURL().toString();
			resolvedPart.setUrl(url);
		} catch (MalformedURLException e) {
			log.error( "cannot turn part's location [" + location + "] to an valid URL", e);
		}
				
		return resolvedPart;
	}
}
