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
package com.braintribe.devrock.api.ui.viewers.repository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;

import com.braintribe.devrock.api.ui.commons.UiSupport;
import com.braintribe.devrock.api.ui.fonts.FontHandler;
import com.braintribe.devrock.api.ui.tree.TreeViewerColumnResizer;
import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.devrock.preferences.pages.cfg.dialog.RepositoryDisplayDialog;
import com.braintribe.logging.Logger;

/**
 * a viewer that can display a {@link RepositoryConfiguration}
 * 
 * @author pit
 *
 */
public class RepositoryViewer {
	private static Logger log = Logger.getLogger(RepositoryViewer.class);
	public static String REPOSITORY_VIEWER_UISUPPORT_KEY = "devrock:repository-viewer";

	private RepositoryConfiguration configuration;
	private Image cacheColumnImage;
	private Image artifactFilterColumnImage;
	private Image updateColumnImage;

	private UiSupport uiSupport = DevrockPlugin.instance().uiSupport();

	private Font bigFont;
	
	private ViewLabelProvider updateColumnLabelProvider;
	private ViewLabelProvider filterColumnLabelProvider;
	private ViewLabelProvider cacheColumnLabelProvider;
	private ViewLabelProvider pathColumnLabelProvider;
	private ViewLabelProvider typeColumnLabelProvider;
	private ViewLabelProvider nameColumnLabelProvider;
	
	
	public RepositoryViewer( RepositoryConfiguration configuration) {
		this.configuration = configuration;
				
		artifactFilterColumnImage = uiSupport.images().addImage("filterColumn", RepositoryViewer.class, "filter_on.gif");							
		cacheColumnImage = uiSupport.images().addImage("cacheColumn", RepositoryViewer.class, "dirty.gif");				
		updateColumnImage =uiSupport.images().addImage("dynamicColumn", RepositoryViewer.class, "rebuild_index.gif");		
		
	}
	
