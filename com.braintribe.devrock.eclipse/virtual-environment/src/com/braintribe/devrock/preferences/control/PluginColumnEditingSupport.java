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
package com.braintribe.devrock.preferences.control;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;

public class PluginColumnEditingSupport extends EditingSupport {
	private ColumnViewer viewer;
	
	public PluginColumnEditingSupport(ColumnViewer viewer) {
		super(viewer);
		this.viewer = viewer;
	}

	@Override
	protected void setValue(Object element, Object input) {
		ContributerTuple tuple = (ContributerTuple) element;
		tuple.setSelected( (Boolean) input);
		viewer.update(element, null);
	}
	
	@Override
	protected Object getValue(Object element) {
		ContributerTuple tuple = (ContributerTuple) element;
		return tuple.getSelected();
	}
	
	@Override
	protected CellEditor getCellEditor(Object element) {
		return new CheckboxCellEditor();
	}
	
	@Override
	protected boolean canEdit(Object element) {
		return true;
	}

}
