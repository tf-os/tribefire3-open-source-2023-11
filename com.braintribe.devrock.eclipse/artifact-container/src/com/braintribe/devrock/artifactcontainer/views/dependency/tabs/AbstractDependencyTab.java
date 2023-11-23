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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.retrieval.multi.coding.DependencyWrapperCodec;
import com.braintribe.cc.lcd.CodingMap;
import com.braintribe.cc.lcd.CodingSet;
import com.braintribe.devrock.artifactcontainer.control.workspace.ArtifactWrapperCodec;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.filter.FilterCapable;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.filter.FilterPatternBuilder;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.project.ProjectLoader;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.project.ProjectLoadingCapable;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.quickimport.QuickImportCapable;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.quickimport.QuickImportLauncher;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.reposcan.RepositoryScanCapable;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.reposcan.RepositoryScanLauncher;
import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.artifact.ArtifactProcessor;
import com.braintribe.model.artifact.processing.version.VersionRangeProcessor;
import com.braintribe.model.artifact.version.Version;
import com.braintribe.model.malaclypse.container.DependencyContainer;
import com.braintribe.plugin.commons.DependencySortComparator;
import com.braintribe.plugin.commons.selection.TargetProviderImpl;
import com.braintribe.plugin.commons.ui.tree.TreeItemPainter;

