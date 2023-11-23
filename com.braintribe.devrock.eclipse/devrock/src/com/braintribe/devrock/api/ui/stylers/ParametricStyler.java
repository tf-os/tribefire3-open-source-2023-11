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

import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.services.IDisposable;

import com.braintribe.utils.lcd.LazyInitialized;

/**
 * a {@link StyledString} {@link Styler} that can be parametrized for font, font-style (bold or so) and foreground, background color 
 * @author pit
 *
 */
public class ParametricStyler extends Styler  implements IDisposable {

	private final Font initialFont;
	private FontDescriptor fontDescriptor;
	private Integer fontModifier;
	private Color foreground;
	private Color background;
	private Float sizeFactor;
	
	LazyInitialized<Font> derivedFont = new LazyInitialized<>( this::createFont);

	public ParametricStyler(Font initialFont, Integer fontModifier) {
		this.initialFont = initialFont;
		this.fontModifier = fontModifier;
		foreground = background = null;
		sizeFactor = null;
	}
			
	
	/**
	 * @param initialFont
	 * @param fontModifier
	 * @param foreground - system color
	 * @param background - system color
	 * @param sizeFactor
	 */
	public ParametricStyler(Font initialFont, Integer fontModifier, Integer foreground, Integer background, Float sizeFactor) {
		this.initialFont = initialFont;
		this.fontModifier = fontModifier;
		this.sizeFactor = sizeFactor;
		
		Display display = Display.getCurrent();
		if (foreground != null) {
			this.foreground = display.getSystemColor(foreground);
		}
		if (background != null) {
			this.background = display.getSystemColor(background);
		}
	}
	
	/**
	 * @param initialFont
	 * @param fontModifier
	 * @param foreground
	 * @param background
	 * @param sizeFactor
	 */
	public ParametricStyler(Font initialFont, Integer fontModifier, Color foreground, Color background, Float sizeFactor) {
		this.initialFont = initialFont;
		this.fontModifier = fontModifier;
		this.sizeFactor = sizeFactor;		

		this.foreground = foreground;
		this.background = background;
	}
	

	@Override
	public void applyStyles(TextStyle textStyle) {
		Font font = derivedFont.get();
        textStyle.font = font;
               
        if (foreground != null) {
        	textStyle.foreground = foreground;
        }
        if (background != null) {
        	textStyle.background = background;
        }
	}
	
	private Font createFont() {
		FontData[] fontData = initialFont.getFontData();
		if (sizeFactor != null) {
			for (FontData data : fontData) {
				data.setHeight( Math.round(data.getHeight() * sizeFactor));				
			}
		}		
		if (fontModifier != null) {
			fontDescriptor = FontDescriptor.createFrom( fontData).setStyle( fontModifier);			
		}
		else {
			fontDescriptor = FontDescriptor.createFrom( fontData);	
		}
		Font font = fontDescriptor.createFont(Display.getCurrent());
		return font;
	}
	
	@Override
	public void dispose() {
		if (fontDescriptor == null)
			return;
		Font font = derivedFont.get();
		if (font != null) {
			fontDescriptor.destroyFont(font);
		}
		
	}
	
}
