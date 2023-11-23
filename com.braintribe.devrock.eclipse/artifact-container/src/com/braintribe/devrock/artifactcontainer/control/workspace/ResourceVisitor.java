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
package com.braintribe.devrock.artifactcontainer.control.workspace;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;

import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerStatus;
import com.braintribe.devrock.artifactcontainer.container.ArtifactContainer;
import com.braintribe.devrock.artifactcontainer.control.walk.ArtifactContainerUpdateRequestType;
import com.braintribe.devrock.artifactcontainer.control.walk.wired.WiredArtifactContainerWalkController;
import com.braintribe.logging.Logger;
import com.braintribe.plugin.commons.container.ContainerCommons;

/**
 * @author pit
 *
 */
public class ResourceVisitor implements IResourceDeltaVisitor {
	private static Logger log = Logger.getLogger(ResourceVisitor.class);

	@Override
	public boolean visit(IResourceDelta delta) throws CoreException {
		IResource resource = delta.getResource();
		if (resource == null)
			return true;
			
		String resourceName = resource.getName();
		IProject project = resource.getProject();

		// workspace project controller probably needs to be updated ..
		if (resourceName.equalsIgnoreCase( ".project") || resourceName.equalsIgnoreCase("pom.xml")) {
			if (project != null) {			
				ArtifactContainerPlugin.getWorkspaceProjectRegistry().update( project);
			}
			else {
				ArtifactContainerPlugin.getWorkspaceProjectRegistry().update();
			}
		}
		// standard containers listen to pom
		if (resourceName.equalsIgnoreCase( "pom.xml") ) {
			
			if (project.isAccessible() == false)
				return true;
			File prjDirectory = project.getLocation().toFile();
			File resourceFile = resource.getLocation().toFile();
			if (!resourceFile.getParent().equals( prjDirectory.getAbsolutePath())) {
				//System.out.println("found fake resource [" + resourceFile.getAbsolutePath());
				return true;
			}			
			
			ArtifactContainer container = ArtifactContainerPlugin.getArtifactContainerRegistry().getContainerOfProject(project);
			if (container != null) {
				// get MD5 of pom 
				String md5 = ContainerCommons.getMd5ofResource(resource);
				String containerMd5 = container.getMd5();
				if (!md5.equalsIgnoreCase( containerMd5)) {
					/*
					String msg = "container [" + container.getId() + "], project [" + container.getProject().getProject().getName() + "], hash [" + containerMd5 + "], resource [" + md5 + "] : mismatch";					
					ArtifactContainerPlugin.log(msg);				
					*/
					
					List<ArtifactContainer> containers = new ArrayList<>();
					containers.add(container);
					
					// add dependers
					if (ArtifactContainerPlugin.getInstance().getArtifactContainerPreferences(false).getDynamicContainerPreferences().getChainArtifactSync()) {
						List<ArtifactContainer> dependerContainers = ArtifactContainerPlugin.getWorkspaceProjectRegistry().getDependerContainers( container);
						if (dependerContainers.size() > 0) {
							containers.addAll( dependerContainers);
							String synchedProjects = dependerContainers.stream().map( c -> {
								return c.getProject().getProject().getName();
							}).collect( Collectors.joining( ","));
							String msg = "adding [" + synchedProjects + "] depender containers as they are dependers of [" + project.getName() + "] whose pom changed";
							log.debug(msg);
							ArtifactContainerPlugin.log(msg);
							ArtifactContainerStatus status = new ArtifactContainerStatus( msg, IStatus.INFO);
							ArtifactContainerPlugin.getInstance().log(status);		
						}
					}
					
					
					WiredArtifactContainerWalkController.getInstance().updateContainers(containers, ArtifactContainerUpdateRequestType.combined);
					// workspace registry still has cached version information, so trigger an update
					ArtifactContainerPlugin.getWorkspaceProjectRegistry().update( project);
					
				}
				/*
				else {
					
					String msg = "container [" + container.getId() + "], project [" + container.getProject().getProject().getName() + "], hash [" + containerMd5 + "], resource [" + md5 + "] : match";
					System.out.println(msg);
					ArtifactContainerPlugin.log(msg);
					//WiredArtifactContainerWalkController.getInstance().updateContainer(container, ArtifactContainerUpdateRequestType.refresh);
				}
				*/
				
			}
		}				
								
		// 
		//continue visiting.. 
		return true;
	}

}
