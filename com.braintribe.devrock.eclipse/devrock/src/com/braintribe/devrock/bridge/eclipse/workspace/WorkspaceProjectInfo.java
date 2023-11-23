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
package com.braintribe.devrock.bridge.eclipse.workspace;

import java.io.File;

import org.eclipse.core.resources.IProject;

import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;

public interface WorkspaceProjectInfo {

	/**
	 * @return - the {@link IProject} of the project
	 */
	IProject getProject();

	/**
	 * @return - the project's artifact identification as a {@link CompiledArtifactIdentification}
	 */
	CompiledArtifactIdentification getCompiledArtifactIdentification();
	
	/**
	 * @return - the project's artifact identification as a {@link VersionedArtifactIdentification}
	 */
	VersionedArtifactIdentification getVersionedArtifactIdentification();

	/**
	 * @return - the main folder of the project, i.e. where the .project file lies
	 */
	File getProjectFolder();
	
	/**
	 * @return - the folder that the K_SOURCE package fragment points to 
	 */
	File getSourceFolder();
	
	/**
	 * @return - the folder that stands as output folder for the project
	 */
	File getBinariesFolder();

}
