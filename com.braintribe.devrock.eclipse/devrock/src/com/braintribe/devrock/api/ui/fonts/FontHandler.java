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
package com.braintribe.devrock.api.ui.fonts;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

/**
 * simple handler for fonts.. 
 * @author pit
 *
 */
public class FontHandler {
	
	private Font bigFont;
	private Font boldFont;
	private Font italicFont;

	public void buildCommonFonts(Composite parent) {
		Font initialFont = parent.getFont();		
		// big font 
		bigFont = buildBigFont(parent.getDisplay(), initialFont);		
		boldFont = buildBoldFont(parent.getDisplay(), initialFont);		
		italicFont = buildItalicFont(parent.getDisplay(), initialFont);						
	}
	public void buildCommonFonts(Display display, Font initialFont) {		
		// big font 
		bigFont = buildBigFont(display, initialFont);		
		boldFont = buildBoldFont( display, initialFont);		
		italicFont = buildItalicFont( display, initialFont);						
	}

	public static Font buildItalicFont(Display display, Font initialFont) {
		FontData [] fontDataItalic = initialFont.getFontData();
		for (FontData data : fontDataItalic) {
			data.setStyle( data.getStyle() | SWT.ITALIC);				
		}
		return new Font( display, fontDataItalic);
	}

	public static Font buildBoldFont(Display display, Font initialFont) {
		FontData [] fontDataBold = initialFont.getFontData();
		for (FontData data : fontDataBold) {
			data.setStyle( data.getStyle() | SWT.BOLD);				
		}
		return new Font( display, fontDataBold);
	}

	public static Font buildBigFont(Display display, Font initialFont) {
		FontData [] fontDataBig = initialFont.getFontData();
		for (FontData data : fontDataBig) {
			data.setHeight( data.getHeight() + (data.getHeight() / 5));				
		}
		return new Font( display, fontDataBig);
	}
	
	public void disposeCommonFonts() {
		if (bigFont != null)
			bigFont.dispose();
		
		if (boldFont != null)
			boldFont.dispose();
		
		if (italicFont != null) {
			italicFont.dispose();
		}
	}
	
}
