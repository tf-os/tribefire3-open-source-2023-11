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
package com.braintribe.devrock.eclipse.model.resolution;

import java.util.List;

import com.braintribe.devrock.eclipse.model.resolution.nodes.Node;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.analysis.AnalysisDependency;
import com.braintribe.model.artifact.analysis.AnalysisTerminal;
import com.braintribe.model.artifact.compiled.CompiledTerminal;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;


/**
 * a model that contains a transcribed {@link AnalysisArtifactResolution}
 * @author pit
 *
 */
public interface AnalysisArtifactResolutionViewerModel extends GenericEntity {
	
	String terminals = "terminals";
	
	EntityType<AnalysisArtifactResolutionViewerModel> T = EntityTypes.T(AnalysisArtifactResolutionViewerModel.class);

	/**
	 * @return - a list of the  {@link CompiledTerminal} / {@link AnalysisTerminal} as {@link AnalysisNode}
	 */
	List<Node> getTerminals();
	void setTerminals(List<Node>  Terminals);
	
	/**
	 * @return - a list of the {@link AnalysisArtifact} in the 'solutions' list of the {@link AnalysisArtifactResolution}, as {@link Node}
	 */
	List<Node> getSolutions();
	void setSolutions(List<Node> solutions);
	
	/**
	 * @return - a {@link List} of ALL involved {@link AnalysisArtifact}, inclusive parents/imports,  as {@link Node}
	 */
	List<Node> getPopulation();
	void setPopulation(List<Node> value);

	/**
	 * @return - a {@link List} of clashes, as {@link Node}
	 */
	List<Node> getClashes();
	void setClashes(List<Node> value);
	
	/**
	 * @return - a {@link List} of the {@link AnalysisArtifact} if the 'incomplete artifacts' list of the {@link AnalysisArtifactResolution}
	 */
	List<Node> getIncompleteArtifacts();
	void setIncompleteArtifacts(List<Node> value);
	
	/**
	 * @return - a {@link List} of the {@link AnalysisDependency} if the 'unresolved dependencies' list of the {@link AnalysisArtifactResolution}
	 */
	List<Node> getUnresolvedDependencies();
	void setUnresolvedDependencies(List<Node> value);
	
	/**
	 * @return - a {@link List} of the {@link AnalysisDependency} if the 'filtered artifacts' list of the {@link AnalysisArtifactResolution}
	 */
	List<Node> getFilteredDependencies();
	void setFilteredDependencies(List<Node> value);
	
	
	/**
	 * @return
	 */
	List<Node> getParents();
	void setParents(List<Node> value);

		
}


