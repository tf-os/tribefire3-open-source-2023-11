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
package com.braintribe.devrock.artifactcontainer.plugin.preferences.ant;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Image;

import com.braintribe.model.malaclypse.cfg.AntTarget;

public class TransitivityColumnLabelProvider extends ColumnLabelProvider {
	private Image transitiveImage;
	private Image intransitiveImage;
	
	public TransitivityColumnLabelProvider() {
		ImageDescriptor imageDescriptor = ImageDescriptor.createFromFile( TransitivityColumnLabelProvider.class, "task-active.gif");
		transitiveImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( TransitivityColumnLabelProvider.class, "task-inactive.gif");
		intransitiveImage = imageDescriptor.createImage();
	}

	@Override
	public Image getImage(Object element) {
		AntTarget setting = (AntTarget) element;
		return setting.getTransitiveNature() ? transitiveImage : intransitiveImage;		
	}

	@Override
	public String getText(Object element) {
		return null;
	}

	@Override
	public String getToolTipText(Object element) {
		AntTarget setting = (AntTarget) element;
		if (setting.getTransitiveNature()) {
			return "target is a transitive target (runs in main artifact directory)"; 			
		}
		else {
			return "target is an intransitive target (runs in local artifact directory)";
		}
	}

	@Override
	public void dispose() {
		transitiveImage.dispose();
		intransitiveImage.dispose();
		super.dispose();
	}

	
}
