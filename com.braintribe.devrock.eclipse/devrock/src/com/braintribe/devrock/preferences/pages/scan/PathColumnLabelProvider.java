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
package com.braintribe.devrock.preferences.pages.scan;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import com.braintribe.devrock.eclipse.model.scan.SourceRepositoryEntry;

public class PathColumnLabelProvider extends ColumnLabelProvider {	
	private Display display;

	public PathColumnLabelProvider(Display display) {
		super();
		this.display = display;

	}

	@Override
	public String getText(Object element) {
		SourceRepositoryEntry pairing = (SourceRepositoryEntry) element;		
		return pairing.getActualFile();
	}

	@Override
	public String getToolTipText(Object element) {
		SourceRepositoryEntry pairing = (SourceRepositoryEntry) element;
		String tooltipMsg = "";
		if (pairing.getSymbolLink()) {
			File file = new File(pairing.getActualFile());			
			try {
				Path path = Files.readSymbolicLink( file.toPath());
				tooltipMsg = "target : " + path.toString();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return tooltipMsg.length() > 0 ? tooltipMsg : pairing.getPath();	
	}

	@Override
	public Color getForeground(Object element) {
		SourceRepositoryEntry pairing = (SourceRepositoryEntry) element;
		if (pairing.getSymbolLink()) {
			return display.getSystemColor( SWT.COLOR_DARK_GRAY);
		}
		return super.getForeground(element);
	}

}
