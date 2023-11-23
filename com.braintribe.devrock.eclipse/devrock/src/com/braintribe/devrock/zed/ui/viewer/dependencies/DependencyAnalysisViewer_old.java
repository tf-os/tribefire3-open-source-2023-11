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
package com.braintribe.devrock.zed.ui.viewer.dependencies;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.services.IDisposable;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.devrock.api.clipboard.ArtifactToClipboardExpert;
import com.braintribe.devrock.api.storagelocker.StorageLockerSlots;
import com.braintribe.devrock.api.ui.commons.UiSupport;
import com.braintribe.devrock.api.ui.tree.TreeViewerColumnResizer;
import com.braintribe.devrock.eclipse.model.actions.VersionModificationAction;
import com.braintribe.devrock.eclipse.model.identification.RemoteCompiledDependencyIdentification;
import com.braintribe.devrock.importer.dependencies.listener.ParallelRepositoryScanner;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.devrock.zarathud.model.dependency.DependencyAnalysisNode;
import com.braintribe.devrock.zarathud.model.dependency.DependencyKind;
import com.braintribe.devrock.zed.ui.ZedViewingContext;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;

/**
 * a viewer for zed's dependency analysis 
 * shows the different kinds of result per dependency
 * missing dependencies can be inserted with the 3 different modes
 * 
 * @author pit
 *
 */
public class DependencyAnalysisViewer_old implements IMenuListener, IDisposable, SelectionListener, ISelectionChangedListener {
	private static final String TT_INSERT_UNTOUCHED = " with a version exactly as the one of the artifact in the classpath";
	private static final String TT_INSERT_RANGED = " with a version-ranged based on the standard major/minor range";
	private static final String TT_INSERT_REFERENCED = " with a variable expression based on the group id of the artifact";
	private static final String TT_INSERT_CURRENT = "with a version as set by default";
	
	private ContentProvider contentProvider;
	private TreeViewer treeViewer;
	private Tree tree;	
	
	private UiSupport uiSupport; 
	private List<DependencyAnalysisNode> nodes;
	

	private MenuManager mainMenuManager;	
	private ImageDescriptor insertDependencyImgDescr;
	
	private ParallelRepositoryScanner scanController = DevrockPlugin.instance().repositoryImportController();
	private ViewLabelProvider viewLabelProvider;
	private ZedViewingContext context;
	private Button insertWithDefault;
	private Button insertReferenced;
	private Button insertRangified;
	private Button insertUntouched;
	private Set<Button> insertButtons = new HashSet<>();
	private Label buttonLabel;
	
	public DependencyAnalysisViewer_old(ZedViewingContext context) {
		this.context = context;
	}

	@Configurable @Required
	public void setUiSupport(UiSupport uiSupport) {
		this.uiSupport = uiSupport;			
		insertDependencyImgDescr= ImageDescriptor.createFromFile(DependencyAnalysisViewer_old.class, "pasteFromClipboard.png");
	}
	
	@Configurable @Required
	public void setNodes(List<DependencyAnalysisNode> nodes) {
		this.nodes = nodes;
	}
	
