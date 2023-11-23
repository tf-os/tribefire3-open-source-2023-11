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
package com.braintribe.devrock.preferences.pages.ve;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Image;

import com.braintribe.devrock.eclipse.model.ve.EnvironmentOverride;

public class ActiveColumnLabelProvider extends ColumnLabelProvider {
	
	private Image activeImage;
	private Image inactiveImage;
	
	public ActiveColumnLabelProvider() {
		ImageDescriptor imageDescriptor = ImageDescriptor.createFromFile(ActiveColumnLabelProvider.class, "task-active.gif");
		activeImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile(ActiveColumnLabelProvider.class, "task-inactive.gif");
		inactiveImage = imageDescriptor.createImage();		
	}

	@Override
	public Image getImage(Object element) {
		EnvironmentOverride override = (EnvironmentOverride) element;		
		return override.getActive() ? activeImage : inactiveImage;
	}

	@Override
	public String getText(Object element) {
		EnvironmentOverride override = (EnvironmentOverride) element;		
		return override.getActive() ? "active" : "inactive"; 
	}

	@Override
	public void dispose() {	
		activeImage.dispose();
		inactiveImage.dispose();
		super.dispose();
	}

}
