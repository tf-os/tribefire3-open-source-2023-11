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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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

import com.braintribe.cc.lcd.CodingSet;
import com.braintribe.cc.lcd.HashSupportWrapperCodec;
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
import com.braintribe.devrock.greyface.process.retrieval.TempFileHelper;
import com.braintribe.devrock.greyface.view.TabItemImageListener;
import com.braintribe.devrock.greyface.view.tab.GenericViewTab;
import com.braintribe.devrock.greyface.view.tab.HasTreeTokens;
import com.braintribe.devrock.greyface.view.tab.TreeItemHelper;
import com.braintribe.devrock.greyface.views.dependency.tabs.capability.clipboard.ClipboardCopier;
import com.braintribe.devrock.greyface.views.dependency.tabs.capability.clipboard.ClipboardCopyCapable;
import com.braintribe.devrock.greyface.views.dependency.tabs.capability.expansion.ViewExpansionCapable;
import com.braintribe.devrock.greyface.views.dependency.tabs.capability.pomloading.PomLoader;
import com.braintribe.devrock.greyface.views.dependency.tabs.capability.pomloading.PomLoadingCapable;
import com.braintribe.devrock.greyface.views.dependency.tabs.capability.selection.GlobalSelectionCapable;
import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Identification;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.PartTuple;
import com.braintribe.model.artifact.PartType;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.artifact.ArtifactProcessor;
import com.braintribe.model.artifact.processing.part.PartTupleProcessor;
import com.braintribe.model.malaclypse.cfg.preferences.gf.RepositorySetting;

