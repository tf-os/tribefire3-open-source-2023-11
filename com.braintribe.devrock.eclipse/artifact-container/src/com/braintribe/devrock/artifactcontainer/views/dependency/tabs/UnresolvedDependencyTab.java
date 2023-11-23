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
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TreeItem;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerStatus;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.clipboard.ClipboardContentsProviderCapable;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.clipboard.ClipboardContentsProviderMode;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.expansion.ViewExpansionCapable;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.project.ProjectLoader;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.quickimport.QuickImportCapable;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.reposcan.RepositoryScanCapable;
import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.artifact.processing.version.VersionRangeProcessor;
import com.braintribe.plugin.commons.DependencySortComparator;
import com.braintribe.plugin.commons.selection.TargetProviderImpl;
import com.braintribe.plugin.commons.ui.tree.TreeExpander;
import com.braintribe.plugin.commons.ui.tree.TreeItemPainter;


public class UnresolvedDependencyTab extends AbstractDependencyTab implements ViewExpansionCapable, 
																				RepositoryScanCapable, 
																				QuickImportCapable, 
																				ClipboardContentsProviderCapable {
	private Image terminalImage;
	private boolean condensed = true;
	
	public UnresolvedDependencyTab(Display display) {
		super(display);
		setColumnNames( new String [] { "Artifact", "Assigned Version", "Requested Version",  "Group", "Classifier", "Scope"});
		setColumnWeights( new int [] { 150, 50, 50, 150, 50, 50 });
		
		ImageDescriptor imageDescriptor = ImageDescriptor.createFromFile( SolutionTab.class, "terminal.png");
		terminalImage = imageDescriptor.createImage();
	}
	
	

	@Override
	public void dispose() {
		terminalImage.dispose();
		super.dispose();
	}



	@Override
	protected void buildContents(boolean interactive) {
		
		if (interactive) {
			condensed = true;
		}
		Set<Dependency> unresolvedDependencies = monitoringResult.getUnresolvedDependencies();
		if (unresolvedDependencies.size() > 0) {			
			setTabState( DependencyViewTabState.errorState);				
		}
		List<Dependency> dependencies = new ArrayList<Dependency>( unresolvedDependencies);
		dependencies.sort( new DependencySortComparator());
		buildEntries(tree, interactive, dependencies, null);
	}

	@Override
	protected void buildEntriesForDependencyItem( TreeItem parent, Dependency dependency, boolean interactive) {
		Set<Artifact> solutions = dependency.getRequestors();
		for (Artifact artifact : solutions) {
			Solution solution = (Solution) artifact;
			List<Dependency> dependencies = new ArrayList<Dependency>( solution.getRequestors());
			if (dependencies.size() > 0) {
				dependencies.sort( new DependencySortComparator());
				buildEntries( parent, interactive, dependencies, solution);
			}
			else {
				// no requesters -> must be terminal 
				TreeItem item = new TreeItem( parent, SWT.NONE);
				List<String> texts = new ArrayList<String>();
				texts.add( terminalSolution.getArtifactId());
		
				texts.add( VersionProcessor.toString( terminalSolution.getVersion()));		
				texts.add( VersionProcessor.toString( terminalSolution.getVersion()));
				texts.add( terminalSolution.getGroupId());
				texts.add("n/a");
				
				item.setText( texts.toArray( new String[0]));
				item.setData( DATAKEY_DEPENDENCY, dependency);
				
				item.setImage(terminalImage);
				item.setData( TreeItemPainter.PainterKey.image.toString(), terminalImage);
			}
		}
	}



	@Override
	protected void broadcastTabState() {
		if (!ensureMonitorData()) {
			super.broadcastTabState();
			return;
		}
		Set<Dependency> unresolvedDependencies = monitoringResult.getUnresolvedDependencies();
		if (unresolvedDependencies.size() > 0) {			
			setTabState( DependencyViewTabState.errorState);				
		}
		else {
			super.broadcastTabState();
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
	public String apply(ClipboardContentsProviderMode mode) throws RuntimeException {
		Set<Dependency> unresolvedDependencies = monitoringResult.getUnresolvedDependencies();
		if (unresolvedDependencies.size() == 0)
			return null;
		
		switch (mode) {
			case enhanced:
				return "<dependencies>" + unresolvedDependencies.stream().map( d -> {
					return "\t<dependency>"
							+ "\t\t<groupId>" + d.getGroupId() + "</groupId>\n"
							+ "\t\t<artifactId>" + d.getArtifactId() + "</artifactId>\n"
							+ "\t\t<version>" + VersionRangeProcessor.toString( d.getVersionRange()) + "</version>\n"							
							+ "\t</dependency>"; 
					
				}).collect(Collectors.joining("\n")) + "</dependencies>";
			case standard:
			default:
				return unresolvedDependencies.stream().map( d -> NameParser.buildName(d)).collect(Collectors.joining("\n"));						
		}		
	}

	@Override
	public boolean supportsMode(ClipboardContentsProviderMode mode) {	
		return true;
	}
	
	@Override
	public void importProject() {
		TreeItem [] items = tree.getSelection();
		if (items == null) 
			return;
		Set<TreeItem> loadedItems = ProjectLoader.loadProjects(new TargetProviderImpl(), this, items);
		if (loadedItems.size() != items.length) {
			// 
			for (TreeItem item : items) {
				if (!loadedItems.contains( item)) {
					Dependency dependency  = (Dependency) item.getData( DATAKEY_DEPENDENCY);
					String msg = "Dependency [" + NameParser.buildName(dependency) + "] could not be resolved within your declared source repositories";
					ArtifactContainerStatus status = new ArtifactContainerStatus( msg, IStatus.WARNING);
					ArtifactContainerPlugin.getInstance().log(status);		
				}
			}
		}
	}
	
}
