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
package com.braintribe.devrock.zarathud.model.response;

import com.braintribe.devrock.zarathud.model.request.AnalyzeArtifact;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.resource.Resource;

/**
 * zed's response to an {@link AnalyzeArtifact} request
 * @author pit
 *
 */
public interface AnalyzedArtifact extends ZedResponse {
	
	
	final EntityType<AnalyzedArtifact> T = EntityTypes.T(AnalyzedArtifact.class);

	/**
	 * @return - a resource containing the fingerprints (expressive marshaller)
	 */
	Resource getFingerPrints();
	void setFingerPrints(Resource fingerPrints);
	
	/**
	 * @return - a resource containing the extraction (yaml)
	 */
	Resource getExtraction();
	void setExtraction(Resource extration);
	
	/**
	 * @return - a resource containing the dependency forensics result (yaml)
	 */
	Resource getDependencyForensics();
	void setDependencyForensics(Resource dependencyForensics);
	
	/**
	 * @return - a resource containing the classpath forensics (yaml)
 	 */
	Resource getClasspathForensics();
	void setClasspathForensics(Resource classpathForensics);
	
	/**
	 * @return - a resource containing the model forensics (yaml)
	 */
	Resource getModelForensics();
	void setModelForensics(Resource modelForensics);
}
