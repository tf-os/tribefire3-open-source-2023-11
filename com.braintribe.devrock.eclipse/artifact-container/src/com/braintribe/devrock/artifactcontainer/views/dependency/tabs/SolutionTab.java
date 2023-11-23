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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.TreeItem;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerStatus;
import com.braintribe.devrock.artifactcontainer.container.diagnostics.ClasspathDiagnosticsClassification;
import com.braintribe.devrock.artifactcontainer.container.diagnostics.ContainerClasspathDiagnosticsRegistry;
import com.braintribe.devrock.artifactcontainer.control.walk.ArtifactContainerUpdateRequestType;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.clipboard.ClipboardContentsProviderCapable;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.clipboard.ClipboardContentsProviderMode;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.expansion.ViewExpansionCapable;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.filter.FilterCapable;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.filter.FilterPatternBuilder;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.pom.PomLoader;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.pom.PomLoadingCapable;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.project.ProjectLoader;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.project.ProjectLoadingCapable;
import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.artifact.ArtifactProcessor;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.plugin.commons.SolutionSortComparator;
import com.braintribe.plugin.commons.selection.TargetProviderImpl;
import com.braintribe.plugin.commons.ui.tree.TreeExpander;
import com.braintribe.plugin.commons.ui.tree.TreeItemPainter;


/**
 * a tab showing the solution list of the terminal artifact
 * @author pit
 *
 */
