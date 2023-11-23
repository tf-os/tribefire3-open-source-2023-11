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
package com.braintribe.devrock.api.ui.viewers.artifacts.selector.editors.dependency;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;

import com.braintribe.devrock.api.ui.fonts.FontHandler;
import com.braintribe.devrock.api.ui.tree.TreeViewerColumnResizer;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;

public class MatchingDependencySelector implements SelectionListener {
	private List<CompiledDependencyIdentification> matchingArtifactIdentifications;
	private TreeViewer treeViewer;
	private ContentProvider contentProvider;
	private ViewLabelProvider viewLabelProvider = new ViewLabelProvider();
	private Tree tree;
	private Font initialFont;
	
		
	private Image deleteImage;
	private Button deleteButton;
	private Font bigFont;
	
	public MatchingDependencySelector() {
		ImageDescriptor imageDescriptor = ImageDescriptor.createFromFile( MatchingDependencySelector.class, "remove.gif");
		deleteImage = imageDescriptor.createImage();
	}
	
	public void setMatchingDependencyIdentifications( List<CompiledDependencyIdentification> matchingArtifactIdentifications) {
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
      
        
        // label 
 		Composite treeLabelComposite = new Composite( composite, SWT.NONE);
         treeLabelComposite.setLayout( layout);
         treeLabelComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4,1));
         
         Label treeLabel = new Label( treeLabelComposite, SWT.NONE);
         treeLabel.setText( tag);
         treeLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, true, false, 3, 1));
         bigFont = FontHandler.buildBigFont(parent.getDisplay(), parent.getFont());
         treeLabel.setFont(bigFont);
         
         deleteButton = new Button( treeLabelComposite, SWT.NONE);
         deleteButton.setLayoutData( new GridData( SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
         deleteButton.setImage(deleteImage);
         deleteButton.addSelectionListener(this);
         deleteButton.setToolTipText("remove the selected listed dependencies from the list");
         
 		
         // tree for display
        Composite treeComposite = new Composite( composite, SWT.BORDER);              
 		treeComposite.setLayout( new FillLayout());
 		treeComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 4, 20));
 		
 		treeViewer = new TreeViewer( treeComposite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI); 				
 		
 		contentProvider = new ContentProvider(); 		
 		treeViewer.setContentProvider( contentProvider);
 		
     	treeViewer.getTree().setHeaderVisible(true);
     	
     	treeViewer.addSelectionChangedListener( new ISelectionChangedListener() {		
			@Override
			public void selectionChanged(SelectionChangedEvent event) {				
			}
		});
     	
     	
     	ColumnViewerToolTipSupport.enableFor(treeViewer);
     	
     	// columns 
     	List<TreeViewerColumn> columns = new ArrayList<>();        	
     	
     	TreeViewerColumn nameColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
        nameColumn.getColumn().setText("Dependency");
        nameColumn.getColumn().setToolTipText( "id of the dependency");
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
        
        return composite;
	}

	public List<CompiledDependencyIdentification> getSelection() {
		return matchingArtifactIdentifications;
	}
	
	public void addToSelection( CompiledDependencyIdentification cdi) {
		if (matchingArtifactIdentifications == null) {
			matchingArtifactIdentifications = new ArrayList<>();
		}
		matchingArtifactIdentifications.add( cdi);
		
		contentProvider.setInput( matchingArtifactIdentifications);
     	treeViewer.setInput( matchingArtifactIdentifications);
		treeViewer.refresh();
	}
	
	public void addToSelection( List<CompiledDependencyIdentification> cdis) {
		if (matchingArtifactIdentifications == null) {
			matchingArtifactIdentifications = new ArrayList<>();
		}
		cdis.stream().forEach( matchingArtifactIdentifications::add);				
		contentProvider.setInput( matchingArtifactIdentifications);
     	treeViewer.setInput( matchingArtifactIdentifications);
		treeViewer.refresh();
	}
	

	public void setInitialFont(Font initialFont) {
		this.initialFont = initialFont;				
	}
	
	
	
	@Override
	public void widgetDefaultSelected(SelectionEvent arg0) {}

	@Override
	public void widgetSelected(SelectionEvent event) {
		if (event.widget == deleteButton) {
			List<CompiledDependencyIdentification> currentlySelected = extractSelection();
			matchingArtifactIdentifications.removeAll(currentlySelected);
			contentProvider.setInput( matchingArtifactIdentifications);
	     	treeViewer.setInput( matchingArtifactIdentifications);
			treeViewer.refresh();
		}		
	}

	/**
	 * @return - all {@link CompiledDependencyIdentification} that are currently selected
	 */
	private List<CompiledDependencyIdentification> extractSelection() {
		@SuppressWarnings("rawtypes")
		Iterator iterator = treeViewer.getStructuredSelection().iterator();
		List<CompiledDependencyIdentification> selection = new ArrayList<CompiledDependencyIdentification>();
		while (iterator.hasNext()) {
			Object object = iterator.next();
			if (object != null && object instanceof CompiledDependencyIdentification)
				selection.add( (CompiledDependencyIdentification) object);
		}
		return selection;
	}
		
	
	public void dispose() {
		viewLabelProvider.dispose();
		deleteImage.dispose();
		bigFont.dispose();
	}
}
