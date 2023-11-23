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

public interface DependerNode extends Node {
	
	String dependerArtifact = "dependerArtifact";
	String backingArtifact = "backingArtifact";
	String dependency = "dependency";
	String backingDependency = "backingDependency";
	String isParentDepender = "isParentDepender";
	String isTerminal = "isTerminal";
	
	EntityType<DependerNode> T = EntityTypes.T(DependerNode.class);

	
	VersionedArtifactIdentification getDependerArtifact();
	void setDependerArtifact(VersionedArtifactIdentification value);
	
	AnalysisArtifact getBackingArtifact();
	void setBackingArtifact(AnalysisArtifact value);


	VersionedArtifactIdentification getDependency();
	void setDependency(VersionedArtifactIdentification value);
	
	AnalysisDependency getBackingDependency();
	void setBackingDependency(AnalysisDependency value);
 
	boolean getIsParentDepender();
	void setIsParentDepender(boolean value);

	boolean getIsTerminal();
	void setIsTerminal(boolean value);


}
