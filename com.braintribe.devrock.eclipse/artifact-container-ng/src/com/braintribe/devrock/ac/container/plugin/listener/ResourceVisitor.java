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
package com.braintribe.devrock.ac.container.plugin.listener;

import java.io.File;
import java.util.Collections;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;

import com.braintribe.devrock.ac.container.plugin.ArtifactContainerPlugin;
import com.braintribe.devrock.ac.container.plugin.ArtifactContainerStatus;
import com.braintribe.devrock.ac.container.updater.ProjectUpdater;
import com.braintribe.devrock.ac.container.updater.ProjectUpdater.Mode;
import com.braintribe.logging.Logger;

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
				//ArtifactContainerPlugin.getWorkspaceProjectRegistry().update( project);
			}
			else {
				//ArtifactContainerPlugin.getWorkspaceProjectRegistry().update();
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
			// perhaps calculate MD5 as in old mc and if changed, trigger and update?
			ProjectUpdater updater = new ProjectUpdater( Mode.pom);
			try {
				updater.setSelectedProjects( Collections.singleton( project));
				updater.runAsJob();
			} catch (Exception e) {
				String msg = "cannot react on changes in [" + resource.getFullPath().toOSString() + "]";
				log.error( msg, e);
				ArtifactContainerStatus status = new ArtifactContainerStatus( msg, e);
				ArtifactContainerPlugin.instance().log(status);
			} 
						
		}				
								
		// 
		//continue visiting.. 
		return true;
	}

}
