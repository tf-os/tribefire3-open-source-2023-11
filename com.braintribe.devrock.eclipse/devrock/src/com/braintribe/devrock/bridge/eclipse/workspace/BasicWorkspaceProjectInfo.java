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
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import com.braintribe.devrock.api.nature.CommonNatureIds;
import com.braintribe.devrock.api.nature.NatureHelper;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.utils.lcd.LazyInitialized;

/**
 * a structure to hold information about a project in the workspace
 * @author pit
 *
 */
public class BasicWorkspaceProjectInfo implements WorkspaceProjectInfo {
	private static Logger log = Logger.getLogger(BasicWorkspaceProjectInfo.class);
	private IProject  project;
	private VersionedArtifactIdentification versionedArtifactIdentification;
	
	private LazyInitialized<IJavaProject> javaProject = new LazyInitialized<>(this::getJavaProject);

	/**
	 * @return - the {@link IProject} of the project
	 */
	@Override
	public IProject getProject() {
		return project;
	}
	public void setProject(IProject project) {
		this.project = project;
	}
	
	@Override
	public File getProjectFolder() {
		return project.getLocation().toFile();
	}
	/**
	 * @return - the {@link VersionedArtifactIdentification}
	 */
	public VersionedArtifactIdentification getVersionedArtifactIdentification() {
		return versionedArtifactIdentification;
	}
	public void setVersionedArtifactIdentification(VersionedArtifactIdentification versionedArtifactIdentification) {
		this.versionedArtifactIdentification = versionedArtifactIdentification;
	}
		
	@Override
	public CompiledArtifactIdentification getCompiledArtifactIdentification() {
		return CompiledArtifactIdentification.from(versionedArtifactIdentification);
	}
	@Override
	public File getSourceFolder() {
		IJavaProject iJavaProject = javaProject.get();
		if (iJavaProject == null) 
			return null;		
		
	
		try {
			IClasspathEntry [] rawEntries = iJavaProject.getRawClasspath();			
			if (rawEntries == null)
				return null;
			for (IClasspathEntry entry : rawEntries) {
				if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
					IPath outputLocation = entry.getPath();
					File outputLocationFile = ResourcesPlugin.getWorkspace().getRoot().getFile(outputLocation).getRawLocation().toFile();
					return outputLocationFile;					
				}
			}
		} catch (JavaModelException e) {
			log.error("cannot extract project [" + project.getName() + "]'s source folder", e);	
		}
		return null;
	
	}
	@Override
	public File getBinariesFolder() {
		try {
			IJavaProject iJavaProject = javaProject.get();
			if (iJavaProject == null) 
				return null;					
			File outputLocationFile = ResourcesPlugin.getWorkspace().getRoot().getFile( iJavaProject.getOutputLocation()).getRawLocation().toFile();
			return outputLocationFile;								
		} catch (JavaModelException e) {
			log.error("cannot extract project [" + project.getName() + "]'s output folder", e);
			return null;
		}		
	}
	
	/**
	 * @param project
	 * @return
	 */
	private IJavaProject getJavaProject() {
		if (NatureHelper.hasAnyNatureOf(project, CommonNatureIds.NATURE_JAVA)) {
			return JavaCore.create(project);
		}
		return null;
	}
	
}
