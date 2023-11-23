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
package com.braintribe.devrock.ac.container.decorator;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;

import com.braintribe.devrock.ac.container.ArtifactContainer;
import com.braintribe.devrock.ac.container.plugin.ArtifactContainerPlugin;
import com.braintribe.devrock.api.ui.commons.ResolutionValidator;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;


/**
 * simple decorator that marks the project if there's no backing resolution or a failed resolution
 * @author pit
 *
 */
public class ArtifactContainerDecorator implements ILightweightLabelDecorator {
	private ImageDescriptor imageError;
	//private ImageDescriptor imageSkull;

	public ArtifactContainerDecorator() {
		imageError = ImageDescriptor.createFromFile(ArtifactContainerDecorator.class,"error.container.gif");
		//imageSkull = ImageDescriptor.createFromFile(ArtifactContainerDecorator.class,"dead.container.png");
	}

	@Override
	public void addListener(ILabelProviderListener arg0) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public boolean isLabelProperty(Object element, String arg1) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener arg0) {
	}

	@Override
	public void decorate(Object element, IDecoration decoration) {
		if (element instanceof IProject == false)
			return;		
		IProject project = (IProject) element;
		
		if (!project.isAccessible()) {			
			return;
		}
			
		ArtifactContainer container = ArtifactContainerPlugin.instance().containerRegistry().getContainerOfProject(project);
		if (container == null)
			return;
		
		AnalysisArtifactResolution resolution = container.getCompileResolution();
		// just check if it's failed
		if (resolution == null || ResolutionValidator.isResolutionInvalid(resolution)) {			
			decoration.addOverlay(imageError, IDecoration.TOP_LEFT);
		}
		
		
	}


}
