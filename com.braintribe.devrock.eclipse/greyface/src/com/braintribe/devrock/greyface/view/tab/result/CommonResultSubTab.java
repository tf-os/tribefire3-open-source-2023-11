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
package com.braintribe.devrock.greyface.view.tab.result;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import com.braintribe.devrock.greyface.view.tab.GenericViewTab;

public abstract class CommonResultSubTab extends GenericViewTab {

	protected Image successImage;
	protected Image failImage;
	protected Image activityImage;
		
	public CommonResultSubTab(Display display) {
		super(display);
		
		ImageDescriptor imageDescriptor = ImageDescriptor.createFromFile( GenericViewTab.class, "accept.png");
		successImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( GenericViewTab.class, "exclamation--frame.png");
		failImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( GenericViewTab.class, "arrow_refresh_small.png");
		activityImage = imageDescriptor.createImage();							
	}

	@Override
	public void dispose() {	
		successImage.dispose();
		failImage.dispose();
		activityImage.dispose();		
		super.dispose();
	}


}
