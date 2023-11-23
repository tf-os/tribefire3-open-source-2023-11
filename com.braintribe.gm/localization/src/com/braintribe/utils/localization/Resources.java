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
package com.braintribe.utils.localization;

import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;

import javax.swing.Icon;
import javax.swing.ImageIcon;

public abstract class Resources {
	private static final Map<String, Object> cache = new HashMap<String, Object>();
	private static final Object MISSING = new Object();
	
	/**
	 * Returns an icon created from an image found in the classpath in the same place as this class 
	 * - that is, using Messages.class.getResource(name). Icons that where created once are cached.
	 * If the icon cannot be found, a MissingResourceException is raised.
	 */
	public final synchronized Icon getIcon(String name) {
		String k = "icon:"+name;
		
		Object obj = cache.get(k);
		
		//check for negative cache entry
		if (obj==MISSING) 
			throw new MissingResourceException("image not found in classloader: "+name, getClass().getName(), name);
		
		Icon icon = (Icon)obj;
		
		if (icon==null) {
			URL url = getClass().getResource(name);
			
			if (url==null) {
				cache.put(k, MISSING);
				throw new MissingResourceException("image not found in classloader: "+name, getClass().getName(), name);
			}
			
			icon = new ImageIcon(url); 
			cache.put(k, icon);
		}

		return icon;
	}
	
	/**
	 * Returns an image found in the classpath in the same place as this class 
	 * - that is, using Messages.class.getResource(name). Images that where created once are cached.
	 * If the image cannot be found, a MissingResourceException is raised.
	 */
	public final synchronized Image getImage(String name) {
		String k = "image:"+name;
		
		Object obj = cache.get(k);
		
		//check for negative cache entry
		if (obj==MISSING) 
			throw new MissingResourceException("image not found in classloader: "+name, getClass().getName(), name);
		
		Image image = (Image)obj;
		
		if (image==null) {
			URL url = getClass().getResource(name);
			
			if (url==null) {
				cache.put(k, MISSING);
				throw new MissingResourceException("image not found in classloader: "+name, getClass().getName(), name);
			}
			
			image = Toolkit.getDefaultToolkit().getImage(url); 
			cache.put(k, image);
		}

		return image;
	}
}
