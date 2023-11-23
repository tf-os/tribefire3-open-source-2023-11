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
package com.braintribe.devrock.greyface.view.tab.result;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import com.braintribe.cc.lcd.CodingSet;
import com.braintribe.devrock.greyface.generics.tree.TreeColumnResizer;
import com.braintribe.devrock.greyface.generics.tree.TreeExpander;
import com.braintribe.devrock.greyface.generics.tree.TreeItemTooltipProvider;
import com.braintribe.devrock.greyface.process.notification.ScanProcessListener;
import com.braintribe.devrock.greyface.process.notification.UploadProcessListener;
import com.braintribe.devrock.greyface.process.retrieval.TempFileHelper;
import com.braintribe.devrock.greyface.view.tab.HasTreeTokens;
import com.braintribe.devrock.greyface.view.tab.TreeItemHelper;
import com.braintribe.devrock.greyface.view.tab.selection.SelectionCodingCodec;
import com.braintribe.devrock.greyface.views.dependency.tabs.capability.expansion.ViewExpansionCapable;
import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Identification;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.malaclypse.cfg.preferences.gf.RepositorySetting;

public class FlatResultTab extends CommonResultSubTab implements UploadProcessListener, HasTreeTokens, ScanProcessListener, ViewExpansionCapable {

	private static final String COLLECTED_UPLOAD_RESULTS = "collected upload results";
	private static final String LAST_UPLOAD_RESULTS = "last upload results";
	private TreeColumnResizer flatTreeColumnResizer;
	private Tree flatTree;
	private Label scanResultTitleLabel;
	private boolean lastResultsOnly;
	private TreeSet<String> items;
	private Map<String, Set<TreeItem>> itemToDuplicates = new HashMap<String, Set<TreeItem>>();
	private Set<Identification> listed = new HashSet<Identification>();
	private int countParts=0;	
	
	private Set<Solution>  rootSolutions = CodingSet.createHashSetBased( new SelectionCodingCodec());
	
