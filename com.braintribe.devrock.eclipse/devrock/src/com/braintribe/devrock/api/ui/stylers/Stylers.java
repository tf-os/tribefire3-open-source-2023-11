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
package com.braintribe.devrock.api.ui.stylers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.services.IDisposable;

import com.braintribe.cfg.Configurable;

/**
 * a 'group' of Styler instances.. 
 * @author pit
 *
 */
public class Stylers {
	public static final String KEY_ITALIC = "italic";
	public static final String KEY_BOLD = "bold";
	public static final String KEY_DEFAULT = "default";
	
	private Map<String, Styler> keyToStylerMap = new ConcurrentHashMap<>();
	private Font initialFont;
	
	/**
	 * constructor using the current font as a base
	 */
	public Stylers() {
		this.initialFont = Display.getCurrent().getSystemFont();
	}
	
	/**
	 * constructor using the passed font as a base
	 * @param initialFont - the {@link Font} to base the derivations from
	 */
	public Stylers(Font initialFont) {
		this.initialFont = initialFont;		
	}
	
	/**
	 * sets the base font 
	 * @param initialFont - the {@link Font} to base the derivations from
	 */
	@Configurable
	public void setInitialFont(Font initialFont) {
		this.initialFont = initialFont;
	}
	
	public Font getInitialFont() {
		return initialFont;
	}
	
	/**
	 * adds a bold, italic and default styler 
	 */
	public void addStandardStylers() {
		keyToStylerMap.computeIfAbsent(KEY_BOLD, k -> new BoldStyler( initialFont));				
		keyToStylerMap.computeIfAbsent(KEY_ITALIC, k -> new ItalicStyler(initialFont));
		keyToStylerMap.computeIfAbsent(KEY_DEFAULT, k -> new ParametricStyler(initialFont, null, (Color) null, (Color) null, null)); 
	}
	
	/**
	 * @param key - the key of one of the standard stylers
	 * @return
	 */
	public Styler standardStyler(String key) {
		addStandardStylers();
		return keyToStylerMap.get(key);
	}
	
	/**
	 * add a styler 
	 * @param key - the key of the Styler
	 * @param styler - the {@link Styler}
	 */
	public void addStyler( String key, Styler styler) {
		keyToStylerMap.put(key, styler);
	}
	
	
	public Styler addDefaultStyler(String key) {
		Styler styler = keyToStylerMap.computeIfAbsent(key, k -> new ParametricStyler(initialFont, (Integer) null));
		return styler;
	}
	public Styler addStyler(String key, Integer fontModifier) {
		Styler styler = keyToStylerMap.computeIfAbsent(key, k -> new ParametricStyler(initialFont, fontModifier));
		return styler;
	}
	
	/**
	 * create and add a styler 
	 * @param key - the key 
	 * @param fontModifier - the font modifier (see SWT)
	 * @param foreground - the foreground (see SWT)
	 * @param background - the background (see SWT)
	 * @return - the {@link Styler} created and added to list
	 */
	public Styler addStyler(String key, Integer fontModifier, Integer foreground, Integer background, Float sizeFactor) {
		Styler styler = keyToStylerMap.computeIfAbsent(key, k -> new ParametricStyler(initialFont, fontModifier, foreground, background, sizeFactor));
		return styler;
	}
	
	public Styler addStyler(String key, Integer fontModifier, Color foreground, Color background, Float sizeFactor) {
		Styler styler = keyToStylerMap.computeIfAbsent(key, k -> new ParametricStyler(initialFont, fontModifier, foreground, background, sizeFactor));
		return styler;
	}
	
	
	/**
	 * @param key - the key of the styler to retrieve
	 * @return - the {@link Styler} if any
	 */
	public Styler styler(String key) {
		return keyToStylerMap.get(key);				
	}
	
	/**
	 * dispose all stylers 
	 */
	public void dispose() {
		keyToStylerMap.values().stream().map( s -> (IDisposable) s).forEach( s -> s.dispose());		
	}

}
