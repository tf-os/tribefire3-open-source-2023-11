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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.clipboard.ClipboardContentsProviderCapable;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.clipboard.ClipboardContentsProviderMode;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.expansion.ViewExpansionCapable;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.filter.FilterCapable;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.filter.FilterPatternBuilder;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.pom.PomLoader;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.pom.PomLoadingCapable;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.project.ProjectLoader;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.project.ProjectLoadingCapable;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.PartTuple;
import com.braintribe.model.artifact.PartType;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.part.PartTupleProcessor;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.plugin.commons.SolutionSortComparator;
import com.braintribe.plugin.commons.selection.TargetProviderImpl;
import com.braintribe.plugin.commons.ui.tree.TreeExpander;


public class PartListTab extends AbstractDependencyViewTab implements ProjectLoadingCapable, 
																		PomLoadingCapable,
																		ViewExpansionCapable,
																		FilterCapable,
																		ClipboardContentsProviderCapable {

	private static final String DATAKEY_SOLUTION = "DATAKEY_SOLUTION";
	private boolean condensed = true;
	private boolean filterActive = false;
	
	private Image jarImage;
	private Image sourceImage;
	private Image pomImage;
	private Image javadocImage;	
	private Image warningImage;
	private Image unknownImage;
	private Image importImage;

	public PartListTab(Display display) {
		super(display);
		setColumnNames( new String [] { "Artifact", "Version", "Group", "Type",  "Comment"});
		setColumnWeights( new int [] { 150, 50, 200, 100, 100});
		
		ImageDescriptor imageDescriptor = ImageDescriptor.createFromFile( SolutionTab.class, "solution.jar.gif");
		jarImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( PomLoadingCapable.class, "pom_obj.gif");
		pomImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( SolutionTab.class, "solution.source.gif");
		sourceImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( SolutionTab.class, "solution.javadoc.gif");
		javadocImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( SolutionTab.class, "warning.png");
		warningImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( ProjectLoadingCapable.class, "import.gif");
		importImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( SolutionTab.class, "question-small.png");
		unknownImage = imageDescriptor.createImage();		
	}
	
	

	@Override
	public void dispose() {
		jarImage.dispose();
		pomImage.dispose();
		sourceImage.dispose();
		javadocImage.dispose();
		warningImage.dispose();
		super.dispose();
	}



	@Override
	protected void broadcastTabState() {
		if (!ensureMonitorData()) {
			super.broadcastTabState();
			return;
		}
		if (monitoringResult != null && !monitoringResult.getSolutions().isEmpty()) {
			setTabState( DependencyViewTabState.validState);
		}
		else {
			super.broadcastTabState();
		}
	}

	@Override
	protected Collection<String> getRelevantDataKeys() {
		Set<String> result = new HashSet<String>();
		result.add( DATAKEY_SOLUTION);
		result.add( DATAKEY_PROJECT);
		result.add( DATAKEY_POM);
		return result;
	}


	@Override
	protected void buildContents(boolean interactive) {
		broadcastTabState();
		if (interactive) {
			condensed = true;
		}
		String pattern = FilterPatternBuilder.getPattern();		
		// sort
		List<Solution> sortedList = new ArrayList<Solution>( monitoringResult.getSolutions());		
		Collections.sort( sortedList, new SolutionSortComparator());
		
		for (Solution solution : sortedList) {
			
			// filter
			if (filterActive && pattern != null && pattern.length() > 0) {
				String value = NameParser.buildName(solution, solution.getVersion());
				if (!value.matches( pattern))
					continue;
			}
			
			TreeItem item = new TreeItem( tree, SWT.NONE);
			List<String> texts = new ArrayList<String>();
			texts.add( solution.getArtifactId());		
			texts.add( VersionProcessor.toString( solution.getVersion()));
			texts.add( solution.getGroupId());			
			item.setText( texts.toArray( new String[0]));
			item.setData( DATAKEY_SOLUTION, solution);
			if (ProjectLoader.markSolutionAsAvailable(solution, item)) {
				item.setImage( importImage);
				item.setData( PainterKey.image.toString(), importImage);
			}
			if (interactive) {
				TreeItem deferred = new TreeItem( item, SWT.NONE);
				deferred.setText( MARKER_DEFERRED);
			}
			else {
				attachParts( item, solution);
			}
		}

	}
	
	private void attachParts( TreeItem parent, Solution solution){
		// 
		PartTuple jarTuple = PartTupleProcessor.createJarPartTuple();
		boolean jarFound = false;
		Set<String> processed = new HashSet<>();
		for (Part part : solution.getParts()) {
			if (part.getLocation() == null) {
				continue;
			}
			if (!processed.add( part.getLocation())) {
				System.out.println("already processed [" + part.getLocation() + "]");
				continue;
			}
			TreeItem item = new TreeItem( parent, SWT.NONE);
			PartTuple tuple = part.getType();
			List<String> texts = new ArrayList<String>();
			texts.add( part.getArtifactId());
		
			texts.add( VersionProcessor.toString( part.getVersion()));
			texts.add( part.getGroupId());
			texts.add( PartTupleProcessor.toString(tuple));
			item.setText( texts.toArray( new String[0]));
			assignIcon( item, part);
			if (PartTupleProcessor.equals(tuple, jarTuple)) {
				jarFound = true;
			}
		}
		// only aggregators or pom packaged solution don't need to have a jar
		if (!jarFound) {
			if (	!solution.getAggregator() && // not an aggregator  
					(solution.getPackaging() == null ||  // or not specified -> jar 
					!solution.getPackaging().equalsIgnoreCase( "pom")) // or not marked as pom
				) {
				parent.setImage( warningImage);
				parent.setData( PainterKey.image.toString(), warningImage);
			}
		}
		
	}

	private void assignIcon( TreeItem item, Part part) {
	
		PartType partType = PartTupleProcessor.toPartType( part.getType());
		if (partType != null) {
			switch (partType) {
				case JAR:
					item.setData( PainterKey.image.toString(), jarImage);
					item.setImage( jarImage);					
					return;
				case JAVADOC:
					item.setData( PainterKey.image.toString(), javadocImage);
					item.setImage( javadocImage);
					return;
				case SOURCES:
					item.setData( PainterKey.image.toString(), sourceImage);
					item.setImage( sourceImage);
					return;
				case POM:
					item.setData( PainterKey.image.toString(), pomImage);
					item.setImage( pomImage);
					item.setData( DATAKEY_POM, new File( part.getLocation()));					
					return;
				default:				
					break;
			}
		} 
		else {
			item.setData( "image", unknownImage);
			item.setImage( unknownImage);
		}
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
					Solution solution = (Solution) item.getData( DATAKEY_SOLUTION);
					attachParts( item, solution);
				}
			});					
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
	public void importProject() {
		TreeItem [] items = tree.getSelection();
		ProjectLoader.loadProjects(new TargetProviderImpl(), this, items);
	}
			

	@Override
	public void acknowledgeProjectImport( final TreeItem item) {
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
				tree.removeAll();
				buildContents( false); // fully build tree				
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
				//populateView( true);
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
				if (builder.length() > 0)
					builder.append( System.lineSeparator());

				String name = NameParser.buildName(solution);
				
				if (filterActive && pattern != null && pattern.length() > 0) {					
					if (!name.matches( pattern))
						continue;
				}
								
				builder.append( name);
				builder.append( System.lineSeparator());
									
			}
			return builder.toString();
		}
		return "<no content>";		
	}


	@Override
	public boolean supportsMode(ClipboardContentsProviderMode mode) {
		switch (mode) {
			case standard:
				return true;
			default:
			return false;			
		}
	}	

}
