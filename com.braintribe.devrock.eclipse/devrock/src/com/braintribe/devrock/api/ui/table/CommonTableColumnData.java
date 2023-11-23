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
package com.braintribe.devrock.api.ui.table;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.swt.SWT;

/**
 * configuration data for the {@link CommonTableViewer}
 * 
 * @author Pit
 *
 */
public class CommonTableColumnData {

	private String columnName;
	private int columnWeight;
	private int columnMinimalSize;
	private ColumnLabelProvider labelProvider;
	private EditingSupport editingSupport;
	private int columnStyle;
	private boolean resizable = true;
	private boolean moveable = true;
	private String toolTip;
	
	public CommonTableColumnData(String name, int columnMinimalSize, int weight, String toolTip, ColumnLabelProvider labelProvider) {
		this.columnName = name;
		this.columnWeight = weight;
		this.toolTip = toolTip;
		this.labelProvider = labelProvider;		
		this.columnStyle = SWT.NONE;
		this.columnMinimalSize = columnMinimalSize;
	}
	
	public CommonTableColumnData(String name, int columnMinimalSize, int weight, String toolTip, ColumnLabelProvider labelProvider, EditingSupport editingSupport) {
		this.columnName = name;
		this.columnWeight = weight;
		this.columnMinimalSize = columnMinimalSize;
		this.toolTip = toolTip;
		this.labelProvider = labelProvider;
		this.editingSupport = editingSupport;
		this.columnStyle = SWT.NONE;
	}
	public CommonTableColumnData(String name, int columnMinimalSize, int weight, String toolTip, ColumnLabelProvider labelProvider, EditingSupport editingSupport, int columnStyle, boolean resizeable, boolean moveable) {
		this.columnName = name;
		this.columnWeight = weight;
		this.columnMinimalSize = columnMinimalSize;
		this.toolTip = toolTip;
		this.labelProvider = labelProvider;
		this.editingSupport = editingSupport;
		this.columnStyle = columnStyle;		
		this.resizable = resizeable;
		this.moveable = moveable;
	}
	
	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	public int getColumnWeight() {
		return columnWeight;
	}
	public void setColumnWeight(int columnWeight) {
		this.columnWeight = columnWeight;
	}
	public ColumnLabelProvider getLabelProvider() {
		return labelProvider;
	}
	public void setLabelProvider(ColumnLabelProvider labelProvider) {
		this.labelProvider = labelProvider;
	}
	public EditingSupport getEditingSupport() {
		return editingSupport;
	}
	public void setEditingSupport(EditingSupport editingSupport) {
		this.editingSupport = editingSupport;
	}

	public int getColumnStyle() {
		return columnStyle;
	}

	public void setColumnStyle(int columnStyle) {
		this.columnStyle = columnStyle;
	}

	public boolean isResizable() {
		return resizable;
	}

	public void setResizable(boolean resizable) {
		this.resizable = resizable;
	}

	public boolean isMoveable() {
		return moveable;
	}

	public void setMoveable(boolean moveable) {
		this.moveable = moveable;
	}

	public int getColumnMinimalSize() {
		return columnMinimalSize;
	}

	public void setColumnMinimalSize(int columnMinimalSize) {
		this.columnMinimalSize = columnMinimalSize;
	}

	public String getToolTip() {
		return toolTip;
	}

	public void setToolTip(String toolTip) {
		this.toolTip = toolTip;
	}
	
	
	
	
}
