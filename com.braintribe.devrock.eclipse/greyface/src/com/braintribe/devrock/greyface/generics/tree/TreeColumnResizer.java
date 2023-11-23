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
package com.braintribe.devrock.greyface.generics.tree;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

public class TreeColumnResizer extends ControlAdapter {

	private List<TreeColumn>  columns = null;
	private int [] columnWeights = null;
	private Composite parent = null;
	int sumOfColumnWeights = 0;
	private Tree tree = null;
	
	public void setColumns(List<TreeColumn> columns) {
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
		
	public void setTree(Tree tree) {
		this.tree = tree;
	}
	private void recalculateColumns(int width) { 				 
		 float widthPerColumn = ((float)width) / sumOfColumnWeights;
    	 for (int i = 0; i < columns.size(); i++) {
    		TreeColumn column = columns.get(i);
    		 column.setWidth( (int) (widthPerColumn * columnWeights[i]));
    	 }
	 }
	
	public void resize() {
		  Rectangle area = parent.getClientArea();
	      Point preferredSize = tree.computeSize(SWT.DEFAULT, SWT.DEFAULT);
	      int width = area.width - 2 * tree.getBorderWidth();
	      
	      if (preferredSize.y > area.height + tree.getHeaderHeight()) {
	        // Subtract the scrollbar width from the total column width
	        // if a vertical scrollbar will be required
	        Point vBarSize = tree.getVerticalBar().getSize();
	        width -= vBarSize.x;
	      }
	      recalculateColumns( width);
	      
	      int height = tree.getSize().y;
	  	  tree.setSize(area.width, height);
	  
	  	  Point oldSize = tree.getSize();
	      if (oldSize.x > area.width) {
	        // table is getting smaller so make the columns 
	        // smaller first and then resize the table to
	        // match the client area width
	    	  tree.setSize(area.width, height);
	    	recalculateColumns( width);
	    	
	      } else {
	        // table is getting bigger so make the table 
	        // bigger first and then make the columns wider
	        // to match the client area width
	    	  tree.setSize(area.width, height);
	    	 recalculateColumns(width);
	    	 
	      }
	}
	 
    @Override
	public void controlResized(ControlEvent e) {    	
    	resize();  
    }
}
