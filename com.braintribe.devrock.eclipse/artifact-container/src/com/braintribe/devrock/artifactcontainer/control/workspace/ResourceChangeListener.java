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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;

import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerStatus;
import com.braintribe.devrock.artifactcontainer.control.project.AccessibleProjectsController;
import com.braintribe.devrock.artifactcontainer.control.walk.ArtifactContainerUpdateRequestType;
import com.braintribe.devrock.artifactcontainer.control.walk.wired.WiredArtifactContainerWalkController;

public class ResourceChangeListener implements IResourceChangeListener {

	private ResourceVisitor resourceVisitor = null;
	private AccessibleProjectsController accessibleProjectController = null;		
	
	public ResourceChangeListener() {
		accessibleProjectController = new AccessibleProjectsController();
		resourceVisitor = new ResourceVisitor();
	}
	
	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		try {
			if (
					(event.getType() == IResourceChangeEvent.PRE_DELETE) &&
					(event.getResource() instanceof IProject)
				) {

				System.out.println("Pre deleting project");
				ArtifactContainerPlugin.getWorkspaceProjectRegistry().dropProject( (IProject) event.getResource());
				// 				
				// might require a refresh? 
			}
			IResourceDelta delta = event.getDelta();
			// no changes? 
			if (delta == null)
				return;
			
			int kind = delta.getKind();
			switch( kind ) {
				case IResourceDelta.CHANGED : {				
					int flags = delta.getFlags();
					if ((flags & IResourceDelta.MARKERS) != 0)
						return;
				}
			}
			
			// install visitor - to detect changes in the pom
			try {
				delta.accept( resourceVisitor);
			} catch (CoreException e1) {		
				e1.printStackTrace();
			}
			

			IResource resource = delta.getResource();
			if (resource != null) {

				// update entries as the workspace has been changed (projects opened/closed/loaded/deleted)			
				if (
						resource instanceof IWorkspaceRoot &&						
						event.getType() == IResourceChangeEvent.POST_CHANGE 
					) {
					// check the controller for changes..
					if (accessibleProjectController.accessibleProjectsChanged() == false)
						return;						
					ArtifactContainerPlugin.getWorkspaceProjectRegistry().update();
					WiredArtifactContainerWalkController.getInstance().updateContainers( ArtifactContainerUpdateRequestType.refresh);										
				}			
			}
		} catch (Exception e) {			
			ArtifactContainerStatus status = new ArtifactContainerStatus("error while reacting to resource change", e);
			ArtifactContainerPlugin.getInstance().log(status);	
		}
	}
	
}
