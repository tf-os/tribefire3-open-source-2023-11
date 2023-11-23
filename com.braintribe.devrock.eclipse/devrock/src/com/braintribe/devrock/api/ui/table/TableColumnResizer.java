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

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class TableColumnResizer extends ControlAdapter {
	
	private List<TableColumn>  columns = null;
	private int [] columnWeights = null;
	private Composite parent = null;
	int sumOfColumnWeights = 0;
	private Table table = null;
	
	public void setColumns(List<TableColumn> columns) {
		this.columns = columns;
	}
	public void setColumnWeights(int[] columnWeights) {
		this.columnWeights = columnWeights;
		for (int i = 0; i < columnWeights.length; i++) {
			sumOfColumnWeights += columnWeights[i];
		}
	}
	public void setParent(Composite parent) {
		this.parent = parent;
	}
	
	public void setTable(Table table) {
		this.table = table;
	}

	private void recalculateColumns(int width) { 				 
		 float widthPerColumn = ((float)width) / sumOfColumnWeights;
    	 for (int i = 0; i < columns.size(); i++) {
    		TableColumn column = columns.get(i);
    		 column.setWidth( (int) (widthPerColumn * columnWeights[i]));
    	 }
	 }
	 
	@Override
    public void controlResized(ControlEvent e) {
      Rectangle area = parent.getClientArea();
      Point preferredSize = table.computeSize(SWT.DEFAULT, SWT.DEFAULT);
      int width = area.width - 2*table.getBorderWidth();
      if (preferredSize.y > area.height + table.getHeaderHeight()) {
        // Subtract the scrollbar width from the total column width
        // if a vertical scrollbar will be required
        Point vBarSize = table.getVerticalBar().getSize();
        width -= vBarSize.x;
      }
      recalculateColumns( width);
  	  table.setSize(area.width, area.height);
  	/*
      Point oldSize = table.getSize();      
      if (oldSize.x > area.width) {
        // table is getting smaller so make the columns 
        // smaller first and then resize the table to
        // match the client area width
    	recalculateColumns( width);
    	table.setSize(area.width, area.height);
      } else {
        // table is getting bigger so make the table 
        // bigger first and then make the columns wider
        // to match the client area width 		    	
    	 recalculateColumns(width);
    	 table.setSize(area.width, area.height);      
      }
      */
    }
  
}
