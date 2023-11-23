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
package com.braintribe.devrock.zed.ui.viewer.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.services.IDisposable;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.devrock.api.storagelocker.StorageLockerSlots;
import com.braintribe.devrock.api.ui.commons.UiSupport;
import com.braintribe.devrock.api.ui.tree.TreeViewerColumnResizer;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.devrock.zarathud.model.common.FingerPrintNode;
import com.braintribe.devrock.zarathud.model.common.HasRelatedFingerPrint;
import com.braintribe.devrock.zarathud.model.common.Node;
import com.braintribe.devrock.zarathud.model.model.EnumEntityNode;
import com.braintribe.devrock.zarathud.model.model.GenericEntityNode;
import com.braintribe.devrock.zed.forensics.fingerprint.HasFingerPrintTokens;
import com.braintribe.devrock.zed.ui.ZedViewerCommons;
import com.braintribe.devrock.zed.ui.ZedViewingContext;
import com.braintribe.devrock.zed.ui.transposer.ModelAnalysisContentTransposer;
import com.braintribe.zarathud.model.forensics.FingerPrint;
import com.braintribe.zarathud.model.forensics.ModelForensicsResult;

public class ModelAnalysisViewer implements IMenuListener, IDisposable, HasFingerPrintTokens {
	private ContentProvider contentProvider;
	private TreeViewer treeViewer;
	private Tree tree;	
	
	private UiSupport uiSupport; 
	private ModelForensicsResult modelForensics;
	
	private ZedViewingContext context;
	
	private MenuManager mainMenuManager;
	private ImageDescriptor openFileImgDescr;
	private Image switchModeImage;
	private Image sortModeImage;
	
	private boolean entityCentric = DevrockPlugin.instance().storageLocker().getValue(StorageLockerSlots.SLOT_ZED_MODEL_INITIAL_VIEWMODE, false);
	private boolean sortRatingCentric = DevrockPlugin.instance().storageLocker().getValue(StorageLockerSlots.SLOT_ZED_MODEL_INITIAL_SORTMODE, false);
	
	public ModelAnalysisViewer( ZedViewingContext context) {
		this.context = context;		
	}
	
	@Configurable @Required
	public void setUiSupport(UiSupport uiSupport) {
		this.uiSupport = uiSupport;				
		openFileImgDescr= ImageDescriptor.createFromFile(ModelAnalysisViewer.class, "pasteFromClipboard.png");	
		switchModeImage = uiSupport.images().addImage("ma_switchMode", ModelAnalysisViewer.class, "rebuild_index.png");
		sortModeImage = uiSupport.images().addImage("ma_sortMode", ModelAnalysisViewer.class, "sort.png");
	}
	
	@Configurable @Required
	public void setModelForensics(ModelForensicsResult result) {
		this.modelForensics = result;
	}
	
