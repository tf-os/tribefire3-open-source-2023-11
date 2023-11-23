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
package com.braintribe.devrock.artifactcontainer.control.container.decorator;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;

import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.control.container.ArtifactContainerRegistry;
import com.braintribe.devrock.artifactcontainer.views.dependency.diagnostics.ContainerState;
import com.braintribe.devrock.artifactcontainer.views.dependency.diagnostics.ProjectDiagnostics;
import com.braintribe.model.malaclypse.WalkMonitoringResult;

public class ProjectDecorator implements ILightweightLabelDecorator {
	private ImageDescriptor errorImageDescriptor;
	private ImageDescriptor warningImageDescriptor;
	private ImageDescriptor deadImageDescriptor;
	private ArtifactContainerRegistry artifactContainerRegistry = ArtifactContainerPlugin.getArtifactContainerRegistry();
	
	
	public ProjectDecorator()  {
		errorImageDescriptor = ImageDescriptor.createFromFile( ProjectDecorator.class, "error_ovr.gif");
		warningImageDescriptor = ImageDescriptor.createFromFile( ProjectDecorator.class, "warning_small.png");
		deadImageDescriptor = ImageDescriptor.createFromFile( ProjectDecorator.class, "skull.png");
		
	}

	@Override
	public void addListener(ILabelProviderListener arg0){}
	@Override
	public void removeListener(ILabelProviderListener arg0){}
	@Override
	public boolean isLabelProperty(Object arg0, String arg1) {
		return false;
	}

	@Override
	public void dispose(){}


	@Override
	public void decorate(Object element, IDecoration iDecoration) {
		if (element instanceof IProject == false)
			return;		
		IProject project = (IProject) element;
		
		// check if project has a container, if not, none of our business
		if (ArtifactContainerPlugin.getArtifactContainerRegistry().getContainerOfProject(project) == null) {
			return;
		}
		// dead check 
		if (ArtifactContainerPlugin.getWorkspaceProjectRegistry().hasLastWalkFailed( project)) {
			iDecoration.addOverlay(deadImageDescriptor, IDecoration.TOP_LEFT);
			return;
		}
		
		WalkMonitoringResult walkResult = artifactContainerRegistry.getCompileWalkResult( project);
		
		
		// no result at all - something's fishy 
		if (walkResult == null ) {
			if (artifactContainerRegistry.getContainerOfProject(project) != null) {
				iDecoration.addOverlay(deadImageDescriptor, IDecoration.TOP_LEFT);
			}
			return;
		}
		
		ContainerState containerState = ProjectDiagnostics.isProjectHealhty(walkResult);
		switch (containerState) {
			case error:
				iDecoration.addOverlay(errorImageDescriptor, IDecoration.TOP_LEFT);
				break;
			case warning:
				iDecoration.addOverlay(warningImageDescriptor, IDecoration.TOP_LEFT);
				break;
			case dead:
				iDecoration.addOverlay(deadImageDescriptor, IDecoration.TOP_LEFT);
				break;
			default:
				break;
		}
	}
	

}
