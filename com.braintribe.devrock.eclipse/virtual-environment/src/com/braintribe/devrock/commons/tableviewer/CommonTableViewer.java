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
package com.braintribe.devrock.commons.tableviewer;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;

/**
 * convenient helper to create tables in SWT/JFace
 * @author Pit
 *
 */
public class CommonTableViewer extends TableViewer{
	private Composite tableComposite;
	private CommonTableColumnData [] columnDati;
	private TableViewerColumn [] viewerColumns;

	public CommonTableViewer(Composite parent, int style) {
		super(parent, style);
		tableComposite = parent;
	}

	public CommonTableViewer(Composite parent, int style, CommonTableColumnData [] data) {
		super(parent, style);
		this.columnDati = data;
		tableComposite = parent;
	}
	
	public void setup(CommonTableColumnData [] data) {
		this.columnDati = data;
		setup();
	}
	
	public void setup() {
		viewerColumns = new TableViewerColumn [columnDati.length];
		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		tableComposite.setLayout(tableColumnLayout);
		
		for (int i = 0; i < columnDati.length; i++) {
			CommonTableColumnData data = columnDati[i];
			TableViewerColumn viewerColumn = new TableViewerColumn(this, data.getColumnStyle());
					
			TableColumn column = viewerColumn.getColumn();
			column.setText( data.getColumnName());
			column.setResizable( data.isResizable());
			column.setMoveable( data.isMoveable());

			viewerColumn.setLabelProvider(data.getLabelProvider());
			viewerColumn.setEditingSupport( data.getEditingSupport());
			column.setToolTipText( data.getToolTip());
			
			tableColumnLayout.setColumnData( column, new ColumnWeightData( data.getColumnMinimalSize(), data.getColumnWeight(), data.isResizable()));
			
			viewerColumns[i] = viewerColumn;
		}
		ColumnViewerToolTipSupport.enableFor(this, ToolTip.NO_RECREATE);
	}
	
}
