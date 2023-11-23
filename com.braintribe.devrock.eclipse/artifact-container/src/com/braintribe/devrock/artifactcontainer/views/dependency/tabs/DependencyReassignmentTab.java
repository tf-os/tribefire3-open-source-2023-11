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
package com.braintribe.devrock.artifactcontainer.views.dependency.tabs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.braintribe.build.artifact.retrieval.multi.coding.ArtifactWrapperCodec;
import com.braintribe.build.artifact.retrieval.multi.coding.DependencyWrapperCodec;
import com.braintribe.cc.lcd.CodingMap;
import com.braintribe.cc.lcd.CodingSet;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerStatus;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.clipboard.ClipboardContentsProviderCapable;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.clipboard.ClipboardContentsProviderMode;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.expansion.ViewExpansionCapable;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.filter.FilterCapable;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.pom.PomLoader;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.pom.PomLoadingCapable;
import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.artifact.processing.version.VersionRangeProcessor;
import com.braintribe.model.artifact.version.VersionRange;
import com.braintribe.plugin.commons.ui.tree.TreeExpander;


public class DependencyReassignmentTab extends AbstractDependencyViewTab implements ViewExpansionCapable,
																					FilterCapable,
																					PomLoadingCapable,
																					ClipboardContentsProviderCapable {

	private Image requesterImage;
	private Image reassignedImage;
	private boolean condensed = true;
	private boolean filterActive;

	public DependencyReassignmentTab(Display display) {
		super(display);
		setColumnNames( new String [] { "Artifact", "Assigned Version", "Group"});
		setColumnWeights( new int [] { 200, 50, 200});
		
		ImageDescriptor imageDescriptor = ImageDescriptor.createFromFile( DependencyClashesTab.class, "requester.png");
		requesterImage = imageDescriptor.createImage();		
		
		imageDescriptor = ImageDescriptor.createFromFile( DependencyClashesTab.class, "redirected.pom.gif");
		reassignedImage = imageDescriptor.createImage();		
	}

	
	@Override
	public void dispose() {
		requesterImage.dispose();
		reassignedImage.dispose();
		super.dispose();
	}


	@Override
	protected void buildContents(boolean interactive) {
		Set<Dependency> undeterminedDependencies = monitoringResult.getUndeterminedDependencies();
		if (undeterminedDependencies.size() == 0) {
			setTabState( DependencyViewTabState.noState);
			return;
		}
		setTabState(DependencyViewTabState.warningState);
		
		Map<Dependency, Dependency> reassignments = CodingMap.createHashMapBased( new DependencyWrapperCodec());
		reassignments.putAll( monitoringResult.getDependencyReassignments());
		for (Dependency dependency : undeterminedDependencies) {
			// build entry for undetermined
			TreeItem undeterminedItem = buildEntry(tree, dependency, null);
			// attach requesters
			attachRequesters(undeterminedItem, dependency, null);
			// attach replacement
			Dependency replacement = reassignments.get(dependency);
			if (replacement != null) {
				// attach its requesters
				TreeItem replaceItem = buildEntry(undeterminedItem, replacement, reassignedImage);
				Set<Artifact> blockedRequesters = CodingSet.createHashSetBased( new ArtifactWrapperCodec());
				blockedRequesters.addAll(dependency.getRequestors());
				attachRequesters(replaceItem, replacement, blockedRequesters);
			}
		}		
	}
	
	private TreeItem buildEntry( Object parent, Dependency dependency, Image image) {
		TreeItem item;
		if (parent instanceof Tree) {
			item = new TreeItem ( (Tree) parent, SWT.NONE);
		}
		else {
			item = new TreeItem( (TreeItem) parent, SWT.NONE);
		}
		List<String> texts = new ArrayList<String>();
		texts.add( dependency.getArtifactId());
		if (image == null) {
			texts.add("");			
		}
		else {
			VersionRange range = dependency.getVersionRange();				
			texts.add( VersionRangeProcessor.toString(range));		
		}
		
		texts.add( dependency.getGroupId());		
		item.setText( texts.toArray( new String[0]));
		
		if (image != null) {
			item.setImage(image);
			item.setData( PainterKey.image.toString(), image);
		}
		return item;		
	}

	private void attachRequesters( TreeItem parent, Dependency dependency, Set<Artifact> blockedRequesters) {
		Set<Artifact> requesters = dependency.getRequestors();
		for (Artifact requester : requesters) {
			if (blockedRequesters != null && blockedRequesters.contains(requester)) {
				continue;
			}
			TreeItem requesterItem = new TreeItem( parent, SWT.NONE);
			List<String> texts = new ArrayList<String>();
			texts.add( requester.getArtifactId());
			texts.add( VersionProcessor.toString( requester.getVersion()));			
			texts.add( requester.getGroupId());
			requesterItem.setText( texts.toArray( new String[0]));
			requesterItem.setImage( requesterImage);
			requesterItem.setData( PainterKey.image.toString(), requesterImage);
			Solution solution = (Solution) requester;
			PomLoader.attachPomToTreeItem(requesterItem, solution); 						
		}
	}
	
	@Override
	public void expand() {
		if (!condensed) {
			return;
		}
		int count = tree.getItemCount();
		if (count > 100) {
			String msg = "Message from [" + SolutionTab.class.getName() + "]: Size of tree is at [" + count + "], too big to auto expand";
			ArtifactContainerStatus status = new ArtifactContainerStatus( msg, IStatus.WARNING);
			ArtifactContainerPlugin.getInstance().log(status);		
			return;
		}
		condensed = false;
		if (deferUpdate)
			return;
		display.asyncExec( new Runnable() {			
			@Override
			public void run() {				
				new TreeExpander().expand(tree);
			}
		});
	}


	@Override
	public void condense() {
		if (condensed) {
			return;
		}
		condensed = true;
		if (deferUpdate)
			return;
		display.asyncExec( new Runnable() {			
			@Override
			public void run() {
				new TreeExpander().collapse(tree);
			}
		});
	}


	@Override
	public boolean isCondensed() {	
		return condensed;
	}


	@Override
	public boolean isFilterActive() {
		return filterActive;
	}


	@Override
	public void filter() {
		if (filterActive) 
			return;
		filterActive = true;
		applyFilter();
		
	}


	@Override
	public void stopFilter() {
		if (!filterActive)
			return;
		filterActive = false;
		applyFilter();
	}


	@Override
	public void applyFilter() {
		display.asyncExec( new Runnable() {			
			@Override
			public void run() {
				populateView( condensed);
			}
		});
	}


	@Override
	public void importPom() {
		TreeItem [] items = tree.getSelection();
		if (items != null) {
			PomLoader.loadPom(items);
		}		
	}


	@Override
	public String apply(ClipboardContentsProviderMode mode) throws RuntimeException {				
			return "<no content>";		
	}


	@Override
	public boolean supportsMode(ClipboardContentsProviderMode mode) {
		switch (mode) {
			default:
			return true;
		}
	}


	@Override
	protected void broadcastTabState() {
		if (!ensureMonitorData()) {
			super.broadcastTabState();
			return;
		}
		Set<Dependency> undeterminedDependencies = monitoringResult.getUndeterminedDependencies();
		if (undeterminedDependencies.size() == 0) {
			super.broadcastTabState();			
			return;
		}
		setTabState(DependencyViewTabState.warningState);
	}
	
	
}
