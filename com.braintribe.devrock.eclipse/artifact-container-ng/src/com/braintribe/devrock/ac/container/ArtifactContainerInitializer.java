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
package com.braintribe.devrock.ac.container;

import java.io.File;
import java.util.UUID;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import com.braintribe.devrock.ac.container.plugin.ArtifactContainerPlugin;
import com.braintribe.devrock.ac.container.plugin.ArtifactContainerStatus;
import com.braintribe.devrock.mc.core.declared.DeclaredArtifactIdentificationExtractor;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;

/**
 * initializer for the container - called when Eclipse is assigning the container
 * @author pit
 *
 */
//TODO : should a check for duplicates happen? Can there be duplicates?  
public class ArtifactContainerInitializer extends ClasspathContainerInitializer {
	private static Logger log = Logger.getLogger(ArtifactContainerInitializer.class);

	@Override
	public void initialize(IPath iPath, IJavaProject iJavaProject) throws CoreException {	
		
		ArtifactContainer container = new ArtifactContainer( iPath, iJavaProject, UUID.randomUUID().toString());
		
		IProject project = iJavaProject.getProject();
		File projectDir = project.getLocation().toFile();
		File pomFile = new File(projectDir, "pom.xml");
		if (!pomFile.exists()) {
			log.info("project [" + project.getName() + "] has no associated pom in [" + projectDir.getAbsolutePath() + "] and is not a candidate for a container");
			return;
		}
		Maybe<CompiledArtifactIdentification> extractedIdentificationPotential = DeclaredArtifactIdentificationExtractor.extractIdentification(pomFile);
		
	
		if (extractedIdentificationPotential.isUnsatisfied()) {
			ArtifactContainerStatus status = new ArtifactContainerStatus(" project [" + project.getName() + "] isn't a suitable candidate for a container : cannot read pom [" + pomFile.getAbsolutePath() + "] associated with project [" + project.getName() + "] as [" + extractedIdentificationPotential.whyUnsatisfied().stringify() + "]", IStatus.WARNING);
			ArtifactContainerPlugin.instance().log(status);
			return;
		}
		CompiledArtifactIdentification cai = extractedIdentificationPotential.get();							
		container.setVersionedArtifactIdentification( VersionedArtifactIdentification.create( cai.getGroupId(), cai.getArtifactId(), cai.getVersion().asString()));
		
	
		
		JavaCore.setClasspathContainer(iPath, new IJavaProject[] {iJavaProject}, new IClasspathContainer[] {container}, null);
				
	}

}
