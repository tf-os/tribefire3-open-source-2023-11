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
package com.braintribe.devrock.api.ui.tree;

import java.util.Comparator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TreeItem;


public class TreeItemComparator implements Comparator<TreeItem> {

	int direction = SWT.UP;
	int [] indices;
	int numColumns = 0;
	
	public TreeItemComparator( int mode, int index, int numColumns) {
		direction = mode;
		indices = new int[1];
		indices[0] = index;
		this.numColumns = numColumns;
	}
	
	public TreeItemComparator( int mode, int [] indices, int numColumns) {
		direction = mode;
		this.indices = indices;
		this.numColumns = numColumns;
	}
	@Override
	public int compare(TreeItem o1, TreeItem o2) {
		
		String [] values1 = TreeSortHelper.getColumnValues( o1, numColumns);
		String [] values2 = TreeSortHelper.getColumnValues( o2, numColumns);
		
		for (int index = 0; index < indices.length; index++) {
			String value1 = values1[ indices[index]];
			String value2 = values2[ indices[index]];
	
			int retval = value1.compareToIgnoreCase( value2);
			
			if (retval != 0) {
				switch (direction) {
					case SWT.UP:
						return retval;
					case SWT.DOWN:
						return retval*-1;
				}
			}
			
		}
		
		return 0;
	}
	
	
}