public abstract class AbstractDependencyTab extends AbstractDependencyViewTab implements QuickImportCapable, 
																		RepositoryScanCapable,
																		ProjectLoadingCapable,
																		FilterCapable{
	
	protected static final String DATAKEY_DEPENDENCY = "dependency";	
		
	private boolean condensed = true;
	private boolean filterActive = false;
		
	protected Image unresolvedImage;
	protected Image undeterminedImage;
	protected Image leafImage;
	protected Image clashedImage;
	protected Map<Dependency, DependencyContainer> codingMapForDependencyClashes;
	
	public AbstractDependencyTab(Display display) {
		super(display);	
		ImageDescriptor imageDescriptor = ImageDescriptor.createFromFile( DependencyTab.class, "error.gif");
		unresolvedImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( DependencyTab.class, "question-small.png");
		undeterminedImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( DependencyTab.class, "terminal.png");
		leafImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( DependencyTab.class, "warning.png");
		clashedImage = imageDescriptor.createImage();
		
	}
	

	@Override
	public void dispose() {
		unresolvedImage.dispose();
		undeterminedImage.dispose();
		leafImage.dispose();
		super.dispose();
	}
	



	@Override
	protected void initializeTree() {
	}
	@Override
	protected void handleTreeEvent(Event event) {
		final TreeItem item = (TreeItem) event.item;
		TreeItem [] children = item.getItems();
		if (
			(children != null) &&
			(children.length == 1) 
		   ) {
			final TreeItem suspect = children[0];
			String name = suspect.getText(0);
			if (name.equalsIgnoreCase( MARKER_DEFERRED) == false)
				return;
			// 
			display.asyncExec( new Runnable() {
				
				@Override
				public void run() {
					suspect.dispose();
					Dependency dependency = (Dependency) item.getData( DATAKEY_DEPENDENCY);
					buildEntriesForDependencyItem(item, dependency, true);
				}
			});					
		}
	}
				

	
	/**
	 * find the clash looser, i.e. if this dependency is a winner, there might be a looser (unless it's the winner itself)
	 * @param parentSolution - the {@link Solution} that has this {@link Dependency} amongst others 
	 * @param dependency - the {@link Dependency}
	 * @return - the loosing {@link Dependency} or null if it's not a clash
	 */
	private Dependency getClashLooser( Solution parentSolution, Dependency dependency) {
		if (codingMapForDependencyClashes == null)
			return null;
		DependencyContainer container = codingMapForDependencyClashes.get( dependency);
		if (parentSolution == null || container == null) {
			return null;
		}
		else {
			Set<Artifact> winningRequesters = CodingSet.createHashSetBased( new ArtifactWrapperCodec());
			winningRequesters.addAll( dependency.getRequestors());
			for (Dependency looser : container.getDependencies()) {
				for (Artifact loosingRequester : looser.getRequestors()) {
					if (!ArtifactProcessor.identificationEquals(parentSolution, loosingRequester)) {
						continue;
					}
					if (winningRequesters.contains(loosingRequester)) {
						return looser;
					}
				}
			}
			return null;
		}
	}
	
	/**
	 * look up what version eventually was chosen 
	 * @param dependency - the {@link Dependency} to check for 
	 * @return - the {@link String} representation of the {@link Version} of the {@link Solution} chosen for the {@link Dependency}
	 */
	private String getOverridenVersionRange( Solution solution, Dependency dependency) {
		Dependency loosingDependency = getClashLooser(solution, dependency);
		if (loosingDependency == null) {
			return VersionRangeProcessor.toString( dependency.getVersionRange());
		}
		else {
			return VersionRangeProcessor.toString( loosingDependency.getVersionRange());
		}		
	}
	
	protected void buildEntry( Solution solution, Object parent, boolean interactive) {
		List<Dependency> dependencies = new ArrayList<Dependency>( solution.getDependencies());				
		dependencies.sort( new DependencySortComparator());
		buildEntries(parent, interactive, dependencies, solution);
	}
		

	protected void buildEntries(Object parent, boolean interactive, List<Dependency> dependencies, Solution solution) {		
		String pattern = FilterPatternBuilder.getPattern();
		if (dependencies.size() > 0) {
			for (Dependency dependency : dependencies) {
				if (dependency.getExcluded()) {
					continue;
				}
				if (filterActive && pattern != null && pattern.length() > 0) {
					String value = NameParser.buildName(dependency);
					if (!value.matches( pattern))
						continue;
				}			
				TreeItem item;
				if (parent instanceof Tree) {
					item = new TreeItem( (Tree) parent, SWT.NONE);
				} else {
					TreeItem parentItem = (TreeItem) parent;
					item = new TreeItem( parentItem, SWT.NONE);			
				}
				
				List<String> texts = new ArrayList<String>();
				texts.add( dependency.getArtifactId());				
				texts.add( VersionRangeProcessor.toString( dependency.getVersionRange()));
				texts.add( getOverridenVersionRange(solution, dependency));
				texts.add( dependency.getGroupId());
				String classifier = dependency.getClassifier();
				if (classifier == null) {
					classifier = "";
				}
				texts.add( classifier);
				String scope = dependency.getScope();
				if (scope == null)
					scope = "compile";
				texts.add( scope);
				item.setText( texts.toArray( new String[0]));
				
				boolean canHaveChildren = false;
				Image image;
			
				canHaveChildren = true;
				if (dependency.getUndetermined()) {
					image = undeterminedImage;
					// set (and override) the tab's image
					setTabState( DependencyViewTabState.errorState);

				}
				else if (dependency.getUnresolved()) {
					image = unresolvedImage;			
					// set (and override) the tab's image
					setTabState( DependencyViewTabState.errorState);
				}
				else {					
					image = null;
					setTabState( DependencyViewTabState.validState);					
				}
				if (image != null) {
					item.setImage(image);
					item.setData( TreeItemPainter.PainterKey.image.toString(), image);
				}
				item.setData( DATAKEY_DEPENDENCY, dependency);
				
				// 
				if (canHaveChildren) {
					if (interactive) {
						TreeItem dummy = new TreeItem( item, SWT.NONE);
						dummy.setText( MARKER_DEFERRED);
					} 
					else {
						buildEntriesForDependencyItem(item, dependency, interactive);
					}			
				}
			}		
		} 
		else {
			if (parent instanceof TreeItem) {
				TreeItem item = (TreeItem) parent;
				item.setImage(leafImage);
				item.setData( TreeItemPainter.PainterKey.image.toString(), leafImage);
			}
		}
	}
	
	@Override
	protected Collection<String> getRelevantDataKeys() {
		Set<String> result = new HashSet<String>();	
		result.add( DATAKEY_DEPENDENCY);
		return result;
	}
	

	
	@Override
	protected void buildContents(boolean interactive) {
		// translate the clash info to a coding map 
		codingMapForDependencyClashes = CodingMap.createHashMapBased( new DependencyWrapperCodec());		
		codingMapForDependencyClashes.putAll( monitoringResult.getDependencyClashes());		
	}


	protected abstract void buildEntriesForDependencyItem( TreeItem parent, Dependency dependency, boolean interactive);
			

	@Override
	public void scanRepository() {
		TreeItem [] items = tree.getSelection();		
		RepositoryScanLauncher.initiateRepositoryScan(display, items);
	}

	@Override
	public void quickImportArtifact() {			
		TreeItem [] items = tree.getSelection();
		QuickImportLauncher.initiateQuickImport(display, items);
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
	public void acknowledgeProjectImport(final TreeItem item) {
		display.asyncExec( new Runnable() {
			
			@Override
			public void run() {
				item.setData(DATAKEY_PROJECT, null);
				item.setImage( (Image) null);
				item.setData( PainterKey.image.toString(), null);					
			}
		});
	}
	
	@Override
	public void importProject() {
		TreeItem [] items = tree.getSelection();
		ProjectLoader.loadProjects(new TargetProviderImpl(), this, items);
	}
	
	
	
			
}
