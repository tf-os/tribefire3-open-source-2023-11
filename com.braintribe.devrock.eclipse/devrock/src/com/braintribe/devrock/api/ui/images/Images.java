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
package com.braintribe.devrock.api.ui.images;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.services.IDisposable;

public class Images implements IDisposable {
	private Map<String, Image> images = new ConcurrentHashMap<>();

	public Image addImage( String key, Class<?> owner, String name) {
		Image image = images.computeIfAbsent( key, k -> {		
			ImageDescriptor descriptor = ImageDescriptor.createFromFile(owner, name);
			return descriptor.createImage();
		});			
		return image;
	}
	
	public Image getImage(String key) {
		return images.get(key);
	}

	@Override
	public void dispose() {
		images.values().stream().forEach( i -> i.dispose());
		images.clear();
	}

	
	
}
