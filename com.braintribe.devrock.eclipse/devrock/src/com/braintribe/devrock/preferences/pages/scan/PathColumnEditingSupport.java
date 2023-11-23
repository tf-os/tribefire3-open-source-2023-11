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

import org.eclipse.jface.viewers.TableViewer;

import com.braintribe.devrock.eclipse.model.scan.SourceRepositoryEntry;

public class PathColumnEditingSupport extends AbstractColumnEditingSupport {
	
	private TableViewer viewer;
	
	
	public PathColumnEditingSupport(TableViewer viewer, boolean local) {
		super(viewer);
		this.viewer = viewer;	
	}

	@Override
	protected Object getValue(Object element) {
		SourceRepositoryEntry pairing = (SourceRepositoryEntry) element;
				
		return pairing.getPath();			
	}

	@Override
	protected void setValue(Object element, Object value) {
		SourceRepositoryEntry pairing = (SourceRepositoryEntry) element;
		pairing.setPath( (String) value);
		viewer.refresh( element);

	}

}
