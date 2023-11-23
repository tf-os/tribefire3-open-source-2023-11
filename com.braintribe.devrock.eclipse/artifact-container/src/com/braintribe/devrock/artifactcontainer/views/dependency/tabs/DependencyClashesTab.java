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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TreeItem;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.cc.lcd.CodingSet;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerStatus;
import com.braintribe.devrock.artifactcontainer.control.workspace.ArtifactWrapperCodec;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.clipboard.ClipboardContentsProviderCapable;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.clipboard.ClipboardContentsProviderMode;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.expansion.ViewExpansionCapable;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.filter.FilterCapable;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.filter.FilterPatternBuilder;
import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.artifact.processing.version.VersionRangeProcessor;
import com.braintribe.model.malaclypse.container.DependencyContainer;
import com.braintribe.plugin.commons.ui.tree.TreeExpander;



public class DependencyClashesTab extends AbstractDependencyViewTab implements 	ViewExpansionCapable,
																				FilterCapable,
																				ClipboardContentsProviderCapable {
	private Image requesterImage;
	private boolean condensed = true;
	private boolean filterActive = false;

	public DependencyClashesTab(Display display) {
		super(display);
		setColumnNames( new String [] {"Dependency", "Version", "Group", "Index", "Depth"});
		setColumnWeights( new int [] {100, 50, 300, 50, 50});
		
		ImageDescriptor imageDescriptor = ImageDescriptor.createFromFile( DependencyClashesTab.class, "requester.png");
		requesterImage = imageDescriptor.createImage();
		
	}
	

	@Override
	public void dispose() {
		requesterImage.dispose();
		super.dispose();
	}

	@Override
	protected void buildContents(boolean interactive) {
		broadcastTabState();
		Map<Dependency, DependencyContainer> clashes = monitoringResult.getDependencyClashes();
		
		String pattern = FilterPatternBuilder.getPattern();
		for (Entry<Dependency, DependencyContainer> entry : clashes.entrySet()) {			
			Dependency winner = entry.getKey();
			// filter
			if (filterActive && pattern != null && pattern.length() > 0) {
				String name = NameParser.buildName(winner);
				if (!name.matches(pattern))
					continue;
			}
			
			DependencyContainer container = entry.getValue();
			Set<Dependency> loosers = container.getDependencies();
			Set<Artifact> looserRequesters = CodingSet.createHashSetBased( new ArtifactWrapperCodec());
			for (Dependency looser : loosers) {
				looserRequesters.addAll( looser.getRequestors());
			}
			TreeItem winnerItem = buildEntryForWinner( winner);
			
			buildEntry( winnerItem, winner, looserRequesters);
			for (Dependency looser : loosers) {
				buildEntry( winnerItem, looser, null);
			}
		}
	}
		
	private TreeItem buildEntryForWinner( Dependency dependency) {
		TreeItem item = new TreeItem( tree, SWT.NONE);
		List<String> texts = new ArrayList<String>();
		texts.add( dependency.getArtifactId());
		texts.add( VersionRangeProcessor.toString( dependency.getVersionRange()));
		texts.add( dependency.getGroupId());
		
		Integer pathIndex = dependency.getPathIndex();
		texts.add( pathIndex != null ? pathIndex.toString() : "n/a");
		
		Integer hierarchyLevel = dependency.getHierarchyLevel();
		texts.add( hierarchyLevel != null ? hierarchyLevel.toString() : "n/a");
		
		item.setText( texts.toArray( new String[0]));				
			
		return item;
	}
	
	private TreeItem buildEntry( TreeItem parent, Dependency dependency, Set<Artifact> blockedRequesters){
		TreeItem item = new TreeItem( parent, SWT.NONE);		
		
		List<String> texts = new ArrayList<String>();
		texts.add( dependency.getArtifactId());		
		texts.add( VersionRangeProcessor.toString( dependency.getVersionRange()));		
		texts.add( dependency.getGroupId());
		
		Integer pathIndex = dependency.getPathIndex();
		texts.add( pathIndex != null ? pathIndex.toString() : "n/a");
		
		Integer hierarchyLevel = dependency.getHierarchyLevel();
		texts.add( hierarchyLevel != null ? hierarchyLevel.toString() : "n/a");
					
		item.setText( texts.toArray( new String[0]));				
		
		attachRequesters(item, dependency, blockedRequesters);
		return item;
	}
	
	private void attachRequesters( TreeItem parent, Dependency dependency, Set<Artifact> blockedRequesters) {
		Set<Artifact> requestors = dependency.getRequestors();
		for (Artifact requester : requestors) {
			if (blockedRequesters != null && blockedRequesters.contains(requester))
				continue;			
			Solution solution = (Solution) requester;
			TreeItem item = new TreeItem( parent, SWT.NONE);
			List<String> texts = new ArrayList<String>();
			texts.add( solution.getArtifactId());			
			texts.add( VersionProcessor.toString( solution.getVersion()));			
			texts.add( solution.getGroupId());
			
			//int hierarchyLevel = solution.getHierarchyLevel();
			//texts.add( "" + hierarchyLevel);
			
			item.setImage( requesterImage);
			item.setData( PainterKey.image.toString(), requesterImage);
			
			// find requesters' dependencies index
			List<Integer> hierarchy = new ArrayList<Integer>();
			List<Integer> indexes = new ArrayList<Integer>();
			for (Dependency requesterDep : requester.getDependencies()) {
				hierarchy.add(requesterDep.getHierarchyLevel());
				indexes.add( requesterDep.getPathIndex());
			}
			Comparator<Integer> comparator = new Comparator<Integer>() {
				@Override
				public int compare(Integer value0, Integer value1) {				
					return value0.compareTo(value1);
				}				
			};
			hierarchy.sort(comparator);
			indexes.sort(comparator);
			// indexes as strings 
			StringBuffer buffer = new StringBuffer();
			for (int i : indexes) {
				if (buffer.length() > 0) {
					buffer.append(",");
				}
				buffer.append( i);
			}
			texts.add( buffer.toString());
			// hierarchy as string
			buffer = new StringBuffer();
			for (int i : hierarchy) {
				if (buffer.length() > 0) {
					buffer.append(",");
				}
				buffer.append( i);
			}
			texts.add( buffer.toString());
			
			item.setText( texts.toArray( new String[0]));
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
				//populateView( false);
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
				populateView( true);
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
	public boolean supportsMode(ClipboardContentsProviderMode mode) {
		switch (mode) {
		case standard:
			return true;
		case enhanced:
		default:
			return false;			
		}
	}


	@Override
	public String apply(ClipboardContentsProviderMode mode) throws RuntimeException {
		StringBuilder builder = new StringBuilder();
		Map<Dependency, DependencyContainer> clashes = monitoringResult.getDependencyClashes();
		
		String pattern = FilterPatternBuilder.getPattern();
		for (Entry<Dependency, DependencyContainer> entry : clashes.entrySet()) {			
			Dependency winner = entry.getKey();
			// filter
			if (filterActive && pattern != null && pattern.length() > 0) {
				String name = NameParser.buildName(winner);
				if (!name.matches(pattern))
					continue;
			}
			
			DependencyContainer container = entry.getValue();
			Set<Dependency> loosers = container.getDependencies();
			Set<Artifact> looserRequesters = CodingSet.createHashSetBased( new ArtifactWrapperCodec());
			for (Dependency looser : loosers) {
				looserRequesters.addAll( looser.getRequestors());
			}
			if (builder.length() > 0)
				builder.append( System.lineSeparator());
			// winner - topic   
			builder.append( NameParser.buildName( winner));
			builder.append( System.lineSeparator());
														
			// winner requester
			builder.append(buildString( winner, looserRequesters));
			// looser requester
			for (Dependency looser : loosers) {
				builder.append( System.lineSeparator());
				builder.append( buildString( looser, null));
			}
		}		
		return builder.toString();
	}

	private String buildString( Dependency dependency, Set<Artifact> blockedRequesters) {
		StringBuilder builder = new StringBuilder();
		builder.append( "\t");
		builder.append( NameParser.buildName(dependency));
		for (Artifact requester : dependency.getRequestors()) {
			if (blockedRequesters != null && blockedRequesters.contains(requester)) {
				continue;
			}
			builder.append( System.lineSeparator());
			builder.append("\t\t");
			builder.append( NameParser.buildName(requester));			
		}		
		return builder.toString();
	}


	@Override
	protected void broadcastTabState() {
		if (!ensureMonitorData()) {
			super.broadcastTabState();
			return;
		}
		Map<Dependency, DependencyContainer> clashes = monitoringResult.getDependencyClashes();
		if (clashes.size() == 0) {
			super.broadcastTabState();
		}
		else {
			setTabState( DependencyViewTabState.warningState);
		}
	}
	
	
}
