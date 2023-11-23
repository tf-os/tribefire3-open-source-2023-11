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
package com.braintribe.devrock.greyface.view.tab.selection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.retrieval.multi.coding.ArtifactWrapperCodec;
import com.braintribe.cc.lcd.CodingMap;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.devrock.greyface.generics.tree.TreeColumnResizer;
import com.braintribe.devrock.greyface.generics.tree.TreeExpander;
import com.braintribe.devrock.greyface.generics.tree.TreeItemTooltipProvider;
import com.braintribe.devrock.greyface.process.notification.ScanProcessListener;
import com.braintribe.devrock.greyface.process.notification.SelectionContext;
import com.braintribe.devrock.greyface.process.notification.SelectionContextListener;
import com.braintribe.devrock.greyface.process.notification.SelectionProcessListener;
import com.braintribe.devrock.greyface.process.notification.UploadProcessListener;
import com.braintribe.devrock.greyface.view.tab.GenericViewTab;
import com.braintribe.devrock.greyface.view.tab.HasTreeTokens;
import com.braintribe.devrock.greyface.view.tab.TreeItemHelper;
import com.braintribe.devrock.greyface.views.dependency.tabs.capability.clipboard.ClipboardCopier;
import com.braintribe.devrock.greyface.views.dependency.tabs.capability.clipboard.ClipboardCopyCapable;
import com.braintribe.devrock.greyface.views.dependency.tabs.capability.expansion.ViewExpansionCapable;
import com.braintribe.devrock.greyface.views.dependency.tabs.capability.pomloading.PomLoadingCapable;
import com.braintribe.devrock.greyface.views.dependency.tabs.capability.selection.GlobalSelectionCapable;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Identification;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.artifact.ArtifactProcessor;
import com.braintribe.model.malaclypse.cfg.preferences.gf.RepositorySetting;

