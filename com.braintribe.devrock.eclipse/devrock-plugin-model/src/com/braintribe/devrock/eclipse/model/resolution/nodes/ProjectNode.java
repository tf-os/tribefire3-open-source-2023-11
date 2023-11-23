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
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * represents a project that AC has taken instead of the actual dependency 
 * @author pit
 *
 */
public interface ProjectNode extends Node {
	
	EntityType<ProjectNode> T = EntityTypes.T(ProjectNode.class);
	
	String name = "name";
	String osLocation = "osLocation";
	String replacedSolution = "replacedSolution";

	/**
	 * @return - the name of the project
	 */
	String getName();
	void setName(String value);
	
	/**
	 * @return - the location of the project in the filesystem
	 */
	String getOsLocation();
	void setOsLocation(String value);

	
	/**
	 * @return - the {@link AnalysisArtifact} that the project stands for
	 */
	AnalysisArtifact getReplacedSolution();
	void setReplacedSolution(AnalysisArtifact value);

	

}
