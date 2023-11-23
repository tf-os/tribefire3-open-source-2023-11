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
package com.braintribe.devrock.api.ui.commons;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.graphics.Font;
import org.eclipse.ui.services.IDisposable;

import com.braintribe.devrock.api.ui.images.Images;
import com.braintribe.devrock.api.ui.stylers.Stylers;
import com.braintribe.logging.Logger;


/**
 * a class to manage resources - lives only during plugin's running.
 * @author pit
 *
 */
public class UiSupport implements IDisposable {
	private static Logger log = Logger.getLogger(UiSupport.class);
	
	// stylers 
	private Map<String, Stylers> stylerMap = new ConcurrentHashMap<>();
	// images
	private Images images = new Images();
	// unstructured stuff
	private final Map<Object, Object> map = new ConcurrentHashMap<>();
	
	/**
	 * if the styler doesn't exist yet, it will be created 
	 * @param key - the key
	 * @return - the {@link Stylers} which may be freshly instantiated
	 */
	public Stylers stylers(String key) { 
		return stylerMap.computeIfAbsent( key, k -> new Stylers());
	}
	
	/**
	 * if the styler doesn't exist yet, it will created with the font passed
	 * @param key - the key 
	 * @param basefont - the basefont for the styler 
	 * @return - the {@link Styler}
	 */
	public Stylers stylers(String key, Font basefont) {
		Stylers styler = stylerMap.get(key);
		if (styler == null) {
			styler = new Stylers();
			styler.setInitialFont(basefont);
		}
		else {
			log.warn("a styler is already present for the key: " + key);
		}
		return styler;
	}	
	
	/**
	 * @return - the {@link Images}
	 */
	public Images images() {
		return images;
	}

	@Override
	public void dispose() {
		stylerMap.values().stream().forEach( s -> s.dispose());
		stylerMap.clear();
		images.dispose();		
	}
	

	/**
	 * let's you add a custom value to the map 
	 * @param key - the key
	 * @param value - the {@link Object} to store 
	 */
	public void setCustomValue(Object key, Object value) {
		map.put(key, value);
	}

	/**
	 * get a custom value
	 * @param <T>
	 * @param key
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T getCustomValue(Object key) {
		return (T) map.get(key);
	}

}