	private List<Node> generateNodes( boolean entityCentric, boolean ratingCentric) {
		if (entityCentric) {
			return ModelAnalysisContentTransposer.transposePerOwner(context, modelForensics, sortRatingCentric).getChildren();
		}
		else {
			return ModelAnalysisContentTransposer.transposePerFingerPrints(context, modelForensics, sortRatingCentric).getChildren();
		}
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
        treeLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, true, false, 2, 1));

        Button sortButton = new Button( treeLabelComposite, SWT.None);
        sortButton.setLayoutData( new GridData( SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        sortButton.setImage(sortModeImage);
        sortButton.setToolTipText("changes how the top-level content is sorted: alphabetically or by rating");
        sortButton.addSelectionListener( new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				sortRatingCentric = !sortRatingCentric;
				contentProvider.setupFrom( generateNodes(entityCentric, sortRatingCentric));
				treeViewer.refresh();
			}
        	
		});
        
        
        
        Button switchViewButton = new Button( treeLabelComposite, SWT.None);
        switchViewButton.setLayoutData( new GridData( SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        switchViewButton.setImage(switchModeImage);
        switchViewButton.setToolTipText("changes the hierarchical order of the shown issues: centered on issues or on model entities");
        switchViewButton.addSelectionListener( new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				entityCentric = !entityCentric;
				contentProvider.setupFrom( generateNodes(entityCentric, sortRatingCentric));
				treeViewer.refresh();
			}
        	
		});
        
        
        
        // tree for display
        Composite treeComposite = new Composite( composite, SWT.BORDER);	
        treeComposite.setLayout(layout);
		treeComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 4, 2));
				
		contentProvider = new ContentProvider();
		contentProvider.setupFrom( generateNodes(entityCentric, sortRatingCentric));
		
		treeViewer = new TreeViewer( treeComposite, SWT.H_SCROLL | SWT.V_SCROLL);
		treeViewer.setContentProvider( contentProvider);
    	treeViewer.getTree().setHeaderVisible(true);
		
		// columns 
    	List<TreeViewerColumn> columns = new ArrayList<>();        	
    	
    	TreeViewerColumn nameColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
        nameColumn.getColumn().setText("Findings");
        nameColumn.getColumn().setToolTipText( "Findings of the model analysis");
        nameColumn.getColumn().setWidth(1000);
        
        ViewLabelProvider viewLabelProvider = new ViewLabelProvider();
        viewLabelProvider.setUiSupport(uiSupport);
        viewLabelProvider.setUiSupportStylersKey("zed-model-analysis-view");
                
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
	
    	treeViewer.setInput( generateNodes(entityCentric, sortRatingCentric));
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
	}

	@Override
	public void menuAboutToShow(IMenuManager mmgr) {
		if (context.getProject() == null)
			return; 
		ISelection selection = treeViewer.getSelection();
		 if(!selection.isEmpty()){
	        if (selection instanceof IStructuredSelection) {
	        	IStructuredSelection structuredSelection = (IStructuredSelection) selection;	        	
	        	Iterator<?> iter = structuredSelection.iterator();
	        	while (iter.hasNext()) {
	        		String qualifiedName = null, packageName = null, typeName = null;
	        		
	        		Object item = iter.next();
	        		if (item instanceof GenericEntityNode) {
	        			GenericEntityNode gn = (GenericEntityNode) item;
	        			qualifiedName = gn.getName();
	        			int p = qualifiedName.lastIndexOf('.');
	        			packageName = qualifiedName.substring(0, p);
	        			typeName = qualifiedName.substring(p+1);
	        		}
	        		else if (item instanceof EnumEntityNode) {
	        			EnumEntityNode en = (EnumEntityNode) item;
	        			qualifiedName = en.getName();
	        			int p = qualifiedName.lastIndexOf('.');
	        			packageName = qualifiedName.substring(0, p);
	        			typeName = qualifiedName.substring(p+1);
	        		}
	        		else if (item instanceof HasRelatedFingerPrint) {
	        			HasRelatedFingerPrint hrfp = (HasRelatedFingerPrint) item;
	        			FingerPrintNode fpn = hrfp.getRelatedFingerPrint();
	        			if (fpn == null) {
	        				// not all nodes seem to have a FingerPrint attached
	        				continue;
	        			}
	        			FingerPrint fp = fpn.getFingerPrint();

	        			packageName = fp.getSlots().get( PACKAGE);
	        			typeName = fp.getSlots().get(TYPE);
	        			if (packageName == null || typeName == null) {
	        				continue;	        				
	        			}
	        			qualifiedName = packageName + "." + typeName;	        			
	        		}
	        		
	        		final String actionPackageName = packageName;
	        		final String actionTypeName = typeName;
	        		
	        		Action openInEditor = new Action("open relevant file for: " + qualifiedName, openFileImgDescr) {
	        			@Override
	        			public void run() {
	        				ZedViewerCommons.openFile( context, actionPackageName, actionTypeName);		
	        			}								
	        			@Override
	        			public String getToolTipText() {
	        				return "opens corresponding file: " + actionPackageName + "." + actionTypeName;
	        			}
	        		}; 
	        		mainMenuManager.add( openInEditor);	        				        			

	        	}
	        }
		 }		
	}


			
	
}
