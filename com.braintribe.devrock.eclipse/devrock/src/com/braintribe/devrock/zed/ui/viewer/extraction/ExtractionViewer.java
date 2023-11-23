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
package com.braintribe.devrock.zed.ui.viewer.extraction;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.services.IDisposable;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.devrock.api.ui.commons.UiSupport;
import com.braintribe.devrock.api.ui.tree.TreeViewerColumnResizer;
import com.braintribe.devrock.zarathud.model.common.Node;
import com.braintribe.devrock.zarathud.model.extraction.ExtractionNode;
import com.braintribe.devrock.zed.forensics.fingerprint.HasFingerPrintTokens;

public class ExtractionViewer implements IMenuListener, IDisposable, HasFingerPrintTokens {
	private ContentProvider contentProvider;
	private TreeViewer treeViewer;
	private Tree tree;	
	
	private UiSupport uiSupport;
	private ZedExtractionViewerContext context;
	
	private MenuManager mainMenuManager;
	
	private Node artifactNode;
	private ViewLabelProvider viewLabelProvider;
	
	
	public ExtractionViewer( ZedExtractionViewerContext context) {
		this.context = context;		
	}
	
	@Configurable @Required
	public void setUiSupport(UiSupport uiSupport) {
		this.uiSupport = uiSupport;				
	}
	
	@Configurable @Required
	public void setArtifactNode(Node node) {
		artifactNode = node;
	}
	
	private List<Node> generateNodes() {
		return artifactNode.getChildren();
	}
	
	private List<List<ExtractionNode>> getTreePathForNode( ExtractionNode node) {
		List<List<ExtractionNode>> result = new ArrayList<>();
		result.add( node.getTreepathElements());
		ExtractionNode parent = node.getParent();
		while (parent != null) {
			result.add( parent.getTreepathElements());
			parent = parent.getParent();
		}
		return result;
	}
	
	private void expandToNode( ExtractionNode node) {
		List<List<ExtractionNode>> pathsForNode = getTreePathForNode(node);
		for (int i = pathsForNode.size() - 1; i >= 0; i--) {
			treeViewer.setExpandedElements( pathsForNode.get(i).toArray());			
		}		
	}
	
	public void select(ExtractionNode node) {
		expandToNode(node);
		treeViewer.setSelection( new StructuredSelection(node));		
	}
	
	public void select( List<ExtractionNode> nodes) {		
		for (ExtractionNode en : nodes) {
			expandToNode(en);
		}
		treeViewer.setSelection(new StructuredSelection( nodes));				
	}
	
	/**
	 * @param parent
	 * @param tag
	 * @return
	 */
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
        treeLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, true, false, 3, 1));
        
        // tree for display
        Composite treeComposite = new Composite( composite, SWT.BORDER);	
        treeComposite.setLayout(layout);
		treeComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 4, 2));
				
		contentProvider = new ContentProvider();
		contentProvider.setupFrom( generateNodes());
		
		treeViewer = new TreeViewer( treeComposite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
		treeViewer.setContentProvider( contentProvider);
    	treeViewer.getTree().setHeaderVisible(true);
		
		// columns 
    	List<TreeViewerColumn> columns = new ArrayList<>();        	
    	
    	TreeViewerColumn nameColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
        nameColumn.getColumn().setText("Extraction");
        nameColumn.getColumn().setToolTipText( "Extraction data");
        nameColumn.getColumn().setWidth(1000);
        
        viewLabelProvider = new ViewLabelProvider();
        viewLabelProvider.setUiSupport(uiSupport);
        viewLabelProvider.setUiSupportStylersKey("zed-extraction-view");
                
		nameColumn.setLabelProvider(new DelegatingStyledCellLabelProvider( viewLabelProvider));
        nameColumn.getColumn().setResizable(true);
        columns.add(nameColumn);
               
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
	
    	treeViewer.setInput( generateNodes());
    	//treeViewer.expandAll();    	
	    
        composite.pack();
        
        mainMenuManager = new MenuManager();
		mainMenuManager.setRemoveAllWhenShown( true);
		mainMenuManager.addMenuListener( this);
		
		// b) attach
		Control control = treeViewer.getControl();
		Menu menu = mainMenuManager.createContextMenu(control);
		control.setMenu( menu);
		
	
		return composite;
	}

	@Override
	public void dispose() {
		viewLabelProvider.dispose();
	}

	@Override
	public void menuAboutToShow(IMenuManager arg0) {		
	} 
	

	
}
