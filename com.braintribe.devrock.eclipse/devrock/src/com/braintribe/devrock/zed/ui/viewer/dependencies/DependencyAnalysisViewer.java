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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
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

import com.braintribe.cc.lcd.EqProxy;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.devrock.api.pom.PomDependencyHandler;
import com.braintribe.devrock.api.storagelocker.StorageLockerSlots;
import com.braintribe.devrock.api.ui.commons.UiSupport;
import com.braintribe.devrock.api.ui.editors.EnumEditor;
import com.braintribe.devrock.api.ui.tree.TreeViewerColumnResizer;
import com.braintribe.devrock.eclipse.model.actions.VersionModificationAction;
import com.braintribe.devrock.mc.core.declared.commons.HashComparators;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.devrock.zarathud.model.common.FingerPrintRating;
import com.braintribe.devrock.zarathud.model.common.ReferenceNode;
import com.braintribe.devrock.zarathud.model.dependency.DependencyAnalysisNode;
import com.braintribe.devrock.zarathud.model.dependency.DependencyKind;
import com.braintribe.devrock.zed.ui.ZedViewerCommons;
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
public class DependencyAnalysisViewer implements IMenuListener, IDisposable, SelectionListener, ISelectionChangedListener {		
	private ContentProvider contentProvider;
	private TreeViewer treeViewer;
	private Tree tree;	
	
	private UiSupport uiSupport; 
	private List<DependencyAnalysisNode> nodes;
			
	private ViewLabelProvider viewLabelProvider;
	private ZedViewingContext context;
	
	private Button insertDependenciesBt;
	private EnumEditor<VersionModificationAction> insertDependenciesOptionsEditor;
	private Map<String, VersionModificationAction> optionChoices = new LinkedHashMap<>();
	private Map<VersionModificationAction, String> optionTooltips = new HashMap<>();
	{
		optionChoices.put( "referenced", VersionModificationAction.referenced);
		optionTooltips.put( VersionModificationAction.referenced, "Version expression is replaced by a variable derived from the groupId");
	
		optionChoices.put( "rangified", VersionModificationAction.rangified);
		optionTooltips.put( VersionModificationAction.rangified, "Version expression is replaced by a standard version range expression");

		optionChoices.put( "as is", VersionModificationAction.untouched);
		optionTooltips.put( VersionModificationAction.untouched, "Version expression is replaced by the exact version specified");
	}
	
	private Button deleteDependenciesBt;
	private Button sanitizeBt;
	private Set<Button> dependencyManipulationButtons = new HashSet<>();
	private Set<Button> dependencyInsertionButtons = new HashSet<>();
	private Set<Button> dependencyDeletionButtons = new HashSet<>();
	
	private VersionModificationAction currentVma = DevrockPlugin.envBridge().storageLocker().getValue( StorageLockerSlots.SLOT_CLIP_PASTE_MODE, VersionModificationAction.referenced);
	
	private boolean allowPurgeOfExcessDependencies = DevrockPlugin.envBridge().storageLocker().getValue( StorageLockerSlots.SLOT_ZED_ALLOW_PURGE, false);
	private MenuManager mainMenuManager;
	private ImageDescriptor openFileImgDescr;
	
	public DependencyAnalysisViewer(ZedViewingContext context) {
		this.context = context;
	}

	@Configurable @Required
	public void setUiSupport(UiSupport uiSupport) {
		this.uiSupport = uiSupport;					
		openFileImgDescr= ImageDescriptor.createFromFile(DependencyAnalysisViewer.class, "pasteFromClipboard.png");	
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
    	
    	Control control = treeViewer.getControl();
    	
    
    	mainMenuManager = new MenuManager();
		mainMenuManager.setRemoveAllWhenShown( true);
		mainMenuManager.addMenuListener( this);
		
		// b) attach
		Menu menu = mainMenuManager.createContextMenu(control);
		control.setMenu( menu);
	
		
		if (context.getProject() != null) {
			// buttons 
			Composite buttonComposite = new Composite( composite, SWT.NONE);
	        buttonComposite.setLayout( layout);
	        buttonComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4,1));
	        
