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
package com.braintribe.devrock.artifactcontainer.natures;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;

/**
 * the decorator for the module carrier nature 
 * 
 * @author pit
 *
 */
public class TribefireServicesNatureDecorator implements ILightweightLabelDecorator {
	private ImageDescriptor image;

	public TribefireServicesNatureDecorator() {
		image = ImageDescriptor.createFromFile(TribefireServicesNatureDecorator.class, "carrier.png");
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
		if (hasTribefireServicesNature(project)) {
			decoration.addOverlay(image, IDecoration.TOP_LEFT);
		}
	}

	private boolean hasTribefireServicesNature(IProject project) {
		try {
			IProjectDescription descriptions = project.getDescription();
			String[] natureIds = descriptions.getNatureIds();
			if (natureIds == null || natureIds.length == 0)
				return false;
			for (String id : natureIds) {
				if (id.equalsIgnoreCase(TribefireServicesNature.NATURE_ID))
					return true;
			}

		} catch (CoreException e) {
		}
		return false;
	}

}
