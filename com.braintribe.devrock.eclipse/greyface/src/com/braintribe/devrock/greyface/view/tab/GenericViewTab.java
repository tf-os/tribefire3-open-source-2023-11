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
package com.braintribe.devrock.greyface.view.tab;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.braintribe.cfg.Configurable;
import com.braintribe.devrock.greyface.view.TabItemImageListener;

public abstract class GenericViewTab {
	protected Display display;
	protected Font italicFont;
	protected Font boldFont;
	protected Font italicBoldFont;
	protected Font bigFont;
	protected String id;
	
	protected TabItemImageListener imageListener;

	protected GenericViewTab( Display display){
		this.display = display;
	}
	
	@Configurable
	public void setImageListener(TabItemImageListener imageListener) {
		this.imageListener = imageListener;
	}
	
	protected Composite createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);		  
		
		Font initialFont = parent.getFont();
		FontData [] fontDataItalic = initialFont.getFontData();
		for (FontData data : fontDataItalic) {
			data.setStyle( data.getStyle() | SWT.ITALIC);		
		}
		italicFont = new Font( display, fontDataItalic);
		
		FontData [] fontDataItalicBold = initialFont.getFontData();
		for (FontData data : fontDataItalicBold) {
			data.setStyle( data.getStyle() | SWT.BOLD | SWT.ITALIC);		
		}
		italicBoldFont = new Font( display, fontDataItalicBold);
		
		FontData [] fontDataBold = initialFont.getFontData();
		for (FontData data : fontDataBold) {
			data.setStyle( data.getStyle() | SWT.BOLD);		
		}
		boldFont = new Font( display, fontDataBold);
		
		FontData [] fontDataBig = initialFont.getFontData();
		for (FontData data : fontDataBig) {
			data.setHeight( data.getHeight() + (data.getHeight() / 5));				
		}
		bigFont = new Font( display, fontDataBig);
		
		
        return composite;
	}
	
	public abstract void adjustSize();
	public String getId() {
		return id;
	}
	
	public void dispose() {
		boldFont.dispose();
		italicFont.dispose();
		bigFont.dispose();
		italicBoldFont.dispose();
	}
	
	public void clear() {}
	
	public int getItemCount() { return 0;}
	
	public void acknowledgeActivation() {}
	public void acknowledgeDeactivation() {}
}