	public Composite createControl( Composite parent, String tag) {
		
		log.trace("repository viewer : creating dialog area ... ");
		long before = System.nanoTime();
		
		bigFont = FontHandler.buildBigFont(parent.getDisplay(), parent.getFont());
		
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 4;
		composite.setLayout(layout);
		
	
		// label 
		Composite treeLabelComposite = new Composite( composite, SWT.NONE);
        treeLabelComposite.setLayout( layout);
        treeLabelComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4,1));
        
        Label treeLabel = new Label( treeLabelComposite, SWT.NONE);
        treeLabel.setText( tag);
        treeLabel.setFont(bigFont);
        treeLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, true, false, 4, 1));
		
	
        // tree
        Composite treeComposite = new Composite(composite, SWT.NONE);    	
		treeComposite.setLayout(layout);
		treeComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 4, 10));
		
		// cache
		String cachePath = configuration.getCachePath();
		if (cachePath != null) {
			
			File cache = new File( cachePath);
			
			String canonicalPath = null;
			
			try {
				canonicalPath = cache.getCanonicalPath();
			} catch (IOException e1) {
				canonicalPath = cachePath;
			}
			
			
			Composite cacheLabelComposite = new Composite( treeComposite, SWT.NONE);
			cacheLabelComposite.setLayout( layout);
			cacheLabelComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4,1));
			
			Label cacheLabel = new Label( cacheLabelComposite, SWT.NONE);
			cacheLabel.setText( "cache: ");
			cacheLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 1, 1));
			cacheLabel.setFont(bigFont);
			
			Text cacheText = new Text( cacheLabelComposite, SWT.NONE);
			cacheText.setText( canonicalPath.toString());
			cacheText.setLayoutData(new GridData( SWT.FILL, SWT.CENTER, true, false, 3, 1));
			cacheText.setToolTipText( "Path to the filesystem repository that acts as a cache");
			
			cacheText.setToolTipText("actual value as defined in the configuration: " + cachePath);
		}
		
		
		
		
		TreeViewer treeViewer = new TreeViewer( treeComposite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
    	List<Repository> repositories = configuration.getRepositories();
		treeViewer.setContentProvider( new ContentProvider( repositories));
    	treeViewer.getTree().setHeaderVisible(true);
    	
    	ColumnViewerToolTipSupport.enableFor(treeViewer);
    	
    	// columns 
    	List<TreeViewerColumn> columns = new ArrayList<>();        	
    	TreeViewerColumn nameColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
        nameColumn.getColumn().setText("Name");
        nameColumn.getColumn().setToolTipText( "Name of the repository");
        nameColumn.getColumn().setWidth(100);
        nameColumnLabelProvider = new ViewLabelProvider( Repository.name, parent.getFont());
		nameColumn.setLabelProvider(new DelegatingStyledCellLabelProvider( nameColumnLabelProvider));
        nameColumn.getColumn().setResizable(true);
        columns.add(nameColumn);
        
        //type
        TreeViewerColumn changesUrlColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
        changesUrlColumn.getColumn().setText("Type");
        changesUrlColumn.getColumn().setToolTipText( "Type of the repository");
        changesUrlColumn.getColumn().setWidth(120);
        typeColumnLabelProvider = new ViewLabelProvider( "instance", parent.getFont());
		changesUrlColumn.setLabelProvider(new DelegatingStyledCellLabelProvider( typeColumnLabelProvider));
        changesUrlColumn.getColumn().setResizable(true);
        columns.add(changesUrlColumn);

        // path
        TreeViewerColumn urlColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
        urlColumn.getColumn().setText("Path");
        urlColumn.getColumn().setToolTipText( "Path (or URL) of the repository");
        urlColumn.getColumn().setWidth(250);
        pathColumnLabelProvider = new ViewLabelProvider( "url", parent.getFont());
		urlColumn.setLabelProvider(new DelegatingStyledCellLabelProvider( pathColumnLabelProvider));
        urlColumn.getColumn().setResizable(true);
        columns.add(urlColumn);
       
                         
        TreeViewerColumn cachableColumn = new TreeViewerColumn(treeViewer, SWT.NONE);        
        cachableColumn.getColumn().setToolTipText( "Whether the repository should be cached");
        cachableColumn.getColumn().setImage(cacheColumnImage);
        cachableColumn.getColumn().setWidth(30);
        cacheColumnLabelProvider = new ViewLabelProvider( Repository.cachable, parent.getFont());
		cachableColumn.setLabelProvider(new DelegatingStyledCellLabelProvider( cacheColumnLabelProvider));
        cachableColumn.getColumn().setResizable(true);
        columns.add(cachableColumn);
                   
        TreeViewerColumn artifactFilterColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
        artifactFilterColumn.getColumn().setImage( artifactFilterColumnImage);
        artifactFilterColumn.getColumn().setToolTipText("artifact filter");
        artifactFilterColumn.getColumn().setWidth(30);
        filterColumnLabelProvider = new ViewLabelProvider( Repository.artifactFilter, parent.getFont());
		artifactFilterColumn.setLabelProvider(new DelegatingStyledCellLabelProvider( filterColumnLabelProvider));
        artifactFilterColumn.getColumn().setResizable(true);
        columns.add(artifactFilterColumn);
        
        TreeViewerColumn timespanColumn = new TreeViewerColumn(treeViewer, SWT.NONE);      
        timespanColumn.getColumn().setImage(  updateColumnImage);
        timespanColumn.getColumn().setToolTipText("update time span");
        timespanColumn.getColumn().setWidth(60);
        updateColumnLabelProvider = new ViewLabelProvider( Repository.updateTimespan, parent.getFont());
		timespanColumn.setLabelProvider(new DelegatingStyledCellLabelProvider( updateColumnLabelProvider));
        timespanColumn.getColumn().setResizable(true);
        columns.add(timespanColumn);

        
        
		treeViewer.setInput( repositories);
		
		Tree tree = treeViewer.getTree();
	    
		GridData layoutData = new GridData( SWT.FILL, SWT.FILL);			
		int ht = (tree.getItemHeight() * 10) + tree.getHeaderHeight();
		
    	Point computedSize = tree.computeSize(SWT.DEFAULT, ht);
		layoutData.heightHint = computedSize.y;    					
		layoutData.widthHint = (int) Math.round(computedSize.x * 1.25);
    	tree.setLayoutData(layoutData);
		
		TreeViewerColumnResizer columnResizer = new TreeViewerColumnResizer();
    	columnResizer.setColumns( columns);		
    	columnResizer.setParent( treeComposite);
    	columnResizer.setTree( tree);    	
    	tree.addControlListener(columnResizer);
    	
    	Button detailsButton = new Button(composite, SWT.NONE);
    	detailsButton.setText( "details");
    	detailsButton.addSelectionListener( new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// popup display dialog 
				TreeItem[] selection = tree.getSelection();
				if (selection == null || selection.length == 0)
					return;
				TreeItem item = selection[0];
				String repositoryId = item.getText(0);
				Repository selectedRepository = repositories.stream().filter( r -> r.getName().equals(repositoryId)).findFirst().orElse(null);
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				RepositoryDisplayDialog dialog = new RepositoryDisplayDialog(shell);
				dialog.setRepository(selectedRepository);
				dialog.open();										
			}    	
		});
				
		composite.pack();	
		
		long after = System.nanoTime();
		log.trace("create dialog area took [" + ((after-before) / 1E6) + "] ms");
	    
		return composite;
	}
	

	public void dispose() {		
		nameColumnLabelProvider.dispose();
		typeColumnLabelProvider.dispose();
		pathColumnLabelProvider.dispose();
		cacheColumnLabelProvider.dispose();
		filterColumnLabelProvider.dispose();
		updateColumnLabelProvider.dispose();
		bigFont.dispose();
	
	}
}
