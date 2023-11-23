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
package com.braintribe.devrock.api.ui.viewers.artifacts.selector.editors.artifact;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.devrock.api.ui.fonts.FontHandler;
import com.braintribe.devrock.api.ui.tree.TreeViewerColumnResizer;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;

public class MatchingArtifactSelector {
	private List<CompiledArtifactIdentification> matchingArtifactIdentifications;
	private TreeViewer treeViewer;
	private ContentProvider contentProvider;
	private ViewLabelProvider viewLabelProvider = new ViewLabelProvider();
	private Tree tree;
	private Font initialFont;
	private List<CompiledArtifactIdentification> selection = new ArrayList<>();
	private Font bigFont;
	private Consumer<Boolean> validSelectionListener;  
	
	private boolean expectTwoSelections = false;
	
	@Configurable
	public void setExpectTwoSelections(boolean expectTwoSelections) {
		this.expectTwoSelections = expectTwoSelections;
	}
	
	@Configurable @Required
	public void setValidSelectionListener(Consumer<Boolean> validSelectionListener) {
		this.validSelectionListener = validSelectionListener;
	}
	
	public void setMatchingArtifactIdentifications(CompiledDependencyIdentification cdi,  List<CompiledArtifactIdentification> matchingArtifactIdentifications) {
		matchingArtifactIdentifications.sort( new Comparator<CompiledArtifactIdentification>() {

			@Override
			public int compare(CompiledArtifactIdentification o1, CompiledArtifactIdentification o2) {			
				return o1.compareTo(o2)*-1;
			}			
		});
		if (matchingArtifactIdentifications.size() > 0) {
			if (cdi != null) {
				tree.setToolTipText("Select one of the possible matching artifacts of: " + cdi.asString());
			}
			else { 
				tree.setToolTipText("Select one of the possible matching artifacts");
			}
		}
		else {
			tree.setToolTipText("No artifacts found for dependency: " + cdi.asString());
		}
		
		this.matchingArtifactIdentifications = matchingArtifactIdentifications;
		contentProvider.setInput(matchingArtifactIdentifications);
		treeViewer.setInput(matchingArtifactIdentifications);
		treeViewer.refresh();
	}
	
	public Composite createControl( Composite parent, String tag) {

		final Composite composite = new Composite(parent, SWT.NONE);
				
		int nColumns= 4;
        GridLayout layout= new GridLayout();
        layout.numColumns = nColumns;
        composite.setLayout( layout);
        
        layout.verticalSpacing=2;                
        //composite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true));
        
        // label 
 		Composite treeLabelComposite = new Composite( composite, SWT.NONE);
         treeLabelComposite.setLayout( layout);
         treeLabelComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4,1));
         
         Label treeLabel = new Label( treeLabelComposite, SWT.NONE);
         treeLabel.setText( tag);
         treeLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, true, false, 4, 1));
         bigFont = FontHandler.buildBigFont(parent.getDisplay(), parent.getFont());
         treeLabel.setFont(bigFont);
         
 		
         // tree for display
        Composite treeComposite = new Composite( composite, SWT.BORDER);              
 		treeComposite.setLayout( new FillLayout());
 		treeComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 4, 20));
 		
 		if (expectTwoSelections) {
 			treeViewer = new TreeViewer( treeComposite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
 		}
 		else {
 			treeViewer = new TreeViewer( treeComposite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE); 			
 		}
 		 				 		
 		contentProvider = new ContentProvider(); 		
 		treeViewer.setContentProvider( contentProvider);
 		
     	treeViewer.getTree().setHeaderVisible(true);
     	
     	treeViewer.addSelectionChangedListener( new ISelectionChangedListener() {		
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				// iterate
				Iterator<?> iter = event.getStructuredSelection().iterator();
				while (iter.hasNext()) {
					Object object = iter.next();
					if (object != null && object instanceof CompiledArtifactIdentification) {						
						selection.add((CompiledArtifactIdentification) object);
					}					
				}
				// 	
				boolean readyForSelection = false;
				if (expectTwoSelections) { 					
					if (selection.size() == 2) {
						readyForSelection = true;				
					}					
				}
				else {
					if (selection.size() > 0 ) {
						readyForSelection = true;				
					}					
				}
				if (validSelectionListener != null) {
					validSelectionListener.accept( readyForSelection);
				}				 
			}
		});
     	
     	
     	ColumnViewerToolTipSupport.enableFor(treeViewer);
     	
     	// columns 
     	List<TreeViewerColumn> columns = new ArrayList<>();        	
     	
     	TreeViewerColumn nameColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
        nameColumn.getColumn().setText("Artifact");
        nameColumn.getColumn().setToolTipText( "id of the artifact");
        nameColumn.getColumn().setWidth(100);
        
        viewLabelProvider.setInitialFont(initialFont);
		nameColumn.setLabelProvider(new DelegatingStyledCellLabelProvider( viewLabelProvider));
        nameColumn.getColumn().setResizable(true);                   
        columns.add(nameColumn);
     
 	        
 		ColumnViewerToolTipSupport.enableFor(treeViewer);
 		
 		tree = treeViewer.getTree();
 	     	
 		TreeViewerColumnResizer columnResizer = new TreeViewerColumnResizer();
     	columnResizer.setColumns( columns);		
     	columnResizer.setParent( treeComposite);
     	columnResizer.setTree( tree);    	
     	tree.addControlListener(columnResizer);
     	
     	contentProvider.setInput( matchingArtifactIdentifications);
     	treeViewer.setInput( matchingArtifactIdentifications);
        
     //	composite.pack();
        return composite;
	}

	public List<CompiledArtifactIdentification> getSelection() {
		return selection;
	} 

	public void setInitialFont(Font initialFont) {
		this.initialFont = initialFont;				
	}
	
	public void dispose() {
		bigFont.dispose();
	}
}
