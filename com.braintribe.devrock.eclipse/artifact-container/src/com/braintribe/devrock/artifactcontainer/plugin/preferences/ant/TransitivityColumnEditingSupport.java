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
package com.braintribe.devrock.artifactcontainer.plugin.preferences.ant;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;

import com.braintribe.model.malaclypse.cfg.AntTarget;

public class TransitivityColumnEditingSupport extends EditingSupport {
	
	private TableViewer viewer;

	public TransitivityColumnEditingSupport(TableViewer viewer) {
		super(viewer);
		this.viewer = viewer;
	}

	@Override
	protected boolean canEdit(Object arg0) {		
		return true;
	}

	@Override
	protected CellEditor getCellEditor(Object arg0) {
		return new CheckboxCellEditor( viewer.getTable());
	}

	@Override
	protected Object getValue(Object element) {
		AntTarget setting = (AntTarget) element;	
		return setting.getTransitiveNature();
	}

	@Override
	protected void setValue(Object element, Object value) {
		AntTarget setting = (AntTarget) element;
		setting.setTransitiveNature( (Boolean) value);
		viewer.refresh(element);

	}

}
