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
package com.braintribe.devrock.api.ui.viewers.artifacts.transpose.transposer;

import java.util.Comparator;

import com.braintribe.devrock.eclipse.model.resolution.nodes.AnalysisNode;
import com.braintribe.devrock.eclipse.model.resolution.nodes.DependerNode;
import com.braintribe.devrock.eclipse.model.resolution.nodes.Node;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;

/**
 * divers comparators for the transposer
 * @author pit
 *
 */
public class TranspositionCommons {

	public static DependerNodeComparator dependencyNodeComparator = new DependerNodeComparator();
	public static AnalysisNodeComparator analysisNodeComparator = new AnalysisNodeComparator();
	public static NodeComparator nodeComparator = new NodeComparator();
	
	/**
	 * compares {@link DependerNode} based on the {@link VersionedArtifactIdentification} comparator
	 * @author pit
	 *
	 */
	public static class DependerNodeComparator implements Comparator<DependerNode> {		
		@Override
		public int compare(DependerNode o1, DependerNode o2) {				
			return o1.getDependerArtifact().compareTo( o2.getDependerArtifact());
		}					
	}
	
	/**
	 * compares {@link AnalysisNode} based on either the {@link VersionedArtifactIdentification}s of the dependency ID or the artifact ID
	 * @author pit
	 *
	 */
	public static class AnalysisNodeComparator implements Comparator<AnalysisNode> {		
		@Override
		public int compare(AnalysisNode o1, AnalysisNode o2) {
			if (o1.getDependencyIdentification() != null) {
				o1.getDependencyIdentification().compareTo( o2.getDependencyIdentification());
			}
			else {				
				return o1.getSolutionIdentification().compareTo( o2.getSolutionIdentification());
			}
			return 0;
		}					
	}

	/**
	 * common comparator for {@link Node}, decides which comparison to make depending on first instance,
	 * either {@link AnalysisNodeComparator} or {@link DependerNodeComparator}
	 * @author pit
	 *
	 */
	public static class NodeComparator implements Comparator<Node> {		
		@Override
		public int compare(Node o1, Node o2) {
			if (o1 instanceof AnalysisNode) {
				return analysisNodeComparator.compare( (AnalysisNode) o1, (AnalysisNode) o2);
			}
			else if (o1 instanceof DependerNode) {
				return dependencyNodeComparator.compare( (DependerNode) o1, (DependerNode) o2);
			}						
			return 0;
		}
	}
		 	
}
