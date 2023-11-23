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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.services.IDisposable;

public class Fonts implements IDisposable {
	private Map<String,Font> keyToFontMap = new ConcurrentHashMap<>();
	private Font initialFont;
	private Display display;
	
	
	public Fonts() {
		display = Display.getCurrent();
	}
	public void setInitialFont(Font initialFont) {
		this.initialFont = initialFont;
	}
	
	public Font getInitialFont() {
		return initialFont;
	}
	
	public Font addBigFont(String key) {
		return keyToFontMap.computeIfAbsent(key, k -> {
			FontData [] fontDataBig = initialFont.getFontData();
			for (FontData data : fontDataBig) {
				data.setHeight( data.getHeight() + (data.getHeight() / 5));				
			}
			Font bigFont = new Font( display, fontDataBig);		
			return bigFont;
		});		
	}
	
	public Font addBigFont(String key, FontData[] data) {
		return keyToFontMap.computeIfAbsent(key, k -> {			
			Font bigFont = new Font( display, data);		
			return bigFont;
		});	
	}
	
	@Override
	public void dispose() {
		keyToFontMap.values().stream().forEach( f -> f.dispose());
		
	}
	
	}