public class StructureSelectionTab extends GenericViewTab implements ScanProcessListener, HasTreeTokens, Listener, 
																	 SelectionListener, SelectionProcessListener, SelectionContextListener, UploadProcessListener,
																	 ViewExpansionCapable, GlobalSelectionCapable, PomLoadingCapable, ClipboardCopyCapable {
	private static Logger log = Logger.getLogger(StructureSelectionTab.class);
	
	private TreeColumnResizer structureTreeColumnResizer;
	private Tree structureTree;
	private Set<TreeItem> structureTreeItems = new HashSet<TreeItem>();
	private TreeSet<String> items;
	
	private Image dependencyImage;
	private Image parentImage;

	private Image selectedImage;
	private Image deselectedImage;
	private Image unresolvedImage;
	private Image scanEndPointImage;
		
	private SelectionProcessListener selectionListener;
	private SelectionContext context;
		
	private Map<String, Set<TreeItem>> itemToDuplicates = new HashMap<String, Set<TreeItem>>();
	private Map<Artifact, Set<Solution>> artifactToChildrenMap = CodingMap.createHashMapBased( new ArtifactWrapperCodec());

	private Button select;
	private Button selectRelated;
	
	
	private TreeExpander treeExpander;
	
		
	public StructureSelectionTab(Display display) {
		super(display);
		ImageDescriptor imageDescriptor = ImageDescriptor.createFromFile( GenericViewTab.class, "arrow-090-small.png");
		parentImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( GenericViewTab.class, "arrow-270-small.png");
		dependencyImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( GenericViewTab.class, "check_selected.png");
		selectedImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( GenericViewTab.class, "check_unselected.png");
		deselectedImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( GenericViewTab.class, "error.gif");
		unresolvedImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( GenericViewTab.class, "suspend_co.png");
		scanEndPointImage = imageDescriptor.createImage();
		
		items = new TreeSet<String>( new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {			
				return o1.compareTo(o2);
			}
			
		});
		
		treeExpander = new TreeExpander();
		treeExpander.addListener( this);		
	}

	@Configurable @Required
	public void setSelectionListener(SelectionProcessListener selectionListener) {
		this.selectionListener = selectionListener;
	}

	@Override
	protected Composite createControl(Composite parent) {	
		
		Composite composite = super.createControl(parent);
		GridLayout layout = new GridLayout();
		layout.numColumns = 4;
		composite.setLayout(layout);
		composite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true));
		
		Composite scanComposite = new Composite( composite, SWT.NONE);	
		scanComposite.setLayout(layout);
		scanComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 4, 2));
		
		Label scanResultTitleLabel = new Label( scanComposite, SWT.NONE);
    	scanResultTitleLabel.setText( "scan results as a structured list");
    	scanResultTitleLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 4,1));
    	scanResultTitleLabel.setFont( bigFont);

    	GridLayout scanLayout = new GridLayout();
    	scanLayout.numColumns = 4;
    	scanLayout.marginHeight = 0;
    	scanLayout.marginWidth = 0;
    	scanLayout.verticalSpacing = 0;
    	scanLayout.horizontalSpacing = 0;
		
		Composite treeComposite = new Composite(scanComposite, SWT.NONE);
		treeComposite.setLayout(scanLayout);
		treeComposite.setLayoutData(new GridData( SWT.FILL, SWT.FILL, true, true));
		
		structureTree = new Tree ( treeComposite, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		structureTree.setHeaderVisible( true);
		structureTree.setLayout(scanLayout);
		structureTree.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 4, 4));
			
		String [] targetColumnNames = new String [] {"name"};
		int [] targetColumnWeights = new int [] { 200};
		
		List<TreeColumn> targetColumns = new ArrayList<TreeColumn>();		
		for (int i = 0; i < targetColumnNames.length; i++) {
			TreeColumn treeColumn = new TreeColumn( structureTree, SWT.LEFT);
			treeColumn.setText( targetColumnNames[i]);
			treeColumn.setWidth( targetColumnWeights[i]);
			treeColumn.setResizable( true);
			//treeColumn.addSelectionListener(treeSortListener);
			targetColumns.add( treeColumn);
		}
		
		structureTreeColumnResizer = new TreeColumnResizer();
		structureTreeColumnResizer.setColumns( targetColumns);
		structureTreeColumnResizer.setColumnWeights( targetColumnWeights);
		structureTreeColumnResizer.setParent( treeComposite);
		structureTreeColumnResizer.setTree( structureTree);		
		structureTree.addControlListener(structureTreeColumnResizer);
		
		structureTree.addListener( SWT.MouseDoubleClick, this);
		
		TreeItemTooltipProvider.attach(structureTree, KEY_TOOLTIP);
		
		
		
		select = new Button( composite, SWT.NONE);
		select.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 1,1));		
		select.setText("Select artifact/part");
		select.addSelectionListener(this);
										
		selectRelated = new Button( composite, SWT.NONE);
		selectRelated.setText("Select related artifacts");
		selectRelated.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 1,1));
		selectRelated.addSelectionListener( this);
			
		
		
		return composite;		
	}




	@Override
	public void adjustSize() {	
		structureTreeColumnResizer.resize();		
	}

	@Override
	public void dispose() {
		parentImage.dispose();
		dependencyImage.dispose();
		selectedImage.dispose();
		deselectedImage.dispose();
		unresolvedImage.dispose();
		scanEndPointImage.dispose();
		
		super.dispose();
	}

	@Override
	public void acknowledgeScannedArtifact( final RepositorySetting setting, final Solution artifact, final Set<Artifact> parents, final boolean presentInTarget) {
		display.asyncExec( new Runnable() { 
			@Override
			public void run() {	
				for (Artifact parent : parents) {
					TreeItem parentItem = TreeItemHelper.findTreeItem( structureTreeItems, parent);
					if (parentItem == null) {
						log.warn("cannot find item with artifact [" + NameParser.buildName(artifact, artifact.getVersion()));
						return;
					}
					TreeItem [] childItems = parentItem.getItems();
					for (TreeItem childItem : childItems) {
						if (Boolean.TRUE.equals(childItem.getData( KEY_UNRESOLVED))) {
							break;
						}
						Solution childSolution = (Solution) childItem.getData( KEY_ARTIFACT);
						if (ArtifactProcessor.artifactEquals(childSolution, artifact)) 
							return;
					}
					
					TreeItem item = TreeItemHelper.appendItem(parentItem, itemToDuplicates, setting, artifact);					
					structureTreeItems.add( item);					
					if (presentInTarget) {
						item.setFont( italicFont);
						item.setData( KEY_EXISTS_IN_TARGET, Boolean.TRUE);
						if (context.getOverwriteExistingInTarget()) {
							item.setImage( deselectedImage);			
						}
					}
					else {
						item.setImage( deselectedImage);
					}
				}
			}
		});		
	}
	
		

	@Override
	public void acknowledgeScannedParentArtifact(final RepositorySetting setting, final Solution artifact, final Artifact parent, final boolean presentInTarget) {		
		display.asyncExec( new Runnable() { 
			@Override
			public void run() {
								
				TreeItem parentItem = TreeItemHelper.findTreeItem( structureTreeItems, parent);
				if (parentItem == null) {
					log.warn("cannot find item with artifact [" + NameParser.buildName(artifact, artifact.getVersion()));
					return;
				}
				
				TreeItem [] childItems = parentItem.getItems();
				for (TreeItem childItem : childItems) {
					Solution childSolution = (Solution) childItem.getData( KEY_ARTIFACT);
					if (ArtifactProcessor.artifactEquals(childSolution, artifact)) {
						return;
					}
				}
				
				TreeItem item = TreeItemHelper.createItem(parentItem, items, itemToDuplicates, setting, artifact);
				structureTreeItems.add( item);								
				
				if (presentInTarget) {
					item.setFont( italicFont);
					item.setData( KEY_EXISTS_IN_TARGET, Boolean.TRUE);
					if (context.getOverwriteExistingInTarget()) {
						item.setImage( deselectedImage);			
					}
				}
				else {
					item.setImage( deselectedImage);	
				}
			}
		});		
	}

	@Override
	public void acknowledgeScannedRootArtifact(final RepositorySetting setting, final Solution artifact, final boolean presentInTarget) {		
		display.asyncExec( new Runnable() { 
			@Override
			public void run() {
				TreeItem item = TreeItemHelper.createItem( structureTree, items, itemToDuplicates, setting, artifact);
				structureTreeItems.add( item);					
				item.setData( KEY_ROOT, Boolean.TRUE);	
				
				if (presentInTarget) {
					item.setFont(italicBoldFont);
					item.setData( KEY_EXISTS_IN_TARGET, Boolean.TRUE);
					if (context.getOverwriteExistingInTarget()) {
						item.setImage( deselectedImage);			
					}
				} else {
					item.setFont(boldFont);
					item.setImage( deselectedImage);
				}
			}
		});
	}

	@Override
	public void acknowledgeScanAbortedAsArtifactIsPresentInTarget( final RepositorySetting target, final Solution artifact, final Set<Artifact> parents) {		
		display.asyncExec( new Runnable() { 
			@Override
			public void run() {
				// root 
				if (parents == null || parents.size() == 0) {
					TreeItem item = TreeItemHelper.createItem( structureTree, items, itemToDuplicates, target, artifact);
					structureTreeItems.add( item);					
					item.setImage( scanEndPointImage);				
					item.setFont( italicBoldFont);
					item.setData( KEY_ROOT, Boolean.TRUE);	
					item.setData( KEY_SCAN_END, Boolean.TRUE);
					item.setData( KEY_EXISTS_IN_TARGET, Boolean.TRUE);	
				}
				else {
					for (Artifact parent : parents) {
						TreeItem parentItem = TreeItemHelper.findTreeItem( structureTreeItems, parent);
						if (parentItem == null) {
							log.warn("cannot find item with artifact [" + NameParser.buildName(artifact, artifact.getVersion()));
							return;
						}
						TreeItem item = TreeItemHelper.appendItem(parentItem, itemToDuplicates, target, artifact);					
						structureTreeItems.add( item);
						item.setImage( scanEndPointImage);				
						item.setFont( italicFont);
						item.setData( KEY_SCAN_END, Boolean.TRUE);
						item.setData( KEY_EXISTS_IN_TARGET, Boolean.TRUE);				
					}
				}
			}
		});				
	}
	

	@Override
	public void acknowledgeUnresolvedArtifact(final List<RepositorySetting> sources, final Dependency dependency, final Collection<Artifact> requestors) {		
		display.asyncExec( new Runnable() { 
			@Override
			public void run() {
				if (requestors != null) {
					for (Artifact parent : requestors) {
						TreeItem parentItem = TreeItemHelper.findTreeItem( structureTreeItems, parent);
						if (parentItem == null) {
							log.warn("cannot find item with artifact [" + NameParser.buildName(dependency));
							return;
						}					
						TreeItem item = TreeItemHelper.appendItem(parentItem, itemToDuplicates, null, dependency);					
						structureTreeItems.add( item);						
						item.setImage(unresolvedImage);
						item.setData( KEY_UNRESOLVED, true);
					}
				}
				else {
					
				}
			}
		});		
	}



	@Override
	public void acknowledgeStartScan() {
		display.asyncExec( new Runnable() { 
			@Override
			public void run() {
				structureTree.removeAll();
				structureTreeItems.clear();
				itemToDuplicates.clear();
				items.clear();
				artifactToChildrenMap.clear();
			}
		});		
	}


	@Override
	public void acknowledgeStopScan() {		
	}


	@Override
	public void acknowledgeArtifactSelected(Solution solution) {
		Set<TreeItem> items = TreeItemHelper.findTreeItems( structureTreeItems, solution);
		for (TreeItem item : items) {
			// check if overwrite's activated  
			if (
					context == null ||
					context.getOverwriteExistingInTarget() ||
					!Boolean.TRUE.equals(item.getData( KEY_EXISTS_IN_TARGET))
				) {
				item.setData(KEY_SELECTION,  Boolean.TRUE);
				item.setImage( selectedImage);
			}
		}
	}

	@Override
	public void acknowledgeArtifactDeSelected(Solution solution) {		
		Set<TreeItem> items = TreeItemHelper.findTreeItems( structureTreeItems, solution);
		for (TreeItem item : items) {
			item.setData(KEY_SELECTION,  Boolean.FALSE);
			item.setImage( deselectedImage);
		}
	}
	
	


	@Override
	public void acknowledgeSelectionContext(String id, SelectionContext selectionContext) {
		context = selectionContext;
		display.asyncExec( new Runnable() { 
			@Override
			public void run() {
				// reorganize 
				for (TreeItem item : structureTreeItems) {
					if (
							Boolean.TRUE.equals( item.getData( KEY_SCAN_END)) ||
							Boolean.TRUE.equals( item.getData( KEY_UNRESOLVED))
						)
						continue;
					if (!context.getOverwriteExistingInTarget()) {
						if (Boolean.TRUE.equals(item.getData(KEY_EXISTS_IN_TARGET))) {				
								item.setImage((Image) null); 
						}
						else {
							if (Boolean.TRUE.equals( item.getData( KEY_SELECTION))){
								item.setImage( selectedImage);
							}
							else {
								item.setImage(deselectedImage);
							}
						}		
					}
					else {
						if (Boolean.TRUE.equals( item.getData( KEY_SELECTION))){
							item.setImage( selectedImage);
						}
						else {
							item.setImage(deselectedImage);
						}
					}
				}
			}
		});
		
	}

	@Override
	public void handleEvent(Event event) {
		switch (event.type) {					
			case SWT.MouseDoubleClick:
				handleSelection( event);
				break;
			default:
				break;
		}
		
	}
	
	private void handleSelection( Event event){
		Point point = new Point(event.x, event.y);
        TreeItem item = structureTree.getItem(point);
        if (item == null)
        	return;        
        
		Solution solution = (Solution) item.getData(KEY_ARTIFACT);
		if (Boolean.TRUE.equals( item.getData( KEY_SELECTION))){
			selectionListener.acknowledgeArtifactDeSelected(solution);
		}
		else {
			selectionListener.acknowledgeArtifactSelected(solution);
		}
	}
	
	

	@Override
	public void widgetSelected(SelectionEvent e) {		
		// recursive selection 
		if (e.widget == selectRelated) {
			// 
			TreeItem [] items = structureTree.getSelection();
			if (items == null || items.length == 0)
				return;
			TreeItem root = items[0];
			Identification identification = (Identification) root.getData( KEY_ARTIFACT);
			if (identification instanceof Dependency) {
				return;
			}
			boolean select = true;
			if (Boolean.TRUE.equals(root.getData( KEY_SELECTION))) {
				select = false;
			}
			Set<Solution> selectedSolutions = TreeItemHelper.selectArtifactsByHierarchy( (Solution) identification);
			for (Solution solution : selectedSolutions) {
				if (select)
					selectionListener.acknowledgeArtifactSelected(solution);
				else
					selectionListener.acknowledgeArtifactDeSelected(solution);
		
			}
			return;
		}
		// single selection 
		if (e.widget == select) {
			TreeItem [] items = structureTree.getSelection();
			if (items == null || items.length == 0)
				return;
			for (TreeItem item : items) {
				Identification identification = (Identification) item.getData( KEY_ARTIFACT);				
				if (identification instanceof Solution == false)
					return;
				
				boolean select = true;
				if (Boolean.TRUE.equals(item.getData( KEY_SELECTION))) {
					select = false;
				}
				if (select)
					selectionListener.acknowledgeArtifactSelected((Solution) identification);
				else
					selectionListener.acknowledgeArtifactDeSelected((Solution) identification);				
			}
		}
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {		
	}
	
	@Override
	public void acknowledgeUploadBegin(RepositorySetting setting, int count) {}
	@Override
	public void acknowledgeUploadEnd(RepositorySetting setting) {}
	@Override
	public void acknowledgeUploadSolutionBegin(RepositorySetting setting, Solution solution) {}
	@Override
	public void acknowledgeUploadSolutionFail(RepositorySetting setting, Solution solution) {}
	@Override
	public void acknowledgeFailedPart(RepositorySetting setting, Solution solution, Part part, String reason, int worked) {}	
	@Override
	public void acknowledgeFailPartCRC(RepositorySetting setting, Solution solution, Part part, String reason, int index) {
		acknowledgeFailedPart(setting, solution, part, reason, index);		
	}
	@Override
	public void acknowledgeRootSolutions(RepositorySetting setting,Set<Solution> solution) {}	
	@Override
	public void acknowledgeUploadedPart(RepositorySetting setting,Solution solution, Part part, long time, int worked) {}
	@Override
	public void acknowledgePartSelected(Part part) {}
	@Override
	public void acknowledgePartDeSelected(Part part) {}

	@Override
	public void acknowledgeUploadSolutionEnd( final RepositorySetting setting, final Solution solution) {
		display.asyncExec( new Runnable() { 
			@Override
			public void run() {											
				TreeItem item = TreeItemHelper.findTreeItem( structureTreeItems, solution);
				if (item != null) {
					item.setData( KEY_EXISTS_IN_TARGET, true);
					if (Boolean.TRUE.equals(item.getData( KEY_SELECTION))) {
						item.setData( KEY_SELECTION, false);
						if (Boolean.TRUE.equals( item.getData (KEY_ROOT))) {
							item.setFont(italicBoldFont);
						} else { 
							item.setFont(italicFont);
						}						
						item.setImage(deselectedImage);
					}
				} 
				else {
					// root ? 
				}
			
			}
		});
	}

	@Override
	public Set<Solution> getSelectedSolutions() {
		Set<Solution> solutions = new HashSet<Solution>();
		for (TreeItem item : structureTreeItems) {
			if (!Boolean.TRUE.equals( item.getData( KEY_SELECTION))) {
				continue;
			}
			if (Boolean.TRUE.equals(item.getData( KEY_EXISTS_IN_TARGET))) {
				if (!context.getOverwriteExistingInTarget()) {
					continue;
				}
			}
			solutions.add((Solution) item.getData( KEY_ARTIFACT));
		}
		return solutions;
	}

	@Override
	public Set<Part> getSelectedParts() {	
		return null;
	}

	@Override
	public void expand() {
		treeExpander.expandTree(structureTree, true);		
	}

	@Override
	public void condense() {
		treeExpander.expandTree(structureTree, false);		
	}

	@Override
	public boolean isCondensed() {	
		return false;
	}

	@Override
	public void selectAll() {
		for (TreeItem item : structureTree.getItems()) {
			if (Boolean.TRUE.equals(item.getData( KEY_EXISTS_IN_TARGET))) {
				if (!context.getOverwriteExistingInTarget()) {
					continue;
				}
			}
			item.setData( KEY_SELECTION, true);
			Solution solution = (Solution) item.getData(KEY_ARTIFACT);
			selectionListener.acknowledgeArtifactSelected(solution);
		}		
	}

	@Override
	public void deselectAll() {
		for (TreeItem item : structureTree.getItems()) {
			if (Boolean.TRUE.equals(item.getData( KEY_EXISTS_IN_TARGET))) {
				if (!context.getOverwriteExistingInTarget()) {
					continue;
				}
			}
			item.setData( KEY_SELECTION, false);
			Solution solution = (Solution) item.getData(KEY_ARTIFACT);
			selectionListener.acknowledgeArtifactDeSelected(solution);
		}		
		
	}

	@Override
	public String copyContents() {
		///expand();
		return ClipboardCopier.copyToClipboard(structureTree);
	}

	@Override
	public void loadPom() {
	}
	
	

}
