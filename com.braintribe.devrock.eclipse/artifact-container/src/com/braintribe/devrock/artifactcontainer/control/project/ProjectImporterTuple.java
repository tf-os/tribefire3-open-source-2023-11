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
package com.braintribe.devrock.artifactcontainer.control.project;

import org.eclipse.core.resources.IProject;

import com.braintribe.model.artifact.Artifact;

/**
 * @author pit
 *
 */
public class ProjectImporterTuple {

	private String projectFile;
	private Artifact artifact;
	private IProject project;

	public ProjectImporterTuple(String projectFile, Artifact artifact) {
		this.projectFile = projectFile;
		this.artifact = artifact;
	}
	
	public ProjectImporterTuple( IProject project, Artifact artifact) {
		this.project = project;
		this.artifact = artifact;	
	}

	public String getProjectFile() {
		return projectFile;
	}

	public Artifact getArtifact() {
		return artifact;
	}

	public IProject getProject() {
		return project;
	}

	public void setProject(IProject project) {
		this.project = project;
	}
	
	
	

}
