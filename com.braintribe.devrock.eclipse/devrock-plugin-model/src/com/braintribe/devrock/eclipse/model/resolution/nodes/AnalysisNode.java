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
package com.braintribe.devrock.eclipse.model.resolution.nodes;

import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.analysis.AnalysisDependency;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * common GE to be used in the transposed viewers 
 * @author pit
 *
 */
public interface AnalysisNode extends Node {
		
	EntityType<AnalysisNode> T = EntityTypes.T(AnalysisNode.class);
	
	String dependencyIdentification = "dependencyIdentification";
	String backingDependency = "backingDependency";
	String dependerIdentification = "dependerIdentification";
	String solutionIdentification = "solutionIdentification";
	String backingSolution = "backingSolution";
	String imports = "imports";
	String parentNode = "parentNode";	
	String relevantResourceOrigin = "relevantResourceOrigin";

	/**
	 * @return - the {@link VersionedArtifactIdentification} of the involved dependency (if any)
	 */
	VersionedArtifactIdentification getDependencyIdentification();
	void setDependencyIdentification(VersionedArtifactIdentification value);
	
	/**
	 * @return - the {@link AnalysisDependency} backing the node (if any)
	 */
	AnalysisDependency getBackingDependency();
	void setBackingDependency(AnalysisDependency value);

	/**
	 * @return - the {@link VersionedArtifactIdentification} of the depender 
	 */
	VersionedArtifactIdentification getDependerIdentification();
	void setDependerIdentification(VersionedArtifactIdentification value);
	
	/**
	 * @return - the {@link VersionedArtifactIdentification} of the solution (if any)
	 */
	VersionedArtifactIdentification getSolutionIdentification();
	void setSolutionIdentification(VersionedArtifactIdentification value);
	
	/**
	 * @return - the {@link AnalysisArtifact} backing the node (if any)
	 */
	AnalysisArtifact getBackingSolution();
	void setBackingSolution(AnalysisArtifact value);

	
	/**
	 * @return - imports
	 */
	AnalysisNode getImports();
	void setImports(AnalysisNode value);
 
	
	/**
	 * @return - the parent node 
	 */
	AnalysisNode getParentNode();
	void setParentNode(AnalysisNode value);
 

	/**
	 * @return - the name of repository of the relevants parts of this artifact are located (pom/jar)
	 */
	String getRelevantResourceOrigin();
	void setRelevantResourceOrigin(String value);

	
	/**
	 * @return - true if the node has been purged from the 'file system repo' and that it cannot be 
	 * purged again (it's a ghost anyway)
	 */
	boolean getIsPurged();
	void setIsPurged(boolean value);

	
	
}
