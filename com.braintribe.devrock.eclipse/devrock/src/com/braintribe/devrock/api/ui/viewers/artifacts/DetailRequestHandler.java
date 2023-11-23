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
package com.braintribe.devrock.api.ui.viewers.artifacts;

import java.util.List;

import com.braintribe.devrock.eclipse.model.resolution.nodes.AnalysisNode;
import com.braintribe.devrock.eclipse.model.resolution.nodes.Node;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;

/**
 * interface exposed by the {@link TransposedAnalysisArtifactViewer}, so that the external users 
 * can receive / handle detail requests 
 * @author pit
 *
 */
public interface DetailRequestHandler {

	/**
	 * accepts a request to show the passed {@link Node} in a separate viewer tab 
	 * @param node - the {@link Node}
	 */
	void acknowledgeOpenDetailRequest( Node node);
	
	/**
	 * accepts a request to close the viewer's tab. Currently not sent by the 
	 * viewer, as its tab can/is made to be auto-closeable
	 * @param viewer - the {@link TransposedAnalysisArtifactViewer} sending the request
	 */
	void acknowledgeCloseDetailRequest( TransposedAnalysisArtifactViewer viewer);
	
	/**
	 * accepts a request to open the {@link Node}'s attached pom file in an editor (if any)
	 * @param node - the {@link Node} to retrieve the pom from 
	 */
	void acknowledgeOpenPomRequest( Node node);

	/**
	 * accepts a request to open the {@link Node}'s attached pom file in a tab (if any)  
	 * @param node - the {@link Node} to retrieve the pom from 
	 */
	void acknowledgeViewPomRequest(Node node);
	
	
	/**
	 * accepts a request to copy the dependency related to a {@link Node} to the clipboard
	 * @param node - the {@link Node} to retrieve the {@link CompiledDependencyIdentification} from 
	 */
	void acknowledgeCopyDependencyToClipboardRequest( Node node);
	
	
	/**
	 * accepts a request to dump the currently displayed resolution as YAML file to disk
	 */
	void acknowledgeResolutionDumpRequest();
	
	
	/**
	 * accepts a request to remove a pc-versioned artifact from the install-repository 
	 * @param node - the {@link AnalysisNode} who's artifact should be removed
	 */
	void acknowledgeRemovalFromPcRepositoryRequest( List<AnalysisNode> node);
	
	/**
	 * accepts a request to identify all 'removable pc-versioned artifacts of the current resolution from the install-repository 
	 */
	void acknowledgeRemovalFromPcRepositoryRequest();
	
	/**
	 * requests whether the artifact has been marked as 'obsolete', i.e. the resolution doesn't reflect the artifact anymore 
	 * @param vai - the {@link VersionedArtifactIdentification} that identifies the artifact
	 * @return - true if it's obsolete (and should be marked accordingly), false if it's still ok
	 */
	boolean acknowledgeObsoleteCheckRequest( VersionedArtifactIdentification vai);
			
}
