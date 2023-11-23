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
package com.braintribe.devrock.artifactcontainer.container.diagnostics;

import com.braintribe.devrock.artifactcontainer.container.ArtifactContainer;
import com.braintribe.devrock.artifactcontainer.control.walk.ArtifactContainerUpdateRequestType;
import com.braintribe.model.artifact.Solution;

/**
 * listener for the {@link ContainerDiagnostics} - 
 * 
 * @author Pit
 *
 */
public interface ContainerClasspathDiagnosticsListener {
	
	/**
	 * acknowledges start of classpath processing. 
	 * @param container - the {@link ArtifactContainer} that is processed 
	 * @param mode - the {@link ArtifactContainerUpdateRequestType} of the current process 
	 */
	void acknowledgeContainerProcessingStart( ArtifactContainer container, ArtifactContainerUpdateRequestType mode);
	void acknowledgeContainerProcessingEnd( ArtifactContainer container, ArtifactContainerUpdateRequestType mode);
	
	/**
	 * acknowledges that a pom-packaged solution which is also only referenced as pom dependency
	 * is not relevant for the classpath  - this is ok, as intended
	 * @param container - the {@link ArtifactContainer} that is processed 
	 * @param requestType - the {@link ArtifactContainerUpdateRequestType} of the current process
	 * @param solution - the {@link Solution} that is not relevant
	 */
	void acknowledgeSolutionPomPackagedAndReferencedAsPom( ArtifactContainer container, ArtifactContainerUpdateRequestType requestType, Solution solution);
	/**
	 * acknowledges that a pom-packaged solution which is at least once referenced as non-pom dependency
	 * is relevant for the classpath  - this is ok, but discouraged (sloppy actually), and needs to be reported as warning
	 * @param container - the {@link ArtifactContainer} that is processed 
	 * @param requestType - the {@link ArtifactContainerUpdateRequestType} of the current process 
	 * @param solution - the {@link Solution} that is relevant
	 */ 
	void acknowledgeSolutionPomPackagedAndReferencedAsJarSolution( ArtifactContainer container, ArtifactContainerUpdateRequestType requestType, Solution solution);
	
	/**
	 * acknowledge that a non-jar packaged solution which is at least once referenced as a classes dependency 
	 * @param container - the {@link ArtifactContainer} that is processed 
	 * @param requestType - the {@link ArtifactContainerUpdateRequestType} of the current process
	 * @param solution - the {@link Solution} that is relevant
	 */
	void acknowledgeSolutionNonJarPackagedAndReferencedAsClassesJarSolution( ArtifactContainer container, ArtifactContainerUpdateRequestType requestType, Solution solution);
	
	/**
	 * acknowledges that a jar-packaged solution which is only referenced as pom dependency
	 * is not relevant for the classpath - this is not ok, and needs to be reported as a problem 
	 * @param container - the {@link ArtifactContainer} that is processed 
	 * @param requestType - the {@link ArtifactContainerUpdateRequestType} of the current process
	 * @param solution - the {@link Solution} that is irrelevant 
	 */
	void acknowledgeSolutionJarPackagedAndReferencedAsPom( ArtifactContainer container, ArtifactContainerUpdateRequestType requestType, Solution solution);
	
}