public class FlatSelectionTab extends GenericViewTab implements Listener, HasTreeTokens, ScanProcessListener, SelectionProcessListener, 
																SelectionListener, UploadProcessListener, SelectionContextListener,
																ViewExpansionCapable, GlobalSelectionCapable, ClipboardCopyCapable, PomLoadingCapable {

	private TreeColumnResizer flatTreeColumnResizer;
	private Tree flatTree;
	private TreeSet<String> items;
	private Map<String, Set<TreeItem>> itemToDuplicates = new HashMap<String, Set<TreeItem>>();
	
	private Image unknownPartImage;
	private Image pomPartImage;
	private Image jarPartImage;
	private Image sourcesPartImage;
	private Image javadocPartImage;
	private Image partsImage;

	private Image unresolvedImage;
	
	private Image selectedImage;
	private Image deselectedImage;
	
	private Image scanEndPointImage;
	private Image containsSelectedPartImage; 
	
	private Set<Identification> listed = new HashSet<Identification>();

	private SelectionProcessListener selectionListener;
	private Button select;
	private Button selectRelated;
	private TreeExpander treeExpander;
	
	private SelectionContext context;

	@Configurable @Required
	public void setSelectionListener(SelectionProcessListener selectionListener) {
		this.selectionListener = selectionListener;
	}
	
	public FlatSelectionTab(Display display) {
		super(display);
		
		ImageDescriptor imageDescriptor = ImageDescriptor.createFromFile( GenericViewTab.class, "question-small.png");
		unknownPartImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( GenericViewTab.class, "solution.pom.gif");
		pomPartImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( GenericViewTab.class, "solution.jar.gif");
		jarPartImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( GenericViewTab.class, "solution.source.gif");
		sourcesPartImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( GenericViewTab.class, "solution.javadoc.gif");
		javadocPartImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( GenericViewTab.class, "library_obj.gif");
		partsImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( GenericViewTab.class, "check_selected.png");
		selectedImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( GenericViewTab.class, "check_unselected.png");
		deselectedImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( GenericViewTab.class, "error.gif");
		unresolvedImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( GenericViewTab.class, "suspend_co.png");
		scanEndPointImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( GenericViewTab.class, "change.png");
		containsSelectedPartImage = imageDescriptor.createImage();
		
		
		items = new TreeSet<String>( new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {			
				return o1.toLowerCase().compareTo(o2.toLowerCase());
			}
			
		});
		
		treeExpander = new TreeExpander();
		treeExpander.addListener( this);
	}

	@Override
	public void setImageListener(TabItemImageListener imageListener) {		
		super.setImageListener(imageListener);
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
    	scanResultTitleLabel.setText( "scan results as a flat list");
    	scanResultTitleLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 4,1));
    	scanResultTitleLabel.setFont( bigFont);
    	
		GridLayout scanLayout = new GridLayout();
    	scanLayout.numColumns = 4;
    	scanLayout.marginHeight = 0;
    	scanLayout.marginWidth = 0;
    	scanLayout.verticalSpacing = 0;
    	
		Composite treeComposite = new Composite(scanComposite, SWT.NONE);
		treeComposite.setLayout(scanLayout);
		treeComposite.setLayoutData(new GridData( SWT.FILL, SWT.FILL, true, true));
		
		flatTree = new Tree ( treeComposite, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		flatTree.setHeaderVisible( true);
		flatTree.setLayout(scanLayout);
		flatTree.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 4, 4));
			
		String [] targetColumnNames = new String [] {"name"};
		int [] targetColumnWeights = new int [] { 200};
		
		List<TreeColumn> targetColumns = new ArrayList<TreeColumn>();		
		for (int i = 0; i < targetColumnNames.length; i++) {
			TreeColumn treeColumn = new TreeColumn( flatTree, SWT.LEFT);
			treeColumn.setText( targetColumnNames[i]);
			treeColumn.setWidth(targetColumnWeights[i]);
			treeColumn.setResizable( true);
			//treeColumn.addSelectionListener(treeSortListener);
			targetColumns.add( treeColumn);
		}
		
		flatTreeColumnResizer = new TreeColumnResizer();
		flatTreeColumnResizer.setColumns( targetColumns);
		flatTreeColumnResizer.setColumnWeights( targetColumnWeights);
		flatTreeColumnResizer.setParent( treeComposite);
		flatTreeColumnResizer.setTree( flatTree);		
		flatTree.addControlListener(flatTreeColumnResizer);
		
		flatTree.addListener(SWT.Expand, this);
		flatTree.addListener( SWT.MouseDoubleClick, this);
		
		TreeItemTooltipProvider.attach(flatTree, KEY_TOOLTIP);
		
		
		select = new Button( composite, SWT.NONE);
		select.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 3,1));		
		select.setText("Select artifact/part");
		select.addSelectionListener(this);
		
		selectRelated = new Button( composite, SWT.NONE);
		selectRelated.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 1,1));		
		selectRelated.setText("Select related artifacts");
		selectRelated.addSelectionListener(this);
		
		return composite;
	}

	@Override
	public void dispose() {
		unknownPartImage.dispose();
		pomPartImage.dispose();
		jarPartImage.dispose();
		sourcesPartImage.dispose();
		javadocPartImage.dispose();	
		unresolvedImage.dispose();
		selectedImage.dispose();
		deselectedImage.dispose();
		scanEndPointImage.dispose();
		containsSelectedPartImage.dispose();
		
		super.dispose();
	}

	@Override
	public void adjustSize() {
		flatTreeColumnResizer.resize();
	}	

	@Override
	public void handleEvent(Event event) {
		switch (event.type) {
			case SWT.Expand:
				handleExpansion(event);
				break;
			case SWT.MouseDoubleClick:
				handleSelection( event);
				break;
			default:
				break;
		}		
	}
	
	private void handleSelection( Event event){
		Point point = new Point(event.x, event.y);
        TreeItem item = flatTree.getItem(point);
        if (item == null)
        	return;       
        Object artifact = item.getData(KEY_ARTIFACT);
        if (artifact instanceof Dependency)
        	return;
		Solution solution = (Solution) artifact;
		if (solution != null) {
			if (Boolean.TRUE.equals( item.getData( KEY_SELECTION))){
				selectionListener.acknowledgeArtifactDeSelected(solution);
			}
			else {
				selectionListener.acknowledgeArtifactSelected(solution);
			}
			return;
		}
		if (
				context == null ||
				context.getOverwriteExistingInTarget() ||
				context.getRepairExistingInTarget() == false
			) 
			return;
		
		Part part = (Part) item.getData( KEY_PART);
		if (part != null) {
			if (Boolean.TRUE.equals( item.getData( KEY_SELECTION))){
				item.setData(KEY_SELECTION, false);
				handlePartItemSelection( item, part, null);
				selectionListener.acknowledgePartDeSelected(part);
			}
			else {
				item.setData(KEY_SELECTION, true);
				handlePartItemSelection( item, part, null);
				selectionListener.acknowledgePartSelected(part);
			}
			return;
		}
	}
	
	
	private void handlePartItemSelection( TreeItem item, Part part, Boolean select) {
		
		PartType type = PartTupleProcessor.toPartType( part.getType());
		setPartItemImage(item, type, true, select);			 
				
	}
	
	/**
	 * set the parts image, according state if any 	 
	 */
	private void setPartItemImage( TreeItem item, PartType type, boolean selectable, Boolean select) {
		boolean selected;
		if (select == null) {
			selected = Boolean.TRUE.equals(item.getData( KEY_SELECTION));			
		}
		else 
			selected = select;
		if (type == null) {
			type = PartType._UNKNOWN_;
		}
		switch (type) {
			case POM:				
				if (!selectable) {					
					item.setImage( pomPartImage);
				} 
				else if (selected){
					item.setImage( selectedImage);
				}
				else {
					item.setImage( deselectedImage);
				}
				break;
			case JAR:
				if (!selectable) {
					item.setImage( jarPartImage);
				}
				else if (selected){
					item.setImage( selectedImage);
				}
				else {
					item.setImage( deselectedImage);
				} 										
				break;
			case JAVADOC:										
				if (!selectable) {
					item.setImage( javadocPartImage);
				}
				else if (selected){
					item.setImage( selectedImage);
				}
				else {
					item.setImage( deselectedImage);
				} 			
				break;
			case SOURCES:										
				if (!selectable) {
					item.setImage( sourcesPartImage);
				}
				else if (selected){
					item.setImage( selectedImage);
				}
				else {
					item.setImage( deselectedImage);
				}
				break;
			default:
				if (!selectable) {
					item.setImage( unknownPartImage);
				}
				else if (selected){
					item.setImage( selectedImage);
				}
				else {
					item.setImage( deselectedImage);
				}
				break;			
		}
	}
	
	
	private void handleExpansion( Event event) {
		TreeItem item = (TreeItem) event.item;
		handleExpansion( item);
	}
	
	private void handleExpansion( final TreeItem item) {
				
		TreeItem [] children = item.getItems();
		if (children != null) {
			TreeItem target = null;
			for (TreeItem child : children) {
				String name = child.getText(0);
				if (name.equalsIgnoreCase( KEY_DEFERRED)) {
					target = child;
					break;
				}
			}
			if (target == null)
				return;			
			final TreeItem suspect = target;			
			
			// 
			display.asyncExec( new Runnable() {
				
				@Override
				public void run() {					
					suspect.dispose();
					//
					Solution solution = (Solution) item.getData( KEY_ARTIFACT);
					if (solution == null)
						return;
					// sort list here 
					List<Part> sortedPartList = new ArrayList<Part>( solution.getParts());
					Collections.sort(sortedPartList, new Comparator<Part>() {

						@Override
						public int compare(Part o1, Part o2) {														
							String location1 = o1.getLocation().replace('\\', '/');
							if (TempFileHelper.isATempFile(location1)) {
								location1 = TempFileHelper.extractFilenameFromTempFile(location1);
							}
							String name1 = location1.substring( location1.lastIndexOf( '/') + 1);
							
							String location2 = o2.getLocation().replace('\\', '/');
							if (TempFileHelper.isATempFile(location2)) {
								location2 = TempFileHelper.extractFilenameFromTempFile(location2);
							}
							String name2 = location2.substring( location2.lastIndexOf( '/') + 1);
															
							int val = name1.toLowerCase().compareTo(name2.toLowerCase()); 															
							return val;							
						}						
					});					
					// build from parts
					// only attach a part once 
					Set<PartTuple> processedTuples = CodingSet.createHashSetBased( new HashSupportWrapperCodec<PartTuple>() {

						@Override
						protected boolean entityEquals(PartTuple t1, PartTuple t2) {
							return PartTupleProcessor.equals(t1, t2);							
						}

						@Override
						protected int entityHashCode(PartTuple t) {
							return PartTupleProcessor.toString(t).hashCode();							
						}
						
					});
					for (Part part : sortedPartList) {						
						if (
								PartTupleProcessor.equals( part.getType(), PartTupleProcessor.create( PartType.MD5)) ||
								PartTupleProcessor.equals( part.getType(), PartTupleProcessor.create( PartType.SHA1))
							) {
							continue;
						}						
						PartTuple tuple = part.getType();
						if (processedTuples.contains(tuple)) {
							continue;
						}
						else {
							processedTuples.add(tuple);
						}
					
						createItemForPart(item, part);						
					}					
																									
				}
									
			});						
		} 	
	}
		
	private TreeItem createItemForPart( TreeItem parentItem, Part part) {
		//
		TreeItem [] existing = parentItem.getItems();
		int index = -1;
		String name = TreeItemHelper.getName( part);
		
		// calculate index to insert new entry  
		if (existing != null && existing.length > 0) {
			List<String> names = new ArrayList<String>( existing.length);
			for (TreeItem item : existing) {
				names.add( item.getText());
			}
			names.add( name);
			Collections.sort(names, new Comparator<String>() {

				@Override
				public int compare(String o1, String o2) {				
					return o1.compareTo(o2);
				}				
			});
			for (int i = 0; i < names.size(); i++) {
				if (names.get(i).equalsIgnoreCase(name)) {
					index = i;
					break;
				}
			}
		}
		
		TreeItem partItem = index >= 0 ? new TreeItem( parentItem, SWT.NONE, index) : new TreeItem( parentItem, SWT.NONE);
							
		partItem.setText( name);
		partItem.setFont(italicFont);
		partItem.setData( KEY_TOOLTIP, part.getLocation());
		partItem.setData( KEY_SELECTION, false);
		partItem.setData( KEY_PART, part);
		
		boolean selectableParts = false;
		if (context != null) {
			selectableParts = context.getRepairExistingInTarget();
		}
		
		
		PartType type = PartTupleProcessor.toPartType( part.getType());
		if (type == null) {
			type = PartType._UNKNOWN_;
		}
		switch (type) {
		case _UNKNOWN_ : 
			if (!selectableParts) {
				partItem.setImage( unknownPartImage);
			}
			else {
				partItem.setImage( deselectedImage);
			}
			break;			
			default:
				setPartItemImage( partItem, type, selectableParts, false);																				
			
		}
		
		return partItem;
	}
	
	private synchronized boolean listed( Identification artifact) {
		for (Identification solution : listed) {
			if (
					artifact instanceof Solution &&
					solution instanceof Solution
				) {
				if (ArtifactProcessor.artifactEquals((Artifact) artifact, (Artifact) solution))
					return true;			
			}
			else if (
					artifact instanceof Dependency &&
					solution instanceof Dependency
				) 
			{
				if (ArtifactProcessor.coarseDependencyEquals((Dependency) artifact, (Dependency) solution))
					return true;
			}
		}
		listed.add( artifact);
		return false;
	}
	
	@Override
	public void acknowledgeScannedArtifact( final RepositorySetting setting, final Solution artifact, final Set<Artifact> parents, final boolean presentInTarget) {
		if (listed( artifact))
			return;
		display.asyncExec( new Runnable() { 
			@Override			
			public void run() {								
				TreeItem item = TreeItemHelper.createItem( flatTree, items, itemToDuplicates, setting, artifact);					
				TreeItem parts = TreeItemHelper.attachParts(item, artifact);
				if (parts != null) {
					parts.setImage(partsImage);
				}
				if (presentInTarget) {
					item.setFont( italicFont);
					item.setData( KEY_EXISTS_IN_TARGET, Boolean.TRUE);
					if (context.getOverwriteExistingInTarget()) {
						if (Boolean.TRUE.equals( item.getData( KEY_SELECTION))){
							item.setImage( selectedImage);
						}
						else {
							item.setImage(deselectedImage);
						}	
					}				
				}
				else {
					item.setImage(deselectedImage);
				}
			}
		});		
	}
	
	
	@Override
	public void acknowledgeScannedParentArtifact(final RepositorySetting setting, final Solution artifact, final Artifact child, final boolean presentInTarget) {
		if (listed( artifact))
			return;
		display.asyncExec( new Runnable() { 
			@Override
			public void run() {										
				TreeItem item = TreeItemHelper.createItem( flatTree, items, itemToDuplicates, setting, artifact);
				TreeItem parts = TreeItemHelper.attachParts(item, artifact);
				if (parts != null)
					parts.setImage(partsImage);
				
				if (presentInTarget) {
					item.setFont( italicFont);
					item.setData( KEY_EXISTS_IN_TARGET, Boolean.TRUE);		
					if (context.getOverwriteExistingInTarget()) {
						item.setImage(deselectedImage);
					}
				}
				else {
					item.setImage(deselectedImage);
				}
			}
		});		
	}

	@Override
	public void acknowledgeScannedRootArtifact(final RepositorySetting setting, final Solution artifact, final boolean presentInTarget) {	
		if (listed( artifact))
			return;
		display.asyncExec( new Runnable() { 
			@Override
			public void run() {
				TreeItem item = TreeItemHelper.createItem( flatTree, items, itemToDuplicates, setting, artifact);																				
				TreeItem parts = TreeItemHelper.attachParts(item, artifact);
				if (parts != null)
					parts.setImage(partsImage);
				item.setData( KEY_ROOT, Boolean.TRUE);
				item.setFont(boldFont);
				
				if (presentInTarget) {
					item.setFont(italicBoldFont);
					item.setData( KEY_EXISTS_IN_TARGET, Boolean.TRUE);
					if (context.getOverwriteExistingInTarget()) {
						item.setImage(deselectedImage);
					}
				} else {
					item.setFont(boldFont);
					item.setImage(deselectedImage);
				}
			}
		});		
	}

	


	@Override
	public void acknowledgeScanAbortedAsArtifactIsPresentInTarget( final RepositorySetting target, final Solution artifact, final Set<Artifact> parents) {
		if (listed( artifact))
			return;
		display.asyncExec( new Runnable() { 
			@Override
			public void run() {
				TreeItem item = TreeItemHelper.createItem( flatTree, items, itemToDuplicates, target, artifact);																				
				TreeItem parts = TreeItemHelper.attachParts(item, artifact);
				if (parts != null)
					parts.setImage(partsImage);
				item.setFont( italicFont);
				item.setImage( scanEndPointImage);
				item.setData( KEY_SCAN_END, Boolean.TRUE);
				item.setData( KEY_EXISTS_IN_TARGET, Boolean.TRUE);
			}
		});				
	}
	
	

	@Override
	public void acknowledgeUnresolvedArtifact(final List<RepositorySetting> sources, final Dependency dependency, final Collection<Artifact> requestors) {
		if (listed( dependency))
			return;
		display.asyncExec( new Runnable() { 
			@Override			
			public void run() {								
				TreeItem item = TreeItemHelper.createItem( flatTree, items, itemToDuplicates, null, dependency);		
				item.setImage(unresolvedImage);
				item.setData( KEY_UNRESOLVED, true);
			}
		});		
	}

	@Override
	public void acknowledgeStartScan() {
		display.asyncExec( new Runnable() { 
			@Override
			public void run() {
				flatTree.removeAll();
				items.clear();
				itemToDuplicates.clear();
				listed.clear();
			}
		});
		
	}

	public List<Part> getPartsToUpload(){
		return null;
	}
	
	@Override
	public void acknowledgeStopScan() {		
	}


	@Override
	public void acknowledgeArtifactSelected(Solution solution) {
		Set<TreeItem> items = new HashSet<TreeItem>();		
		items.addAll( Arrays.asList( flatTree.getItems()));		
		TreeItem item = TreeItemHelper.findTreeItem( items, solution);
		if (item != null) {
			if (
					context == null ||
					context.getOverwriteExistingInTarget() ||
					context.getRepairExistingInTarget() ||
					!Boolean.TRUE.equals( item.getData( KEY_EXISTS_IN_TARGET))
				){
				item.setData(KEY_SELECTION,  Boolean.TRUE);
				if (!context.getOverwriteExistingInTarget()) {
					if (Boolean.TRUE.equals(item.getData(KEY_EXISTS_IN_TARGET))) {
							item.setImage( (Image) null);
					} else {
						item.setImage( selectedImage);
					}						
				} 
				else {
					item.setImage( selectedImage);
				}
				// select all parts 
				if (
						context != null && 
						context.getRepairExistingInTarget()
					) {
					TreeItem [] partItems = item.getItems();
					if (partItems != null) {
						for (TreeItem partItem : partItems) {
							partItem.setData( KEY_SELECTION, true);
							partItem.setImage( selectedImage);
						}
					}
				}
			}			
		}
	}

	@Override
	public void acknowledgeArtifactDeSelected(Solution solution) {
		Set<TreeItem> items = new HashSet<TreeItem>();		
		items.addAll( Arrays.asList( flatTree.getItems()));		
		TreeItem item = TreeItemHelper.findTreeItem( items, solution);
		if (item != null) {
			item.setData(KEY_SELECTION,  Boolean.FALSE);
			if (!context.getOverwriteExistingInTarget())
				item.setImage( (Image) null);
			else
				item.setImage( deselectedImage);
			// select all parts 
			if (
					context != null && 
					context.getRepairExistingInTarget()
				) {
				TreeItem [] partItems = item.getItems();
				if (partItems != null) {
					for (TreeItem partItem : partItems) {
						partItem.setData( KEY_SELECTION, false);
						partItem.setImage( deselectedImage);
					}
				}
			}
		}
	}
	
	
		
	@Override
	public void acknowledgePartSelected(Part part) {			
	}


	@Override
	public void acknowledgePartDeSelected(Part part) {		
	}


	@Override
	public void acknowledgeSelectionContext(String id, SelectionContext selectionContext) {
		context = selectionContext;
		
		display.asyncExec( new Runnable() { 
			@Override
			public void run() {
		
				List<TreeItem> partItemList = new ArrayList<TreeItem>();			
				TreeItem [] items = flatTree.getItems();
				if (items != null) {
					for (TreeItem item : items) {				
						Object artifact = item.getData(KEY_ARTIFACT);
						if (artifact instanceof Dependency)
							continue;
						Solution solution = (Solution) artifact;
						if (solution != null) {
							TreeItem [] partItems = item.getItems();
							if (partItems != null) {
								partItemList.addAll( Arrays.asList( partItems));							
							}	
							//
							// set the solution's features 
							//
							if (Boolean.TRUE.equals( item.getData( KEY_EXISTS_IN_TARGET))) {
								if (context.getOverwriteExistingInTarget()) {
									if (Boolean.TRUE.equals( item.getData( KEY_SELECTION))) {
										item.setImage(selectedImage);
									}
									else {
										item.setImage(deselectedImage);
									}
								}
								else {
									item.setImage( (Image) null);
								}
							}
						}
					}
				}
				//
				// attached parts
				//
				// depending on the sets, we must structure our view
				if (
						context.getOverwriteExistingInTarget() ||
						context.getRepairExistingInTarget() == false
					) {
					// remove all selection image, and switch to identification images									
					for (TreeItem partItem : partItemList) {
						Part part = (Part) partItem.getData( KEY_PART);
						if (part == null)
							continue;					
						PartType type = PartTupleProcessor.toPartType(part.getType());
						if (type == null) {
							type = PartType._UNKNOWN_;
						}					
						setPartItemImage(partItem, type, false, null);
					}					
				}
				else if (context.getRepairExistingInTarget()) {
					// set images to selection images, depending on the selection state  
					// remove all selection image, and switch to identification images									
					for (TreeItem partItem : partItemList) {
						Part part = (Part) partItem.getData( KEY_PART);
						if (part == null)
							continue;						
						PartType type = PartTupleProcessor.toPartType(part.getType());
						if (type == null) {
							type = PartType._UNKNOWN_;
						}
						setPartItemImage(partItem, type, true, null);						
					}
				}
			}
		});
	}


	@Override
	public void widgetSelected(SelectionEvent e) {		
		// structural selection 
		if (e.widget == selectRelated) {
			// 
			TreeItem [] items = flatTree.getSelection();
			if (items == null || items.length == 0)
				return;
			TreeItem root = items[0];
			Identification identification = (Identification) root.getData( KEY_ARTIFACT);
			if (identification == null || identification instanceof Dependency)
				return;	
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
		// simple selection 
		if (e.widget == select) {
			TreeItem [] items = flatTree.getSelection();
			if (items == null || items.length == 0)
				return;
			for (TreeItem item : items) {
				Identification identification = (Identification) item.getData( KEY_ARTIFACT);			
				if (identification != null && identification instanceof Solution == false) {
					return;
				}
				//
				if (identification == null) {
					identification = (Identification) item.getData( KEY_PART);
				}
			
				if (Boolean.TRUE.equals( item.getData( KEY_EXISTS_IN_TARGET))) {
					if (identification instanceof Part) {		
						if (!context.getRepairExistingInTarget())
							continue;
					}
					else if (identification instanceof Solution) {
						if (!context.getOverwriteExistingInTarget())
							continue;
					}
				}
				boolean select = true;
				if (Boolean.TRUE.equals(item.getData( KEY_SELECTION))) {
					select = false;
				}
				if (select) {
					if (identification instanceof Part) {
						item.setData(KEY_SELECTION, true);
						Part part = (Part) identification;
						handlePartItemSelection( item, part, null);
						selectionListener.acknowledgePartSelected((Part) identification); 
					}
					else { 
						selectionListener.acknowledgeArtifactSelected((Solution) identification);
					}
				} 
				else {
					if (identification instanceof Part) {
						item.setData(KEY_SELECTION, false);
						Part part = (Part) identification;
						handlePartItemSelection( item, part, null);
						selectionListener.acknowledgePartDeSelected((Part) identification);
					}
					else {
						selectionListener.acknowledgeArtifactDeSelected((Solution) identification);
					}
				}
					
			}
		}
	}


	@Override
	public void widgetDefaultSelected(SelectionEvent e) {}
	
	@Override
	public void acknowledgeUploadBegin(RepositorySetting setting, int count) {}
	@Override
	public void acknowledgeUploadEnd(RepositorySetting setting) {}
	@Override
	public void acknowledgeUploadSolutionBegin(RepositorySetting setting, Solution solution) {}
	@Override
	public void acknowledgeUploadSolutionFail(RepositorySetting setting, Solution solution) {}
	@Override
	public void acknowledgeFailedPart(RepositorySetting setting, final Solution solution, final Part part, String reason, int worked) {
		display.asyncExec( new Runnable() { 
			@Override
			public void run() {						
				List<TreeItem> items = TreeItemHelper.collectTreeItems(flatTree);			
				TreeItem solutionItem = TreeItemHelper.findTreeItem(items, solution);
				if (solutionItem != null) {
					handleExpansion(solutionItem);
					solutionItem.setImage( containsSelectedPartImage);
				}
				
				List<TreeItem> siblings = new ArrayList<TreeItem>( Arrays.asList( solutionItem.getItems()));
				TreeItem item = TreeItemHelper.findTreeItem( siblings, part);
				if (item == null) {
					item = createItemForPart(solutionItem, part);
				}				
				item.setData( KEY_SELECTION, true);
				item.setImage(selectedImage);
				item.setData( KEY_EXISTS_IN_TARGET, false);
								
			}
		});
	}
	
	
	@Override
	public void acknowledgeFailPartCRC(RepositorySetting setting, Solution solution, Part part, String reason, int index) {
		acknowledgeFailedPart(setting, solution, part, reason, index);		
	}

	@Override
	public void acknowledgeRootSolutions(RepositorySetting setting,Set<Solution> solution) {}


	@Override
	public void acknowledgeUploadedPart(RepositorySetting setting, final Solution solution, final Part part, long time, int worked) {
		display.asyncExec( new Runnable() { 
			@Override
			public void run() {				
				List<TreeItem> items = TreeItemHelper.collectTreeItems(flatTree);								
				TreeItem solutionItem = TreeItemHelper.findTreeItem(items, solution);
				if (solutionItem != null) {
					handleExpansion(solutionItem);				
				}
				List<TreeItem> siblings = new ArrayList<TreeItem>( Arrays.asList( solutionItem.getItems()));
				TreeItem item = TreeItemHelper.findTreeItem( siblings, part);
				if (item == null) {
					item = createItemForPart(solutionItem, part);
				}
			
				if (item != null) {
					item.setData( KEY_EXISTS_IN_TARGET, true);
					item.setData( KEY_SELECTION, false);
					if (context.getRepairExistingInTarget()){									
						item.setImage(deselectedImage);
					}		
				}
			}
		});
	}
	@Override
	public void acknowledgeUploadSolutionEnd(final RepositorySetting setting, final Solution solution) {
		display.asyncExec( new Runnable() { 
			@Override
			public void run() {						
				List<TreeItem> items = TreeItemHelper.collectTreeItems(flatTree);							
				TreeItem item = TreeItemHelper.findTreeItem( items, solution);
				if (item != null) {
					item.setData( KEY_EXISTS_IN_TARGET, true);
					item.setData( KEY_SELECTION, false);
					if (Boolean.TRUE.equals( item.getData (KEY_ROOT))) {
						item.setFont(italicBoldFont);
					} else { 
						item.setFont(italicFont);
					}
					if (!context.getRepairExistingInTarget()) {
						item.setImage(deselectedImage);
					} 
					else {
						item.setImage((Image) null); 
					}
				}
			}
		});
	}

	@Override
	public Set<Solution> getSelectedSolutions() {	
		return null;
	}

	@Override
	public Set<Part> getSelectedParts() {
		Set<Part> parts = new HashSet<Part>();
		if (!context.getRepairExistingInTarget())
			return parts;		
		for (TreeItem item : TreeItemHelper.collectTreeItems(flatTree)) {		
			if (Boolean.TRUE.equals( item.getData( KEY_SELECTION))) {
				Part part = (Part) item.getData( KEY_PART);
				if (part != null)
					parts.add( part);
			}												
		}
		return parts;
	}

	@Override
	public void expand() {
		treeExpander.expandTree(flatTree, true);
		
	}

	@Override
	public void condense() {
		treeExpander.expandTree(flatTree, false);		
	}

	@Override
	public boolean isCondensed() {
		return false;
	}

	@Override
	public void selectAll() {
		for (TreeItem item : flatTree.getItems()) {
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
		for (TreeItem item : flatTree.getItems()) {
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
	public void loadPom() {
		TreeItem [] items = flatTree.getSelection();
	
		if (items == null || items.length == 0) {
			return;
		}
		PomLoader.loadPoms(items);		
	}

	@Override
	public String copyContents() {
		TreeItem [] items = flatTree.getItems();
		if (items == null)
			return "";
		StringBuilder builder = new StringBuilder();
		int i = 0;
		for (TreeItem item : items) {
			if (builder.length() > 0) {
				builder.append("\n");
			}
			builder.append( item.getText());
			builder.append( "\t");
			builder.append( item.getData( KEY_TOOLTIP));
			if (Boolean.TRUE.equals( item.getData(KEY_UNRESOLVED))) {
				;
			}
			else {
				Solution solution = (Solution) item.getData( KEY_ARTIFACT);
				builder.append("\n");
				builder.append(ClipboardCopier.processArtifact(solution, i+1));
			}
			builder.append("\n");
		}
		return builder.toString();
	}
	
	
}