	public FlatResultTab(Display display, boolean lastResultsOnly) {
		super(display);		
		this.lastResultsOnly = lastResultsOnly;
		items = new TreeSet<String>( new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {			
				return o1.compareTo(o2);
			}			
		});		
	}
		
	@Override
	public void dispose() {		
		super.dispose();
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
		scanComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true));
		
		scanResultTitleLabel = new Label( scanComposite, SWT.NONE);
		
		setTitle( null);
			
    	scanResultTitleLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 4,1));
    	scanResultTitleLabel.setFont( bigFont);
    	
		GridLayout scanLayout = new GridLayout();
    	scanLayout.numColumns = 4;
    	scanLayout.marginHeight = 0;
    	scanLayout.verticalSpacing = 0;
    	scanLayout.marginWidth = 0;
    	
		
		Composite treeComposite = new Composite(scanComposite, SWT.NONE);
		treeComposite.setLayout(scanLayout);
		treeComposite.setLayoutData(new GridData( SWT.FILL, SWT.FILL, true, true, 4, 1));
		
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
		
		//flatTree.addListener(SWT.Expand, this);		
		TreeItemTooltipProvider.attach(flatTree, KEY_TOOLTIP);
			
		return composite;		
	}

	private void setTitle( Integer worked) {
		
		if (lastResultsOnly) {
			if (worked == null) {
				scanResultTitleLabel.setText( LAST_UPLOAD_RESULTS);
			}
			else {
				scanResultTitleLabel.setText( LAST_UPLOAD_RESULTS + "(" + worked + " of " + countParts + ")");
			}
		} 
		else {
			if (worked == null) {
				scanResultTitleLabel.setText( COLLECTED_UPLOAD_RESULTS);
			}
			else {
				scanResultTitleLabel.setText( COLLECTED_UPLOAD_RESULTS + "(" + worked + " of " + countParts + ")");
			}
		}
		
	}

	@Override
	public void adjustSize() {
		flatTreeColumnResizer.resize();
	}

	@Override
	public void acknowledgeUploadBegin(RepositorySetting setting, int count) {
		countParts = count;
		if (!lastResultsOnly)
			return;
		display.asyncExec( new Runnable() { 
			@Override			
			public void run() {
				flatTree.removeAll();
				listed.clear();
				items.clear();
				itemToDuplicates.clear();
				rootSolutions.clear();
			}
		});
	}

	@Override
	public void acknowledgeUploadEnd(RepositorySetting setting) {
		display.asyncExec( new Runnable() { 
			@Override			
			public void run() {
				setTitle(null);
			}
		});
	}



	@Override
	public void acknowledgeUploadSolutionBegin(final RepositorySetting setting, final Solution solution) {
		display.asyncExec( new Runnable() { 
			@Override			
			public void run() {							
				// build tree item for the solution, and set the activity image
				Set<TreeItem> treeItems = new HashSet<TreeItem>();
				treeItems.addAll( Arrays.asList(flatTree.getItems()));
				TreeItem item = TreeItemHelper.findTreeItem(treeItems, solution);
				if (item == null) {
					item = TreeItemHelper.createItem( flatTree, items, itemToDuplicates, setting, solution);
					if (rootSolutions.contains(solution)) {
						item.setFont( boldFont);
					}
				}											
				item.setImage(activityImage);				
			}
		});
	}



	@Override
	public void acknowledgeUploadSolutionEnd(final RepositorySetting setting, final Solution solution) {
		display.asyncExec( new Runnable() { 
			@Override			
			public void run() {	
				Set<TreeItem> treeItems = new HashSet<TreeItem>();
				treeItems.addAll( Arrays.asList(flatTree.getItems()));
				TreeItem item = TreeItemHelper.findTreeItem(treeItems, solution);
				if (item != null)
					item.setImage(successImage);
			}
		});		
	}



	@Override
	public void acknowledgeUploadSolutionFail(final RepositorySetting setting, final Solution solution) {
		display.asyncExec( new Runnable() { 
			@Override			
			public void run() {	
				// find the tree item for the solution, and set the fail image 
				Set<TreeItem> treeItems = new HashSet<TreeItem>();
				treeItems.addAll( Arrays.asList(flatTree.getItems()));
				TreeItem item = TreeItemHelper.findTreeItem(treeItems, solution);
				if (item != null) {
					item.setImage(failImage);
					item.setData( KEY_TOOLTIP, "upload failed: do you have write access to [" + setting.getName() + "] at [" + setting.getUrl() + "] ?");
				}
			}
		});
	}

	private int getInsertionIndex(TreeItem [] items, String label){
		TreeSet<String> treeSet = new TreeSet<String>( new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}			
		});
		for (TreeItem item : items) {
			treeSet.add( item.getText());
		}
		treeSet.add( label);
		return treeSet.headSet(label).size();
	}


	@Override
	public void acknowledgeUploadedPart(final RepositorySetting setting, final Solution solution, final Part part, final long time, final int worked) {
		display.asyncExec( new Runnable() { 
			@Override			
			public void run() {
				// update title 
				setTitle(worked);
				// find the tree item for the part, set the success image to it, and prepare the tool tip with the time  
				Set<TreeItem> treeItems = new HashSet<TreeItem>();
				treeItems.addAll( Arrays.asList(flatTree.getItems()));
				TreeItem item = TreeItemHelper.findTreeItem(treeItems, solution);
				if (item == null) {
					return;
				}
				List<TreeItem> siblings = new ArrayList<TreeItem>( Arrays.asList( item.getItems()));
				TreeItem partItem = TreeItemHelper.findTreeItem( siblings, part);
				if (partItem == null) {
										
					String location = part.getLocation().replace('\\', '/');
					if (TempFileHelper.isATempFile(location)) {
						location = TempFileHelper.extractFilenameFromTempFile(location);
					}
					String label = TreeItemHelper.getName(part);
					int p = getInsertionIndex( item.getItems(), label);
					partItem = new TreeItem( item, SWT.NONE, p);
					partItem.setText( label);				
					partItem.setData( KEY_PART, part);
					partItem.setData( KEY_TOOLTIP, location + " : uploaded in ["  + time / 1E6 + "] ms");
					partItem.setImage(successImage);
				} 
				else {
					String location = part.getLocation().replace('\\', '/');
					if (TempFileHelper.isATempFile(location)) {
						location = TempFileHelper.extractFilenameFromTempFile(location);
					}
					partItem.setData( KEY_TOOLTIP, location + " : uploaded in ["  + time / 1E6 + "] ms");
					partItem.setImage(successImage);
				}				
			}
		});
	}

	@Override
	public void acknowledgeFailedPart(final RepositorySetting setting, final Solution solution, final Part part, final String reason, final int worked) {
		display.asyncExec( new Runnable() { 
			@Override			
			public void run() {	
				// update title
				setTitle(worked);
				// find the tree item for the part, set the fail image and prepare the tooltip with reason 
				Set<TreeItem> treeItems = new HashSet<TreeItem>();
				treeItems.addAll( Arrays.asList(flatTree.getItems()));
				TreeItem item = TreeItemHelper.findTreeItem(treeItems, solution);
				if (item == null) {
					return;
				}
				List<TreeItem> siblings = new ArrayList<TreeItem>( Arrays.asList( item.getItems()));
				TreeItem partItem = TreeItemHelper.findTreeItem( siblings, part);
				if (partItem == null) {									
					String location = part.getLocation().replace('\\', '/');
					// this is still a local file name ?..
					if (TempFileHelper.isATempFile(location)) {
						location = TempFileHelper.extractFilenameFromTempFile(location);
					}
					String label = TreeItemHelper.getName(part);
					int p = getInsertionIndex( item.getItems(), label);
					partItem = new TreeItem( item, SWT.NONE, p);
					partItem.setText( label);
					partItem.setData( KEY_PART, part);
					partItem.setData( KEY_TOOLTIP, "upload failed:  ["  + reason + "]");
					partItem.setImage(failImage);
				}
				else {
					// actually only needed for test - how would retry to upload a file that just has been successfully uploaded? 
					String location = part.getLocation().replace('\\', '/');
					if (TempFileHelper.isATempFile(location)) {
						location = TempFileHelper.extractFilenameFromTempFile(location);						
					}
					partItem.setData( KEY_TOOLTIP, "upload failed:  ["  + reason + "]");
					partItem.setImage(failImage);
				}
			}
		});
	}
	
	
	
	@Override
	public void acknowledgeFailPartCRC(RepositorySetting setting, Solution solution, Part part, String reason, int index) {
		acknowledgeFailedPart(setting, solution, part, reason, index);		
	}

	@Override
	public void acknowledgeRootSolutions(RepositorySetting setting,Set<Solution> solutions) {
		rootSolutions.addAll(solutions);	
	}

	@Override
	public void acknowledgeStartScan() {
		display.asyncExec( new Runnable() { 
			@Override			
			public void run() {
				flatTree.removeAll();
				listed.clear();
				items.clear();
				itemToDuplicates.clear();
				rootSolutions.clear();
			}
		});	
	}
	
	@Override
	public void expand() {	
		TreeExpander.expand(flatTree);
	}

	@Override
	public void condense() {
		TreeExpander.collapse( flatTree);
	}

	@Override
	public boolean isCondensed() {
		return false;
	}

	@Override
	public void acknowledgeStopScan() {}

	@Override
	public void acknowledgeScanAbortedAsArtifactIsPresentInTarget( RepositorySetting target, Solution artifact, Set<Artifact> parents) {}

	@Override
	public void acknowledgeScannedArtifact(RepositorySetting setting, Solution artifact, Set<Artifact> parents, boolean presentInTarget) {}

	@Override
	public void acknowledgeScannedParentArtifact(RepositorySetting setting, Solution artifact, Artifact child, boolean presentInTarget) {}

	@Override
	public void acknowledgeScannedRootArtifact(RepositorySetting setting, Solution artifact, boolean presentInTarget) {}

	@Override
	public void acknowledgeUnresolvedArtifact(List<RepositorySetting> sources, Dependency dependency, Collection<Artifact> requestors) {}
}
