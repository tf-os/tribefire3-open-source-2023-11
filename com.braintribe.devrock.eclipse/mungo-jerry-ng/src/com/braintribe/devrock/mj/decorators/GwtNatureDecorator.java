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
package com.braintribe.devrock.mj.decorators;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;

import com.braintribe.devrock.api.nature.NatureHelper;
import com.braintribe.devrock.mj.natures.GwtLibraryNature;
import com.braintribe.devrock.mj.natures.GwtTerminalNature;

public class GwtNatureDecorator implements ILightweightLabelDecorator {
	private ImageDescriptor image;

	public GwtNatureDecorator() {
		image = ImageDescriptor.createFromFile(GwtNatureDecorator.class,"gwt-logo3t.png");
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
		if (hasModelNature(project)) {
			decoration.addOverlay(image, IDecoration.TOP_LEFT);
		}
	}

	private boolean hasModelNature(IProject project) {
		return NatureHelper.hasAnyNatureOf(project, GwtTerminalNature.NATURE_ID, GwtLibraryNature.NATURE_ID);
	}

}
