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
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerStatus;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.expansion.ViewExpansionCapable;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.pom.PomLoader;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.pom.PomLoadingCapable;
import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.artifact.processing.version.VersionRangeProcessor;
import com.braintribe.plugin.commons.ui.tree.TreeExpander;

public class MergedDependenciesTab extends AbstractDependencyViewTab implements PomLoadingCapable, ViewExpansionCapable {
	private Image mergeImage;
	private Image requesterImage;
	private boolean condensed = true;

	public MergedDependenciesTab(Display display) {
		super(display);
		setColumnNames( new String [] { "Dependency", "Resulting Range", "Requested Range",  "Group"});
		setColumnWeights( new int [] { 150, 50, 50, 250});
		
		ImageDescriptor imageDescriptor = ImageDescriptor.createFromFile( MergedDependenciesTab.class, "merge.gif");
		mergeImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( MergedDependenciesTab.class, "requester.png");
		requesterImage = imageDescriptor.createImage();			
	}
	
	@Override
	public void dispose() {
		mergeImage.dispose();
		requesterImage.dispose();
		super.dispose();
	}

	@Override
	protected void buildContents(boolean interactive) {
		Set<Dependency> mergedDependencies = monitoringResult.getMergedDependencies();
		if (mergedDependencies.size() == 0) {
			setTabState( DependencyViewTabState.noState);
			return;
		}
		setTabState(DependencyViewTabState.validState);
		for (Dependency dependency : mergedDependencies) {
			buildEntry( tree, dependency);
		}
	}
	
	private void buildEntry( Object parent, Dependency dependency){
		// build 
		TreeItem item;
		
		if (parent instanceof Tree) {
			item = new TreeItem( (Tree) parent, SWT.NONE);
		}
		else {
			item = new TreeItem( (TreeItem) parent, SWT.NONE);
		}
		List<String> texts = new ArrayList<String>();
		texts.add( dependency.getArtifactId());
		Set<Dependency> mergedParents = dependency.getMergeParents();
		
		if (mergedParents.size() > 0) {
	
			texts.add( VersionRangeProcessor.toString( dependency.getVersionRange()));
			texts.add("");
		}
		else {
			texts.add("");
			texts.add( VersionRangeProcessor.toString( dependency.getVersionRange()));
		}
		texts.add( dependency.getGroupId());
		item.setText( texts.toArray( new String[0]));
		if (mergedParents.size() > 0) {
			item.setImage( mergeImage);
			item.setData( PainterKey.image.toString(), mergeImage);
		}
		else {
			attachRequesters(item, dependency);
		}
		
		// 
		for (Dependency merged : mergedParents) {
			buildEntry( item, merged);
		}
				
	}
	
	private void attachRequesters( TreeItem parent, Dependency dependency) {
		Set<Artifact> requesters = dependency.getRequestors();
		for (Artifact requester : requesters) {
			TreeItem item = new TreeItem ( parent, SWT.NONE);
			List<String> texts = new ArrayList<String>();
			texts.add( requester.getArtifactId());
			texts.add( "");		
			texts.add( VersionProcessor.toString( requester.getVersion()));
			texts.add( requester.getGroupId());
			item.setText( texts.toArray( new String[0]));
			
			item.setImage( requesterImage);
			item.setData( PainterKey.image.toString(), requesterImage);
			PomLoader.attachPomToTreeItem(item, (Solution) requester); 
		}
	}
	
	@Override
	public void importPom() {
		TreeItem [] items = tree.getSelection();
		if (items != null) {
			PomLoader.loadPom(items);
		}		
	}

	@Override
	protected void broadcastTabState() {
		if (!ensureMonitorData()) {
			super.broadcastTabState();
			return;
		}
		Set<Dependency> mergedDependencies = monitoringResult.getMergedDependencies();
		if (mergedDependencies.size() == 0) {
			super.broadcastTabState();
			return;
		}
		setTabState(DependencyViewTabState.validState);
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

}
