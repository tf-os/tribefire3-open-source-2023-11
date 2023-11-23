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

public class NameColumLabelProvider extends ColumnLabelProvider {
	
	private Image variableLabelImage;
	private Image propertyLabelImage;
	
	public NameColumLabelProvider() {
		ImageDescriptor imageDescriptor = ImageDescriptor.createFromFile( NameColumLabelProvider.class, "variable.gif");
		variableLabelImage = imageDescriptor.createImage();
	
		imageDescriptor = ImageDescriptor.createFromFile( NameColumLabelProvider.class, "property.png");
		propertyLabelImage = imageDescriptor.createImage();
	}

	@Override
	public Image getImage(Object element) {
		EnvironmentOverride override = (EnvironmentOverride) element;
		switch (override.getOverrideNature()) {
			case environment:
				return variableLabelImage;					
			case property:
				return propertyLabelImage;			
			default:
				return null;
		}
	}

	@Override
	public String getText(Object element) {
		EnvironmentOverride override = (EnvironmentOverride) element;
		return override.getName();
	}

	
	@Override
	public String getToolTipText(Object element) {
		EnvironmentOverride override = (EnvironmentOverride) element;
		switch ( override.getOverrideNature()) {
			case environment:
				return "Override for environment variable [" + override.getName() + "]";
			case property:
				return "Override for environment variable [" + override.getName() + "]";			
		default:
			return null;		
		}		
	}

	@Override
	public void dispose() {
		variableLabelImage.dispose();
		propertyLabelImage.dispose();
		super.dispose();
	}

}
