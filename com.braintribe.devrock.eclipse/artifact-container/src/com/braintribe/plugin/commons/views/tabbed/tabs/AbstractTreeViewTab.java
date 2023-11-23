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
package com.braintribe.plugin.commons.views.tabbed.tabs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import com.braintribe.plugin.commons.ui.tree.AsyncTreeSortSelectionListener;
import com.braintribe.plugin.commons.ui.tree.TreeColumnResizer;
import com.braintribe.plugin.commons.ui.tree.TreeItemPainter;
import com.braintribe.plugin.commons.ui.tree.TreeItemTooltipProvider;

public abstract class AbstractTreeViewTab extends AbstractViewTab implements Listener, TreeItemPainter {
	protected final static String MARKER_DEFERRED = "loading..";
	protected Tree tree;
	private List<TreeColumn> columns = new ArrayList<TreeColumn>();
	private String [] columnNames;
	private int [] columnWeights;
	protected AsyncTreeSortSelectionListener treeSortListener;
	
	protected Font italicFont;
	protected Font boldFont;
	
	public void setColumnNames(String[] columnNames) {
		this.columnNames = columnNames;
	}

	public void setColumnWeights(int[] columnWeights) {
		this.columnWeights = columnWeights;
	}

	public AbstractTreeViewTab(Display display) {
		super(display);	
		treeSortListener = new AsyncTreeSortSelectionListener(display);
		treeSortListener.setPainter( this);
	}

	@Override
	public Composite createControl(Composite parent) {
	
		final Composite composite = new Composite(parent, SWT.NONE);
		
		int nColumns= 4;
        GridLayout layout= new GridLayout();
        layout.numColumns = nColumns;
        composite.setLayout( layout);
        composite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true));
        
        // 
        //
        //
        
        Composite treeComposite = new Composite( composite, SWT.BORDER);
        treeComposite.setLayout(layout);
		treeComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 4, 4));
		
		tree = new Tree ( treeComposite, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		tree.setHeaderVisible( true);
		
		
		Font initialFont = tree.getFont();
		FontData [] fontDataItalic = initialFont.getFontData();
		for (FontData data : fontDataItalic) {
			data.setStyle( data.getStyle() | SWT.ITALIC);		
		}
		italicFont = new Font( tree.getDisplay(), fontDataItalic);
		
		FontData [] fontDataBold = initialFont.getFontData();
		for (FontData data : fontDataBold) {
			data.setStyle( data.getStyle() | SWT.BOLD);		
		}
		boldFont = new Font( tree.getDisplay(), fontDataBold);
		
		
		
		for (int i = 0; i < columnNames.length; i++) {
			TreeColumn treeColumn = new TreeColumn( tree, SWT.LEFT);
			treeColumn.setText( columnNames[i]);
			treeColumn.setWidth( columnWeights[i]);
			treeColumn.setResizable( true);
			treeColumn.addSelectionListener(treeSortListener);
			columns.add( treeColumn);
		}
		
				
		addAdditionalButtons( composite, layout);
										       		
		TreeColumnResizer columnResizer = new TreeColumnResizer();
		columnResizer.setColumns( columns);
		columnResizer.setColumnWeights( columnWeights);
		columnResizer.setParent( treeComposite);
		columnResizer.setTree( tree);
		
		tree.addControlListener(columnResizer);
		
		tree.addListener(SWT.Expand, this);
		//treeExpander.addListener( this);
		
		TreeItemTooltipProvider.attach(tree, PainterKey.tooltip.name());
		
		initializeTree();
		
		composite.layout();
		return composite;
	}
	
	
	
	@Override
	public void handleEvent(Event event) {
		handleTreeEvent( event);		
	}

	@Override
	public void paint(TreeItem newItem, TreeItem oldItem) {
		// transfer all data as directed 
		for (String key : getRelevantDataKeys()) {
			Object value = oldItem.getData(key);
			if (value == null)
				continue;
			newItem.setData(key, value);
		}
		for (PainterKey painterKey : PainterKey.values()) {
			String key = painterKey.name();
			Object value = oldItem.getData(key);
			if (value == null)
				continue;
			newItem.setData(key, value);
			switch (painterKey) {
				case image : 
					newItem.setImage( (Image) value);
					break;
				case color : 
					newItem.setForeground( (Color) value);
					break;
				case font:
					newItem.setFont( (Font) value);
					break;
				default:
					break;
			}
		}
	}
	
	public int getItemCount() {
		return tree.getItemCount();
	}
	
	
	protected Collection<String> getRelevantDataKeys(){
		return Collections.emptySet();
	}
	
	protected abstract void addAdditionalButtons( Composite composite, Layout layout);
	protected abstract void initializeTree();
	protected abstract void handleTreeEvent( Event event);
	
	
}
