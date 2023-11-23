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
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import com.braintribe.model.malaclypse.cfg.AntTarget;

public class AntTargetLabelProvider extends LabelProvider implements ITableLabelProvider {
	ImageDescriptor yes_imageDescriptor = ImageDescriptor.createFromFile( AntTargetLabelProvider.class, "task-active.gif");
	ImageDescriptor no_imageDescriptor = ImageDescriptor.createFromFile( AntTargetLabelProvider.class, "task-inactive.gif");
	
	@Override
	public Image getColumnImage(Object object, int columnIndex) {
		AntTarget setting = (AntTarget) object;		
		
		switch (columnIndex) {
			case 0:
				return null;
			case 1: 				
				return null;			
			case 2:		
				return setting.getTransitiveNature() ? yes_imageDescriptor.createImage() : no_imageDescriptor.createImage();			
		}		
		return null;
	}

	@Override
	public String getColumnText(Object object, int columnIndex) {
		AntTarget setting = (AntTarget) object;	
		switch (columnIndex) {
			case 0:
				return setting.getName();
			case 1:
				return setting.getTarget();
			case 2:
				return setting.getTransitiveNature() ? "transitive" : "single artifact";
			default:
				return null;		
		}
	}



}
