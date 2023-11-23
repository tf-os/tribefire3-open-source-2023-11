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
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.services.IDisposable;

import com.braintribe.utils.lcd.LazyInitialized;

/**
 * a {@link Styler} to make {@link StyledString} ITALIC
 * 
 * @author pit
 *
 */
public class ItalicStyler extends Styler implements IDisposable {
	
	private Font initialFont;	
	private FontDescriptor fontDescriptor;

	LazyInitialized<Font> italicFont = new LazyInitialized<>( this::createFont);
	
	public ItalicStyler(Font initialFont) {
		this.initialFont = initialFont;
	}

	@Override
	public void applyStyles(TextStyle textStyle) {
        textStyle.font = italicFont.get();		
	}
	

	private Font createFont() {				
		fontDescriptor = FontDescriptor.createFrom( initialFont.getFontData()).setStyle( SWT.ITALIC);					
		Font font = fontDescriptor.createFont(Display.getCurrent());
		return font;
	}
	
	public void dispose() {
		if (fontDescriptor == null) {
			return;			
		}
		Font font = italicFont.get();
		if (font != null) {
			fontDescriptor.destroyFont(font);
		}
	}
	
}
