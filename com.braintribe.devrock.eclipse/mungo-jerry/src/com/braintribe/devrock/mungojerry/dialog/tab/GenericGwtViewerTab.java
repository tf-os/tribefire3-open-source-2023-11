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
package com.braintribe.devrock.mungojerry.dialog.tab;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.commons.plugin.ui.tree.TreeColumnResizer;


public class GenericGwtViewerTab implements Listener {
	protected ParentPage parentPage;
	protected AnalysisController analysisController;
	protected Display display;
	

	protected Tree tree;
	private String [] columnNames; //new String [] {"Module name", "Package"};
	private  int [] columnWeights; //new int [] {400, 200};
	private List<TreeColumn> columns = new ArrayList<TreeColumn>();
	
	public void setColumnNames(String[] columnNames) {
		this.columnNames = columnNames;
	}
	public void setColumnWeights(int[] columnWeights) {
		this.columnWeights = columnWeights;
	}

	public GenericGwtViewerTab(Display display){
		this.display = display;			
	}
	
	@Configurable @Required
	public void setAnalysisController(AnalysisController analysisController) {
		this.analysisController = analysisController;
	}
	
	@Configurable @Required
	public void setParentPage(ParentPage parentPage) {
		this.parentPage = parentPage;
	}
	
	
	public Composite createControl( Composite parent){
		final Composite composite = new Composite( parent, SWT.NONE);		               
		composite.setLayout( new FillLayout());
		tree = new Tree ( composite, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		tree.setHeaderVisible( true);
		
		for (int i = 0; i < columnNames.length; i++) {
			TreeColumn treeColumn = new TreeColumn( tree, SWT.LEFT);
			treeColumn.setText( columnNames[i]);
			treeColumn.setWidth( columnWeights[i]);
			treeColumn.setResizable( true);		
			columns.add( treeColumn);
		}
		
		//tree.setLayoutData(new GridData( SWT.FILL, SWT.FILL));
													
       composite.pack();
			
			
		TreeColumnResizer columnResizer = new TreeColumnResizer();
		columnResizer.setColumns( columns);
		columnResizer.setColumnWeights( columnWeights);
		columnResizer.setParent( parent);
		columnResizer.setTree( tree);
		tree.addControlListener(columnResizer);
		
		tree.addListener(SWT.Expand, this);
		//treeExpander.addListener( this);
					
		return composite;      
	}
	
	public void setVisible( boolean visible) {}
	
	@Override
	public void handleEvent(Event event) {}
		
	public void dispose() {}
}