	        Label buttonLabel = new Label( buttonComposite, SWT.NONE);
	        buttonLabel.setText( "Modify pom of project: " + context.getProject().getName());
	        buttonLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, true, false, 4, 1));
	        buttonLabel.setEnabled(false);
	        
	        sanitizeBt = new Button(buttonComposite, SWT.NONE);
	        sanitizeBt.setText("Sanitize pom");
	        sanitizeBt.setLayoutData(new GridData( SWT.FILL, SWT.CENTER, true, true, 4,1));
	        sanitizeBt.setToolTipText( "insert all missing dependencies and delete all excess dependencies");	       	    
	        sanitizeBt.addSelectionListener(this);
	        	     
	        // insertion
	        VersionModificationAction currentVma = DevrockPlugin.envBridge().storageLocker().getValue( StorageLockerSlots.SLOT_CLIP_PASTE_MODE, VersionModificationAction.referenced);
	 
	        insertDependenciesBt = new Button(buttonComposite, SWT.NONE);
	        insertDependenciesBt.setText("Insert missing dependencies");
	        insertDependenciesBt.setLayoutData(new GridData( SWT.FILL, SWT.CENTER, true, true, 3,1));
	        insertDependenciesBt.setToolTipText("Insert the selected 'missing' dependencies into the pom using the mode selected");
	        dependencyManipulationButtons.add(insertDependenciesBt);
	        dependencyInsertionButtons.add(insertDependenciesBt);
	        insertDependenciesBt.addSelectionListener(this);
	     
	        
	        // insertion options 
	        insertDependenciesOptionsEditor = new EnumEditor<>();	        
	        insertDependenciesOptionsEditor.setToolTips( optionTooltips);
	        insertDependenciesOptionsEditor.setChoices( optionChoices);
			control = insertDependenciesOptionsEditor.createControl(buttonComposite, null, null);
			control.setLayoutData(new GridData( SWT.RIGHT, SWT.CENTER, false, false, 1, 1));		
			insertDependenciesOptionsEditor.setSelection( currentVma);
			

			if (allowPurgeOfExcessDependencies) {
		        // deletion
		        deleteDependenciesBt = new Button(buttonComposite, SWT.NONE);
		        deleteDependenciesBt.setText("Delete excess dependencies");
		        deleteDependenciesBt.setLayoutData(new GridData( SWT.FILL, SWT.CENTER, true, true, 3,1));
		        deleteDependenciesBt.setToolTipText("Delete the selected 'excess' dependencies from the pom");
		        deleteDependenciesBt.addSelectionListener(this);
		        dependencyManipulationButtons.add(deleteDependenciesBt);
		        dependencyDeletionButtons.add(deleteDependenciesBt);
			}
		}
				       
        composite.pack();	
        
        // no selection, no insert
        if (dependencyManipulationButtons.size() > 0) {
        	toggleButtonMenu(false);
        }
        
        toogleSanitizeButton();
	
		return composite;
	}
	
	private void toogleSanitizeButton() {
		if (sanitizeBt == null) {
			return;
		}
		DependencyAnalysisNode node =  nodes.stream().filter(this::testForSanitizing).findFirst().orElse(null);
		if (node != null) {		
			sanitizeBt.setEnabled(true);
		}
		else  {
			sanitizeBt.setEnabled(false);
		}
	}

	
	private void toggleButtonMenu(boolean activate) {
		dependencyManipulationButtons.stream().forEach( b -> b.setEnabled( activate));
		insertDependenciesOptionsEditor.setEnable(activate);
	}
	private void toggleInsertionButtonMenu(boolean activate) {
		dependencyInsertionButtons.stream().forEach( b -> b.setEnabled( activate));
		insertDependenciesOptionsEditor.setEnable(activate);
	}
	private void toggleDeletionButtonMenu(boolean activate) {
		dependencyDeletionButtons.stream().forEach( b -> b.setEnabled( activate));		
	}
	
	/**
	 * @param selection
	 * @param filter
	 * @return
	 */
	private List<DependencyAnalysisNode> filterNodes(ISelection selection, Function<DependencyAnalysisNode, Boolean> filter) {		
		 if(!selection.isEmpty()){
	        	if (selection instanceof IStructuredSelection) {
	        		IStructuredSelection structuredSelection = (IStructuredSelection) selection;
	        		List<DependencyAnalysisNode> nodes = new ArrayList<>(structuredSelection.size());
	        		Iterator<?> iter = structuredSelection.iterator();
	        		while (iter.hasNext()) {
	        			Object item = iter.next();
	        			if (item instanceof DependencyAnalysisNode) {
	        				DependencyAnalysisNode selectedNode = (DependencyAnalysisNode) item;
	        				if (filter.apply(selectedNode)) {
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
	public void menuAboutToShow(IMenuManager mmgr) {
		if (context.getProject() == null)
			return; 
		ISelection selection = treeViewer.getSelection();
		 if(!selection.isEmpty()){
	        if (selection instanceof IStructuredSelection) {
	        	IStructuredSelection structuredSelection = (IStructuredSelection) selection;	        	
	        	Iterator<?> iter = structuredSelection.iterator();
	        	while (iter.hasNext()) {
	        		Object obj = iter.next();
	        		if (obj instanceof ReferenceNode){
	        			ReferenceNode rfn = (ReferenceNode) obj;	        			
	        			String qualifiedName = rfn.getSource().getName();
	        			int p = qualifiedName.lastIndexOf('.');
	        			String packageName = qualifiedName.substring(0, p);
	        			String typeName = qualifiedName.substring(p+1);
	        			
	        			Action openInEditor = new Action("open relevant file for: " + qualifiedName, openFileImgDescr) {
		        			@Override
		        			public void run() {
		        				ZedViewerCommons.openFile( context, packageName, typeName);		
		        			}								
		        			@Override
		        			public String getToolTipText() {
		        				return "opens corresponding file: " + packageName + "." + typeName;
		        			}
		        		}; 
		        		mainMenuManager.add( openInEditor);	        				        			
	        			
	        		}
	        	}
	        }
		 }
	}
	
	private boolean testForRelevance(DependencyAnalysisNode node) {
		FingerPrintRating rating = node.getRating();
		boolean ignore = rating == FingerPrintRating.ok || rating == FingerPrintRating.ignore;
		return !ignore;		
	}
	
	/**
	 * tests whether the node is a candidate for insertion 
	 * @param node - the {@link DependencyAnalysisNode}
	 * @return - true if it the depedency can be inserted, false otherwise
	 */
	private boolean testForInsertion(DependencyAnalysisNode node) {
		if (node.getRedacted() || !testForRelevance(node)) {
			return false;
		}		
		if (
				node.getKind() == DependencyKind.missing || 
				(node.getKind() == DependencyKind.forward && node.getIncompleteForwardReference())
			) {
			return true;
		}
		return false;
	}
	
	/**
	 * tests whether the node is a candidate for deletion 
	 * @param node - the {@link DependencyAnalysisNode}
	 * @return - true if it can be deleted, false otherwise 
	 */
	private boolean testForDeletion( DependencyAnalysisNode node) {
		if (node.getRedacted() || !testForRelevance( node)) {
			return false;
		}
		if (node.getKind() == DependencyKind.excess) {
			return true;
		}
		return false;
	}
	
	/**
	 * tests whether any sanitizing works on this node
	 * @param node - the {@link DependencyAnalysisNode}
	 * @return - true if it can be sanitized (added/deleted) or false otherwise
	 */
	boolean testForSanitizing( DependencyAnalysisNode node) {
		return testForInsertion(node) || testForDeletion(node);
	}
	
	/**
	 * identify the {@link DependencyAnalysisNode} of the current selection in the tree viewer
	 * @param selection - the {@link ISelection}
	 * @return - a {@link List} of {@link DependencyAnalysisNode} that can be inserted, i.e
	 * have the {@link DependencyKind#missing}
	 */
	private List<DependencyAnalysisNode> getSelectedInsertionCapableNodes(ISelection selection) {
		return filterNodes(selection, this::testForInsertion);		    	
	}
	
	private List<DependencyAnalysisNode> getSelectedDeletionCapableNodes(ISelection selection) {
		return filterNodes(selection, this::testForDeletion);
    	
	}
	
	private void reflectResultInViewer( Map<VersionedArtifactIdentification,Boolean> result) {
		Map<EqProxy<VersionedArtifactIdentification>, DependencyAnalysisNode> nodeMap = new HashMap<>();
		nodes.stream().forEach( n -> nodeMap.put( HashComparators.versionedArtifactIdentification.eqProxy(n.getIdentification()), n));
		for (Map.Entry<VersionedArtifactIdentification,Boolean> entry : result.entrySet()) {
			DependencyAnalysisNode node = nodeMap.get( HashComparators.versionedArtifactIdentification.eqProxy( entry.getKey()));
			if (entry.getValue()) {
				node.setRedacted(true);
			}
			else {
				node.setRedacted(false);
			}
		}		
		treeViewer.refresh();
	}
			
	/**
	 * insert the {@link DependencyAnalysisNode} as dependencies into the pom
	 * @param nodes - the {@link DependencyAnalysisNode} to insert
	 * @param vma - the {@link VersionModificationAction} to use
	 */
	private void insertDependencies(List<DependencyAnalysisNode> nodes, VersionModificationAction vma) {
		Map<VersionedArtifactIdentification, VersionModificationAction> inp = buildInsertionData(nodes, vma);
		Map<VersionedArtifactIdentification,Boolean> insertDependencies = PomDependencyHandler.insertDependencies(context.getProject(), inp);
		// reflect the inserted in the viewer
		reflectResultInViewer( insertDependencies);
		
	}

	private Map<VersionedArtifactIdentification, VersionModificationAction> buildInsertionData( List<DependencyAnalysisNode> nodes, VersionModificationAction vma) {
		Map<VersionedArtifactIdentification, VersionModificationAction> inp = new HashMap<>();
		for (DependencyAnalysisNode node : nodes) {
			// such nodes can only be inserted with a groupId-derived variable reference as it has no version specified
			if (node.getIncompleteForwardReference()) {
				inp.put( node.getIdentification(), VersionModificationAction.referenced);				
			}
			else {
				inp.put(node.getIdentification(), vma);
			}			
		}
		return inp;
	}			
	
	private void deleteDependencies( List<DependencyAnalysisNode> nodes) {
		Map<VersionedArtifactIdentification,Boolean> deleteDependencies = PomDependencyHandler.deleteDependencies( context.getProject(), nodes.stream().map( n -> n.getIdentification()).collect( Collectors.toList()));
		// reflect the deleted in the viewer
		reflectResultInViewer( deleteDependencies);
		treeViewer.refresh();
	}
	
	@Override
	public void widgetDefaultSelected(SelectionEvent arg0) {}

	@Override
	public void widgetSelected(SelectionEvent event) {
		// no project -> no support for insertions
		if (context.getProject() == null) {
			return;
		}
		// nothing to insert -> no support 
		
		Widget widget = event.widget;
			
		if (widget == sanitizeBt) {			
			// allow purge? 
			List<DependencyAnalysisNode> insertionNodes = nodes.stream().filter( this::testForInsertion).collect(Collectors.toList());
			List<VersionedArtifactIdentification> deletionNodes = null;
			if (allowPurgeOfExcessDependencies) {
				deletionNodes = nodes.stream().filter( this::testForDeletion).map(n -> n.getIdentification()).collect(Collectors.toList());
				if (insertionNodes.size() == 0 && deletionNodes.size() == 0) {
					return;
				}		
			}
			else {
				if (insertionNodes.size() == 0) {
					return;
				}
				deletionNodes = new ArrayList<>();
			}
			Map<VersionedArtifactIdentification,VersionModificationAction> insertionData = buildInsertionData(insertionNodes, currentVma);
			Map<VersionedArtifactIdentification,Boolean> result = PomDependencyHandler.manageDependencies( context.getProject(), insertionData, deletionNodes);			
			// reflect
			reflectResultInViewer(result);			
		} 
		else if (widget == insertDependenciesBt) {
			List<DependencyAnalysisNode> nodes = getSelectedInsertionCapableNodes( treeViewer.getSelection());
			if (nodes == null) {
				return;
			}
			VersionModificationAction action = insertDependenciesOptionsEditor.getSelection();
			insertDependencies(nodes, action);
		}		
		else if (widget == deleteDependenciesBt) {
			List<DependencyAnalysisNode> nodes = getSelectedDeletionCapableNodes( treeViewer.getSelection());
			if (nodes == null) {
				return;
			}
			deleteDependencies(nodes);
		}								
	}
	
	

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		if (context.getProject() == null) {
			return;
		}
		
		ISelection selection = event.getSelection();
		if (selection.isEmpty()) {
			// disable buttons
			dependencyManipulationButtons.stream().forEach( b -> b.setEnabled(false));
			//buttonLabel.setEnabled(false);
		}
		else {
			List<DependencyAnalysisNode> insertionNodes = getSelectedInsertionCapableNodes( treeViewer.getSelection());
			
			if (insertionNodes == null || insertionNodes.size() == 0) {
				// disable
				toggleInsertionButtonMenu(false);
			}
			else {
				// enable
				toggleInsertionButtonMenu(true);				
			}
			
			List<DependencyAnalysisNode> deletionNodes = getSelectedDeletionCapableNodes( treeViewer.getSelection());
			if (deletionNodes == null || deletionNodes.size() == 0) {
				// disable
				toggleDeletionButtonMenu(false);
			}
			else {
				// enable
				toggleDeletionButtonMenu(true);
			}						
		}		
	}

	
	
	@Override
	public void dispose() {		
		viewLabelProvider.dispose();
	}


}