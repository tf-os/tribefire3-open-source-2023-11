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
package com.braintribe.zarathud.model.forensics;

import java.util.List;
import java.util.Map;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.zarathud.model.data.Artifact;
import com.braintribe.zarathud.model.forensics.data.ArtifactReference;

/**
 * represents what Zed knows about dependencies of the terminal
 * 
 * @author pit
 *
 */
public interface DependencyForensicsResult extends ForensicsResult {

	EntityType<DependencyForensicsResult> T = EntityTypes.T(DependencyForensicsResult.class);
	
	String declarations = "declarations";
	String requiredDeclarations = "requiredDeclarations";
	String missingDeclarations = "missingDeclarations";
	String excessDeclarations = "excessDeclarations";
	String missingArtifactDetails = "missingArtifactDetails";
	String forwardedReferences = "forwardedReferences";
	String missingForwardDeclarations = "missingForwardDeclarations";
	
	/**
	 * @return - the {@link Artifact}s that were actually declared as dependencies
	 */
	List<Artifact> getDeclarations();
	void setDeclarations(List<Artifact> declaredArtifacts);

	/**
	 * @return - the {@link Artifact}s that are actually referenced in the code
	 */
	List<Artifact> getRequiredDeclarations();
	void setRequiredDeclarations(List<Artifact> actualDependencies);

	/**
	 * @return - the {@link Artifact}s required, but not declared
	 */
	List<Artifact> getMissingDeclarations();
	void setMissingDeclarations(List<Artifact> missingDependencyDeclarations);

	/**
	 * @return - the {@link Artifact}s that are in excess, i.e. declared but not required
	 */
	List<Artifact> getExcessDeclarations();
	void setExcessDeclarations(List<Artifact> excessDependencyDeclarations);

	/**
	 * @return - details in form of {@link ArtifactForensicsResult}s of the missing {@link Artifact}
	 */
	List<ArtifactForensicsResult> getMissingArtifactDetails();
	void setMissingArtifactDetails(List<ArtifactForensicsResult> missingArtifactDetails);
	
	/**
	 * @return - a {@link Map} of {@link ArtifactReference} and {@link Artifact} of foward declarations
	 */
	Map<ArtifactReference,Artifact> getForwardedReferences();
	void setForwardedReferences(Map<ArtifactReference,Artifact> value);
	
	/**
	 * @return - the {@link Artifact}s references in forward-annotations, but not declared as dependencies
	 */
	List<Artifact> getMissingForwardDeclarations();
	void setMissingForwardDeclarations(List<Artifact> missingForwardDependencyDeclarations);
	
}
