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
package com.braintribe.devrock.api.ui.viewers.reason;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.devrock.api.ui.commons.UiSupport;
import com.braintribe.devrock.api.ui.tree.TreeViewerColumnResizer;
import com.braintribe.gm.model.reason.Reason;


public class ReasonViewer {
	
	private ContentProvider contentProvider;
	private TreeViewer treeViewer;
	private Tree tree;
	private Reason reason;
	
	private UiSupport uiSupport; 
	
	
	@Configurable @Required
	public void setUiSupport(UiSupport uiSupport) {
		this.uiSupport = uiSupport;				
	}
	
	
	public ReasonViewer( Reason reason) {
		this.reason = reason;
		
	}

	public Composite createControl( Composite parent, String tag) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 4;
		composite.setLayout(layout);
		
		Composite treeLabelComposite = new Composite( composite, SWT.NONE);
        treeLabelComposite.setLayout( layout);
        treeLabelComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4,1));
        
        Label treeLabel = new Label( treeLabelComposite, SWT.NONE);
        treeLabel.setText( tag);
        treeLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, true, false, 4, 1));
        
        // tree for display
        Composite treeComposite = new Composite( composite, SWT.NONE);	
        treeComposite.setLayout(layout);
		treeComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 4, 2));
		
		treeViewer = new TreeViewer( treeComposite, SWT.H_SCROLL | SWT.V_SCROLL);
		
		contentProvider = new ContentProvider();
		contentProvider.setupFrom( reason);
		treeViewer.setContentProvider( contentProvider);
    	treeViewer.getTree().setHeaderVisible(true);
    	
    	List<Reason> reasons = new ArrayList<>();
    	if (reason != null) {
    		reasons.add( reason);
    	}
    	
    	ColumnViewerToolTipSupport.enableFor(treeViewer);
    	
    	// columns 
    	List<TreeViewerColumn> columns = new ArrayList<>();        	
    	
    	TreeViewerColumn nameColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
        nameColumn.getColumn().setText("Type");
        nameColumn.getColumn().setToolTipText( "type of the reason");
        nameColumn.getColumn().setWidth(50);
        ReasonViewLabelProvider viewTypeLabelProvider = new ReasonViewLabelProvider( ReasonViewLabelProvider.KEY_TYPE, uiSupport, "reason-viewer");
		nameColumn.setLabelProvider(new DelegatingStyledCellLabelProvider( viewTypeLabelProvider));
        nameColumn.getColumn().setResizable(true);
        columns.add(nameColumn);
        
        TreeViewerColumn pathColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
        pathColumn.getColumn().setText("Text");
        pathColumn.getColumn().setToolTipText( "message of the reason");
        pathColumn.getColumn().setWidth(100);
        ReasonViewLabelProvider viewTextLabelProvider = new ReasonViewLabelProvider( ReasonViewLabelProvider.KEY_MESSAGE, uiSupport, "reason-viewer");
		pathColumn.setLabelProvider(new DelegatingStyledCellLabelProvider( viewTextLabelProvider));
        pathColumn.getColumn().setResizable(true);
        columns.add(pathColumn);
		
        
		ColumnViewerToolTipSupport.enableFor(treeViewer);
		
		tree = treeViewer.getTree();
	    
		GridData layoutData = new GridData( SWT.FILL, SWT.FILL);			
		int ht = (tree.getItemHeight() * 3) + tree.getHeaderHeight();
		
    	Point computedSize = tree.computeSize(SWT.DEFAULT, ht);
		layoutData.heightHint = computedSize.y;    					
		layoutData.widthHint = computedSize.x;// * 2;
    	tree.setLayoutData(layoutData);
		
    	
		TreeViewerColumnResizer columnResizer = new TreeViewerColumnResizer();
    	columnResizer.setColumns( columns);		
    	columnResizer.setParent( treeComposite);
    	columnResizer.setTree( tree);    	
    	tree.addControlListener(columnResizer);
	
    	treeViewer.setInput( reasons);
    	treeViewer.expandAll();
	    
        composite.pack();	
		return composite;
	}
	
	public void dispose() {
		
	}
}
