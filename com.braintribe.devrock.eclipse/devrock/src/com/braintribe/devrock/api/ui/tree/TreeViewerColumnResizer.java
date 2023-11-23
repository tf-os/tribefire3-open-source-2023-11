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

import java.util.List;

import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;

public class TreeViewerColumnResizer extends ControlAdapter {

	private List<TreeViewerColumn> columns = null;
	private Composite parent = null;
	private Tree tree = null;

	private volatile boolean active = false;

	public void setColumns(List<TreeViewerColumn> columns) {
		this.columns = columns;
	}

	public void setParent(Composite parent) {
		this.parent = parent;
	}

	public void setTree(Tree tree) {
		this.tree = tree;
	}

	@Override
	public void controlResized(ControlEvent e) {
		if (!activate())
			return;

		try {
			controlResized();
		} finally {
			deactivate();
		}
	}

	private boolean activate() {
		if (active)
			return false;

		synchronized (this) {
			if (active)
				return false;

			return active = true;
		}
	}

	private void deactivate() {
		active = false;
	}

	private void controlResized() {
		Rectangle area = parent.getClientArea();
		Point preferredSize = tree.computeSize(SWT.DEFAULT, SWT.DEFAULT);

		int width = area.width - 2 * tree.getBorderWidth();
		if (preferredSize.y > area.height + tree.getHeaderHeight()) {
			// Subtract the scrollbar width from the total column width
			// if a vertical scrollbar will be required
			Point vBarSize = tree.getVerticalBar().getSize();
			width -= vBarSize.x;
		}

		resizeColumnsToTotalWidthOf(width);
		tree.setSize(area.width, area.height);
	}

	private void resizeColumnsToTotalWidthOf(int width) {
		int sum = 0;
		int[] columnWidths = new int[columns.size()];

		for (int i = 0; i < columnWidths.length; i++) {
			columnWidths[i] = columns.get(i).getColumn().getWidth();
			sum += columnWidths[i];
		}

		float widthPerColumn = ((float) width) / sum;

		for (int i = 0; i < columns.size(); i++) {
			int newWidth = (int) (widthPerColumn * columnWidths[i]);
			columns.get(i).getColumn().setWidth(newWidth);
		}
	}

}