public class SolutionTab extends AbstractDependencyViewTab implements ProjectLoadingCapable, 																	  
																	  ViewExpansionCapable,
																	  FilterCapable,
																	  PomLoadingCapable,
																	  ClipboardContentsProviderCapable
																	 {
	private final static String DATAKEY_SOLUTION = "solution";
	private final static String DATAKEY_DEPENDENCY = "dependency";	
	
	private Image validDocumentImage;
	private Image invalidDocumentImage;
	private Image availableImage;
	private Image terminalImage;
	
	private Image pomAggregateImage;
	private Image pomAggregateAsJarImage;
	private Image jarAsPomAggregateImage;
		
	private boolean condensed = true;
	private boolean filterActive = false;
	private ContainerClasspathDiagnosticsRegistry diagnosticsRegistry = ArtifactContainerPlugin.getArtifactContainerRegistry().getContainerClasspathDiagnosticsRegistry();
	
	public SolutionTab(Display display) {
		super(display);
		setColumnNames( new String [] {"Artifact", "Version", "Group", "Classpath relevancy", "Index" });
		setColumnWeights( new int [] {150, 80, 250, 100, 25} );
		
		ImageDescriptor imageDescriptor = ImageDescriptor.createFromFile( SolutionTab.class, "validation-valid-document.png");
		validDocumentImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( SolutionTab.class, "validation-invalid-document.png");
		invalidDocumentImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( SolutionTab.class, "import.gif");
		availableImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( SolutionTab.class, "terminal.png");
		terminalImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( SolutionTab.class, "info_st_obj.gif");
		pomAggregateImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( SolutionTab.class, "warning_st_obj.gif");
		pomAggregateAsJarImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( SolutionTab.class, "error_st_obj.gif");
		jarAsPomAggregateImage = imageDescriptor.createImage();
		
	}

	
	@Override
	public void dispose() {
		validDocumentImage.dispose();
		invalidDocumentImage.dispose();
		availableImage.dispose();
		terminalImage.dispose();
		pomAggregateImage.dispose();
		pomAggregateAsJarImage.dispose();
		jarAsPomAggregateImage.dispose();
		
		super.dispose();
	}


	@Override
	protected void initializeTree() {
	}
	
	
	@Override
	public void importProject() {
		TreeItem [] items = tree.getSelection();
		ProjectLoader.loadProjects(new TargetProviderImpl(), this, items);
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
	protected void buildContents(boolean interactive) {
		if (interactive) {
			condensed = true;
		}
		boolean hasContent = false;				
		String pattern = FilterPatternBuilder.getPattern();
		List<Solution> solutions = new ArrayList<Solution>( monitoringResult.getSolutions());
		solutions.sort( new SolutionSortComparator());
		for (Solution solution : solutions) {
			if (filterActive && pattern != null && pattern.length() > 0) {
				String value = NameParser.buildName(solution, solution.getVersion());
				if (!value.matches( pattern))
					continue;
			}
			// filter 
			hasContent = true;
			buildTreeItemForSolution(solution, interactive);					
		}				
		if (hasContent) {
			setTabState( DependencyViewTabState.validState);
		}		
	}


	private TreeItem buildTreeItemForSolution(Solution solution, boolean interactive) {
		TreeItem item = new TreeItem( tree, SWT.NONE);
		
		List<String> texts = new ArrayList<String>(4);
		texts.add( solution.getArtifactId());	
		texts.add( VersionProcessor.toString( solution.getVersion()));
		texts.add( solution.getGroupId());
		//		monitoringResult
		ArtifactContainerUpdateRequestType requestType;
		switch (currentContainerMode) {
			case runtime : {			
				requestType = ArtifactContainerUpdateRequestType.launch;
				break;
			}
			default : {
				requestType = ArtifactContainerUpdateRequestType.compile;
				break;
			}
		}
		int imageIndex = 3;
		ClasspathDiagnosticsClassification classpathClassificationForSolution = diagnosticsRegistry.getClasspathClassificationForSolution(currentProject, requestType, solution);
		switch (classpathClassificationForSolution) {
			case pomAsPom : {
				texts.add("aggregate (omitted)");
				//
				item.setImage(imageIndex, pomAggregateImage);
				break;
			}
			case pomAsJar : {
				texts.add("aggregate as jar");
				item.setImage(imageIndex, pomAggregateAsJarImage);
				break;
			}
			case jarAsPom : {
				texts.add("jar as aggregate (omitted)");
				item.setImage(imageIndex, jarAsPomAggregateImage);
				break;
			}			
			case nonJarAsClassesJar: {
				texts.add("aggregate as classes.jar");
				item.setImage(imageIndex, pomAggregateAsJarImage);
				break;
			}
			default:
				texts.add("");
				break;
		}
		// 
		texts.add( "" + solution.getHierarchyLevel());
		item.setText( texts.toArray( new String[0]));
		
		item.setData(DATAKEY_SOLUTION, solution);
		PomLoader.attachPomToTreeItem(item, solution);
		ProjectLoader.markSolutionAsAvailable(solution, item);
		// decide what image
		Image image = null;
		if (solution.getCorrupt()) {
			image = invalidDocumentImage;
			setTabState( DependencyViewTabState.warningState);
		}
		else if (ProjectLoader.markSolutionAsAvailable(solution, item)) {
			image = availableImage;
		}
		else {
			image = validDocumentImage;
		}
		item.setImage(image);		
		item.setData( TreeItemPainter.PainterKey.image.toString(), image);
		
		for (Dependency requestor : solution.getRequestors()) {
			List<Artifact> requestingSolutions = new ArrayList<Artifact>();
			requestingSolutions.addAll(requestor.getRequestors());
			Collections.sort( requestingSolutions, new Comparator<Artifact>() {
				@Override
				public int compare(Artifact o1, Artifact o2) {
					return ArtifactProcessor.compare(o1, o2);
				}				
			});
			for (Artifact requesting : requestingSolutions) {
				buildTreeItemForRequestor(item, (Solution) requesting, interactive);
			}
		}
		return item;
	}
	
	private TreeItem buildTreeItemForRequestor( TreeItem parent, Solution requestor, boolean interactive){
		TreeItem item = new TreeItem( parent, SWT.NONE);

		List<String> texts = new ArrayList<String>(3);
		texts.add( requestor.getArtifactId());		
		texts.add( VersionProcessor.toString( requestor.getVersion()));		
		texts.add( requestor.getGroupId());
		texts.add("");
		texts.add( "" + requestor.getHierarchyLevel());
		item.setText( texts.toArray( new String[0]));
		PomLoader.attachPomToTreeItem(item, requestor);
		
		Image image;
		boolean canHaveChildren = true;
		if (terminalSolution != null && ArtifactProcessor.artifactEquals(requestor, terminalSolution)) {
			image = terminalImage;
			canHaveChildren = false;
		}
		else if (ProjectLoader.isSolutionAvailable( requestor)) {
			image = availableImage;
		}
		else {
			image = validDocumentImage;
		}
		item.setImage( image);
		item.setData( TreeItemPainter.PainterKey.image.toString(), image);
		
		item.setData( DATAKEY_SOLUTION, requestor);	
		
		if (interactive && canHaveChildren) {
			TreeItem dummyItem = new TreeItem( item, SWT.NONE);
			dummyItem.setText( MARKER_DEFERRED);						
		}
		else {
			buildRequesters( item, requestor, interactive);
		}
		return item;
	}
	
	
	private void buildRequesters( TreeItem parent, Solution solution, boolean interactive) {	
		Set<Dependency> requestingDependencies = solution.getRequestors();
		for (Dependency dependency : requestingDependencies) {
			for (Artifact requester : dependency.getRequestors()) {
				Solution requestingSolution = (Solution) requester;								
				buildTreeItemForRequestor(parent, requestingSolution, interactive);				
			}
		}
	}

	

	@Override
	protected Collection<String> getRelevantDataKeys() {
		Set<String> result = new HashSet<String>();
		result.add( DATAKEY_SOLUTION);
		result.add( DATAKEY_PROJECT);
		result.add( DATAKEY_DEPENDENCY);
		result.add( DATAKEY_POM);
		return result;
	}

	@Override
	public void handleTreeEvent(Event event) {
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
					Solution solution = (Solution) item.getData( DATAKEY_SOLUTION);
					buildRequesters(item, solution, true);
				}
			});					
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
				buildContents( false);
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
	public void importPom() {
		TreeItem [] items = tree.getSelection();
		if (items != null) {
			PomLoader.loadPom(items);
		}		
	}


	@Override
	public String apply(ClipboardContentsProviderMode mode) throws RuntimeException {		
		if (
				monitoringResult != null && 
				monitoringResult.getSolutions().size() > 0
			) {
				
				// sort the list by artifact id and group id
			List<Solution> sortedList = new ArrayList<Solution>( monitoringResult.getSolutions());
			
				Collections.sort( sortedList, new SolutionSortComparator());
				String pattern = FilterPatternBuilder.getPattern();
					
				StringBuilder builder = new StringBuilder();
				
							
				for (Solution solution : sortedList) {				

					String name = NameParser.buildName(solution);
					
					if (filterActive && pattern != null && pattern.length() > 0) {					
						if (!name.matches( pattern))
							continue;
					}
					
					if (builder.length() > 0)
						builder.append("\n");
					
					builder.append( name);
					
					if (mode == ClipboardContentsProviderMode.enhanced) {
						builder.append("\n");
						
						Set<Dependency> requesters = solution.getRequestors();
						StringBuilder requesterBuilder = new StringBuilder();
						for (Dependency dependency : requesters) {
							for (Artifact artifact : dependency.getRequestors()) {
								if (requesterBuilder.length() > 0)
									requesterBuilder.append("\n");						
								requesterBuilder.append( "\t" + NameParser.buildName(artifact, artifact.getVersion()));
							}						
						}					
						builder.append(requesterBuilder.toString());
					}
				}
				return builder.toString();
			}
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
		if (monitoringResult != null && monitoringResult.getSolutions().size() > 0) {
			setTabState( DependencyViewTabState.validState);
		}
		else {
			super.broadcastTabState();
		}
	}
	
	
	
}
