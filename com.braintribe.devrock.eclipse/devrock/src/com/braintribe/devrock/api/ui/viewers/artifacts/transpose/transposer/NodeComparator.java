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
import com.braintribe.devrock.eclipse.model.resolution.nodes.Node;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;

public class NodeComparator implements Comparator<Node> {
	@Override
	public int compare(Node o1, Node o2) {
		if (o1 instanceof AnalysisNode && o2 instanceof AnalysisNode) {
			// compare on artifactId, make sure they're reachable
			
			// first node
			AnalysisNode a1 = (AnalysisNode) o1;										
			VersionedArtifactIdentification identification1 = a1.getSolutionIdentification();
			if (identification1 == null) {
				return 0;
			}
			String aa1 = identification1.getArtifactId();
			if (aa1 == null) {
				return 0;
			}
			// second node
			AnalysisNode a2 = (AnalysisNode) o2;
			VersionedArtifactIdentification identification2 = a2.getSolutionIdentification();
			if (identification2 == null) {
				return 0;
			}
			String aa2 = identification2.getArtifactId();
			if (aa2 == null) {
				return 0;
			}
			// compare artifactId 
			return aa1.compareTo(aa2);
		}
		return 0;
	}
}