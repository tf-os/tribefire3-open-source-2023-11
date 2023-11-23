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
 * represents a 'declaration' relation of a {@link AnalysisDependency} and it's declaring {@link AnalysisArtifact},
 * used for the case of parent-inherited dependencies and dept-mgt'd (at least imported) dependencies
 *  
 * @author pit
 *
 */
public interface DeclaratorNode extends Node {
	
	EntityType<DeclaratorNode> T = EntityTypes.T(DeclaratorNode.class);
	
	String declaratorArtifact = "declaratorArtifact";
	String backingDeclaratorArtifact = "backingDeclaratorArtifact";
	String dependingDependency = "dependingDependency";
	String backingDependingDependency = "backingDependingDependency";

	/**
	 * @return the {@link VersionedArtifactIdentification} of the declaring artifact (parent or import)
	 */
	VersionedArtifactIdentification getDeclaratorArtifact();
	void setDeclaratorArtifact(VersionedArtifactIdentification value);
	
	/**
	 * @return - the declaring {@link AnalysisArtifact} 
	 */
	AnalysisArtifact getBackingDeclaratorArtifact();
	void setBackingDeclaratorArtifact(AnalysisArtifact value);


	/**
	 * @return - the {@link VersionedArtifactIdentification} of the depender, a dependency 
	 */
	VersionedArtifactIdentification getDependingDependency();
	void setDependingDependency(VersionedArtifactIdentification value);
	
	/**
	 * @return - the depender {@link AnalysisDependency} 
	 */
	AnalysisDependency getBackingDependingDependency();
	void setBackingDependingDependency(AnalysisDependency value);

}