	/**
	 * build the UI
	 * @param parent - parent {@link Composite} to integrate into 
	 * @param tag - the text for the main label of the composite returned 
	 * @return - the composite containing the full UI
	 */
	public Composite createControl( Composite parent, String tag) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 4;
		composite.setLayout(layout);
	
		
		// label..
		Composite treeLabelComposite = new Composite( composite, SWT.NONE);
        treeLabelComposite.setLayout( layout);
        treeLabelComposite.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, true, false, 4,1));
        
        Label treeLabel = new Label( treeLabelComposite, SWT.NONE);
        treeLabel.setText( tag);
        treeLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, true, false, 4, 1));
        
        // tree for display
        Composite treeComposite = new Composite( composite, SWT.BORDER);	
        treeComposite.setLayout(layout);
		treeComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 4, 2));
				
		contentProvider = new ContentProvider();
		contentProvider.setupFrom( nodes);
		
		treeViewer = new TreeViewer( treeComposite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
		treeViewer.setContentProvider( contentProvider);
    	treeViewer.getTree().setHeaderVisible(true);
		
		// columns 
    	List<TreeViewerColumn> columns = new ArrayList<>();        	
    	
    	TreeViewerColumn nameColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
        nameColumn.getColumn().setText("Dependency");
        nameColumn.getColumn().setToolTipText( "dependency, either declared, missing or excessive");
        nameColumn.getColumn().setWidth(1000);
        
        viewLabelProvider = new ViewLabelProvider();
        viewLabelProvider.setUiSupport(uiSupport);
        viewLabelProvider.setUiSupportStylersKey("zed-dependency-view");
                
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
    	
    	treeViewer.addSelectionChangedListener( this);
		
  
		TreeViewerColumnResizer columnResizer = new TreeViewerColumnResizer();
    	columnResizer.setColumns( columns);		
    	columnResizer.setParent( treeComposite);
    	columnResizer.setTree( tree);    	
    	tree.addControlListener(columnResizer);
	
    	treeViewer.setInput( nodes);
    	//treeViewer.expandAll();
    	
    	mainMenuManager = new MenuManager();
		mainMenuManager.setRemoveAllWhenShown( true);
		mainMenuManager.addMenuListener( this);
		
		// b) attach
		Control control = treeViewer.getControl();
		Menu menu = mainMenuManager.createContextMenu(control);
		control.setMenu( menu);
		
		if (context.getProject() != null) {
			// buttons 
			Composite buttonComposite = new Composite( composite, SWT.NONE);
	        buttonComposite.setLayout( layout);
	        buttonComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4,1));
	        
	        buttonLabel = new Label( buttonComposite, SWT.NONE);
	        buttonLabel.setText( "insert selected missing dependencies with following options for the version:");
	        buttonLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, true, false, 4, 1));
	        buttonLabel.setEnabled(false);
	
	        VersionModificationAction currentVma = DevrockPlugin.envBridge().storageLocker().getValue( StorageLockerSlots.SLOT_CLIP_PASTE_MODE, VersionModificationAction.referenced);
	 
	        insertWithDefault = new Button(buttonComposite, SWT.NONE);
	        insertWithDefault.setText(" with default settings (" + currentVma + ") ");
	        insertWithDefault.setLayoutData(new GridData( SWT.FILL, SWT.CENTER, true, true, 1,1));
	        insertWithDefault.setToolTipText(TT_INSERT_CURRENT);
	        insertButtons.add(insertWithDefault);
	        
	
	        insertReferenced = new Button(buttonComposite, SWT.NONE);
	        insertReferenced.setText(" with a variable (referenced) ");
	        insertReferenced.setLayoutData(new GridData( SWT.FILL, SWT.CENTER, true, true, 1,1));
	        insertReferenced.setToolTipText(TT_INSERT_REFERENCED);
	        insertButtons.add( insertReferenced);
	        
	        insertRangified = new Button(buttonComposite, SWT.NONE);
	        insertRangified.setText(" with a standard range ");
	        insertRangified.setLayoutData(new GridData( SWT.FILL, SWT.CENTER, true, true, 1,1));
	        insertRangified.setToolTipText(TT_INSERT_RANGED);
	        insertButtons.add( insertRangified);
	        
	        insertUntouched = new Button(buttonComposite, SWT.NONE);
	        insertUntouched.setText(" as is ");
	        insertUntouched.setLayoutData(new GridData( SWT.FILL, SWT.CENTER, true, true, 1,1));
	        insertUntouched.setToolTipText(TT_INSERT_UNTOUCHED);
	        insertButtons.add(insertUntouched);
		}
		
		        
        composite.pack();	
        
        // no selection, no insert
        if (insertButtons.size() > 0) {
        	toggleInsertionMenu(false);
        }
	
		return composite;
	}
	
	private void toggleInsertionMenu(boolean activate) {
		insertButtons.stream().forEach( b -> b.setEnabled( activate));		
	}
	
	/**
	 * identify the {@link DependencyAnalysisNode} of the current selection in the tree viewer
	 * @param selection - the {@link ISelection}
	 * @return - a {@link List} of {@link DependencyAnalysisNode} that can be inserted, i.e
	 * have the {@link DependencyKind#missing}
	 */
	private List<DependencyAnalysisNode> getSelectedInsertionCapableNode(ISelection selection) {
        if(!selection.isEmpty()){
        	if (selection instanceof IStructuredSelection) {
        		IStructuredSelection structuredSelection = (IStructuredSelection) selection;
        		List<DependencyAnalysisNode> nodes = new ArrayList<>(structuredSelection.size());
        		Iterator<?> iter = structuredSelection.iterator();
        		while (iter.hasNext()) {
        			Object item = iter.next();
        			if (item instanceof DependencyAnalysisNode) {
        				DependencyAnalysisNode selectedNode = (DependencyAnalysisNode) item;
        				if (
        						selectedNode.getKind() == DependencyKind.missing || 
        						(selectedNode.getKind() == DependencyKind.forward && selectedNode.getIncompleteForwardReference())
        					) {
        					nodes.add(selectedNode);
        				}
        			}        		        			
        		}
        		return nodes;
        	}              	
        }
        return null;
    	
	}

	@Override
	public void menuAboutToShow(IMenuManager manager) {
		// must be an analysis of a project
		if (context.getProject() == null) {
			toggleInsertionMenu(false);
			return;
		}
		// must have at least some dependencies to insert
		List<DependencyAnalysisNode> nodes = getSelectedInsertionCapableNode( treeViewer.getSelection());
		if (nodes == null || nodes.size() == 0) {
			toggleInsertionMenu(false);
			return;
		}
		
		toggleInsertionMenu(true);
		int numNodes = nodes.size();
		
		String actionNamePrefix;
		if (numNodes == 1) {
			DependencyAnalysisNode dan = nodes.get(0);
			actionNamePrefix = "Insert " + dan.getIdentification().asString(); 
		}
		else {
			actionNamePrefix = "Insert " + numNodes + " dependencies";
		}
		final VersionModificationAction currentVma = DevrockPlugin.envBridge().storageLocker().getValue( StorageLockerSlots.SLOT_CLIP_PASTE_MODE, VersionModificationAction.referenced);

		// insert using the current mode
		Action insertCurrent = new Action( actionNamePrefix + " using default mode (" + currentVma + ")", insertDependencyImgDescr) {
			@Override
			public void run() {
				insertDependencies(nodes, currentVma);		
			}								
			@Override
			public String getToolTipText() {
				String t;
				switch ( currentVma) {
					case rangified:
						t = TT_INSERT_RANGED;
						break;
					case referenced:
						t = TT_INSERT_REFERENCED;
						break;
					case untouched:
					default:
						t = TT_INSERT_UNTOUCHED;
						break;				
				}
				return actionNamePrefix + t;
			}
		}; 
		mainMenuManager.add(insertCurrent);
		mainMenuManager.add( new Separator());
		
		// insert dependency per reference
		Action insertReferenced = new Action(actionNamePrefix + " with a version variable (referenced)", insertDependencyImgDescr) {
			@Override
			public void run() {
				insertDependencies(nodes, VersionModificationAction.referenced);		
			}								
			@Override
			public String getToolTipText() {
				return actionNamePrefix + TT_INSERT_REFERENCED;
			}
		}; 
		mainMenuManager.add(insertReferenced);
		
		// insert dependency rangified
		Action insertRangified = new Action(actionNamePrefix + " with a default range (rangified)", insertDependencyImgDescr) {
			@Override
			public void run() {
				insertDependencies(nodes, VersionModificationAction.rangified);		
			}								
			@Override
			public String getToolTipText() {
				return actionNamePrefix + TT_INSERT_RANGED;
			}
		}; 
		mainMenuManager.add(insertRangified);
		
		// insert dependency untouched
		Action insertUntouched = new Action(actionNamePrefix + " as is (untouched)", insertDependencyImgDescr) {
			@Override
			public void run() {
				insertDependencies(nodes, VersionModificationAction.untouched);		
			}								
			@Override
			public String getToolTipText() {
				return actionNamePrefix + TT_INSERT_UNTOUCHED;
			}
		}; 
		mainMenuManager.add(insertUntouched);							
	}	
	/**
	 * insert the {@link DependencyAnalysisNode} as dependencies into the pom
	 * @param nodes - the {@link DependencyAnalysisNode} to insert
	 * @param vma - the {@link VersionModificationAction} to use
	 */
	private void insertDependencies(List<DependencyAnalysisNode> nodes, VersionModificationAction vma) {
		for (DependencyAnalysisNode node : nodes) {
			// such nodes can only be inserted with a groupId-derived variable reference as it has no version specified
			if (node.getIncompleteForwardReference()) {
				insertDependency(node, VersionModificationAction.referenced);
			}
			else {
				insertDependency(node, vma);
			}
		}
		treeViewer.refresh();
	}
	
	/**
	 * insert a single {@link DependencyAnalysisNode}, 
	 * i.e. find the matching target artifact (actually, this just makes sure that the dependency is reachable) 
	 * @param node - the {@link DependencyAnalysisNode}
	 * @param vma - the {@link VersionModificationAction} to useew
	 */
	private void insertDependency(DependencyAnalysisNode node, VersionModificationAction vma) {
		VersionedArtifactIdentification identification = node.getIdentification();
		
		List<RemoteCompiledDependencyIdentification> rdis = scanController.runQuery( identification.asString());
		if (rdis != null && rdis.size() != 0 ) {									
			boolean success = ArtifactToClipboardExpert.injectDependenciesIntoProject( context.getProject(), vma, rdis);
			if (success) {
				node.setKind(DependencyKind.confirmed);
			}
		}
	}
	
	


	@Override
	public void widgetDefaultSelected(SelectionEvent arg0) {			
	}

	@Override
	public void widgetSelected(SelectionEvent event) {
		// no project -> no support for insertions
		if (context.getProject() == null) {
			return;
		}
		// nothing to insert -> no support 
		List<DependencyAnalysisNode> nodes = getSelectedInsertionCapableNode( treeViewer.getSelection());
		if (nodes == null) {
			return;
		}
		
		Widget widget = event.widget;
		
		VersionModificationAction vma = null;
		
		if (widget == insertWithDefault) {
			vma = DevrockPlugin.envBridge().storageLocker().getValue( StorageLockerSlots.SLOT_CLIP_PASTE_MODE, VersionModificationAction.referenced);
		}
		else if (widget == insertReferenced) {
			vma = VersionModificationAction.referenced;
		}
		else if (widget == insertRangified) {
			vma = VersionModificationAction.rangified;
		}
		else if (widget == insertUntouched) {
			vma = VersionModificationAction.untouched;
		}		
		insertDependencies(nodes, vma);		
	}
	
	

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		if (context.getProject() == null) {
			return;
		}
		
		ISelection selection = event.getSelection();
		if (selection.isEmpty()) {
			// disable buttons
			insertButtons.stream().forEach( b -> b.setEnabled(false));
			buttonLabel.setEnabled(false);
		}
		else {
			List<DependencyAnalysisNode> nodes = getSelectedInsertionCapableNode( treeViewer.getSelection());
			if (nodes == null || nodes.size() == 0) {
				// disable
				insertButtons.stream().forEach( b -> b.setEnabled(false));
				buttonLabel.setEnabled(false);
			}
			else {
				// enable
				insertButtons.stream().forEach( b -> b.setEnabled(true));
				buttonLabel.setEnabled(true);
			}
		}		
	}

	@Override
	public void dispose() {		
		viewLabelProvider.dispose();
	}

	
}
